package com.muheda.utils;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


/**
 * @author zhangshaofan
 * @desc 字符串处理的工具类
 */


public class StringUtils {
    /**
     * 去除字符串中的空格
     *
     * @param str
     * @return comment by ddb
     */
    public static String trim(String str) {
        return isNullOrBlank(str) ? str : str.trim();
    }

    /**
     * 如果string为null则返回conv，否则返回string本身
     *
     * @param string
     * @param conv
     * @return comment by ddb
     */
    public static String convNull2What(String string, String conv) {
        if (string == null) {
            return conv;
        }
        return string;
    }

    /**
     * 如果第一个参数为null，则返回第二个参数，否则返回第一个参数的toString();
     *
     * @param obj
     * @param conv
     * @return comment by ddb
     */
    public static String convNull2String(Object obj, String conv) {
        if (obj == null) {
            return conv;
        }
        return obj.toString();
    }

    /**
     * value为原值，key为value字符串中需要替换的部分，replaceValue是替换key的新字符串
     * 例如：
     * String str = "books";
     * String rStr=StringUtil.replace(str , "oo", "ee");
     * System.out.println(rStr);
     * 输出结果为：beeks
     *
     * @param value
     * @param key
     * @param replaceValue
     * @return comment by ddb
     */
    public static String replace(String value, String key, String replaceValue) {
        if (value != null && key != null && replaceValue != null) {
            int pos = value.indexOf(key);

            if (pos >= 0) {
                int length = value.length();
                int start = pos;
                int end = pos + key.length();

                if (length == key.length()) {
                    value = replaceValue;
                } else if (end == length) {
                    value = value.substring(0, start) + replaceValue;
                } else {
                    value = value.substring(0, start) + replaceValue
                            + replace(value.substring(end), key, replaceValue);
                }
            }
        }
        return value;
    }

    /**
     * 如果value字符串为"",则返回true，否则返回false
     *
     * @param value
     * @return comment by ddb
     */
    public static boolean isBlank(String value) {
        boolean ret = false;
        if (value != null && value.equals("")) {
            ret = true;
        }
        return ret;
    }

    /**
     * 如果value字符串为null，则返回true，否则返回false
     *
     * @param value
     * @return comment by ddb
     */
    public static boolean isNull(String value) {
        return value == null ? true : false;
    }

    /**
     * 如果字符串value为null或者为""则返回true，否则返回false
     *
     * @param value
     * @return comment by ddb
     */
    public static boolean isNullOrBlank(String value) {
        return isNull(value) || isBlank(value);
    }

    /**
     * 如果参数fieldValue为""或者为null则返回null字符串，否则返回'fieldValue'形式的字符串
     *
     * @param fieldValue
     * @return comment by ddb
     */
    public static String fieldValue(String fieldValue) {
        if (StringUtils.isNullOrBlank(fieldValue)) {
            return "null";
        } else {
            return ("'" + fieldValue + "'");
        }

    }

    public static double fieldValue(double fieldValue) {

        return fieldValue;

    }

    public static float fieldValue(float fieldValue) {

        return fieldValue;

    }

    public static int fieldValue(int fieldValue) {

        return fieldValue;

    }

    public static long fieldValue(long fieldValue) {

        return fieldValue;

    }

    /**
     * 把string类型的数组转换成stringBuffer形式的字符串
     * 例如：string数组{aa,bb,cc}
     * 转换后为[aa,bb,cc,]
     *
     * @param arr
     * @return comment by ddb
     */
    public static String getArrayElementString(String[] arr) {
        StringBuffer ret = new StringBuffer("");
        if (arr == null) {
            return null;
        }

        ret.append("[");
        for (int i = 0; i < arr.length; i++) {
            ret.append(arr[i] + ",");
        }
        ret.append("]");

        return ret.toString();

    }

