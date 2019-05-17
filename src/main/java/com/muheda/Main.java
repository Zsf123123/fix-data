package com.muheda;


import com.muheda.AsynchronousTripCache.CacheQueueHandle;
import com.muheda.AsynchronousTripCache.ConsumeCacheGenerateRouteLabel;
import com.muheda.dao.HbaseDao;
import com.muheda.dataSourece.DataPreDealWith;
import com.muheda.domain.LngAndLat;
import com.muheda.domain.Road;
import com.muheda.domain.RoadInfo;
import com.muheda.service.DealWithRoute;
import com.muheda.utils.DateUtils;
import com.muheda.utils.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.SQLException;
import java.util.*;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private  static  CacheQueueHandle cacheQueueHandle = new CacheQueueHandle();

    private  static  List<String>  allDeviceId;

    //用于缓存当前修复到哪一天了
    private static String  tempDay;


    public static void main(String[] args) {

        //开启一个消费数据标签队列的线程
        new ConsumeCacheGenerateRouteLabel().startThreadConsume();

        //获取该设备的目前的所有的设备号
        allDeviceId = HbaseDao.getAllDeviceId("device_register_info", "getuyunjing", "deviceId", "gt");

        tempDay = DateUtils.getTodayTime();

        //从现在开始，往回进行查询每一天的数据进行修复
        while (true){

            for (String deviceId : allDeviceId) {
                //获取当前正在修复的时间，拼接成rowKey，进行数据的查询
                String startRow = "gt" + "_" + deviceId + "_" + tempDay;
                String endRow   = "gt" + "_" + deviceId + "_" + tempDay + "z";

                List<LngAndLat> lngAndLats = DataPreDealWith.datareProcessingFromHbase(startRow, endRow);

                if(  lngAndLats != null && lngAndLats.size() > 0){
                    startFixRoad(lngAndLats,deviceId);
                }
            }

            tempDay = DateUtils.getTheDayBeforeYesterday(tempDay);

        }

    }

    /**
     * @desc 之前的代码基本上不需要进行优化了
     */
    public  static  void startFixRoad(List<LngAndLat> route, String deviceId) {


        //数据预处理阶段，获取数据源
//        List<LngAndLat> lngAndLats = DataPreDealWith.datareProcessingFromConfigureFile();

        /**
         * @desc 按照时间先将路按照时间进行分段处理。再按照路径的距离进行划分
         */
        List<List<LngAndLat>> routeByroads = DealWithRoute.splitRoadByTime(route);


        //在这些按照路段分割的行程之内。再次按照距离进行分割。以免出现一些关于设备出现的其他的问题。比如有的设备会经常发送一些经纬度为0的数据
        List<List<LngAndLat>> lists = DealWithRoute.fineGrainPathSegmentation(routeByroads);


        //需要将切分割完之后的路段。根据判断该行驶路程是否出现拐弯的情况，再进行分路段分割，以确保每一段路是在一条路上
        List<List<LngAndLat>> routesOneWay = DealWithRoute.splitRoadByDirection(lists);


        for (List<LngAndLat> list : routesOneWay) {

            System.out.println("原始路径数据 ");
            for (LngAndLat lngAndLat : list) {

                System.out.print("[" + lngAndLat.getLng() + "," + lngAndLat.getLat() + "]" + ",");

            }

            System.out.println();


            //求出该条路段的最小包含矩形
            List<LngAndLat> min = MapUtils.minimumRectangle(list);

            List<Road> matchRoads = null;

            // 确定出该路段的一个区域

//            String routeAdcode = DealWithRoute.findRouteAdcode(min.get(0), min.get(1));

            try {

                //拿到的匹配的路
                matchRoads = DealWithRoute.findRoutesByMinRectangle(min.get(0), min.get(1));

            } catch (SQLException e) {
                logger.error("查询匹配路段异常");
                e.printStackTrace();
            }

            int index = DealWithRoute.averageDistanceTop2(list, matchRoads);

            // 如果匹配不上路网中的路段则直接使用未经修复的路段
            if(index == -1){

                Map<RoadInfo, List<LngAndLat>> map = new HashMap<>();
                RoadInfo roadInfo = new RoadInfo();
                roadInfo.setMatch(false);
                roadInfo.setAdcode(null);
                map.put(roadInfo,list);
                
                cacheQueueHandle.appendToQueue(map);
                
                continue;
            }


            Road road = matchRoads.get(index);

            //该路段所对应的实际路段的地区编号
            String adcode = road.getAdcode();


            LinkedList<LngAndLat> roadList = new LinkedList<>();


            //将取出来的路段转成集合的格式方便后面进行计算而避免转换
            String[] split = road.getShape().split(";");


            System.out.println("匹配到的路网数据：");
            System.out.println(road.getName());
            for (int i = 0; i < split.length; i++) {

                String[] point = split[i].split(",");

                if (point.length == 2 && point[0] != null && point[1] != null) {

                    roadList.add(new LngAndLat(Double.parseDouble(point[0]), Double.parseDouble(point[1])));


                    //获取到匹配的原始的路径
                    System.out.print("[" + point[0] + "," + point[1] + "]" + ",");

                }

            }


            System.out.println();

            Map<String, Object> resultMap = DealWithRoute.fixDataAction(list, road.getShape());

            List<LngAndLat> repairedList = (List<LngAndLat>) resultMap.get("repairedList");

            List<Integer> mappingIndex = (List<Integer>) resultMap.get("mappingIndex");


            //找出行程的起始的点对应的路网的路网的点的index和终点上的index,从而切出来映射的整个路段
            Integer start = mappingIndex.get(0);
            Integer end = mappingIndex.get(mappingIndex.size() - 1);


            /**如果 start < end 则行程的方向与路径的方向相同反之,则end作为开始idnex,start作为结束的index，将数据取出来之后进行倒序。倒序之后的路段才是真正匹配上的路段
             * 出现这种情况的原因是所匹配的路是一条可以双向行驶的道路，在进行截取的时候可能会出现这种问题，但是不影响修复过程
             **/


            List<LngAndLat> mappingRoad = null;

            if (start < end) {
                mappingRoad = roadList.subList(start, end);
            } else {
                mappingRoad = roadList.subList(end, start);

                //进行反转
                Collections.reverse(mappingRoad);
            }


            //找出行程与切完之后的最近的映射的点，并给这些的点打上时间的标签，存的使用使用的是[位置：time]

            //真实映射到的对应的路

            System.out.println("真实的路段：");


            for (LngAndLat point : mappingRoad) {

                System.out.print("[" + point.getLng() + "," + point.getLat() + "]" + ",");

            }


            //将原始数据, 修复之后的数据，映射的路网的数据都给存储到表中
            HbaseDao.saveDeviceRoute(deviceId,list, repairedList, mappingRoad, road.getId());

            
            if(repairedList != null){

                Map<RoadInfo, List<LngAndLat>> map = new HashMap<>();
                RoadInfo roadInfo = new RoadInfo();
                roadInfo.setAdcode(adcode);
                roadInfo.setMatch(true);

                map.put(roadInfo,repairedList);

                cacheQueueHandle.appendToQueue(map);
            }



        }

    }




}







