package com.muheda.dao;


import com.muheda.domain.DriveData;
import com.muheda.domain.LngAndLat;
import com.muheda.utils.DateUtils;
import com.muheda.utils.ReadProperty;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @desc 用于行车数据标签的存储
 */
public class DrivingLabelDao {

    private static Logger  logger = LoggerFactory.getLogger(DrivingLabelDao.class);

    // 设备类型前缀用于区分这个是什么设备的数据
    private static  String deviceTypePre = ReadProperty.getConfigData("hbase.basicData.rowkey.pre");


    // hbase的连接建立
    private static Connection connection = null;

    // 声明静态配置
    private static Configuration conf = null;
    private static HBaseAdmin admin = null;
    private volatile static HTable table = null;

    private  static  String zkQuorum = ReadProperty.getConfigData("hbase.zookeeper.quorum");
    private  static  String zkPort   = ReadProperty.getConfigData("hbase.zookeeper.property.clientPort");
    private  static  String hbaseMaster = ReadProperty.getConfigData("hbase.master");
    private  static  String zkParent = ReadProperty.getConfigData("zookeeper.znode.parent");

    private  static  String routeLabelTableName = ReadProperty.getConfigData("hbase.route.label.tableName");

    private  static  Table routeLabelTable = null;



    static {

        conf = HBaseConfiguration.create();//使用eclipse时必须添加这个，否则无法定位

        conf.set("hbase.zookeeper.quorum", zkQuorum);
        conf.set("hbase.zookeeper.property.clientPort", zkPort);
        conf.set("hbase.master", hbaseMaster);
        conf.set("zookeeper.znode.parent", zkParent);

        try {
            connection =  ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            logger.error("hbase 数据库连接失败");
            e.printStackTrace();
        }


        // 基础数据表的初始化

        try {

            if(routeLabelTableName != null){
                routeLabelTable  = connection.getTable(TableName.valueOf(routeLabelTableName));
            }else {

                logger.error("行程标签数据表名获取失败");
            }

        } catch (IOException e) {
            logger.error("行程标签数据表初始化失败");
            e.printStackTrace();
        }


    }


    /**
     * @desc 存储行车的三急标签
     * @param urgentSpeed     急加速或者急减速标签
     */
    public static  boolean saveRouteUrgentSpeedLabel( String deviceId, DriveData urgentSpeed){

        if(routeLabelTable == null){

            if(routeLabelTableName != null){
                try {
                    routeLabelTable = connection.getTable(TableName.valueOf(routeLabelTableName));
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("行程标签表连接失败");
                }

            }


        }


        List<Double> urgentSpeedValue = urgentSpeed.getCheckValue();
        List<Date>   urgentSpeedTime = urgentSpeed.getCheckTime();
        List<LngAndLat>  urgentSpeedPoints = urgentSpeed.getCheckPoints();

        // 理论上三个集合的size 是相同的
        int size = urgentSpeedPoints.size();


        List<Put> urgentSpeedPuts = new ArrayList<>();

        // 将急加速和急减速放在一起
        for (int i = 0; i < size ; i++) {

            String time = DateUtils.dateToStrLong(urgentSpeedTime.get(i));

            String rowkey = deviceTypePre + "_" + deviceId + "_" + time;

            Put put = new Put(Bytes.toBytes(rowkey));
            put.addColumn("urgentSpeed".getBytes(),"value".getBytes(),String.valueOf(urgentSpeedValue.get(i)).getBytes());
            put.addColumn("urgentSpeed".getBytes(),"time".getBytes(),String.valueOf(urgentSpeedTime.get(i)).getBytes());

            // 存放发生该急加速或者急减速的点坐标
            put.addColumn("urgentSpeed".getBytes(),"point".getBytes(),String.valueOf(urgentSpeedPoints.get(i)).getBytes());

            urgentSpeedPuts.add(put);
        }




        try {
            routeLabelTable.put(urgentSpeedPuts);
        } catch (IOException e) {
            logger.error("存储急加速数据出现异常");
            e.printStackTrace();
            return false;
        }




        return true;
   }


    /**
     * @desc 存储急转弯标签
     */
    public static  boolean saveRouteUrgentSharpTurn(String deviceId, DriveData urgentSharpTurn){


        if(routeLabelTable == null){

            if(routeLabelTableName != null){
                try {
                    routeLabelTable = connection.getTable(TableName.valueOf(routeLabelTableName));
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("行程标签表连接失败");
                }

            }


        }


        List<LngAndLat> urgentSharpTurnPoints = urgentSharpTurn.getCheckPoints();
        List<Date> urgentSharpTurnTime = urgentSharpTurn.getCheckTime();
        List<Double> urgentSharpTurnValue  = urgentSharpTurn.getCheckValue();


        int  urgentSharpTurnSize = urgentSharpTurnPoints.size();

        List<Put> urgentSharpTurnPuts = new ArrayList<>();

        if( urgentSharpTurnSize > 0){

            List<Put>  urgentSharpSpeed = new ArrayList<>();

            for (int i = 0; i < urgentSharpTurnSize ; i++) {


                String time = DateUtils.dateToStrLong(urgentSharpTurnTime.get(i));

                String rowkey = deviceTypePre + "_" + deviceId + "_" + time;

                Put put = new Put(Bytes.toBytes(rowkey));
                put.addColumn("urgentSharpTurn".getBytes(),"value".getBytes(),String.valueOf(urgentSharpTurnValue.get(i)).getBytes());
                put.addColumn("urgentSharpTurn".getBytes(),"time".getBytes(),String.valueOf(time).getBytes());

                // 存放发生该急加速或者急减速的点坐标
                put.addColumn("urgentSharpTurn".getBytes(),"point".getBytes(),String.valueOf(urgentSharpTurnPoints.get(i)).getBytes());

                urgentSharpTurnPuts.add(put);
            }



        }




        try {
            routeLabelTable.put(urgentSharpTurnPuts);
        } catch (IOException e) {
            logger.error("存储急转弯数据出现异常");
            e.printStackTrace();
            return false;
        }


        return true;


    }






}
