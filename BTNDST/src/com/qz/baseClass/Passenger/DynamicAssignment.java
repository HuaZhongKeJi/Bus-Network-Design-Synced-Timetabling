package com.qz.baseClass.Passenger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.qz.tool.IniData;
import com.qz.tool.TransitSimulation;

public class DynamicAssignment {

    double addRate = 0.2;

    public void assignPassenger(OD od, TransitSimulation ts){
        for(int time=0;time<od.passengers.size();time++){
            List<Passenger> passengers = od.passengers.get(time);
            if(passengers==null||passengers.isEmpty()){
                continue;
            }
            int passengerNum = passengers.size();
            if(!ts.modeMinTravelTimes.containsKey(time)||!ts.modeMinTravelTimes.get(time).containsKey(od.id)){
                continue;
            }
            Map<Integer,Integer> modeMinTravelTimes = ts.modeMinTravelTimes.get(time).get(od.id);

            double intraMode = 0;
            for(Map.Entry<Integer, Integer> entry : modeMinTravelTimes.entrySet()){
                intraMode += Math.exp(-0.5*entry.getValue());
            }

            //计算每种模式应当分配多少乘客
            Map<Integer,Integer> modePassengerNum = new HashMap<>();
            int assignmentedP = 0;
            int seq = 1;
            for(Map.Entry<Integer, Integer> entry : modeMinTravelTimes.entrySet()){
                double intraModeS = Math.exp(-0.5*entry.getValue());
                int tmPassengerNum = (int)Math.round(passengerNum*intraModeS/intraMode);

                if(seq==modeMinTravelTimes.size()){
                    modePassengerNum.put(entry.getKey(),passengerNum-assignmentedP);
                }else{
                    if(assignmentedP+tmPassengerNum>passengerNum){
                        modePassengerNum.put(entry.getKey(),passengerNum-assignmentedP);
                        assignmentedP = passengerNum;
                    }else{
                        modePassengerNum.put(entry.getKey(),tmPassengerNum);
                        assignmentedP += tmPassengerNum;
                    }
                }
                seq ++;
            }

            //计算每条线路应当分配多少乘客
            Map<Integer,Integer> shortestPath = ts.shortestPath.get(time).get(od.id);
            int tmPassenger = 0;
            for(Map.Entry<Integer, Integer> entry : modePassengerNum.entrySet()){
                int mode = entry.getKey();
                int minPathId = shortestPath.get(mode);
                int nowShortestPathPassenger = ts.pathPassengers.get(time).get(od.id).get(minPathId);
                int assP = (int)(nowShortestPathPassenger*(1+addRate))+1;
                if(assP>entry.getValue()){
                    assP = entry.getValue();
                }

                List<Path> modePath = new ArrayList<>();
                for (Map.Entry<Integer, Path> entryP : od.paths.entrySet()) {
                    Path value = entryP.getValue();
                    if(value.mode==mode&&value.id!=minPathId){
                        modePath.add(value);
                    }
                }

                if(modePath.isEmpty()){
                    assP = entry.getValue();
                }

                for(int i=0;i<assP;i++){
                    Path shortestP =od.paths.get(minPathId);
                    if(passengers.size()==tmPassenger){
                        int dd =0;
                    }
                    passengers.get(tmPassenger).setPath(shortestP);
                    tmPassenger++;
                }

                for(int i=0;i<entry.getValue()-assP;i++){
                    Path p =modePath.get(i%modePath.size());
                    passengers.get(tmPassenger).setPath(p);
                    tmPassenger++;
                }

            }
        }
    }

    public void assignPassengerFirst(OD od){
        List<Path> paths = od.paths.values().stream().collect(Collectors.toList());
        for(int time=0;time<IniData.endTime;time++){
            if(!od.passengers.containsKey(time)){
                continue;
            }
            List<Passenger> passengers = od.passengers.get(time);
            if(!paths.isEmpty()&&passengers!=null){
                for(int passengerNum=0;passengerNum<passengers.size();passengerNum++){
                    passengers.get(passengerNum).setPath(paths.get(passengerNum%paths.size()));
                }
            }
        }
    }

    public double calRGap(List<OD> ods, TransitSimulation ts){
        double minTravel = 0;
        for(OD od : ods){
            for(int time = 0; time< IniData.endTime; time++){
                List<Passenger> passengers = od.passengers.get(time);
                if(passengers==null||passengers.isEmpty()){
                    continue;
                }
                int passengerNum = passengers.size();
                if(ts.modeMinTravelTimes.get(time) == null){
                    continue;
                }
                Map<Integer,Integer> modeMinTravelTimes = ts.modeMinTravelTimes.get(time).get(od.id);
                if(modeMinTravelTimes==null||modeMinTravelTimes.isEmpty()){
                    continue;
                }
                double intraMode = 0;
                for(Map.Entry<Integer, Integer> entry : modeMinTravelTimes.entrySet()){
                    intraMode += Math.exp(-0.5*entry.getValue());
                }

                //计算每种模式应当分配多少乘客
                Map<Integer,Integer> modePassengerNum = new HashMap<>();
                int assignmentedP = 0;
                int seq = 1;
                for(Map.Entry<Integer, Integer> entry : modeMinTravelTimes.entrySet()){
                    double intraModeS = Math.exp(-0.5*entry.getValue());
                    int tmPassengerNum = (int)Math.round(passengerNum*intraModeS/intraMode);
                    if(seq==modeMinTravelTimes.size()){
                        modePassengerNum.put(entry.getKey(),passengerNum-assignmentedP);
                    }else{
                        modePassengerNum.put(entry.getKey(),tmPassengerNum);
                        assignmentedP += tmPassengerNum;
                    }
                    seq ++;
                }

                for(Map.Entry<Integer, Integer> entry : modePassengerNum.entrySet()){
                    minTravel+=entry.getValue()*modeMinTravelTimes.get(entry.getKey());
                }

            }
        }

        /*int totalP = 0;
        int servedP = 0;
        double sumT = 0;
        for(OD od : ods){
            for(int time=0;time<IniData.endTime;time++){
                List<Passenger> passengers = od.passengers.get(time);
                if(passengers==null||passengers.isEmpty()){
                    continue;
                }
                for(Passenger passenger : passengers){
                    totalP++;
                    if(!passenger.unServiedPassenger){
                        servedP++;
                        sumT+=passenger.endTime-passenger.startTime;
                    }
                }
            }
        }*/
        return (ts.totalTravelTime-minTravel)/minTravel;
    }

}
