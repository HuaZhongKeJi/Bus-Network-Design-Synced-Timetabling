package com.qz.tool;

import com.qz.baseClass.BusLine;
import com.qz.baseClass.PassengerGroup;
import com.qz.baseClass.PassengerRoute;
import com.qz.network.GA;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EvaluateRunnable implements Runnable {
    public boolean finFlag = false;
    private Thread t;
    private int threadID;
    private  List<List<int[]>> solutionList;
    private List<Double> tmSolutionSufficiency;
    private List<List<Double>> tmSolutionPartSufficiency;
    private List<List<Float>> tmSolutionFrq;
    private int startIndex;
    private List<PassengerGroup> passengerGroups;
    private int pointNum;
    private List<List<Integer>> unSerPassenger;
    List<List<Integer>> pointReachabilitySet;

    public EvaluateRunnable(int ThreadID, int StartIndex, int PointNum, List<PassengerGroup> PassengerGroups,
                            List<List<int[]>> SolutionList, List<Double> TmSolutionSufficiency,
                            List<List<Double>> TmSolutionPartSufficiency, List<List<Float>> TmSolutionFrq, List<List<Integer>> PointReachabilitySet, List<List<Integer>> UnSerPassenger) {
        solutionList = SolutionList;
        threadID = ThreadID;
        tmSolutionSufficiency = TmSolutionSufficiency;
        tmSolutionPartSufficiency = TmSolutionPartSufficiency;
        tmSolutionFrq = TmSolutionFrq;
        startIndex = StartIndex;
        passengerGroups = PassengerGroups;
        pointNum = PointNum;
        pointReachabilitySet = PointReachabilitySet;
        unSerPassenger = UnSerPassenger;
    }

    public void run() {//实验组
        System.out.println("Running " +  threadID );
        try {


            double tmMaxCost = 0;
            for (PassengerGroup group : passengerGroups) {
                tmMaxCost += group.getPassengerNum() * group.getUnservicedPunish();
            }


            for (int i = 0; i < solutionList.size(); i++) {//遍历所有的解


                Double totalTraveltime = 0.0;
                Double traveledPassenger = 0.0;
                Double unservedPassenger = 0.0;
                Double transferNumber = 0.0;
                Double routeNumber = 0.0;
                Double operatingCost = 0.0;


                long t1=System.currentTimeMillis();

                int PassengerNotAsignRes = 0;
                List<int[]> ints = solutionList.get(i);
                List<BusLine> tmBusLine = deCode(ints);

                double tmCost = 0;

                float[] BRTCommonRouteBijMax = BaseInfo.getBRTCommonRouteBijMax();
                float[] tmBRTCommonRouteBijMax = new float[BaseInfo.getBRTCommonNum()];
                int[][] BRTCommonRoute = BaseInfo.getBRTCommonRoute();

                System.arraycopy(BRTCommonRouteBijMax, 0, tmBRTCommonRouteBijMax, 0, BaseInfo.getBRTCommonNum());

                for (int ib = 0; ib < BaseInfo.getBRTNum(); ib++) {//去掉现有的BRT发车频率
                    float tmFrq = tmBusLine.get(ib).getFreq();
                    List<Integer> tmPointList = tmBusLine.get(ib).getPointList();
                    for (int jb = 1; jb < tmPointList.size(); jb++) {
                        tmBRTCommonRouteBijMax[BRTCommonRoute[tmPointList.get(jb - 1)][tmPointList.get(jb)] - 1] -= tmFrq;
                    }
                }

                int tmPassengerNotAss = 0;

                for (PassengerGroup group : passengerGroups) {//此处的passengerGroup已经按照惩罚成本排序
                    //List<PassengerRoute> tmPassengerRoutes = new ArrayList<>();
                    //PassengerGroupRoutes.clear();
                    int tmPassengerNum = group.getPassengerNum();

                    List<PassengerRoute> PassengerRoutes1 = group.findRoute0Ts0Lw(tmBusLine);//短步行可以直达
                    if (PassengerRoutes1 != null) {
                        for (PassengerRoute passengerRoute : PassengerRoutes1) {
                            passengerRoute.setPassengerCostDivideBusCost(tmBusLine);
                        }
                        for (PassengerRoute passengerRoute : PassengerRoutes1) {
                            passengerRoute.setPassengerCostDivideBusCost(tmBusLine);
                        }
                        PassengerRoutes1.sort(new Comparator<PassengerRoute>() {//按照乘客成本/公交成本排序，依次分配客流
                            @Override
                            public int compare(PassengerRoute p1, PassengerRoute p2) {
                                float diff = p1.getPassengerCostDivideBusCost() - p2.getPassengerCostDivideBusCost();
                                if (diff > 0) {
                                    return 1;
                                } else if(diff<0){
                                    return -1;
                                }else{
                                    return 0;
                                }

                            }
                        });

                        for (PassengerRoute tmPassengerRoute : PassengerRoutes1) {//对一组乘客的多条线路分配客流
                            if (((float)tmPassengerRoute.getPassengerTravelTime())/60*BaseInfo.getCp() < group.getUnservicedPunish()) {//只有travel——time小于惩罚成本时才会选择该线路
                                int tmPassengerNum1 = tmPassengerRoute.assignPassenger(tmPassengerNum, tmBRTCommonRouteBijMax, tmBusLine);

                                totalTraveltime += (tmPassengerNum - tmPassengerNum1) * ((float)tmPassengerRoute.getPureTravelTime()/60);
                                traveledPassenger += tmPassengerNum - tmPassengerNum1;
                                transferNumber += (tmPassengerNum - tmPassengerNum1) * tmPassengerRoute.getTrsNum();

                                tmCost += (tmPassengerNum - tmPassengerNum1) * ((float)tmPassengerRoute.getPassengerTravelTime()/60) * BaseInfo.getCp();
                                tmPassengerNum = tmPassengerNum1;
                                if (tmPassengerNum == 0) {
                                    break;
                                }
                            }
                        }

                        if (tmPassengerNum == 0){
                            PassengerRoutes1 = null;
                            continue;
                        }

                    }
                    PassengerRoutes1 = null;

                    List<PassengerRoute> PassengerRoutes2 = group.findRoute1TsOR1Lw(tmBusLine);//一次长步行或者换乘可以直达
                    if (PassengerRoutes2 != null) {
                        for (PassengerRoute passengerRoute : PassengerRoutes2) {
                            passengerRoute.setPassengerCostDivideBusCost(tmBusLine);
                        }
                        for (PassengerRoute passengerRoute : PassengerRoutes2) {
                            passengerRoute.setPassengerCostDivideBusCost(tmBusLine);
                        }
                        PassengerRoutes2.sort(new Comparator<PassengerRoute>() {//按照乘客成本/公交成本排序，依次分配客流
                            @Override
                            public int compare(PassengerRoute p1, PassengerRoute p2) {
                                float diff = p1.getPassengerCostDivideBusCost() - p2.getPassengerCostDivideBusCost();
                                if (diff > 0) {
                                    return 1;
                                } else if(diff<0){
                                    return -1;
                                }else{
                                    return 0;
                                }

                            }
                        });

                        for (PassengerRoute tmPassengerRoute : PassengerRoutes2) {//对一组乘客的多条线路分配客流
                            if (((float)tmPassengerRoute.getPassengerTravelTime())/60*BaseInfo.getCp() < group.getUnservicedPunish()) {//只有travel——time小于惩罚成本时才会选择该线路
                                int tmPassengerNum1 = tmPassengerRoute.assignPassenger(tmPassengerNum, tmBRTCommonRouteBijMax, tmBusLine);

                                totalTraveltime += (tmPassengerNum - tmPassengerNum1) * ((float)tmPassengerRoute.getPureTravelTime()/60);
                                traveledPassenger += tmPassengerNum - tmPassengerNum1;
                                transferNumber += (tmPassengerNum - tmPassengerNum1) * tmPassengerRoute.getTrsNum();

                                tmCost += (tmPassengerNum - tmPassengerNum1) * ((float)tmPassengerRoute.getPassengerTravelTime()/60) * BaseInfo.getCp();
                                tmPassengerNum = tmPassengerNum1;
                                if (tmPassengerNum == 0) {
                                    break;
                                }
                            }
                        }

                        if (tmPassengerNum == 0){
                            PassengerRoutes2 = null;
                            continue;
                        }

                    }
                    PassengerRoutes2 = null;


                    List<PassengerRoute> PassengerRoutes3 = group.findRoute2TsOR1Lw1TsOR2Lw(tmBusLine);//一次长步行或者换乘可以直达
                    if (PassengerRoutes3 != null) {
                        for (PassengerRoute passengerRoute : PassengerRoutes3) {
                            passengerRoute.setPassengerCostDivideBusCost(tmBusLine);
                        }
                        for (PassengerRoute passengerRoute : PassengerRoutes3) {
                            passengerRoute.setPassengerCostDivideBusCost(tmBusLine);
                        }
                        PassengerRoutes3.sort(new Comparator<PassengerRoute>() {//按照乘客成本/公交成本排序，依次分配客流
                            @Override
                            public int compare(PassengerRoute p1, PassengerRoute p2) {
                                float diff = p1.getPassengerCostDivideBusCost() - p2.getPassengerCostDivideBusCost();
                                if (diff > 0) {
                                    return 1;
                                } else if(diff<0){
                                    return -1;
                                }else{
                                    return 0;
                                }

                            }
                        });

                        for (PassengerRoute tmPassengerRoute : PassengerRoutes3) {//对一组乘客的多条线路分配客流
                            if (((float)tmPassengerRoute.getPassengerTravelTime())/60*BaseInfo.getCp() < group.getUnservicedPunish()) {//只有travel——time小于惩罚成本时才会选择该线路
                                int tmPassengerNum1 = tmPassengerRoute.assignPassenger(tmPassengerNum, tmBRTCommonRouteBijMax, tmBusLine);

                                totalTraveltime += (tmPassengerNum - tmPassengerNum1) * ((float)tmPassengerRoute.getPureTravelTime()/60);
                                traveledPassenger += tmPassengerNum - tmPassengerNum1;
                                transferNumber += (tmPassengerNum - tmPassengerNum1) * tmPassengerRoute.getTrsNum();

                                tmCost += (tmPassengerNum - tmPassengerNum1) * ((float)tmPassengerRoute.getPassengerTravelTime()/60) * BaseInfo.getCp();
                                tmPassengerNum = tmPassengerNum1;
                                if (tmPassengerNum == 0) {
                                    break;
                                }
                            }
                        }

                        if (tmPassengerNum != 0){
                            unSerPassenger.get(startIndex+i).add(group.getPassengerGroupID());
                            tmPassengerNotAss += tmPassengerNum;

                            unservedPassenger += tmPassengerNum;

                            tmCost += tmPassengerNum * group.getUnservicedPunish();
                        }

                    }else{
                        unSerPassenger.get(startIndex+i).add(group.getPassengerGroupID());
                        tmPassengerNotAss += tmPassengerNum;

                        unservedPassenger += tmPassengerNum;

                        tmCost += tmPassengerNum * group.getUnservicedPunish();
                    }
                    PassengerRoutes3 = null;

                }

                //临沂专用*************************************************************************************
                double tmPassengerCost = tmCost;
                //临沂专用*************************************************************************************

                PassengerNotAsignRes = tmPassengerNotAss;

                List<Double> tmSolutionPartSufficiencyPt = new ArrayList<>();
                List<Float> tmSolutionFrqs = new ArrayList<>();
                for (int t = 0; t < tmBusLine.size(); t++) {
                    float tmCost1 = tmBusLine.get(t).getFreq() * tmBusLine.get(t).getTravelTime();
                    tmCost1 = tmCost1/60;

                    if (tmBusLine.get(t).getLineType() == 1) {

                        operatingCost += tmCost1;
                        routeNumber ++;

                        tmSolutionFrqs.add(tmBusLine.get(t).getFreq());
                        if (tmCost1 > 0) {
                            tmCost += 1000 + tmCost1 * BaseInfo.getCb();
                        } else {
                            tmCost += 3000;
                        }

                    }
                }

                //临沂专用*************************************************************************************
                double tmOperaCost = tmCost-tmPassengerCost;
                traveledPassenger += 2000;
                //临沂专用*************************************************************************************

                //要用最大的cost减去这个cost
                double tmSufficiency = tmMaxCost + 3000 * BaseInfo.getMaxLineNum() - tmCost + 100;

                for (int t = BaseInfo.getBRTNum(); t < tmBusLine.size(); t++) {
                    float tmFreq = tmBusLine.get(t).getFreq();

                    if (GA.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum()) == 0) {
                        if (tmFreq > 0) {
                            tmSolutionPartSufficiencyPt.add(tmSufficiency);
                        } else {
                            tmSolutionPartSufficiencyPt.add(1.2 * tmSufficiency);
                        }
                    } else {
                        if (tmFreq > 0) {
                            tmSolutionPartSufficiencyPt.add(GA.b * (GA.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum())) + (1 - GA.b) * tmSufficiency);//累积片段适应度
                        } else {//此线路没有提供贡献，要给惩罚
                            tmSolutionPartSufficiencyPt.add(1.2 * (GA.b * (GA.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum())) + (1 - GA.b) * tmSufficiency));//累积片段适应度
                        }
                    }
                }
                tmSolutionFrq.set(startIndex+i,tmSolutionFrqs);
                tmSolutionPartSufficiency.set(startIndex+i,tmSolutionPartSufficiencyPt);
                tmSolutionSufficiency.set(startIndex+i,tmSufficiency);

                //临沂专用
//                List<Integer> tmBRTLoad = new ArrayList<>();//BRT上客流
//                List<Integer> otherLoad = new ArrayList<>();//其他线路上客流
//                List<Integer> allLoad = new ArrayList<>();//汇总客流
//                List<Integer> routeLgh = new ArrayList<>();//线路长度
//                List<Float> sumFreq = new ArrayList<>();
//
//                for(int tmss = 0;tmss<17;tmss++){
//                    int passenger = tmBusLine.get(0).passengerLoad[tmss][tmss+1];
//                    tmBRTLoad.add(passenger);
//
//                    sumFreq.add(60 - tmBRTCommonRouteBijMax[tmss]);
//                    float otherPassenger = BaseInfo.getBusCapacity()*(45 - tmBRTCommonRouteBijMax[tmss]);
//                    otherLoad.add((int)otherPassenger);
//                    allLoad.add((int)otherPassenger+passenger);
//                }
//
//                for(int routS = 1;routS<tmBusLine.size();routS ++){
//                    routeLgh.add(tmBusLine.get(routS).getTravelTime());
//                }
                //临沂专用171064

                if (GA.BestCost < tmSufficiency) {
                    GA.BestCost = tmSufficiency;
                    GA.BestBusLine = tmBusLine;
                    GA.BestSolution = ints;
                    GA.setPassengerCost(tmPassengerCost);
                    GA.setOperatorCost(tmOperaCost);
                    GA.bestSoluBRTLaneFeq = tmBRTCommonRouteBijMax;
                    GA.PassengerNotAsignRes = PassengerNotAsignRes;

                    GA.totalTraveltime = totalTraveltime;
                    GA.traveledPassenger = traveledPassenger;
                    GA.unservedPassenger = unservedPassenger;
                    GA.transferNumber = transferNumber;
                    GA.routeNumber = routeNumber;
                    GA.operatingCost = operatingCost;

                }

                tmBusLine = null;

                //这里要更新解自己的适应度与每个片段的适应度（Cost越大适应性越差）可以取-tmCost
                tmBRTCommonRouteBijMax = null;

                long t2=System.currentTimeMillis();
//                System.out.println("运行时间:"+(t2-t1));
//                System.out.println("passegnerCost:"+tmPassengerCost);
            }

            finFlag = true;

        }catch (Exception e) {
            System.out.println("Thread " +  threadID + " interrupted.");
        }
        System.out.println("Thread " +  threadID + " exiting.");
    }


    public void run2() {//对照组
        System.out.println("Running " +  threadID );
        try {


            double tmMaxCost = 0;
            for (PassengerGroup group : passengerGroups) {
                tmMaxCost += group.getPassengerNum() * group.getUnservicedPunish();
            }


            for (int i = 0; i < solutionList.size(); i++) {//遍历所有的解

                long t1=System.currentTimeMillis();

                int PassengerNotAsignRes = 0;
                List<int[]> ints = solutionList.get(i);
                List<BusLine> tmBusLine = deCode(ints);

                double tmCost = 0;

                float[] BRTCommonRouteBijMax = BaseInfo.getBRTCommonRouteBijMax();
                float[] tmBRTCommonRouteBijMax = new float[BaseInfo.getBRTCommonNum()];
                int[][] BRTCommonRoute = BaseInfo.getBRTCommonRoute();


                System.arraycopy(BRTCommonRouteBijMax, 0, tmBRTCommonRouteBijMax, 0, BaseInfo.getBRTCommonNum());

                for (int ib = 0; ib < BaseInfo.getBRTNum(); ib++) {//去掉现有的BRT发车频率
                    float tmFrq = tmBusLine.get(ib).getFreq();
                    List<Integer> tmPointList = tmBusLine.get(ib).getPointList();
                    for (int jb = 1; jb < tmPointList.size(); jb++) {
                        tmBRTCommonRouteBijMax[BRTCommonRoute[tmPointList.get(jb - 1)][tmPointList.get(jb)] - 1] -= tmFrq;
                    }
                }

                int tmPassengerNotAss = 0;

                for (PassengerGroup group : passengerGroups) {//此处的passengerGroup已经按照惩罚成本排序
                    //List<PassengerRoute> tmPassengerRoutes = new ArrayList<>();
                    //PassengerGroupRoutes.clear();
                    if(group.getStartSWPoints().contains(13) || group.getEndSWPoints().contains(13)){
                        int sss = 0;
                    }
                    int tmPassengerNum = group.getPassengerNum();

                    List<PassengerRoute> PassengerRoutes = new ArrayList<>();
                    //生成所有乘客路线
                    List<PassengerRoute> PassengerRoutes1 = group.findRoute0Ts0Lw(tmBusLine);//
                    List<PassengerRoute> PassengerRoutes2 = group.findRoute1TsOR1Lw(tmBusLine);//
                    List<PassengerRoute> PassengerRoutes3 = group.findRoute2TsOR1Lw1TsOR2Lw(tmBusLine);//

                    if(PassengerRoutes1!=null){
                        PassengerRoutes.addAll(PassengerRoutes1);
                    }
                    if(PassengerRoutes2!=null){
                        PassengerRoutes.addAll(PassengerRoutes2);
                    }
                    if(PassengerRoutes3!=null){
                        PassengerRoutes.addAll(PassengerRoutes3);
                    }



                    PassengerRoutes1 = null;
                    PassengerRoutes2 = null;
                    PassengerRoutes3 = null;


                    if (PassengerRoutes.size() == 0) {
                        unSerPassenger.get(startIndex+i).add(group.getPassengerGroupID());
                        tmPassengerNotAss += tmPassengerNum;
                        tmCost += tmPassengerNum * group.getUnservicedPunish();
                        PassengerRoutes = null;
                        continue;
                    }

                    //按照路线的成本排序
                    PassengerRoutes.sort(new Comparator<PassengerRoute>() {//按照乘客成本/公交成本排序，依次分配客流
                        @Override
                        public int compare(PassengerRoute p1, PassengerRoute p2) {
                            float diff = p1.getPassengerTravelTime()*BaseInfo.getCb() - p2.getPassengerTravelTime()*BaseInfo.getCb();
                            if (diff > 0) {
                                return 1;
                            } else if(diff<0){
                                return -1;
                            }else{
                                return 0;
                            }

                        }
                    });


                    //分配客流
                    for (PassengerRoute tmPassengerRoute : PassengerRoutes) {//对一组乘客的多条线路分配客流
                        if (((float)tmPassengerRoute.getPassengerTravelTime())/60*BaseInfo.getCp() < group.getUnservicedPunish()) {//只有travel——time小于惩罚成本时才会选择该线路
                            int tmPassengerNum1 = tmPassengerRoute.assignPassenger(tmPassengerNum, tmBRTCommonRouteBijMax, tmBusLine);

                            tmCost += (tmPassengerNum - tmPassengerNum1) * ((float)tmPassengerRoute.getPassengerTravelTime()/60) * BaseInfo.getCp();
                            tmPassengerNum = tmPassengerNum1;
                            if (tmPassengerNum == 0) {
                                break;
                            }
                        }
                    }

                    if (tmPassengerNum != 0){
                        unSerPassenger.get(startIndex+i).add(group.getPassengerGroupID());
                        tmPassengerNotAss += tmPassengerNum;
                        tmCost += tmPassengerNum * group.getUnservicedPunish();
                    }
                    PassengerRoutes = null;

                }

                //临沂专用*************************************************************************************
                double tmPassengerCost = tmCost;
                //临沂专用*************************************************************************************

                PassengerNotAsignRes = tmPassengerNotAss;

                List<Double> tmSolutionPartSufficiencyPt = new ArrayList<>();
                List<Float> tmSolutionFrqs = new ArrayList<>();
                for (int t = 0; t < tmBusLine.size(); t++) {
                    float tmCost1 = tmBusLine.get(t).getFreq() * tmBusLine.get(t).getTravelTime();
                    tmCost1 = tmCost1/60;
                    if (tmBusLine.get(t).getLineType() == 1) {
                        tmSolutionFrqs.add(tmBusLine.get(t).getFreq());
                        if (tmCost1 > 0) {
                            tmCost += 1000 + tmCost1 * BaseInfo.getCb();
                        } else {
                            tmCost += 3000;
                        }

                    }
                }

                //临沂专用*************************************************************************************
                double tmOperaCost = tmCost-tmPassengerCost;
                //临沂专用*************************************************************************************

                //要用最大的cost减去这个cost
                double tmSufficiency = tmMaxCost + 3000 * BaseInfo.getMaxLineNum() - tmCost + 100;

                for (int t = BaseInfo.getBRTNum(); t < tmBusLine.size(); t++) {
                    float tmFreq = tmBusLine.get(t).getFreq();

                    if (GA.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum()) == 0) {
                        if (tmFreq > 0) {
                            tmSolutionPartSufficiencyPt.add(tmSufficiency);
                        } else {
                            tmSolutionPartSufficiencyPt.add(1.2 * tmSufficiency);
                        }
                    } else {
                        if (tmFreq > 0) {
                            tmSolutionPartSufficiencyPt.add(GA.b * (GA.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum())) + (1 - GA.b) * tmSufficiency);//累积片段适应度
                        } else {//此线路没有提供贡献，要给惩罚
                            tmSolutionPartSufficiencyPt.add(1.2 * (GA.b * (GA.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum())) + (1 - GA.b) * tmSufficiency));//累积片段适应度
                        }
                    }
                }
                tmSolutionFrq.set(startIndex+i,tmSolutionFrqs);
                tmSolutionPartSufficiency.set(startIndex+i,tmSolutionPartSufficiencyPt);
                tmSolutionSufficiency.set(startIndex+i,tmSufficiency);

                //临沂专用
