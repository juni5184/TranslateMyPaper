package com.example.translatemypaper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ParsePDFActivity extends AppCompatActivity {

    TextView tvResult;
    String result;
    Button btnTranslate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parse_pdfactivity);

        init();

        Intent intent = getIntent();
        result = intent.getStringExtra("result_data");
        //Log.e("Result", result+"");
        tvResult.setText(result);

        int language = 1; //0 이면 한->영 / 1이면 영->한

        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        String word = tvResult.getText().toString();
                        Menu_papago papago = new Menu_papago();
                        String resultWord;
                        if(language == 0){
                            resultWord= papago.getTranslation(word,"ko","en");
                        }else{
                            resultWord= papago.getTranslation(word,"en","ko");
                        }

                        try {

                            JSONObject jObject = new JSONObject(resultWord);
                            JSONObject parse_response = (JSONObject) jObject.get("message");
                            JSONObject parse_body = (JSONObject) parse_response.get("result");
                            resultWord = parse_body.getString("translatedText");

                            Log.e("translatedText", resultWord+"");

                        }catch(JSONException e){
                            e.printStackTrace();
                        }


                        Bundle papagoBundle = new Bundle();
                        papagoBundle.putString("resultWord",resultWord);

                        Message msg = papago_handler.obtainMessage();
                        msg.setData(papagoBundle);
                        papago_handler.sendMessage(msg);
                    }
                }.start();

            }
        });

    }

    @SuppressLint("HandlerLeak")
    Handler papago_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String resultWord = bundle.getString("resultWord");
            // result_translation.setText(resultWord);
            Intent intent = new Intent(ParsePDFActivity.this, TranslateResultActivity.class);
            intent.putExtra("translateResult", resultWord+"");
            startActivity(intent);
            //Toast.makeText(getApplicationContext(),resultWord,Toast.LENGTH_SHORT).show();
        }
    };

    public void init(){
        tvResult = findViewById(R.id.tv_result);
        btnTranslate = findViewById(R.id.btn_translate);
    }
}