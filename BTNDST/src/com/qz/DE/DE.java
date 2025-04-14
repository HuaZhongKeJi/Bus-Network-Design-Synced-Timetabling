package com.qz.DE;

import com.qz.PBGA.Chromosome;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Node.Node;
import com.qz.baseClass.Passenger.Passenger;
import com.qz.baseClass.PassengerGroup;
import com.qz.tool.IniData;

import java.util.*;

public class DE {
    public static Double PassengerCost = -9999999999999999999999999.0;
    public static Double OperatorCost = -9999999999999999999999999.0;
    public static float[] bestSoluBRTLaneFeq;
    public static int PassengerNotAsignRes;
    public static List<BusLineNew> BestBusLine;
    public static Chromosome BestSolution;
    private List<Chromosome> solutionList;//解集
    private List<Chromosome> vsolutionList;//变异解集
    private List<Chromosome> cSolutionList;//变异解集
    public static Double b = 0.8;//片段适应度衰减系数
    public static Double BestCost = -9999999999999999999999999.0;
    public static Double totalTraveltime = 9999999999999999999999999999.0;
    public static Double traveledPassenger = 9999999999999999999999999999.0;
    public static Double unservedPassenger = 9999999999999999999999999999.0;
    public static Double transferNumber = 9999999999999999999999999999.0;
    public static Double routeNumber = 9999999999999999999999999999.0;
    public static Double operatingCost = 9999999999999999999999999999.0;

    public static List<Double> vSoluFitness;//粒子最优适应度

    private List<Double> solutionSufficiency;//解的适应度函数

    private final int pointNum;
    private final int populationSize;//种群规模

    public DE(int population_size) {

        vSoluFitness = new ArrayList<>();

        BestSolution = null;

        solutionList = new ArrayList<>();
        solutionSufficiency = new ArrayList<>();

        pointNum = Node.BRTNodes.size()+Node.busNodes.size();
        populationSize = population_size;

    }

    public void ini() {
        //生成初始解集合，最开始均生成max的结果
        List<Integer> tm1 = new ArrayList<>();
        List<Integer> tm2 = new ArrayList<>();

        for (int i = 0; i < pointNum; i++) {
            tm1.add(i);
        }
        boolean tmflag = true;
        for (int i = 0; i < populationSize; i++) {//解集要包含pointNum+1个元素
            //Chromosome tmSolution = new ArrayList<>();
            //Chromosome tmSolution2 = new ArrayList<>();
            List<List<Integer>> gen = new ArrayList<>();
            Random random = new Random();
            int randomNumber = random.nextInt(IniData.maxLineNum - IniData.minLineNum + 1) + IniData.minLineNum;

            for (int j = 0; j < randomNumber; j++) {//------------------------------------------------------------
                List<Integer> tmSolutionLine = new ArrayList<>();
                //int[] List<Integer> = new int[pointNum + 1];

                for (int k = 0; k <= pointNum; k++) {
                    Random df = new Random();
                    if (k == pointNum) {
                        tmSolutionLine.add(df.nextInt(IniData.maxLineNodeNum - IniData.minLineNodeNum + 1) + IniData.minLineNodeNum);
                    } else {
                        if (tmflag) {
                            tmSolutionLine.add(tm1.remove(df.nextInt(tm1.size())));
                            tm2.add(k);
                        } else {
                            tmSolutionLine.add(tm2.remove(df.nextInt(tm2.size())));
                            tm1.add(k);
                        }
                    }
                }

                tmflag = !tmflag;
                gen.add(tmSolutionLine);
            }

            Chromosome tmSolution = new Chromosome(gen,pointNum);
            vSoluFitness.add(-999999999999.0);
            solutionList.add(tmSolution);
            solutionSufficiency.add(0.0);
        }
    }

