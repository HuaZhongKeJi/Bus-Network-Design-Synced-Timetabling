package com.qz.baseClass;

import com.qz.tool.BaseInfo;

import java.util.ArrayList;
import java.util.List;

public class PassengerRoute extends Route{
    private int[][] arcType;//标识路线属于那一条公交线路，如果没有标识就表明为步行
    private int passengerTravelTime =0;//该条线路的乘客旅行时间

    private int PureTravelTime = 0;

    private int longWalkNum =0;
    private int shortWalkNum =0;
    private int trsNum =-1;
    private int start2Point =0;
    private int end2Point =0;
    private int passengerNum =0;


    public int getPassengerNum() {
        return passengerNum;
    }

    public void setPassengerNum(int passengerNum) {
        this.passengerNum = passengerNum;
    }

    public int getPureTravelTime() {
        return PureTravelTime;
    }

    public int getPassengerTravelTime() {
        return passengerTravelTime;
    }

    public void setPassengerTravelTime(int passengerTravelTime) {
        this.passengerTravelTime = passengerTravelTime;
    }

    public int getLongWalkNum() {
        return longWalkNum;
    }

    public void setLongWalkNum(int longWalkNum) {
        this.longWalkNum = longWalkNum;
    }

    public int getShortWalkNum() {
        return shortWalkNum;
    }

    public void setShortWalkNum(int shortWalkNum) {
        this.shortWalkNum = shortWalkNum;
    }

    public int getTrsNum() {
        return trsNum;
    }

    public void setTrsNum(int trsNum) {
        this.trsNum = trsNum;
    }

    public List<Integer> getTrsLine() {
        return trsLine;
    }

    public List<Integer> trsLine;//乘客线路经过的公交

    public float passengerCostDivideBusCost;

    public float getPassengerCostDivideBusCost() {
        return passengerCostDivideBusCost;
    }

    public void setPassengerCostDivideBusCost(List<BusLine> busLines) {
        float sumBusCost = 0;
        for (BusLine busLine : busLines) {
            if (busLine.getLineType() == 1 && trsLine.contains(busLine.getLineNum())) {
                sumBusCost += busLine.getTravelTime()*BaseInfo.getCb();
            }
        }
        passengerCostDivideBusCost = passengerTravelTime*BaseInfo.getCp() + sumBusCost/BaseInfo.getBusCapacity();
//        passengerCostDivideBusCost = (float)passengerTravelTime/(float)sumBusCost;
    }



    public int[][] getArcType() {
        return arcType;
    }

    public int getPassengerGroupID() {
        return passengerGroupID;
    }

    public void setPassengerGroupID(int passengerGroupID) {
        this.passengerGroupID = passengerGroupID;
    }

    private int passengerGroupID;//所属的乘客
    public PassengerRoute(int point_num,int start_point,int end_point,int[][] route,int passenger_group_id,int[][] arc_type,int start2_point,int end2_point) {
        super(point_num, start_point,end_point,route);
        start2Point = start2_point;
        end2Point = end2_point;
        passengerGroupID = passenger_group_id;

        List<Integer> tmPassengerRouteList = getPointList();

        arcType = new int[point_num][point_num];

        for(int i=0;i<point_num;i++){
            for(int j=0;j<point_num;j++){
                arcType[i][j] = arc_type[i][j];
            }
        }

        int[][] pointWalkTime = BaseInfo.getPointWalkTime();
        int[][] pointDriveTime = BaseInfo.getPointDriveTime();

        trsLine = new ArrayList<>();
        for(int i=0;i< BaseInfo.getPointNum();i++){
            for(int j=0;j< BaseInfo.getPointNum();j++){
                if(route[i][j]==1){
                    if(arc_type[i][j]==0){
                        passengerTravelTime+=pointWalkTime[i][j];
                        if(pointWalkTime[i][j]<=BaseInfo.getShortWalk()){
                            shortWalkNum++;
                        }else{
                            longWalkNum++;
                        }
                    }else if(arc_type[i][j]<=BaseInfo.getBRTNum()){
                        if(!trsLine.contains(arc_type[i][j])){
                            trsLine.add(arc_type[i][j]);
                            trsNum++;
                        }
                        passengerTravelTime+=pointDriveTime[i][j];
                    }else{
                        if(!trsLine.contains(arc_type[i][j])){
                            trsLine.add(arc_type[i][j]);
                            trsNum++;
                        }
                        passengerTravelTime+=pointDriveTime[i][j];
                    }
                }
            }
        }


        longWalkNum = longWalkNum/2;
        shortWalkNum = shortWalkNum/2;

        if(start2_point>0&&start2_point<BaseInfo.getShortWalk()){
            shortWalkNum++;
        }else{
            longWalkNum++;
        }

        if(end2_point>0&&end2_point<BaseInfo.getShortWalk()){
            shortWalkNum++;
        }else{
            longWalkNum++;
        }

        passengerTravelTime = passengerTravelTime/2;
        passengerTravelTime += start2_point;
        passengerTravelTime += end2_point;

        PureTravelTime = passengerTravelTime;

        passengerTravelTime += longWalkNum*BaseInfo.getLongWalkPunish();
        passengerTravelTime += shortWalkNum*BaseInfo.getShortWalkPunish();




        passengerTravelTime += trsNum*BaseInfo.getTrsPunish();
    }

