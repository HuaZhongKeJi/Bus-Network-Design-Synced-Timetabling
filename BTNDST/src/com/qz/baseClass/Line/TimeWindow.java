package com.qz.baseClass.Line;

public class TimeWindow {
    public int startTime;
    public int endTime;
    TimeWindow(int startTime, int endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public boolean timeInWindow(int time){
        return time >= startTime && time <= endTime;
    }
}