    public void deMutation(){
        vsolutionList = new ArrayList<>();

        for(int i=0;i<populationSize;i++){

            Random random =new Random();
            int num1 =random.nextInt(populationSize);
            int num2 =random.nextInt(populationSize);
            int num3 =random.nextInt(populationSize);

//            List<int[]> tmSolution1 = solutionList.get(num1);
//            List<int[]> tmSolution2 = solutionList.get(num2);
//            List<int[]> tmSolution3 = solutionList.get(num3);

            Chromosome tmSolution1 = solutionList.get(num1);
            Chromosome tmSolution2 = solutionList.get(num2);
            Chromosome tmSolution3 = solutionList.get(num3);

            int minLineNum = 999;
            if(minLineNum>tmSolution1.solution.size()){
                minLineNum = tmSolution1.solution.size();
            }
            if(minLineNum>tmSolution2.solution.size()){
                minLineNum = tmSolution2.solution.size();
            }
            if(minLineNum>tmSolution3.solution.size()){
                minLineNum = tmSolution3.solution.size();
            }

            List<List<Integer>> tmVSolution = new ArrayList<>();
            for(int j=0;j<minLineNum;j++){
                Map<Integer,Double> tmNewSolu = new HashMap<>();

                for(int k=0;k<pointNum;k++){
                    tmNewSolu.put(k, tmSolution1.solution.get(j).get(k)+0.5*(tmSolution2.solution.get(j).get(k)-tmSolution3.solution.get(j).get(k)));
                }
                tmNewSolu.put(pointNum, (double)random.nextInt(IniData.maxLineNodeNum - IniData.minLineNodeNum + 1) + IniData.minLineNodeNum);

                List<Map.Entry<Integer, Double>> sortedEntries = new ArrayList<>(tmNewSolu.entrySet());
                sortedEntries.sort(Map.Entry.comparingByValue());

                List<Integer> tnewSolu = new ArrayList<>();
                for(int k=0;k<pointNum + 1;k++){
                    tnewSolu.add(0);
                }
                //int[] tnewSolu = new int[pointNum + 1];
                int tk = 0;

                for (Map.Entry<Integer, Double> entry : sortedEntries) {
                    if(entry.getKey() != pointNum){
                        tnewSolu.set(entry.getKey(),tk);
                        //tnewSolu[entry.getKey()] = tk;
                        tk ++;
                    }else{
                        tnewSolu.set(entry.getKey(),(int) Math.round(entry.getValue()));
                        //tnewSolu[entry.getKey()] = );
                    }
                }

                tmVSolution.add(tnewSolu);
            }
            Chromosome tmC = new Chromosome(tmVSolution,pointNum);
            vsolutionList.add(tmC);
        }
    }

