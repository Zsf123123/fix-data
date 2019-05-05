package com.muheda.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @desc 关于时间处理的工具类
 */
public class DateUtil {

    /**
     * @desc 生成国际时间今日 UTC
     * @return
     */
    public static String generateUtcTime(){

        TimeZone timeZone = TimeZone.getTimeZone("GMT+0:00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(new Date());
    }


    /**
     * @desc 生成国际昨日时间
     * @return
     */
    public static String generateUtcYesterTime(){

        TimeZone timeZone = TimeZone.getTimeZone("GMT+0:00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(timeZone);

        Date date=new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        String yesterTime = simpleDateFormat.format(date);

        return simpleDateFormat.format(date);

    }


    /**
     * @desc 生成北京时间
     * @return
     */
    public static String generateBktTime(){

        TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(new Date());
    }


    /**
     * @desc 生成北京时间
     * @return
     */
    public static String generateBktYesterTime(){

        TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(timeZone);

        Date date=new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        String yesterTime = simpleDateFormat.format(date);

        return yesterTime;
    }




    /**
     * @desc 生成国际今日时间
     * @return
     */
    public static String generateUtcTimeWithBar(){

        TimeZone timeZone = TimeZone.getTimeZone("GMT+0:00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(new Date());

    }

    /**
     * @desc 生成国际时间UTC
     * @return
     */
    public static String generateUtcYesterTimeWithBar(){

        TimeZone timeZone = TimeZone.getTimeZone("GMT+0:00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(timeZone);

        Date date=new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return simpleDateFormat.format(date);
    }


    /**
     * @desc 生成北京时间昨日带有横杠
     * @return
     */
    public static String generateBktTimeWithBar(){

        TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(new Date());
    }

    /**
     * @desc 生成国际时间UTC
     * @return
     */
    public static String generateBktYesterTimeWithBar(){

        TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(timeZone);

        Date date=new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return simpleDateFormat.format(date);

    }

}
