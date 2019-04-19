package com.muheda.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.muheda.domain.Road;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * @desc mysql的数据库连接池
 * @desc 主要用于查询路网数据
 */
public class DbPoolConnection {

    private static Logger logger = Logger.getLogger(DbPoolConnection.class);
    private static DbPoolConnection databasePool = null;
    private static DruidDataSource dataSource = null;


    static {

        Properties properties = loadPropertyFile("druid.properties");
        try {
            dataSource = (DruidDataSource) DruidDataSourceFactory
                    .createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private DbPoolConnection() {
    }

    /**
     * 获取数据库连接实例
     *
     * @return
     */
    public static synchronized DbPoolConnection getInstance() {
        if (null == databasePool) {
            databasePool = new DbPoolConnection();
        }
        return databasePool;
    }


    public synchronized DruidPooledConnection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static Properties loadPropertyFile(String fullFile) {

        Properties p = new Properties();
        if (fullFile == "" || fullFile.equals("")) {
            logger.error("数据库连接池的配置文件文件为空!~");
        } else {
            //加载属性文件
            InputStream inStream = DbPoolConnection.class.getClassLoader().getResourceAsStream(fullFile);
            try {
                p.load(inStream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return p;
    }


    /**
     * @desc 预加载一些sql函数,以便后续能够进行调用
     */
    public static  void   executePreloadFun(){

//      执行计算地球上点与点之间的距离的sql函数
        String sql = "CREATE FUNCTION `point2point_distance` (lat1 FLOAT, lon1 FLOAT, lat2 FLOAT, lon2 FLOAT)\n" +
                "RETURNS FLOAT\n" +
                "DETERMINISTIC\n" +
                "BEGIN\n" +
                "    RETURN ROUND(6378.138 * 2 * ASIN(SQRT(POW(SIN((lat1 * PI() / 180 - lat2 * PI() / 180) / 2), 2)\n" +
                "           + COS(lat1 * PI() / 180) * COS(lat2 * PI() / 180)\n" +
                "           * POW(SIN(( lon1 * PI() / 180 - lon2 * PI() / 180 ) / 2),2))),2);\n" +
                "END";


        if (databasePool == null) {
            databasePool = DbPoolConnection.getInstance();
        }

        DruidPooledConnection con = null;
        PreparedStatement statement = null;

        ResultSet roads = null;



        try {
            //获取数据库连接
            con = databasePool.getConnection();
            statement = con.prepareStatement(sql);
            roads = statement.executeQuery(sql);
        }catch (Exception e){
            logger.error("预加载sql执行异常");
        }finally {
            closeConnection(con, statement);
        }



    }




    /**
     * @desc   传入需要的查询语句sql，
     * @param  sql  需要执行的sql语句
     * @return 返回的是查到的匹配的路
     *
     */
    public static List<Road> findRoutesByRectangle(String sql) {


        if (databasePool == null) {
            databasePool = DbPoolConnection.getInstance();
        }

        DruidPooledConnection con = null;
        PreparedStatement statement = null;

        ResultSet roads = null;

        try {
            //获取数据库连接
            con = databasePool.getConnection();
            statement = con.prepareStatement(sql);
            roads = statement.executeQuery(sql);


            List<Road> roadList = new ArrayList<Road>();

            while (roads.next()) {

                Road road = new Road();

                road.setId(roads.getString("id"));
                road.setName(roads.getString("name"));
                road.setAdcode(roads.getString("adcode"));
                road.setShape(roads.getString("shape_id"));
                road.setMin_x(roads.getFloat("min_x"));
                road.setMin_y(roads.getFloat("min_y"));
                road.setMax_x(roads.getFloat("max_x"));
                road.setMax_y(roads.getFloat("max_y"));
                road.setShape(roads.getString("shape"));
                road.setShape_id(roads.getString("shape_id"));

                roadList.add(road);
            }

            return roadList;

        } catch (SQLException ex) {
            logger.error("sql语句执行出错");
            ex.printStackTrace();
        } finally {
            closeConnection(con, statement);
        }

        return null;

    }




    //关闭数据库连接
    private static void closeConnection(DruidPooledConnection con, PreparedStatement statement) {
        //释放资源
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }





}
