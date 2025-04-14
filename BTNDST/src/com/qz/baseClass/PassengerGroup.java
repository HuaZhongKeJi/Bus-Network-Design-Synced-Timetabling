package com.qz.baseClass;

import com.qz.tool.BaseInfo;
import com.qz.tool.MatOperate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PassengerGroup {
    private int pointNum;

    public int getPassengerGroupID() {
        return passengerGroupID;
    }

    public void setPassengerGroupID(int passengerGroupID) {
        this.passengerGroupID = passengerGroupID;
    }

    private int passengerGroupID;

    private int[] start2PointDist;//出发点到各站点的步行距离
    private int[] end2PointDist;//终点到各站点的步行距离

    public List<Integer> getStartSWPoints() {
        return startSWPoints;
    }

    public List<Integer> getEndSWPoints() {
        return endSWPoints;
    }

    private List<Integer> startSWPoints;
    private List<Integer> endSWPoints;

    private List<Integer> startLWPoints;
    private List<Integer> endLWPoints;


    public int getPassengerNum() {
        return passengerNum;
    }

    public void setPassengerNum(int passengerNum) {
        this.passengerNum = passengerNum;
    }

    private int passengerNum;//乘客组内乘客数
    private int shortWalk = 15;//不算longwalk
    private int longWalk = 30;//longwalk

    public int getUnservicedPunish() {
        return unservicedPunish;
    }

    public void setUnservicedPunish(int unservicedPunish) {
        this.unservicedPunish = unservicedPunish;
    }

    private int unservicedPunish =0;

    public PassengerGroup(int point_num,int[] start_point_dist, int[] end_point_dist, int passenger_num){
        pointNum = point_num;
        start2PointDist = start_point_dist;
        end2PointDist = end_point_dist;
        passengerNum = passenger_num;
        startSWPoints = new ArrayList<>();
        endSWPoints = new ArrayList<>();
        startLWPoints = new ArrayList<>();
        endLWPoints = new ArrayList<>();
        //solution = tmSolution;

        for(int i=0;i<pointNum;i++){
            if(start2PointDist[i]<=shortWalk){
                startSWPoints.add(i);
            }else if(start2PointDist[i]<=longWalk){
                startLWPoints.add(i);
            }
            if(end2PointDist[i]<=shortWalk){
                endSWPoints.add(i);
            }else if(end2PointDist[i]<=longWalk){
                endLWPoints.add(i);
            }
        }
    }

    public PassengerGroup(int point_num,int[] start_point_dist, int[] end_point_dist, int passenger_num,int short_walk,int long_walk,int PassengerGroupID,int UnservicedPunish){
        pointNum = point_num;
        start2PointDist = start_point_dist;
        end2PointDist = end_point_dist;
        passengerNum = passenger_num;
        startSWPoints = new ArrayList<>();
        endSWPoints = new ArrayList<>();
        startLWPoints = new ArrayList<>();
        endLWPoints = new ArrayList<>();
        shortWalk = short_walk;
        longWalk = long_walk;
        unservicedPunish = UnservicedPunish;
        passengerGroupID = PassengerGroupID;
        //solution = tmSolution;

        for(int i=0;i<pointNum;i++){
            if(start2PointDist[i]<=shortWalk){
                startSWPoints.add(i);
            }else if(start2PointDist[i]<=longWalk){
                startLWPoints.add(i);
            }
            if(end2PointDist[i]<=shortWalk){
                endSWPoints.add(i);
            }else if(end2PointDist[i]<=longWalk){
                endLWPoints.add(i);
            }
        }

    }

    public List<PassengerRoute> findRoute0Ts0Lw(List<BusLine> bus_line){//计算0-trs-0-lw
        List<PassengerRoute> findPassengerRoute = new ArrayList<>();
        int[][] tmPassengerRoute = new int[pointNum][pointNum];
        int[][] tmArcType = new int[pointNum][pointNum];
        List<Integer> routeStartPointList = new ArrayList<>();
        List<Integer> routeEndPointList = new ArrayList<>();

        for (BusLine busLine : bus_line) {
            List<Integer> linePointList = busLine.getPointList();
            routeStartPointList.clear();
            routeEndPointList.clear();

            for (Integer startSWPoint : startSWPoints) {
                if (linePointList.contains(startSWPoint)) {
                    routeStartPointList.add(startSWPoint);
                }
            }
            for (Integer endSWPoint : endSWPoints) {
                if (linePointList.contains(endSWPoint)) {
                    routeEndPointList.add(endSWPoint);
                }
            }


            for (Integer value : routeStartPointList) {
                for (Integer integer : routeEndPointList) {

                    int tmPassengerRouteStart = value;
                    int tmPointS = tmPassengerRouteStart;
                    int tmPassengerRouteEnd = integer;
                    MatOperate.ZeroMat(pointNum, pointNum,tmPassengerRoute);

                    MatOperate.ZeroMat(pointNum, pointNum,tmArcType);

                    if (linePointList.indexOf(tmPassengerRouteStart) < linePointList.indexOf(tmPassengerRouteEnd)) {
                        for (int k = linePointList.indexOf(tmPassengerRouteStart) + 1; k <= linePointList.indexOf(tmPassengerRouteEnd); k++) {
                            tmPassengerRoute[tmPointS][linePointList.get(k)] = 1;
                            tmPassengerRoute[linePointList.get(k)][tmPointS] = 1;
                            tmArcType[tmPointS][linePointList.get(k)] = busLine.getLineNum();
                            tmArcType[linePointList.get(k)][tmPointS] = busLine.getLineNum();
                            tmPointS = linePointList.get(k);
                        }
                    } else if (linePointList.indexOf(tmPassengerRouteStart) > linePointList.indexOf(tmPassengerRouteEnd)) {
                        for (int k = linePointList.indexOf(tmPassengerRouteStart) - 1; k >= linePointList.indexOf(tmPassengerRouteEnd); k--) {
                            tmPassengerRoute[tmPointS][linePointList.get(k)] = 1;
                            tmPassengerRoute[linePointList.get(k)][tmPointS] = 1;
                            tmArcType[tmPointS][linePointList.get(k)] = busLine.getLineNum();
                            tmArcType[linePointList.get(k)][tmPointS] = busLine.getLineNum();
                            tmPointS = linePointList.get(k);
                        }
                    }
                    PassengerRoute tmPR = new PassengerRoute(pointNum, tmPassengerRouteStart, tmPassengerRouteEnd, tmPassengerRoute, this.passengerGroupID, tmArcType, start2PointDist[tmPassengerRouteStart], end2PointDist[tmPassengerRouteEnd]);

                    findPassengerRoute.add(tmPR);

                }
            }
        }

        if(findPassengerRoute.size()==0){
            return null;
        }
        return findPassengerRoute;
    }



    public List<PassengerRoute> findRoute1TsOR1Lw(List<BusLine> bus_line){//还需要检查是否有重复线路！，如果有则禁止该线路
        List<PassengerRoute> findPassengerRoute = new ArrayList<>();
        int[] startSwPoint = new int[pointNum];
        for(int i=0;i<pointNum;i++){
            if(startSWPoints.contains(i)){
                startSwPoint[i]=1;
            }else{
                startSwPoint[i]=0;
            }
        }

        int[] startLwPoint = new int[pointNum];
        for(int i=0;i<pointNum;i++){
            if(startLWPoints.contains(i)){
                startLwPoint[i]=1;
            }else{
                startLwPoint[i]=0;
            }
        }

        int[][] tmBusRouteReachability = MatOperate.IMat(BaseInfo.getPointNum());
        for (BusLine busLine : bus_line) {
            List<Integer> tmBusPoint = busLine.getPointList();
            for (int i1 = 0; i1 < pointNum; i1++) {
                for (int j1 = 0; j1 < pointNum; j1++) {
                    if (tmBusPoint.contains(i1) && tmBusPoint.contains(j1)) {
                        tmBusRouteReachability[i1][j1] = 1;
                        tmBusRouteReachability[j1][i1] = 1;
                    }
                }
            }
        }

        int[][] tmShortWalkReachability = BaseInfo.getShortWalkReachability();//shortwalk可达矩阵
        int[] tmReachability = new int[pointNum];//shortwalk加公交可达
        int[] tmReachability2 = new int[pointNum];//shortwalk+公交+shortwalk可达
        int[] tmReachability3 = new int[pointNum];//shortwalk+公交+shortwalk+公交可达（一次换乘）
        List<List<Integer>> busLineSeg1 = new ArrayList();
        List<List<Integer>> busLineSeg2 = new ArrayList();
        List<Integer> busLineNum1 = new ArrayList();
        List<Integer> busLineNum2 = new ArrayList();

        int[][] tmPassengerRoute = new int[pointNum][pointNum];
        int[][] tmArcType = new int[pointNum][pointNum];
        int[][] tTmPassengerRoute = new int[pointNum][pointNum];
        int[][] tTmArcType = new int[pointNum][pointNum];

        List<Integer> tmpCheckRepeat = new ArrayList<>();

                //1首站一次longWalk---------------------------------------------------------------------------------------------------------------------------------------------------------
        /*MatOperate.MatMul(startLwPoint,tmBusRouteReachability,tmReachability);//longwalk加公交可达
        for (int tmEndPoint : endSWPoints) {
            assert tmReachability != null;
            if (tmReachability[tmEndPoint] > 0) {//可以到达endSW点即tmEndPoint
                for (int tmStartPoint = 0; tmStartPoint < pointNum; tmStartPoint++) {
                    if (startLwPoint[tmStartPoint] > 0 && tmBusRouteReachability[tmStartPoint][tmEndPoint] > 0) {
                        if (tmStartPoint == tmEndPoint) {
                            continue;
                        }
                        busLineSeg1.clear();
                        busLineNum1.clear();

                        for (BusLine tmBusLine : bus_line) {//可能会有多条公交线路
                            List<Integer> tmTointList = tmBusLine.getPointList();
                            if (tmTointList.contains(tmEndPoint) && tmTointList.contains(tmStartPoint)) {
                                busLineSeg1.add(tmBusLine.getPointSeq(tmStartPoint, tmEndPoint));
                                busLineNum1.add(tmBusLine.getLineNum());
                            }
                        }

                        for (int bs1 = 0; bs1 < busLineSeg1.size(); bs1++) {//为重复的线路生成多条公交线路
                            List<Integer> tmBusLine1 = busLineSeg1.get(bs1);
                            MatOperate.ZeroMat(pointNum, pointNum,tTmPassengerRoute);
                            MatOperate.ZeroMat(pointNum, pointNum,tTmArcType);


                            for (int tmLine1Point = 0; tmLine1Point < tmBusLine1.size() - 1; tmLine1Point++) {

                                tTmPassengerRoute[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = 1;
                                tTmPassengerRoute[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = 1;

                                tTmArcType[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = busLineNum1.get(bs1);
                                tTmArcType[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = busLineNum1.get(bs1);
                            }

                            PassengerRoute tmPR = new PassengerRoute(pointNum, tmStartPoint, tmEndPoint, tTmPassengerRoute, this.passengerGroupID, tTmArcType, start2PointDist[tmStartPoint], end2PointDist[tmEndPoint]);

                            findPassengerRoute.add(tmPR);



                        }

                    }
                }
            }
        }*/

        //2末站一次longWalk---------------------------------------------------------------------------------------------------------------------------------------------------------

        /*MatOperate.MatMul(startLwPoint,tmBusRouteReachability,tmReachability);//longwalk加公交可达
        for (int tmEndPoint : endLWPoints) {
            assert tmReachability != null;
            if (tmReachability[tmEndPoint] > 0) {//可以到达endSW点即tmEndPoint
                for (int tmStartPoint = 0; tmStartPoint < pointNum; tmStartPoint++) {
                    if (startSwPoint[tmStartPoint] > 0 && tmBusRouteReachability[tmStartPoint][tmEndPoint] > 0) {
                        if (tmStartPoint == tmEndPoint) {
                            continue;
                        }

                        busLineSeg1.clear();
                        busLineNum1.clear();


                        for (BusLine tmBusLine : bus_line) {//可能会有多条公交线路
                            List<Integer> tmTointList = tmBusLine.getPointList();
                            if (tmTointList.contains(tmEndPoint) && tmTointList.contains(tmStartPoint)) {
                                busLineSeg1.add(tmBusLine.getPointSeq(tmStartPoint, tmEndPoint));
                                busLineNum1.add(tmBusLine.getLineNum());
                            }
                        }

                        for (int bs1 = 0; bs1 < busLineSeg1.size(); bs1++) {//为重复的线路生成多条公交线路
                            List<Integer> tmBusLine1 = busLineSeg1.get(bs1);
                            MatOperate.ZeroMat(pointNum, pointNum,tTmPassengerRoute);
                            MatOperate.ZeroMat(pointNum, pointNum,tTmArcType);


                            for (int tmLine1Point = 0; tmLine1Point < tmBusLine1.size() - 1; tmLine1Point++) {

                                tTmPassengerRoute[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = 1;
                                tTmPassengerRoute[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = 1;

                                tTmArcType[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = busLineNum1.get(bs1);
                                tTmArcType[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = busLineNum1.get(bs1);
                            }

                            PassengerRoute tmPR = new PassengerRoute(pointNum, tmStartPoint, tmEndPoint, tTmPassengerRoute, this.passengerGroupID, tTmArcType, start2PointDist[tmStartPoint], end2PointDist[tmEndPoint]);

                            findPassengerRoute.add(tmPR);

                        }
                    }
                }
            }
        }*/
        //3中间一次换乘---------------------------------------------------------------------------------------------------------------------------------------------------------------
        MatOperate.MatMul(startSwPoint,tmBusRouteReachability,tmReachability);//shortwalk加公交可达
        MatOperate.MatMul(tmReachability,tmShortWalkReachability,tmReachability2);//shortwalk+公交+shortwalk可达
        MatOperate.MatMul(tmReachability2,tmBusRouteReachability,tmReachability3);//shortwalk+公交+shortwalk+公交可达（一次换乘）

        for (int tmEndPoint : endSWPoints) {
            if (tmReachability3[tmEndPoint] > 0) {//可以到达endSW点即tmEndPoint
                for (int i2 = 0; i2 < pointNum; i2++) {
                    if (tmReachability2[i2] > 0 && tmBusRouteReachability[i2][tmEndPoint] > 0) {//寻找为tmReachability3贡献1的位置
                        if (i2 == tmEndPoint) {
                            continue;
                        }
                        //寻找短步行到i2的节点
                        for (int i3 = 0; i3 < pointNum; i3++) {
                            if (tmReachability[i3] > 0 && tmShortWalkReachability[i3][i2] > 0) {
                                for (int tmStartPoint = 0; tmStartPoint < pointNum; tmStartPoint++) {
                                    if (startSwPoint[tmStartPoint] > 0 && tmBusRouteReachability[tmStartPoint][i3] > 0) {
                                        if (i3 == tmStartPoint) {//代表不需要这一次公交，一次换乘即可达
                                            continue;
                                        }
                                        MatOperate.ZeroMat(pointNum, pointNum,tmPassengerRoute);
                                        MatOperate.ZeroMat(pointNum, pointNum,tmArcType);

                                        tmpCheckRepeat.clear();
                                        tmpCheckRepeat.add(tmStartPoint);

                                        busLineSeg1.clear();
                                        busLineSeg2.clear();
                                        busLineNum1.clear();
                                        busLineNum2.clear();


                                        for (BusLine tmBusLine : bus_line) {//可能会有多条公交线路
                                            List<Integer> tmTointList = tmBusLine.getPointList();
                                            if (tmTointList.contains(i3) && tmTointList.contains(tmStartPoint)) {
                                                busLineSeg1.add(tmBusLine.getPointSeq(tmStartPoint, i3));
                                                busLineNum1.add(tmBusLine.getLineNum());
                                            }

                                            if (tmTointList.contains(i2) && tmTointList.contains(tmEndPoint)) {
                                                busLineSeg2.add(tmBusLine.getPointSeq(i2, tmEndPoint));
                                                busLineNum2.add(tmBusLine.getLineNum());
                                            }

                                        }
                                        if (i2 != i3) {
                                            if (tmpCheckRepeat.contains(i2)) {
                                                continue;
                                            }
                                            tmpCheckRepeat.add(i2);

                                            tmPassengerRoute[i2][i3] = 1;
                                            tmPassengerRoute[i3][i2] = 1;
                                        }
                                        for (int bs1 = 0; bs1 < busLineSeg1.size(); bs1++) {//为重复的线路生成多条公交线路
                                            for (int bs2 = 0; bs2 < busLineSeg2.size(); bs2++) {
                                                if (!Objects.equals(busLineNum1.get(bs1), busLineNum2.get(bs2))) {
                                                    List<Integer> tmBusLine1 = busLineSeg1.get(bs1);
                                                    List<Integer> tmBusLine2 = busLineSeg2.get(bs2);

                                                    List<Integer> tmpCheckRepeatCopy = new ArrayList<>(tmpCheckRepeat);
                                                    boolean breakFlag = false;

                                                    MatOperate.ZeroMat(pointNum, pointNum,tTmPassengerRoute);
                                                    MatOperate.ZeroMat(pointNum, pointNum,tTmArcType);

                                                    for (int cp1 = 0; cp1 < pointNum; cp1++) {
                                                        for (int cp2 = 0; cp2 < pointNum; cp2++) {
                                                            tTmPassengerRoute[cp1][cp2] = tmPassengerRoute[cp1][cp2];
                                                            tTmArcType[cp1][cp2] = tmArcType[cp1][cp2];
                                                        }
                                                    }

                                                    for (int tmLine1Point = 0; tmLine1Point < tmBusLine1.size() - 1; tmLine1Point++) {
                                                        if (tmpCheckRepeatCopy.contains(tmBusLine1.get(tmLine1Point + 1))) {
                                                            breakFlag = true;
                                                            break;
                                                        } else {
                                                            tmpCheckRepeatCopy.add(tmBusLine1.get(tmLine1Point + 1));
                                                        }

                                                        tTmPassengerRoute[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = 1;
                                                        tTmPassengerRoute[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = 1;

                                                        tTmArcType[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = busLineNum1.get(bs1);
                                                        tTmArcType[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = busLineNum1.get(bs1);
                                                    }
                                                    for (int tmLine2Point = 0; tmLine2Point < tmBusLine2.size() - 1; tmLine2Point++) {
                                                        if (tmpCheckRepeatCopy.contains(tmBusLine2.get(tmLine2Point + 1))) {
                                                            breakFlag = true;
                                                            break;
                                                        } else {
                                                            tmpCheckRepeatCopy.add(tmBusLine2.get(tmLine2Point + 1));
                                                        }

                                                        tTmPassengerRoute[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = 1;
                                                        tTmPassengerRoute[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = 1;

                                                        tTmArcType[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = busLineNum2.get(bs2);
                                                        tTmArcType[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = busLineNum2.get(bs2);
                                                    }
                                                    if (breakFlag) {
                                                        continue;
                                                    }

                                                    PassengerRoute tmPR = new PassengerRoute(pointNum, tmStartPoint, tmEndPoint, tTmPassengerRoute, this.passengerGroupID, tTmArcType, start2PointDist[tmStartPoint], end2PointDist[tmEndPoint]);

                                                    findPassengerRoute.add(tmPR);


                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        if(findPassengerRoute.size()==0){
            return null;
        }
        return findPassengerRoute;
    }

    public List<PassengerRoute> findRoute2TsOR1Lw1TsOR2Lw(List<BusLine> bus_line){
        List<PassengerRoute> findPassengerRoute = new ArrayList<>();

        int[] startSwPoint = new int[pointNum];
        for(int i=0;i<pointNum;i++){
            if(startSWPoints.contains(i)){
                startSwPoint[i]=1;
            }else{
                startSwPoint[i]=0;
            }
        }

        int[] startLwPoint = new int[pointNum];
        for(int i=0;i<pointNum;i++){
            if(startLWPoints.contains(i)){
                startLwPoint[i]=1;
            }else{
                startLwPoint[i]=0;
            }
        }

        int[][] tmBusRouteReachability = MatOperate.IMat(BaseInfo.getPointNum());
        for(int i=0;i<bus_line.size();i++){
            List<Integer> tmBusPoint = bus_line.get(i).getPointList();
            for(int i1=0;i1<pointNum;i1++){
                for(int j1=0;j1<pointNum;j1++){
                    if(tmBusPoint.contains(i1)&&tmBusPoint.contains(j1)){
                        tmBusRouteReachability[i1][j1] = 1;
                        tmBusRouteReachability[j1][i1] = 1;
                    }
                }
            }
        }


        int[][] tmShortWalkReachability = BaseInfo.getShortWalkReachability();//shortwalk可达矩阵
        int[][] tmLongWalkReachability = BaseInfo.getLongWalkReachability();//shortwalk可达矩阵
        int[] tmReachability = new int[pointNum];
        int[] tmReachability2 = new int[pointNum];
        int[] tmReachability3 = new int[pointNum];
        int[] tmReachability4 = new int[pointNum];
        int[] tmReachability5 = new int[pointNum];
        List<Integer> tmpCheckRepeat = new ArrayList<>();
        int[][] tmPassengerRoute = new int[pointNum][pointNum];//MatOperate.ZeroMat(pointNum, pointNum);
        int[][] tmArcType = new int[pointNum][pointNum];

        int[][] tTmPassengerRoute = new int[pointNum][pointNum];
        int[][] tTmArcType = new int[pointNum][pointNum];

        List<List<Integer>> busLineSeg1 = new ArrayList();
        List<List<Integer>> busLineSeg2 = new ArrayList();
        List<List<Integer>> busLineSeg3 = new ArrayList();

        List<Integer> busLineNum1 = new ArrayList();
        List<Integer> busLineNum2 = new ArrayList();
        List<Integer> busLineNum3 = new ArrayList();

        List<Integer> tmpCheckRepeatCopy = new ArrayList<>();


        //两次换乘-------------------------------------------------------------------------------------------------------------------------------------------------------------
        //startSWPoints;

        MatOperate.MatMul(startSwPoint,tmBusRouteReachability,tmReachability);//shortwalk加公交可达
        MatOperate.MatMul(tmReachability,tmShortWalkReachability,tmReachability2);//shortwalk+公交+shortwalk可达
        MatOperate.MatMul(tmReachability2,tmBusRouteReachability,tmReachability3);//shortwalk+公交+shortwalk+公交可达（一次换乘）
        MatOperate.MatMul(tmReachability3,tmShortWalkReachability,tmReachability4);//shortwalk+公交+shortwalk+公交+shortwalk可达
        MatOperate.MatMul(tmReachability4,tmBusRouteReachability,tmReachability5);//shortwalk+公交+shortwalk+公交+shortwalk+公交可达（两次换乘）

        for (int tmEndPoint : endSWPoints) {
            if (tmReachability5[tmEndPoint] > 0) {//可以到达endSW点即tmEndPoint
                //遍历tmReachability3寻找公交可达tmEndPoint线路
                for (int i2 = 0; i2 < pointNum; i2++) {
                    if (tmReachability4[i2] > 0 && tmBusRouteReachability[i2][tmEndPoint] > 0) {//寻找为tmReachability3贡献1的位置
                        if (i2 == tmEndPoint) {
                            continue;
                        }
                        //寻找短步行到i2的节点
                        for (int i3 = 0; i3 < pointNum; i3++) {
                            if (tmReachability3[i3] > 0 && tmShortWalkReachability[i3][i2] > 0) {
                                //寻找公交可达i3的节点
                                for (int i4 = 0; i4 < pointNum; i4++) {
                                    if (tmReachability2[i4] > 0 && tmBusRouteReachability[i4][i3] > 0) {
                                        if (i3 == i4) {//代表不需要这一次公交，一次换乘即可达
                                            continue;
                                        }
                                        //再寻找sw可达i4的站点
                                        for (int i5 = 0; i5 < pointNum; i5++) {
                                            if (tmReachability[i5] > 0 && tmShortWalkReachability[i5][i4] > 0) {
                                                //再寻找公交可达i5的站点
                                                for (int tmStartPoint = 0; tmStartPoint < pointNum; tmStartPoint++) {
                                                    if (startSwPoint[tmStartPoint] > 0 && tmBusRouteReachability[tmStartPoint][i5] > 0) {
                                                        if (i5 == tmStartPoint) {//代表不需要这一次公交，一次换乘即可达
                                                            continue;
                                                        }
                                                        tmpCheckRepeat.clear();
                                                        //List<Integer> tmpCheckRepeat = new ArrayList<>();
                                                        tmpCheckRepeat.add(tmStartPoint);
                                                        //到此为止，乘客线路从tmStartPoint出发，到tmEndPoint终止,后面还需要推理出所有的线路
                                                        MatOperate.ZeroMat(pointNum, pointNum,tmPassengerRoute);
                                                        MatOperate.ZeroMat(pointNum, pointNum,tmArcType);

                                                        busLineSeg1.clear();
                                                        busLineSeg2.clear();
                                                        busLineSeg3.clear();
                                                        busLineNum1.clear();
                                                        busLineNum2.clear();
                                                        busLineNum3.clear();

                                                        for (BusLine tmBusLine : bus_line) {//可能会有多条公交线路
                                                            List<Integer> tmTointList = tmBusLine.getPointList();
                                                            if (tmTointList.contains(i5) && tmTointList.contains(tmStartPoint)) {
                                                                busLineSeg1.add(tmBusLine.getPointSeq(tmStartPoint, i5));
                                                                busLineNum1.add(tmBusLine.getLineNum());
                                                            }

                                                            if (tmTointList.contains(i4) && tmTointList.contains(i3)) {
                                                                busLineSeg2.add(tmBusLine.getPointSeq(i4, i3));
                                                                busLineNum2.add(tmBusLine.getLineNum());
                                                            }

                                                            if (tmTointList.contains(i2) && tmTointList.contains(tmEndPoint)) {
                                                                busLineSeg3.add(tmBusLine.getPointSeq(i2, tmEndPoint));
                                                                busLineNum3.add(tmBusLine.getLineNum());
                                                            }
                                                        }

                                                        if (i4 != i5) {
                                                            if (tmpCheckRepeat.contains(i4)) {
                                                                continue;
                                                            }
                                                            tmpCheckRepeat.add(i4);
                                                            tmPassengerRoute[i4][i5] = 1;
                                                            tmPassengerRoute[i5][i4] = 1;
                                                        }

                                                        if (i2 != i3) {
                                                            if (tmpCheckRepeat.contains(i2)) {
                                                                continue;
                                                            }
                                                            tmpCheckRepeat.add(i2);
                                                            tmPassengerRoute[i2][i3] = 1;
                                                            tmPassengerRoute[i3][i2] = 1;
                                                        }

                                                        for (int bs1 = 0; bs1 < busLineSeg1.size(); bs1++) {//为重复的线路生成多条公交线路
                                                            for (int bs2 = 0; bs2 < busLineSeg2.size(); bs2++) {
                                                                for (int bs3 = 0; bs3 < busLineSeg3.size(); bs3++) {//周转的线路号不能相同
                                                                    if (!Objects.equals(busLineNum1.get(bs1), busLineNum2.get(bs2)) && !Objects.equals(busLineNum1.get(bs1), busLineNum3.get(bs3)) && busLineNum2.get(bs2) != busLineNum3.get(bs3)) {
                                                                        List<Integer> tmBusLine1 = busLineSeg1.get(bs1);
                                                                        List<Integer> tmBusLine2 = busLineSeg2.get(bs2);
                                                                        List<Integer> tmBusLine3 = busLineSeg3.get(bs3);

                                                                        tmpCheckRepeatCopy.clear();
                                                                        tmpCheckRepeatCopy.addAll(tmpCheckRepeat);
                                                                        boolean breakFlag = false;

                                                                        MatOperate.ZeroMat(pointNum, pointNum,tTmPassengerRoute);
                                                                        MatOperate.ZeroMat(pointNum, pointNum,tTmArcType);

                                                                        for (int cp1 = 0; cp1 < pointNum; cp1++) {
                                                                            for (int cp2 = 0; cp2 < pointNum; cp2++) {
                                                                                tTmPassengerRoute[cp1][cp2] = tmPassengerRoute[cp1][cp2];
                                                                                tTmArcType[cp1][cp2] = tmArcType[cp1][cp2];
                                                                            }
                                                                        }

                                                                        for (int tmLine1Point = 0; tmLine1Point < tmBusLine1.size() - 1; tmLine1Point++) {
                                                                            if (tmpCheckRepeatCopy.contains(tmBusLine1.get(tmLine1Point + 1))) {
                                                                                breakFlag = true;
                                                                                break;
                                                                            } else {
                                                                                tmpCheckRepeatCopy.add(tmBusLine1.get(tmLine1Point + 1));
                                                                            }

                                                                            tTmPassengerRoute[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = 1;
                                                                            tTmPassengerRoute[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = 1;

                                                                            tTmArcType[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = busLineNum1.get(bs1);
                                                                            tTmArcType[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = busLineNum1.get(bs1);
                                                                        }
                                                                        for (int tmLine2Point = 0; tmLine2Point < tmBusLine2.size() - 1; tmLine2Point++) {
                                                                            if (tmpCheckRepeatCopy.contains(tmBusLine2.get(tmLine2Point + 1))) {
                                                                                breakFlag = true;
                                                                                break;
                                                                            } else {
                                                                                tmpCheckRepeatCopy.add(tmBusLine2.get(tmLine2Point + 1));
                                                                            }

                                                                            tTmPassengerRoute[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = 1;
                                                                            tTmPassengerRoute[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = 1;

                                                                            tTmArcType[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = busLineNum2.get(bs2);
                                                                            tTmArcType[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = busLineNum2.get(bs2);
                                                                        }
                                                                        for (int tmLine3Point = 0; tmLine3Point < tmBusLine3.size() - 1; tmLine3Point++) {
                                                                            if (tmpCheckRepeatCopy.contains(tmBusLine3.get(tmLine3Point + 1))) {
                                                                                breakFlag = true;
                                                                                break;
                                                                            } else {
                                                                                tmpCheckRepeatCopy.add(tmBusLine3.get(tmLine3Point + 1));
                                                                            }

                                                                            tTmPassengerRoute[tmBusLine3.get(tmLine3Point)][tmBusLine3.get(tmLine3Point + 1)] = 1;
                                                                            tTmPassengerRoute[tmBusLine3.get(tmLine3Point + 1)][tmBusLine3.get(tmLine3Point)] = 1;

                                                                            tTmArcType[tmBusLine3.get(tmLine3Point)][tmBusLine3.get(tmLine3Point + 1)] = busLineNum3.get(bs3);
                                                                            tTmArcType[tmBusLine3.get(tmLine3Point + 1)][tmBusLine3.get(tmLine3Point)] = busLineNum3.get(bs3);
                                                                        }
                                                                        if (breakFlag) {
                                                                            continue;
                                                                        }
                                                                        PassengerRoute tmPR = new PassengerRoute(pointNum, tmStartPoint, tmEndPoint, tTmPassengerRoute, this.passengerGroupID, tTmArcType, start2PointDist[tmStartPoint], end2PointDist[tmEndPoint]);
                                                                        findPassengerRoute.add(tmPR);
//                                                                        passengerRoute.add(tmPR);
//                                                                        route2Ts0Lw.add(tmPR);

                                                                    }
                                                                }
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        //可以先换乘，然后longwalk到终点------------------------------------------------------------------------------------------------------------------------------------------

        /*MatOperate.MatMul(startSwPoint,tmBusRouteReachability,tmReachability);//shortwalk加公交可达
        MatOperate.MatMul(tmReachability,tmShortWalkReachability,tmReachability2);//shortwalk+公交+shortwalk可达
        MatOperate.MatMul(tmReachability2,tmBusRouteReachability,tmReachability3);//shortwalk+公交+shortwalk+公交可达（一次换乘）

        for (int tmEndPoint : endLWPoints) {
            if (tmReachability3[tmEndPoint] > 0) {//可以到达endSW点即tmEndPoint
                for (int i2 = 0; i2 < pointNum; i2++) {
                    if (tmReachability2[i2] > 0 && tmBusRouteReachability[i2][tmEndPoint] > 0) {//寻找为tmReachability3贡献1的位置
                        if (i2 == tmEndPoint) {
                            continue;
                        }
                        //寻找短步行到i2的节点
                        for (int i3 = 0; i3 < pointNum; i3++) {
                            if (tmReachability[i3] > 0 && tmShortWalkReachability[i3][i2] > 0) {
                                for (int tmStartPoint = 0; tmStartPoint < pointNum; tmStartPoint++) {
                                    if (startSwPoint[tmStartPoint] > 0 && tmBusRouteReachability[tmStartPoint][i3] > 0) {
                                        if (i3 == tmStartPoint) {//代表不需要这一次公交，一次换乘即可达
                                            continue;
                                        }
                                        MatOperate.ZeroMat(pointNum, pointNum,tmPassengerRoute);
                                        MatOperate.ZeroMat(pointNum, pointNum,tmArcType);

                                        tmpCheckRepeat.clear();
                                        tmpCheckRepeat.add(tmStartPoint);

                                        busLineSeg1.clear();
                                        busLineSeg2.clear();
                                        busLineNum1.clear();
                                        busLineNum2.clear();

                                        for (BusLine tmBusLine : bus_line) {//可能会有多条公交线路
                                            List<Integer> tmTointList = tmBusLine.getPointList();
                                            if (tmTointList.contains(i3) && tmTointList.contains(tmStartPoint)) {
                                                busLineSeg1.add(tmBusLine.getPointSeq(tmStartPoint, i3));
                                                busLineNum1.add(tmBusLine.getLineNum());
                                            }

                                            if (tmTointList.contains(i2) && tmTointList.contains(tmEndPoint)) {
                                                busLineSeg2.add(tmBusLine.getPointSeq(i2, tmEndPoint));
                                                busLineNum2.add(tmBusLine.getLineNum());
                                            }

                                        }
                                        if (i2 != i3) {
                                            if (tmpCheckRepeat.contains(i2)) {
                                                continue;
                                            }
                                            tmpCheckRepeat.add(i2);

                                            tmPassengerRoute[i2][i3] = 1;
                                            tmPassengerRoute[i3][i2] = 1;
                                        }
                                        for (int bs1 = 0; bs1 < busLineSeg1.size(); bs1++) {//为重复的线路生成多条公交线路
                                            for (int bs2 = 0; bs2 < busLineSeg2.size(); bs2++) {
                                                if (!Objects.equals(busLineNum1.get(bs1), busLineNum2.get(bs2))) {

                                                    tmpCheckRepeatCopy.clear();
                                                    tmpCheckRepeatCopy.addAll(tmpCheckRepeat);
                                                    boolean breakFlag = false;

                                                    List<Integer> tmBusLine1 = busLineSeg1.get(bs1);
                                                    List<Integer> tmBusLine2 = busLineSeg2.get(bs2);

                                                    MatOperate.ZeroMat(pointNum, pointNum,tTmPassengerRoute);
                                                    MatOperate.ZeroMat(pointNum, pointNum,tTmArcType);

                                                    for (int cp1 = 0; cp1 < pointNum; cp1++) {
                                                        for (int cp2 = 0; cp2 < pointNum; cp2++) {
                                                            tTmPassengerRoute[cp1][cp2] = tmPassengerRoute[cp1][cp2];
                                                            tTmArcType[cp1][cp2] = tmArcType[cp1][cp2];
                                                        }
                                                    }

                                                    for (int tmLine1Point = 0; tmLine1Point < tmBusLine1.size() - 1; tmLine1Point++) {
                                                        if (tmpCheckRepeatCopy.contains(tmBusLine1.get(tmLine1Point + 1))) {
                                                            breakFlag = true;
                                                            break;
                                                        } else {
                                                            tmpCheckRepeatCopy.add(tmBusLine1.get(tmLine1Point + 1));
                                                        }

                                                        tTmPassengerRoute[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = 1;
                                                        tTmPassengerRoute[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = 1;

                                                        tTmArcType[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = busLineNum1.get(bs1);
                                                        tTmArcType[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = busLineNum1.get(bs1);
                                                    }
                                                    for (int tmLine2Point = 0; tmLine2Point < tmBusLine2.size() - 1; tmLine2Point++) {
                                                        if (tmpCheckRepeatCopy.contains(tmBusLine2.get(tmLine2Point + 1))) {
                                                            breakFlag = true;
                                                            break;
                                                        } else {
                                                            tmpCheckRepeatCopy.add(tmBusLine2.get(tmLine2Point + 1));
                                                        }

                                                        tTmPassengerRoute[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = 1;
                                                        tTmPassengerRoute[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = 1;

                                                        tTmArcType[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = busLineNum2.get(bs2);
                                                        tTmArcType[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = busLineNum2.get(bs2);
                                                    }
                                                    if (breakFlag) {
                                                        continue;
                                                    }

                                                    PassengerRoute tmPR = new PassengerRoute(pointNum, tmStartPoint, tmEndPoint, tTmPassengerRoute, this.passengerGroupID, tTmArcType, start2PointDist[tmStartPoint], end2PointDist[tmEndPoint]);
                                                    findPassengerRoute.add(tmPR);
//                                                    passengerRoute.add(tmPR);
//                                                    route1Ts1Lw.add(tmPR);

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }*/

        // longwalk然后换乘到终点-------------------------------------------------------------------------------------------------------------------------------------------------

        /*MatOperate.MatMul(startLwPoint,tmBusRouteReachability,tmReachability);//longwalk加公交可达
        MatOperate.MatMul(tmReachability,tmShortWalkReachability,tmReachability2);//shortwalk+公交+shortwalk可达
        MatOperate.MatMul(tmReachability2,tmBusRouteReachability,tmReachability3);//shortwalk+公交+shortwalk+公交可达（一次换乘）

        for (int tmEndPoint : endSWPoints) {
            if (tmReachability3[tmEndPoint] > 0) {//可以到达endSW点即tmEndPoint
                for (int i2 = 0; i2 < pointNum; i2++) {
                    if (tmReachability2[i2] > 0 && tmBusRouteReachability[i2][tmEndPoint] > 0) {//寻找为tmReachability3贡献1的位置
                        if (i2 == tmEndPoint) {
                            continue;
                        }
                        //寻找短步行到i2的节点
                        for (int i3 = 0; i3 < pointNum; i3++) {
                            if (tmReachability[i3] > 0 && tmShortWalkReachability[i3][i2] > 0) {
                                for (int tmStartPoint = 0; tmStartPoint < pointNum; tmStartPoint++) {
                                    if (startLwPoint[tmStartPoint] > 0 && tmBusRouteReachability[tmStartPoint][i3] > 0) {
                                        if (i3 == tmStartPoint) {//代表不需要这一次公交，一次换乘即可达
                                            continue;
                                        }
                                        MatOperate.ZeroMat(pointNum, pointNum,tmPassengerRoute);
                                        MatOperate.ZeroMat(pointNum, pointNum,tmArcType);

                                        tmpCheckRepeat.clear();
                                        tmpCheckRepeat.add(tmStartPoint);

                                        busLineSeg1.clear();
                                        busLineSeg2.clear();
                                        busLineNum1.clear();
                                        busLineNum2.clear();

                                        for (BusLine tmBusLine : bus_line) {//可能会有多条公交线路
                                            List<Integer> tmTointList = tmBusLine.getPointList();
                                            if (tmTointList.contains(i3) && tmTointList.contains(tmStartPoint)) {
                                                busLineSeg1.add(tmBusLine.getPointSeq(tmStartPoint, i3));
                                                busLineNum1.add(tmBusLine.getLineNum());
                                            }

                                            if (tmTointList.contains(i2) && tmTointList.contains(tmEndPoint)) {
                                                busLineSeg2.add(tmBusLine.getPointSeq(i2, tmEndPoint));
                                                busLineNum2.add(tmBusLine.getLineNum());
                                            }

                                        }
                                        if (i2 != i3) {
                                            if (tmpCheckRepeat.contains(i2)) {
                                                continue;
                                            }
                                            tmpCheckRepeat.add(i2);

                                            tmPassengerRoute[i2][i3] = 1;
                                            tmPassengerRoute[i3][i2] = 1;
                                        }
                                        for (int bs1 = 0; bs1 < busLineSeg1.size(); bs1++) {//为重复的线路生成多条公交线路
                                            for (int bs2 = 0; bs2 < busLineSeg2.size(); bs2++) {
                                                if (!Objects.equals(busLineNum1.get(bs1), busLineNum2.get(bs2))) {
                                                    List<Integer> tmBusLine1 = busLineSeg1.get(bs1);
                                                    List<Integer> tmBusLine2 = busLineSeg2.get(bs2);

                                                    tmpCheckRepeatCopy.clear();
                                                    tmpCheckRepeatCopy.addAll(tmpCheckRepeat);

                                                    boolean breakFlag = false;

                                                    MatOperate.ZeroMat(pointNum, pointNum,tTmPassengerRoute);
                                                    MatOperate.ZeroMat(pointNum, pointNum,tTmArcType);

                                                    for (int cp1 = 0; cp1 < pointNum; cp1++) {
                                                        for (int cp2 = 0; cp2 < pointNum; cp2++) {
                                                            tTmPassengerRoute[cp1][cp2] = tmPassengerRoute[cp1][cp2];
                                                            tTmArcType[cp1][cp2] = tmArcType[cp1][cp2];
                                                        }
                                                    }

                                                    for (int tmLine1Point = 0; tmLine1Point < tmBusLine1.size() - 1; tmLine1Point++) {

                                                        if (tmpCheckRepeatCopy.contains(tmBusLine1.get(tmLine1Point + 1))) {
                                                            breakFlag = true;
                                                            break;
                                                        } else {
                                                            tmpCheckRepeatCopy.add(tmBusLine1.get(tmLine1Point + 1));
                                                        }

                                                        tTmPassengerRoute[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = 1;
                                                        tTmPassengerRoute[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = 1;

                                                        tTmArcType[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = busLineNum1.get(bs1);
                                                        tTmArcType[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = busLineNum1.get(bs1);
                                                    }
                                                    for (int tmLine2Point = 0; tmLine2Point < tmBusLine2.size() - 1; tmLine2Point++) {

                                                        if (tmpCheckRepeatCopy.contains(tmBusLine2.get(tmLine2Point + 1))) {
                                                            breakFlag = true;
                                                            break;
                                                        } else {
                                                            tmpCheckRepeatCopy.add(tmBusLine2.get(tmLine2Point + 1));
                                                        }

                                                        tTmPassengerRoute[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = 1;
                                                        tTmPassengerRoute[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = 1;

                                                        tTmArcType[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = busLineNum2.get(bs2);
                                                        tTmArcType[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = busLineNum2.get(bs2);
                                                    }
                                                    if (breakFlag) {
                                                        continue;
                                                    }

                                                    PassengerRoute tmPR = new PassengerRoute(pointNum, tmStartPoint, tmEndPoint, tTmPassengerRoute, this.passengerGroupID, tTmArcType, start2PointDist[tmStartPoint], end2PointDist[tmEndPoint]);
                                                    findPassengerRoute.add(tmPR);
//                                                    passengerRoute.add(tmPR);
//                                                    route1Ts1Lw.add(tmPR);

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }*/

        //中间一次longwalk+换乘到终点-----------------------------------------------------------------------------------------------------------------------------------------------
        /*MatOperate.MatMul(startSwPoint,tmBusRouteReachability,tmReachability);//shortwalk加公交可达
        MatOperate.MatMul(tmReachability,tmLongWalkReachability,tmReachability2);//shortwalk+公交+shortwalk可达
        MatOperate.MatMul(tmReachability2,tmBusRouteReachability,tmReachability3);//shortwalk+公交+shortwalk+公交可达（一次换乘）

        for (int tmEndPoint : endSWPoints) {
            if (tmReachability3[tmEndPoint] > 0) {//可以到达endSW点即tmEndPoint
                for (int i2 = 0; i2 < pointNum; i2++) {
                    if (tmReachability2[i2] > 0 && tmBusRouteReachability[i2][tmEndPoint] > 0) {//寻找为tmReachability3贡献1的位置
                        if (i2 == tmEndPoint) {
                            continue;
                        }
                        //寻找短步行到i2的节点
                        for (int i3 = 0; i3 < pointNum; i3++) {
                            if (tmReachability[i3] > 0 && tmLongWalkReachability[i3][i2] > 0) {
                                for (int tmStartPoint = 0; tmStartPoint < pointNum; tmStartPoint++) {
                                    if (startLwPoint[tmStartPoint] > 0 && tmBusRouteReachability[tmStartPoint][i3] > 0) {
                                        if (i3 == tmStartPoint) {//代表不需要这一次公交，一次换乘即可达
                                            continue;
                                        }
                                        MatOperate.ZeroMat(pointNum, pointNum,tmPassengerRoute);
                                        MatOperate.ZeroMat(pointNum, pointNum,tmArcType);

                                        tmpCheckRepeat.clear();
                                        tmpCheckRepeat.add(tmStartPoint);

                                        busLineSeg1.clear();
                                        busLineSeg2.clear();
                                        busLineNum1.clear();
                                        busLineNum2.clear();

                                        for (BusLine tmBusLine : bus_line) {//可能会有多条公交线路
                                            List<Integer> tmTointList = tmBusLine.getPointList();
                                            if (tmTointList.contains(i3) && tmTointList.contains(tmStartPoint)) {
                                                busLineSeg1.add(tmBusLine.getPointSeq(tmStartPoint, i3));
                                                busLineNum1.add(tmBusLine.getLineNum());
                                            }

                                            if (tmTointList.contains(i2) && tmTointList.contains(tmEndPoint)) {
                                                busLineSeg2.add(tmBusLine.getPointSeq(i2, tmEndPoint));
                                                busLineNum2.add(tmBusLine.getLineNum());
                                            }

                                        }
                                        if (i2 != i3) {
                                            if (tmpCheckRepeat.contains(i2)) {
                                                continue;
                                            }
                                            tmpCheckRepeat.add(i2);

                                            tmPassengerRoute[i2][i3] = 1;
                                            tmPassengerRoute[i3][i2] = 1;
                                        }
                                        for (int bs1 = 0; bs1 < busLineSeg1.size(); bs1++) {//为重复的线路生成多条公交线路
                                            for (int bs2 = 0; bs2 < busLineSeg2.size(); bs2++) {
                                                if (!Objects.equals(busLineNum1.get(bs1), busLineNum2.get(bs2))) {
                                                    List<Integer> tmBusLine1 = busLineSeg1.get(bs1);
                                                    List<Integer> tmBusLine2 = busLineSeg2.get(bs2);

                                                    tmpCheckRepeatCopy.clear();
                                                    tmpCheckRepeatCopy.addAll(tmpCheckRepeat);
                                                    boolean breakFlag = false;

                                                    MatOperate.ZeroMat(pointNum, pointNum,tTmPassengerRoute);
                                                    MatOperate.ZeroMat(pointNum, pointNum,tTmArcType);

                                                    for (int cp1 = 0; cp1 < pointNum; cp1++) {
                                                        for (int cp2 = 0; cp2 < pointNum; cp2++) {
                                                            tTmPassengerRoute[cp1][cp2] = tmPassengerRoute[cp1][cp2];
                                                            tTmArcType[cp1][cp2] = tmArcType[cp1][cp2];
                                                        }
                                                    }

                                                    for (int tmLine1Point = 0; tmLine1Point < tmBusLine1.size() - 1; tmLine1Point++) {
                                                        if (tmpCheckRepeatCopy.contains(tmBusLine1.get(tmLine1Point + 1))) {
                                                            breakFlag = true;
                                                            break;
                                                        } else {
                                                            tmpCheckRepeatCopy.add(tmBusLine1.get(tmLine1Point + 1));
                                                        }

                                                        tTmPassengerRoute[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = 1;
                                                        tTmPassengerRoute[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = 1;

                                                        tTmArcType[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = busLineNum1.get(bs1);
                                                        tTmArcType[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = busLineNum1.get(bs1);
                                                    }
                                                    for (int tmLine2Point = 0; tmLine2Point < tmBusLine2.size() - 1; tmLine2Point++) {
                                                        if (tmpCheckRepeatCopy.contains(tmBusLine2.get(tmLine2Point + 1))) {
                                                            breakFlag = true;
                                                            break;
                                                        } else {
                                                            tmpCheckRepeatCopy.add(tmBusLine2.get(tmLine2Point + 1));
                                                        }

                                                        tTmPassengerRoute[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = 1;
                                                        tTmPassengerRoute[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = 1;

                                                        tTmArcType[tmBusLine2.get(tmLine2Point)][tmBusLine2.get(tmLine2Point + 1)] = busLineNum2.get(bs2);
                                                        tTmArcType[tmBusLine2.get(tmLine2Point + 1)][tmBusLine2.get(tmLine2Point)] = busLineNum2.get(bs2);
                                                    }
                                                    if (breakFlag) {
                                                        continue;
                                                    }

                                                    PassengerRoute tmPR = new PassengerRoute(pointNum, tmStartPoint, tmEndPoint, tTmPassengerRoute, this.passengerGroupID, tTmArcType, start2PointDist[tmStartPoint], end2PointDist[tmEndPoint]);
                                                    findPassengerRoute.add(tmPR);
//                                                    passengerRoute.add(tmPR);
//                                                    route1Ts1Lw.add(tmPR);

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }*/

        //首末站两次longwalk-------------------------------------------------------------------------------------------------------------------------------------------------------
        /*MatOperate.MatMul(startLwPoint,tmBusRouteReachability,tmReachability);//shortwalk加公交可达
        for (int tmEndPoint : endLWPoints) {
            if (tmReachability[tmEndPoint] > 0) {

                for (int tmStartPoint = 0; tmStartPoint < pointNum; tmStartPoint++) {
                    if (startLwPoint[tmStartPoint] > 0 && tmBusRouteReachability[tmStartPoint][tmEndPoint] > 0) {
                        if (tmEndPoint == tmStartPoint) {//代表不需要这一次公交，一次换乘即可达
                            continue;
                        }

                        tmpCheckRepeat.clear();
                        tmpCheckRepeat.add(tmStartPoint);

                        busLineSeg1.clear();
                        busLineNum1.clear();

                        for (BusLine tmBusLine : bus_line) {//可能会有多条公交线路
                            List<Integer> tmTointList = tmBusLine.getPointList();
                            if (tmTointList.contains(tmEndPoint) && tmTointList.contains(tmStartPoint)) {
                                busLineSeg1.add(tmBusLine.getPointSeq(tmStartPoint, tmEndPoint));
                                busLineNum1.add(tmBusLine.getLineNum());
                            }
                        }

                        for (int bs1 = 0; bs1 < busLineSeg1.size(); bs1++) {//为重复的线路生成多条公交线路
                            List<Integer> tmBusLine1 = busLineSeg1.get(bs1);
                            MatOperate.ZeroMat(pointNum, pointNum,tmPassengerRoute);
                            MatOperate.ZeroMat(pointNum, pointNum,tmArcType);

                            tmpCheckRepeatCopy.clear();
                            tmpCheckRepeatCopy.addAll(tmpCheckRepeat);

                            boolean breakFlag = false;

                            for (int tmLine1Point = 0; tmLine1Point < tmBusLine1.size() - 1; tmLine1Point++) {
                                if (tmpCheckRepeatCopy.contains(tmBusLine1.get(tmLine1Point + 1))) {
                                    breakFlag = true;
                                    break;
                                } else {
                                    tmpCheckRepeatCopy.add(tmBusLine1.get(tmLine1Point + 1));
                                }

                                tmPassengerRoute[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = 1;
                                tmPassengerRoute[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = 1;

                                tmArcType[tmBusLine1.get(tmLine1Point)][tmBusLine1.get(tmLine1Point + 1)] = busLineNum1.get(bs1);
                                tmArcType[tmBusLine1.get(tmLine1Point + 1)][tmBusLine1.get(tmLine1Point)] = busLineNum1.get(bs1);
                            }
                            if (breakFlag) {
                                continue;
                            }

                            PassengerRoute tmPR = new PassengerRoute(pointNum, tmStartPoint, tmEndPoint, tmPassengerRoute, this.passengerGroupID, tmArcType, start2PointDist[tmStartPoint], end2PointDist[tmEndPoint]);

                            findPassengerRoute.add(tmPR);
//                            passengerRoute.add(tmPR);
//                            route0Ts2Lw.add(tmPR);

                        }

                    }
                }

            }
        }*/


        if(findPassengerRoute.size()==0){
            return null;
        }
        return findPassengerRoute;
    }


    public void clearRoute(){
//        passengerRoute.clear();
//        route0Ts0Lw.clear();
    }

}
