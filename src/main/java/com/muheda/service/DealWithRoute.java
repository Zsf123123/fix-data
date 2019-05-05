package com.muheda.service;

import clojure.lang.Obj;
import com.muheda.dao.DbPoolConnection;
import com.muheda.dao.HbaseDao;
import com.muheda.domain.LngAndLat;
import com.muheda.domain.Road;
import com.muheda.utils.MapUtils;
import com.muheda.utils.StringUtils;
import com.muheda.utils.TimeUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;


/**
 * @desc 根据设备号和时间查询该设备的相关数据
 */

public class DealWithRoute {

    private static Logger logger = LoggerFactory.getLogger(DealWithRoute.class);


    /**
     * @desc 拿到当前的路线和匹配到的路线
     */
//    public static void routeMappingMatch(List<LngAndLat> list, List<Road> matchRoads){
//
//
//        // 匹配到的路线的集合
////        Set<String> undetermineoaRds = DealWithRoute.findRoadByOneRoute(list, matchRoads);
//
//
//        DealWithRoute.averageDistanceTop2(list,matchRoads);
//
//
//        if (undetermineoaRds.size() == 0) {
//
//            System.out.println("----");
//        }
//
//
//        String shapeIdd = null;
//
//
//        //找到了该段路所对应的真实的路
//        for (String shapeId : undetermineoaRds) {
//
//            System.out.println("匹配到的路网数据：" + shapeId);
//            shapeIdd = shapeId;
//
//        }
//
//
//        //拿到了之后，有可能是有多条匹配的路,也有可能是没有合适的路线
//
//        //找到之后进行映射,修复路网数据
//
//
//        if( shapeIdd != null){
//
//            DealWithRoute.fixDataAction(list,shapeIdd);
//
//        }
//
//
//
//    }

    /**
     * @desc 将一段行程切割成若干段更小的路, 按照五公里进行切割分段, 如果切分到最后一个点的时候没有到5公里, 那么就直接作为一段路程
     */
    public static List<List<LngAndLat>> splitRoutesWithDistance(List<LngAndLat> list) {

        if (list.size() <= 1) {
            return null;
        }

        List<List<LngAndLat>> resultList = new LinkedList<>();
        Double tempDistance = 0.0;

        //分段时的指针  from 代表的是一段路的起始index,to代表的是分段的终止的index
        int from = 0;
        int to = 0;

        //从第二个点开始进行计算
        for (int i = 1; i < list.size(); i++) {

            // current point
            LngAndLat current = list.get(i);
            //last point
            LngAndLat last = list.get(i - 1);

            //计算与前一个点之间的距离
            double distance = MapUtils.getDistance(current.getLng(), current.getLat(), last.getLng(), last.getLat());
            tempDistance += distance;

            //如果此时是最后一个点，并且此时的tempDistance 还没有达到临界值。那么也需要分段处理
            if (i == list.size() && tempDistance < 100) {
                resultList.add(list.subList(from, i + 1));
            }


            if (tempDistance > 100) {

                //将切分路段的结束指针执行此时的index,并将这一段的数据添加到返回的集合中
                to = i;

                resultList.add(list.subList(from, to + 1));

                tempDistance = 0.0;
                //将下一个分段路程的from设置为上一段路的to
                from = to;
            }
        }

        return resultList;

    }


    /**
     * @desc 通过最小矩形去数据库匹配出相应的路出来
     */
    public static List<Road> findRoutesByMinRectangle(LngAndLat min, LngAndLat max) throws SQLException {


        String sql = "select * from roads_v6 where " + min.getLng() + "> min_x and " + min.getLat() + "> min_y and " + max.getLng() + "< max_x and " + max.getLat() + "< max_y;";
        return DbPoolConnection.findRoutesByRectangle(sql);

    }


    /**
     * @desc 在进行匹配的时候，如果行程的路段只是数据库中路网分段路程的一部分，那么直接使用矩形进行匹配路径就可以了
     * 但是如果行程与所路网中的分段行程之间长度和大小非常相近,可能行程所围成的矩形并不是匹配路径的一个子集。那么这种情况下就无法匹配到相应的路吗，从而造成行程无法进行修复
     * 所以在此基础上还需要加上距离之内的校验
     *
     * @warn 但是值得注意的是 使用这种方法进行查询数据库是非常耗时的，在数据量很大的情况下不建议使用。
     */
    public static List<Road> finRouteByDistance(LngAndLat min, LngAndLat max) {


        String sql = "select * from \n" +
                "( SELECT *,point2point_distance(min_x,min_y," + min.getLng() + "," + min.getLat() + ") as distance1 , " +
                "point2point_distance(max_x,max_y," + max.getLng() + "," + max.getLat() + ") as distance2 from osm_road_v2) temp\n" +
                "where distance1 > 0 AND distance1 < 0.05 AND distance2 > 0 ANd distance2 < 0.05 ";


        System.out.println(sql);

        return DbPoolConnection.findRoutesByRectangle(sql);

    }


