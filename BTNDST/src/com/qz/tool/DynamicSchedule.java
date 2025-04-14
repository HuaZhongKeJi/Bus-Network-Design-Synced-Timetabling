package com.qz.tool;

import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Line.TimeWindow;
import com.qz.baseClass.Node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicSchedule {

    public Map<Integer,List<Integer>> departingTimes;//bus line id,dept
    List<BusLineNew> busLines;
    DynamicSchedule(List<BusLineNew> busLines){
        this.busLines = busLines;
        departingTimes = new HashMap<>();
        for (BusLineNew busLine : busLines) {
            departingTimes.put(busLine.id,new ArrayList<>());
        }
    }

    public boolean dispatch(int lineId,int time,TransitSimulation transit){
        if(time>=IniData.busEndTime){
            return false;
        }
        BusLineNew busLine = null;
        for(BusLineNew tmBusLine:busLines){
            if(tmBusLine.id==lineId){
                busLine = tmBusLine;
            }
        }

        int nodePassengers = 0;
        for(Node node:busLine.getBusNode()){
            nodePassengers += transit.getNodePassengers(node.id,lineId);
        }
        if (time==IniData.busStartTime||time==IniData.busEndTime) {
            return true;
        }else if(time - departingTimes.get(busLine.id).get(departingTimes.get(busLine.id).size()-1)>=IniData.gapTime){
            return true;
        }else if(busLine.synchronizeTimeMap.containsKey(time+2)&&(busLine.synchronizeTimeMap.get(time+1)==1||busLine.synchronizeTimeMap.get(time+2)==1)){
            return false;
        }else if(nodePassengers>=IniData.maxPassengerNum){
            return !isBunching(busLine, time);
        } else if (busLine.synchronizeTimeMap.containsKey(time)&&busLine.synchronizeTimeMap.get(time)==1) {
            return !isBunching(busLine, time);
        } else{
            return false;
        }

        /*if (time==IniData.busStartTime||time==IniData.busEndTime) {
            return true;
        }else if(time - departingTimes.get(busLine.id).get(departingTimes.get(busLine.id).size()-1)>=IniData.gapTime){
            return true;
        }else if(nodePassengers>=IniData.maxPassengerNum){
            return !isBunching(busLine, time);
        } else{
            return false;
        }*/

    }

    public void setDeptTime(int lineId,int time){
        departingTimes.get(lineId).add(time);
    }

    public void clearDeptTime(){
        departingTimes.clear();
        for (BusLineNew busLine : busLines) {
            departingTimes.put(busLine.id,new ArrayList<>());
        }
    }

    public boolean isBunching(BusLineNew busLine,int time){
        if(time - departingTimes.get(busLine.id).get(departingTimes.get(busLine.id).size()-1)<=IniData.bunchingTime){
            return  true;
        }
        if(busLine.bunchingTimeMap.containsKey(time)&&busLine.bunchingTimeMap.get(time)==1){
            return  true;
        }
        for(int t=time+1;t<time+IniData.bunchingTime;t++){
            if(busLine.synchronizeTimeMap.containsKey(time)&&busLine.synchronizeTimeMap.get(time)==1){
                return true;
            }
        }
        return false;
    }

    public int getBunchingTimes(){
        int bunchingTime = 0;
        for (BusLineNew busLine : busLines) {
            List<Integer> departTimes = departingTimes.get(busLine.id);
            for(int departTime:departTimes){
                if(busLine.isBunching(departTime)){
                    bunchingTime++;
                }
            }
        }
        return bunchingTime;
    }

    public int getTimeSyNum(){
        int timeSyNum = 0;
        for(BusLineNew bl:busLines){
            for(TimeWindow tw:bl.synchronizeTimeWindow){
                for(Integer deptTime:departingTimes.get(bl.id)){
                    if(tw.timeInWindow(deptTime)){
                        timeSyNum++;
                    }
                }
            }
        }
        return timeSyNum;
    }

}
