package com.qz.tool;

import com.qz.PBGA.Chromosome;
import com.qz.PBGA.PBGA;
import com.qz.baseClass.Line.BRTLine;
import com.qz.baseClass.Line.BaseLine;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Line.RailLine;
import com.qz.baseClass.Passenger.DynamicAssignment;
import com.qz.baseClass.Passenger.OD;
import com.qz.baseClass.Passenger.Passenger;
import com.qz.baseClass.Passenger.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvaluateRunnableNetworkFrequency implements Runnable {
    public boolean finFlag = false;
    private Thread t;
    private int threadID;
    private  List<Chromosome> solutionList;
    private List<Double> tmSolutionSufficiency;
    private List<List<Double>> tmSolutionPartSufficiency;
    private List<List<Float>> tmSolutionFrq;
    private List<List<Passenger>> unSerPassenger;
    private int startIndex;

    public EvaluateRunnableNetworkFrequency(int ThreadID, int StartIndex, List<Chromosome> SolutionList, List<Double> TmSolutionSufficiency,
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

                List<BRTLine> brtLines = new ArrayList<>();
                for(BRTLine line:BRTLine.brtLines){
                    BRTLine newLines = new BRTLine(BaseLine.tmID,line.busNode,line.timeTable,false);
                    BaseLine.tmID++;
                    brtLines.add(newLines);
                }

                List<RailLine> railLines = new ArrayList<>();
                for(RailLine line:RailLine.railLines){
                    RailLine newLines = new RailLine(BaseLine.tmID,line.busNode,line.timeTable,false);
                    BaseLine.tmID++;
                    railLines.add(newLines);
                }

                List<BusLineNew> busLines = solutionList.get(i).deCode();

                List<BaseLine> baseLines = new ArrayList<>();
                baseLines.addAll(brtLines);
                baseLines.addAll(railLines);
                baseLines.addAll(busLines);

                int allUnServedPassenger = 0;

                double passengerTravelTime =0;
                List<Passenger> unSP = new ArrayList<>();
                int allP = 0;
                /// 最短路分配
                for(OD od:OD.ods){
                    Path p = od.getShortestPath(brtLines,railLines,busLines);
                    int passengerNum = od.maxPassenger;
                    allP += passengerNum;
                    int unServedPassenger = passengerNum;
                    if(p!=null){
                        unServedPassenger = p.assignPassenger(passengerNum,baseLines);
                        passengerTravelTime += p.getPathTravelTime()*(passengerNum-unServedPassenger);
                        if(unServedPassenger>0){
                            Passenger passenger = new Passenger(Passenger.tmId,od.start,od.end,0);
                            unSP.add(passenger);
                            allUnServedPassenger += unServedPassenger;
                        }
                    }else{
                        Passenger passenger = new Passenger(Passenger.tmId,od.start,od.end,0);
                        unSP.add(passenger);
                        allUnServedPassenger += unServedPassenger;
                    }
                }

                /// 计算发车间隔
                for(BusLineNew bs:busLines){
                    bs.calFrequency();
                }


                //计算成本函数
                //公交出行时间
                double busTravelTime = 0;
                for(BusLineNew busLine:busLines){
                    busTravelTime+=busLine.getTravelTime();
                }
                //乘客旅行时间
                // passengerTravelTime;
                //获取未服务乘客
                // allUnServedPassenger;

                double tmSufficiency = -busTravelTime*IniData.busTravelTimeCoefficient-passengerTravelTime*IniData.passengerTravelTimeCoefficient
                        -allUnServedPassenger*IniData.unServedPassengerCoefficient;

                List<Double> tmSolutionPartSufficiencyPt = new ArrayList<>();
                List<Float> tmSolutionFrqs = new ArrayList<>();
                for (int t = 0; t < busLines.size(); t++) {
                    BusLineNew busLine = busLines.get(t);
                    float tmFreq = (float) busLine.frequency;
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

                unSerPassenger.add(unSP);
                tmSolutionFrq.set(startIndex+i,tmSolutionFrqs);
                tmSolutionPartSufficiency.set(startIndex+i,tmSolutionPartSufficiencyPt);
                tmSolutionSufficiency.set(startIndex+i,tmSufficiency);

                if (PBGA.BestCost < tmSufficiency) {
                    PBGA.BestCost = tmSufficiency;
                    PBGA.BestBusLine = busLines;
                    PBGA.BestSolution = solutionList.get(i);
                    solutionList.get(i).busTravelTime = (int)busTravelTime;
                    solutionList.get(i).passengerTravelTime = passengerTravelTime;
                    solutionList.get(i).syTimes = 0;
                    solutionList.get(i).unServedPassengers = allUnServedPassenger;
                    System.out.println("busTravelTime：" +  busTravelTime +
                            "     passengerTravelTime：" +  passengerTravelTime+
                            "     syTimes：" +  0+
                            "     unServedPassengers：" +  allUnServedPassenger+
                            "     unPathPassenger：" +  0);
                    PBGA.PassengerNotAsignRes = allUnServedPassenger;
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
