package com.muheda.AsynchronousTripCache;

import com.muheda.domain.LngAndLat;
import com.muheda.domain.RoadInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @desc 用于修复好的行程缓存的队列，及相关处理方法
 */
public class CacheQueueHandle {


    private static ConcurrentLinkedQueue<Map<RoadInfo,List<LngAndLat>>> queue = new ConcurrentLinkedQueue();


    public ConcurrentLinkedQueue<Map<RoadInfo,List<LngAndLat>>>getQueue() {
        return queue;
    }

    /**
     * @param map 已经修复好的行程或者是不能够被修复的路径
     * @return 返回是否追加成功
     * @desc 将传来的行程追加到队列的末尾
     */
    public Boolean appendToQueue(Map<RoadInfo,List<LngAndLat>> map) {

        if (map.size() == 0) {
            return false;
        }

        return queue.add(map);
    }


    /**
     * @desc 消费行程的缓存队列，并将消费之后的行程在原来的缓存队列中删除
     * @return
     */
    public List<Map<RoadInfo,List<LngAndLat>>> consumeTrip(){

        List<Map<RoadInfo,List<LngAndLat>>> resultList = new LinkedList<>();

        int size = queue.size();

        if(size == 0){
           return  null;
        }

        for (int i = 0; i < size ; i++) {

           resultList.add(queue.poll());

        }

        return resultList;

    }


}
