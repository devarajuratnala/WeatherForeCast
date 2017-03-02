package com.project.weatherforecast.utils;
/**
 * Author       : Devaraju Ratnala
 * Created Date : 03-02-2015
 * Purpose      : This class is used to get data from API
 * APK Version  : 1.0
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.BuildConfig;
import android.util.Log;
import android.widget.Toast;

import com.project.weatherforecast.MainActivity;
import com.project.weatherforecast.R;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public abstract class HttpClient extends AsyncTask<String, String, HttpOutput> {
    ProgressDialog progressDialog;
    Context context;
    MainActivity activity;
    public int loading = 0;

    public HttpClient(Context context, MainActivity activity, ProgressDialog progressDialog) {
        this.context = context;
        this.activity = activity;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        incLoadingCounter();
        if(!progressDialog.isShowing()) {
            progressDialog.setMessage(context.getString(R.string.loading_data));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    @Override
    protected HttpOutput doInBackground(String... params) {
        HttpOutput output = new HttpOutput();
        String response = "";
        String[] coords = new String[]{};

        if (params != null && params.length > 0) {
            String lat = params[1];
            String lon = params[2];
            coords = new String[]{lat, lon};
        }
        if (response.isEmpty()) {
            try {
                URL url = generateURL(coords);
                if (BuildConfig.DEBUG) Log.i("URL", url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader r = new BufferedReader(inputStreamReader);

                    String line = null;
                    while ((line = r.readLine()) != null) {
                        response += line + "\n";
                    }
                    close(r);
                    urlConnection.disconnect();
                    if (BuildConfig.DEBUG) Log.i("Task", "done successfully");
                    output.taskData = TaskData.SUCCESS;
                } else if (urlConnection.getResponseCode() == 429) {
                    if (BuildConfig.DEBUG) Log.i("Task", "too many requests");
                    output.taskData = TaskData.TOO_MANY_REQUESTS;
                } else {
                    if (BuildConfig.DEBUG)
                        Log.i("Task", "bad response " + urlConnection.getResponseCode());
                    output.taskData = TaskData.BAD_RESPONSE;
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) Log.e("IOException Data", response);
                e.printStackTrace();
                output.taskData = TaskData.IO_EXCEPTION;
            }
        }

        if (TaskData.SUCCESS.equals(output.taskData)) {
            ParseResponse parseResponse = parseResponse(response);
            output.parseResponse = parseResponse;
        }

        return output;
    }

    @Override
    protected void onPostExecute(HttpOutput output) {
        if (loading == 1) {
            progressDialog.dismiss();
        }
        decLoadingCounter();

        updateMainUI();

        handleHttpOutput(output);
    }

    protected final void handleHttpOutput(HttpOutput output) {
        switch (output.taskData) {
            case SUCCESS: {
                ParseResponse parseResponse = output.parseResponse;
                if (parseResponse.JSON_EXCEPTION.equals(parseResponse)) {
                    Toast.makeText(context, context.getString(R.string.msg_err_parsing_json), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case TOO_MANY_REQUESTS: {
                Toast.makeText(context, context.getString(R.string.msg_too_many_requests), Toast.LENGTH_LONG).show();
                break;
            }
            case BAD_RESPONSE: {
                Toast.makeText(context, context.getString(R.string.msg_connection_problem), Toast.LENGTH_LONG).show();
                break;
            }
            case IO_EXCEPTION: {
                Toast.makeText(context, context.getString(R.string.msg_connection_not_available), Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    private String getLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals("cs")) {
            language = "cz";
        }
        return language;
    }

    private URL generateURL(String[] coords) throws UnsupportedEncodingException, MalformedURLException {
        String apiKey = activity.getResources().getString(R.string.apiKey);
        StringBuilder urlBuilder = new StringBuilder(Constants.ServiceType.CURRENT_WEATHER_DATA);
        urlBuilder.append(getAPIName()).append("?");
        urlBuilder.append("lat=").append(coords[0]).append("&lon=").append(coords[1]);
        urlBuilder.append("&lang=").append(getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&appid=").append(apiKey);
        return new URL(urlBuilder.toString());
    }

    private static void close(Closeable x) {
        try {
            if (x != null) {
                x.close();
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.e("IOException Data", "Error occurred while closing stream");
        }
    }

    private void incLoadingCounter() {
        loading++;
    }

    private void decLoadingCounter() {
        loading--;
    }

    protected void updateMainUI() {
    }

    protected abstract ParseResponse parseResponse(String response);

    protected abstract String getAPIName();

}