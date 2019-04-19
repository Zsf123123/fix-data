package com.muheda.AsynchronousTripCache;

import com.muheda.domain.LngAndLat;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @desc 用于修复好的行程缓存的队列，及相关处理方法
 */
public class CacheQueueHandle {


    private static ConcurrentLinkedQueue<List<LngAndLat>> queue = new ConcurrentLinkedQueue();


    public ConcurrentLinkedQueue<List<LngAndLat>> getQueue() {
        return queue;
    }

    /**
     * @param list 传过来的已经修复好的行程
     * @return 返回是否追加成功
     * @desc 将传来的行程追加到队列的末尾
     */
    public Boolean appendToQueue(List<LngAndLat> list) {

        if (list.size() == 0) {
            return false;
        }

        return queue.add(list);
    }


    /**
     * @desc 消费行程的缓存队列，并将消费之后的行程在原来的缓存队列中删除
     * @return
     */
    public List<List<LngAndLat>> consumeTrip(){

        List<List<LngAndLat>> resultList = new LinkedList<>();

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
