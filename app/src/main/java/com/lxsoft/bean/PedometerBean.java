package com.lxsoft.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class PedometerBean implements Serializable {
    private int id;
    private int stepCount;//所走步数
    private double calorie;//消耗卡路里
    private double distance;//所走距离
    private int pace;//步/每分钟
    private double speed;//速度
    private long startTime;//开始记录时间
    private long lastStepTime;//最后一步的时间
    private String day;//以天为单位的时间戳

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double calorie) {
        this.calorie = calorie;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getPace() {
        return pace;
    }

    public void setPace(int pace) {
        this.pace = pace;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastStepTime() {
        return lastStepTime;
    }

    public void setLastStepTime(long lastStepTime) {
        this.lastStepTime = lastStepTime;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void reset(){
        stepCount = 0;
        calorie = 0;
        distance = 0;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "id:"+id+" stepCount:"+stepCount+" calorie:"+calorie+" distance:"+distance+" pace:"+pace+" speed:"+speed+" startTime:"+startTime+" lastStepTime:"+lastStepTime+" day:"+day;
    }
    public String[] toArray() {
        return new String[]{String.valueOf(id),String.valueOf(stepCount),String.valueOf(calorie),String.valueOf(distance),String.valueOf(pace),String.valueOf(speed),String.valueOf(startTime),String.valueOf(lastStepTime),day};
    }
    public String[] toTitleArray() {
        return new String[]{"id","stepCount","calorie","distance","pace","speed","startTime","lastStepTime","day"};
    }

    public PedometerBean(int id, int stepCount, double calorie, double distance, int pace, double speed, long startTime, long lastStepTime, String day) {
        this.id = id;
        this.stepCount = stepCount;
        this.calorie = calorie;
        this.distance = distance;
        this.pace = pace;
        this.speed = speed;
        this.startTime = startTime;
        this.lastStepTime = lastStepTime;
        this.day = day;
    }

    public PedometerBean() {
    }
}
