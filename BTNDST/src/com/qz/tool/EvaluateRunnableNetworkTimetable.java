package com.qz.tool;

import com.qz.PBGA.Chromosome;
import com.qz.PBGA.PBGA;
import com.qz.baseClass.Line.BRTLine;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Line.RailLine;
import com.qz.baseClass.Passenger.DynamicAssignment;
import com.qz.baseClass.Passenger.OD;
import com.qz.baseClass.Passenger.Passenger;
import com.qz.baseClass.Passenger.Path;

import java.util.*;
import java.util.stream.Collectors;

public class EvaluateRunnableNetworkTimetable implements Runnable {
    public boolean finFlag = false;
    private Thread t;
    private int threadID;
    private  List<Chromosome> solutionList;
    private List<Double> tmSolutionSufficiency;
    private List<List<Double>> tmSolutionPartSufficiency;
    private List<List<Float>> tmSolutionFrq;
    private List<List<Passenger>> unSerPassenger;
    private int startIndex;

    public EvaluateRunnableNetworkTimetable(int ThreadID, int StartIndex, List<Chromosome> SolutionList, List<Double> TmSolutionSufficiency,
                                            List<List<Double>> TmSolutionPartSufficiency, List<List<Float>> TmSolutionFrq, List<List<Passenger>> unSerPassenger) {
        solutionList = SolutionList;
        threadID = ThreadID;
        tmSolutionSufficiency = TmSolutionSufficiency;
        tmSolutionPartSufficiency = TmSolutionPartSufficiency;
        tmSolutionFrq = TmSolutionFrq;
        startIndex = StartIndex;
        this.unSerPassenger = unSerPassenger;
    }

