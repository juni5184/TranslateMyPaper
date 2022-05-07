package com.example.translatemypaper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    TextView tvPageCount;
    ImageView imgGetPDF;
    Button btnOpenPDF, btnPrev, btnNext, btnRunOCR;

    PdfRenderer renderer;
    int total_pages = 0;
    int display_page = 0;
    public static final int PICK_FILE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        btnOpenPDF = findViewById(R.id.btn_open_pdf);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnRunOCR = findViewById(R.id.btn_run_ocr);

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

        btnRunOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
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

