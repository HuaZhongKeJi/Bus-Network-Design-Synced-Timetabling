package com.qz.network;

import com.qz.DE.DE;
import com.qz.PSO.PSO;
import com.qz.baseClass.BusLine;
import com.qz.tool.BaseInfo;
import com.qz.tool.ExcelUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkDesign {
    //测试一下GIT222222
    //线路用一个字符串表达，线网用一个字符串list表达

    public static void main(String args[]) {
    //public static void mainGA(String args[]) {

        ExcelUtil.readExcel("./datafile/dataLinyi.xls");
//        ExcelUtil.readExcel("./datafile/anli.xls");
//        ExcelUtil.readExcel("./datafile/anliwithBRT.xls");

        BaseInfo.iniBusLineSet();

        GA ga = new GA(100);
//        GA2 ga = new GA2(100);
        ga.ini();
        List<Integer> loopR = new ArrayList<>();
        List<Double> costR = new ArrayList<>();
        List<Integer> UnSignedPassenger = new ArrayList<>();

        double tmCost = 0;
        int tmF = 0;

        //for(int i=0;i<400;i++){
        for(int i=0;i<1;i++){
            System.out.println("Loop: " +  i);
            ga.evaluation(BaseInfo.getPassengerGroups());
            ga.selectSolution();
            ga.solutionVariation(i);
            loopR.add(i);
            costR.add(ga.getBestCost());
            UnSignedPassenger.add(ga.getPassengerNotAsignRes());

            if(tmCost == ga.getBestCost()){
                tmF ++;
            }else{
                tmCost = ga.getBestCost();
                tmF = 0;
            }

            System.out.println("tmF: " +  tmF);
            System.out.println("BEST: " +  ga.getBestCost());
//            if(tmF>=25){
//                break;
//            }
        }

        List<BusLine> BestBusLine = ga.getBestBusLine();
        double BestCost = ga.getBestCost();
         List<int[]> BestSolution = ga.getBestSolution();
//         List<Integer> BestSolution = ga.getBestSolution();

        try{
            File file =new File("./result.txt");
            if(file.exists()) file.delete();
            file.createNewFile();
            FileWriter fileWritter = new FileWriter(file.getName(),true);
            fileWritter.write(costR.toString());
            fileWritter.write("\r\n");
            fileWritter.write("\r\n");
            fileWritter.write(UnSignedPassenger.toString());
            fileWritter.write("\r\n");
            fileWritter.write("\r\n");
            for(int i=0;i<BestBusLine.size();i++){
                fileWritter.write(BestBusLine.get(i).getRouteString());
                fileWritter.write("\r\n");
            }
            fileWritter.write("\r\n");
            fileWritter.write("BRT专用道容量：");
            for(int tmss=0;tmss<GA.bestSoluBRTLaneFeq.length;tmss++){
                fileWritter.write(""+GA.bestSoluBRTLaneFeq[tmss]);
                fileWritter.write("\r\n");
            }
            fileWritter.write("\r\n");
            fileWritter.write("乘客成本：");
            fileWritter.write(""+GA.getPassengerCost());
            fileWritter.write("\r\n");
            fileWritter.write("运营成本：");
            fileWritter.write(""+GA.getOperatorCost());

            fileWritter.write("\r\n");
            fileWritter.write("总乘客出行时间：");
            fileWritter.write(""+GA.totalTraveltime);

            fileWritter.write("\r\n");
            fileWritter.write("服务总乘客数：");
            fileWritter.write(""+GA.traveledPassenger);

            fileWritter.write("\r\n");
            fileWritter.write("未服务乘客数：");
            fileWritter.write(""+GA.unservedPassenger);

            fileWritter.write("\r\n");
            fileWritter.write("换乘总数：");
            fileWritter.write(""+GA.transferNumber);

            fileWritter.write("\r\n");
            fileWritter.write("线路数：");
            fileWritter.write(""+GA.routeNumber);

            fileWritter.write("\r\n");
            fileWritter.write("线路运营成本：");
            fileWritter.write(""+GA.operatingCost);


            fileWritter.write("\r\n");
            for(int i=0;i<BestSolution.size();i++){
//                fileWritter.write(BestSolution.toString());
                fileWritter.write(Arrays.toString(BestSolution.get(i)));
                fileWritter.write("\r\n");
            }

            fileWritter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        //List<Integer> BestSolution = ga.getBestSolution();
    }

//    public static void mainPso(String args[]) {
//    //public static void main(String args[]) {
//
//        ExcelUtil.readExcel("./datafile/dataLinyi.xls");
////        ExcelUtil.readExcel("./datafile/anli.xls");
////        ExcelUtil.readExcel("./datafile/anliwithBRT.xls");
//
//        BaseInfo.iniBusLineSet();
//
//        PSO pso = new PSO(100);
//        pso.ini();
//        List<Integer> loopR = new ArrayList<>();
//        List<Double> costR = new ArrayList<>();
//        List<Integer> UnSignedPassenger = new ArrayList<>();
//
//        double tmCost = 0;
//        int tmF = 0;
//
//        for(int i=0;i<400;i++){
//            System.out.println("Loop: " +  i);
//            pso.evaluation(BaseInfo.getPassengerGroups());
//            pso.psoUpdate(400,i);
//            pso.mutation();
//
//            loopR.add(i);
//            costR.add(PSO.BestCost);
//            UnSignedPassenger.add(PSO.PassengerNotAsignRes);
//
//            if(tmCost == PSO.BestCost){
//                tmF ++;
//            }else{
//                tmCost = PSO.BestCost;
//                tmF = 0;
//            }
//
//            System.out.println("tmF: " +  tmF);
//            System.out.println("BEST: " +  PSO.BestCost);
//
//        }
//
//        List<BusLine> BestBusLine = PSO.BestBusLine;
//        double BestCost = PSO.BestCost;
//        List<int[]> BestSolution = PSO.BestSolution;
//
//        try{
//            File file =new File("./result.txt");
//            if(file.exists()) file.delete();
//            file.createNewFile();
//            FileWriter fileWritter = new FileWriter(file.getName(),true);
//            fileWritter.write(costR.toString());
//            fileWritter.write("\r\n");
//            fileWritter.write("\r\n");
//            fileWritter.write(UnSignedPassenger.toString());
//            fileWritter.write("\r\n");
//            fileWritter.write("\r\n");
//            for(int i=0;i<BestBusLine.size();i++){
//                fileWritter.write(BestBusLine.get(i).getRouteString());
//                fileWritter.write("\r\n");
//            }
//            fileWritter.write("\r\n");
//            fileWritter.write("BRT专用道容量：");
//            for(int tmss=0;tmss<PSO.bestSoluBRTLaneFeq.length;tmss++){
//                fileWritter.write(""+PSO.bestSoluBRTLaneFeq[tmss]);
//                fileWritter.write("\r\n");
//            }
//            fileWritter.write("\r\n");
//            fileWritter.write("乘客成本：");
//            fileWritter.write(""+PSO.PassengerCost);
//            fileWritter.write("\r\n");
//            fileWritter.write("运营成本：");
//            fileWritter.write(""+PSO.OperatorCost);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("总乘客出行时间：");
//            fileWritter.write(""+PSO.totalTraveltime);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("服务总乘客数：");
//            fileWritter.write(""+PSO.traveledPassenger);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("未服务乘客数：");
//            fileWritter.write(""+PSO.unservedPassenger);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("换乘总数：");
//            fileWritter.write(""+PSO.transferNumber);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("线路数：");
//            fileWritter.write(""+PSO.routeNumber);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("线路运营成本：");
//            fileWritter.write(""+PSO.operatingCost);
//
//
//            fileWritter.write("\r\n");
//            for(int i=0;i<BestSolution.size();i++){
//                fileWritter.write(Arrays.toString(BestSolution.get(i)));
//                fileWritter.write("\r\n");
//            }
//
//            fileWritter.close();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }

//    public static void mainDE(String args[]) {
//    //public static void main(String args[]) {
//
//        ExcelUtil.readExcel("./datafile/dataLinyi.xls");
////        ExcelUtil.readExcel("./datafile/anli.xls");
////        ExcelUtil.readExcel("./datafile/anliwithBRT.xls");
//
//        BaseInfo.iniBusLineSet();
//
//        DE de = new DE(100);
//        de.ini();
//        List<Integer> loopR = new ArrayList<>();
//        List<Double> costR = new ArrayList<>();
//        List<Integer> UnSignedPassenger = new ArrayList<>();
//
//        double tmCost = 0;
//        int tmF = 0;
//
//        for(int i=0;i<400;i++){
//            System.out.println("Loop: " +  i);
//
//            de.evaluateSolution(BaseInfo.getPassengerGroups());
//            de.deMutation();
//            de.deCrossover();
//            de.evaluateVSolution(BaseInfo.getPassengerGroups());
//            de.selection();
//
//            loopR.add(i);
//            costR.add(DE.BestCost);
//            UnSignedPassenger.add(DE.PassengerNotAsignRes);
//
//            if(tmCost == DE.BestCost){
//                tmF ++;
//            }else{
//                tmCost = DE.BestCost;
//                tmF = 0;
//            }
//
//            System.out.println("tmF: " +  tmF);
//            System.out.println("BEST: " +  DE.BestCost);
//
//        }
//
//        List<BusLine> BestBusLine = DE.BestBusLine;
//        double BestCost = DE.BestCost;
//        List<int[]> BestSolution = DE.BestSolution;
//
//        try{
//            File file =new File("./result.txt");
//            if(file.exists()) file.delete();
//            file.createNewFile();
//            FileWriter fileWritter = new FileWriter(file.getName(),true);
//            fileWritter.write(costR.toString());
//            fileWritter.write("\r\n");
//            fileWritter.write("\r\n");
//            fileWritter.write(UnSignedPassenger.toString());
//            fileWritter.write("\r\n");
//            fileWritter.write("\r\n");
//            for(int i=0;i<BestBusLine.size();i++){
//                fileWritter.write(BestBusLine.get(i).getRouteString());
//                fileWritter.write("\r\n");
//            }
//            fileWritter.write("\r\n");
//            fileWritter.write("BRT专用道容量：");
//            for(int tmss=0;tmss<DE.bestSoluBRTLaneFeq.length;tmss++){
//                fileWritter.write(""+DE.bestSoluBRTLaneFeq[tmss]);
//                fileWritter.write("\r\n");
//            }
//            fileWritter.write("\r\n");
//            fileWritter.write("乘客成本：");
//            fileWritter.write(""+DE.PassengerCost);
//            fileWritter.write("\r\n");
//            fileWritter.write("运营成本：");
//            fileWritter.write(""+DE.OperatorCost);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("总乘客出行时间：");
//            fileWritter.write(""+DE.totalTraveltime);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("服务总乘客数：");
//            fileWritter.write(""+DE.traveledPassenger);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("未服务乘客数：");
//            fileWritter.write(""+DE.unservedPassenger);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("换乘总数：");
//            fileWritter.write(""+DE.transferNumber);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("线路数：");
//            fileWritter.write(""+DE.routeNumber);
//
//            fileWritter.write("\r\n");
//            fileWritter.write("线路运营成本：");
//            fileWritter.write(""+DE.operatingCost);
//
//
//            fileWritter.write("\r\n");
//            for(int i=0;i<BestSolution.size();i++){
//                fileWritter.write(Arrays.toString(BestSolution.get(i)));
//                fileWritter.write("\r\n");
//            }
//
//            fileWritter.close();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }

}
