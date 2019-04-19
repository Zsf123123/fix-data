package com.muheda.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateUtils {


    public static final String DATETIME_FORMAT = "yyyyMMddHHmmss";

    private  static Logger logger = LoggerFactory.getLogger(DateUtils.class);

    /**
     * 时间相减
     *
     * @param strDateBegin
     * @param strDateEnd
     * @param iType
     * @return
     */
    public static int getDiffDate(Date strDateBegin, Date strDateEnd, int iType) {
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(strDateBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(strDateEnd);
        long lBegin = calBegin.getTimeInMillis();
        long lEnd = calEnd.getTimeInMillis();
        if (iType == Calendar.SECOND)
            return (int) ((lEnd - lBegin) / 1000L);
        if (iType == Calendar.MINUTE)
            return (int) ((lEnd - lBegin) / 60000L);
        if (iType == Calendar.HOUR)
            return (int) ((lEnd - lBegin) / 3600000L);
        if (iType == Calendar.DAY_OF_MONTH) {
            return (int) ((lEnd - lBegin) / 86400000L);
        }
        return -1;
    }

    /**
     * @desc 日期解析
     * @param date
     * @param format
     * @return
     */
    public static Date parseDate(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Date timeFormat(String str) {

        Date parse = null;
        SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT);

        try {
            parse = format.parse(str);
        } catch (ParseException e) {
            logger.error("时间格式转换失败");
            e.printStackTrace();
        }

        return parse;
    }


    /**
     * @desc  将Date 格式转化成String
     * @param dateDate
     * @return
     */
    public static String dateToStrLong(Date dateDate) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(dateDate);

        return dateString;
    }



}
