package com.qz.baseClass;

import com.qz.tool.BaseInfo;
import com.qz.tool.MatOperate;

import java.util.ArrayList;
import java.util.List;

public class BusLine extends Route{
    private int lineNum;//线路号
    public int getLineType() {
        return LineType;
    }

    public void setLineType(int lineType) {
        LineType = lineType;
    }

    private int LineType;

    public int getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
    }

    private int travelTime=0;

    public float getFreq() {
        return freq;
    }

    public void setFreq(float freq) {
        this.freq = freq;
    }

    public int[][] passengerLoad;

    private float  freq=0;

    public int getMaxPassengerLoad() {
        if(maxPassengerLoad==0){
            if(LineType==0){//BRT
                maxPassengerLoad = (int)Math.round(freq*BaseInfo.getBRTCapacity()-0.5);
            }else{
                maxPassengerLoad = (int)Math.round(freq*BaseInfo.getBusCapacity()-0.5);
            }

        }
        return maxPassengerLoad;
    }

    public void setMaxPassengerLoad(int maxPassengerLoad) {
        this.maxPassengerLoad = maxPassengerLoad;
    }

    private int maxPassengerLoad = 0;

    public BusLine(int point_num, int start_point,int end_point,int[][] route,int line_num,int lineType) {

        super(point_num, start_point,end_point,route);
        int[][] pointDriveTime = BaseInfo.getPointDriveTime();
        for(int i = 0; i< BaseInfo.getPointNum(); i++){
            for(int j=0;j< BaseInfo.getPointNum();j++){
                if(route[i][j]==1){
                    travelTime += pointDriveTime[i][j];
                }
            }
        }
        passengerLoad = MatOperate.ZeroMat(point_num,point_num);
        LineType = lineType;
        lineNum = line_num;
    }

    public void cleanPassengerLoad(){
        MatOperate.ZeroMat(pointNum,pointNum,passengerLoad);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public float getMaxFreq(float[] BRTCommonRouteBijMax){//线路的最大发车间隔

        if(LineType==0){//BRT
            return freq;
        }else{
            List<Integer> tmLinePointList = getPointList();
            float maxFreq = 999;
            int[][] BRTCommonRoute = BaseInfo.getBRTCommonRoute();
            for(int i=1;i<tmLinePointList.size();i++){//每一段去判断最大容量能是多少
                if(BRTCommonRoute[tmLinePointList.get(i-1)][tmLinePointList.get(i)]>0){//是BRT线路
                    if(BRTCommonRouteBijMax[BRTCommonRoute[tmLinePointList.get(i-1)][tmLinePointList.get(i)]-1]<maxFreq){
                        maxFreq = BRTCommonRouteBijMax[BRTCommonRoute[tmLinePointList.get(i-1)][tmLinePointList.get(i)]-1];
                    }
                }else{
                    if(BaseInfo.getMaxFeq()<maxFreq){
                        maxFreq = (float)BaseInfo.getMaxFeq();
                    }
                }
            }
            if(BaseInfo.getMaxFeq()<maxFreq){
                maxFreq = (float)BaseInfo.getMaxFeq();
            }
            return maxFreq;
        }
    }

    public int getRemainPassengerNum(int start,int end,float[] BRTCommonRouteBijMax){//剩余可分配客流
        List<Integer> pointSeq=getPointSeq(start,end);
        //List<Integer> pointSeq2=getPointList();

        float maxFreq = getMaxFreq(BRTCommonRouteBijMax);
        int maxPassenger = 0;
        if(LineType==0){//BRT
            maxPassenger = (int)(BaseInfo.getBRTFeq()*BaseInfo.getBRTCapacity());
        }else{
            maxPassenger = (int)(maxFreq*BaseInfo.getBusCapacity());
        }


        int tmRemainPassenger = 99999;
        for(int i=1;i<pointSeq.size();i++){
            int t = maxPassenger-passengerLoad[pointSeq.get(i-1)][pointSeq.get(i)];
            if(t<tmRemainPassenger){
                tmRemainPassenger = t;
            }
        }

        return (Math.max(tmRemainPassenger, 0));
    }

    public int setPassengerLoadAndUpdateFreq(int start,int end,int passengerNum,float[] BRTCommonRouteBijMax){//分配客流
        List<Integer> pointSeq=getPointSeq(start,end);
        int[][] BRTCommonRoute = BaseInfo.getBRTCommonRoute();
        int remainPassengerNum  = getRemainPassengerNum(start,end,BRTCommonRouteBijMax);//先找出剩余能分配客流
        float tmFre = 0;
//        float tmpMaxPassengerNum = freq*BaseInfo.getBRTCapacity();
        float tmF = 0;
        if(passengerNum-remainPassengerNum>0){//分配remainPassengerNum
            if(LineType==0){
                //tmFre = remainPassengerNum/BaseInfo.getBRTCapacity();
                for(int i=1;i<pointSeq.size();i++){
                    passengerLoad[pointSeq.get(i-1)][pointSeq.get(i)] += remainPassengerNum;
                    passengerLoad[pointSeq.get(i)][pointSeq.get(i-1)] += remainPassengerNum;
                }
            }else{
                //tmFre = (float)remainPassengerNum/(float)BaseInfo.getBusCapacity();
                for(int i=1;i<pointSeq.size();i++){
                    passengerLoad[pointSeq.get(i-1)][pointSeq.get(i)] += remainPassengerNum;
                    passengerLoad[pointSeq.get(i)][pointSeq.get(i-1)] += remainPassengerNum;
                    if((float)passengerLoad[pointSeq.get(i)][pointSeq.get(i-1)]/(float)BaseInfo.getBusCapacity()>tmF){
                        tmF = (float)passengerLoad[pointSeq.get(i)][pointSeq.get(i-1)]/(float)BaseInfo.getBusCapacity();
                    }
                }
                if(tmF>freq){
                    tmFre = tmF - freq;
                }else{
                    tmFre = 0;
                }
                for(int i=1;i<pointSeq.size();i++){
                    if(BRTCommonRoute[pointSeq.get(i-1)][pointSeq.get(i)]>0){//BRT线路
                        BRTCommonRouteBijMax[BRTCommonRoute[pointSeq.get(i-1)][pointSeq.get(i)]-1] -= tmFre;
                    }
                }

                freq += tmFre;
                if(freq>BaseInfo.getMaxFeq()){
                    int fssss = 0;
                }
            }

            return (passengerNum-remainPassengerNum);
        }else{//分配passengerNum
            if(LineType==0){
                for(int i=1;i<pointSeq.size();i++){
                    passengerLoad[pointSeq.get(i-1)][pointSeq.get(i)] += passengerNum;
                    passengerLoad[pointSeq.get(i)][pointSeq.get(i-1)] += passengerNum;
                }
            }else{
                //tmFre = (float)passengerNum/(float)BaseInfo.getBusCapacity();
                for(int i=1;i<pointSeq.size();i++){
                    passengerLoad[pointSeq.get(i-1)][pointSeq.get(i)] += passengerNum;
                    passengerLoad[pointSeq.get(i)][pointSeq.get(i-1)] += passengerNum;
                    if((float)passengerLoad[pointSeq.get(i)][pointSeq.get(i-1)]/(float)BaseInfo.getBusCapacity()>tmF){
                        tmF = (float)passengerLoad[pointSeq.get(i)][pointSeq.get(i-1)]/(float)BaseInfo.getBusCapacity();
                    }
                }
                if(tmF>freq){
                    tmFre = tmF - freq;
                }else{
                    tmFre = 0;
                }
                for(int i=1;i<pointSeq.size();i++){
                    if(BRTCommonRoute[pointSeq.get(i-1)][pointSeq.get(i)]>0){//BRT线路
                        BRTCommonRouteBijMax[BRTCommonRoute[pointSeq.get(i-1)][pointSeq.get(i)]-1] -= tmFre;
                    }
                }
                freq += tmFre;

            }
            return 0;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------------------------------
    public int getLineNum() {
        return lineNum;
    }

    public List<Integer> getPointSeq(int start_Point,int end_Point){
        List<Integer> pointList = new ArrayList<>();
        pointList.add(start_Point);
        int tmPoint = start_Point;
        int tmp = 0;
        while(tmPoint!=end_Point && tmp<=pointNum){
            for(int i=0;i<pointNum;i++){
                if(routeMat[tmPoint][i]==1 && !pointList.contains(i)){
                    pointList.add(i);
                    tmPoint = i;
                    break;
                }
            }
            tmp ++;
        }

        if(tmp>pointNum){
            pointList.clear();
            pointList.add(start_Point);
            tmPoint = start_Point;
            tmp = 0;
            while(tmPoint!=end_Point && tmp<=pointNum){
                for(int i=pointNum-1;i>=0;i--){
                    if(routeMat[tmPoint][i]==1 && !pointList.contains(i)){
                        pointList.add(i);
                        tmPoint = i;
                        break;
                    }
                }
                tmp ++;
            }
        }

        if(tmp>pointNum){
            return null;
        }else{
            return pointList;
        }


    }
}