    /**
     * @param lngAndLat
     * @param road
     * @return 返回点到路该距离的2个最小值
     * @desc 目标： 在寻找一个点到一条路上的最短的2个距离所对应的点的坐标
     * @desc 取值的时候 min:最小值  second:倒数第二小的值
     */
    public static Map<String, Map<String, Object>> minPointToRoadDistance(LngAndLat lngAndLat, String road) {

        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        //最短的距离
        double minDistance = 0.0;

        //第二短的距离
        double secondDistance = 0.0;

        //最短距离所对应的点的索引
        int minIndex = 0;

        //次短距离所对应的点的索引
        int secondIndex = 0;

        //最短距离的点所对应的点
        LngAndLat minDistancePoint = new LngAndLat();

        //此段距离所对应的点
        LngAndLat secondDistancePoint = new LngAndLat();

        //取出最开始的2个距离，比较其中的最小的值和第二小队的值。初始化上述字段

        String[] split = road.split(";");


        if (split.length <= 0) {

            return null;

        } else if (split.length == 1) {

            //此时的点的距离就是最短的距离
            String[] point = split[0].split("，");
            double distance = MapUtils.getDistance(Double.parseDouble(point[0]), Double.parseDouble(point[1]), lngAndLat.getLng(), lngAndLat.getLat());
            minDistance = distance;
            minIndex = 0;


        } else if (split.length == 2) {

            //如果点的个数是为2的，则将确定出一个是最大值和最小值

            String[] point1 = split[0].split(",");
            double distance1 = MapUtils.getDistance(Double.parseDouble(point1[0]), Double.parseDouble(point1[1]), lngAndLat.getLng(), lngAndLat.getLat());

            String[] point2 = split[1].split(",");
            double distance2 = MapUtils.getDistance(Double.parseDouble(point1[0]), Double.parseDouble(point2[1]), lngAndLat.getLng(), lngAndLat.getLat());

            if (distance1 < distance2) {
                minDistance = distance1;
                secondDistance = distance2;

                minIndex = 0;
                secondIndex = 1;
            } else {
                minDistance = distance2;
                secondDistance = distance1;

                minIndex = 1;
                secondIndex = 0;
            }


        }


        // 进行大于2个点的初始化操作
        if (split.length >= 2) {


            String[] point1 = split[0].split(",");
            double distance1 = MapUtils.getDistance(Double.parseDouble(point1[0]), Double.parseDouble(point1[1]), lngAndLat.getLng(), lngAndLat.getLat());

            String[] point2 = split[1].split(",");
            double distance2 = MapUtils.getDistance(Double.parseDouble(point1[0]), Double.parseDouble(point2[1]), lngAndLat.getLng(), lngAndLat.getLat());

            if (distance1 < distance2) {
                minDistance = distance1;
                secondDistance = distance2;

                minIndex = 0;
                secondIndex = 1;
            } else {
                minDistance = distance2;
                secondDistance = distance1;

                minIndex = 1;
                secondIndex = 0;
            }


        }

        // 如果该路的点数大于2，则会继续寻找
        for (int i = 2; i < split.length; i++) {

            String[] point = split[i].split(",");
            double distance = MapUtils.getDistance(Double.parseDouble(point[0]), Double.parseDouble(point[1]), lngAndLat.getLng(), lngAndLat.getLat());

            if (distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }

            if (distance > minDistance && distance < secondDistance) {
                secondDistance = distance;
                secondIndex = i;
            }

        }

        //进行装配返回数据

        if (minIndex >= 0 && minIndex <= split.length) {
            String[] point = split[minIndex].split(",");
            minDistancePoint.setLng(Double.parseDouble(point[0]));
            minDistancePoint.setLat(Double.parseDouble(point[1]));


        }

        if (secondIndex >= 0 && secondIndex <= split.length) {
            String[] point = split[secondIndex].split(",");
            secondDistancePoint.setLng(Double.parseDouble(point[0]));
            secondDistancePoint.setLat(Double.parseDouble(point[1]));
        }





        Map<String, Object> min = new HashMap<>();
        min.put("minDistance", minDistance);
        min.put("minDistancePoint", minDistancePoint);
        min.put("minIndex", minIndex);
        resultMap.put("min", min);


        Map<String, Object> second = new HashMap<>();

        second.put("secondDistance", secondDistance);
        second.put("secondDistancePoint", secondDistancePoint);
        resultMap.put("second", second);

        return resultMap;

    }


