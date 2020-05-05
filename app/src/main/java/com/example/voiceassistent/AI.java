package com.example.voiceassistent;
import com.example.voiceassistent.HolidayAPI.ParsingHtmlService;
import com.example.voiceassistent.NumberConverterAPI.ConvertNumberToString;
import com.example.voiceassistent.WeatherAPI.ForecastToString;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AI {
    public static Map<String, String> answerDictionary = new HashMap<String, String>() {{
        put("привет", "Привет!");
        put("как дела", "Прекрасно!");
        put("чем занимаешься", "Отвечаю на вопросы");
    }};

    static private String howManyDaysTo(String question) {
        int ind = question.indexOf("дней до");
        ind += 8;
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", new Locale("ru"));
        String str = question.substring(ind,ind+10).toString();
        Date endDate= null;
        try {
            endDate = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diffInMillies = endDate.getTime() - curDate.getTime();
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return String.valueOf(diff + 1);
    }
    public static String getGradusEnding(int n) {
        n = (n < 0) ? -n : n;
        return (n % 100 >= 11 && n % 100 <= 19) || n % 10 == 0 || n % 10 > 4 ? "ов" : n % 10 == 1 ? "" : "a";
    }
    private static String modify(String date) {
        String[] mas = date.split(" ");
        Matcher matcher = Pattern.compile("0\\d").matcher(mas[0]);
        if (matcher.find()) {
            return date.substring(1);
        } else {
            return date;
        }
    }
    public static String getDate(String question) throws ParseException {
        Date tmp;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", dateFormatSymbols);
        String tmpQuestion = question.replace("праздник ", "");;
        String[] dates = tmpQuestion.split(",");
        for (int i = 0; i < dates.length; i++) {
            if (dates[i].contains("вчера")) {
                calendar.add(Calendar.DAY_OF_YEAR, - 1);
                tmp = calendar.getTime();
                dates[i] = sdf.format(tmp) + ",";
                calendar.add(Calendar.DAY_OF_YEAR, + 1);
            } else if (dates[i].contains("сегодня")) {
                tmp = calendar.getTime();
                dates[i] = sdf.format(tmp) + ",";
            } else if (dates[i].contains("завтра")) {
                calendar.add(Calendar.DAY_OF_YEAR, + 1);
                tmp = calendar.getTime();
                dates[i] = sdf.format(tmp);
                calendar.add(Calendar.DAY_OF_YEAR, - 1);
            } else {
                String pattern = "\\d{1,2}\\.\\d{1,2}\\.\\d{4}";
                Matcher matcher = Pattern.compile(pattern).matcher(dates[i]);
                if (matcher.find()) {
                    dates[i] = sdf.format(
                            Objects.requireNonNull(new SimpleDateFormat("dd.MM.yyyy").
                                    parse(dates[i].substring(matcher.start(), matcher.end()))));
                    dates[i] = modify(dates[i]);
                }
            }
        }
        return String.join(",", dates);
    }
    private static DateFormatSymbols dateFormatSymbols = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
    };
    static void getAnswer(String question, final Consumer<String> callback) throws ParseException {
        question = question.toLowerCase();
        if (question.contains("привет")) {
            callback.accept(answerDictionary.get("привет"));
        }
        else if (question.contains("как дела")) {
            callback.accept(answerDictionary.get("как дела"));
        }
        else if (question.contains("чем занимаешься") || question.contains("что делаешь")) {
            callback.accept(answerDictionary.get("чем занимаешься"));
        }
        else if (question.contains("перевод")) {
            final String number = question.replaceAll("[^0-9+]", "");
            ConvertNumberToString.getConvertNumber(number, callback);
        }
        else if (question.contains("какой день")) {
            Date currentDate = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            callback.accept(dateFormat.format(currentDate));
        }
        else if (question.contains("который час") || question.contains("время")) {
            Date currentDate = new Date();
            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            callback.accept(timeFormat.format(currentDate));
        }
        else if (question.contains("день недели")) {
            Date currentDate = new Date();
            DateFormat timeFormat = new SimpleDateFormat("u", Locale.getDefault());
            String answer;
            switch (timeFormat.format(currentDate)) {
                case "1":
                    answer = "Понедельник";
                    break;
                case "2":
                    answer = "Вторник";
                    break;
                case "3":
                    answer = "Среда";
                    break;
                case "4":
                    answer = "Четверг";
                    break;
                case "5":
                    answer = "Пятница";
                    break;
                case "6":
                    answer = "Суббота";
                    break;
                case "7":
                    answer = "Воскресенье";
                    break;
                default:
                    answer = "Я не знаю какой день недели";
                    break;
            }
            callback.accept(answer);
        }
        else if (question.contains("дней до")) {
            String answer = howManyDaysTo(question);
            callback.accept(answer);
        }
        else if (question.contains("праздник")) {
            String date = getDate(question);
            String[] strings = date.split(",");
            Observable.fromCallable(() -> {
                String result = "";
                for (String str : strings) {
                    try {
                        result += " " + str + ": " + ParsingHtmlService.getHolyday(str) + "\n";
                    } catch (IOException e) {
                        result += " " + str + ": Я не знаю ответ :(";
                    }
                }
                return result;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(callback::accept);
        }
        else if (question.contains("погода")) {
            Pattern cityPattern = Pattern.compile("погода в городе (\\p{L}+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = cityPattern.matcher(question);
            if (matcher.find()) {
                String cityName = matcher.group(1);
                ForecastToString.getForecast(cityName, s -> {
                    callback.accept(s);
                });
            }
        }
        else {
            callback.accept("Задайте другой вопрос!");
        }

        for(Map.Entry<String, String> item : answerDictionary.entrySet()) {
            if (question.contains(item.getKey())) {
                callback.accept(item.getValue());
            }
        }
    }
}
