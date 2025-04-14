package com.qz.baseClass.Line;

import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BRTLine extends BaseLine{
    public List<Integer> timeTable;
    public static List<BRTLine> brtLines = new ArrayList<>();
    int bunchingTime = 2;
    int syTime = 2;
    public BRTLine(int id, List<Node> busNode, List<Integer> timeTable,boolean addList) {
        super(id, busNode,2);
        this.timeTable = timeTable;
        if(addList){
            brtLines.add(this);
        }
        linkStartSyTimeWindows = new HashMap<>();
        linkStartBhTimeWindows = new HashMap<>();
        setLinkStartTime();
    }

    Map<Integer,List<TimeWindow>> linkStartSyTimeWindows;
    Map<Integer,List<TimeWindow>> linkStartBhTimeWindows;



    public void addLinkStartSyTimeWindows(int linkID,TimeWindow timeWindow){
        if(linkStartSyTimeWindows.containsKey(linkID)){
            if(!linkStartSyTimeWindows.get(linkID).contains(timeWindow)){
                linkStartSyTimeWindows.get(linkID).add(timeWindow);
            }
        }else{
            List<TimeWindow> list = new ArrayList<>();
            list.add(timeWindow);
            linkStartSyTimeWindows.put(linkID,list);
        }
    }

    public void addLinkStartBhTimeWindows(int linkID,TimeWindow timeWindow){
        if(linkStartBhTimeWindows.containsKey(linkID)){
            if(!linkStartBhTimeWindows.get(linkID).contains(timeWindow)){
                linkStartBhTimeWindows.get(linkID).add(timeWindow);
            }
        }else{
            List<TimeWindow> list = new ArrayList<>();
            list.add(timeWindow);
            linkStartBhTimeWindows.put(linkID,list);
        }
    }

    //计算到每个点的时间窗
    public void setLinkStartTime(){
        for(int startTime:timeTable){
            int tmpTime = startTime;
            for(Link link:links){
                TimeWindow timeWindow1 = new TimeWindow(startTime-bunchingTime-syTime,startTime-bunchingTime);
                TimeWindow timeWindow2 = new TimeWindow(startTime+bunchingTime,startTime+bunchingTime+syTime);
                TimeWindow timeWindow3 = new TimeWindow(startTime-bunchingTime,startTime+bunchingTime);
                tmpTime += link.getLinkTravelTime(tmpTime);

                addLinkStartSyTimeWindows(link.id,timeWindow1);
                addLinkStartSyTimeWindows(link.id,timeWindow2);
                addLinkStartBhTimeWindows(link.id,timeWindow3);
            }
        }
    }

}