//                List<Integer> tmBRTLoad = new ArrayList<>();//BRT上客流
//                List<Integer> otherLoad = new ArrayList<>();//其他线路上客流
//                List<Integer> allLoad = new ArrayList<>();//汇总客流
//                List<Integer> routeLgh = new ArrayList<>();//线路长度
//                List<Float> sumFreq = new ArrayList<>();
//
//                for(int tmss = 0;tmss<17;tmss++){
//                    int passenger = tmBusLine.get(0).passengerLoad[tmss][tmss+1];
//                    tmBRTLoad.add(passenger);
//
//                    sumFreq.add(60 - tmBRTCommonRouteBijMax[tmss]);
//                    float otherPassenger = BaseInfo.getBusCapacity()*(45 - tmBRTCommonRouteBijMax[tmss]);
//                    otherLoad.add((int)otherPassenger);
//                    allLoad.add((int)otherPassenger+passenger);
//                }
//
//                for(int routS = 1;routS<tmBusLine.size();routS ++){
//                    routeLgh.add(tmBusLine.get(routS).getTravelTime());
//                }
                //临沂专用171064

                if (GA.BestCost < tmSufficiency) {
                    GA.BestCost = tmSufficiency;
                    GA.BestBusLine = tmBusLine;
                    GA.BestSolution = ints;
                    GA.PassengerNotAsignRes = PassengerNotAsignRes;
                }

                tmBusLine = null;

                //这里要更新解自己的适应度与每个片段的适应度（Cost越大适应性越差）可以取-tmCost
                tmBRTCommonRouteBijMax = null;

                long t2=System.currentTimeMillis();
