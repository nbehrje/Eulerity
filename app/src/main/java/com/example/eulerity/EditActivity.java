package com.example.eulerity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class EditActivity extends AppCompatActivity {
    EditView editView;
    String img_url;
    Button grayButton, resetButton, sepiaButton, invertButton, textButton, uploadButton;
    ToggleButton brushButton;
    EditText inputEditText;
    RadioGroup textColorGroup, brushColorGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        editView = (EditView) findViewById(R.id.editView);

        grayButton = (Button) findViewById(R.id.grayButton);
        grayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editView.setFilter(Filter.GRAYSCALE);
            }
        });

        sepiaButton = (Button) findViewById(R.id.sepiaButton);
        sepiaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editView.setFilter(Filter.SEPIA);
            }
        });

        invertButton = (Button) findViewById(R.id.invertButton);
        invertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editView.setFilter(Filter.INVERT);
            }
        });

        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editView.reset();
            }
        });

        textButton = (Button) findViewById(R.id.textButton);
        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder.setTitle("Text Overlay");
                View overlayView = getLayoutInflater().inflate(R.layout.text_overlay_layout,null);
                builder.setView(overlayView);
                builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputEditText = (EditText) overlayView.findViewById(R.id.textOverlay);
                        textColorGroup = (RadioGroup) overlayView.findViewById(R.id.brushRadioGroup);
                        String inputStr = inputEditText.getText().toString();
                        if(inputStr.length() > 0) {
                            int colorId = textColorGroup.getCheckedRadioButtonId();
                            RadioButton radioButton = (RadioButton) overlayView.findViewById(colorId);
                            editView.setText(inputStr, radioButton.getText().toString());

                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        brushButton = (ToggleButton) findViewById(R.id.brushButton);
        brushButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                    builder.setTitle("Brush Color");
                    View overlayView = getLayoutInflater().inflate(R.layout.brush_layout,null);
                    brushColorGroup = (RadioGroup) overlayView.findViewById(R.id.brushRadioGroup);
                    builder.setView(overlayView);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            int colorId = brushColorGroup.getCheckedRadioButtonId();
                            RadioButton radioButton = (RadioButton) overlayView.findViewById(colorId);
                            editView.setBrushColor(radioButton.getText().toString());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            brushButton.setChecked(false);
                            editView.setBrushColor(null);
                        }
                    });
                    builder.show();
                } else{
                    editView.setBrushColor(null);
                }
            }
        });

        uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadImageTask().execute();
            }
        });

        Intent i = getIntent();
        img_url = i.getStringExtra("url");

        new LoadCanvasTask().execute(img_url);
    }

    protected class LoadCanvasTask extends AsyncTask<String, Void, Bitmap> {
        Bitmap image = null;
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                InputStream stream = url.openConnection().getInputStream();
                image = BitmapFactory.decodeStream(stream);
                stream.close();
                return image;
            } catch(IOException e) {
                System.out.println(e);
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap img) {
            if(img != null) {
                editView.setImage(img);
                grayButton.setEnabled(true);
                resetButton.setEnabled(true);
                sepiaButton.setEnabled(true);
                invertButton.setEnabled(true);
                textButton.setEnabled(true);
                brushButton.setEnabled(true);
                uploadButton.setEnabled(true);
            }
        }
    }

    protected class UploadImageTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try{
                URL url = new URL(getString(R.string.image_upload_url));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                response = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
                JSONObject jsonResult = new JSONObject(response);

                String uploadUrl = jsonResult.getString("url");
                String crlf = "\r\n";
                String dashes = "--";
                String boundary =  "****";
                url = new URL(uploadUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                DataOutputStream request = new DataOutputStream(connection.getOutputStream());
                request.writeBytes(dashes+boundary+crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"appid\""+ crlf);
                request.writeBytes("nbehrje"+ crlf + boundary);
                request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"untitled.bmp\""+ crlf);
                request.write(editView.getBytes());
                request.writeBytes(crlf+boundary);
                request.writeBytes("Content-Disposition: form-data; name=\"original\""+ crlf);
                request.writeBytes(img_url+ crlf + boundary);
                request.writeBytes(boundary + dashes);
                request.flush();
                request.close();

                int status = connection.getResponseCode();
                InputStream responseStream = new BufferedInputStream(connection.getInputStream());
                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();
                String postResponse = stringBuilder.toString();
                return postResponse;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }
    }
}