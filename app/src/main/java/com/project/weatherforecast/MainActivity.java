package com.project.weatherforecast;

/**
 * Author       : Devaraju Ratnala
 * Created Date : 03-02-2015
 * Purpose      : This is a Main Activity
 * APK Version  : 1.0
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.project.weatherforecast.adapter.DayForecastAdapter;
import com.project.weatherforecast.models.DayForeCastData;
import com.project.weatherforecast.utils.Common;
import com.project.weatherforecast.utils.HttpClient;
import com.project.weatherforecast.utils.HttpOutput;
import com.project.weatherforecast.utils.ParseResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    //region Constants
    protected static final int PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    protected static final int INTERNET_OPTIONS = 2;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    //endregion

    //region Variable Declarations
    LocationManager mLocationManager;
    ProgressDialog mProgressDialog;
    boolean destroyed = false;
    double mLatitude, mLongitude;
    boolean checkGPS = false;
    boolean checkNetwork = false;
    boolean canGetLocation = false;
    private List<DayForeCastData> mListNextDaysForeCast;
    private List<DayForeCastData> mListTodayForeCast;
    Location mLocation;
    TextView mTextViewCityName, mTextViewWind, mTextViewHumidity, mTextViewVisiblity,
            mTextViewPressure, mTextViewSunset, mTextViewSunrise, mTextViewTemperature,
            mTextViewCurrentDayTab, mTextViewNextDaysTab;
    DayForeCastData mDayForeCastData;
    ImageView mImageViewCurrentWeather;
    ViewFlipper mViewfinderWeatherForecast;
    ListView mListViewDayForecast, mListViewAllDayForeCast;
    //endregion

    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            initVariables();
            initControls();
            loadWeatherData();
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void loadWeatherData() {
        mLocation = getLocation();
        if (mLocation != null) {
            mLatitude = mLocation.getLatitude();
            mLongitude = mLocation.getLongitude();
        }
        if (isNetworkAvailable()) {
            try {
                new DayForeCastDataTask(this, this, mProgressDialog).execute("weather", Double.toString(mLatitude), Double.toString(mLongitude)).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                new NextDaysForeCastDataTask(this, this, mProgressDialog).execute("forecast", Double.toString(mLatitude), Double.toString(mLongitude)).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showNoConnectionDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;

        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(MainActivity.this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mProgressDialog.hide();
        try {
            mLocationManager.removeUpdates(this);
        } catch (SecurityException e) {
            if (BuildConfig.DEBUG)
                Log.e("LocationManager", "Error while trying to stop listening for location updates. This is probably a permissions issue", e);
        }
        if (BuildConfig.DEBUG)
            Log.i("LOCATION (" + location.getProvider().toUpperCase() + ")", location.getLatitude() + ", " + location.getLongitude());
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_FINE_LOCATION:
                getLocation();
                break;
            case INTERNET_OPTIONS:
                loadWeatherData();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_current_day_tab:
                mViewfinderWeatherForecast.showPrevious();
                mTextViewCurrentDayTab.setBackgroundResource(R.color.colorPrimaryDark);
                mTextViewNextDaysTab.setBackgroundResource(R.color.colorPrimary);

                break;
            case R.id.text_view_next_day_tab:
                mViewfinderWeatherForecast.showNext();
                mTextViewCurrentDayTab.setBackgroundResource(R.color.colorPrimary);
                mTextViewNextDaysTab.setBackgroundResource(R.color.colorPrimaryDark);
                break;
        }
    }
    //endregion

    //region Public Methods
    public ParseResponse parseNextDayResponse(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                if (mListNextDaysForeCast == null) {
                    mListNextDaysForeCast = new ArrayList<>();
                    mListTodayForeCast = new ArrayList<>();
                }
                return ParseResponse.CITY_NOT_FOUND;
            }

            mListNextDaysForeCast = new ArrayList<>();
            mListTodayForeCast = new ArrayList<>();

            JSONArray list = reader.getJSONArray("list");
            for (i = 0; i < list.length(); i++) {
                DayForeCastData dayForeCastData = new DayForeCastData();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");
                dayForeCastData.setDate(listItem.getString("dt"));
                dayForeCastData.setTemperature(main.getString("temp") + " " + (char) 0x00B0 + "C");
                dayForeCastData.setTemperaturemax(main.getString("temp_max") + " " + (char) 0x00B0 + "C");
                dayForeCastData.setTemperaturemin(main.getString("temp_min") + " " + (char) 0x00B0 + "C");
                dayForeCastData.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                if (windObj != null) {
                    dayForeCastData.setWind(windObj.getString("speed") + " m/s");
                    dayForeCastData.setWindDirectionDegree(windObj.getDouble("deg"));
                }
                dayForeCastData.setPressure(main.getString("pressure") + " hpa");
                dayForeCastData.setHumidity(main.getString("humidity") + " %");

                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                if (rainObj != null) {
                    rain = getRainString(rainObj);
                } else {
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        rain = getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                dayForeCastData.setRain(rain);

                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                dayForeCastData.setId(idString);

                final String dateMsString = listItem.getString("dt") + "000";
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(dateMsString));
                dayForeCastData.setIcon(setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), getApplicationContext()));
                Calendar today = Calendar.getInstance();
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    mListTodayForeCast.add(dayForeCastData);
                } else {
                    mListNextDaysForeCast.add(dayForeCastData);
                }
            }

        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResponse.JSON_EXCEPTION;
        }

        return ParseResponse.OK;
    }

    public static String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }
    //endregion

    //region Private Methods
    private void initVariables() {
        mDayForeCastData = new DayForeCastData();
        mListNextDaysForeCast = new ArrayList<>();
        mListTodayForeCast = new ArrayList<>();
    }

    private void initControls() {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mTextViewCityName = (TextView) findViewById(R.id.text_view_City_Name);
        mTextViewWind = (TextView) findViewById(R.id.text_view_wind);
        mTextViewHumidity = (TextView) findViewById(R.id.text_view_humidity);
        mTextViewVisiblity = (TextView) findViewById(R.id.text_view_visiblity);
        mTextViewPressure = (TextView) findViewById(R.id.text_view_pressure);
        mTextViewSunset = (TextView) findViewById(R.id.text_view_sunset);
        mTextViewSunrise = (TextView) findViewById(R.id.text_view_sunrise);
        mTextViewTemperature = (TextView) findViewById(R.id.text_view_temperature);
        mImageViewCurrentWeather = (ImageView) findViewById(R.id.image_current_weather);
        mTextViewCurrentDayTab = (TextView) findViewById(R.id.text_view_current_day_tab);
        mTextViewCurrentDayTab.setOnClickListener(this);
        mTextViewNextDaysTab = (TextView) findViewById(R.id.text_view_next_day_tab);
        mTextViewNextDaysTab.setOnClickListener(this);
        mViewfinderWeatherForecast = (ViewFlipper) findViewById(R.id.viewflipper_weather_forecast);
        mListViewDayForecast = (ListView)findViewById(R.id.listview_currentday_forecast);
        mListViewAllDayForeCast = (ListView)findViewById(R.id.listview_nextday_forecast);
    }
    private Location getLocation() {
        Location location = null;
        try {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            checkGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            checkNetwork = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!checkGPS && !checkNetwork) {
                showLocationSettingsDialog();
                location = null;
            }
            this.canGetLocation = true;
            if (checkNetwork) {
                try {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (mLocationManager != null) {
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    }

                   /* if (mLocation != null) {
                        location = mLocation;
                        mLatitude = mLocation.getLatitude();
                        mLongitude = mLocation.getLongitude();
                        getCityName();
                        status = true;
                    }*/
                } catch (SecurityException e) {
                    e.printStackTrace();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}, PERMISSIONS_ACCESS_FINE_LOCATION);
                }
            } else if (checkGPS) {
                if (mLocation == null) {
                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (mLocationManager != null) {
                            location = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                           /* if (mLocation != null) {
                                mLatitude = mLocation.getLatitude();
                                mLongitude = mLocation.getLongitude();
                                getCityName();
                                status = true;
                            }*/
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}, PERMISSIONS_ACCESS_FINE_LOCATION);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }
    private void showLocationSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.location_settings);
        alertDialog.setMessage(R.string.location_settings_message);
        alertDialog.setPositiveButton(R.string.location_settings_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, PERMISSIONS_ACCESS_FINE_LOCATION);
            }
        });
        alertDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int grantResult = grantResults[0];
        if (grantResult != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
        } else {
            getLocation();
        }

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void updateForecastUI() {
        DayForecastAdapter dayForecastAdapter = new DayForecastAdapter(MainActivity.this, mListTodayForeCast);
        mListViewDayForecast.setAdapter(dayForecastAdapter);
        DayForecastAdapter daysForecastAdapter = new DayForecastAdapter(MainActivity.this, mListNextDaysForeCast);
        mListViewAllDayForeCast.setAdapter(daysForecastAdapter);
    }

    private void updateDayWeatherUI() {
        try {
           // DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
            mTextViewCityName.setText(mDayForeCastData.getCity() + ", " + mDayForeCastData.getCountry());
            mTextViewWind.setText(mDayForeCastData.getWind());
            mTextViewHumidity.setText(mDayForeCastData.getHumidity());
            mTextViewPressure.setText(mDayForeCastData.getPressure());
            mTextViewSunset.setText(Common.FormatTime(mDayForeCastData.getSunset(),MainActivity.this));
            mTextViewSunrise.setText(Common.FormatTime(mDayForeCastData.getSunrise(),MainActivity.this));
            mTextViewTemperature.setText(mDayForeCastData.getTemperature());
            mTextViewVisiblity.setText(mDayForeCastData.getVisiblity());
            mImageViewCurrentWeather.setBackground(mDayForeCastData.getIcon());
        } catch (Exception e) {
            e.getMessage();

        }

    }
    private ParseResponse parseCurrentDayJson(String result) {
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResponse.CITY_NOT_FOUND;
            }

            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                mDayForeCastData.setSunrise(countryObj.getString("sunrise"));
                mDayForeCastData.setSunset(countryObj.getString("sunset"));
            }
            mDayForeCastData.setCity(city);
            mDayForeCastData.setCountry(country);
            JSONObject main = reader.getJSONObject("main");
            mDayForeCastData.setTemperature(main.getString("temp") + " " + (char) 0x00B0 + "C");
            mDayForeCastData.setTemperaturemax(main.getString("temp_max"));
            mDayForeCastData.setTemperaturemin(main.getString("temp_min"));
            mDayForeCastData.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            JSONObject windObj = reader.getJSONObject("wind");
            mDayForeCastData.setWind(windObj.getString("speed") + " m/s");
            if (windObj.has("deg")) {
                mDayForeCastData.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                if (BuildConfig.DEBUG) Log.e("parseTodayJson", "No wind direction available");
                mDayForeCastData.setWindDirectionDegree(null);
            }
            mDayForeCastData.setPressure(main.getString("pressure") + " hpa");
            mDayForeCastData.setHumidity(main.getString("humidity") + " %");
            mDayForeCastData.setVisiblity("850");
            mDayForeCastData.setIcon(setWeatherIcon(Integer.parseInt(reader.optJSONArray("weather").getJSONObject(0).getString("id")), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), getApplicationContext()));

        } catch (JSONException e) {
            if (BuildConfig.DEBUG) Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResponse.JSON_EXCEPTION;
        }

        return ParseResponse.OK;
    }
    private Drawable setWeatherIcon(int actualId, int hourOfDay, Context context) {
        int id = actualId / 100;
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_sunrise);
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 17) {
                icon = ContextCompat.getDrawable(context, R.drawable.ic_sunrise);
            } else if (hourOfDay >= 17 && hourOfDay < 20) {
                icon = ContextCompat.getDrawable(context, R.drawable.ic_sunset);
            } else {
                icon = ContextCompat.getDrawable(context, R.drawable.ic_moon);
            }
        } else {
            switch (id) {
                case 2:
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_thunder);
                    break;
                case 3:
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_drizzle);
                    break;
                case 7:
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_foggy);
                    break;
                case 8:
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_cloud);
                    break;
                case 6:
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_foggy);
                    break;
                case 5:
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_drizzle);
                    break;
            }
        }
        return icon;
    }

    public void showNoConnectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setMessage(R.string.msg_connection_not_available);
        builder.setTitle("Connection Error");
        builder.setPositiveButton(R.string.location_settings_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), INTERNET_OPTIONS);
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                return;
            }
        });

        builder.show();
    }
    //endregion

    //region Http Classes
    private class DayForeCastDataTask extends HttpClient {

        public DayForeCastDataTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() {
            loading = 0;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(HttpOutput output) {
            super.onPostExecute(output);
        }

        @Override
        protected ParseResponse parseResponse(String response) {
            return parseCurrentDayJson(response);
        }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected void updateMainUI() {
            updateDayWeatherUI();
        }
    }

    private class NextDaysForeCastDataTask extends HttpClient {

        public NextDaysForeCastDataTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() {
            loading = 0;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(HttpOutput output) {
            super.onPostExecute(output);
        }

        @Override
        protected ParseResponse parseResponse(String response) {
            return parseNextDayResponse(response);
        }

        @Override
        protected String getAPIName() {
            return "forecast";
        }

        @Override
        protected void updateMainUI() {
            updateForecastUI();
        }
    }
    //endregion
}
