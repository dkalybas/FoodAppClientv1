package com.example.newfoodapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class AboutUsActivity extends AppCompatActivity {


    TextView aboutUsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about_us);

        aboutUsTextView = (TextView)findViewById(R.id.aboutUs);

    }
}