    public void deCrossover(){
        cSolutionList = new ArrayList<>();
        for(int i=0;i<populationSize;i++){
            List<List<Integer>> tmSolution1 = solutionList.get(i).solution;
            List<List<Integer>> tmSolution2 = vsolutionList.get(i).solution;
            int maxRouteNum = Math.min(IniData.maxLineNum, Math.max(tmSolution1.size(), tmSolution2.size())+1);
            int minRouteNum = Math.max(IniData.minLineNum, Math.min(tmSolution1.size(), tmSolution2.size())-5);

            Random rand = new Random();
            int randNumber = rand.nextInt(maxRouteNum - minRouteNum + 1) + minRouteNum;

            List<List<Integer>> tmNSolution = new ArrayList<>();

            for(int j=0;j<randNumber;j++){
                List<Integer> newRoute = new ArrayList<>();//int[pointNum + 1];

                if(j>=tmSolution1.size()&&j>=tmSolution2.size()){
                    //随机生成
                    List<Integer> tm1 = new ArrayList<>();
                    List<Integer> tm2 = new ArrayList<>();

                    for (int i2 = 0; i2 < pointNum; i2++) {
                        tm1.add(i2);
                    }
                    for (int k = 0; k <= pointNum; k++) {
                        Random df = new Random();
                        if (k == pointNum) {
                            newRoute.add(df.nextInt(IniData.maxLineNodeNum - IniData.minLineNodeNum + 1) + IniData.minLineNodeNum);
                        } else {
                            newRoute.add(tm1.remove(df.nextInt(tm1.size())));
                            tm2.add(k);
                        }
                    }

                }else{
                    if(j<tmSolution1.size()&&j<tmSolution2.size()){
                        Map<Integer,Integer> tmNewSolu = new HashMap<>();
                        for(int k=0;k<=pointNum;k++){
                            double randomNumber = Math.random();
                            if(randomNumber>0.5){
                                tmNewSolu.put(k, tmSolution1.get(j).get(k));
                            }else{
                                tmNewSolu.put(k, tmSolution2.get(j).get(k));
                            }
                        }
                        List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(tmNewSolu.entrySet());
                        sortedEntries.sort(Map.Entry.comparingByValue());
                        int tk = 0;

                        for(int k=0;k<pointNum + 1;k++){
                            newRoute.add(0);
                        }

                        for (Map.Entry<Integer, Integer> entry : sortedEntries) {
                            if(entry.getKey() != pointNum){
                                newRoute.set(entry.getKey(),tk);
                                tk ++;
                            }else{
                                newRoute.set(entry.getKey(),entry.getValue());
                            }
                        }

                    }else if(j<tmSolution1.size()){
                        for(int k=0;k<=pointNum;k++){
                            newRoute.add(tmSolution1.get(j).get(k));
                        }
                    }else{
                        for(int k=0;k<=pointNum;k++){
                            newRoute.add(tmSolution2.get(j).get(k));
                        }
                    }
                }
                tmNSolution.add(newRoute);
            }
            Chromosome tmC = new Chromosome(tmNSolution,pointNum);
            cSolutionList.add(tmC);
        }
    }

    public void selection(){

        //List<int[]> BestSolution;
        for(int i=0;i<populationSize;i++){
            if(vSoluFitness.get(i)>solutionSufficiency.get(i)){
                solutionList.set(i,cSolutionList.get(i));
            }
        }

        for(int i=0;i<20;i++){
            if(solutionSufficiency.get(i)<BestCost){
                List<List<Integer>> tmSolution = new ArrayList<>();
                for(int k=0;k<BestSolution.solution.size();k++){
                    List<Integer> tmSolutionLine = new ArrayList<>();//int[pointNum + 1];
                    for(int l=0;l<=pointNum;l++){
                        tmSolutionLine.add(BestSolution.solution.get(k).get(l));
                    }
                    tmSolution.add(tmSolutionLine);
                }
                Chromosome tmC = new Chromosome(tmSolution,pointNum);
                solutionList.set(i,tmC);
            }
        }

    }


    public void evaluation() {
        evaluateSolution();
    }//进化

