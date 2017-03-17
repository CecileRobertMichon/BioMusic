package com.dp.cecile.biomusic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by cecile on 2017-03-17.
 */

public class SessionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_details_page);

        Intent intent = getIntent();

        String date = intent.getStringExtra("date");
        String emotion = intent.getStringExtra("emotion");
        String comment = intent.getStringExtra("comment");

        TextView date_text = (TextView) findViewById(R.id.details_date);
        TextView emotion_text = (TextView) findViewById(R.id.details_emotion);
        TextView comment_text = (TextView) findViewById(R.id.details_comment);

        date_text.setText(date);
        emotion_text.setText(emotion);
        comment_text.setText(comment);

    }
}
