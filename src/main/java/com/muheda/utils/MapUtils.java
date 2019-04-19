package com.muheda.utils;


import com.muheda.domain.LngAndLat;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @desc 百度坐标与高德坐标相互转化
 */

public class MapUtils {


    /*GCJ-02(火星坐标) 和 BD-09 （百度坐标）
     *    算法代码如下，其中 bd_encrypt 将 GCJ-02 坐标转换成 BD-09 坐标， bd_decrypt 反之。
     */
    static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;


    //高德转百度
    public static Map<String, Float> bdEncrypt(double gg_lat, double gg_lon) {
        Map<String, Float> data = new HashMap<String, Float>();
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
//        System.out.println(bd_lon+","+bd_lat);
//        System.out.println(new BigDecimal(String.valueOf(bd_lon)).floatValue()+","+new BigDecimal(String.valueOf(bd_lat)).floatValue());
        data.put("lon", new BigDecimal(String.valueOf(bd_lon)).floatValue());
        data.put("lat", new BigDecimal(String.valueOf(bd_lat)).floatValue());
        return data;
    }

    public static Map<String, Double> bd_decrypt(double bd_lon, double bd_lat) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        Map<String, Double> data = new HashMap<String, Double>();
        data.put("lng", gg_lon);
        data.put("lat", gg_lat);
        return data;
    }


    /**
     * @param lng1 经度1
     * @param lat1 维度1
     * @param lng2 经度2
     * @param lat2 纬度2
     * @retur n
     */
    public static double getDistance(double lng1, double lat1, double lng2,
                                     double lat2) {

        lng1 = (Math.PI / 180) * lng1;
        lng2 = (Math.PI / 180) * lng2;
        lat1 = (Math.PI / 180) * lat1;
        lat2 = (Math.PI / 180) * lat2;


        // 地球半径
        double R = 6371;

        // 两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1)) * R;

        return d * 1000;
    }

    /*
     * 根据经纬度计算两点之间的距离（单位米）
     * */
    public static String algorithm(double longitude1, double latitude1, double longitude2, double latitude2) {

        double Lat1 = rad(latitude1); // 纬度
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;//两点纬度之差
        double b = rad(longitude1) - rad(longitude2); //经度之差
        double s = 2 * Math.asin(Math
                .sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));//计算两点距离的公式

        s = s * 6378137.0;//弧长乘地球半径（半径为米）
        s = Math.round(s * 10000d) / 10000d;//精确距离的数值
        s = s / 1000;//将单位转换为km，如果想得到以米为单位的数据 就不用除以1000
        //四舍五入 保留一位小数
        DecimalFormat df = new DecimalFormat("#.0");

        return df.format(s);

    }

    private static double rad(double d) {
        return d * Math.PI / 180.00; //角度转换成弧度
    }

    //计算出一段路程的最小矩形
    public static List<LngAndLat> minimumRectangle(List<LngAndLat> list) {


        ArrayList<LngAndLat> minRectangle = new ArrayList<>();

        //将穿过来的集合中的所有的数据的经度存入lngs这个集合中
        List<Double> lngs = new ArrayList<>();
        List<Double> lats = new ArrayList<>();

        for (LngAndLat lngAndLat : list) {
            lngs.add(lngAndLat.getLng());
            lats.add(lngAndLat.getLat());
        }

        Collections.sort(lngs);
        Collections.sort(lats);

        //将经度的集合和纬度的集合进行排序，取出最大和最小值。组成一个最小矩形
        LngAndLat smallest = new LngAndLat();
        LngAndLat bigger = new LngAndLat();

        //最小包围矩形的最小点(逻辑上的）
        smallest.setLng(lngs.get(0));
        smallest.setLat(lats.get(0));

        //最小包围矩形的最大的点（逻辑上的）
        bigger.setLng(lngs.get(lngs.size() - 1));
        bigger.setLat(lats.get(lats.size() - 1));


        minRectangle.add(smallest);
        minRectangle.add(bigger);

        return minRectangle;


    }

    /**
     * @param lat_a 纬度1
     * @param lng_a 经度1
     * @param lat_b 纬度2
     * @param lng_b 经度2
     * @return
     * @desc 2个点之间形成的角度
     */
    private double getAngle1(double lat_a, double lng_a, double lat_b, double lng_b) {

        double y = Math.sin(lng_b - lng_a) * Math.cos(lat_b);
        double x = Math.cos(lat_a) * Math.sin(lat_b) - Math.sin(lat_a) * Math.cos(lat_b) * Math.cos(lng_b - lng_a);
        double brng = Math.atan2(y, x);
        brng = Math.toDegrees(brng);
        if (brng < 0)
            brng = brng + 360;
        return brng;

    }


    /**
     * @desc 通过2个点确定出该条直线的方程 ax+by+c = 0，之后再计算出与之垂直的直线方程 bx-ay+z =0
     * @desc 将被传入的点带入进去之后确定出垂直的方程。之后计算2条直线的相交点确定出修复之后的点的坐标
     *@param  p1 构成直线的点x
     * @param p2 构成直线的点y
     * @param toBeRepairedPoint 构成直线的待修复的点
     */
    public static LngAndLat getFoot(LngAndLat p1,LngAndLat p2,LngAndLat toBeRepairedPoint){
        LngAndLat foot=new LngAndLat();

        double dx = p1.getLng() - p2.getLng();
        double dy = p1.getLat() - p2.getLat();

        double u = (toBeRepairedPoint.getLng() - p1.getLng()) * dx + (toBeRepairedPoint.getLat() - p1.getLat()) * dy;
        
        u /= dx * dx + dy * dy;

        foot.setLng(p1.getLng() + u * dx);
        
        foot.setLat(p1.getLat()+u*dy);


        double d= Math.abs((p1.getLng()-p2.getLng())*(p1.getLng()-p2.getLng())+(p1.getLat()-p2.getLat())*(p1.getLng()-p2.getLat()));

        double d1=Math.abs((p1.getLng()-foot.getLng())*(p1.getLng()-foot.getLng())+(p1.getLat()-foot.getLat())*(p1.getLng()-foot.getLat()));


        double d2=Math.abs((p2.getLng()-foot.getLng())*(p2.getLng()-foot.getLng())+(p2.getLat()-foot.getLat())*(p2.getLng()-foot.getLat()));

        if(d1>d||d2>d){
            if (d1>d2)  return p2;
            else return p1;
        }

        return foot;
    }


    /**
     * @desc 获取点到2个直线间的垂直距离
     */
    public  static  double pointToLineVerticalDistance(LngAndLat p1,LngAndLat p2,LngAndLat p3){

        LngAndLat foot = getFoot(p1, p2, p3);
        return getDistance(p3.getLng(), p3.getLat(), foot.getLng(), foot.getLat());

    }


    public static void main(String[] args) {


        double v = pointToLineVerticalDistance(new LngAndLat(123.589471, 41.913959), new LngAndLat(123.577471, 41.914971), new LngAndLat(123.58598162639836, 41.91425744137035));

        System.out.println(v);
    }

}
