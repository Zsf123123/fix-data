package com.muheda.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @desc 经纬度的实体类
 */
public class LngAndLat implements Serializable , Cloneable {


    private  String deviceId;

    // 经度
    private  Double lng;

    //纬度
    private Double lat;

    //记录下该点所对应的时刻
    private Date date;


    public LngAndLat(){

    }

    public LngAndLat(Double lng, Double lat){

        this.lng = lng;
        this.lat = lat;

    }


    public LngAndLat(String deviceId ,Double lng, Double lat) {
        this.deviceId = deviceId;
        this.lng = lng;
        this.lat = lat;
    }

    public LngAndLat(Double lng, Double lat, Date date) {
        this.lng = lng;
        this.lat = lat;
        this.date = date;
    }

    public LngAndLat(String deviceId, Double lng, Double lat, Date date) {
        this.deviceId = deviceId;
        this.lng = lng;
        this.lat = lat;
        this.date = date;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }


    public void setDate(Date date) {
        this.date = date;
    }

    public Double getLat() {
        return lat;
    }


    public Double getLng() {
        return lng;
    }

    public Date getDate() {
        return date;
    }


    @Override
    public String toString() {
        return "LngAndLat{" +
                "deviceId='" + deviceId + '\'' +
                ", lng=" + lng +
                ", lat=" + lat +
                ", date=" + date +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