    /**
     * @desc 找到行程上的每个点距离路上的距离最短的后2个点
     * @return  直接返回最短的垂足
     */
    public static LngAndLat minPointToRoadFootDistance(LngAndLat lngAndLat, String road){

        double minDistance = 0.0;

        LngAndLat foot = null;

        String[] split = road.split(";");

        if(split.length < 2){

            return  null;
        }


        minDistance = MapUtils.pointToLineVerticalDistance(arrayToPointObject(split[0]), arrayToPointObject(split[1]), lngAndLat);
        foot = MapUtils.getFoot(arrayToPointObject(split[0]), arrayToPointObject(split[1]), lngAndLat);

        for (int i = 1; i < split.length - 1 ; i++) {


            double distance = MapUtils.pointToLineVerticalDistance(arrayToPointObject(split[i]), arrayToPointObject(split[i + 1]), lngAndLat);

            if(distance < minDistance){

                minDistance = distance;
                foot = MapUtils.getFoot(arrayToPointObject(split[i]), arrayToPointObject(split[i+1]), lngAndLat);

            }

        }

        return  foot;
    }

    /**
     * @param list
     * @param roads
     * @desc 选出这段行程与每一段路的最短距离之和的平均值的前两条路。如果这两条路是属于同一短路的不同方向，则进行行程与路段匹配算法进行验证取出最佳的匹配路段
     * 如果前两条路是不同的路段则直接取最近的路。并且如果平均距离如果大于某一个临界值则放弃匹配。说明该路段属于不能修复的额路段
     */
    public static int averageDistanceTop2(List<LngAndLat> list, List<Road> roads) {


        Map<String, Object> resultMap = new HashMap<>();

        //最短的距离
        Double minAverageDistance = null;

        //倒数第二的距离
        Double secondLastAverageDistance = null;

        Integer minIndex = null;

        Integer secondLastIndex = null;

        if (roads.size() == 0) {

            return -1;
        }


        if (roads.size() == 1) {

            minAverageDistance = findMinDistanceByPointToRoad(list, roads.get(0));
        }

        if (roads.size() >= 2) {

            double distance0 = findMinDistanceByPointToRoad(list, roads.get(0));

            double distance1 = findMinDistanceByPointToRoad(list, roads.get(1));

            if (distance0 > distance1) {

                minAverageDistance = distance1;
                minIndex = 1;

                secondLastAverageDistance = distance0;
                secondLastIndex = 0;

            } else {

                minAverageDistance = distance0;
                minIndex = 0;

                secondLastAverageDistance = distance1;
                secondLastIndex = 1;

            }


            for (int i = 2; i < roads.size(); i++) {


                double minDistanceByPointToRoad = 0.0;

                try{

                    Double minDistanceByPointToRoad1 = findMinDistanceByPointToRoad(list, roads.get(i));

                    if(minDistanceByPointToRoad1 == null){

                        continue;
                    }else {

                        minDistanceByPointToRoad = minDistanceByPointToRoad1;
                    }


                }catch (Exception e){
                    System.out.println( e.getCause());
                    e.printStackTrace();
                }



                if (minDistanceByPointToRoad < minAverageDistance) {

                    minAverageDistance = minDistanceByPointToRoad;

                    //并记录下路的编号
                    minIndex = i;

                    continue;
                }


                if (minDistanceByPointToRoad < secondLastAverageDistance) {

                    secondLastAverageDistance = minDistanceByPointToRoad;
                    secondLastIndex = i;

                }


            }


        }


        //过滤数据，如果平均距离是大于100米的，则进行过滤操作
        if (minAverageDistance > 100) {

            return -1;
        }


        if (roads.size() == 1) {

            // 只有一个匹配的路径，那么直接返回第一个点的index 即0
            return 0;
        }

        if (minAverageDistance < 100 && secondLastAverageDistance > 100) {

            //返回的是最短距离的
            return minIndex;
        }


        if (minAverageDistance < 100 && secondLastAverageDistance < 100) {

            //验证是否是属于同一条路的不同的方向的路段
            //如果不是，直接返回最短的距离对应
            if (!roads.get(minIndex).getId().equals(roads.get(secondLastIndex).getId())) {
                return minIndex;
            } else {

                // 如果现在匹配到两条相同的路径的不同方向，则进行路径方向匹配

                // 取出前2条匹配的路径，和自己的行程的前2个点

                if (list.size() <= 1) {
                    return -1;
                }

                int i = matchRouteWithoutDirection(list, roads.get(minIndex), roads.get(secondLastIndex));


                // 调用未知路径方向匹配路径 //
                if (i == 0) {

                    return minIndex;
                } else {
                    return secondLastIndex;
                }


            }


        }


        return minIndex;

    }


