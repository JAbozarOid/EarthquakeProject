package com.aratel.earthquake.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aratel.earthquake.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

// this class is used for checking network connection
public class MyViewModel extends AndroidViewModel {

    private static final String TAG = "MyViewModel";

    private MutableLiveData<String> data;

    public MyViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getData() {
        if (data == null) {
            data = new MutableLiveData<String>();
            loadData();
        }

        return data;
    }

    private void loadData() {
        new AsyncTask<String,Void,String>(){

            @Override
            protected String doInBackground(String... strings) {
                String result = null;
                //String myFeed = getApplication().getString(R.string.app_feed);
                try{
                    URL url = new URL("https://www.android.com/");

                    // create a new HTTP URL connection
                    URLConnection connection = url.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;

                    int responseCode = httpConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream in = httpConnection.getInputStream();
                        // process the input stream to generate out result list
                        result = processStream(in);
                    }else {
                        result = null;
                    }
                    httpConnection.disconnect();
                } catch (MalformedURLException e) {
                    Log.e(TAG,"Malformed URL Exception." , e);
                } catch (IOException e) {
                    Log.e(TAG,"IO Exception." , e);
                }
                return result;
            }

            @Override
            protected void onPostExecute(String strings) {
                //update the live data value
                if(strings != null){
                    data.setValue(strings);
                }else{
                    data.setValue(null);
                }
            }
        }.execute();
    }

    private String processStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }


}
