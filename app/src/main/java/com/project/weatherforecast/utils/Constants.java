package com.project.weatherforecast.utils;

/**
 * Author       : Devaraju Ratnala
 * Created Date : 03-02-2015
 * Purpose      : This class is used for to maintain all API Urls
 * APK Version  : 1.0
 */

public class Constants {
    public class ServiceType {
        private static final String HOST_URL = "http://api.openweathermap.org/data/2.5/";
        public static final String CURRENT_WEATHER_DATA = HOST_URL + "weather";
        public static final String FORECAST_DATA = HOST_URL + "forecast";
    }
}