    /**
     * @desc 实现未知方向下的路径匹配算法
     *
     * @param road1 需要进行匹配的路径一
     * @param road2 需要进行匹配的路径二
     * @param list  车辆行驶的行程
     */
//    public static  void matchRouteWithoutDirection( List<LngAndLat> list, Road road1, Road road2) {
//
//        //合法性处理
//        if (list.size() < 2) {
//            return;
//        }
//
//        //取出行程的前2个点，求出表示该段行程的向量
//        LngAndLat routePoint1 = list.get(0);
//        LngAndLat routePoint2 = list.get(1);
//
//        //设置需要寻找的点设置为 x2,y2
//        double x2 = 0.0;
//        double y2 = 0.0;
//
//
//        //向量a
//        double a_x = routePoint2.getLng() - routePoint1.getLng();
//        double a_y = routePoint2.getLat() - routePoint1.getLat();
//
//        //向量b
//        double b_x = x2 - routePoint1.getLng();
//        double b_y = y2 - routePoint1.getLat();
//
//
//        //2个向量垂直
//        double v = 0.0;
//        v = a_x * (x2 - routePoint1.getLat()) + (a_y * (y2 - routePoint1.getLat()));
//
//        //由此可以求出 用y2 表示出x2
//        x2 = (-(a_y * (y2 - routePoint1.getLat())) / a_x) + routePoint1.getLat();
//
//        //有因为可以行程一个圆
//        v = x2 * x2 + y2 * y2 - a_x * a_x + a_y * a_y;
//
//        //所以有
//        double x = (-(a_y * (y2 - routePoint1.getLat())) / a_x) + routePoint1.getLat() * (-(a_y * (y2 - routePoint1.getLat())) / a_x) + routePoint1.getLat() - a_x * a_x + a_y * a_y;
//
//
//        // 这种方式下的计算任务表达起来非常繁琐，不利于计算机识别，所有采取其他的解决方案
//
//    }


    /**
     * @param list
     * @param road1
     * @param road2
     * @desc 实现未知方向下的路径匹配算法
     */
    public static int matchRouteWithoutDirection(List<LngAndLat> list, Road road1, Road road2) {


        //取出行驶路径的前2个点
        LngAndLat routePoint1 = list.get(0);
        LngAndLat routePoint2 = list.get(1);


        //根据这2个点求出这2个点的法向量（垂直的向量）
        LngAndLat routeDirectorVector = new LngAndLat();

        routeDirectorVector.setLng(routePoint2.getLng() - routePoint1.getLng());
        routeDirectorVector.setLat(routePoint2.getLat() - routePoint1.getLat());

        //根据此时的向量找出逆时针旋转90度之后的向量
        LngAndLat verticalVector = counterclockwiseRotate90(routeDirectorVector);

        //找出经过该向量的2个点的指向方程

        //取出匹配到表示2个路段的2个向量

        //第一条路
        Double road1Point1Lng = Double.parseDouble(road1.getShape().split(";")[0].split(",")[0]);
        Double road1Point1Lat = Double.parseDouble(road1.getShape().split(";")[0].split(",")[1]);

        Double road1Point2Lng = Double.parseDouble(road1.getShape().split(";")[1].split(",")[0]);
        Double road1Point2Lat = Double.parseDouble(road1.getShape().split(";")[1].split(",")[1]);

        //确定出路段1的向量
        LngAndLat road1Vector = new LngAndLat(road1Point2Lng - road1Point1Lng, road1Point2Lat - road1Point1Lat);


        //第一条路
        Double road2Point1Lng = Double.parseDouble(road2.getShape().split(";")[0].split(",")[0]);
        Double road2Point1Lat = Double.parseDouble(road2.getShape().split(";")[0].split(",")[1]);

        Double road2Point2Lng = Double.parseDouble(road2.getShape().split(";")[1].split(",")[0]);
        Double road2Point2Lat = Double.parseDouble(road2.getShape().split(";")[1].split(",")[1]);

        //确定出路段1的向量
        LngAndLat road2Vector = new LngAndLat(road2Point2Lng - road2Point1Lng, road2Point2Lat - road2Point1Lat);

        //计算出行程向量与路段的向量的交点

        //行程向量与路段一的交点

        LngAndLat intersection1 = intersectionBetweenTwoLines(new LngAndLat(road1Point1Lng, road1Point1Lat), new LngAndLat(road1Point2Lng, road1Point2Lat), routePoint1, routePoint2);

        LngAndLat intersection2 = intersectionBetweenTwoLines(new LngAndLat(road2Point1Lng, road2Point1Lat), new LngAndLat(road2Point2Lng, road2Point2Lat), routePoint1, routePoint2);


        //确定交点的向量
        LngAndLat intersectionVector = new LngAndLat(intersection2.getLng() - intersection1.getLng(), intersection2.getLat() - intersection1.getLng());


        //计算出 交点向量与垂直向量之间的夹角，如果夹角的范围在 0到90度 则行程所在的路段就是 road1 否则就是road2
        double v = CalculaeVectorAngel(verticalVector.getLng(), verticalVector.getLat(), intersectionVector.getLng(), intersectionVector.getLat());

        if (0 < v && v < 90) {

            return 1;
        } else {
            return 0;
        }


    }


