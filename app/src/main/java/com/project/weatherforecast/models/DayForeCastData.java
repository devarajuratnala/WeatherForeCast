package com.project.weatherforecast.models;

import android.graphics.drawable.Drawable;

import com.project.weatherforecast.utils.Common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Author       : Devaraju Ratnala
 * Created Date : 03-02-2015
 * Purpose      : This class is a Day Forecast Data Model
 * APK Version  : 1.0
 */

public class DayForeCastData {
    private String city;
    private String country;
    private String date;
    private String temperature;
    private String temperaturemin;
    private String temperaturemax;
    private String description;
    private String wind;
    private String pressure;
    private String humidity;
    private String id;
    private Drawable icon;
    private Date sunrise;
    private Date sunset;
    private String visiblity;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String dateString) {
        this.date = Common.FormatDateWithTime(dateString);
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public void setWindDirectionDegree(Double windDirectionDegree) {
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }


    public void setRain(String rain) {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }


    public Date getSunrise() {
        if (sunrise == null) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                sunrise = inputFormat.parse(String.valueOf(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return sunrise;
    }

    public void setSunrise(Date sunset) {
        this.sunrise = sunset;
    }
    public void setSunrise(String dateString) {
        setSunrise(ConvertStringToDate(dateString));
    }

    public Date getSunset() {
        return sunset;
    }

    public void setSunset(Date sunset) {
        this.sunset = sunset;
    }
    public void setSunset(String dateString) {
        setSunset(ConvertStringToDate(dateString));
    }

    public String getTemperaturemin() {
        return temperaturemin;
    }

    public void setTemperaturemin(String temperaturemin) {
        this.temperaturemin = temperaturemin;
    }

    public String getTemperaturemax() {
        return temperaturemax;
    }

    public void setTemperaturemax(String temperaturemax) {
        this.temperaturemax = temperaturemax;
    }

    public String getVisiblity() {
        return visiblity;
    }

    public void setVisiblity(String visiblity) {
        this.visiblity = visiblity;
    }

    private Date ConvertStringToDate(String dateString) {
        Date datetime;
        datetime = new Date(Long.parseLong(dateString) * 1000);
        return datetime;
    }

}
