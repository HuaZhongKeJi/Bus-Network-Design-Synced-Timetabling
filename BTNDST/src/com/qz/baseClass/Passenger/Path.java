package com.qz.baseClass.Passenger;

import com.qz.baseClass.Line.BaseLine;
import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Path {
    public int id;
    public static int tmId = 1;
    Node startNode;
    Node endNode;
    List<Link> links;
    List<Node> Nodes;
    /// link所属的线路,linkid+routeid，如果是walk或者bike就为-1
    Map<Integer,Integer> linkLine;

    /// 换乘站点集合 nodeid+要换乘的线路，如果是walk或者bike就为-1,如果不是就返回0
    Map<Integer,Integer> transferNode;

    /// 1bus,2BRT,3Rail,4bike,5bus+BRT,6bus+rail,7bus+bike,8BRT+rail,9BRT+bike,10rail+bike,11bus+BRT+bike,12bus+rail+bike,13rail+BRT+bike
    public int mode;

    Path(int id,Node startNode, Node endNode, List<Link> links, Map<Integer,Integer> linkLine) {
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        this.links = links;
        this.linkLine = linkLine;
        this.transferNode = new HashMap<>();

        Nodes = new ArrayList<>();
        for (Link link : links) {
            Nodes.add(link.getStartNode());
        }
        Nodes.add(endNode);

        List<Integer> linkTypes = new ArrayList<>();
        int oldLine = -2;
        for(int i=0;i<links.size();i++){
            if(!linkTypes.contains(links.get(i).type)){
                linkTypes.add(links.get(i).type);
            }
            int newLine = linkLine.get(links.get(i).id);
            if(links.get(i).type==4||links.get(i).type==5){
                this.transferNode.put(links.get(i).getStartNode().id, newLine);
                oldLine = newLine;
            }else{
                if(newLine!=oldLine){
                    this.transferNode.put(links.get(i).getStartNode().id, newLine);
                    oldLine = newLine;
                }
            }

            if(i==links.size()-1){
                this.transferNode.put(links.get(i).getEndNode().id, newLine);
            }
        }

        // 1bus,2BRT,3Rail,4bike,5bus+BRT,6bus+rail,7bus+bike,8BRT+rail,9BRT+bike,10rail+bike,11bus+BRT+bike,12bus+rail+bike,13rail+BRT+bike
        if(linkTypes.contains(1)&&!linkTypes.contains(2)&&!linkTypes.contains(3)&&!linkTypes.contains(4)){
            mode = 1;
        }else if(!linkTypes.contains(1)&&linkTypes.contains(2)&&!linkTypes.contains(3)&&!linkTypes.contains(4)){
            mode = 2;
        }else if(!linkTypes.contains(1)&&!linkTypes.contains(2)&&linkTypes.contains(3)&&!linkTypes.contains(4)){
            mode = 3;
        }else if(!linkTypes.contains(1)&&!linkTypes.contains(2)&&!linkTypes.contains(3)&&linkTypes.contains(4)){
            mode = 4;
        }else if(linkTypes.contains(1)&&linkTypes.contains(2)&&!linkTypes.contains(3)&&!linkTypes.contains(4)){
            mode = 5;
        }else if(linkTypes.contains(1)&&!linkTypes.contains(2)&&linkTypes.contains(3)&&!linkTypes.contains(4)){
            mode = 6;
        }else if(linkTypes.contains(1)&&!linkTypes.contains(2)&&!linkTypes.contains(3)&&linkTypes.contains(4)){
            mode = 7;
        }else if(!linkTypes.contains(1)&&linkTypes.contains(2)&&linkTypes.contains(3)&&!linkTypes.contains(4)){
            mode = 8;
        }else if(!linkTypes.contains(1)&&linkTypes.contains(2)&&!linkTypes.contains(3)&&linkTypes.contains(4)){
            mode = 9;
        }else if(!linkTypes.contains(1)&&!linkTypes.contains(2)&&linkTypes.contains(3)&&linkTypes.contains(4)){
            mode = 10;
        }else if(linkTypes.contains(1)&&linkTypes.contains(2)&&!linkTypes.contains(3)&&linkTypes.contains(4)){
            mode = 11;
        }else if(linkTypes.contains(1)&&!linkTypes.contains(2)&&linkTypes.contains(3)&&linkTypes.contains(4)){
            mode = 12;
        }else if(!linkTypes.contains(1)&&linkTypes.contains(2)&&linkTypes.contains(3)&&linkTypes.contains(4)){
            mode = 13;
        }else{
            mode = 14;
        }

    }

    double weiLen = -1;
    public double getPathWeiLength(){
        if(weiLen==-1){
            weiLen = 0;
            for(Link link:links){
                if(link.type==1){
                    weiLen+=link.length;
                }else if(link.type==2){
                    weiLen+=link.length*0.8;
                }else if(link.type==3){
                    weiLen+=link.length*0.6;
                }else if(link.type==4){
                    weiLen+=link.length*1.5;
                }else if(link.type==5){
                    weiLen+=link.length*4;
                }
            }
            return weiLen;
        }else{
            return weiLen;
        }
    }

    double travelTime = -1;
    public double getPathTravelTime(){
        if(travelTime==-1){
            travelTime =0;
            for(Link link:links){
                travelTime+=link.maxTravelTime;
            }
            return travelTime;
        }else{
            return travelTime;
        }
    }

    ///要换乘的线路，如果是walk或者bike就为-1,如果继续就返回0
    public int getTransferLine(Node currentNode){
        return transferNode.getOrDefault(currentNode.id, 0);
    }

    public Node getNextNode(Node currentNode) {
        if (currentNode == endNode) {
            return null;
        }else{
            int i = Nodes.indexOf(currentNode);
            return Nodes.get(i+1);
        }
    }

    /// 返回不能运送的乘客
    public int assignPassenger(int passengerNum, List<BaseLine> lines){
        int maxAssignPassengerNum = 9999999;
        for(Link l:links){
            int lineID = linkLine.get(l.id);
            if(lineID!=-1){
                for(BaseLine line:lines){
                    if(lineID==line.id){
                        int linkPassengers = 0;
                        if(line.linkPassengers.containsKey(l.id)){
                            linkPassengers = line.linkPassengers.get(l.id);
                        }
                        int canAssPassenger = line.maxLinkPassengers-linkPassengers;
                        if(maxAssignPassengerNum>canAssPassenger){
                            maxAssignPassengerNum = canAssPassenger;
                        }
                    }
                }
            }
        }
        if(maxAssignPassengerNum<passengerNum){
            for(Link l:links){
                int lineID = linkLine.get(l.id);
                if(lineID!=-1){
                    for(BaseLine line:lines){
                        if(lineID==line.id){
                            if(line.linkPassengers.containsKey(l.id)){
                                int linkPassengers = line.linkPassengers.get(l.id);
                                line.linkPassengers.put(l.id, maxAssignPassengerNum+linkPassengers);
                            }else{
                                line.linkPassengers.put(l.id, maxAssignPassengerNum);
                            }
                        }
                    }
                }
            }
            return passengerNum - maxAssignPassengerNum;
        }else{
            for(Link l:links){
                int lineID = linkLine.get(l.id);
                if(lineID!=-1){
                    for(BaseLine line:lines){
                        if(lineID==line.id){
                            if(line.linkPassengers.containsKey(l.id)){
                                int linkPassengers = line.linkPassengers.get(l.id);
                                line.linkPassengers.put(l.id, passengerNum+linkPassengers);
                            }else{
                                line.linkPassengers.put(l.id, passengerNum);
                            }
                        }
                    }
                }
            }
            return 0;
        }
    }

}