    /**
     * @return 旋转之后的向量
     * @desc 传过来的向量按照逆时针旋转90度之后所获得的向量
     */
    public static LngAndLat counterclockwiseRotate90(LngAndLat lngAndLat) {

        Double lng = lngAndLat.getLng();
        Double lat = lngAndLat.getLat();

        //无论向量是在哪一个象限中，都满足 一下的转换条件，具体原理自己推理
        LngAndLat resultLngAndLat = new LngAndLat();

        resultLngAndLat.setLng(-lat);
        resultLngAndLat.setLat(lng);

        return resultLngAndLat;

    }


    /**
     * @desc 计算2条直线之间的交点的坐标
     */
    public static LngAndLat intersectionBetweenTwoLines(LngAndLat point1, LngAndLat point2, LngAndLat point3, LngAndLat point4) {

        //第一条直线
        double x1 = point1.getLng(), y1 = point1.getLat(), x2 = point2.getLng(), y2 = point2.getLat();

        double a = (y1 - y2) / (x1 - x2);
        double b = (x1 * y2 - x2 * y1) / (x1 - x2);
        System.out.println("求出该直线方程为: y=" + a + "x + " + b);

//第二条
        double x3 = point3.getLng(), y3 = point3.getLat(), x4 = point4.getLng(), y4 = point4.getLat();

        double c = (y3 - y4) / (x3 - x4);
        double d = (x3 * y4 - x4 * y3) / (x3 - x4);
        System.out.println("求出该直线方程为: y=" + c + "x + " + d);

        double x = ((x1 - x2) * (x3 * y4 - x4 * y3) - (x3 - x4) * (x1 * y2 - x2 * y1))
                / ((x3 - x4) * (y1 - y2) - (x1 - x2) * (y3 - y4));

        double y = ((y1 - y2) * (x3 * y4 - x4 * y3) - (x1 * y2 - x2 * y1) * (y3 - y4))
                / ((y1 - y2) * (x3 - x4) - (x1 - x2) * (y3 - y4));

        return new LngAndLat(x, y);
    }


    /**
     * @param list 行程s
     * @param road 路段
     * @desc 该方法的功能是求出该行程的每个点到该条路的平均距离
     * @desc 点到路的距离计算:找出点到相邻2个点所行成的直线的垂线距离
     */
    public static Double findMinDistanceByPointToRoad(List<LngAndLat> list, Road road) {


        Double average = 0.0;

        try {

          Double sum = 0.0;

          String[] split = road.getShape().split(";");

          // 如果该条路的点数小于等于1，那么这种情况没有必要进行计算了。
          if (split.length <= 2) {

              return null;
          }

          //通过行程的每个点去计算
          for (LngAndLat lngAndLat : list) {

              //该点到这条路的最短距离
              double minDistancestor;

              minDistancestor = MapUtils.pointToLineVerticalDistance(arrayToPointObject(split[0]), arrayToPointObject(split[1]), lngAndLat);

              //从第二段行程开始，到最后一个行程
              for (int i = 1; i < split.length - 1; i++) {

                  double distance = MapUtils.pointToLineVerticalDistance(arrayToPointObject(split[i]), arrayToPointObject(split[i + 1]), lngAndLat);

                  if (distance < minDistancestor) {
                      minDistancestor = distance;
                  }
              }
              sum += minDistancestor;

          }

          average = sum / list.size();
      }catch (Exception e){

          e.printStackTrace();
      }

        return  average;
    }


    /**
     * @param point
     * @return
     * @desc 将数组形式的封装成坐标对象
     */
    public static LngAndLat arrayToPointObject(String point) {

        String[] split = point.split(",");

        if (split.length != 2) {
            return null;
        }

        LngAndLat lngAndLat = new LngAndLat();

        lngAndLat.setLng(Double.parseDouble(split[0]));
        lngAndLat.setLat(Double.parseDouble(split[1]));

        return lngAndLat;

    }


    /**
     * 使用2中方案查看哪一种修复之后的效果是更好的
     *
     */

