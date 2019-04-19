package com.muheda.service;

import clojure.lang.IFn;
import clojure.lang.Obj;
import com.muheda.domain.DriveData;
import com.muheda.domain.LngAndLat;
import com.muheda.utils.DateUtils;
import org.apache.tools.ant.types.resources.Last;
import org.junit.Test;

import java.util.*;

public class SafetyDrivingCheck {


    // 数组依次递减

    //急加速，急减速
    private double[] maxAcceleration;

    //急转弯数据
    private double[] maxSharpTurn;


    // 数据单位必须为：米/秒^2,m/s2 或m·s-2（米每二次方秒）

    //将传过来的急加速的数据赋值给成员变量
    public SafetyDrivingCheck setAcceleration(double[] acc){
        this.maxAcceleration = null;
        this.maxAcceleration = new double[acc.length];
        for (int i=0;i<acc.length;++i){
            this.maxAcceleration[i] = acc[i];
        }

        return this;
    }

    public double[] getMaxAcceleration() {
        return maxAcceleration;
    }


    // 数据单位必须为：弧度/秒,rad/s 或rad·s-1（弧度每秒）
    public SafetyDrivingCheck setSharpTurn(double[] sharp){

        this.maxSharpTurn = null;

        this.maxSharpTurn = new double[sharp.length];

        for (int i = 0; i < sharp.length; ++i){

            this.maxSharpTurn[i] = sharp[i];

        }

        return this;
    }



    public double[] getMaxSharpTurn() {

        return maxSharpTurn;
    }




    /**
     * @desc  求出行车的加速度,在存储的时候只会存储加速度的具体的数值
     * @param lon  对应的经度
     * @param lat  对应的纬度
     * @param time 对应的时间，单位为：秒， 传过来的时间精确到秒 元素可以为0，当为0时，表示当前打点时间忽略、缺失或异常，程序处理时将自动跳过
     * @return
     */
    public DriveData getSpeedCheck(List<Double> lon, List<Double> lat, List<Date> time){


        if(!checkData(lon, lat, time)){
            return null;
        }

        Date preTime = null;

        double s = 0;
        double v = 0;
        double delte = 0;
        double a = 0;
        int type = -1;

        DriveData driveData = new DriveData();

        for (int i=0;i<time.size();++i){

            // 时间不等于空才进行处理相对应的数据
            if(time != null){
                if(preTime == null){
                    // 如果是第一次进来的话，进行初始化操作
                    preTime = time.get(i);
                    continue;
                }


                //计算当前经纬度与该点之前的经纬度之间的距离
                s += distance(lon.get(i-1), lat.get(i-1), lon.get(i), lat.get(i));

                int delteTime = Math.abs(DateUtils.getDiffDate(time.get(i), preTime, 13));

                // 求出速度
                delte = s/ delteTime;


                if(v > 0){
                    //计算出加速度
                    a = (delte - v)/(delteTime);
                    driveData.addData(a, time.get(i), new LngAndLat(lon.get(i),lat.get(i)));
                }

                preTime = time.get(i);

                v = delte;
                s = 0;
            }
            else{
                if(preTime != null){
                    s += distance(lon.get(i-1), lat.get(i-1), lon.get(i), lat.get(i));
                }
            }
        }

        return driveData;
    }


    /**
     * @desc 将double   数组实现数据的插入某个固定的位置
     * @param insetNum  需要插入的数据
     * @param origin    原始的数组
     * @param index      希望插入的位置
     * @return 返回一个插入之后的新数组
     */
    public Object [] insertToArray(Object[] origin, Object insetNum, int index){

        if(insetNum == null){

            return origin;
        }

        if(index < 0 || index > origin.length -1){
            return origin;
        }

        Object [] newDoubleArray = new Double[origin.length + 1];


        //确定好数据需要被嵌入的位置，将原数组的数据赋值到新的数组中
        for (int  i = 0; i < index; i++){

            newDoubleArray[i] = origin[i];
        }


        for(int i = index; i < origin.length; i++){

            newDoubleArray[i + 1] = origin[i];
        }


        newDoubleArray[index] = insetNum;

        return newDoubleArray;
    }


