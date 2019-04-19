package com.muheda.utils;

import java.io.*;


public class TestReadLine {

    public static String [] readLineToArray() {
        // 在E盘下新建一个文本文件
        File file1 = new File("/Users/zhangshaofan/poroject/fix-data/src/main/resources/points.txt"); // 创建File类对象
        FileInputStream fis = null; // 创建FileInputStream类对象读取File
        InputStreamReader isr = null; // 创建InputStreamReader对象接收文件流
        BufferedReader br = null; // 创建reader缓冲区将文件流装进去
        try {
            fis = new FileInputStream(file1);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String lineTxt = null;
            // 从缓冲区中逐行读取代码，调用readLine()方法
            while ((lineTxt = br.readLine()) != null) {

                String[] points = lineTxt.split(";");

                return points;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 文件执行完毕别忘了关闭数据流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return   null;
    }
}