    /**
     * @param lngAndLat 传入的点
     * @param road      该条路上的点的坐标集合
     * @return
     * @desc 目标: 在寻找一个点到一条路上的最短的距离并且找到他前后的几个点的坐标
     */
    public static Map<String, LngAndLat> minPointWithNeighbourPionts(LngAndLat lngAndLat, String road) {


        //将最短距离所对应的点以及其周围的2个点都返回
        Map<String, LngAndLat> resultMap = new HashMap<>();

        int minIndex = 0;

        int leftIndex = 0;

        int rightIndex = 0;

        double minDistance = 0.0;

        //切分出该条路上的所有的点
        String[] split = road.split(";");


        for (int i = 0; i < split.length; i++) {

            String[] point = split[i].split(",");

            double distance = MapUtils.getDistance(Double.parseDouble(point[0]), Double.parseDouble(point[1]), lngAndLat.getLng(), lngAndLat.getLat());

            //将第一个产生的距离作为一开始的最短的距离
            if (i == 0) {
                minDistance = distance;
            } else {

                //在更新最小的距离的时候同时更新他的index
                if (distance < minDistance) {
                    minDistance = distance;
                    minIndex = i;
                }
            }

        }


        return resultMap;
    }

    /**
     * @return 返回的是按照时间进行分割的每一个行程
     * @desc 传入带时间的点，进行时间路段的切分
     */
    public static List<List<LngAndLat>> splitRoadByTime(List<LngAndLat> lngAndLats) {


        List<List<LngAndLat>> resultList = new LinkedList<>();


        //切分路段的开始标记
        int sectionStartIndex = 0;

        //切分路段的结束的标记
        int sectionEndIndex = 0;

        Date lastTime = null;


        if (lngAndLats.size() <= 0) {
            return null;
        }


        LngAndLat lngAndLat = lngAndLats.get(0);
        lastTime = lngAndLat.getDate();

        for (int i = 1; i < lngAndLats.size(); i++) {


            long difference = TimeUtils.timeDifference(lngAndLats.get(i).getDate(), lastTime);

            //如果时间之差大于10分钟则进行切分路段
            if (difference > 1) {

                List<LngAndLat> section = lngAndLats.subList(sectionStartIndex, i);

                resultList.add(section);

//                for (LngAndLat point : section) {
//                    System.out.print("[" + point.getLng() + "," + point.getLat() + "]" + ",");
//                }

                sectionStartIndex = i;

                //将切分完成之后的路段进行距离切分

//                System.out.println();

            } else {

                //如果此时是最后一个点了，并且到目前为止还没有结束此时的这段行程。此时就应该将剩下的所有的点进行切分为一段路程
                if (i == lngAndLats.size() - 1) {

                    //此时切分出的应该是最后一段路程
                    List<LngAndLat> lastSection = lngAndLats.subList(sectionStartIndex, i);

                    resultList.add(lastSection);

                    // 再次进行距离的切分路段
                    for (LngAndLat point : lastSection) {
//                        System.out.print("[" + point.getLng() + "," + point.getLat() + "]" + ",");
                    }

                }

            }


            //更新上一个坐标的时间
            lastTime = lngAndLats.get(i).getDate();

        }

        return resultList;

    }

    /**
     * @desc 在按照时间分割完成之后的路程的基础之上更加细粒度的路程分割，排除一些异常的设备发送的特殊数据
     * Fine-grained path segmentation
     */
    public static List<List<LngAndLat>> fineGrainPathSegmentation(List<List<LngAndLat>> lists) {

        LinkedList<List<LngAndLat>> resultLists = new LinkedList<>();

        for (List<LngAndLat> list : lists) {

            //拿到一堆按照时间分割的点，再按照距离或者特殊异常的数据进行分割路程

            List<List<LngAndLat>> lists1 = dealWithRoutereventException(list);

            //将此时的多段行程加入resultList中

            for (List<LngAndLat> list1 : lists1) {

                resultLists.add(list1);
            }

        }

        return resultLists;
    }


    /**
     * @return
     * @desc 检测行程是否需要进行路程的分割
     */
    public static List<List<LngAndLat>> dealWithRoutereventException(List<LngAndLat> list) {


        List<List<LngAndLat>> resultLists = new LinkedList<List<LngAndLat>>();

        int start = 0;

        //从第二个点开始进行验证
        for (int i = 1; i < list.size(); i++) {


            //如果验证该点有问题
            if (!verifLegalPoint(list.get(i - 1), list.get(i))) {

                resultLists.add(list.subList(start, i));
                start = i;
            }
        }

        //在遍历之后查看是否还存在没有最后一段行程没有放入返回的集合中
        if (start < list.size() - 1) {

            resultLists.add(list.subList(start, list.size() - 1));

        }

        return resultLists;
    }

    /**
     * @param pre     当前点的前一个点
     * @param current 当前的点
     * @return true 表示此点没有问题
     * @desc 验证一个点是不是合法的点
     */
    public static boolean verifLegalPoint(LngAndLat pre, LngAndLat current) {


        //如果是某个点的经纬度之一为0.0 或者该点距离上一个点的距离大于一个临界值 1000m （暂定，后期写入配置文件中）
        return current.getLng() != 0.0 && current.getLat() != 0.0 && !(MapUtils.getDistance(current.getLng(), current.getLat(), pre.getLng(), pre.getLat()) > 1000);

    }

