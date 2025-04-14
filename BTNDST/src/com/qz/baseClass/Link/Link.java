package com.qz.baseClass.Link;

import com.qz.baseClass.Node.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Link {
    public int id;

    /// 1 bus link; 2 BRT link; 3 Rail link; 4 biking link; 5 walking link;
    public int type;
    Node startNode;
    Node endNode;
    public double length;
    //Map<Integer,Double> travelTime;
    //double travelTime;
    List<Double> travelTime;//每个小时一变化
    public double minTravelTime = 9999;
    public double maxTravelTime = 0;

    static Map<String, Link> linkMap = new HashMap<String, Link>();
    static Map<Integer, Link> linkIDMap = new HashMap<Integer, Link>();

    public Link(int id, int type, int startNodeId, int endNodeId, double length, List<Double> travelTime) {
        if(linkMap.containsKey(startNodeId+"-"+endNodeId)){
           return;
        }
        this.id = id;
        this.type = type;
        this.startNode = Node.getNode(startNodeId);
        this.endNode = Node.getNode(endNodeId);
        this.length = length;
        this.travelTime = travelTime;

        for(Double time: travelTime){
            if(time<minTravelTime){
                minTravelTime = time;
            }
            if(time>maxTravelTime){
                maxTravelTime = time;
            }
        }

        startNode.addToNode(endNode);
        endNode.addFromNode(startNode);
        linkMap.put(startNodeId+"-"+endNodeId, this);
        linkIDMap.put(id, this);
    }

    public Node getStartNode() {
        return startNode;
    }
    public Node getEndNode() {
        return endNode;
    }

    static public Link getLink(int startNodeId, int endNodeId) {
        return linkMap.getOrDefault(startNodeId + "-" + endNodeId, null);
    }
    static public Link getLinkByID(int id) {
        return linkIDMap.getOrDefault(id, null);
    }


    public Double getLinkTravelTime(int startTime){
        int f = startTime/60;
        return travelTime.get(f%travelTime.size());
    }

}
