package com.muheda.dao;


import com.alibaba.fastjson.JSON;
import com.muheda.domain.LngAndLat;
import com.muheda.utils.ReadProperty;
import com.muheda.utils.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;

import java.util.*;


/**
 * 操作Hbase的DAO
 * @author zhangshaofan
 *
 */
@SuppressWarnings("deprecation")
public class HbaseDao {

    private static final Logger logger = LoggerFactory.getLogger(HbaseDao.class);


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


    private  static  String repairedFamily = ReadProperty.getConfigData("hbase.route.repaired.family");
    private  static  String repairedOrign = ReadProperty.getConfigData("hbase.route.repaired.orign");
    private  static  String repairedFixed = ReadProperty.getConfigData("hbase.route.repaired.fixed");
    private  static  String repairedMapping = ReadProperty.getConfigData("hbase.route.repaired.mapping");





    // 行程数据修复表
    private  static  String repairedTableName = ReadProperty.getConfigData("hbase.route.repaired.table");
    private  static Table routeRepairedTable = null;


    // 基础数据的表
    private  static  Table  basicDataTable = null;
    private  static  String basicDataTableName = ReadProperty.getConfigData("hbase.basicData.tableName");


    // 存储三急数据的表
    private  static  Table threeUrgent = null;





    // 初始化静态配置
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