    /**
     * @desc 将Double 数组转成 double 类型的数组  单个可以进行自动拆装箱，但是如果是数组的形式是不能强制转换的
     */
    public double[] formatArrayDouble(Double [] array){

        double[] resultArray = new double[array.length];

        for (int i = 0; i < resultArray.length; i++) {

            resultArray[i] = array[i];
        }


        return resultArray;
    }




    // 参数为三位数据：经纬度和时间
    // lon 对应的经度
    // lat 对应的维度
    // timre 对应的时间，单位为：秒，元素可以为0，当为0时，表示当前打点时间忽略、缺失或异常，程序处理时将自动跳过
    public DriveData getSharpTurnCheck(LngAndLat lastRoutePoint, List<Double> lon, List<Double> lat, List<Date> time){

        lon.add(0,lastRoutePoint.getLng());
        lat.add(0,lastRoutePoint.getLat());
        time.add(0,lastRoutePoint.getDate());


        if(!checkData(lon, lat, time)){
            return null;
        }


        Date preTime = null;

        double rad = 0;
        double delte = 0;
        int type = -1;

        DriveData driveData = new DriveData();

        for (int i=0;i<time.size();++i){
            if(time.get(i) != null){
                if(i < 2){
                    preTime = time.get(i);
                    continue;
                }

                rad += rad(lon.get(i -1) - lon.get(i-2), lat.get(i-1) - lat.get(i-2), lon.get(i) - lon.get(i-1), lat.get(i) - lat.get(i -1));

                int delteTime = DateUtils.getDiffDate(time.get(i), preTime, 13);

                delte = rad/(delteTime);


                driveData.addData(delte, time.get(i), new LngAndLat(lon.get(i), lat.get(i)));

                preTime = time.get(i);

                rad = 0;
            }
            else{
                if(preTime != null){
                    if(i >= 2){
                        rad += rad(lon.get(i-1) - lon.get(i-2), lat.get(i-1) - lat.get(i-2), lon.get(i) - lon.get(i-1), lat.get(i) - lat.get(i-1));
                    }
                }
            }
        }

        return driveData;
    }

    private boolean checkData(List<Double> lon, List<Double> lat, List<Date> time){
        if(lon == null || lat == null || time == null){
            return false;
        }

        if(lon.size() != lat.size()){
            return false;
        }

        if(lon.size() != time.size()){
            return false;
        }

        if(time.size() <= 2){
            return false;
        }

        return true;
    }

    private int getAccIndex(double d){
        d = Math.abs(d);

        for (int i=0;i<maxAcceleration.length;++i){
            if(d >= maxAcceleration[i]){
                return i;
            }
        }

        return -1;
}

    private int getSharpTurnIndex(double d){
        d = Math.abs(d);

        for (int i=0;i<maxSharpTurn.length;++i){
            if(d >= maxSharpTurn[i]){
                return i;
            }
        }

        return -1;
    }





    private double distance(double lon1, double lat1, double lon2, double lat2){
        double s = 0;

        s += (lon2 - lon1)*(lon2 - lon1);
        s += (lat2 - lat1)*(lat2 - lat1);

        return Math.sqrt(s);
    }


    private double rad(double lon1, double lat1, double lon2, double lat2){
        double vecFZ = 0;
        double vecFM = 0;

        vecFZ = lon1 * lon2 + lat1 * lat2;
        vecFM = Math.sqrt(lon1*lon1 + lat1*lat1)*Math.sqrt(lon2*lon2 + lat2*lat2);

        if(vecFM == 0){
            return 0;
        }

        return Math.acos(vecFZ/vecFM);
    }



    /**
     * @desc 将行程转化成可以进行计算三急的格式
     * @param routes
     * @return  将行程中的三个维度给拆分成三个double数组 double [] lng , double [] lat , double [] time
     */
    public Map< String, Double[]>  coordinateFormatConver(List<LngAndLat> routes){

        int size = routes.size();

        Double [] lng = new Double[size];
        Double [] lat = new Double[size];
        Double [] time = new Double[size];



        return  null;

    }


}
