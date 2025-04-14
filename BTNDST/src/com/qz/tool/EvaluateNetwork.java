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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvaluateNetwork {

    private  Chromosome solution;

    public EvaluateNetwork(Chromosome solution) {
        this.solution = solution;
    }

    public void evaluate() {//实验组
        try {

            List<BRTLine> brtLines = BRTLine.brtLines;
            List<RailLine> railLines = RailLine.railLines;

            List<BusLineNew> busLines = solution.deCode();
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

            double tmSufficiency = -busTravelTime*IniData.busTravelTimeCoefficient-passengerTravelTime*IniData.passengerTravelTimeCoefficient
                    +syTimes*IniData.syCoefficient-unServedPassengers*IniData.unServedPassengerCoefficient;

            List<Passenger> unSPassengers = new ArrayList<>();
            for(Passenger p:ts.finishPassengers){
                if(p.unServiedPassenger){
                    unSPassengers.add(p);
                }
            }

            solution.busTravelTime = busTravelTime;
            solution.passengerTravelTime = passengerTravelTime;
            solution.syTimes = syTimes;
            solution.unServedPassengers = unServedPassengers;

            System.out.println("tmSufficiency：" +  tmSufficiency +
                    "busTravelTime：" +  busTravelTime +
                    "     passengerTravelTime：" +  passengerTravelTime+
                    "     syTimes：" +  syTimes+
                    "     unServedPassengers：" +  unServedPassengers+
                    "     unPathPassenger：" +  unPathPassenger);
            PBGA.PassengerNotAsignRes = unServedPassengers;

        }catch (Exception e) {
            System.out.println("interrupted.");
        }
    }

}
