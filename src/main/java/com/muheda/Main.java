package com.muheda;


import com.muheda.AsynchronousTripCache.CacheQueueHandle;
import com.muheda.AsynchronousTripCache.ConsumeCacheGenerateRouteLabel;
import com.muheda.dao.HbaseDao;
import com.muheda.dataSourece.DataPreDealWith;
import com.muheda.domain.LngAndLat;
import com.muheda.domain.Road;
import com.muheda.service.DealWithRoute;
import com.muheda.utils.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.SQLException;
import java.util.*;

public class Main {


    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private  static  int roadNum = 0;


    private  static   CacheQueueHandle cacheQueueHandle = new CacheQueueHandle();



    public static void main(String[] args) {


        new ConsumeCacheGenerateRouteLabel().startThreadConsume();

        //数据预处理阶段，获取数据源
        List<LngAndLat> lngAndLats = DataPreDealWith.datareProcessingFromConfigureFile();

        String deviceId = "010001";
        //传入设备， 因为在存储行程数据的时候需要
        startFixRoad(lngAndLats, deviceId);


        //扫描一个设备的数据从当天开始往之前开始遍历

        //从hbase中获取


//        DataPreDealWith.datareProcessingFromHbase("")





    }


    //实时的数据数据修复的开发




    /**
     * @desc 之前的代码基本上不需要进行优化了
     */
    public  static  void startFixRoad(List<LngAndLat> route, String deviceId) {



        //数据预处理阶段，获取数据源
        List<LngAndLat> lngAndLats = DataPreDealWith.datareProcessingFromConfigureFile();

        /**
         * @desc 按照时间先将路按照时间进行分段处理。再按照路径的距离进行划分
         */
        List<List<LngAndLat>> routeByroads = DealWithRoute.splitRoadByTime(lngAndLats);


        //在这些按照路段分割的行程之内。再次按照距离进行分割。以免出现一些关于设备出现的其他的问题。比如有的设备会经常发送一些经纬度为0的数据
        List<List<LngAndLat>> lists = DealWithRoute.fineGrainPathSegmentation(routeByroads);


        //需要将切分割完之后的路段。根据判断该行驶路程是否出现拐弯的情况，再进行分路段分割，以确保每一段路是在一条路上
        List<List<LngAndLat>> routesOneWay = DealWithRoute.splitRoadByDirection(lists);


        for (List<LngAndLat> list : routesOneWay) {

            //遍历list
            System.out.println("路径编号:" + roadNum++ );

            System.out.println("原始路径数据 ");
            for (LngAndLat lngAndLat : list) {

                System.out.print("[" + lngAndLat.getLng() + "," + lngAndLat.getLat() + "]" + ",");

            }

            System.out.println();


            //求出该条路段的最小包含矩形
            List<LngAndLat> min = MapUtils.minimumRectangle(list);

            List<Road> matchRoads = null;


            try {
                //拿到的匹配的路
                matchRoads = DealWithRoute.findRoutesByMinRectangle(min.get(0), min.get(1));

            } catch (SQLException e) {
                logger.error("查询匹配路段异常");
                e.printStackTrace();
            }


            int index = DealWithRoute.averageDistanceTop2(list, matchRoads);

            // 表示的是再已有的路段中没有找到符合的路段
           /* if (index == -1) {

                //使用最下矩形的的距离的判断方法进行寻找可以匹配的路段
                List<Road> roads = DealWithRoute.finRouteByDistance(min.get(0), min.get(1));

                 int index2 = DealWithRoute.averageDistanceTop2(list, matchRoads);

                 if(index2 == -1){
                     continue;
                 }

                 // 如果此时还是没有匹配到路径,那么判定找不到对应的路了
                 index = index2;

             }
           */

            if(index == -1){

                continue;
            }


            Road road = matchRoads.get(index);


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


            for (Integer integer : mappingIndex) {

                System.out.println(integer);
            }


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
            HbaseDao.saveDeviceRoute( deviceId,list, repairedList, mappingRoad, road.getId());



            /**
             * 并且将原始数据的时间添加到路网数据的点上
             * 进行计算的时候是将原始数据和路网数据交织在一起相互进行补充进行修复的
             * 因为行程数据本身由于时间间隔过长等的原因，会造成如何拿这些数据计算可能会造成后面的计算的不准确。故将2者的点进行交互交织
             */


            //将此时的三急数据对应的路段发送到一个异步处理的程序中，这边只是做一个发送的作用,发送到一个异步的队列中

            cacheQueueHandle.appendToQueue(repairedList);


            //


        }

    }


    /**
     * @desc 将原始数据和路网映射数据进行结合
     */


}







