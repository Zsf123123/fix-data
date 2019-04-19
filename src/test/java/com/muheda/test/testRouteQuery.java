package com.muheda.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.muheda.domain.LngAndLat;
import com.muheda.service.DealWithRoute;
import com.muheda.utils.HttpRequest;
import com.muheda.utils.MapUtils;
import org.junit.Test;

import java.util.List;
import java.util.Map;


/**
 * @desc 测试单个方法的功能
 */
public class testRouteQuery {


    /**
     * @desc 测试查询路径
     *
     */
    @Test
    public void testRouteQuery(){


        DealWithRoute dealWithRoute = new DealWithRoute();


//        String imei, String tableName, String family , String pre , String startTime, String endTime)


        String imei = "0000000001032";

        String tableName = "gtInfo";

        String family = "basic";

        String pre = "gt";

        String startTime = "2018";

        String  endTime = "2019";

        // 行程集合
        List<LngAndLat>  routeList = dealWithRoute.getSpecifiedRoutesData(imei, tableName,family,pre, startTime, endTime);

        System.out.println(routeList.size());

    }


    /**
     * @desc 测试坐标的转化
     */
    @Test
    public void testCoordinate(){


        String url = "https://restapi.amap.com/v3/assistant/coordinate/convert";

        String parm = "locations=116.481499,39.990475&coordsys=baidu&output=json&key=78dc787bcd8fd9bf0d5c41f54e52d4ff";

        JSONObject parse = (JSONObject) JSON.parse(HttpRequest.sendPost(url, parm));

        Object locaions = parse.get("locations");

        String[] split = locaions.toString().split(",");

        Map<String, Double> point = MapUtils.bd_decrypt(Double.parseDouble("116.481499"), Double.parseDouble("39.990475"));


//        System.out.println("lng : " + point.get("lng") + "lat :" + point.get("lat") );


//        System.out.println("lng: " + split[0] + "      lat :" + split[1]);


        /**
         * @result  高德的接口与我们自定义的方法之间的差别几乎可以忽略
         *
         * @input :   116.481499,39.990475
         *
         * @output :
         * 1:自定义方法坐标的转化：
         *   lng : 116.47489604504428lat :39.98471586964859
         *
         * 2:调用高德的接口
         *   lng: 116.4748955248      lt :39.984717169345
         */

    }


    /**
     * @desc  测试最小矩形的工具类
     *
     */
    public void  TestminimumRectangle() {

    }


    /**
     * @desc 测试点到直线做垂线的垂足
     */
    @Test
    public void testPointToLineFoot(){


        LngAndLat lngAndLat = new LngAndLat();
        lngAndLat.setLng(1.0);
        lngAndLat.setLat(7.0);



        LngAndLat lngAndLat1 = new LngAndLat();
        lngAndLat1.setLng(3.0);
        lngAndLat1.setLat(9.0);


        LngAndLat lngAndLat2 = new LngAndLat();
        lngAndLat2.setLng(7.0);
        lngAndLat2.setLat(9.0);

        LngAndLat foot = MapUtils.getFoot(lngAndLat, lngAndLat1, lngAndLat2);

//        System.out.println(foot);

    }





}