    public void evaluateSolution() {

        List<Double> tmSolutionSufficiency = new ArrayList<>();
        List<List<Double>> tmSolutionPartSufficiency = new ArrayList<>();
        List<List<Float>> tmSolutionFrq = new ArrayList<>();

        List<List<Passenger>> tmUnSerPassenger = new ArrayList<>();

        for(int s = 0;s<solutionList.size();s++){
            tmSolutionSufficiency.add(0.0);
            tmUnSerPassenger.add(new ArrayList<>());
            tmSolutionPartSufficiency.add(null);
            tmSolutionFrq.add(null);
        }

        EvaluateRunnableNetworkTimetableDE R1 = new EvaluateRunnableNetworkTimetableDE(1, 0,
                solutionList.subList(0,10), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R2 = new EvaluateRunnableNetworkTimetableDE(2, 10,
                solutionList.subList(10,20), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R3 = new EvaluateRunnableNetworkTimetableDE(3, 20,
                solutionList.subList(20,30), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R4 = new EvaluateRunnableNetworkTimetableDE(4, 30,
                solutionList.subList(30,40), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R5 = new EvaluateRunnableNetworkTimetableDE(5, 40,
                solutionList.subList(40,50), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R6 = new EvaluateRunnableNetworkTimetableDE(6, 50,
                solutionList.subList(50,60), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R7 = new EvaluateRunnableNetworkTimetableDE(7, 60,
                solutionList.subList(60,70), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R8 = new EvaluateRunnableNetworkTimetableDE(8, 70,
                solutionList.subList(70,80), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R9 = new EvaluateRunnableNetworkTimetableDE(9, 80,
                solutionList.subList(80,90), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetableDE R10 = new EvaluateRunnableNetworkTimetableDE(10, 90,
                solutionList.subList(90,100), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);

        R1.start();
        R2.start();
        R3.start();
        R4.start();
        R5.start();
        R6.start();
        R7.start();
        R8.start();
        R9.start();
        R10.start();

        while ((!R1.finFlag)||(!R2.finFlag)||(!R3.finFlag)||(!R4.finFlag)||(!R5.finFlag)||(!R6.finFlag)||(!R7.finFlag)||(!R8.finFlag)||(!R9.finFlag)||(!R10.finFlag)){//||(!R11.finFlag)||(!R12.finFlag)){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        R1 = null;
        R2 = null;
        R3 = null;
        R4 = null;
        R5 = null;
        R6 = null;
        R7 = null;
        R8 = null;
        R9 = null;
        R10 = null;


        solutionSufficiency = tmSolutionSufficiency;//这里只需要存当前解的评价函数

    }

    public void evaluateVSolution() {

        EvaluateRunnableNetworkTimetableVDE R1 = new EvaluateRunnableNetworkTimetableVDE(1, 0,  cSolutionList.subList(0,10));
        EvaluateRunnableNetworkTimetableVDE R2 = new EvaluateRunnableNetworkTimetableVDE(2, 10, cSolutionList.subList(10,20));
        EvaluateRunnableNetworkTimetableVDE R3 = new EvaluateRunnableNetworkTimetableVDE(3, 20, cSolutionList.subList(20,30));
        EvaluateRunnableNetworkTimetableVDE R4 = new EvaluateRunnableNetworkTimetableVDE(4, 30, cSolutionList.subList(30,40));
        EvaluateRunnableNetworkTimetableVDE R5 = new EvaluateRunnableNetworkTimetableVDE(5, 40, cSolutionList.subList(40,50));
        EvaluateRunnableNetworkTimetableVDE R6 = new EvaluateRunnableNetworkTimetableVDE(6, 50, cSolutionList.subList(50,60));
        EvaluateRunnableNetworkTimetableVDE R7 = new EvaluateRunnableNetworkTimetableVDE(7, 60, cSolutionList.subList(60,70));
        EvaluateRunnableNetworkTimetableVDE R8 = new EvaluateRunnableNetworkTimetableVDE(8, 70, cSolutionList.subList(70,80));
        EvaluateRunnableNetworkTimetableVDE R9 = new EvaluateRunnableNetworkTimetableVDE(9, 80, cSolutionList.subList(80,90));
        EvaluateRunnableNetworkTimetableVDE R10 = new EvaluateRunnableNetworkTimetableVDE(10, 90, cSolutionList.subList(90,100));

        R1.start();
        R2.start();
        R3.start();
        R4.start();
        R5.start();
        R6.start();
        R7.start();
        R8.start();
        R9.start();
        R10.start();

        while ((!R1.finFlag)||(!R2.finFlag)||(!R3.finFlag)||(!R4.finFlag)||(!R5.finFlag)||(!R6.finFlag)||(!R7.finFlag)||(!R8.finFlag)||(!R9.finFlag)||(!R10.finFlag)){//||(!R11.finFlag)||(!R12.finFlag)){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        R1 = null;
        R2 = null;
        R3 = null;
        R4 = null;
        R5 = null;
        R6 = null;
        R7 = null;
        R8 = null;
        R9 = null;
        R10 = null;

    }

}
