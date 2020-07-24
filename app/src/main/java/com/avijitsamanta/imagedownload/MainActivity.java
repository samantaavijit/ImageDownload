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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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

import java.net.URL;
import java.net.URLConnection;

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

                new ImageDownload().execute(url);
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

    private class ImageDownload extends AsyncTask<String, Integer, String> {

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
        protected String doInBackground(String... str) {
            File myImageFile = null;
            try {
                URL url = new URL(str[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // getting file length
                int length = connection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "MY_Images");
                dir.mkdir();
                myImageFile = new File(dir, System.currentTimeMillis() + ".jpeg");

                // Output stream to write file
                @SuppressLint("SdCardPath") OutputStream output = new FileOutputStream(myImageFile);
                //img = myImageFile.toString();

                byte[] data = new byte[1024];
                long read = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    read += count;
                    publishProgress((int) (read * 100 / length));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (myImageFile != null)
                return myImageFile.getAbsolutePath();
            else return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.cancel();
            imageView.setImageDrawable(Drawable.createFromPath(s));
        }
    }


}