    public int assignPassenger(int passenger_num,float[] BRTCommonRouteBijMax,List<BusLine> busLines){
        //busLines.get(0).setFreq(999);
        List<Integer> tmPassengerRouteList = getPointList();
        //分段筛选出走了哪条公交线路的哪一段，然后再去判断能否分配，最后返回不能分配的乘客
        List<int[]> busSegs = new ArrayList<>();
        int tmBusLine = this.arcType[tmPassengerRouteList.get(0)][tmPassengerRouteList.get(1)];
        int tmStartP=0;
        int tmEndP;
        if(tmBusLine != 0){
            tmStartP = tmPassengerRouteList.get(0);
        }
        for(int i=1;i<tmPassengerRouteList.size()-1;i++){
            int tmPointS = tmPassengerRouteList.get(i);
            int tmPointE = tmPassengerRouteList.get(i+1);

            if(tmBusLine!=this.arcType[tmPointS][tmPointE]){
                if(tmBusLine!=0){//说明要换线了
                    tmEndP = tmPointS;

                    int[] busSeg = new int[3];
                    busSeg[0] = tmStartP;
                    busSeg[1] = tmEndP;
                    busSeg[2] = tmBusLine;
                    busSegs.add(busSeg);

                }
                if(this.arcType[tmPointS][tmPointE]!=0){//说明下一条是公交线路
                    tmStartP = tmPointS;
                    if(i == tmPassengerRouteList.size()-2){
                        tmEndP = tmPointE;

                        int[] busSeg = new int[3];
                        busSeg[0] = tmStartP;
                        busSeg[1] = tmEndP;
                        busSeg[2] = this.arcType[tmPointS][tmPointE];
                        busSegs.add(busSeg);
                    }
                }
            }else if(i == tmPassengerRouteList.size()-2){//最后一个了
                if(this.arcType[tmPointS][tmPointE]!=0){//说明要换线了
                    tmEndP = tmPointE;

                    int[] busSeg = new int[3];
                    busSeg[0] = tmStartP;
                    busSeg[1] = tmEndP;
                    busSeg[2] = this.arcType[tmPointS][tmPointE];
                    busSegs.add(busSeg);

                }
            }

            tmBusLine = this.arcType[tmPointS][tmPointE];

        }

        int tmMaxAssignPass = passenger_num;
        for (int[] tmBusSeg : busSegs) {//每一段去判断最大容量能是多少
            for (BusLine busLine : busLines) {
                if (busLine.getLineNum() == tmBusSeg[2]) {
                    int tmAssignPass = busLine.getRemainPassengerNum(tmBusSeg[0], tmBusSeg[1], BRTCommonRouteBijMax);
                    if (tmMaxAssignPass > tmAssignPass) {
                        tmMaxAssignPass = tmAssignPass;
                    }
                }
            }
        }

        for (int[] tmBusSeg : busSegs) {//每一段去分配客流
            for (BusLine busLine : busLines) {
                if (busLine.getLineNum() == tmBusSeg[2]) {
                    busLine.setPassengerLoadAndUpdateFreq(tmBusSeg[0], tmBusSeg[1], tmMaxAssignPass, BRTCommonRouteBijMax);
                }
            }
        }
        this.passengerNum = tmMaxAssignPass;
        return passenger_num-tmMaxAssignPass;
    }

    public void setArcType(int[][] arc_type){
        arcType = arc_type;
    }
}
