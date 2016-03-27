package com.hendiware.uploadfileexample;
// www.hendiware.com

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    String serverURL = "http://developerhendy.16mb.com/uploadfile.php";
    TextView responseTextView;
    HttpURLConnection uploadConnection = null;
    DataOutputStream outputStream;
    String boundary = "**hee**ndiii**wareee***";
    String CRLF = "\r\n";
    String Hyphens = "--";
    int bytesRead, bytesAvailable, bufferSize;
    int maxBufferSize = 1024 * 1024;
    byte[] buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        responseTextView = (TextView) findViewById(R.id.responseText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent, 10);
                    Log.e("ss", serverURL);
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String filePath = getPathfromURI(uri);
            Log.e("File Path :", filePath);
            hendiwareFileUpload(filePath);
        }
    }

    private void hendiwareFileUpload(final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection uploadConnection = null;
                DataOutputStream outputStream;
                String boundary = "********";
                String CRLF = "\r\n";
                String Hyphens = "--";
                int bytesRead, bytesAvailable, bufferSize;
                int maxBufferSize = 1024 * 1024;
                byte[] buffer;
                File ourFile = new File(filePath);

                try {
                    FileInputStream fileInputStream = new FileInputStream(ourFile);
                    URL url = new URL(serverURL);
                    uploadConnection = (HttpURLConnection) url.openConnection();
                    uploadConnection.setDoInput(true);
                    uploadConnection.setDoOutput(true);
                    uploadConnection.setRequestMethod("POST");

                    uploadConnection.setRequestProperty("Connection", "Keep-Alive");
                    uploadConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    uploadConnection.setRequestProperty("uploaded_file", filePath);

                    outputStream = new DataOutputStream(uploadConnection.getOutputStream());

                    outputStream.writeBytes(Hyphens + boundary + CRLF);

                    outputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + filePath + "\"" + CRLF);
                    outputStream.writeBytes(CRLF);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        outputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    outputStream.writeBytes(CRLF);
                    outputStream.writeBytes(Hyphens + boundary + Hyphens + CRLF);

                    InputStreamReader resultReader = new InputStreamReader(uploadConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(resultReader);
                    ;
                    String line = "";
                    String response = "";
                    while ((line = reader.readLine()) != null) {
                        response += line;
                    }

                    final String finalResponse = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            responseTextView.setText(finalResponse);
                        }
                    });

                    fileInputStream.close();
                    outputStream.flush();
                    outputStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
        }).start();
    }


    public String getPathfromURI(Uri uri) {
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
