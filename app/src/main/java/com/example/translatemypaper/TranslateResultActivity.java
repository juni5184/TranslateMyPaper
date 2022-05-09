package com.example.translatemypaper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TranslateResultActivity extends AppCompatActivity {

    TextView tvTranslateResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_result);

        tvTranslateResult = findViewById(R.id.tv_translate_result);

        Intent intent = getIntent();
        Log.e("log",intent.getStringExtra("translateResult")+"");
        tvTranslateResult.setText(intent.getStringExtra("translateResult"));


    }
}