package com.muheda.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {


    private  static Logger logger = LoggerFactory.getLogger(TimeUtils.class);

    /**
     * @description 利用SimileDateFormat将字符串的时间转成Date格式的时间
     * @param time  传入的字符串的格式时间
     * @return     表示时间没有问题
     */
    public static Date timeFormat(String  time){

        Date date = null;

        SimpleDateFormat format = new SimpleDateFormat ( "yyyyMMddHHmmss" );
        try {
            format.setLenient ( false );
            date = format.parse(time);
            return  date;
        } catch (ParseException e) {

            logger.error ( "时间格式校验失败");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @desc 计算2个时间之间的时间差(分钟)
     *
     * @param from  开始的时间
     * @param to    结束的时间
     */
    public static long timeDifference(Date to, Date from){

        //获得这两个时间的毫秒值后进行处理(因为我的需求不需要处理时间大小，所以此处没有处理，可以判断一下哪个大就用哪个作为减数。)
        long diff = to.getTime() - from.getTime();

        //此处用毫秒值除以分钟再除以毫秒既得两个时间相差的分钟数
        long minute = diff/60/1000;

        return minute;

    }





}
