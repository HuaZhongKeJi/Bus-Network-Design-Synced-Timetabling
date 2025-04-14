package com.qz.baseClass.Veh;

import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;
import com.qz.baseClass.Passenger.Passenger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseVeh {
    /// 1bus;2BRT;3Rail;4bike
    public int type;
    public int id;
    int capacity;
    /// 共享单车就设置为-1
    public int lineId;
    public static int tmID = 1;
    public List<Link> vehPath;
    Link onLink;
    public List<Passenger> passengersOnVeh;
    Node onNode;
    public boolean startedRun;

    public int travelTime;

    public int deptTime = 0;
    public int currentTravelTime = 0;

    double linkTraveledTime;
    double linkTravelTime;

    BaseVeh(int id, int type, int capacity,int lineId) {
        this.id = id;
        startedRun = false;
        this.type = type;
        this.capacity = capacity;
        this.lineId = lineId;
        travelTime =0;
        onNode = null;
        passengersOnVeh = new ArrayList<Passenger>();
    }

    public Link getNextLink(Link link) {
        int i = vehPath.indexOf(link);
        if (i == vehPath.size() - 1) {
            return null;
        }else{
            return vehPath.get(i+1);
        }
    }

    public void setVehPath(List<Link> vehPath) {
        this.vehPath = vehPath;
    }

    /// 发车 time 车辆还是运营时间
    public boolean deptVehicle(int time) {
        deptTime = time;
        currentTravelTime = 0;
        if(vehPath.isEmpty()){
            return false;
        }else{
            startedRun = true;
            onLink = vehPath.get(0);
            onNode = onLink.getStartNode();
            linkTravelTime = onLink.getLinkTravelTime(time);
            linkTraveledTime = 0;
            return true;
        }
    }

    public Node getOnNode(){
        return onNode;
    }

    public Link getOnLink(){
        return onLink;
    }

    /// Node返回到站的站点
    public Node run(){
        if(onLink == null){
            return vehPath.get(vehPath.size()-1).getEndNode();
        }
        travelTime ++;
        currentTravelTime++;
        linkTraveledTime ++;
        if(linkTraveledTime>=linkTravelTime){
            onNode = onLink.getEndNode();
            onLink = getNextLink(onLink);
            linkTraveledTime = 0;
            if(onLink == null){
                startedRun = false;
            }
            return onNode;
        }else{
            onNode = null;
            return null;
        }
    }

    //判断乘客下车
    public void alight(int time,Map<Integer,List<Passenger>>  newNodePassengers){
        if(onNode!=null){
            List<Passenger> delPassengers = new ArrayList<>();
            for(Passenger passenger:passengersOnVeh){
                if(passenger.alight(onNode,time)==1){
                    delPassengers.add(passenger);
                    if(newNodePassengers.containsKey(onNode.id)){
                        newNodePassengers.get(onNode.id).add(passenger);
                    }else{
                        List<Passenger> passengers = new ArrayList<>();
                        passengers.add(passenger);
                        newNodePassengers.put(onNode.id,passengers);
                    }
                }
            }
            passengersOnVeh.removeAll(delPassengers);
            if(!startedRun){
                onNode = null;
            }
        }
    }

    public void board(List<Passenger> passengers){
        if(getVehPassenger()<capacity && onLink!=null){
            List<Passenger> delPassengers = new ArrayList<>();
            for(Passenger passenger:passengers){
                if(passenger.board(this)==1){
                    passengersOnVeh.add(passenger);
                    passenger.onLink = null;
                    passenger.onNode = null;
                    passenger.isOnVeh = true;
                    delPassengers.add(passenger);
                    if(getVehPassenger()==capacity){
                        break;
                    }
                }
            }
            passengers.removeAll(delPassengers);
        }
    }

    public int getVehPassenger(){
        int passengerNum = 0;
        for(Passenger passenger:passengersOnVeh){
            if(!passenger.dummyPassenger){
                passengerNum++;
            }
        }
        return passengerNum;
    }

}
