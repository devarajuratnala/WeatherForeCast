package com.project.weatherforecast.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.weatherforecast.R;
import com.project.weatherforecast.models.DayForeCastData;
import com.project.weatherforecast.utils.Common;

import java.util.List;

/**
 * Created by Super on 3/3/2017.
 */

public class DayForecastAdapter extends BaseAdapter {
    private List<DayForeCastData> mListNextDaysForeCast;
    Context mContext;
    private static LayoutInflater inflater = null;

    public DayForecastAdapter(Context context, List<DayForeCastData> dayForeCastDataList) {
        mListNextDaysForeCast = dayForeCastDataList;
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mListNextDaysForeCast.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        TextView mTextViewDate, mTextViewWind, mTextViewHumidity, mTextViewPressure, mTextViewTemperatureMax, mTextViewTemperatureMin, mTextViewTemperature;
        ImageView mImageViewWeather;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = convertView;
        try {
            if (convertView == null) {
                rowView = inflater.inflate(R.layout.item_day_forecast, null);
                holder.mTextViewDate = (TextView) rowView.findViewById(R.id.text_view_date);
                holder.mTextViewWind = (TextView) rowView.findViewById(R.id.text_view_wind);
                holder.mTextViewHumidity = (TextView) rowView.findViewById(R.id.text_view_humidity);
                holder.mTextViewPressure = (TextView) rowView.findViewById(R.id.text_view_pressure);
                holder.mTextViewTemperatureMax = (TextView) rowView.findViewById(R.id.text_view_temperature_max);
                holder.mTextViewTemperatureMin = (TextView) rowView.findViewById(R.id.text_view_temperature_min);
                holder.mTextViewTemperature = (TextView) rowView.findViewById(R.id.text_view_temperature);
                holder.mImageViewWeather = (ImageView) rowView.findViewById(R.id.image_current_weather);
            }
            holder.mTextViewDate.setText("Time : " + Common.FormatTime(mListNextDaysForeCast.get(position).getDate(), mContext));
            holder.mTextViewWind.setText(mListNextDaysForeCast.get(position).getWind());
            holder.mTextViewHumidity.setText(mListNextDaysForeCast.get(position).getHumidity());
            holder.mTextViewPressure.setText(mListNextDaysForeCast.get(position).getPressure());
            holder.mTextViewTemperatureMax.setText(mListNextDaysForeCast.get(position).getTemperaturemax());
            holder.mTextViewTemperatureMin.setText(mListNextDaysForeCast.get(position).getTemperaturemin());
            holder.mTextViewTemperature.setText(mListNextDaysForeCast.get(position).getTemperature());
            holder.mImageViewWeather.setImageDrawable(mListNextDaysForeCast.get(position).getIcon());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowView;
    }
}