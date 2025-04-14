package com.qz.tool;

import com.qz.baseClass.BusLine;
import com.qz.baseClass.PassengerGroup;
import com.qz.baseClass.PassengerRoute;
import com.qz.network.GA2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EvaluateRunnable2 implements Runnable {
    public boolean finFlag = false;
    private Thread t;
    private int threadID;
    private  List<List<Integer>> solutionList;
    private List<Double> tmSolutionSufficiency;
    private List<List<Double>> tmSolutionPartSufficiency;
    private List<List<Float>> tmSolutionFrq;
    private int startIndex;
    private List<PassengerGroup> passengerGroups;
    private int pointNum;

    public EvaluateRunnable2(int ThreadID, int StartIndex, int PointNum, List<PassengerGroup> PassengerGroups,
                             List<List<Integer>> SolutionList, List<Double> TmSolutionSufficiency,
                             List<List<Double>> TmSolutionPartSufficiency, List<List<Float>> TmSolutionFrq) {
        solutionList = SolutionList;
        threadID = ThreadID;
        tmSolutionSufficiency = TmSolutionSufficiency;
        tmSolutionPartSufficiency = TmSolutionPartSufficiency;
        tmSolutionFrq = TmSolutionFrq;
        startIndex = StartIndex;
        passengerGroups = PassengerGroups;
        pointNum = PointNum;
    }

    public List<BusLine> deCode(List<Integer> solution) {//解码
        List<BusLine> tmBusLineList = new ArrayList<>(BaseInfo.getBRTLine());//不能等于
        for (int i = 0; i < solution.size(); i++) {
            List<Integer> pointList = BaseInfo.busLineSet.get(solution.get(i));

            int[][] tmBusRouteMat = MatOperate.ZeroMat(pointNum, pointNum);

            for(int j=1;j<pointList.size();j++){
                tmBusRouteMat[pointList.get(j-1)][pointList.get(j)] = 1;
                tmBusRouteMat[pointList.get(j)][pointList.get(j-1)] = 1;
            }

            BusLine tmBusLine = new BusLine(pointNum, pointList.get(0), pointList.get(pointList.size() - 1), tmBusRouteMat, BaseInfo.getBRTNum() + 1 + i, 1);
            tmBusLineList.add(tmBusLine);
        }
        return tmBusLineList;
    }

    public void run() {
        System.out.println("Running " +  threadID );
        try {


            double tmMaxCost = 0;
            for (PassengerGroup group : passengerGroups) {
                tmMaxCost += group.getPassengerNum() * group.getUnservicedPunish();
            }


            for (int i = 0; i < solutionList.size(); i++) {//遍历所有的解

                int PassengerNotAsignRes = 0;
                List<Integer> ints = solutionList.get(i);
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
                                tmCost += (tmPassengerNum - tmPassengerNum1) * ((float)tmPassengerRoute.getPassengerTravelTime()/60) * BaseInfo.getCp();
                                tmPassengerNum = tmPassengerNum1;
                                if (tmPassengerNum == 0) {
                                    break;
                                }
                            }
                        }

                        if (tmPassengerNum != 0){
                            tmPassengerNotAss += tmPassengerNum;
                            tmCost += tmPassengerNum * group.getUnservicedPunish();
                        }

                    }else{
                        tmPassengerNotAss += tmPassengerNum;
                        tmCost += tmPassengerNum * group.getUnservicedPunish();
                    }
                    PassengerRoutes3 = null;

                }

                PassengerNotAsignRes = tmPassengerNotAss;

//                List<Double> tmSolutionPartSufficiencyPt = new ArrayList<>();
                List<Float> tmSolutionFrqs = new ArrayList<>();
                for (int t = 0; t < tmBusLine.size(); t++) {
                    float tmCost1 = tmBusLine.get(t).getFreq() * tmBusLine.get(t).getTravelTime()*2;
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

                //要用最大的cost减去这个cost
                double tmSufficiency = tmMaxCost + 3000 * BaseInfo.getMaxLineNum() - tmCost + 100;

//                for (int t = BaseInfo.getBRTNum(); t < tmBusLine.size(); t++) {
//                    float tmFreq = tmBusLine.get(t).getFreq();
//
//                    if (GA2.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum()) == 0) {
//                        if (tmFreq > 0) {
//                            tmSolutionPartSufficiencyPt.add(tmSufficiency);
//                        } else {
//                            tmSolutionPartSufficiencyPt.add(1.2 * tmSufficiency);
//                        }
//                    } else {
//                        if (tmFreq > 0) {
//                            tmSolutionPartSufficiencyPt.add(GA2.b * (GA2.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum())) + (1 - GA2.b) * tmSufficiency);//累积片段适应度
//                        } else {//此线路没有提供贡献，要给惩罚
//                            tmSolutionPartSufficiencyPt.add(1.2 * (GA2.b * (GA2.solutionPartSufficiency.get(startIndex+i).get(t - BaseInfo.getBRTNum())) + (1 - GA2.b) * tmSufficiency));//累积片段适应度
//                        }
//                    }
//                }
                tmSolutionFrq.set(startIndex+i,tmSolutionFrqs);
//                tmSolutionPartSufficiency.set(startIndex+i,tmSolutionPartSufficiencyPt);
                tmSolutionSufficiency.set(startIndex+i,tmSufficiency);

                if (GA2.BestCost < tmSufficiency) {
                    GA2.BestCost = tmSufficiency;
                    GA2.BestBusLine = tmBusLine;
                    GA2.BestSolution = ints;
                    GA2.PassengerNotAsignRes = PassengerNotAsignRes;
                }

                tmBusLine = null;

                //这里要更新解自己的适应度与每个片段的适应度（Cost越大适应性越差）可以取-tmCost
                tmBRTCommonRouteBijMax = null;
            }

            finFlag = true;

        }catch (Exception e) {
            System.out.println("Thread " +  threadID + " interrupted.");
        }
        System.out.println("Thread " +  threadID + " exiting.");
    }




    public void start () {
        if (t == null) {
            t = new Thread (this, threadID+"");
            t.start ();
        }
    }
}
