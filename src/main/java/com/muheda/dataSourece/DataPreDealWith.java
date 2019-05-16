package com.muheda.dataSourece;


import com.muheda.dao.HbaseDao;
import com.muheda.domain.LngAndLat;
import com.muheda.utils.MapUtils;
import com.muheda.utils.ReadProperty;
import com.muheda.utils.TestReadLine;
import com.muheda.utils.TimeUtils;
import kafka.security.auth.Read;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * @desc 数据预处理阶段，包括数据源的获取。和转换成我们想要的格式的数据
 *
 */
public class DataPreDealWith {

    private  static Logger logger = LoggerFactory.getLogger(DataPreDealWith.class);

    private  static  String  tableName = ReadProperty.getConfigData("hbase.basicData.tableName");
    private  static  String  family = ReadProperty.getConfigData("hbase.basicData.family");
    private  static  String  pre = ReadProperty.getConfigData("hbase.basicData.rowkey.pre");
    private  static  String  longitude = ReadProperty.getConfigData("hbase.basicData.rowkey.longitude");
    private  static  String  latitude = ReadProperty.getConfigData("hbase.basicData.rowkey.latitude");
    private  static  String  time = ReadProperty.getConfigData("hbase.basicData.time");
//    private  static  String  deviceId = ReadProperty.getConfigData();


    /**
     * @desc 数据预处理阶段,使用的是通过定时任务，定时的从hbase中获取数据，用于正式的环境中
     * @param startRow  查询的起始的rowKey
     * @param endRow    查询的结束的rowkey
     */
    public static List<LngAndLat>  datareProcessingFromHbase(String startRow, String endRow){

        List<LngAndLat> lngAndLats = new LinkedList<>();


        ResultScanner resultScanner = HbaseDao.getRangeRows(tableName,family,startRow,endRow);

        for (Result result :resultScanner) {

            List<Cell> cells = result.listCells();


            LngAndLat lngAndLat = new LngAndLat();

            for (Cell  cell : cells) {

                String col = new String(CellUtil.cloneQualifier(cell));

                String lng = null;
                String lat = null;

                if(null != col && col.equals(longitude)){

                    lng = new String(CellUtil.cloneValue(cell));

                    if(lng != null){

                        lngAndLat.setLng(Double.parseDouble(lng));
                    }

                }

                if(null != col && col.equals(latitude)){

                    lat = new String(CellUtil.cloneValue(cell));

                    if(lat != null){

                        lngAndLat.setLat(Double.parseDouble(lat));
                    }

                }


                if(null != col && col.equals("imei")){

                }

                lngAndLat.setDeviceId("00001");

            }

            lngAndLats.add(lngAndLat);
        }

        return  lngAndLats;
    }




    /**
     * @desc数据预处理阶段,获取数据源，从配置文本中读取数据，并转化成我们所能计算的数据的格式。这种数据获取的方式是用于测试使用的
     */
    public static List<LngAndLat> datareProcessingFromConfigureFile() {

        String[] points = TestReadLine.readLineToArray();

        List<LngAndLat> lngAndLats = new LinkedList<>();

        for (int i = 0; i < points.length; i++) {

            if (points[i] != null) {
                String point = points[i];

                String[] pointArr = point.replace("[", "").replace("]", "").split(",");

                String lng  = null;
                String lat  = null;
                String time = null;


                try {

                    lng = pointArr[0];
                    lat = pointArr[1];
                    time = pointArr[2];
                }catch (Exception e){
                    e.printStackTrace();
                }


//             logger.info("lng: " + lng + "lat :" + lat + "time:" + time);



                LngAndLat lngAndLat = new LngAndLat();

                Date date = TimeUtils.timeFormat(time);

                lngAndLat.setDeviceId("1101");
                lngAndLat.setLng(Double.parseDouble(lng));
                lngAndLat.setLat(Double.parseDouble(lat));
                lngAndLat.setDate(date);

                lngAndLats.add(lngAndLat);



            }

        }

        return lngAndLats;
    }




}
