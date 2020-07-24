package com.avijitsamanta.imagedownload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
    private DownloadManager downloadManager;
    private BroadcastReceiver mDLCompleteReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_KEY);
        }

        imageView = findViewById(R.id.imageView);
        Button defaultDownload = findViewById(R.id.defaultDownload);
        Button viaDownloadManager = findViewById(R.id.downloadManager);

        final String url = "https://firebasestorage.googleapis.com/v0/b/waldowalpaper.appspot.com/o/Flowers%2F1593496111386.jpg?alt=media&token=8cbfd479-fd52-45af-86c2-8eff257ca0dd";

        defaultDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new ImageDownload().execute(url);
            }
        });

        viaDownloadManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadImage(url);
            }
        });
    }


    private void downloadImage(String url) {

        try {
            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            String imageName = System.currentTimeMillis() + ".jpeg";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setTitle("Downloading... " + imageName);
            //request.setDescription("Downloading... " + imageName);

            /* we let the user see the download in a notification */
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            /* set the destination path for this download */
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS +
                    "/MY_Images", imageName);
            /* allow the MediaScanner to scan the downloaded file */
            request.allowScanningByMediaScanner();

            /* this is our unique download id */

            final long DL_ID = downloadManager.enqueue(request);

            mDLCompleteReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (DL_ID == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)) {

                        /* get the path of the downloaded file */
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(DL_ID);
                        Cursor cursor = downloadManager.query(query);
                        if (!cursor.moveToFirst()) {
                            Toast.makeText(context, "Download error: cursor is empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                                != DownloadManager.STATUS_SUCCESSFUL) {
                            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                       // imageView.setImageDrawable(Drawable.createFromPath(path));
                        Toast.makeText(context, "Storage location " + path, Toast.LENGTH_SHORT).show();
                    }
                }
            };
            /* register receiver to listen for ACTION_DOWNLOAD_COMPLETE action */
            registerReceiver(mDLCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDLCompleteReceiver != null)
            unregisterReceiver(mDLCompleteReceiver);
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
                //File dir = new File(Environment.DIRECTORY_DOWNLOADS, "MY_Images");
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
            Toast.makeText(MainActivity.this, "Storage location " + s, Toast.LENGTH_SHORT).show();
        }
    }

}