package com.project.weatherforecast;

/**
 * Author       : Devaraju Ratnala
 * Created Date : 03-02-2015
 * Purpose      : This is a Main Activity
 * APK Version  : 1.0
 */

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    LocationManager mLocationManager;
    ProgressDialog mProgressDialog;
    protected static final int PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    private static final int UPDATE_NOT_REQUIRED_THRESHOLD = 300000;
    boolean destroyed = false;
    double mLatitude, mLongitude;
    boolean checkGPS = false;
    boolean checkNetwork = false;
    boolean canGetLocation = false;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    Location loc;
    TextView cityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariables();
        if (getLocation()) {

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

    void initVariables() {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        cityName = (TextView) findViewById(R.id.text_City_Name);
    }

    private boolean getLocation() {
        boolean status = false;
        try {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            checkGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            checkNetwork = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!checkGPS && !checkNetwork) {
                showLocationSettingsDialog();
                status = false;
            }
            this.canGetLocation = true;
            if (checkNetwork) {
                try {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (mLocationManager != null) {
                        loc = mLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    }

                    if (loc != null) {
                        mLatitude = loc.getLatitude();
                        mLongitude = loc.getLongitude();
                        getCityName();
                        status = true;
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}, PERMISSIONS_ACCESS_FINE_LOCATION);
                }
            } else if (checkGPS) {
                if (loc == null) {
                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (mLocationManager != null) {
                            loc = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (loc != null) {
                                mLatitude = loc.getLatitude();
                                mLongitude = loc.getLongitude();
                                getCityName();
                                status = true;
                            }
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
        return status;
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.location_settings);
        alertDialog.setMessage(R.string.location_settings_message);
        alertDialog.setPositiveButton(R.string.location_settings_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_FINE_LOCATION: {

                getLocation();
            }
            return;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int grantResult = grantResults[0];
        if (grantResult != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
        } else {
            getLocation();
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        mProgressDialog.hide();
        try {
            mLocationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("LocationManager", "Error while trying to stop listening for location updates. This is probably a permissions issue", e);
        }
        Log.i("LOCATION (" + location.getProvider().toUpperCase() + ")", location.getLatitude() + ", " + location.getLongitude());
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        getCityName();
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


    void getCityName() {
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(mLatitude, mLongitude, 1);
            String add = "";
            if (addresses.size() > 0) {
                add = addresses.get(0).getCountryCode()+" "+addresses.get(0).getLocality();
            }
            cityName.setText(add);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