    /**
     * @desc 将一段路按照向量角度分割出不同的路
     */
    public static List<List<LngAndLat>> makeOneRoadToSomeRoad(List<LngAndLat> list) {


        List<List<LngAndLat>> resultList = new LinkedList<>();

        // 前一个向量
        LngAndLat preVector = new LngAndLat();

        //后一个向量
        LngAndLat nextVector = new LngAndLat();


        // 截取的开始的index
        int from = 0;


        // 遍历的时候从第二个点开始。到倒数第二个点作为结束

        for (int i = 1; i < list.size() - 1; i++) {

            LngAndLat pre = list.get(i - 1);

            LngAndLat current = list.get(i);

            LngAndLat next = list.get(i + 1);


            double v = current.getLng() - pre.getLng();


            preVector.setLng(v);
            preVector.setLat(current.getLat() - pre.getLat());


            nextVector.setLng(next.getLng() - current.getLng());
            nextVector.setLat(next.getLat() - current.getLat());


            double degree = CalculaeVectorAngel(preVector.getLng(), preVector.getLat(), nextVector.getLng(), nextVector.getLat());


            // 如果2个向量之间的夹角大于30度。 那么将这2条路进行分开
            if (degree > 20.0) {
                resultList.add(list.subList(from, i));
                from = i;
            }


        }


        //在根据方向将路段分割完之后，判断是否还有最后一段没有添加到分割路段之后
        if (from != (list.size() - 1)) {

            resultList.add(list.subList(from, list.size() - 1));
        }


        return resultList;

    }

    /**
     * @desc Vector angle  向量的角度计算方法，计算2个二维向量的夹角
     */
    public static double CalculaeVectorAngel(double point1X, double point1Y, double point2X, double point2Y) {


        double x = point1X * point2X + point1Y * point2Y;
        double y = Math.sqrt(point1X * point1X + point1Y * point1Y) * Math.sqrt(point2X * point2X + point2Y * point2Y);

        double num = x / y;

        double degree = Math.acos(num) * (180 / 3.14159265);

        return degree;
    }

    /**
     * @param lists 之前以前分割好的路段
     * @return 理想状态下返回的每一段路都是属于某一条路的
     * @desc 目标：如果相邻的2个向量之间的角度大于30度。我们将会将路段分割开来
     */
    public static List<List<LngAndLat>> splitRoadByDirection(List<List<LngAndLat>> lists) {

        List<List<LngAndLat>> resultLists = new LinkedList<>();

        for (List<LngAndLat> list : lists) {

            List<List<LngAndLat>> lists1 = makeOneRoadToSomeRoad(list);

            for (List<LngAndLat> list1 : lists1) {
                resultLists.add(list1);
            }
        }

        return resultLists;
    }

    /**
     * @param list       行车路径
     * @param matchRoads 之前了已经匹配的路网数据
     * @return 返回的是最有可能的路的信息
     * @desc 根据一段行车路径，匹配上一条路网数据,将该分段的路上的所有的点进行 路段的匹配,拿出所有的点对匹配到的所有的路进行距离计算
     */
    public static Set<String> findRoadByOneRoute(List<LngAndLat> list, List<Road> matchRoads) {


        Set<String> undetermineoaRds = new HashSet<>();


        for (LngAndLat lngAndLat : list) {

            //记录下最小的距离，并且记录下所对应的路的id
            double minDistance = 0.0;

            double secondDistance = 0.0;

            LngAndLat minPoint = new LngAndLat();

            LngAndLat secondPoint = new LngAndLat();

            String shapeId = null;

            Boolean derail = true;


            //根据点找到对应的最近的路

            for (Road road : matchRoads) {

                // 获取该条路上的所有点
                String shape = road.getShape();


                Map<String, Map<String, Object>> twoMinDistance = DealWithRoute.minPointToRoadDistance(lngAndLat, shape);

                //获取最短路径的距离
                Map<String, Object> minDistancePoint = twoMinDistance.get("min");
                Double tempMinDistance = (Double) minDistancePoint.get("minDistance");
                LngAndLat tempMinDistancePoint = (LngAndLat) minDistancePoint.get("minDistancePoint");


                Map<String, Object> secondMinDistance = twoMinDistance.get("second");
                Double tempSecondDistance = (Double) secondMinDistance.get("secondDistance");
                LngAndLat tempSecondDistancePoint = (LngAndLat) secondMinDistance.get("secondDistancePoint");


                //数据的初始化操作
                if (true) {

                    minDistance = tempMinDistance;
                    minPoint = tempMinDistancePoint;

                    secondDistance = tempSecondDistance;
                    secondPoint = tempSecondDistancePoint;

                    shapeId = shape;

                    derail = false;

                }


                //如果该点与该路之间的距离小于之前的路与点之间的距离，那么将替换之前的信息
                if (tempMinDistance < minDistance) {

                    minDistance = tempMinDistance;
                    minPoint = tempMinDistancePoint;

                    secondDistance = tempSecondDistance;
                    secondPoint = tempSecondDistancePoint;
                    shapeId = shape;
                }

            }

            //如果最短点的距离是大于 100 米 ，则表示该路径已经匹配不上我们的路网的数据了，则直接进行放弃匹配了
            if (minDistance > 100) {

                continue;

            } else {
                undetermineoaRds.add(shapeId);
            }


        }

        return undetermineoaRds;

    }

