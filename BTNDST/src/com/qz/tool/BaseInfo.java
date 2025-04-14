package com.qz.tool;

import com.qz.baseClass.BusLine;
import com.qz.baseClass.PassengerGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseInfo {
    public static int[][] getPointWalkTime() {
        return pointWalkTime;
    }

    public static void setBRTLine(List<BusLine> BRTLine) {
        BaseInfo.BRTLine = BRTLine;
    }


    public static int[][] getLinkType() {
        return linkType;
    }

    public static void setLinkType(int[][] linkType) {
        BaseInfo.linkType = linkType;
    }

    public static int getTrsPunish() {
        return trsPunish;
    }

    public static void setTrsPunish(int trsPunish) {
        BaseInfo.trsPunish = trsPunish;
    }

    private static int trsPunish = 15;
    private static int shortWalkPunish = 10;
    private static int longWalkPunish = 30;

    public static int getMaxFrequency() {
        return maxFrequency;
    }

    public static void setMaxFrequency(int maxFrequency) {
        BaseInfo.maxFrequency = maxFrequency;
    }

    private static int maxFrequency = 30;

    public static int getCb() {
        return Cb;
    }

    public static void setCb(int cb) {
        Cb = cb;
    }

    public static int getCp() {
        return Cp;
    }

    public static void setCp(int cp) {
        Cp = cp;
    }

    private static int Cb = 20;
    private static int Cp = 20;

    public static int getLongWalkPunish() {
        return longWalkPunish;
    }

    public static void setLongWalkPunish(int longWalkPunish) {
        BaseInfo.longWalkPunish = longWalkPunish;
    }

    public static int getShortWalkPunish() {
        return shortWalkPunish;
    }

    public static void setShortWalkPunish(int shortWalkPunish) {
        BaseInfo.shortWalkPunish = shortWalkPunish;
    }


    private static int[][] linkType;

    public static int getBRTCommonNum() {
        return BRTCommonNum;
    }

    public static void setBRTCommonNum(int BRTCommonNum) {
        BaseInfo.BRTCommonNum = BRTCommonNum;
    }

    private static int BRTCommonNum;

    public static int[][] getBRTCommonRoute() {
        return BRTCommonRoute;
    }

    public static void setBRTCommonRoute(int[][] BRTCommonRoute) {
        BaseInfo.BRTCommonRoute = BRTCommonRoute;
    }

    public static float[] getBRTCommonRouteBijMax() {
        return BRTCommonRouteBijMax;
    }

    public static void setBRTCommonRouteBijMax(float[] BRTCommonRouteBijMax) {
        /*float[] tmBRTCommonRouteBijMax = new float[BaseInfo.getBRTCommonNum()];
        for(int is=0;is<pointNum;is++){
            for(int js=0;js<pointNum;js++){
                if(BRTCommonRoute[is][js]!=0){
                    tmBRTCommonRouteBijMax[BRTCommonRoute[is][js]-1] = (BRTCommonRouteBijMax[BRTCommonRoute[is][js]-1]*60)/pointDriveTime[is][js];
                }
            }
        }*/

        BaseInfo.BRTCommonRouteBijMax = BRTCommonRouteBijMax;
    }

    private static float[] BRTCommonRouteBijMax;
    private static int[][] BRTCommonRoute;
    private static int[][] pointWalkTime;
    private static int[][] pointDriveTime;

    public static int getMinLineNum() {
        return minLineNum;
    }

    public static void setMinLineNum(int minLineNum) {
        BaseInfo.minLineNum = minLineNum;
    }

    public static int getMaxLineNum() {
        return maxLineNum;
    }

    public static void setMaxLineNum(int maxLineNum) {
        BaseInfo.maxLineNum = maxLineNum;
    }

    private static int minLineNum;
    private static int maxLineNum;
    private static int busCapacity;
    private static int BRTCapacity;

    public static int getBusCapacity() {
        return busCapacity;
    }

    public static void setBusCapacity(int busCapacity) {
        BaseInfo.busCapacity = busCapacity;
    }

    public static int getBRTCapacity() {
        return BRTCapacity;
    }

    public static void setBRTCapacity(int BRTCapacity) {
        BaseInfo.BRTCapacity = BRTCapacity;
    }

    public static int getShortWalk() {
        return shortWalk;
    }

    private static int shortWalk = 15;//不算longwalk

    public static int getLongWalk() {
        return longWalk;
    }

    private static int longWalk = 30;//longwalk
    private static int pointNum;

    public static int getPassengerPointNum() {
        return passengerPointNum;
    }

    public static void setPassengerPointNum(int passengerPointNum) {
        BaseInfo.passengerPointNum = passengerPointNum;
    }

    private static int passengerPointNum;

    private static double BRTFeq;

    public static double getMinFeq() {
        return minFeq;
    }

    public static void setMinFeq(double minFeq) {
        BaseInfo.minFeq = minFeq;
    }

    private static double minFeq;

    public static double getMaxFeq() {
        return maxFeq;
    }

    public static void setMaxFeq(double maxFeq) {
        BaseInfo.maxFeq = maxFeq;
    }

    private static double maxFeq;

    private static int BRTNum;

    public static int getMinLinePointNum() {
        return minLinePointNum;
    }

    public static void setMinLinePointNum(int minLinePointNum) {
        BaseInfo.minLinePointNum = minLinePointNum;
    }

    public static int getMaxLinePointNum() {
        return maxLinePointNum;
    }

    public static void setMaxLinePointNum(int maxLinePointNum) {
        BaseInfo.maxLinePointNum = maxLinePointNum;
    }

    private static int minLinePointNum;
    private static int maxLinePointNum;
    private static List<BusLine> BRTLine = new ArrayList<>();

    public static List<PassengerGroup> getPassengerGroups() {
        return passengerGroups;
    }

    public static void setPassengerGroups(List<PassengerGroup> passenger_groups) {
        passengerGroups = passenger_groups;
    }

    private static List<PassengerGroup> passengerGroups = new ArrayList<>();
    //private static List<BusLine> busLine;

    private static int[][] shortWalkReachability;
    private static int[][] longWalkReachability;


    public static void setPointWalkTime(int[][] pointWorkTime) {
        BaseInfo.pointWalkTime = pointWorkTime;
    }

    public static void setPointDriveTime(int[][] pointDriveTime) {
        BaseInfo.pointDriveTime = pointDriveTime;
    }

    public static void setPointNum(int pointNum) {
        BaseInfo.pointNum = pointNum;
    }

    public static void setShortWalk(int shortWalk) {
        BaseInfo.shortWalk = shortWalk;
    }

    public static void setLongWalk(int longWalk) {
        BaseInfo.longWalk = longWalk;
    }

    public static void setBRTLine(int BRT_num,List<BusLine> BRT_line) {
        BRTNum = BRT_num;
        BRTLine = BRT_line;
    }

    public static void setBRTFeq(double BRTFeq) {
        BaseInfo.BRTFeq = BRTFeq;
    }

    public static double getBRTFeq() {
        return BRTFeq;
    }

    public static void setBRTNum(int BRTNum) {
        BaseInfo.BRTNum = BRTNum;
    }

    public static int getBRTNum() {
        return BRTNum;
    }

    public static List<BusLine> getBRTLine() {
        for(int i=0;i<BRTLine.size();i++){
            BRTLine.get(i).cleanPassengerLoad();
        }
        return BRTLine;
    }

    public static int getPointNum() {
        return pointNum;
    }

    public static int[][] getPointDriveTime() {
        return pointDriveTime;
    }

    public static int[][] getShortWalkReachability() {
        if(shortWalkReachability==null || shortWalkReachability.length==0){
            shortWalkReachability = MatOperate.IMat(pointNum);
            for(int i=0;i<pointNum;i++){
                for(int j=0;j<pointNum;j++){
                    if(pointWalkTime[i][j]<=shortWalk){
                        shortWalkReachability[i][j] = 1;
                    }
                }
            }
        }
        return shortWalkReachability;
    }

    public static int[][] getLongWalkReachability() {
        if(longWalkReachability==null || longWalkReachability.length==0){
            longWalkReachability = MatOperate.IMat(pointNum);
            for(int i=0;i<pointNum;i++){
                for(int j=0;j<pointNum;j++){
                    if(pointWalkTime[i][j]>shortWalk && pointWalkTime[i][j]<=longWalk){
                        longWalkReachability[i][j] = 1;
                    }
                }
            }
        }
        return longWalkReachability;
    }

    public static int getMaxTravelTime() {
        return maxTravelTime;
    }

    public static void setMaxTravelTime(int maxTravelTimes) {
        maxTravelTime = maxTravelTimes;
    }

    private static int maxTravelTime = 120;

    public static List<List<Integer>> getBusLineSet() {
        return busLineSet;
    }

    private static List<List<Integer>> pointReachabilitySet;//站点可达矩阵集合，用于编码和解码

    public static List<List<Integer>> busLineSet;
    private static int[][] point2pointSLine;
    private static int[][] busLinePosition;

    public static void iniBusLineSet(){

        pointReachabilitySet = new ArrayList<>();
        pointNum = BaseInfo.getPointNum();
        //生成可达矩阵集合
        int[][] pointDriveTime = BaseInfo.getPointDriveTime();
        for (int i = 0; i < pointNum; i++) {
            List<Integer> tmPointReachability = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                if (pointDriveTime[i][j] < 999) {
                    tmPointReachability.add(j);
                }
            }
            pointReachabilitySet.add(tmPointReachability);
        }

        point2pointSLine = MatOperate.ZeroMat(pointNum,pointNum);
        busLineSet = new ArrayList<>();
        busLinePosition = MatOperate.NumMat(pointNum,pointNum,-1);

        for(int s=0;s<pointNum;s++){
            List<Integer> tmPointSet = new ArrayList<>();
            tmPointSet.add(s);
            checkPoint(busLineSet,tmPointSet,s,0);
        }

    }

    private static void checkPoint(List<List<Integer>> busLineSet,List<Integer> tmPointSet,int nowPoint,int travelTime){
        for(int i=0;i<pointReachabilitySet.get(nowPoint).size();i++){
            if(!tmPointSet.contains(pointReachabilitySet.get(nowPoint).get(i))){
                int tmEPoint = pointReachabilitySet.get(nowPoint).get(i);
                int tmSPoint = tmPointSet.get(0);
                int tmTravelTime = travelTime+2*BaseInfo.getPointDriveTime()[nowPoint][tmEPoint];
                if(tmSPoint>tmEPoint){
                    if(tmTravelTime<=maxTravelTime && (point2pointSLine[tmEPoint][tmSPoint]>tmTravelTime||point2pointSLine[tmEPoint][tmSPoint]==0)){

                        List<Integer> tmPointSet1 = deepCopy(tmPointSet);
                        tmPointSet1.add(tmEPoint);

                        if(tmPointSet1.size()>=BaseInfo.getMinLinePointNum()){
                            if(busLinePosition[tmEPoint][tmSPoint]==-1){
                                busLinePosition[tmEPoint][tmSPoint] = busLineSet.size();
                                point2pointSLine[tmEPoint][tmSPoint] = tmTravelTime;
                                busLineSet.add(tmPointSet1);
                            }else{
                                point2pointSLine[tmEPoint][tmSPoint] = tmTravelTime;
                                busLineSet.set(busLinePosition[tmEPoint][tmSPoint],tmPointSet1);
                            }

                        }
                        if(tmPointSet1.size()<BaseInfo.getMaxLinePointNum()){
                            checkPoint(busLineSet,tmPointSet1,tmEPoint,tmTravelTime);
                        }
                    }
                }else if(tmSPoint<tmEPoint){
                    if(tmTravelTime<=maxTravelTime && (point2pointSLine[tmSPoint][tmEPoint]>tmTravelTime||point2pointSLine[tmSPoint][tmEPoint]==0)){

                        List<Integer> tmPointSet1 = deepCopy(tmPointSet);
                        tmPointSet1.add(tmEPoint);
                        if(tmPointSet1.size()>=BaseInfo.getMinLinePointNum()){
                            if(busLinePosition[tmSPoint][tmEPoint]==-1){
                                busLinePosition[tmSPoint][tmEPoint] = busLineSet.size();
                                point2pointSLine[tmSPoint][tmEPoint] = tmTravelTime;
                                busLineSet.add(tmPointSet1);
                            }else{
                                point2pointSLine[tmSPoint][tmEPoint] = tmTravelTime;
                                busLineSet.set(busLinePosition[tmSPoint][tmEPoint],tmPointSet1);
                            }

                        }
                        if(tmPointSet1.size()<BaseInfo.getMaxLinePointNum()){
                            checkPoint(busLineSet,tmPointSet1,tmEPoint,tmTravelTime);
                        }
                    }
                }
            }
        }
    }

    public static <T> List<T> deepCopy(List<T> src) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteOut);
        ) {
            outputStream.writeObject(src);
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream inputStream = new ObjectInputStream(byteIn);
            ) {
                return (List<T>) inputStream.readObject();
            }
        } catch (Exception ignored) {

        }
        return Collections.emptyList();
    }

}
