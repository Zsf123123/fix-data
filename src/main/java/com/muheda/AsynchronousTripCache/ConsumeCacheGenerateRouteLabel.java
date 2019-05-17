package com.muheda.AsynchronousTripCache;


import com.muheda.dao.DrivingLabelDao;
import com.muheda.domain.DriveData;
import com.muheda.domain.LngAndLat;
import com.muheda.domain.RoadInfo;
import com.muheda.service.DealWithRoute;
import com.muheda.service.SafetyDrivingCheck;
import com.muheda.utils.DateUtils;
import com.muheda.utils.MapUtils;

import java.util.*;

public class ConsumeCacheGenerateRouteLabel {

    private  CacheQueueHandle cacheQueueHandle = new CacheQueueHandle();

    private  Map<String, Object> lastRoute =  null;

    private  String lastRouteDeviceId;

    private  Date lastRouteTime;

    private  LngAndLat lastRouteLastPoint;

    public  void startThreadConsume() {

        new Thread(() -> {
            while (true) {

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                consumeCacheTrip();

            }

        }).start();



    }


    /**
     * @desc 从外部启一个线程来调用这个消费的方法
     * @desc 消费缓存起来的行程
     */
    public   void consumeCacheTrip(){


        List<Map<RoadInfo, List<LngAndLat>>> maps = cacheQueueHandle.consumeTrip();

        if(null == maps){
            return;
        }

        //消费传过来的行程的时候,查看2个行程之间的时间是不是一个连续的过程,我们需要缓存上一个行程的数据
        
        for (Map<RoadInfo,List<LngAndLat>> map : maps) {

            List<LngAndLat> thisRoute = null;


            for (Map.Entry<RoadInfo,List<LngAndLat>> entry : map.entrySet()) {

                RoadInfo key = entry.getKey();
                thisRoute = entry.getValue();


            }

            LngAndLat thisPoint = thisRoute.get(0);

            String deviceId = thisPoint.getDeviceId();

            if(thisPoint == null){
                continue;
            }


            // 如果此时的上一段行程为空,则直接计算该行程的急加速，急减速
            if(lastRoute == null){

                //在此处对行程进行非空判断
                Map<String, Object>  thisRouteMap = splitRouteToArray(thisRoute);

                //只算急加速，急减速
                DriveData urgentSpeed = SafetyDrivingCheck.getSpeedCheck(deviceId ,(List<Double>) thisRouteMap.get("lon"),  (List<Double>) thisRouteMap.get("lat"), (List<Date>) thisRouteMap.get("time"));

                //todo:将计算的急加速，急减速存储起来
                if(urgentSpeed != null){

                    DrivingLabelDao.saveRouteUrgentSpeedLabel("deviceId", urgentSpeed);
                }

                updateCurrentRoute(thisRouteMap,thisRoute);

                continue;


            }

            Date  thisDate = thisPoint.getDate();

            List<Double> lon  = null;
            List<Double> lat  = null;
            List<Date>   time = null;


            DriveData  urgentSpeed = null;
            DriveData  urgentSharpTurn  = null;


            // 如果此时的上一段行程不为空，但是上一段行程与现在的行程不是属于同一个设备,或者此时的这段行程与上一段的时间差过大。 则也是进行单独的计算
            if(lastRoute != null
                    &&  lastRouteDeviceId.equals(deviceId) || DateUtils.getDiffDate(thisDate, lastRouteTime, 12) < 10){


                Map<String, Object> thisRouteMap = splitRouteToArray(thisRoute);

                lon  = (List<Double>) thisRouteMap.get("lon");
                lat  = (List<Double>) thisRouteMap.get("lat");
                time = (List<Date>)thisRouteMap.get("time");


                //急加速,急减速
                urgentSpeed = SafetyDrivingCheck.getSpeedCheck(deviceId,lon, lat, time);



                //急转弯，在计算急转弯的时候，需要将上次行程的结束点插入现有的行程之中
                urgentSharpTurn = SafetyDrivingCheck.getSharpTurnCheck( deviceId,lastRouteLastPoint,lon, lat, time);

                if(urgentSpeed != null){

                    DrivingLabelDao.saveRouteUrgentSpeedLabel(deviceId, urgentSpeed);
                }

                if(urgentSharpTurn != null){

                    //将标签数据存储到hbase中
                    DrivingLabelDao.saveRouteUrgentSharpTurn(deviceId, urgentSharpTurn);
                }



            }else {

                //如果不满足条件
                Map<String, Object> thisRouteMap = splitRouteToArray(thisRoute);

                System.out.println("正在计算急加速和急减速");
                //急加速,急减速
                urgentSpeed = SafetyDrivingCheck.getSpeedCheck(deviceId,lon, lat, time);

                //将此行程更新为上次
                updateCurrentRoute(thisRouteMap,thisRoute);
                continue;

            }


        }

        
        //此时已经拿到急加速，急转弯的标签。在这时我们需要开始关联出相关数据
        
        
        
        
        
        
        
        

    }




    /**
     * @desc 将行程切割成经度数组的集合和纬度数组的集合和时间数组的集合
     * @param route
     * @return  返回类型为Map，里面的key为 "lon", "lat" ,"time"
     */
    public static   Map<String,Object> splitRouteToArray(List<LngAndLat>  route){

        Map<String, Object> resultMap = new HashMap<>();

        int size = route.size();

        List<Double> lon  = new ArrayList<>();
        List<Object> lat  = new ArrayList<>();
        List<Date>   time = new ArrayList<>();


        for (int i = 0; i < size; i++) {

            LngAndLat lngAndLat = route.get(i);
            lon.add(lngAndLat.getLng());
            lat.add(lngAndLat.getLat());
            time.add(lngAndLat.getDate());
        }


        resultMap.put("lon", lon);
        resultMap.put("lat", lat);
        resultMap.put("time", time);


        return  resultMap;

    }


    /**
     * @desc 更新当前行程
     */
    public void updateCurrentRoute(Map<String,Object> thisRouteMap, List<LngAndLat> thisRoute){

        //将此次行程的数据更新
        lastRoute = thisRouteMap;

        //获取最后一个点，本次行程的最后一个点 为了方便在计算急转弯的时候进行计算
        LngAndLat lngAndLat = thisRoute.get(thisRoute.size() - 1);
        lastRouteDeviceId = lngAndLat.getDeviceId();
        lastRouteLastPoint = lngAndLat;
        lastRouteTime = lngAndLat.getDate();


    }














}
