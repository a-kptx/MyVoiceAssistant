package com.example.voiceassistent.RecycleView;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voiceassistent.R;

import java.text.SimpleDateFormat;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    protected TextView messageText;
    protected TextView messageDate;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.messageTextView);
        messageDate = itemView.findViewById(R.id.messageDateView);
    }

    public void bind(Message message) {
        messageText.setText(message.text);
        messageDate.setText((new SimpleDateFormat("HH:mm dd.MM.YYYY")).format(message.date));
    }
}
