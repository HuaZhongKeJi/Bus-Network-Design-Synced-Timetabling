package com.qz.tool;

import com.qz.baseClass.Line.BRTLine;
import com.qz.baseClass.Line.BaseLine;
import com.qz.baseClass.Line.RailLine;
import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;
import com.qz.baseClass.Passenger.OD;
import com.qz.baseClass.Passenger.Passenger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class IniData {
    /// 运营终止时间
    public static int endTime = 820;
    public static int busStartTime = 1;
    public static int busEndTime = 720;

    public static double maxBusFrequency = 30;
    public static double BRTFrequency = 8;
    public static double RailFrequency = 10;

    public static int maxLineNum = 8;
    public static int minLineNum = 4;
    public static int maxLineNodeNum = 12;
    public static int minLineNodeNum = 5;
    public static double maxGap = 0.1;
    public static double busTravelTimeCoefficient = 0.007;
    public static double passengerTravelTimeCoefficient = 0.003;
    public static double syCoefficient = 0.01;
    //public static double syCoefficient = 0.0;
    public static double unServedPassengerCoefficient = 0.98;
    public static int maxBusVehicles = 120;
    public static int busCapacity = 60;
    public static int BRTCapacity = 80;
    public static int railCapacity = 200;
    /// 串车的判断时间
    public static int bunchingTime = 2;
    /// 大间隔的判断时间
    public static int gapTime = 30;
    /// 调度时判断发车的最大站点乘客数
    public static int maxPassengerNum = 50;

    public static void IniDataFromCsv(){
        //初始化Node
        String csvFile = "Z:\\数据盘1\\project\\NetworkDesignTimetable\\datafile\\yiwu\\node.csv";
        //String csvFile = "Z:\\project\\NetworkDesignTimetable\\datafile\\yiwu\\node.csv";
        String line = "";
        String cvsSplitBy = ",";
        int lineNum = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                lineNum ++;
                if(lineNum == 1){
                    continue;
                }
                String[] data = line.split(cvsSplitBy);
                Node n = new Node(Integer.parseInt(data[0]),Integer.parseInt(data[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        //初始化Link
        csvFile = "Z:\\数据盘1\\project\\NetworkDesignTimetable\\datafile\\yiwu\\link.csv";
        //csvFile = "Z:\\project\\NetworkDesignTimetable\\datafile\\yiwu\\link.csv";
        line = "";
        List<Double> busSpeedChange = new ArrayList<>();
        busSpeedChange.add(100.0);//8:00
        busSpeedChange.add(150.0);//9:00
        busSpeedChange.add(200.0);//10:00
        busSpeedChange.add(200.0);//11:00
        busSpeedChange.add(200.0);//12:00
        busSpeedChange.add(200.0);//13:00
        busSpeedChange.add(200.0);//14:00
        busSpeedChange.add(200.0);//15:00
        busSpeedChange.add(150.0);//16:00
        busSpeedChange.add(100.0);//17:00
        busSpeedChange.add(100.0);//18:00

        double bRTSpeedChange = 200;
        double railSpeedChange = 300;
        double bikeSpeedChange = 150;
        double walkSpeedChange = 60;

        cvsSplitBy = ",";
        lineNum = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                lineNum ++;
                if(lineNum == 1){
                    continue;
                }
                String[] data = line.split(cvsSplitBy);

                List<Double> travelTime = new ArrayList<>();
                if(Integer.parseInt(data[3])==1){
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(0));//8:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(1));//9:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(2));//10:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(3));//11:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(4));//12:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(5));//13:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(6));//14:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(7));//15:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(8));//16:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(9));//17:00
                    travelTime.add(Double.parseDouble(data[7])/busSpeedChange.get(10));//18:00
                } else if (Integer.parseInt(data[3])==2) {
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//8:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//9:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//10:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//11:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//12:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//13:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//14:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//15:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//16:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//17:00
                    travelTime.add(Double.parseDouble(data[7])/bRTSpeedChange);//18:00
                }else if (Integer.parseInt(data[3])==3) {
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//8:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//9:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//10:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//11:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//12:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//13:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//14:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//15:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//16:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//17:00
                    travelTime.add(Double.parseDouble(data[7])/railSpeedChange);//18:00
                }else if (Integer.parseInt(data[3])==4) {
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//8:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//9:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//10:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//11:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//12:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//13:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//14:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//15:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//16:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//17:00
                    travelTime.add(Double.parseDouble(data[7])/bikeSpeedChange);//18:00
                }else if (Integer.parseInt(data[3])==5) {
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//8:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//9:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//10:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//11:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//12:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//13:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//14:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//15:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//16:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//17:00
                    travelTime.add(Double.parseDouble(data[7])/walkSpeedChange);//18:00
                }


                Link link = new Link(Integer.parseInt(data[4]),Integer.parseInt(data[3]),
                        Integer.parseInt(data[0]),Integer.parseInt(data[1]),Double.parseDouble(data[6]),travelTime);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //初始化Passegner
        csvFile = "Z:\\数据盘1\\project\\NetworkDesignTimetable\\datafile\\yiwu\\passenger.csv";
        //csvFile = "Z:\\project\\NetworkDesignTimetable\\datafile\\yiwu\\passenger.csv";
        line = "";
        cvsSplitBy = ",";
        lineNum = 0;

        List<Double> passengerChange = new ArrayList<>();
        passengerChange.add(1.0);//8:00
        passengerChange.add(0.8);//9:00
        passengerChange.add(0.5);//10:00
        passengerChange.add(0.2);//11:00
        passengerChange.add(0.1);//12:00
        passengerChange.add(0.3);//13:00
        passengerChange.add(0.2);//14:00
        passengerChange.add(0.1);//15:00
        passengerChange.add(0.3);//16:00
        passengerChange.add(0.8);//17:00
        passengerChange.add(0.5);//18:00

        int sumPassengerNum = 0;
        Random random = new Random(12345);
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                lineNum ++;
                if(lineNum == 1){
                    continue;
                }
                String[] data = line.split(cvsSplitBy);
                Map<Integer,List<Passenger>> passengers = new HashMap<>();
                Node startNode = Node.getNode(Integer.parseInt(data[0]));
                Node endNode = Node.getNode(Integer.parseInt(data[1]));

                for(int i=0;i<(busEndTime/60)-1;i++){
                    int passengerNum = (int)(Double.parseDouble(data[2])*passengerChange.get(i));
                    sumPassengerNum+=passengerNum;
                    for(int j=0;j<passengerNum;j++){
                        int rad = random.nextInt(60)+1;
                        int startTime = i*60+rad;
                        Passenger p = new Passenger(Passenger.tmId,startNode,endNode,startTime);
                        Passenger.tmId++;
                        if(passengers.containsKey(startTime)){
                            passengers.get(startTime).add(p);
                        }else{
                            List<Passenger> tmP = new ArrayList<>();
                            tmP.add(p);
                            passengers.put(startTime,tmP);
                        }
                    }
                }

                OD od = new OD(OD.tmId,startNode,endNode,passengers);
                od.maxPassenger = (int)Double.parseDouble(data[2]);
                OD.tmId++;
                OD.ods.add(od);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("sumPassengerNum: " +  sumPassengerNum);

        //初始化BRTline
        List<Node> BRTNodes = new ArrayList<>();
        BRTNodes.add(Node.getNode(0));
        BRTNodes.add(Node.getNode(1));
        BRTNodes.add(Node.getNode(2));
        BRTNodes.add(Node.getNode(3));
        BRTNodes.add(Node.getNode(4));
        BRTNodes.add(Node.getNode(5));
        List<Integer> brtTimetable = new ArrayList<>();
        for(int i=1;i<=busEndTime;i++){
            if(i%8==1){
                brtTimetable.add(i);
            }
        }
        BRTLine brtLine = new BRTLine(BaseLine.tmID,BRTNodes,brtTimetable,true);
        BaseLine.tmID++;

        //初始化RailLine
        List<Node> railNodes = new ArrayList<>();
        railNodes.add(Node.getNode(6));
        railNodes.add(Node.getNode(7));
        railNodes.add(Node.getNode(8));
        railNodes.add(Node.getNode(9));
        railNodes.add(Node.getNode(10));
        List<Integer> railTimetable = new ArrayList<>();
        for(int i=1;i<=busEndTime;i++){
            if(i%10==1){
                railTimetable.add(i);
            }
        }
        RailLine railLine = new RailLine(BaseLine.tmID,railNodes,railTimetable,true);
        BaseLine.tmID++;

    }

}