    /**
     * 传入一个日期格式，返回改格式的当前日期
     *
     * @param dateFormat
     * @return comment by ddb
     */
    public static String getCurrDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat myDateFormat = new SimpleDateFormat(dateFormat);
        return myDateFormat.format(date);
    }

    /**
     * 返回当前时间的Date类型
     * 例如：Tue Feb 15 11:07:54 CST 2011
     *
     * @return comment by ddb
     */
    public static java.util.Date getCurrentDate() {
        return new Date();
    }

    /**
     * 返回当前系统时间
     * 例如：2011-02-15 11:04:35.39
     *
     * @return comment by ddb
     */
    public static Timestamp getCurrDateTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 返回当前时间的Date类型
     * 例如 2011-02-15
     *
     * @return comment by ddb
     */
    public static java.sql.Date getCurrDate() {
        return new java.sql.Date(System.currentTimeMillis());
    }

    /**
     * 获得当前的年份
     * 例如：2011
     *
     * @return comment by ddb
     */
    public static String getCurrYear() {
        return getCurrDate("yyyy");
    }

    /**
     * 把Date类型转换长String类型
     *
     * @param date
     * @return comment by ddb
     */
    public static String dateToString(java.util.Date date) {
        SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return myDateFormat.format(date);
    }

    /**
     * 把string类型转换成Date类型
     *
     * @param strDate
     * @return comment by ddb
     */
    public static java.sql.Date stringToDate(String strDate) {

        java.sql.Date date = null;
        try {
            date = java.sql.Date.valueOf(strDate);
        } catch (Exception ex) {
        }
        return date;
    }

    /**
     * 把String类型转换为yyyy-MM-dd形式的Date类型
     *
     * @param strDate
     * @return comment by ddb
     */
    public static java.util.Date stringToUtilDate(String strDate) {
        java.util.Date date = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = df.parse(strDate);
        } catch (Exception ex) {
        }
        return date;
    }

    /**
     * 把固定格式的日期增加或者减少固定个月
     *
     * @param offset     月份偏移量，可以为负整数值
     * @param dateFormat 日期格式
     * @return comment by ddb
     */
    public static String addMonth(int offset, String dateFormat) {
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, offset);
        return df.format(calendar.getTime());
    }

    /**
     * 把改date日期增加或者减少固定个月
     *
     * @param offset 月份偏移量，可以为负整数值
     * @param date   日期类型
     * @return comment by ddb
     */
    public static Date addMonth(int offset, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, offset);
        return calendar.getTime();
    }

    /**
     * 把改date日期增加或者减少固定个天数
     *
     * @param offset 天数偏移量，可以为负整数值
     * @param date   日期类型
     * @return comment by ddb
     */
    public static Date addDay(int offset, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, offset);
        return calendar.getTime();
    }

    /**
     * 获取当前时间是一周中的某一天，返回值为如下字符串中的一个
     * "sunday", "monday", "tuestay", "wednesday", "thursday", "friday","saturday"
     *
     * @return comment by ddb
     */
    public static String getWeekDay() {
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int i = c.get(Calendar.DAY_OF_WEEK) - 1;
        String day[] = new String[]{"sunday", "monday", "tuestay", "wednesday", "thursday", "friday",
                "saturday"};
        return day[i];
    }

    public static double round(double v, int scale) {

        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }

        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static float floatRound(float a) {
        java.text.DecimalFormat df1 = new java.text.DecimalFormat("######.##");
        return Float.parseFloat(df1.format(a));
    }

    public static double floatRound(double a) {
        java.text.DecimalFormat df1 = new java.text.DecimalFormat("######.##");
        return Double.parseDouble(df1.format(a));
    }

    public static float floatRound1(float a) {
        java.text.DecimalFormat df1 = new java.text.DecimalFormat("######.#");
        return Float.parseFloat(df1.format(a));
    }

    public static String formatFour(int a) {
        java.text.DecimalFormat df1 = new java.text.DecimalFormat("0000");
        return df1.format(a);
    }

    public static String formatNumber(int a, int num) {
        if (num <= 0)
            return null;
        StringBuffer strbuff = new StringBuffer();
        for (int i = 0; i < num; i++) {
            strbuff.append("0");
        }
        java.text.DecimalFormat df1 = new java.text.DecimalFormat(strbuff
                .toString());
        return df1.format(a);
    }

    public static String getNewFileName(String oldFileName) {
        String name = oldFileName.substring(oldFileName.lastIndexOf("/") + 1);
        String extname = name.substring(name.lastIndexOf("."));
        Date now = new Date();
        long i = now.getTime();
        Random r = new Random();
        int rInt = r.nextInt(100);
        String newName = getCurrDate("yyyyMMdd") + new Long(i).toString()
                + new Integer(rInt).toString() + extname;
        return newName;
    }

    public static String getOldFileName(String oldFileName) {
        String name = oldFileName.substring(oldFileName.lastIndexOf("/") + 1);
        return name;
    }

    public static String[] getFileNameArray(String theFileName) {
        String[] StrArr = {
                theFileName.substring(0, theFileName.indexOf(".") - 1),
                theFileName.substring(theFileName.indexOf(".") + 1)};
        return StrArr;
    }

    public static String getFormatString(String targetStr, String appendStr) {
        if (targetStr == null || targetStr.length() < 1) {
            targetStr = appendStr;
        } else {
            targetStr += "/" + appendStr;
        }
        return targetStr;
    }

    public static void delFolder(String s, boolean delDir) {

        File file1 = new File(s.replaceAll("//", "/").replaceAll("\\/", "/"));
        if (file1.exists() && file1.isDirectory()) {
            File afile[] = file1.listFiles();
            for (int i = 0; i < afile.length; i++) {
                if (afile[i].isFile()) {
                    if (afile[i].exists())
                        afile[i].delete();
                    continue;
                }
                if (afile[i].isDirectory())
                    delFolder(afile[i].toString(), delDir);
            }

            if (delDir)
                file1.delete();
        }

    }

    public static final void delFile(String s) {
        File file1 = new File(s);
        if (file1.exists()) {
            file1.delete();
        }

    }

    /**
     * 屏蔽手机号码
     *
     * @param mobile
     * @return
     */
    public static String repMobile(String mobile) {
        String repMobile = "";
        if (null != mobile && !"".equals(mobile)) {
            if (11 == mobile.length()) {
                String h = mobile.substring(0, 3);
                String f = mobile.substring(7, 11);
                repMobile = h + "****" + f;
            } else {
                repMobile = mobile;
            }
        }
        return repMobile;
    }
}

