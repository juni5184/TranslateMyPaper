package com.example.translatemypaper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static android.speech.tts.TextToSpeech.ERROR;

import java.util.Locale;

public class ParsePDFActivity extends AppCompatActivity implements ClipboardManager.OnPrimaryClipChangedListener{

    TextView tvResult;
    String result;
    Button btnTranslate, btnSearchWord, btnRead;

    private ClipboardManager clipboardManager;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parse_pdfactivity);

        tvResult = findViewById(R.id.tv_result);
        btnTranslate = findViewById(R.id.btn_translate);
        btnSearchWord = findViewById(R.id.btn_search_word);
        btnRead = findViewById(R.id.btn_read);

        Intent intent = getIntent();
        result = intent.getStringExtra("result_data");
        //Log.e("Result", result+"");
        tvResult.setText(result);

        clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);




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

        btnSearchWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipData pData = clipboardManager.getPrimaryClip();
                ClipData.Item item = pData.getItemAt(0);
                String txtpaste = item.getText().toString();
                Toast.makeText(getApplicationContext(),txtpaste+"",Toast.LENGTH_SHORT).show();
                // Log.e("txtpaste", txtpaste+"");
                String resultWord;

                new Thread(){
                    @Override
                    public void run() {
                        Menu_papago papago = new Menu_papago();
                        String resultWord = papago.getTranslation(txtpaste,"en","ko");
                        try {
                            JSONObject jObject = new JSONObject(resultWord);
                            JSONObject parse_response = (JSONObject) jObject.get("message");
                            JSONObject parse_body = (JSONObject) parse_response.get("result");
                            resultWord = parse_body.getString("translatedText");

                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                        Bundle papagoBundle = new Bundle();
                        papagoBundle.putString("txtpaste",txtpaste);
                        papagoBundle.putString("resultWord",resultWord);


                        Message msg = papago_word_handler.obtainMessage();
                        msg.setData(papagoBundle);
                        papago_word_handler.sendMessage(msg);

                    }
                }.start();


            }
        });


        // TTS를 생성하고 OnInitListener로 초기화 한다.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnRead.getText().equals("STOP")){
                    btnRead.setText("Read Paper");
                    tts.stop();
                }else{
                    tts.speak(tvResult.getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
                    btnRead.setText("STOP");
                }
            }
        });

    }

    void show(String txtpaste, String result)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(txtpaste);
        builder.setMessage(result);
        builder.setPositiveButton("More",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(); intent.setAction(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, txtpaste);
                        startActivity(intent);

                    }
                });
        builder.setNegativeButton("닫기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
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

    @SuppressLint("HandlerLeak")
    Handler papago_word_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String resultWord = bundle.getString("resultWord");
            String txtpaste = bundle.getString("txtpaste");

            show(txtpaste, resultWord);

        }
    };

    @Override
    public void onPrimaryClipChanged() {
        //updateClipData();
        ClipData pData = clipboardManager.getPrimaryClip();
        ClipData.Item item = pData.getItemAt(0);
        String txtpaste = item.getText().toString();
        Toast.makeText(getApplicationContext(),txtpaste+"",Toast.LENGTH_SHORT).show();
        Log.e("txtpaste", txtpaste+"");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

}