package com.muheda.domain;

import java.io.Serializable;

public class Road  implements Serializable {



   private String id;
   private String name;
   private String adcode;
   private String shape_id;
   private float min_x;
   private float min_y;
   private float max_x;
   private float max_y;
   private String shape;
   private String start;
   private String end;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdcode() {
        return adcode;
    }

    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }

    public String getShape_id() {
        return shape_id;
    }

    public void setShape_id(String shape_id) {
        this.shape_id = shape_id;
    }

    public float getMin_x() {
        return min_x;
    }

    public void setMin_x(float min_x) {
        this.min_x = min_x;
    }

    public float getMin_y() {
        return min_y;
    }

    public void setMin_y(float min_y) {
        this.min_y = min_y;
    }

    public float getMax_x() {
        return max_x;
    }

    public void setMax_x(float max_x) {
        this.max_x = max_x;
    }

    public float getMax_y() {
        return max_y;
    }

    public void setMax_y(float max_y) {
        this.max_y = max_y;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    
    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Road{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", adcode='" + adcode + '\'' +
                ", shape_id='" + shape_id + '\'' +
                ", min_x='" + min_x + '\'' +
                ", min_y='" + min_y + '\'' +
                ", max_x='" + max_x + '\'' +
                ", max_y='" + max_y + '\'' +
                ", shape='" + shape + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                '}';
    }



}
