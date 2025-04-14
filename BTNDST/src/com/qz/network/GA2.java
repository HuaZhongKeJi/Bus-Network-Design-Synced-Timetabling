package com.qz.network;

import com.qz.baseClass.BusLine;
import com.qz.baseClass.PassengerGroup;
import com.qz.baseClass.PassengerRoute;
import com.qz.tool.BaseInfo;
import com.qz.tool.EvaluateRunnable2;
import com.qz.tool.MatOperate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GA2 {
    private static int pointNum;
    private final double mortalityRate = 0.15;//淘汰率
    private static int populationSize;//种群规模

    //List<int[pointNum+1]> 一个解，每一行代表一条公交线路，由站点的优先级构成,最后还要加一个终点的优先级
    public static Double b = 0.8;//片段适应度衰减系数
    private Double c1 = 0.0;//交叉概率（组间交叉，片段交叉）
    private Double c2 = 0.1;//交叉概率（组间交叉，片段交叉）

    private Double m1 = 0.5;//变异概率(仅有片段变异)
    private Double m2 = 0.5;//变异概率

    private double rateOfA = 0.6;
    private double rateOfB = 0.3;
    private double rateOfC = 0.1;

    private double sumSolutionSufficiency = 0;
    private double avgSolutionSufficiency = 0;
    private double maxSolutionSufficiency = 0;

    private double sumSolutionPartSufficiency = 0;
    private double avgSolutionPartSufficiency = 0;
    private double maxSolutionPartSufficiency = 0;

    private List<List<Integer>> solutionList;//解集
    private List<List<Float>> solutionFrq;//解集
    public static List<List<Double>> solutionPartSufficiency;//解集中片段的适应度
    private List<Double> solutionSufficiency;//解的适应度函数

    public List<Integer> getBestSolution() {
        return BestSolution;
    }

    public void setBestSolution(List<Integer> bestSolution) {
        BestSolution = bestSolution;
    }

    public int getPassengerNotAsignRes() {
        return PassengerNotAsignRes;
    }

    public void setPassengerNotAsignRes(int passengerNotAsignRes) {
        PassengerNotAsignRes = passengerNotAsignRes;
    }

    public static int PassengerNotAsignRes;
    public static List<Integer> BestSolution;
    public static List<BusLine> BestBusLine;
    public static Double BestCost = -9999999999999999999999999.0;

    //需要记录没有被用到的片段，着力进行裁剪-----------------------------------------------------------------------------------------------！！！！！！！！！！！！！！！！！！

    public List<BusLine> getBestBusLine() {
        return BestBusLine;
    }

    public double getBestCost() {
        return BestCost;
    }

    public GA2(int population_size) {
        solutionList = new ArrayList<>();
        solutionSufficiency = new ArrayList<>();
        solutionPartSufficiency = new ArrayList<>();
        pointNum = BaseInfo.getPointNum();
        populationSize = population_size;
        //生成可达矩阵集合

    }





    public void selectSolution() {//根据适应度函数，选择算子,遗传
        MatOperate.QuickSort2(solutionSufficiency, 0, solutionSufficiency.size() - 1, solutionList, solutionPartSufficiency, solutionFrq);
        int mortalityNum = (int) (mortalityRate * populationSize);

        List<List<Integer>> newSolutionList = new ArrayList<>();
        List<List<Double>> newSolutionPartSufficiency = new ArrayList<>();
        List<Double> tmsolutionSufficiency = new ArrayList<>();
        List<List<Float>> newSolutionFrq = new ArrayList<>();

        int numOf1 = (int) (0.5 * (populationSize - mortalityNum));
        int numOf2 = (int) (0.3 * (populationSize - mortalityNum));
        int numOf3 = (populationSize - mortalityNum) - numOf1 - numOf2;

        double sumSufficiency = 0;

        double sumSufficiency1 = 0;
        for (int i = mortalityNum; i < (mortalityNum + numOf1); i++) {
            sumSufficiency1 += solutionSufficiency.get(i);
        }

        double sumSufficiency2 = 0;
        for (int i = (mortalityNum + numOf1); i < (mortalityNum + numOf1 + numOf2); i++) {
            sumSufficiency2 += solutionSufficiency.get(i);
        }

        double sumSufficiency3 = 0;
        for (int i = (mortalityNum + numOf1 + numOf2); i < populationSize; i++) {
            sumSufficiency3 += solutionSufficiency.get(i);
        }


        sumSolutionSufficiency = 0;
        sumSolutionPartSufficiency = 0;
        maxSolutionSufficiency = -999999999999999999.0;
        maxSolutionPartSufficiency = -999999999999999999.0;
        double tmPartNum = 0.0;


        int numOfA = (int) (rateOfA * populationSize);
        int numOfB = (int) (rateOfB * populationSize);

        for (int i = 0; i < populationSize; i++) {

            //随机选两个比好坏
            double ram1 = java.lang.Math.random();
            double ram2 = java.lang.Math.random();
            double ram = Math.min(ram1, ram2);

            double tmD = 0;

            int startL = 0;
            int endL = 0;


            if (i < numOfA) {
                sumSufficiency = sumSufficiency3;
                startL = populationSize - 1;
                endL = populationSize - numOf3 - 1;
            } else if (i < numOfA + numOfB) {
                sumSufficiency = sumSufficiency2;
                startL = populationSize - numOf3 - 1;
                endL = populationSize - numOf3 - numOf2 - 1;
            } else {
                sumSufficiency = sumSufficiency1;
                startL = populationSize - numOf3 - numOf2 - 1;
                endL = mortalityNum - 1;
            }


            for (int tmdj = startL; tmdj > endL; tmdj--) {

                double tmSolutionSufficiency = solutionSufficiency.get(tmdj);
                tmD += tmSolutionSufficiency;
                double ts = tmD / sumSufficiency;


                if (ram < ts) {//轮盘赌，选择了j
                    List<Integer> tmSolu = new ArrayList<>(solutionList.get(tmdj));

                    tmsolutionSufficiency.add(tmSolutionSufficiency);
                    newSolutionList.add(tmSolu);
                    sumSolutionSufficiency += tmSolutionSufficiency;
                    if (tmSolutionSufficiency > maxSolutionSufficiency) {
                        maxSolutionSufficiency = tmSolutionSufficiency;
                    }

                    List<Float> tmSolutionFrq = new ArrayList<>(solutionFrq.get(tmdj));
                    newSolutionFrq.add(tmSolutionFrq);


                    break;
                }
            }

        }

        avgSolutionSufficiency = sumSolutionSufficiency / populationSize;
        avgSolutionPartSufficiency = sumSolutionPartSufficiency / tmPartNum;
        solutionList = null;
        solutionPartSufficiency = null;
        solutionFrq = null;
        solutionSufficiency = tmsolutionSufficiency;
        solutionList = newSolutionList;
        solutionPartSufficiency = newSolutionPartSufficiency;
        solutionFrq = newSolutionFrq;
    }


    public void solutionVariation() {//交叉、变异，整体解的长度交叉变异，子解的变异----------------------------------------------------------------------------------------------
        //主解交叉与变异
        Random r = new Random();
        for (int i = 0; i < populationSize; i++) {
            double Sufficiency = solutionSufficiency.get(i);
            double Pm = 0;//交叉概率
            if (Sufficiency >= avgSolutionSufficiency) {
                Pm = m1 * (maxSolutionSufficiency - Sufficiency) / (maxSolutionSufficiency - avgSolutionSufficiency);
            } else {
                Pm = m2;
            }
            double ramPt1 = java.lang.Math.random();//交叉
            double ramPt2 = java.lang.Math.random();//变异

            if (ramPt1 < Pm){//交叉
                for (int j = populationSize-1; j >=0; j--) {

                    double Sufficiencys = solutionSufficiency.get(j);
                    double Pms = 0;//交叉概率
                    if (Sufficiencys >= avgSolutionSufficiency) {
                        Pms = m1 * (maxSolutionSufficiency - Sufficiencys) / (maxSolutionSufficiency - avgSolutionSufficiency);
                    } else {
                        Pms = m2;
                    }

                    double ramPt3 = java.lang.Math.random();

                    if (ramPt3 < Pms) {

                        int tmAddSeq = Math.min(r.nextInt(solutionList.get(i).size()-1),r.nextInt(solutionList.get(j).size()-1));
                        int tmAddSeqEnd = solutionList.get(i).size()-1;
                        int tmAddSeq2End = solutionList.get(j).size()-1;

                        for(int ss = tmAddSeqEnd;ss>=tmAddSeq;ss--){
                            solutionFrq.get(j).add(solutionFrq.get(i).remove(ss));
                            solutionList.get(j).add(solutionList.get(i).remove(ss));
                        }

                        for(int ss = tmAddSeq2End;ss>=tmAddSeq;ss--){
                            solutionFrq.get(i).add(solutionFrq.get(j).remove(ss));
                            solutionList.get(i).add(solutionList.get(j).remove(ss));
                        }
                    }
                }
            }

            if (ramPt2 < Pm) {//交叉
                double ramPt3 = java.lang.Math.random();//变异
                if(ramPt3>0.5 && solutionList.get(i).size()>BaseInfo.getMinLineNum()){
                    int delF = 0;
                    for(int j=solutionFrq.get(i).size()-1;j>=0;j--){
                        if(solutionFrq.get(i).get(j)==0){
                            solutionFrq.get(i).remove(j);
                            solutionList.get(i).remove(j);
                            if(solutionList.get(i).size()==BaseInfo.getMinLineNum()){
                                delF = 1;
                                break;
                            }
                        }
                    }
                    if(delF==0){
                        int lineSeq = r.nextInt(solutionList.get(i).size());
                        solutionFrq.get(i).remove(lineSeq);
                        solutionList.get(i).remove(lineSeq);
                    }

                }else{
                    int tmSeq = r.nextInt(solutionList.get(i).size());
                    int lineSeq = r.nextInt(BaseInfo.busLineSet.size());
                    while(solutionList.get(i).contains(lineSeq)){
                        lineSeq = r.nextInt(BaseInfo.busLineSet.size());
                    }
                    solutionList.get(i).set(tmSeq,lineSeq);
                }
            }

        }
    }//----------------------------------------------------------------------------------------------


    public void ini() {

        Random r = new Random();
        int busLinNum = BaseInfo.busLineSet.size();
        for (int i = 0; i < populationSize; i++) {//解集要包含pointNum+1个元素
            List<Integer> tmSolution = new ArrayList<>();
            List<Double> tmSolutionPartSufficiency = new ArrayList<>();
            int solutionSize = r.nextInt((BaseInfo.getMaxLineNum() - BaseInfo.getMinLineNum()) + 1) + BaseInfo.getMinLineNum();
            while(tmSolution.size()<solutionSize){
                int lineSeq = r.nextInt(busLinNum);
                if(!tmSolution.contains(lineSeq)){
                    tmSolution.add(lineSeq);
                    tmSolutionPartSufficiency.add(0.0);
                }
            }
            solutionPartSufficiency.add(tmSolutionPartSufficiency);
            solutionList.add(tmSolution);
            solutionSufficiency.add(0.0);
        }

    }



    public void evaluation(List<PassengerGroup> passengerGroups) {
        evaluateSolution(passengerGroups);
    }//进化

    public void evaluateSolution(List<PassengerGroup> passengerGroups) {

        List<Double> tmSolutionSufficiency = new ArrayList<>();
        List<List<Double>> tmSolutionPartSufficiency = new ArrayList<>();
        List<List<Float>> tmSolutionFrq = new ArrayList<>();

        for(int s = 0;s<solutionList.size();s++){
            tmSolutionSufficiency.add(0.0);
            tmSolutionPartSufficiency.add(null);
            tmSolutionFrq.add(null);
        }

        EvaluateRunnable2 R1 = new EvaluateRunnable2(1, 0, pointNum, passengerGroups,
                solutionList.subList(0,10), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R2 = new EvaluateRunnable2(2, 10, pointNum, passengerGroups,
                solutionList.subList(10,20), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R3 = new EvaluateRunnable2(3, 20, pointNum, passengerGroups,
                solutionList.subList(20,30), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R4 = new EvaluateRunnable2(4, 30, pointNum, passengerGroups,
                solutionList.subList(30,40), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R5 = new EvaluateRunnable2(5, 40, pointNum, passengerGroups,
                solutionList.subList(40,50), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R6 = new EvaluateRunnable2(6, 50, pointNum, passengerGroups,
                solutionList.subList(50,60), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R7 = new EvaluateRunnable2(7, 60, pointNum, passengerGroups,
                solutionList.subList(60,70), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R8 = new EvaluateRunnable2(8, 70, pointNum, passengerGroups,
                solutionList.subList(70,80), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R9 = new EvaluateRunnable2(9, 80, pointNum, passengerGroups,
                solutionList.subList(80,90), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);
        EvaluateRunnable2 R10 = new EvaluateRunnable2(10, 90, pointNum, passengerGroups,
                solutionList.subList(90,100), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq);

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
                Thread.sleep(1000*5);
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

        solutionPartSufficiency = null;
        solutionSufficiency = null;
        solutionFrq = tmSolutionFrq;
        solutionPartSufficiency = tmSolutionPartSufficiency;
        solutionSufficiency = tmSolutionSufficiency;//这里只需要存当前解的评价函数

    }
}