            if(basicDataTableName != null){

                basicDataTable  = connection.getTable(TableName.valueOf(basicDataTableName));
            }else {

                logger.error("基础数据表名获取失败");
            }

        } catch (IOException e) {
            logger.error("基础数据表初始化失败");
            e.printStackTrace();
        }

        // 行程修复表的初始化
        try {

            if(repairedTableName != null){

                routeRepairedTable = connection.getTable(TableName.valueOf(repairedTableName));
            }else{
                logger.error("行程修复表名获取失败");
            }

        } catch (Exception e) {
            logger.error("行程修复表初始化异常");
            e.printStackTrace();
        }


    }



    /**
     * 根据表名，判断表是否存在
     * @param tableName     表名
     * @return  true 存在 ，false 不存在
     * @throws IOException
     */
    public static boolean tableExists(String tableName) throws IOException {
        // 判断表是否存在
        boolean bool = admin.tableExists("emp");
        logger.info(tableName + " exists? " + bool);
        return bool;
    }

    /**
     * 创建数据库表
     * @param tableName     s
     * @param columnFamilys 列族名
     * @return  true 成功 ，false 失败
     * @throws Exception
     */
    public boolean createTable(String tableName, String[] columnFamilys)
            throws Exception {

        boolean bool;
        // 判断数据库表是否存在
        if (admin.tableExists(tableName)) {
            System.out.println("table " + tableName + " existed!!");
            System.exit(0);
            bool = false;
        } else {
            // 新建一个表的描述
            HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(tableName));
            // 在描述里添加列族
            for (String columnFamily : columnFamilys) {
                HColumnDescriptor descriptor = new HColumnDescriptor(columnFamily);
                descriptor.setMaxVersions(10);
                tableDesc.addFamily(new HColumnDescriptor(descriptor));
            }
            // 根据配置好的描述建表
            admin.createTable(tableDesc);
            System.out.println("create table " + tableName + " success!");
            bool = true;
        }
        return bool;
    }

    /**
     * 列出数据库中所有表
     * @return 返回数据库中所有表的描述的数组
     * @throws IOException
     */
    public HTableDescriptor[] showTables() throws IOException {

        // 获取数据库中表的集合
        HTableDescriptor[] tableDescriptor = admin.listTables();

        // 遍历打印所有表名
        for (int i = 0; i < tableDescriptor.length; i++ ){
            System.out.println(tableDescriptor[i].getNameAsString());
        }
        return tableDescriptor;
    }

    /**
     * 根据表名，获取数据库表的列族信息
     * @param tableName     表名
     * @throws IOException
     */
    public void tableDetail(String tableName) throws IOException {

        HTableDescriptor tableDescriptor = admin.getTableDescriptor(Bytes.toBytes(tableName));

        // 获取数据库表的名称
        byte[] name = tableDescriptor.getName();
        System.out.println("result:");

        System.out.println("table name: " + new String(name));
        // 获取数据库表的列族名称
        HColumnDescriptor[] columnFamilies = tableDescriptor.getColumnFamilies();
        for(HColumnDescriptor d : columnFamilies){
            System.out.println("column Families: " + d.getNameAsString());
        }
    }

    /**
     * 添加一列族到现有的表
     * @param tableName     表名
     * @param coloumn       列族名
     * @throws IOException
     */
    public static void addColoumn(String tableName, String coloumn) throws IOException {

        // 初始化columnDescriptor对象
        HColumnDescriptor columnDescriptor = new HColumnDescriptor(coloumn);

        // 添加一个列族
        admin.addColumn(tableName, columnDescriptor);
        System.out.println("added " + coloumn + " to " + tableName);
    }

    /**
     * 删除一列族从现有的表
     * @param tableName     表名
     * @param coloumn       列族名
     * @throws IOException
     */
    public static void deleteColoumn(String tableName, String coloumn) throws IOException {

        // 删除一个列族
        admin.deleteColumn(tableName, coloumn);
        System.out.println("delete " + coloumn + " from " + tableName);
    }



    /**
     * 添加一条数据
     * @param tableName     表名
     * @param row           行名
     * @param columnFamily  列族名
     * @param column        列名
     * @param value         值
     * @throws Exception
     */
    public void addRow(String tableName, String row,
                       String columnFamily, String column, String value) throws Exception {

        // 指定行
        Put put = new Put(Bytes.toBytes(row));
        // 参数分别:列族、列、值
        put.add(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(value));
        table.put(put);
//        logger.info("add " + columnFamily + ":" + column + ":" + value + " success!");
    }

    /**
     * 批量插入
     * @param row           行名      需要变化
     * @param columnFamily  列族
     * @param set           值       一直变化
     * @throws Exception
     */
    public void addRows(List<String> row, String columnFamily, List<String> set) throws Exception {

        Put put = null;
        String columns = null;
        for (int i = 0; i < set.size(); i++) {

            switch (i % 7) {
                case 0 : columns = "ttl"; break;
                case 1 : columns = "clazz"; break;
                case 2 : columns = "type"; break;
                case 3 : columns = "rdata"; break;
                case 4 : columns = "ispId"; break;
                case 5 : columns = "version"; break;
                case 6 : columns = "owner"; break;
            }

            put = new Put(Bytes.toBytes( row.get((int)(i / 7)) ) );// 设置rowkey
            put.add(Bytes.toBytes(columnFamily), Bytes.toBytes(columns), Bytes.toBytes(set.get(i)));
            table.put(put);
        }
        table.close();
    }

    /**
     * 根据表名、列族名、列名，更新表中的某一列
     * @param tableName     表名
     * @param rowKey
     * @param familyName    列族名
     * @param columnName    列名
     * @param value         更新后的值
     * @throws IOException
     */
    public static void updateTable(String tableName, String rowKey,
                                   String familyName, String columnName, String value)
            throws IOException {

        Put put = new Put(Bytes.toBytes(rowKey));
        put.add(Bytes.toBytes(familyName), Bytes.toBytes(columnName),
                Bytes.toBytes(value));
        table.put(put);
        System.out.println("update table Success!");
    }

    /**
     * 为表添加数据（适合知道有多少列族的固定表）
     * @param rowKey
     * @param tableName     表名
     * @param column1       第一个列族列表
     * @param value1        第一个列的值的列表
     * @param column2       第二个列族列表
     * @param value2        第二个列的值的列表
     * @throws IOException
     */
    public static void addData(String rowKey, String tableName,
                               String[] column1, String[] value1, String[] column2, String[] value2)
            throws IOException {

        // 设置rowkey
        Put put = new Put(Bytes.toBytes(rowKey));
        // 获取所有的列族
        HColumnDescriptor[] columnFamilies = table.getTableDescriptor().getColumnFamilies();

        for (int i = 0; i < columnFamilies.length; i++) {
            // 获取列族名
            String familyName = columnFamilies[i].getNameAsString();
            // article列族put数据
            if (familyName.equals("article")) {
                for (int j = 0; j < column1.length; j++) {
                    put.add(Bytes.toBytes(familyName),
                            Bytes.toBytes(column1[j]), Bytes.toBytes(value1[j]));
                }
            }
            // author列族put数据
            if (familyName.equals("author")) {
                for (int j = 0; j < column2.length; j++) {
                    put.add(Bytes.toBytes(familyName),
                            Bytes.toBytes(column2[j]), Bytes.toBytes(value2[j]));
                }
            }
        }
        table.put(put);
        System.out.println("add data Success!");
    }




    /**
     * 根据表名、行名的数组。删除多条数据
     * @param tableName     表名
     * @param rows          行名的数组
     * @throws Exception
     */
    public void delMultiRows(String tableName, String[] rows)
            throws Exception {

        List<Delete> delList = new ArrayList<Delete>();
        for (String row : rows) {
            Delete del = new Delete(Bytes.toBytes(row));
            delList.add(del);
        }
        table.delete(delList);
    }

    /**
     * 根据表名、行名，获取一条数据
     * @param tableName     表名
     * @param row           行名
     * @throws Exception
     */
    public void getRow(String tableName, String row) throws Exception {

        Get get = new Get(Bytes.toBytes(row));
        Result result = table.get(get);
        // 输出结果,raw方法返回所有keyvalue数组
        for (KeyValue rowKV : result.raw()) {
            System.out.print("表名:" + tableName + " ");
            System.out.print("列族名:" + new String(rowKV.getFamily()) + " ");
            System.out.print("行名:" + new String(rowKV.getRow()) + " ");
            System.out.print("时间戳:" + rowKV.getTimestamp() + " ");
            System.out.print("列名:" + new String(rowKV.getQualifier()) + " ");
            System.out.println("值:" + new String(rowKV.getValue()));
        }
    }

    /**
     * 根据表名、行名、列族名、列名，查询某列数据的多个版本
     * @param tableName      表名
     * @param rowKey        行名
     * @param familyName    列族名
     * @param columnName    列名
     * @throws IOException
     */
    public void getResultByVersion(String tableName, String rowKey,
                                   String familyName, String columnName) throws IOException {

        Get get = new Get(Bytes.toBytes(rowKey));
        // 设置一次性获取所有版本
        get.setMaxVersions();
        get.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName));
        Result result = table.get(get);
        for (KeyValue kv : result.list()) {
            System.out.print("表名:" + tableName + " ");
            System.out.print("行名:" + rowKey + " ");
            System.out.print("列族名:" + new String(kv.getFamily()) + " ");
            System.out.print("列名:" + new String(kv.getQualifier()) + " ");
            System.out.print("值:" + new String(kv.getValue()) + " ");
            System.out.println("时间戳:" + kv.getTimestamp() + " ");
        }
    }

    /**
     * 根据表名、列名、列族名、列名查询表中的某一列
     * @param tableName     表名
     * @param rowKey        行名
     * @param familyName    列族名
     * @param columnName    列名
     * @throws IOException
     */
    public static void getRowsByColumn(String tableName, String rowKey,
                                       String familyName, String columnName) throws IOException {

        Get get = new Get(Bytes.toBytes(rowKey));
        // 获取指定列族和列修饰符对应的列
        get.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName));
        Result result = table.get(get);
        for (KeyValue kv : result.list()) {
            System.out.print("表名:" + tableName + " ");
            System.out.print("列族名:" + new String(kv.getFamily()) + " ");
            System.out.print("行名:" + new String(kv.getRow()) + " ");
            System.out.print("时间戳:" + kv.getTimestamp() + " ");
            System.out.print("列名:" + new String(kv.getQualifier()) + " ");
            System.out.println("值:" + new String(kv.getValue()));
        }
    }

    /**
     * 遍历查询hbase表start_rowkey到stop_rowkey之间的数据
     * @param tableName     表名
     * @param startrow  开始的rowkey
     * @param endrow    结束的rowkey
     * @throws IOException
     */
    public static ResultScanner getRangeRows(String tableName, String family, String startrow,
                                             String endrow) {


        Scan scan = new Scan();
//        scan.addFamily(family.getBytes());
        scan.setStartRow(startrow.getBytes());
        scan.setStopRow( (endrow + "z") .getBytes());

        ResultScanner rs = null;
        try {
            rs = basicDataTable.getScanner(scan);
        } catch (IOException e) {
            logger.error("获取" + tableName + "数据失败 !!");
            e.printStackTrace();
        }

        return rs;
    }

    /**
     * 根据表名，获取所有的数据
     * @param tableName     表名
     * @throws Exception
     */
    public void getAllRows(String tableName) throws Exception {

        Scan scan = new Scan();
        // 设置缓存大小
        scan.setCaching(100);
        // 一次next()返回Result实例的列数，防止超过客户端进程的内存容量
        scan.setBatch(6);
        ResultScanner results = table.getScanner(scan);
        // 输出结果
        for (Result result : results) {
            for (KeyValue rowKV : result.raw()) {
                System.out.print("表名:" + tableName + " ");
                System.out.print("列族名:" + new String(rowKV.getFamily()) + " ");
                System.out.print("行名:" + new String(rowKV.getRow()) + " ");
                System.out.print("时间戳:" + rowKV.getTimestamp() + " ");
                System.out.print("列名:" + new String(rowKV.getQualifier()) + " ");
                System.out.println("值:" + new String(rowKV.getValue()));
            }
        }
    }



    //存储设备的原始行程数据，修复之后的数据，映射到的路网的数据和映射所在路的id
    public static void saveDeviceRoute(String deviceId ,List<LngAndLat> orignRoute, List<LngAndLat> repairedRoute, List<LngAndLat> mappingRoute, String roadId){

        // 判断行程修复的表的状态
        if(routeRepairedTable == null){
            try {
                routeRepairedTable = connection.getTable(TableName.valueOf(repairedTableName));
            } catch (IOException e) {
                logger.error("行程修复表获取失败");
                e.printStackTrace();
            }
        }


        String rowkey = deviceTypePre + "_" + deviceId + "_" + new Random().nextLong();

        Put put = new Put(rowkey.getBytes());

        put.addColumn(repairedFamily.getBytes(), "orign".getBytes(), JSON.toJSON(orignRoute).toString().getBytes());
        put.addColumn(repairedFamily.getBytes(), "repaired".getBytes(), JSON.toJSON(repairedRoute).toString().getBytes());
        put.addColumn(repairedFamily.getBytes(), "mapping".getBytes(), JSON.toJSON(repairedRoute).toString().getBytes());
        put.addColumn(repairedFamily.getBytes(), "roadId".getBytes(),  roadId.getBytes());

        try {
            routeRepairedTable.put(put);
        } catch (IOException e) {
            logger.error("行程修复数据存储失败");
            e.printStackTrace();
        }


    }


    /**
     * @desc 查询该设备的所有的设备号，查询的是该设备的注册表
     * @param tableName   表明
     * @param family      列簇名
     * @param column      列明
     * @param  pre        查询列簇的前缀
     */
    public static List<String> getAllDeviceId(String tableName,String family,String column,String pre){

        if(StringUtil.isEmpty(tableName)){
            logger.error("tableName  is null");
        }

        LinkedList<String> resultList = new LinkedList<>();

        Table table = null;

        try {
            table = connection.getTable(TableName.valueOf(tableName));
        } catch (IOException e) {
            logger.error("表连接获取异常");
            e.printStackTrace();
        }

        Scan scan = new Scan();
        scan.setStartRow((pre + "_" ).getBytes());
        scan.setStopRow((pre + "_" + "z").getBytes());
        scan.addColumn(family.getBytes(),column.getBytes());

        ResultScanner scanner = null;
        try {
            scanner = table.getScanner(scan);
        } catch (IOException e) {
            logger.error("模糊查询Sann时返回数据异常");
            e.printStackTrace();
        }


        for (Result result : scanner) {

            if(result.isEmpty()){
                continue;
            }

            List<Cell> cells = result.listCells();

            for(Cell  cell : cells) {
                String col = new String(CellUtil.cloneQualifier(cell));
                if(col.equals(column)){
                    resultList.add(new String(CellUtil.cloneValue(cell)));
                }
            }

        }


        return  resultList;

    }





}