    public void run() {//实验组
        System.out.println("Running " +  threadID );
        try {

            for (int i = 0; i < solutionList.size(); i++) {//遍历所有的解

                List<BRTLine> brtLines = BRTLine.brtLines;
                List<RailLine> railLines = RailLine.railLines;

                List<BusLineNew> busLines = solutionList.get(i).deCode();
                for (BusLineNew busLine : busLines) {
                    busLine.setSynchronizeTimeWindow(railLines,brtLines);
                    busLine.setSynchronizeTimePoint();
                    busLine.setTimeMap();
                }

                List<OD> ods = new ArrayList<>();
                for(OD od:OD.ods){
                    Map<Integer,List<Passenger>> passengers = new HashMap<>();
                    od.passengers.forEach((key, value) -> {
                        // 处理key和value
                        List<Passenger> tmPs = new ArrayList<>();
                        for(Passenger p:value){
                            Passenger tmp = new Passenger(Passenger.tmId,p.startNode,p.endNode,p.startTime);
                            Passenger.tmId++;
                            tmPs.add(tmp);
                        }
                        passengers.put(key,tmPs);
                    });
                    OD odNew = new OD(OD.tmId,od.start,od.end,passengers);
                    OD.tmId++;
                    odNew.getPath(brtLines,railLines,busLines);
                    ods.add(odNew);
                }

                //首次分配
                DynamicAssignment dta = new DynamicAssignment();
                for(OD od:ods){
                    dta.assignPassengerFirst(od);
                }

                TransitSimulation ts = new TransitSimulation(TransitSimulation.tmId,IniData.endTime,brtLines,railLines,busLines);
                TransitSimulation.tmId++;
                double rGap=1;
                int loopTime = 0;
                while(rGap>IniData.maxGap&&loopTime<5){
                    loopTime++;
                    ts.clean();
                    boolean finFlag = true;
                    //循环仿真分配
                    while(finFlag){
                        for(OD od:ods){
                            if(od.passengers.containsKey(ts.time)){
                                List<Path> paths = new ArrayList<>();
                                paths.addAll(od.paths.values().stream().collect(Collectors.toList()));
                                List<Path> delPaths = new ArrayList<>();

                                for(Passenger passenger:od.passengers.get(ts.time)){
                                    ts.addPassengerNode(passenger);
                                    if(!delPaths.contains(passenger.path)){
                                        delPaths.add(passenger.path);
                                    }
                                }

                                //添加虚拟乘客，测试线路出行时间
                                paths.removeAll(delPaths);
                                for(Path path:paths){
                                    Passenger dPassenger = new Passenger(Passenger.tmId,od.start,od.end,ts.time);
                                    Passenger.tmId++;
                                    dPassenger.OD = od.id;
                                    dPassenger.setPath(path);
                                    dPassenger.dummyPassenger = true;
                                    ts.addPassengerNode(dPassenger);
                                }



                            }
                        }
                        finFlag = ts.simulation();
                    }

                    ts.setPathTravelTime(ods);
                    rGap = dta.calRGap(ods,ts);

                    for(OD od:ods){
                        dta.assignPassenger(od,ts);
                    }
                    //计算均衡误差

                }

                //计算成本函数
                //公交出行时间
                int busTravelTime = ts.getBusTravelTime();
                //乘客旅行时间
                double passengerTravelTime = ts.totalTravelTime;
                //同步次数
                double syTimes = ts.getSyTimes();
                //获取未服务乘客
                int unServedPassengers = ts.getUnServedPassengers();

                int unPathPassenger = ts.getUnPathPassengers();

                int bunchingTimes = ts.getBunchingTimes();

                double tmSufficiency = -busTravelTime*IniData.busTravelTimeCoefficient-passengerTravelTime*IniData.passengerTravelTimeCoefficient
                        +syTimes*IniData.syCoefficient-unServedPassengers*IniData.unServedPassengerCoefficient;

                List<Double> tmSolutionPartSufficiencyPt = new ArrayList<>();
                List<Float> tmSolutionFrqs = new ArrayList<>();
                for (int t = 0; t < busLines.size(); t++) {
                    BusLineNew busLine = busLines.get(t);
                    float tmFreq = ts.dynamicSchedule.departingTimes.get(busLine.id).size();
                    tmSolutionFrqs.add(tmFreq);
                    if (PBGA.solutionPartSufficiency.get(startIndex+i).get(t) == 0) {
                        if (tmFreq > 0) {
                            tmSolutionPartSufficiencyPt.add(tmSufficiency);
                        } else {
                            tmSolutionPartSufficiencyPt.add(1.2 * tmSufficiency);
                        }
                    } else {
                        if (tmFreq > 0) {
                            tmSolutionPartSufficiencyPt.add(PBGA.b * (PBGA.solutionPartSufficiency.get(startIndex+i).get(t)) + (1 - PBGA.b) * tmSufficiency);//累积片段适应度
                        } else {//此线路没有提供贡献，要给惩罚
                            tmSolutionPartSufficiencyPt.add(1.2 * (PBGA.b * (PBGA.solutionPartSufficiency.get(startIndex+i).get(t)) + (1 - PBGA.b) * tmSufficiency));//累积片段适应度
                        }
                    }
                }

                List<Passenger> unSPassengers = new ArrayList<>();
                for(Passenger p:ts.finishPassengers){
                    if(p.unServiedPassenger){
                        unSPassengers.add(p);
                    }
                }
                unSerPassenger.add(unSPassengers);
                tmSolutionFrq.set(startIndex+i,tmSolutionFrqs);
                tmSolutionPartSufficiency.set(startIndex+i,tmSolutionPartSufficiencyPt);
                tmSolutionSufficiency.set(startIndex+i,tmSufficiency);

                if (PBGA.BestCost < tmSufficiency) {
                    PBGA.BestCost = tmSufficiency;
                    PBGA.BestBusLine = busLines;
                    PBGA.BestSolution = solutionList.get(i);
                    solutionList.get(i).busTravelTime = busTravelTime;
                    solutionList.get(i).passengerTravelTime = passengerTravelTime;
                    solutionList.get(i).syTimes = syTimes;
                    solutionList.get(i).unServedPassengers = unServedPassengers;
                    solutionList.get(i).bunchingTimes = bunchingTimes;
                    System.out.println("busTravelTime：" +  busTravelTime +
                            "     passengerTravelTime：" +  passengerTravelTime+
                            "     syTimes：" +  syTimes+
                            "     unServedPassengers：" +  unServedPassengers+
                            "     unPathPassenger：" +  unPathPassenger+
                            "     bunchingTimes：" +  bunchingTimes);
                    PBGA.PassengerNotAsignRes = unServedPassengers;
                }

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
