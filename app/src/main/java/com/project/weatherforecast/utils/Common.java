package com.project.weatherforecast.utils;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Super on 3/3/2017.
 */

public abstract class Common {
    public static String FormatTime(Date dateTime, Context context) {

        DateFormat timeFormat = android.text.format.DateFormat.getDateFormat(context);
        return timeFormat.format(dateTime);
    }


}
