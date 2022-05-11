package com.example.translatemypaper;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    TextView tvPageCount;
    ImageView imgGetPDF;
    Button btnOpenPDF, btnPrev, btnNext, btnExtractPDF;

    //PDF 이미지 잘 추출되는지 테스트용
    //ImageView img = (ImageView)findViewById(R.id.imageView);

    PdfRenderer renderer;
    int total_pages = 0;
    int display_page = 0;
    public static final int PICK_FILE = 99;

    Uri uri;

//    private TessBaseAPI mTess; //Tess API reference
//    String datapath = "" ; //언어데이터가 있는 경로

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenPDF = findViewById(R.id.btn_open_pdf);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnExtractPDF = findViewById(R.id.btn_extract_pdf);

        tvPageCount = findViewById(R.id.tv_cnt);
        imgGetPDF = findViewById(R.id.img_get_pdf);

        // PDF open
        btnOpenPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                startActivityForResult(intent, PICK_FILE);
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // move to previous page
                if (display_page > 0) {
                    display_page--;
                    _display(display_page);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // move to next page
                if (display_page < (total_pages - 1)) {
                    display_page++;
                    _display(display_page);
                }
            }
        });


        btnExtractPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                BitmapDrawable imgBitmapDrawable = (BitmapDrawable) imgGetPDF.getDrawable();
//                Bitmap imgNowPDF = imgBitmapDrawable.getBitmap();
                //이미지 잘 추출되는지 확인 -> 잘됨
                //img.setImageBitmap(imgNowPDF);

                //img process
                String parseResult = extractPDF();
                //Log.e("result", parseResult+"");


                //데이터 담아서 액티비티 호출
                Intent intent = new Intent(MainActivity.this, ParsePDFActivity.class);
                intent.putExtra("result_data", parseResult+"");
                //Log.e("result", parseResult+"");
                startActivity(intent);

            }
        });


    }

    private String extractPDF() {
        try {
            // creating a string for
            // storing our extracted text.
            String extractedText = "";

            // creating a variable for pdf reader
            // and passing our PDF file in it.
            PdfReader reader = new PdfReader("res/raw/thelastleaf1page.pdf");
//            Log.i("uri", uri+"");
//            PdfReader reader = new PdfReader(uri+"");

            // below line is for getting number
            // of pages of PDF file.
            int n = reader.getNumberOfPages();

            // running a for loop to get the data from PDF
            // we are storing that data inside our string.
            for (int i = 0; i < n; i++) {
                extractedText = extractedText + PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + " \n";
                // to extract the PDF content from the different pages
            }

            // below line is used for closing reader.
            reader.close();

            // after extracting all the data we are
            // setting that string value to our text view.
            return extractedText;

        } catch (Exception e) {
            // for handling error while extracting the text file.
            return ("Error found is : \n" + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                uri = data.getData();
                Log.e("uri",uri+"");
                try {
                    ParcelFileDescriptor parcelFileDescriptor = getContentResolver()
                            .openFileDescriptor(uri, "r");
                    renderer = new PdfRenderer(parcelFileDescriptor);
                    total_pages = renderer.getPageCount();
                    display_page = 0;
                    _display(display_page);
                } catch (FileNotFoundException fnfe) {

                } catch (IOException e) {

                }
            }
        }
    }

    // 화면 전환
    private void _display(int _n) {
        if (renderer != null) {
            PdfRenderer.Page page = renderer.openPage(_n);
            Bitmap mBitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            // 이미지 set
            imgGetPDF.setImageBitmap(mBitmap);
            page.close();
            tvPageCount.setText((_n + 1) + "/" + total_pages);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (renderer != null) {
            renderer.close();
        }
    }


}

