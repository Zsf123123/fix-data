package com.muheda.domain;

/**
 * @desc 匹配到路的相关信息
 */
public class RoadInfo {

   // 该路是否匹配到
   private  Boolean  isMatch;

   private  String adcode;


    public void setMatch(boolean match) {
        isMatch = match;
    }


    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }


    public Boolean getMatch() {
        return isMatch;
    }

    public String getAdcode() {
        return adcode;
    }



}

