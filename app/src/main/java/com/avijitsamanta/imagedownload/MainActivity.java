package com.avijitsamanta.imagedownload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_KEY = 2020;
    private ImageView imageView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_KEY);
        }

        imageView = findViewById(R.id.imageView);
        Button defaultDownload = findViewById(R.id.defaultDownload);
        Button downloadManager = findViewById(R.id.downloadManager);

        defaultDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://firebasestorage.googleapis.com/v0/b/waldowalpaper.appspot.com/o/Flowers%2F1593496111386.jpg?alt=media&token=8cbfd479-fd52-45af-86c2-8eff257ca0dd";

                new MyDownloadManager().execute(url);
            }
        });

        downloadManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_KEY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MyDownloadManager extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Downloading......");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... str) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(str[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int length = connection.getContentLength();
                InputStream input = new BufferedInputStream(connection.getInputStream(), 1024);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                long read = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    read += count;
                    output.write(data, 0, count);
                    publishProgress((int) (read * 100 / length));

                }
                bitmap = BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.size());

                connection.disconnect();
                output.flush();
                output.close();
                input.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            progressDialog.cancel();

            if (bitmap == null) {
                Toast.makeText(MainActivity.this, "Downloaded file could not be decoded as bitmap", Toast.LENGTH_SHORT).show();
                return;
            }
            imageView.setImageBitmap(bitmap);
            imageView.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));


            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "MY_Images");
            dir.mkdir();
            File myImageFile = new File(dir, System.currentTimeMillis() + ".jpeg");

            try {

                FileOutputStream outputStream = new FileOutputStream(myImageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                Toast.makeText(MainActivity.this, "Image saved as: " + myImageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }
}