//                System.out.println("运行时间:"+(t2-t1));
//                System.out.println("passegnerCost:"+tmPassengerCost);
            }

            finFlag = true;

        }catch (Exception e) {
            System.out.println("Thread " +  threadID + " interrupted.");
        }
        System.out.println("Thread " +  threadID + " exiting.");
    }


    public List<BusLine> deCode(List<int[]> solution) {
        List<BusLine> tmBusLineList = new ArrayList<>(BaseInfo.getBRTLine());//不能等于

        for (int i = 0; i < solution.size(); i++) {
            int[] pointsPrior = solution.get(i);//站点优先级
            List<Integer> pointList = new ArrayList<>();

            //处理开始节点
            int[][] tmBusRouteMat = MatOperate.ZeroMat(pointNum, pointNum);

            int lineStartPoint = 0;
            int tmPrior = 999;//优先级
            for (int p = 0; p < pointNum; p++) {
                if (pointsPrior[p] < tmPrior) {
                    tmPrior = pointsPrior[p];
                    lineStartPoint = p;
                }
            }
            pointList.add(lineStartPoint);

            int lineNextPoint = lineStartPoint;

            int tmNextPoint = 0;


            while (lineNextPoint != pointNum) {
                List<Integer> tmPointsReachability = pointReachabilitySet.get(lineNextPoint);//当前节点可达节点集合（包含结束点）

                int tmNextPrior = 999;
                for (int j = 0; j < tmPointsReachability.size(); j++) {
                    if (pointList.size() >= pointsPrior[pointNum]) {
                        tmNextPoint = pointNum;
                        //tmNextPrior = pointsPrior[tmPointsReachability.size()-1];
                        break;
                    } else {
                        if (pointsPrior[tmPointsReachability.get(j)] < tmNextPrior && !pointList.contains(tmPointsReachability.get(j))) {
                            tmNextPoint = tmPointsReachability.get(j);
                            tmNextPrior = pointsPrior[tmNextPoint];
                        }
                    }
                }

                if (tmNextPoint != pointNum && tmNextPoint == 999) {//没路了，想办法如何调整，让他可行,可以在生成的时候判断,交换第一与第一可达的两个点
                    tmNextPoint = pointNum;
                }

                if (tmNextPoint != pointNum) {

                    tmBusRouteMat[lineNextPoint][tmNextPoint] = 1;
                    tmBusRouteMat[tmNextPoint][lineNextPoint] = 1;

                    lineNextPoint = tmNextPoint;
                    pointList.add(tmNextPoint);
                } else {
                    lineNextPoint = tmNextPoint;
                }
            }
            BusLine tmBusLine = new BusLine(pointNum, pointList.get(0), pointList.get(pointList.size() - 1), tmBusRouteMat, BaseInfo.getBRTNum() + 1 + i, 1);
            tmBusLineList.add(tmBusLine);

            pointList = null;

        }
        return tmBusLineList;
    }


    public void start () {
        if (t == null) {
            t = new Thread (this, threadID+"");
            t.start ();
        }
    }
}
