package com.lincanbin.carbonforum.util;

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
                return Long.toString(seconds / 86400) + "天前";
            } else if (seconds >= 3600) {
                return Long.toString(seconds / 3600) + "小时前";
            } else if (seconds >= 60) {
                return Long.toString(seconds / 60) + "分钟前";
            } else if (seconds < 0) {
                return "刚刚";
            } else {
                return Long.toString(seconds + 1)+"秒前";
            }
        } else {
            // 大于一月
            Date nowTime = new Date(unixTimeStamp*1000);
            return DateFormat.getDateInstance().format(nowTime);
        }
    }

}
