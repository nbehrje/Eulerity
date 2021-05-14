package com.example.eulerity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class GalleryActivity extends AppCompatActivity {
    protected GalleryAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    private ArrayList<String> img_urls;
    GridLayoutManager layoutManager;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mRecyclerView = findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new GalleryAdapter();
        mRecyclerView.setAdapter(mAdapter);
        img_urls = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);

        try {
            new GetImgUrlsTask().execute(new URL(getString(R.string.image_get_url)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected class GetImgUrlsTask extends AsyncTask<URL, Integer, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response = "";
            for (URL u:urls){
                try {
                    if (isCancelled()) break;

                    HttpURLConnection connection = (HttpURLConnection) u.openConnection();
                    InputStream inputStream = connection.getInputStream();
                    response = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        protected void onPostExecute(String result) {
            try {
                JSONArray jsonResult = new JSONArray(result);
                for(int i = 0; i < jsonResult.length(); i++){
                    String url = jsonResult.getJSONObject(i).getString("url");
                    img_urls.add(url);
                    mAdapter.addItem(null, url);
                }
                mAdapter.notifyDataSetChanged();
                new DownloadGalleryTask().execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected class DownloadGalleryTask extends AsyncTask<Void, Bitmap, String> {
        int idx = 0;

        @Override
        protected String doInBackground(Void... params) {
            String data = "";
            for(int i = 0; i < img_urls.size(); i++){
                if (isCancelled()) break;
                try {
                    URL url = new URL(img_urls.get(i));
                    InputStream stream = url.openConnection().getInputStream();
                    Bitmap image = BitmapFactory.decodeStream(stream);
                    stream.close();
                    Bitmap thumb = decodeSampledBitmap(image, 200, 200);
                    publishProgress(thumb);
                } catch(IOException e) {
                    System.out.println(e);
                }

            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Bitmap... data) {
            mAdapter.setImg(idx, data[0]);
            idx++;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.GONE);
        }

        public int calcInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                // Calculate the largest inSampleSize value that is a power of 2 and keeps both height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }
        public Bitmap decodeSampledBitmap(Bitmap image, int reqWidth, int reqHeight) {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG,75,stream);
            byte[] byteArray = stream.toByteArray();
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            // Calculate inSampleSize
            options.inSampleSize = calcInSampleSize(options, reqWidth, reqHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap thumb =  BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
            return thumb;
        }
    }



}