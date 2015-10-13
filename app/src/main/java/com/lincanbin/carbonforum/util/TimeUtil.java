package com.lincanbin.carbonforum.util;

import android.content.Context;

import com.lincanbin.carbonforum.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by 灿斌 on 5/17/2015.
 */
public class TimeUtil {
    public static String formatTime(Context context, long unixTimeStamp){
        long seconds = System.currentTimeMillis() / 1000 - unixTimeStamp;
        if (seconds < 2592000) {
            // 小于30天如下显示
            if (seconds >= 86400) {
                return Long.toString(seconds / 86400) + " " + context.getString(R.string.days_ago);
            } else if (seconds >= 3600) {
                return Long.toString(seconds / 3600) + " " + context.getString(R.string.hours_ago);
            } else if (seconds >= 60) {
                return Long.toString(seconds / 60) + " " + context.getString(R.string.minutes_ago);
            } else if (seconds < 0) {
                return context.getString(R.string.just_now);
            } else {
                return Long.toString(seconds + 1) + " " + context.getString(R.string.seconds_ago);
            }
        } else {
            // 大于30天直接显示日期
            Date nowTime = new Date(unixTimeStamp*1000);
            return DateFormat.getDateInstance().format(nowTime);
        }
    }

}
