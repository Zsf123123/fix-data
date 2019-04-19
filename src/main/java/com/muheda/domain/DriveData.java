package com.muheda.domain;

import java.util.ArrayList;
import java.util.Date;

public class DriveData {

    // 保存异常数据
    private ArrayList<Double> checkValue;
    // 保存异常数据时间
    private ArrayList<Date>   checkTime;

    // 坐标
    private ArrayList<LngAndLat>  checkPoints;

    public DriveData() {
        checkValue = new ArrayList<>();
        checkTime = new ArrayList<>();
        checkPoints = new ArrayList<>();
    }


    public DriveData addData(double val, Date time, LngAndLat lngAndLat){
        checkTime.add(time);
        checkValue.add(val);
        checkPoints.add(lngAndLat);

        return this;
    }


    public ArrayList<Double> getCheckValue(){
        return this.checkValue;
    }

    public ArrayList<Date> getCheckTime(){
        return this.checkTime;
    }

    public ArrayList<LngAndLat> getCheckPoints() {
        return checkPoints;
    }

}
