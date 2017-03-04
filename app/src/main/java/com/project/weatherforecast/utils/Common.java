package com.project.weatherforecast.utils;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

/**
 * Author       : Devaraju Ratnala
 * Created Date : 03-04-2015
 * Purpose      : This class is a Common class for entire application to call all common functions
 * APK Version  : 1.0
 */

public abstract class Common {
    public static String FormatTime(Date dateTime, Context context) {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        return timeFormat.format(dateTime);
    }
    public static  String FormatDateWithTime(String dateString) {
        Date datetime;
        datetime = new Date(Long.parseLong(dateString) * 1000);
        String dt = (String)android.text.format.DateFormat.format("dd-MM-yyyy HH:MM", datetime);
        return dt;
    }


}
