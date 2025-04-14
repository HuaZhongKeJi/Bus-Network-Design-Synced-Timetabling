package com.qz.baseClass.Line;

import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RailLine extends BaseLine{
    public List<Integer> timeTable;
    public static List<RailLine> railLines = new ArrayList<>();

    public RailLine(int id, List<Node> busNode, List<Integer> timeTable,boolean addList) {
        super(id, busNode,3);
        this.timeTable = timeTable;
        nodeTime = new HashMap<>();
        nodeTimeWindows = new HashMap<>();
        setNodeTime();
        getNodeTimeWindows();
        if(addList){
            railLines.add(this);
        }
    }

    public Map<Integer,List<Integer>> nodeTime;
    public Map<Integer,List<TimeWindow>> nodeTimeWindows;


    public void addNodeTime(int nodeID,int time){
        if(nodeTime.containsKey(nodeID)){
            if(!nodeTime.get(nodeID).contains(time)){
                nodeTime.get(nodeID).add(time);
            }
        }else{
            List<Integer> list = new ArrayList<>();
            list.add(time);
            nodeTime.put(nodeID,list);
        }
    }

    public void addNodeWindows(int nodeID,TimeWindow timeWindow){
        if(nodeTimeWindows.containsKey(nodeID)){
            if(!nodeTimeWindows.get(nodeID).contains(timeWindow)){
                nodeTimeWindows.get(nodeID).add(timeWindow);
            }
        }else{
            List<TimeWindow> list = new ArrayList<>();
            list.add(timeWindow);
            nodeTimeWindows.put(nodeID,list);
        }
    }

    //计算到每个点的时间
    public void setNodeTime(){
        for(int startTime:timeTable){
            addNodeTime(links.get(0).getStartNode().id,startTime);
            int tmpTime = startTime;
            for(Link link:links){
                tmpTime += link.getLinkTravelTime(tmpTime);
                addNodeTime(link.getEndNode().id,tmpTime);
            }
        }
    }

    //计算feedernode的同步时间窗
    public void getNodeTimeWindows(){
        nodeTime.forEach((key, value) -> {
            // 对键值对做相关处理
            for(Integer time:value){
                for(Node node:Node.getNode(key).toNodes()){
                    if(node.type==1||node.type==2){
                        double travelTime = (double)Link.getLink(key,node.id).getLinkTravelTime(time);
                        TimeWindow timeWindow = new TimeWindow(time-(int)travelTime,time+(int)travelTime);
                        addNodeWindows(node.id,timeWindow);
                    }
                }
            }
        });
    }


}