    /**
     * @desc 在确定路的前提下，确定出该行程中的每个点所对应的2个最短的距的点，并通过这2个点对该点进行修复
     */
    public static Map<String, Object> fixDataAction(List<LngAndLat> toBeRepaires, String roadShape) {


        Map<String, Object> resultMap = new HashMap<>();

        //存储返回修复好的点
        List<LngAndLat> repairedList = new LinkedList<>();


        // 返回我们坐标的每个点映射到路网上的最近的点
        List<Integer> mappingIndex = new ArrayList<>();


        System.out.println("修复之后的路径:");


        for (LngAndLat toBeRepairePoint : toBeRepaires) {

            Map<String, Map<String, Object>> twoMinPoints = DealWithRoute.minPointToRoadDistance(toBeRepairePoint, roadShape);

            LngAndLat minPoint = (LngAndLat) twoMinPoints.get("min").get("minDistancePoint");

            LngAndLat foot = DealWithRoute.minPointToRoadFootDistance(toBeRepairePoint, roadShape);

            foot.setDate(toBeRepairePoint.getDate());
            foot.setDeviceId(toBeRepairePoint.getDeviceId());

            //修复好的点
            System.out.print("[" + foot.getLng() + "," + foot.getLat() + "],");

            repairedList.add(foot);
            mappingIndex.add((Integer) twoMinPoints.get("min").get("minIndex"));

        }

        resultMap.put("repairedList", repairedList);
        resultMap.put("mappingIndex", mappingIndex);


        return resultMap;
    }

    /**
     * @param imei      设备号
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @desc 获取某个设备的指定时间内的行程, 返回的是已经处理过的高德作标的数据
     */
    public List<LngAndLat> getSpecifiedRoutesData(String imei, String tableName, String family, String pre, String
            startTime, String endTime) {

        List<LngAndLat> list = new LinkedList<LngAndLat>();


        // 查询的起始rowkey
        String startRow = pre + "_" + imei + "_" + startTime;

        // 查询的结束rowkey
        String endRow = pre + "_" + imei + "_" + endTime + "z";

        ResultScanner resultScanner = null;

        resultScanner = HbaseDao.getRangeRows(tableName, family, startRow, endRow);

        for (Result result : resultScanner) {

            List<Cell> cells = result.listCells();

            String lng = null;
            String lat = null;

            for (Cell cell : cells) {

                String col = new String(CellUtil.cloneQualifier(cell));

                if (null != col && col.equals("lnt")) {
                    lng = new String(CellUtil.cloneValue(cell));
                }

                if (null != col && col.equals("lat")) {
                    lat = new String(CellUtil.cloneValue(cell));
                }
            }


            // 当经度和纬度都不为空的时候
            if (StringUtils.isNullOrBlank(lng) || StringUtils.isNullOrBlank(lat)) {
                continue;
            }

            // 将原始的百度坐标转成高德坐标
            Map<String, Double> point = MapUtils.bd_decrypt(Double.valueOf(lng), Double.valueOf(lat));

            LngAndLat lngAndLat = new LngAndLat();

            lngAndLat.setLng(point.get("lnt"));
            lngAndLat.setLat(point.get("lat"));

            list.add(lngAndLat);

//            logger.info(lng + "," + lat);

        }


        return list;

    }


    //判断2段行程之间有没有急转弯
    public static boolean isSharpTurnBetweenTwoRoute(List<LngAndLat> lastRoute, List<LngAndLat> thisRoute){


        if(lastRoute.size() < 2){

            return false;
        }

        LngAndLat lastPont = lastRoute.get(lastRoute.size() - 1);
        LngAndLat lastSecondPoint = lastRoute.get(lastRoute.size() - 2);

        LngAndLat thisPoint = thisRoute.get(0);

        //计算2个向量

        double x1 = lastPont.getLng() - lastSecondPoint.getLng();
        double y1 = lastPont.getLat() - lastSecondPoint.getLat();

        double x2 = thisPoint.getLng() - lastPont.getLng();
        double y2 = thisPoint.getLat() - lastPont.getLat();

        double degree = DealWithRoute.CalculaeVectorAngel(x1, y2, x2, y2);


        if(degree > 30){

            return true;
        }

        return false;
    }



}
