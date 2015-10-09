package com.lincanbin.carbonforum.util;

import com.lincanbin.carbonforum.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by 灿斌 on 5/17/2015.
 */
public class TimeUtil {
    public static String formatTime(long unixTimeStamp){
        long seconds = System.currentTimeMillis() / 1000 - unixTimeStamp;
        if (seconds < 2592000) {
            // 小于30天如下显示
            if (seconds >= 86400) {
                return Long.toString(seconds / 86400) + R.string.days_ago;
            } else if (seconds >= 3600) {
                return Long.toString(seconds / 3600) + R.string.hours_ago;
            } else if (seconds >= 60) {
                return Long.toString(seconds / 60) + R.string.minutes_ago;
            } else if (seconds < 0) {
                return "" + R.string.just_now;
            } else {
                return Long.toString(seconds + 1) + R.string.seconds_ago;
            }
        } else {
            // 大于30天直接显示日期
            Date nowTime = new Date(unixTimeStamp*1000);
            return DateFormat.getDateInstance().format(nowTime);
        }
    }

}
