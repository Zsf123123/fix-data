package com.muheda.utils;


import com.muheda.domain.Contans;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description 用于检测数据是否正确
 * @author zhangshaofan
 */

public class CheckDataUtils {

    private  static Logger logger = Logger.getLogger(ObjToString(CheckDataUtils.class));

    //手机号检测
    private static final Pattern MOBILE_PATTERN = Pattern
            .compile ( "^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}" );

    private static final String ObjToString(Object obj){
        try{
            if(obj == null){
                return null;
            }
            else{
                return obj.toString();
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }



    /**
     * @desc 对于待检测的数据进行一定范围内的校验，验证该数据是否在给定的范围之内
     * @warm 该功能待测
     */
    public static boolean paramsRangeCheck(Object param) {

        if (param == null) {
            return false;
        }

        String[] strArr = (String[]) param;

        if (strArr == null || strArr.length < 2) {
            return false;
        }

        String s = strArr[0].replace("[","").replace("]","");

        //字段的校验范围
        String[] split = s.split(",");

        for (int i = 0; i <split.length; i++) {

            if(split[i].equals(strArr[1])){

                return true;
            }
        }

        return false;
    }


    /**
     *@desc 验证数据是否为空包括，空字符串，空，和"null"
     *@param obj 表示的是传入过来的参数
     *@return 如果数据校验没有问题则为true,如果检验有问题则为false
     *
     */
    public static boolean validatelNull(Object obj) {

        if(obj == null){
            return  false;
        }

        String  result = ObjToString(obj);

        if("".equals(result) || "null".equals(result) || "NULL".equals(result)){
            return false;
        }

        return true;

    }


    /**
     * @desc 验证设备号是否是正常的设备号
     * @return true 表示是正常设备 false 表示是随机生成的
     */
    public static boolean validatelDeviceId(Object obj){

        String deviceId = ObjToString(obj);

        //如果设备号是以DATAERR开头则表明是异常设备号
        if(deviceId.startsWith("DATAERR")){

            return false;
        }

        return true;
    }


    /**
     * 验证手机号码格式是否正确
     * @param obj
     * @return true 表示正确 false表示不正确
     */
    public static boolean isMobile(Object obj){

        String mobile = ObjToString(obj);
        Pattern p = MOBILE_PATTERN;
        Matcher m = p.matcher ( mobile );
        return m.matches ( );
    }


    /**
     * @description 时间的格式和时间的正确性
     * @param obj
     * @return true表示的是时间的格式和正确性没有问题
     */
    public static Boolean validateTime(Object obj){

        String time = ObjToString(obj);

        //验证格式是否正确
        boolean b = timeFormat(time);

        if(b == false){
            return false;
        }
        //验证时间是否正确
        boolean b1 = isRightTime(time);

        if(b1 == false){
            return false;
        }

        return true;
    }

    /**
     * @description 时间的格式和时间的正确性
     * @param
     * @return true表示的是时间的格式和正确性没有问题
     */
    public static boolean validateTimeWthBar(Object obj){

        String time = ObjToString(obj);
//        logger.warn ( "传入的数据为："+time );
        boolean b = timeFormatWithBar(time);
        if(b){
//            logger.warn ( "格式正确" );
        }else {
            logger.warn ( "格式错误" );
        }

        boolean b1 = isRightTimeWitBar(time);
        if(b1){
//            logger.warn ( "时间内容正确" );
        }else {
            logger.warn ( "时间内容错误" );
        }

        if(b == true && b1 == true){

            return true;
        }

        return false;
    }


    /**
     * @description 利用SimileDateFormat做时间格式的校验，普通时间格式校验 yyyyMMddHHmmss
     * @param str
     * @return  true 表示时间没有问题
     */
    public static boolean timeFormatWithBar(String str) {

        if(str == null){
            return false;
        }

        boolean flag = true;

        SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss.SSS" );
        try {
            format.setLenient ( false );

            format.parse ( str );

        } catch (ParseException e) {

            logger.error ( "时间格式校验失败");
            flag = false;
        }

        return flag;
    }


    /***
     * @description 时间正确性校验
     * @param time
     * @return  返回true表示数据没有问题
     * @author zhangshaofan
     */
    public static boolean isRightTimeWitBar(String time) {

        if(time == null){
            return false;
        }
        //true为有问题，剔除
        boolean isRight;
        //获取当前时间
        String currentTime = DateUtil.generateUtcTimeWithBar();

        //截取到日
        String currentDay = currentTime.substring(0, 10);

        String yesterTime = DateUtil.generateUtcYesterTimeWithBar();

        //截取昨日
        String yesterDay = yesterTime.substring(0, 10);


        //获取设备上传时间截取到日
        String sendDay = null;
        try {
            sendDay = time.substring(0, 10);
        } catch (Exception e) {
            logger.error(sendDay);
            logger.error("时间数据转换错误");
            e.printStackTrace();
        }

        if (sendDay.equals(currentDay) || sendDay.equals(yesterDay)) {
            isRight = true;
        } else {
            isRight = false;
        }
        return isRight;

    }



    /**
     * @description 利用SimileDateFormat做时间格式的校验，普通时间格式校验 yyyyMMddHHmmss
     * @param obj
     * @return  true 表示时间没有问题
     */
    public static boolean timeFormat(Object obj){

        String str = ObjToString(obj);
        boolean flag = true;

        SimpleDateFormat format = new SimpleDateFormat ( "yyyyMMddHHmmss" );
        try {
            format.setLenient ( false );
            format.parse ( str );
        } catch (ParseException e) {
            logger.error ( "时间格式校验失败");

            flag = false;
            e.printStackTrace();
        }

        return flag;
    }


    /**
     * @desc 验证时间是否正确
     * @param obj
     * @return
     */
    public static boolean isRightTime(Object  obj){

        String time = ObjToString(obj);

        //true为有问题，剔除
        boolean isRight;

        String currentTime = DateUtil.generateBktTime();
        String yesterTime = DateUtil.generateBktYesterTime();

        //截取到日
        Long currentDay = Long.parseLong ( currentTime.substring ( 0, 8 ) );
        //昨天
        Long yesterDay = Long.parseLong ( yesterTime.substring ( 0, 8 ) );

        //获取设备上传时间截取到日
        Long sendDay = null;
        try {

            sendDay = Long.parseLong (time.substring ( 0, 8 ));

        } catch (Exception e) {

            logger.error("时间数据转换错误");
            e.printStackTrace();
            return false;
        }

        if (sendDay.equals(currentDay) || sendDay.equals(yesterDay)) {
            isRight = true;
        } else {
            isRight = false;
        }
        return isRight;

    }





    /**
     * 将带有 - 的时间转换成yyyyMMddHHmmssSSS的
     * @param obj
     * @return
     */
    public static String dataFormatBar(Object obj){

        String time = ObjToString(obj);
        String value=null;
        try {
            Date date = DateUtils.parseDate ( time, new String[]{"yyyy-MM-dd HH:mm:ss.SSS"} );
            value = DateFormatUtils.format(date, "yyyyMMddHHmmssSSS");
        } catch (ParseException e) {
            logger.error ( "时间格式转换有误" );
            e.printStackTrace ( );
        }
        return value;
    }

    /**
     * @description  对纬度进行校验,纬度必须是小数的形式
     * @param obj
     * @return  如果返回时true则表示数据没有问题
     */
    public synchronized static  boolean validatelLat(Object obj){

        String lat = ObjToString(obj);

        if(StringUtil.isEmpty(lat)){
            return false;
        }

        String[] split = lat.split("\\.");

//        System.out.println(split[0]);
        if(split.length == 0){
            return false;
        }

        Double  value= Double.parseDouble(split[0]);

        if ((!lat.contains(".")) || value > Contans.LATMAX || value < Contans.LATMIN) {
            return false;
        }

        return true;
    }


    /**
     * @description  对未处理的经度进行校验,纬度必须是小数的形式
     * @param obj
     * @return   如果返回时true则表示数据没有问题
     */
    public static  boolean validateOriginLat(Object obj){

        String lat = ObjToString(obj);
        if(lat == null){
            return false;
        }

        Double l = Double.parseDouble(lat);

        Double l1 = l / 1000000;

        return validatelLat(ObjToString(l1));

    }

    /**
     * @description  对经度进行校验,纬度必须是小数的形式
     * @param obj
     * @return   如果返回时true则表示数据没有问题
     */
    public synchronized static  boolean validatelLng(Object obj){

        String lng = ObjToString(obj);
        if(!lng.contains(".")){
            return false;
        }

        String[] split = lng.split("\\.");
        Double  value= Double.parseDouble(split[0]);

        if (value > Contans.LNGMAX || value < Contans.LNGMIN) {
            return false;
        }

        return true;
    }


    /**
     * @description  对未处理的经度进行校验,纬度必须是小数的形式
     * @param obj
     * @return   如果返回时true则表示数据没有问题
     */
    public static  boolean validateOriginLng(Object obj){

        String lng = ObjToString(obj);
        if(lng == null){
            return false;
        }

        Double l = Double.parseDouble(lng);

        Double l1 = l / 1000000;

        return validatelLng(ObjToString(l1));

    }


    /**
     * @description 用于验证速度是否为零
     * @param obj
     * @return  返回true 表示数据没有问题
     */
    public  static Boolean validatelZero(Object obj){
        String value = ObjToString(obj);

        if(value.equals("0")){

            return false;
        }

        return true;
    }

    /**
     * @description  验证数据是不是true或者false
     * @param
     */
    public static  Boolean validateBoolean(Object obj){

        String value = ObjToString(obj);

        if(value.equals("true") || value.equals("false")){

            return true;
        }else {
            return  false;

        }
    }


    /**
     * 验证数据是不是0或者1
     * @param obj
     * @return
     */
    public static Boolean validateZeroOrOne(Object obj){

        String value = ObjToString(obj);
        Integer num = null;

        try {
            num = Integer.valueOf(value);
        } catch (Exception e) {
            logger.error("数据转换异常");
            return false;
        }

        if(num == 0 ||num ==1){
            return true;
        }else{
            return false;
        }

    }


    /**
     * 判断数据是不是整型
     * @param obj
     * @return
     */
    public static Boolean validateInteger(Object obj){

        String value = ObjToString(obj);
        Integer num = null;

        try {
            num = Integer.valueOf(value);
        } catch (Exception e) {
            logger.error("数据转换异常");
            //如果数据转换失败则返回false
            return false;
        }

        return true;
    }



    /**
     * 判断数据是不是浮点型数
     * @param obj
     * @return
     */
    public static Boolean validateDouble(Object obj){

        String value = ObjToString(obj);
        Double num = null;

        try {
            num = Double.valueOf(value);
        } catch (Exception e) {
            logger.error("数据转换异常");
            //如果数据转换失败则返回false
            return false;
        }

        return true;
    }



}