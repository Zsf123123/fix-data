package com.muheda.utils;

import com.muheda.domain.Contans;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串操作类
 *
 * @author wangchong
 */
public class StringUtil {
    private static Log logger = LogFactory.getLog ( StringUtil.class );

    /**
     * 判断字符串是否为空
     *
     * @param data
     * @return
     */
    public static boolean isEmpty(String data) {
        if (null == data || 0 == data.trim ( ).length ( )|| Contans.NULL.equals ( data )) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否含有特殊字符
     *
     * @param str
     * @return true为不包含，false为包含
     */
    public static boolean isSpecialChar(String[] str) {

        String regEx = "[ _`~!@#$%^&@*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";

        //循环遍历看每一个字符是否是非法字符
        for (int i = 0; i < str.length; i++) {
            Pattern p = Pattern.compile ( regEx );
            Matcher m = p.matcher ( str[i] );
            if (m.find ( ) == true) {
                return false;
            }
        }
        return true;
    }



    private static final Pattern TIME_PATTERN = Pattern.compile (
            "^(20[123][0-9])((0[1-9])|(1[012]))((0[1-9])|([12][0-9])|(3[01]))((0[0-9])|(1[0-9])|(2[0-3]))([0-5][0-9])([0-5][0-9])$" );

    /**
     * 验证时间有效性，时间格式：yyyyMMddHHmmss
     *
     * @param time
     * @return
     */
    public static boolean isTime(String time) {
        if (time.length ( ) != 14) {
            return false;
        }
        // 年 (20[123][0-9])
        // 月 ((0[1-9])|(1[012]))
        // 日 ((0[1-9])|([12][0-9])|(3[01]))
        // 时 ((0[0-9])|(1[0-9])|(2[0-3]))
        // 分 ([0-5][0-9])
        // 秒 ([0-5][0-9])
        Pattern pattern = TIME_PATTERN;
        Matcher m = pattern.matcher ( time );
        return m.matches ( );
    }

    private static final Pattern MOBILE_PATTERN = Pattern
            .compile ( "^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}" );

    /**
     * 验证手机号码格式是否正确
     *
     * @param mobile
     * @return true 表示正确 false表示不正确
     */
    public static boolean isMobile(String mobile) {
        Pattern p = MOBILE_PATTERN;
        Matcher m = p.matcher ( mobile );
        return m.matches ( );
    }

    /**
     * 判断对象是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isObjectEmpty(Object obj) {
        if (null == obj) {
            return true;
        }
        return false;
    }



    // Bean --> Map 1: 利用Introspector和PropertyDescriptor 将Bean --> Map

    /**
     * 将 javabean转化为map
     * 利用Introspector和PropertyDescriptor 将Bean --> Map
     *
     * @param obj
     * @return
     * @author zhanggang
     */
    public static Map<String, String> transBean2Map(Object obj) {

        if (obj == null) {
            return null;
        }
        Map<String, String> map = new HashMap ( 16 );
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo ( obj.getClass ( ) );
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors ( );
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName ( );

                // 过滤class属性
                if (!key.equals ( "class" )) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod ( );
                    String value = String.valueOf ( getter.invoke ( obj ) );
                    map.put ( key, value );
                }

            }
        } catch (Exception e) {
            logger.error ( "transBean2Map Error ", e );
        }
        return map;
    }

    /**
     * 利用org.apache.commons.beanutils 工具类实现 Map --> Bean
     *
     * @param map
     * @param obj
     * @author zhanggang
     */
    public static void transMap2Bean(Map<String, Object> map, Object obj) {
        if (map == null || obj == null) {
            return;
        }
        try {
            BeanUtils.populate ( obj, map );
        } catch (Exception e) {
            System.out.println ( "transMap2Bean2 Error " + e );
        }
    }

    /**
     * 传入一个经纬度，进行长度格式化，统一保留小数点后8位
     *
     * @param num
     * @return
     */
    public static String getLnglat(String num) {
        String newNum=num;
        try {
            DecimalFormat decimalFormat = new DecimalFormat ( "###0.00000000" );
            newNum = decimalFormat.format ( Double.parseDouble ( num ) );
        } catch (Exception e) {
            logger.error ( "经纬度格式化错误", e );
        }
        return newNum;
    }


    /**
     * @DESC 获取在字符串中某一个字符出现的个数
     * @return 返回数量
     */
    public static int getCharNum(String value,char ch){

        //计数器
        int num = 0;

        char[] chars = value.toCharArray();

        for (int i = 0; i < chars.length; i++) {

            if(chars[i] == ch){
                num++;
            }
        }

        return num;
    }


    /**
     *@desc   生成一个随机的设备号
     *@return 返回了随机生成设备号
     */
    public static  String randomDeviceId(String family){

        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String  deviceId = "DATAERR" + "_" + family + "_" + time + UUID.randomUUID ( ).toString ( ).replace ( "-", "" ).substring ( 0, 9 );
        return deviceId;

    }

    public static String transferTime(String str){

        String s1 = str.replaceAll(" ","");
        String s2 = s1.replaceAll(":", "");
        String s3 = s2.replaceAll("-", "");
        String s4 = s3.replaceAll("\\.", "");
        return s4;

    }

}
