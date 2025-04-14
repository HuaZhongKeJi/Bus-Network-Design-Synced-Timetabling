package com.qz.baseClass.Passenger;

import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;
import com.qz.baseClass.Veh.BaseVeh;

import java.util.ArrayList;
import java.util.List;

public class Passenger {
    public int id;
    public int OD;
    public Path path;
    public boolean isOnVeh;
    public Link onLink;
    public Node onNode;
    public Node startNode;
    public Node endNode;
    public boolean dummyPassenger = false;
    public double linkTraveledTime;
    public double linkTravelTime;
    public int startTime;
    public int endTime;
    public boolean unServiedPassenger;
    public static int tmId =1;

    public Passenger(int id, Node startNode, Node endNode, int startTime) {
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        this.startTime = startTime;
        isOnVeh = false;
        onNode = startNode;
        onLink = null;
        this.path =null;
    }

    public void clean(){
        isOnVeh = false;
        onNode = startNode;
        onLink = null;
        endTime = -1;
    }

    public List<Integer> getBoardingNode(){
        List<Integer> boardingNode = new ArrayList<>();
        int busNodeNum = Node.busNodes.size();
        if(startNode.type==1){
            boardingNode.add(Node.busNodes.indexOf(startNode));
        }else if(startNode.type==2){
            boardingNode.add(busNodeNum+Node.BRTNodes.indexOf(startNode));
        }else{
            for(Node node:startNode.toNodes()){
                if(node.type==1){
                    boardingNode.add(Node.busNodes.indexOf(node));
                }else if(node.type==2){
                    boardingNode.add(busNodeNum+Node.BRTNodes.indexOf(node));
                }
            }
        }
        return boardingNode;
    }

    public void setPath(Path path){
        this.path = path;
        endTime = -1;
    }

    public Node getNextNode(){
        return path.getNextNode(onNode);
    }

    public void setEndTime(int endTime){
        this.endTime = endTime;
    }
    public int alightTime = -1;

    /// 1表示要下车，0表示继续
    public int alight(Node node,int time){
        int transferLine = path.getTransferLine(node);
        if(transferLine==0){
            return 0;
        }else{
            isOnVeh = false;
            onNode = node;
            alightTime = time;
            return 1;
        }
    }

    public void bikeBoard(BaseVeh veh){
        onLink = null;
        onNode = null;
        isOnVeh = true;
        veh.passengersOnVeh.add(this);
    }

    /// 1表示要上车，0表示继续
    public int board(BaseVeh veh){
        if(onNode == null){
            return 0;
        }else{
            Node nextNode = path.getNextNode(onNode);
            Link nextLink = Link.getLink(onNode.id,nextNode.id);
            if(nextLink.id == veh.getOnLink().id&&path.linkLine.get(nextLink.id)==veh.lineId){
                return 1;
            }else{
                return 0;
            }
        }
    }



    public Node getStartNode() {
        return startNode;
    }
    public Node getEndNode() {
        return endNode;
    }

    public Node run(){
        if(onLink == null){
            return null;
        }
        linkTraveledTime ++;
        if(linkTraveledTime>=linkTravelTime){
            linkTraveledTime = 0;
            Node n = onLink.getEndNode();
            onNode = n;
            onLink = null;
            return n;
        }else{
            onNode = null;
            return null;
        }
    }

    public Node getOnNode(){
        return onNode;
    }

}
