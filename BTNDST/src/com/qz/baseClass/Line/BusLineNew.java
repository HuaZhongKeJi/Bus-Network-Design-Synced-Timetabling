package com.qz.baseClass.Line;

import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;
import com.qz.tool.IniData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusLineNew extends BaseLine{
    public int startTime;
    public int endTime;
    List<Integer> synchronizeTime;
    List<TimeWindow> bunchingTimeWindow;
    public Map<Integer,Integer> synchronizeTimeMap;
    public Map<Integer,Integer> bunchingTimeMap;

    public boolean isBunching(int deptTime){
        return bunchingTimeMap.get(deptTime) == 1;
    }

    public BusLineNew(int id, List<Node> busNode) {
        super(id, busNode,1);
        synchronizeTimeWindow = new ArrayList<>();
        synchronizeTime = new ArrayList<>();
        bunchingTimeWindow = new ArrayList<>();
        synchronizeTimeMap = new HashMap<>();
        bunchingTimeMap = new HashMap<>();
        startTime = IniData.busStartTime;
        endTime = IniData.busEndTime;
    }

    public List<TimeWindow> synchronizeTimeWindow;


    //同步以及串车时间窗计算
    public void setSynchronizeTimeWindow(List<RailLine> railLines,List<BRTLine> BRTLines) {
        //Rail的同步时间窗计算
        for (RailLine railLine : railLines) {
            railLine.nodeTimeWindows.forEach((key, value) -> {
                Node node = Node.getNode(key);
                if(busNode.contains(node)){//表示需要同步，倒退计算同步的时间窗
                    //倒序便利link
                    for(TimeWindow timeWindow:value){
                        int timeW = timeWindow.endTime-timeWindow.startTime;
                        int time1=0;
                        int time2=0;
                        boolean findFlag1 = true;
                        boolean findFlag2 = true;
                        Node tmN1 = node;
                        Node tmN2 = node;
                        for(int i=links.size()-1;i>=0;i--){
                            Link link = links.get(i);
                            if(link.getEndNode().equals(node)){
                                if(time1==0){
                                    time1 = timeWindow.startTime;
                                }else if(time2==0){
                                    time2 = timeWindow.startTime;
                                }

                            }

                            if(time1!=0 && findFlag1 && link.getEndNode().equals(tmN1)){
                                findFlag1 = false;
                                for(int j=0;j<time1;j++){
                                    if(j+(int)Math.ceil(link.getLinkTravelTime(j))==time1){
                                        time1 -= (int) Math.ceil(link.getLinkTravelTime(j));
                                        findFlag1 = true;
                                        tmN1 = link.getStartNode();
                                        break;
                                    }
                                }
                            }

                            if(time2!=0 && findFlag2 && link.getEndNode().equals(tmN2)){
                                findFlag2 = false;
                                for(int j=0;j<time2;j++){
                                    if(j+(int)Math.ceil(link.getLinkTravelTime(j))==time2){
                                        time2 -= (int)Math.ceil(link.getLinkTravelTime(j));
                                        findFlag2 = true;
                                        tmN2 = link.getStartNode();
                                        break;
                                    }
                                }
                            }
                        }
                        if(findFlag1&&time1!=0){
                            TimeWindow tw = new TimeWindow(time1,time1+timeW);
                            synchronizeTimeWindow.add(tw);
                        }
                        if(findFlag2&&time2!=0){
                            TimeWindow tw = new TimeWindow(time2,time2+timeW);
                            synchronizeTimeWindow.add(tw);
                        }

                    }
                }
            });
        }

        //BRT的同步时间窗计算
        for (BRTLine Line : BRTLines){
            Line.linkStartSyTimeWindows.forEach((key, value) -> {
                Link link = Link.getLinkByID(key);
                if(links.contains(link)){
                    for(TimeWindow timeWindow:value){
                        int timeW = timeWindow.endTime-timeWindow.startTime;
                        int time1=0;
                        Link tmL1 = link;
                        boolean findFlag1 = true;
                        for(int i=links.size()-1;i>=1;i--){
                            Link linkTm = links.get(i);
                            Link linkTm2 = links.get(i-1);
                            if(linkTm.id==link.id){
                                if(time1==0){
                                    time1 = timeWindow.startTime;
                                }
                            }
                            if(time1!=0 && findFlag1 && linkTm.id==tmL1.id){
                                findFlag1 = false;
                                for(int j=0;j<time1;j++){
                                    if(j+(int)Math.ceil(linkTm2.getLinkTravelTime(j))==time1){
                                        time1 -= (int)Math.ceil(linkTm2.getLinkTravelTime(j));
                                        findFlag1 = true;
                                        tmL1 = linkTm2;
                                        break;
                                    }
                                }
                            }
                        }
                        if(findFlag1&&time1!=0){
                            TimeWindow tw = new TimeWindow(time1,time1+timeW);
                            synchronizeTimeWindow.add(tw);
                        }
                    }
                }
            });
        }

        //BRT的串车时间窗计算
        for (BRTLine Line : BRTLines){
            Line.linkStartBhTimeWindows.forEach((key, value) -> {
                Link link = Link.getLinkByID(key);
                if(links.contains(link)){
                    for(TimeWindow timeWindow:value){
                        int timeW = timeWindow.endTime-timeWindow.startTime;
                        int time1=0;
                        Link tmL1 = link;
                        boolean findFlag1 = true;
                        for(int i=links.size()-1;i>=1;i--){
                            Link linkTm = links.get(i);
                            Link linkTm2 = links.get(i-1);
                            if(linkTm.id==link.id){
                                if(time1==0){
                                    time1 = timeWindow.startTime;
                                }
                            }
                            if(time1!=0 && findFlag1 && linkTm.id==tmL1.id){
                                findFlag1 = false;
                                for(int j=0;j<time1;j++){
                                    if(j+(int)Math.ceil(linkTm2.getLinkTravelTime(j))==time1){
                                        time1 -= (int)Math.ceil(linkTm2.getLinkTravelTime(j));
                                        findFlag1 = true;
                                        tmL1 = linkTm2;
                                        break;
                                    }
                                }
                            }
                        }
                        if(findFlag1&&time1!=0){
                            TimeWindow tw = new TimeWindow(time1,time1+timeW);
                            bunchingTimeWindow.add(tw);
                        }
                    }
                }
            });
        }
    }



    public void setSynchronizeTimePoint(){
        boolean matched = true;
        List<TimeWindow> synchronizeTimeWindowTm = new ArrayList<>(synchronizeTimeWindow);
        while(matched){
            int startT = startTime;
            int endT = startTime;
            int timeWNum = 0;
            List<TimeWindow> delTimeWindows=new ArrayList<>();
            int oldTimeWNum = 0;
            int tmStartT = 0;
            for(int i=startTime;i<=endTime;i++){
                List<TimeWindow> tmDelTimeWindows = new ArrayList<>();
                int tmTimeWNum = 0;
                for(TimeWindow tw:synchronizeTimeWindowTm){
                    if((tw.startTime+tw.endTime)/2>endTime){
                        tmDelTimeWindows.add(tw);
                    }else if(tw.timeInWindow(i)){
                        tmTimeWNum ++;
                        tmDelTimeWindows.add(tw);
                    }
                }
                if(tmTimeWNum!=oldTimeWNum){
                    if(tmTimeWNum>timeWNum){
                        timeWNum = tmTimeWNum;
                        delTimeWindows = tmDelTimeWindows;
                        startT = tmStartT;
                        endT = i;
                    }
                    tmStartT = i;
                    oldTimeWNum = tmTimeWNum;
                }
            }
            synchronizeTime.add((startT+endT)/2);
            synchronizeTimeWindowTm.removeAll(delTimeWindows);
            delTimeWindows.clear();
            if(synchronizeTimeWindowTm.isEmpty()){
                matched = false;
            }
        }
    }

    public void setTimeMap(){

        for(int time = startTime;time<=endTime;time++){
            if(synchronizeTime.contains(time)){
                synchronizeTimeMap.put(time,1);
            }else{
                synchronizeTimeMap.put(time,0);
            }
            bunchingTimeMap.put(time,0);
        }
        for(TimeWindow tw:bunchingTimeWindow){
            for(int time = tw.startTime;time<=tw.endTime;time++){
                bunchingTimeMap.put(time,1);
            }
        }
    }


    public double frequency=0;
    /// 计算线路的发车频率
    public void calFrequency(){
        frequency =0;
        double capacity =0;
        if(type==1){
            capacity = IniData.busCapacity;
        }else if(type==2){
            capacity = IniData.BRTCapacity;
        }else if(type==3){
            capacity = IniData.railCapacity;
        }

        for (Map.Entry<Integer, Integer> entry : linkPassengers.entrySet()) {
            Integer linkP = entry.getValue();
            double tmF = linkP/capacity;
            if(tmF>frequency){
                frequency = tmF;
            }
        }
    }

    public double getTravelTime(){
        double totalTime=0;
        for(Link link:links){
            totalTime += link.maxTravelTime;
        }
        return totalTime*frequency;
    }

}
