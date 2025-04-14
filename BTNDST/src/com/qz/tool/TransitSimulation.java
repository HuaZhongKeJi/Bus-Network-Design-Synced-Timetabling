package com.qz.tool;

import com.qz.baseClass.Line.BRTLine;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Line.RailLine;
import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;
import com.qz.baseClass.Passenger.OD;
import com.qz.baseClass.Passenger.Passenger;
import com.qz.baseClass.Passenger.Path;
import com.qz.baseClass.Veh.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransitSimulation {

    static public int tmId = 1;
    int id;
    public int time = 1;
    int endTime;
    List<BRTLine> BRTlines;
    List<RailLine> RailLines;
    List<BusLineNew> busLines;
    public DynamicSchedule dynamicSchedule;
    /// 站点的车辆
    //Map<Integer, List<BaseVeh>> nodeVehicles;
    public TransitSimulation(int id, int endTime, List<BRTLine> BRTlines, List<RailLine> RailLines, List<BusLineNew> busLines){
        this.id=id;
        this.endTime=endTime;
        this.BRTlines=BRTlines;
        this.RailLines=RailLines;
        this.busLines=busLines;
        dynamicSchedule = new DynamicSchedule(busLines);
        finishPassengers = new ArrayList<>();
        vehicles = new ArrayList<>();
        nodeBikes = new HashMap<>();
        linkPassengers = new HashMap<>();
        newLinkPassengers = new HashMap<>();
        nodePassengers = new HashMap<>();
        newNodePassengers = new HashMap<>();
        busVehs = new ArrayList<>();
        setNodeBikes();
    }

    public void clean(){
        time = 1;
        totalTravelTime = 0;
        dynamicSchedule.clearDeptTime();
        totalP = 0;

        linkPassengers.clear();
        newLinkPassengers.clear();
        nodePassengers.clear();
        newNodePassengers.clear();

        vehicles.clear();
        busVehs.clear();
        finishPassengers.clear();
        setNodeBikes();
    }

    Map<Integer,List<Passenger>> linkPassengers;
    Map<Integer,List<Passenger>> newLinkPassengers;
    Map<Integer,List<Passenger>> nodePassengers;
    Map<Integer,List<Passenger>> newNodePassengers;

    /// node的自行车数量
    Map<Integer,Integer> nodeBikes;
    public void setNodeBikes(){
        for(Node n:Node.bikeNodes){
            nodeBikes.put(n.id,80);
        }
    }

    List<BaseVeh> vehicles;
    List<BusVeh> busVehs;
    public List<Passenger> finishPassengers;

    // link passenger run -> new node passenger(或者结束)
    //veh -> 下车 -> new node passenger
    //veh -> 上车 （new+old node passenger）
    //node -> run -> new link passenger
    //合并

    // link passenger run -> new node passenger(或者结束)
    public void runLinkPassenger(){
        linkPassengers.forEach((key, value) -> {
            List<Passenger> delPassengers = new ArrayList<>();
            for(Passenger passenger : value){
                Node n = passenger.run();
                if(n!=null){
                    delPassengers.add(passenger);
                    if(n.id==passenger.getEndNode().id){//passenger结束了
                        passenger.setEndTime(time);
                        finishPassengers.add(passenger);
                    }else{
                        if(newNodePassengers.containsKey(n.id)){
                            newNodePassengers.get(n.id).add(passenger);
                        }else{
                            List<Passenger> list = new ArrayList<>();
                            list.add(passenger);
                            newNodePassengers.put(n.id, list);
                        }
                    }
                }
            }
            //删除其他乘客
            linkPassengers.get(key).removeAll(delPassengers);
        });
    }

    //veh -> 下车 -> new node passenger
    public void alightingVehicles(){
        List<BaseVeh> delVeh = new ArrayList<>();
        for(BaseVeh veh:vehicles){
            if(veh.startedRun){
                Node n = veh.run();
                if(n!=null){
                    boolean dummyP = false;
                    if(!veh.passengersOnVeh.isEmpty()){
                        dummyP = veh.passengersOnVeh.get(0).dummyPassenger;
                    }
                    veh.alight(time,newNodePassengers);
                    if(veh.type==4){
                        int nodeId = veh.vehPath.get(0).getEndNode().id;
                        if(!dummyP){
                            if(nodeBikes.containsKey(nodeId)){
                                nodeBikes.put(nodeId,nodeBikes.get(nodeId)+1);
                            }else{
                                nodeBikes.put(nodeId,1);
                            }
                        }
                        delVeh.add(veh);
                    }
                }
            }
        }
        vehicles.removeAll(delVeh);
    }

    //veh -> 上车 （new+old node passenger）
    public void boardingVehicles(){
        for(BaseVeh veh:vehicles){
            Node n = veh.getOnNode();
            if(n!=null && veh.type!=4){

                if(nodePassengers.containsKey(n.id)){
                    veh.board(nodePassengers.get(n.id));
                }

                if(newNodePassengers.containsKey(n.id)){
                    veh.board(newNodePassengers.get(n.id));
                }


            }
        }
    }

    //node -> run -> new link passenger
    /// 步行，结束，骑车
    public void runNodePassenger(){

        newNodePassengers.forEach((key, value) ->{
            if(nodePassengers.containsKey(key)){
                nodePassengers.get(key).addAll(newNodePassengers.get(key));
            }else{
                nodePassengers.put(key,newNodePassengers.get(key));
            }
        });


        nodePassengers.forEach((key, value) ->{

            List<Passenger> delPassengers = new ArrayList<>();
            for(Passenger passenger : value){
                // 判断结束
                if(key == passenger.getEndNode().id){
                    passenger.setEndTime(time);
                    delPassengers.add(passenger);
                    finishPassengers.add(passenger);
                }else{
                    Node n = passenger.getNextNode();
                    Link link = Link.getLink(key,n.id);
                    // 判断步行
                    if(link.type==5){
                        passenger.onNode = null;
                        passenger.onLink = link;
                        passenger.linkTraveledTime = 0;
                        passenger.linkTravelTime = link.getLinkTravelTime(time);
                        if(newLinkPassengers.containsKey(link.id)){
                            newLinkPassengers.get(link.id).add(passenger);
                        }else{
                            List<Passenger> passengers = new ArrayList<>();
                            passengers.add(passenger);
                            newLinkPassengers.put(link.id,passengers);
                        }
                        delPassengers.add(passenger);
                    }else if(link.type==4){
                        if(nodeBikes.containsKey(key)&&nodeBikes.get(key)>0){

                            if(!passenger.dummyPassenger){
                                nodeBikes.put(key,nodeBikes.get(key)-1);
                            }
                            BikeVeh bike = new BikeVeh(BaseVeh.tmID);
                            BaseVeh.tmID ++;
                            List<Link> links = new ArrayList<>();
                            links.add(link);
                            bike.setVehPath(links);
                            bike.deptVehicle(time);
                            passenger.bikeBoard(bike);
                            vehicles.add(bike);

                            delPassengers.add(passenger);
                        }
                    }
                }
            }
            nodePassengers.get(key).removeAll(delPassengers);

        });
    }

    ///合并
    public void sumLinkPassengers(){
        newLinkPassengers.forEach((key, value) ->{
            if(linkPassengers.containsKey(key)){
                linkPassengers.get(key).addAll(newLinkPassengers.get(key));
            }else{
                linkPassengers.put(key,newLinkPassengers.get(key));
            }
        });

        newLinkPassengers.clear();
        newNodePassengers.clear();
    }

    /// false表示执行完成
    int tmP = 0;
    public boolean simulation(){
        if(time<=endTime){
            runLinkPassenger();
            alightingVehicles();

            deptVeh();

            boardingVehicles();
            runNodePassenger();
            sumLinkPassengers();

            time++;
            return true;
        }else{

            nodePassengers.forEach((key, value) ->{
                for(Passenger passenger : value){
                    passenger.unServiedPassenger = true;
                    finishPassengers.add(passenger);
                }
            });
            linkPassengers.forEach((key, value) ->{
                for(Passenger passenger : value){
                    passenger.unServiedPassenger = true;
                    finishPassengers.add(passenger);
                }
            });

            return false;
        }
    }

    public int getBunchingTimes(){
        return dynamicSchedule.getBunchingTimes();
    }

    public void deptVeh(){
        for(BusLineNew busLine:busLines){
            if(dynamicSchedule.dispatch(busLine.id,time,this)){
                boolean flag = false;
                for(BusVeh busVeh:busVehs){
                    if(busVeh.lineId==busLine.id&&!busVeh.startedRun){
                        busVeh.deptVehicle(time);
                        dynamicSchedule.setDeptTime(busLine.id,time);
                        flag = true;
                        break;
                    }
                }
                if(!flag && busVehs.size()<IniData.maxBusVehicles){
                    dynamicSchedule.setDeptTime(busLine.id,time);
                    BusVeh busVeh = new BusVeh(BaseVeh.tmID,IniData.busCapacity,busLine.id);
                    BaseVeh.tmID ++;
                    busVeh.setVehPath(busLine.links);
                    busVeh.deptVehicle(time);
                    vehicles.add(busVeh);
                    busVehs.add(busVeh);
                }
            }
        }

        for(BRTLine Bline:BRTlines){
            if(Bline.timeTable.contains(time)){
                BRTVeh BVeh = new BRTVeh(BaseVeh.tmID,IniData.BRTCapacity,Bline.id);
                BaseVeh.tmID ++;
                BVeh.setVehPath(Bline.links);
                BVeh.deptVehicle(time);
                vehicles.add(BVeh);
            }
        }

        for(RailLine railLine:RailLines){
            if(railLine.timeTable.contains(time)){
                RailVeh railVeh = new RailVeh(BaseVeh.tmID,IniData.railCapacity,railLine.id);
                BaseVeh.tmID ++;
                railVeh.setVehPath(railLine.links);
                railVeh.deptVehicle(time);
                vehicles.add(railVeh);
            }
        }

    }

    public int getNodePassengers(int nodeId,int lineId){

        if(!nodePassengers.containsKey(nodeId)){
            return 0;
        }
        int passengerNum = 0;
        for(Passenger passenger:nodePassengers.get(nodeId)){
            if(passenger.path.getTransferLine(Node.getNode(nodeId))==lineId){
                passengerNum ++;
            }
        }
        return passengerNum;
    }

    public int getBusTravelTime(){
        int travelTime = 0;
        for(BusVeh busVeh:busVehs){
            travelTime += busVeh.travelTime;
        }
        return travelTime;
    }

    public int totalP = 0;
    public void addPassengerNode(Passenger passenger){
        totalP ++;
        passenger.clean();
        if(passenger.path==null){
            passenger.unServiedPassenger = true;
            //finishP++;
            finishPassengers.add(passenger);
        }else{
            if(nodePassengers.containsKey(passenger.getStartNode().id)){
                nodePassengers.get(passenger.getStartNode().id).add(passenger);
            }else{
                List<Passenger> passengers = new ArrayList<>();
                passengers.add(passenger);
                nodePassengers.put(passenger.getStartNode().id,passengers);
            }


        }
    }

    public Map<Integer,Map<Integer,Map<Integer,Integer>>> pathTravelTimes;//time,OD,pathID,travelTime
    public Map<Integer,Map<Integer,Map<Integer,Integer>>> pathPassengers;//time,OD,pathID,乘客数
    public Map<Integer,Map<Integer,Map<Integer,Integer>>> modeMinTravelTimes;//time,OD,mode,travel time
    public Map<Integer,Map<Integer,Map<Integer,Integer>>> shortestPath;//time,mode,OD,pathID

    public double totalTravelTime;
    public int serP = 0;
    public void setPathTravelTime(List<OD> ods){
        totalTravelTime =0;
        pathTravelTimes = new HashMap<>();
        modeMinTravelTimes = new HashMap<>();
        pathPassengers = new HashMap<>();
        shortestPath = new HashMap<>();
        serP = 0;
        for(Passenger p:finishPassengers){
            if(p.path==null&&p.unServiedPassenger){
                continue;
            }
            serP++;
            int time = p.startTime;
            int pathID = p.path.id;
            int ODid = p.OD;
            int pathTravelTime = p.endTime-p.startTime;
            if(p.unServiedPassenger){
                pathTravelTime = IniData.endTime-p.startTime+1000;
            }
            if(!p.dummyPassenger){
                totalTravelTime += pathTravelTime;
            }
            int pathMode = p.path.mode;

            int passengerNum = 1;
            if(pathPassengers.containsKey(time)&&pathPassengers.get(time).containsKey(ODid)&&pathPassengers.get(time).get(ODid).containsKey(pathID)){
                passengerNum = pathPassengers.get(time).get(ODid).get(pathID);
                addMap(pathPassengers,time,ODid,pathID,passengerNum+1);
            }else{
                addMap(pathPassengers,time,ODid,pathID,1);
            }

            if(pathTravelTimes.containsKey(time)&&pathTravelTimes.get(time).containsKey(ODid)&&pathTravelTimes.get(time).get(ODid).containsKey(pathID)){
                int travelTime = pathTravelTimes.get(time).get(ODid).get(pathID);
                addMap(pathTravelTimes,time,ODid,pathID,(passengerNum*travelTime+pathTravelTime)/(passengerNum+1));

            }else{
                addMap(pathTravelTimes,time,ODid,pathID,pathTravelTime);
            }

        }

        for(int time=1;time<=IniData.endTime;time++){
            for(OD od:ods){
                for (Map.Entry<Integer, Path> entry : od.paths.entrySet()) {
                    Integer pathID = entry.getKey();
                    Path path = entry.getValue();
                    int pathMode = path.mode;
                    int ODid = od.id;
                    if(!pathTravelTimes.containsKey(time)||!pathTravelTimes.get(time).containsKey(ODid)||!pathTravelTimes.get(time).get(ODid).containsKey(pathID)){
                        continue;
                    }
                    int pathTravelTime = pathTravelTimes.get(time).get(ODid).get(pathID);
                    if(modeMinTravelTimes.containsKey(time)&&modeMinTravelTimes.get(time).containsKey(ODid)&&modeMinTravelTimes.get(time).get(ODid).containsKey(pathMode)){
                        int modeTravelTime = modeMinTravelTimes.get(time).get(ODid).get(pathMode);
                        if(pathTravelTime<modeTravelTime){
                            addMap(shortestPath,time,ODid,pathMode,pathID);
                            addMap(modeMinTravelTimes,time,ODid,pathMode,pathTravelTime);
                        }
                    }else{
                        addMap(shortestPath,time,ODid,pathMode,pathID);
                        addMap(modeMinTravelTimes,time,ODid,pathMode,pathTravelTime);
                    }
                }
            }
        }



    }

    public void addMap(Map<Integer,Map<Integer,Map<Integer,Integer>>> map,int key1,int key2, int key3,int value){
        Map<Integer,Map<Integer,Integer>> map1;
        if(map.containsKey(key1)){
            map1 = map.get(key1);
            Map<Integer,Integer> map2;
            if(map1.containsKey(key2)){
                map2 = map1.get(key2);
            }else{
                map2 = new HashMap<>();
            }
            map2.put(key3, value);
            map1.put(key2,map2);
        }else{
            map1 = new HashMap<>();
            Map<Integer,Integer> map2=new HashMap<>();
            map2.put(key3, value);
            map1.put(key2,map2);
        }
        map.put(key1,map1);
    }

    public int getSyTimes(){
        return dynamicSchedule.getTimeSyNum();
    }

    public int getUnServedPassengers(){
        int unServedPassengers = 0;
        for(Passenger p:finishPassengers){
            if(p.unServiedPassenger&&!p.dummyPassenger){
                unServedPassengers++;
            }
        }
        return unServedPassengers;
    }

    public int getUnPathPassengers(){
        int unPathPassengers = 0;
        for(Passenger p:finishPassengers){
            if(p.path==null){
                unPathPassengers++;
            }
        }
        return unPathPassengers;
    }

}
