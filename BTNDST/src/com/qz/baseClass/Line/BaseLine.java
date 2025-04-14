package com.qz.baseClass.Line;

import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;
import com.qz.tool.IniData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseLine {
    public int id;
    public List<Node> busNode;
    public List<Link> links;
    public static int tmID = 1;

    /// 连接的最大乘客数
    public int maxLinkPassengers;

    /// 1bus line;2brt line;3rail line
    int type;

    /// 连接的乘客数
    public Map<Integer,Integer> linkPassengers;

    BaseLine(int id, List<Node> busNode,int type) {
        this.id = id;
        this.type = type;
        this.busNode = busNode;
        links = new ArrayList<>();
        linkPassengers = new HashMap<>();

        if(type == 1){
            maxLinkPassengers = (int)(IniData.maxBusFrequency*IniData.busCapacity);
        }else if(type == 2){
            maxLinkPassengers = (int)(IniData.BRTFrequency*IniData.BRTCapacity);
        }else if(type == 3){
            maxLinkPassengers = (int)(IniData.RailFrequency*IniData.railCapacity);
        }

        for(int i=0;i<busNode.size()-1;i++){
            links.add(Link.getLink(busNode.get(i).id,busNode.get(i+1).id));
        }
        for(int i=busNode.size()-1;i>=1;i--){
            links.add(Link.getLink(busNode.get(i).id,busNode.get(i-1).id));
        }

    }


    public Link getNextLink(Link link) {
        int i = links.indexOf(link);
        if (i == links.size() - 1) {
            return null;
        }else{
            return links.get(i+1);
        }
    }

    public List<Node> getBusNode(){
        return busNode;
    }

    Node getStartNode() {
        return busNode.get(0);
    }
    Node getEndNode() {
        return busNode.get(busNode.size() - 1);
    }
}
