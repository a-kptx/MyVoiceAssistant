package com.example.voiceassistent.WeatherAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ForecastApi {
    @GET("/current?access_key=04e9af4401ab6f452216053943f1268f")
    Call<Forecast> getCurrentWeather(@Query("query") String city);
}
