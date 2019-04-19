package com.muheda.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc 读取配置文件类，获取application.properties的信息
 */
public class ReadProperty {
    private static Log logger = LogFactory.getLog(ReadProperty.class);
    /**
     * 系统配置变量
     */
    private static volatile Map<String, String> confDataMap = null;

    /**
     * 获取系统变量
     *
     * @param key
     * @return
     */

    public static String getConfigData(String key) {

        if (confDataMap != null) {
            return confDataMap.get(key);
        }

        InputStream sysInputStream = null;
        InputStream devInputStream = null;
        confDataMap = new ConcurrentHashMap<>(16);
        try {

            sysInputStream = ReadProperty.class.getResourceAsStream("/application.properties");

            Properties prop = new Properties();

            prop.load(sysInputStream);

            String valueString = prop.getProperty("sys.config.file");
            if ("application.properties".equals(valueString)) {
                Iterator<String> it = prop.stringPropertyNames().iterator();
                String proKeyString;
                while (it.hasNext()) {
                    proKeyString = it.next();
                    confDataMap.put(proKeyString, prop.getProperty(proKeyString));
                }
                return confDataMap.get(key);
            }

            // 若非正式环境下的配置文件
            confDataMap.clear();
            devInputStream = ReadProperty.class.getResourceAsStream("/" + valueString);
            prop = new Properties();
            prop.load(devInputStream);
            Iterator<String> it = prop.stringPropertyNames().iterator();
            String proKeyString;
            while (it.hasNext()) {
                proKeyString = it.next();
                confDataMap.put(proKeyString, prop.getProperty(proKeyString));
            }
            return confDataMap.get(key);

        } catch (Exception e) {
            logger.error(e);
            return null;
        } finally {
            try {
                if (sysInputStream != null) {
                    sysInputStream.close();
                }
                if (devInputStream != null) {
                    devInputStream.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }


    /**
     * @param
     * @return
     * @desc 获取远程的配置文件
     *//*
    public static String getRemoteConfigData(String key) {
        //模拟一个get请求读取配置文件，将配置文件的字符串转成InpuStream
        //http://47.94.106.165:5001/cf/电动车 spark/test/1544595454.193695

        String env = "test";

        String s = HttpClient.doGet("http://47.94.106.165:5001/cf/electricCar/" + env + "/1544669657.865754");
        JSONObject jsonObject = (JSONObject) JSONObject.parse(s);
        return (String) jsonObject.get(key);

    }*/


    //用于测试配置文件的读取的功能
    @Test
    public void readProperties() {

/*
    String configData = getConfigData ( "hbase.zookeeper.quorum" );
    System.out.println (configData );
*/
/*
    String remoteConfigData = getRemoteConfigData("hbase.zookeeper.quorum");
    System.out.println(remoteConfigData);*/


    }


}


