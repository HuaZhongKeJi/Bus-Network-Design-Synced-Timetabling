package com.qz.network;

import com.qz.baseClass.BusLine;
import com.qz.baseClass.PassengerGroup;
import com.qz.baseClass.PassengerRoute;
import com.qz.tool.BaseInfo;
import com.qz.tool.EvaluateRunnable;
import com.qz.tool.MatOperate;

import java.util.*;

public class GA {
    private final int pointNum;
    private final double mortalityRate = 0.15;//淘汰率
    private final int populationSize;//种群规模

    //List<int[pointNum+1]> 一个解，每一行代表一条公交线路，由站点的优先级构成,最后还要加一个终点的优先级
    public static Double b = 0.8;//片段适应度衰减系数
    private Double c1 = 0.1;//基因交叉概率
    private Double c2 = 0.1;//基因交叉概率

    private Double C1 = 0.5;//染色体交叉概率
    private Double C2 = 0.5;//染色体交叉概率

    private Double m1 = 0.5;//变异概率(仅有片段变异)

    private Double m2 = 0.5;//变异概率

    private double rateOfA = 0.5;//后代ABC占比
    private double rateOfB = 0.3;
    private double rateOfC = 0.2;

    private double sumSolutionSufficiency = 0;
    private double avgSolutionSufficiency = 0;
    private double maxSolutionSufficiency = 0;

    private double sumSolutionPartSufficiency = 0;
    private double avgSolutionPartSufficiency = 0;
    private double maxSolutionPartSufficiency = 0;

    private List<List<int[]>> solutionList;//解集
    private List<List<Float>> solutionFrq;//解集
    private List<List<Integer>> unSerPassenger;//解集

    public static List<List<Double>> solutionPartSufficiency;//解集中片段的适应度
    private List<Double> solutionSufficiency;//解的适应度函数
    private final List<List<Integer>> pointReachabilitySet;//站点可达矩阵集合，用于编码和解码

    public List<int[]> getBestSolution() {
        return BestSolution;
    }

    public void setBestSolution(List<int[]> bestSolution) {
        BestSolution = bestSolution;
    }

    public int getPassengerNotAsignRes() {
        return PassengerNotAsignRes;
    }

    public void setPassengerNotAsignRes(int passengerNotAsignRes) {
        PassengerNotAsignRes = passengerNotAsignRes;
    }

    public static int PassengerNotAsignRes;
    public static List<int[]> BestSolution;
    public static List<BusLine> BestBusLine;
    public static Double BestCost = -9999999999999999999999999.0;
    public static float[] bestSoluBRTLaneFeq;

    public static Double getPassengerCost() {
        return PassengerCost;
    }

    public static void setPassengerCost(Double passengerCost) {
        PassengerCost = passengerCost;
    }

    public static Double getOperatorCost() {
        return OperatorCost;
    }

    public static void setOperatorCost(Double operatorCost) {
        OperatorCost = operatorCost;
    }


    public static Double totalTraveltime = 9999999999999999999999999999.0;
    public static Double traveledPassenger = 9999999999999999999999999999.0;
    public static Double unservedPassenger = 9999999999999999999999999999.0;
    public static Double transferNumber = 9999999999999999999999999999.0;
    public static Double routeNumber = 9999999999999999999999999999.0;
    public static Double operatingCost = 9999999999999999999999999999.0;

    public static Double getTotalTraveltime() {
        return totalTraveltime;
    }

    public static void setTotalTraveltime(Double totalTraveltime) {
        GA.totalTraveltime = totalTraveltime;
    }

    public static Double getTraveledPassenger() {
        return traveledPassenger;
    }

    public static void setTraveledPassenger(Double traveledPassenger) {
        GA.traveledPassenger = traveledPassenger;
    }

    public static Double getUnservedPassenger() {
        return unservedPassenger;
    }

    public static void setUnservedPassenger(Double unservedPassenger) {
        GA.unservedPassenger = unservedPassenger;
    }

    public static Double getTransferNumber() {
        return transferNumber;
    }

    public static void setTransferNumber(Double transferNumber) {
        GA.transferNumber = transferNumber;
    }

    public static Double getRouteNumber() {
        return routeNumber;
    }

    public static void setRouteNumber(Double routeNumber) {
        GA.routeNumber = routeNumber;
    }

    public static Double getOperatingCost() {
        return operatingCost;
    }

    public static void setOperatingCost(Double operatingCost) {
        GA.operatingCost = operatingCost;
    }



    public static Double PassengerCost = -9999999999999999999999999.0;
    public static Double OperatorCost = -9999999999999999999999999.0;

    //需要记录没有被用到的片段，着力进行裁剪-----------------------------------------------------------------------------------------------！！！！！！！！！！！！！！！！！！

    public List<BusLine> getBestBusLine() {
        return BestBusLine;
    }

    public double getBestCost() {
        return BestCost;
    }

    public GA(int population_size) {
        solutionList = new ArrayList<>();
        solutionSufficiency = new ArrayList<>();
        pointReachabilitySet = new ArrayList<>();
        solutionPartSufficiency = new ArrayList<>();

        pointNum = BaseInfo.getPointNum();
        populationSize = population_size;
        //生成可达矩阵集合
        int[][] pointDriveTime = BaseInfo.getPointDriveTime();
        for (int i = 0; i < pointNum; i++) {
            List<Integer> tmPointReachability = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                if (pointDriveTime[i][j] < 999) {
                    tmPointReachability.add(j);
                }
            }
            pointReachabilitySet.add(tmPointReachability);
        }

    }

    public void selectSolution() {//根据适应度函数，选择算子,遗传
        MatOperate.QuickSort(solutionSufficiency, 0, solutionSufficiency.size() - 1, solutionList, solutionPartSufficiency, solutionFrq,unSerPassenger);
        int mortalityNum = (int) (mortalityRate * populationSize);
        //double minSuf = solutionSufficiency.get(0);

        List<List<int[]>> newSolutionList = new ArrayList<>();
        List<List<Double>> newSolutionPartSufficiency = new ArrayList<>();
        List<Double> tmsolutionSufficiency = new ArrayList<>();
        List<List<Float>> newSolutionFrq = new ArrayList<>();
        List<List<Integer>> newUnSerPassenger = new ArrayList<>();

        int numOf1 = (int) (0.5 * (populationSize - mortalityNum));//亲代种群中CBA所占比例
        int numOf2 = (int) (0.3 * (populationSize - mortalityNum));
        int numOf3 = (populationSize - mortalityNum) - numOf1 - numOf2;

        double sumSufficiency = 0;

        double sumSufficiency1 = -solutionSufficiency.get(mortalityNum)*numOf1;
        for (int i = mortalityNum; i < (mortalityNum + numOf1); i++) {
            sumSufficiency1 += solutionSufficiency.get(i)+10;
        }

        double sumSufficiency2 = -solutionSufficiency.get(mortalityNum+ numOf1)*numOf2;
        for (int i = (mortalityNum + numOf1); i < (mortalityNum + numOf1 + numOf2); i++) {
            sumSufficiency2 += solutionSufficiency.get(i)+10;
        }

        double sumSufficiency3 = -solutionSufficiency.get(mortalityNum+ numOf1+ numOf2)*numOf3;
        for (int i = (mortalityNum + numOf1 + numOf2); i < populationSize; i++) {
            sumSufficiency3 += solutionSufficiency.get(i)+10;
        }


        sumSolutionSufficiency = 0;
        sumSolutionPartSufficiency = 0;
        maxSolutionSufficiency = -999999999999999999.0;
        maxSolutionPartSufficiency = -999999999999999999.0;
        double tmPartNum = 0.0;


        int numOfA = (int) (rateOfA * populationSize);
        int numOfB = (int) (rateOfB * populationSize);

        //int Best = 0;


        for (int i = 0; i < populationSize; i++) {

            //随机选两个比好坏
            double ram1 = java.lang.Math.random();
            double ram2 = java.lang.Math.random();
            double ram = Math.min(ram1, ram2);

            double tmD = 0;

            int startL = 0;
            int endL = 0;

            double minSuf = 0;

            if (i < numOfA) {

                sumSufficiency = sumSufficiency3;
                minSuf = solutionSufficiency.get(mortalityNum+ numOf1+ numOf2);
                startL = populationSize - 1;
                endL = populationSize - numOf3 - 1;
            } else if (i < numOfA + numOfB) {
                sumSufficiency = sumSufficiency2;
                minSuf = solutionSufficiency.get(mortalityNum+ numOf1);
                startL = populationSize - numOf3 - 1;
                endL = populationSize - numOf3 - numOf2 - 1;
            } else {
                sumSufficiency = sumSufficiency1;
                minSuf = solutionSufficiency.get(mortalityNum);
                startL = populationSize - numOf3 - numOf2 - 1;
                endL = mortalityNum - 1;
            }



            for (int tmdj = startL; tmdj > endL; tmdj--) {

                double tmSolutionSufficiency = solutionSufficiency.get(tmdj);
                tmD += tmSolutionSufficiency-minSuf+10;
                double ts = tmD / sumSufficiency;

//                if(Best<3){
//                    Best ++;
//                    tmdj = startL;
//                    ts = 1;
//                }

                if (ram < ts) {//轮盘赌，选择了j
                    List<int[]> tmSolu = new ArrayList<>();
                    for (int st = 0; st < solutionList.get(tmdj).size(); st++) {
                        int[] tmSolu1 = new int[pointNum + 1];
                        for (int ss = 0; ss < pointNum + 1; ss++) {
                            tmSolu1[ss] = solutionList.get(tmdj).get(st)[ss];
                        }
                        tmSolu.add(tmSolu1);
                    }
                    tmsolutionSufficiency.add(tmSolutionSufficiency);
                    newSolutionList.add(tmSolu);
                    sumSolutionSufficiency += tmSolutionSufficiency;
                    if (tmSolutionSufficiency > maxSolutionSufficiency) {
                        maxSolutionSufficiency = tmSolutionSufficiency;
                    }

                    List<Float> tmSolutionFrq = new ArrayList<>();
                    List<Double> tmSolutionPartSufficiency = new ArrayList<>();
                    List<Integer> tmUnSerPassenger = new ArrayList<>();

                    for (int y = 0; y < unSerPassenger.get(tmdj).size(); y++) {
                        Integer tup = unSerPassenger.get(tmdj).get(y);
                        tmUnSerPassenger.add(tup);
                    }

                    for (int y = 0; y < solutionPartSufficiency.get(tmdj).size(); y++) {
                        Double td = solutionPartSufficiency.get(tmdj).get(y);
                        tmSolutionPartSufficiency.add(td);
                        Float tds = solutionFrq.get(tmdj).get(y);
                        tmSolutionFrq.add(tds);
                    }

                    newUnSerPassenger.add(tmUnSerPassenger);
                    newSolutionPartSufficiency.add(tmSolutionPartSufficiency);
                    newSolutionFrq.add(tmSolutionFrq);
                    tmPartNum += tmSolutionPartSufficiency.size();
                    for (Double aDouble : tmSolutionPartSufficiency) {
                        sumSolutionPartSufficiency += aDouble;
                        if (aDouble > maxSolutionPartSufficiency) {
                            maxSolutionPartSufficiency = aDouble;
                        }
                    }

                    break;
                }
            }

        }

        avgSolutionSufficiency = sumSolutionSufficiency / populationSize;
        avgSolutionPartSufficiency = sumSolutionPartSufficiency / tmPartNum;
        solutionList = null;
        solutionPartSufficiency = null;
        solutionFrq = null;
        unSerPassenger = null;

        unSerPassenger = newUnSerPassenger;
        solutionSufficiency = tmsolutionSufficiency;
        solutionList = newSolutionList;
        solutionPartSufficiency = newSolutionPartSufficiency;
        solutionFrq = newSolutionFrq;
    }

    public void solutionVariation(int loops) {//交叉、变异，整体解的长度交叉变异，子解的变异
        //染色体交叉
        int tsSol = 0;
        int ms1 = 0;
        int ms2 = 0;

        List<Integer> tmL = new ArrayList<>();
        for(int l=0;l<populationSize;l++){
            tmL.add(l);
        }


        while (tmL.size()>0) {

            int tss = (int) (Math.random() * tmL.size());
            int i1 = tmL.remove(tss);

            double Sufficiency = solutionSufficiency.get(i1);
            double Pm = 0;//交叉概率
            if (Sufficiency > avgSolutionSufficiency) {
                Pm = C1 * (maxSolutionSufficiency - Sufficiency) / (maxSolutionSufficiency - avgSolutionSufficiency);
            } else {
                Pm = C2;
            }
            double ramPt2 = Math.random();
            if (ramPt2 < Pm){//染色体交叉
                tsSol ++;
                if(tsSol==1){
                    ms1 = i1;
                }else if(tsSol == 2){
                    ms2 = i1;
                    //普通交叉
                    int Cnum = (int) (Math.random() * BaseInfo.getMinLineNum());
                    for(int ssc = 0;ssc < Cnum;ssc ++){
                        int cs1 = (int) (Math.random() * solutionList.get(ms1).size());
                        int cs2 = (int) (Math.random() * solutionList.get(ms2).size());

                        int[] tmSolu = solutionList.get(ms1).get(cs1);
                        solutionList.get(ms1).set(cs1,solutionList.get(ms2).get(cs2));
                        solutionList.get(ms2).set(cs2,tmSolu);


                        Double tmSolu1 = solutionPartSufficiency.get(ms1).get(cs1);
                        solutionPartSufficiency.get(ms1).set(cs1,solutionPartSufficiency.get(ms2).get(cs2));
                        solutionPartSufficiency.get(ms2).set(cs2,tmSolu1);

                        Float tmSolu2 = solutionFrq.get(ms1).get(cs1);
                        solutionFrq.get(ms1).set(cs1,solutionFrq.get(ms2).get(cs2));
                        solutionFrq.get(ms2).set(cs2,tmSolu2);

                    }
                }
            }
        }



        //子解的交叉与变异
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < solutionPartSufficiency.get(i).size(); j++) {
                double partSufficiency = solutionPartSufficiency.get(i).get(j);
                double Pc = 0;//交叉概率
                if (partSufficiency > avgSolutionPartSufficiency) {
                    Pc = c1 * (maxSolutionPartSufficiency - partSufficiency) / (maxSolutionPartSufficiency - avgSolutionPartSufficiency);
                } else {
                    Pc = c2;
                }
                double ramPt = java.lang.Math.random();
                if (ramPt < Pc) {//需要交叉,随机选择一个解进行交叉，基因组交叉

                    int tVariation = (int) (Math.random() * populationSize);
                    int tVariation2 = (int) (Math.random() * solutionList.get(tVariation).size());

                    Random df = new Random();
                    int ran1 = df.nextInt(pointNum + 1);
                    int ran2 = df.nextInt(pointNum + 1);
                    int tmStart = Math.min(ran1, ran2);
                    int tmEnd = Math.max(ran1, ran2);

                    for (int tm = tmStart; tm <= tmEnd; tm++) {
                        int tmSwInt = solutionList.get(i).get(j)[tm];
                        int tmSwInt2 = solutionList.get(tVariation).get(tVariation2)[tm];
                        int bk = 0;
                        if(tm == pointNum){
                            solutionList.get(i).get(j)[tm] = tmSwInt2;
                            solutionList.get(tVariation).get(tVariation2)[tm] = tmSwInt;
                        }else{
                            for(int sss = 0;sss<pointNum;sss++){
                                if(solutionList.get(i).get(j)[sss] == tmSwInt2){
                                    solutionList.get(i).get(j)[sss] = tmSwInt;
                                    solutionList.get(i).get(j)[tm] = tmSwInt2;
                                    break;
                                }
                            }
                            for(int sss = 0;sss<pointNum;sss++){
                                if(solutionList.get(tVariation).get(tVariation2)[sss] == tmSwInt){
                                    solutionList.get(tVariation).get(tVariation2)[sss] = tmSwInt2;
                                    solutionList.get(tVariation).get(tVariation2)[tm] = tmSwInt;
                                    break;
                                }
                            }
                        }

                    }
                }

            }
        }



        //子解变异
        List<Integer> delInt = new ArrayList<>();
        Random df = new Random();
        for (int i = 0; i < populationSize; i++) {
            double Sufficiency = solutionSufficiency.get(i);
            double Pm = 0;//变异概率
            if (Sufficiency > avgSolutionSufficiency) {
                Pm = m1 * (maxSolutionSufficiency - Sufficiency) / (maxSolutionSufficiency - avgSolutionSufficiency);
            } else {
                Pm = m2;
            }

            double ramPt1 = java.lang.Math.random();//变异位点

            double ramPt2 = java.lang.Math.random();//删除
            double ramPt3 = java.lang.Math.random();//插入（插入为服务乘客起点的线路）

            if (ramPt3 < Pm && solutionList.get(i).size()<BaseInfo.getMaxLineNum()&&unSerPassenger.get(i).size()>0) {//插入一个新解，随机生成的解


                int tmPs = (int) (Math.random() * unSerPassenger.get(i).size());
                int passengerNo = unSerPassenger.get(i).get(tmPs);
                for (PassengerGroup group : BaseInfo.getPassengerGroups()) {
                    if(passengerNo == group.getPassengerGroupID()){
                        List<Integer> startPoint = group.getStartSWPoints();

                        List<Integer> tm1 = new ArrayList<>();

                        for (int is = 1; is < pointNum; is++) {
                            tm1.add(is);
                        }
                        int[] tmSolutionLine = new int[pointNum + 1];
                        for (int k = 0; k <= pointNum; k++) {
                            if(k==startPoint.get(0)){
                                tmSolutionLine[k] = 0;
                            }else if(k==pointNum){
                                tmSolutionLine[k] = df.nextInt(BaseInfo.getMaxLinePointNum() - BaseInfo.getMinLinePointNum() + 1) + BaseInfo.getMinLinePointNum();
                            }else{
                                tmSolutionLine[k] = tm1.remove(df.nextInt(tm1.size()));
                            }
                        }

                        solutionList.get(i).add(tmSolutionLine);
                        solutionPartSufficiency.get(i).add(0.0);
                        solutionFrq.get(i).add(0.5F);

                        break;
                    }
                }
            }

            if (ramPt2 < Pm && solutionList.get(i).size()>BaseInfo.getMinLineNum()) {//删除一个差的解---------------------删除所有未用到的解

                delInt.clear();
                for (int j = 0; j < solutionPartSufficiency.get(i).size(); j++) {

                    if (solutionFrq.get(i).get(j) == 0) {
                        delInt.add(j);
                    }

                }
                if (delInt.size() == 0) {
                    int tmWorst = (int) (Math.random() * solutionPartSufficiency.get(i).size());
                    solutionFrq.get(i).remove(tmWorst);
                    solutionPartSufficiency.get(i).remove(tmWorst);
                    solutionList.get(i).remove(tmWorst);
                } else {
                    solutionFrq.get(i).remove(delInt.size() - 1);
                    solutionPartSufficiency.get(i).remove(delInt.size() - 1);
                    solutionList.get(i).remove(delInt.size() - 1);
                }

            }

            if (ramPt1 < Pm) {//子解变异

                double ramPt12 = java.lang.Math.random();
                List<BusLine> tmBusLine = deCode(solutionList.get(i));
                //随机取一个变异
                int ttVariation = (int) (Math.random() * solutionList.get(i).size());
                boolean swF = false;
                if (ramPt12 > 0.7) {//缩短线路

                    for(int tmpLineNum = 0;tmpLineNum<solutionList.get(i).size()&&!swF;tmpLineNum++){

                        BusLine tBusLine = tmBusLine.get(tmpLineNum+BaseInfo.getBRTNum());
                        List<Integer> RpointList = tBusLine.getPointList();//线路站点集合
                        for(int pN = 0;pN<RpointList.size()-2;pN++){
                            int stop1 = RpointList.get(pN);
                            int stop2 = RpointList.get(pN+1);
                            int stop3 = RpointList.get(pN+2);
                            if(pointReachabilitySet.get(stop1).contains(stop3)){//判断是否存在跨区相连

                                //交换2,3
                                int tmpS3Po = solutionList.get(i).get(tmpLineNum)[stop3];
                                solutionList.get(i).get(tmpLineNum)[stop3] = solutionList.get(i).get(tmpLineNum)[stop2];
                                solutionList.get(i).get(tmpLineNum)[stop2] = tmpS3Po;

                                for(int tmpNumSeq = 0;tmpNumSeq<pointNum;tmpNumSeq++){
                                    if(solutionList.get(i).get(tmpLineNum)[tmpNumSeq] > tmpS3Po){
                                        solutionList.get(i).get(tmpLineNum)[tmpNumSeq] = solutionList.get(i).get(tmpLineNum)[tmpNumSeq]-1;
                                    }
                                }
                                solutionList.get(i).get(tmpLineNum)[stop2] = pointNum-1;
                                solutionList.get(i).get(tmpLineNum)[pointNum] = solutionList.get(i).get(tmpLineNum)[pointNum] - 1;
                                swF = true;
                            }
                        }
                    }

                }
                if(!swF){
                    if (ramPt12 > 0.6) {//变异其中一个点
                        BusLine tBusLine = tmBusLine.get(ttVariation + BaseInfo.getBRTNum());
                        int ttVariation2 = (int) (Math.random() * tBusLine.getPointList().size());
                        int variationPoint = tBusLine.getPointList().get(ttVariation2);
                        //随机选择一个变异位点------variationPoint
                        int variationPoint2 = 0;
                        int variationValue = pointNum-1;//(int) (Math.random() * pointNum);
                        for (int az = 0; az < pointNum; az++) {
                            if (solutionList.get(i).get(ttVariation)[az] == variationValue) {
                                variationPoint2 = az;
                                break;
                            }
                        }
                        int tmSwInt = solutionList.get(i).get(ttVariation)[variationPoint];
                        solutionList.get(i).get(ttVariation)[variationPoint] = solutionList.get(i).get(ttVariation)[variationPoint2];
                        solutionList.get(i).get(ttVariation)[variationPoint2] = tmSwInt;
                    } else if(ramPt12<0.3){//变异长度---------------//并且替换起点
                        double ramPtX = java.lang.Math.random();
                        if(ramPtX>0.5){
                            solutionList.get(i).get(ttVariation)[pointNum] = solutionList.get(i).get(ttVariation)[pointNum] +1;
                        }else{
                            solutionList.get(i).get(ttVariation)[pointNum] = solutionList.get(i).get(ttVariation)[pointNum] -1;
                        }
                    }else{//替换起点并长度减一
                        BusLine tBusLine = tmBusLine.get(ttVariation + BaseInfo.getBRTNum());
                        if(tBusLine.getPointList().size()>1){
                            int variationPoint = tBusLine.getPointList().get(0);
                            int variationPoint2 = tBusLine.getPointList().get(1);
                            int tmSwInt = solutionList.get(i).get(ttVariation)[variationPoint];
                            solutionList.get(i).get(ttVariation)[variationPoint] = solutionList.get(i).get(ttVariation)[variationPoint2];
                            solutionList.get(i).get(ttVariation)[variationPoint2] = tmSwInt;

                            solutionList.get(i).get(ttVariation)[pointNum] = solutionList.get(i).get(ttVariation)[pointNum]-1;
                        }

                    }
                }

                tmBusLine = null;
            }

        }
        df = null;



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
            List<int[]> tmSolution = new ArrayList<>();
            List<Double> tmSolutionSufficiency = new ArrayList<>();

            Random random = new Random();
            int randomNumber = random.nextInt(BaseInfo.getMaxLineNum() - BaseInfo.getMinLineNum() + 1) + BaseInfo.getMinLineNum();

            for (int j = 0; j < randomNumber; j++) {//------------------------------------------------------------
            //for (int j = 0; j < BaseInfo.getMaxLineNum(); j++) {//------------------------------------------------------------
                int[] tmSolutionLine = new int[pointNum + 1];
                for (int k = 0; k <= pointNum; k++) {

                    Random df = new Random();
                    if (k == pointNum) {
                        tmSolutionLine[k] = df.nextInt(BaseInfo.getMaxLinePointNum() - BaseInfo.getMinLinePointNum() + 1) + BaseInfo.getMinLinePointNum();
                    } else {
                        if (tmflag) {
                            tmSolutionLine[k] = tm1.remove(df.nextInt(tm1.size()));
                            tm2.add(k);
                        } else {
                            tmSolutionLine[k] = tm2.remove(df.nextInt(tm2.size()));
                            tm1.add(k);
                        }
                    }
                }
                tmflag = !tmflag;
                tmSolution.add(tmSolutionLine);
                tmSolutionSufficiency.add(0.0);
            }//------------------------------------------------------------
            solutionList.add(tmSolution);
            solutionSufficiency.add(0.0);
            solutionPartSufficiency.add(tmSolutionSufficiency);
        }


        List<int[]> tmSolution = new ArrayList<>();
        int[] tmSolutionLine  = new int[]{27, 20, 69, 47, 78, 33, 26, 31, 25, 38, 39, 18, 90, 1, 85, 5, 84, 63, 64, 41, 35, 17, 55, 60, 36, 75, 89, 6, 57, 42, 76, 21, 82, 11, 15, 80, 62, 2, 52, 73, 54, 46, 71, 19, 83, 56, 58, 9, 88, 4, 51, 28, 53, 44, 32, 14, 66, 68, 48, 8, 34, 72, 92, 87, 13, 86, 29, 3, 49, 30, 67, 81, 59, 45, 23, 22, 79, 24, 37, 40, 7, 43, 91, 65, 77, 61, 10, 12, 16, 74, 50, 0, 70, 13};
        int[] tmSolutionLine2  = new int[]{};
        int[] tmSolutionLine3  = new int[]{};
        int[] tmSolutionLine4  = new int[]{};
        int[] tmSolutionLine5  = new int[]{};
        int[] tmSolutionLine6  = new int[]{};
        int[] tmSolutionLine7  = new int[]{};
        int[] tmSolutionLine8  = new int[]{};
        int[] tmSolutionLine9  = new int[]{};
        int[] tmSolutionLine10  = new int[]{};
        int[] tmSolutionLine11  = new int[]{};
        int[] tmSolutionLine12  = new int[]{};
        int[] tmSolutionLine13  = new int[]{};
        int[] tmSolutionLine14  = new int[]{};


        tmSolution.add(tmSolutionLine);
        tmSolution.add(tmSolutionLine2);
        tmSolution.add(tmSolutionLine3);
        tmSolution.add(tmSolutionLine4);
        tmSolution.add(tmSolutionLine5);
        tmSolution.add(tmSolutionLine6);
        tmSolution.add(tmSolutionLine7);
        tmSolution.add(tmSolutionLine8);
        tmSolution.add(tmSolutionLine9);
        tmSolution.add(tmSolutionLine10);
        tmSolution.add(tmSolutionLine11);
        tmSolution.add(tmSolutionLine12);
        tmSolution.add(tmSolutionLine13);
        tmSolution.add(tmSolutionLine14);


        List<Double> tmSolutionSufficiency = new ArrayList<>();
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);
        tmSolutionSufficiency.add(0.0);


        solutionPartSufficiency.set(0,tmSolutionSufficiency);
        solutionList.set(0,tmSolution);


        //原始线网
//        List<int[]> tmSolution = new ArrayList<>();
//        int[] tmSolutionLine  = new int[]{10,11,12,13,14,15,16,17,18,19,20,21,8,7,6,22,23,24,25,26,27,28,29,30,31,32,33,34,1,0,35,36,37,38,39,40,41,42,43,44,45,46,2,47,48,49,50,51,52,53,54,3,55,56,57,58,59,60,9,61,4,5,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,10};
//        int[] tmSolutionLine2 = new int[]{15,16,17,18,19,20,21,22,23,24,25,26,27,28,7,8,9,10,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,0,50,51,52,53,54,55,1,2,56,57,58,59,60,3,61,62,63,64,4,65,66,67,68,6,5,69,70,71,72,73,74,75,76,77,11,78,79,80,13,81,14,82,83,84,85,86,87,88,89,90,91,92,12,15};
//        int[] tmSolutionLine3 = new int[]{24,25,26,27,28,29,15,30,9,8,7,6,5,4,31,22,23,1,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,13,14,16,50,11,12,51,52,53,54,55,17,10,56,57,58,59,18,60,61,62,63,19,64,65,66,3,21,20,67,68,69,70,71,72,73,74,75,0,76,77,78,79,80,81,82,83,84,85,86,87,88,89,2,90,91,92,24};
//        int[] tmSolutionLine4 = new int[]{17,18,19,20,21,22,23,24,5,6,7,8,9,10,11,12,13,14,25,26,27,0,28,29,30,31,32,1,33,34,35,36,37,38,39,40,2,41,42,43,44,3,45,46,47,48,49,50,4,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,15,73,74,16,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,17};
//        int[] tmSolutionLine5 = new int[]{17,18,19,20,21,22,23,24,4,5,6,7,8,9,10,11,12,13,25,26,27,28,29,30,31,32,33,34,0,35,36,37,38,39,40,41,42,43,44,45,46,2,1,47,48,49,50,51,3,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,15,14,71,72,73,74,16,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,17};
//        int[] tmSolutionLine6 = new int[]{13,14,15,16,17,18,19,20,21,22,5,6,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,0,41,42,43,44,45,46,47,48,1,49,50,51,52,53,54,2,3,55,56,57,58,59,4,60,7,61,62,63,64,65,9,8,66,67,68,69,70,71,72,73,10,74,11,75,76,77,78,12,79,80,81,82,83,84,85,86,87,88,89,90,91,92,13};
//        int[] tmSolutionLine7 = new int[]{19,20,21,22,23,24,25,4,5,6,7,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,0,1,50,51,52,3,2,53,54,55,56,57,58,59,60,61,62,63,8,64,10,65,66,9,67,68,69,11,70,71,72,73,74,75,76,13,77,78,85,14,79,16,15,80,81,82,83,84,17,18,86,87,88,89,12,90,91,92,19};
//        int[] tmSolutionLine8 = new int[]{12,13,14,15,16,17,18,19,20,21,22,23,24,25,6,7,8,9,26,27,28,29,30,31,32,33,34,35,36,0,37,38,39,40,41,42,43,44,45,46,47,48,49,1,50,51,52,53,54,55,56,3,2,57,58,59,60,61,62,63,4,5,64,65,66,67,68,69,70,71,72,73,74,75,10,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,11,12};
//        int[] tmSolutionLine9 = new int[]{6,7,8,9,5,4,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,0,25,26,27,28,29,1,30,31,32,33,34,35,36,37,2,3,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,6};
//        int[] tmSolutionLine10 = new int[]{6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,0,76,77,78,1,79,3,2,80,81,82,83,84,4,5,85,86,87,88,89,90,91,92,6};
//        int[] tmSolutionLine11 = new int[]{14,15,16,17,18,19,20,21,22,23,24,25,26,27,9,10,28,29,30,31,32,0,33,34,35,36,37,1,38,39,40,41,42,43,44,45,2,46,47,48,4,3,49,50,51,52,53,54,5,6,55,56,57,58,59,60,61,62,63,7,64,8,65,11,12,66,67,68,13,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,14};
//        int[] tmSolutionLine12 = new int[]{18,19,20,21,0,1,2,22,6,7,8,9,10,11,12,13,14,15,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,3,42,43,4,44,45,46,47,48,49,50,5,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,16,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,17,18};
//        int[] tmSolutionLine13 = new int[]{13,14,15,16,17,18,19,20,21,22,2,3,4,5,6,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,1,61,62,63,64,0,65,8,7,66,67,68,69,70,71,72,73,74,75,9,76,77,78,79,80,81,82,10,83,84,85,86,87,88,11,12,89,90,91,92,13};
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //1号结果
//        List<int[]> tmSolution = new ArrayList<>();
//        int[] tmSolutionLine  = new int[]{35, 52, 21, 50, 2, 16, 63, 69, 11, 55, 65, 59, 68, 89, 30, 29, 53, 10, 60, 44, 19, 54, 7, 1, 84, 76, 92, 12, 27, 74, 51, 4, 6, 25, 62, 38, 3, 70, 37, 40, 20, 8, 72, 82, 0, 67, 83, 26, 81, 41, 45, 9, 18, 42, 15, 48, 33, 49, 23, 14, 43, 17, 80, 39, 87, 88, 36, 46, 71, 36, 5, 91, 77, 58, 24, 73, 28, 13, 47, 66, 31, 32, 90, 22, 78, 57, 86, 61, 75, 56, 34, 79, 64, 11};
//        int[] tmSolutionLine2  = new int[]{12, 47, 12, 25, 35, 2, 51, 61, 80, 58, 65, 27, 23, 74, 78, 21, 33, 90, 41, 46, 42, 16, 6, 7, 14, 43, 24, 37, 57, 30, 85, 64, 19, 79, 31, 49, 53, 67, 48, 56, 83, 32, 63, 11, 34, 60, 59, 39, 29, 0, 45, 44, 84, 92, 1, 82, 68, 55, 75, 69, 8, 77, 91, 40, 89, 54, 3, 70, 28, 26, 71, 10, 4, 20, 87, 38, 72, 14, 9, 76, 81, 62, 88, 5, 22, 13, 15, 86, 17, 52, 66, 50, 36, 7};
//        int[] tmSolutionLine3  = new int[]{60, 84, 53, 3, 19, 10, 72, 12, 74, 8, 33, 78, 43, 39, 67, 11, 88, 18, 64, 40, 28, 45, 6, 66, 4, 47, 59, 5, 81, 87, 17, 75, 73, 80, 52, 30, 89, 23, 83, 7, 69, 86, 14, 32, 22, 9, 49, 62, 26, 63, 82, 15, 50, 68, 55, 42, 20, 56, 37, 51, 21, 91, 65, 0, 34, 70, 27, 44, 48, 29, 31, 54, 1, 57, 24, 25, 71, 13, 16, 61, 41, 77, 36, 79, 58, 2, 92, 85, 46, 35, 38, 90, 76, 8};
//        int[] tmSolutionLine4  = new int[]{57, 1, 64, 43, 85, 67, 78, 86, 83, 58, 69, 13, 91, 55, 44, 3, 79, 73, 7, 88, 72, 89, 6, 33, 21, 35, 38, 87, 16, 28, 42, 66, 74, 23, 92, 15, 63, 41, 31, 65, 49, 20, 39, 60, 40, 11, 26, 76, 52, 0, 45, 51, 32, 2, 18, 37, 12, 77, 19, 14, 54, 25, 80, 48, 47, 17, 56, 34, 61, 36, 81, 29, 30, 27, 75, 9, 71, 84, 8, 90, 24, 62, 46, 5, 22, 82, 59, 10, 53, 70, 4, 50, 68, 7};
//        int[] tmSolutionLine5  = new int[]{56, 17, 21, 50, 66, 1, 63, 82, 11, 55, 65, 59, 8, 89, 30, 29, 53, 84, 60, 44, 19, 54, 15, 62, 10, 76, 92, 12, 27, 74, 51, 4, 6, 25, 20, 38, 3, 70, 37, 40, 72, 68, 34, 69, 41, 67, 83, 26, 81, 0, 45, 9, 18, 42, 75, 48, 33, 49, 47, 32, 80, 28, 23, 57, 87, 88, 52, 7, 71, 22, 5, 78, 73, 39, 24, 31, 91, 13, 16, 86, 14, 77, 36, 61, 58, 2, 79, 85, 46, 35, 64, 43, 90, 6};
//        int[] tmSolutionLine6  = new int[]{20, 48, 22, 41, 16, 80, 47, 86, 83, 58, 87, 19, 91, 55, 44, 3, 76, 73, 56, 79, 34, 57, 23, 67, 27, 85, 37, 54, 78, 60, 66, 29, 74, 63, 25, 9, 72, 4, 62, 50, 11, 88, 30, 61, 53, 7, 42, 49, 26, 0, 45, 43, 36, 65, 18, 14, 40, 46, 10, 64, 52, 31, 35, 51, 5, 38, 15, 2, 32, 13, 28, 90, 81, 8, 1, 70, 6, 39, 59, 33, 21, 92, 69, 71, 12, 84, 82, 68, 75, 89, 77, 24, 17, 6};
//        int[] tmSolutionLine7  = new int[]{22, 50, 11, 37, 56, 91, 17, 55, 86, 65, 21, 64, 59, 90, 2, 8, 14, 53, 29, 44, 13, 54, 89, 62, 15, 76, 43, 12, 60, 74, 51, 4, 6, 25, 20, 38, 3, 19, 92, 40, 72, 68, 34, 69, 41, 67, 83, 26, 81, 0, 45, 9, 18, 42, 75, 48, 33, 49, 47, 32, 80, 28, 23, 57, 87, 88, 52, 7, 71, 36, 5, 78, 77, 39, 24, 79, 85, 31, 58, 66, 16, 73, 27, 70, 35, 84, 82, 61, 10, 30, 63, 1, 46, 4};
//        int[] tmSolutionLine8  = new int[]{20, 23, 87, 62, 45, 83, 1, 8, 3, 73, 5, 58, 75, 11, 2, 63, 33, 90, 41, 46, 42, 65, 44, 7, 14, 16, 24, 37, 57, 30, 85, 64, 6, 68, 86, 55, 27, 89, 66, 28, 49, 92, 51, 79, 80, 13, 78, 70, 50, 10, 84, 26, 39, 91, 74, 67, 60, 31, 19, 76, 25, 59, 18, 48, 47, 17, 56, 34, 61, 36, 81, 29, 52, 88, 53, 40, 35, 15, 9, 0, 72, 54, 43, 38, 69, 22, 82, 71, 4, 21, 32, 77, 16, 12};
//        int[] tmSolutionLine9  = new int[]{45, 86, 23, 44, 88, 8, 65, 56, 38, 34, 40, 66, 63, 4, 57, 21, 36, 11, 2, 54, 42, 28, 49, 10, 33, 50, 68, 55, 27, 20, 18, 12, 26, 15, 31, 71, 79, 74, 72, 52, 73, 3, 85, 14, 60, 35, 84, 16, 0, 90, 37, 46, 9, 77, 1, 82, 32, 76, 75, 78, 70, 22, 48, 69, 5, 87, 92, 91, 19, 13, 67, 41, 81, 7, 47, 89, 6, 39, 18, 43, 25, 53, 59, 17, 80, 61, 24, 62, 30, 83, 64, 51, 58, 6};
//        int[] tmSolutionLine10  = new int[]{30, 35, 51, 44, 80, 8, 65, 56, 38, 34, 40, 4, 43, 39, 67, 21, 29, 11, 2, 46, 42, 28, 92, 10, 33, 50, 68, 64, 27, 78, 53, 72, 19, 79, 31, 49, 6, 52, 76, 15, 90, 32, 63, 3, 0, 59, 16, 13, 26, 47, 45, 74, 18, 9, 7, 48, 70, 22, 73, 14, 20, 25, 23, 91, 87, 69, 81, 83, 71, 36, 60, 37, 77, 41, 24, 55, 58, 57, 86, 66, 54, 62, 88, 5, 12, 84, 82, 61, 75, 89, 85, 1, 17, 6};
//        int[] tmSolutionLine11  = new int[]{38, 20, 88, 16, 31, 37, 55, 22, 47, 27, 5, 58, 75, 30, 2, 63, 13, 14, 45, 51, 82, 11, 76, 43, 21, 90, 12, 4, 81, 60, 68, 46, 26, 15, 52, 64, 25, 17, 3, 53, 65, 6, 67, 54, 59, 23, 10, 42, 49, 0, 91, 80, 69, 44, 66, 9, 78, 8, 32, 19, 86, 57, 34, 70, 71, 72, 28, 87, 35, 41, 92, 24, 84, 89, 79, 73, 83, 74, 18, 50, 7, 62, 77, 48, 85, 1, 61, 29, 39, 33, 40, 56, 36, 9};
//        int[] tmSolutionLine12  = new int[]{23, 44, 79, 16, 19, 1, 73, 22, 48, 38, 10, 57, 65, 62, 91, 63, 13, 61, 31, 47, 77, 90, 6, 33, 21, 12, 29, 5, 81, 83, 0, 46, 76, 80, 52, 60, 86, 74, 3, 7, 32, 43, 67, 78, 27, 11, 36, 2, 30, 59, 82, 15, 50, 68, 42, 8, 20, 56, 37, 9, 24, 58, 39, 51, 71, 72, 28, 87, 35, 41, 92, 70, 84, 89, 17, 75, 55, 49, 18, 69, 88, 54, 26, 45, 85, 4, 14, 34, 66, 64, 25, 40, 53, 9};
//        int[] tmSolutionLine13  = new int[]{11, 38, 39, 76, 27, 72, 14, 83, 4, 70, 24, 0, 81, 82, 18, 87, 73, 77, 40, 59, 44, 43, 13, 62, 55, 65, 90, 29, 66, 74, 85, 64, 19, 79, 31, 49, 53, 67, 57, 28, 7, 61, 41, 48, 9, 75, 51, 91, 26, 88, 32, 23, 5, 16, 71, 46, 6, 15, 92, 20, 60, 45, 21, 42, 22, 35, 25, 10, 86, 30, 50, 80, 1, 58, 78, 52, 47, 34, 36, 56, 12, 63, 89, 37, 54, 3, 68, 8, 69, 84, 33, 17, 2, 12};
//        int[] tmSolutionLine14  = new int[]{24, 92, 23, 8, 22, 60, 5, 51, 56, 44, 31, 45, 43, 72, 67, 1, 29, 4, 2, 46, 42, 28, 21, 10, 33, 50, 68, 75, 27, 78, 85, 39, 19, 79, 53, 49, 6, 11, 64, 17, 35, 83, 81, 37, 86, 14, 9, 77, 36, 40, 82, 25, 57, 55, 20, 26, 3, 63, 89, 7, 13, 84, 34, 52, 38, 87, 0, 30, 71, 74, 73, 16, 58, 66, 54, 91, 69, 80, 70, 76, 90, 62, 18, 65, 88, 12, 47, 15, 41, 59, 61, 48, 32, 6};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //2号结果
//        List<int[]> tmSolution = new ArrayList<>();
//        int[] tmSolutionLine  = new int[]{19, 75, 42, 25, 72, 46, 48, 80, 53, 71, 16, 78, 40, 85, 20, 32, 69, 36, 7, 8, 31, 74, 90, 79, 38, 62, 15, 35, 81, 0, 65, 88, 26, 56, 41, 14, 83, 82, 73, 45, 58, 84, 13, 4, 68, 87, 55, 21, 59, 91, 28, 37, 77, 22, 2, 86, 43, 50, 29, 47, 9, 61, 18, 67, 16, 27, 12, 64, 87, 5, 10, 70, 11, 89, 51, 92, 3, 39, 57, 44, 33, 66, 30, 1, 6, 60, 17, 23, 24, 52, 54, 63, 76, 9};
//        int[] tmSolutionLine2  = new int[]{30, 6, 24, 69, 78, 92, 89, 33, 18, 71, 29, 45, 50, 80, 0, 87, 91, 22, 62, 21, 7, 11, 25, 88, 56, 8, 65, 74, 60, 4, 64, 82, 72, 41, 9, 66, 55, 84, 51, 5, 83, 12, 59, 44, 32, 17, 47, 28, 90, 79, 36, 10, 3, 14, 39, 19, 40, 67, 86, 61, 23, 15, 70, 49, 48, 77, 13, 57, 73, 1, 2, 76, 68, 38, 42, 52, 85, 27, 81, 43, 54, 31, 26, 58, 20, 46, 34, 75, 37, 53, 63, 16, 35, 6};
//        int[] tmSolutionLine3  = new int[]{4, 26, 50, 58, 35, 48, 5, 6, 28, 89, 31, 12, 74, 53, 24, 66, 10, 21, 46, 72, 70, 78, 75, 85, 36, 90, 71, 0, 33, 25, 73, 1, 62, 81, 61, 30, 8, 51, 14, 19, 83, 49, 20, 40, 47, 34, 7, 18, 54, 86, 13, 65, 37, 63, 29, 80, 56, 59, 76, 23, 3, 44, 82, 11, 45, 69, 77, 22, 42, 55, 88, 15, 16, 84, 17, 79, 43, 87, 2, 39, 92, 52, 38, 27, 57, 67, 32, 60, 9, 64, 68, 91, 41, 10};
//        int[] tmSolutionLine4  = new int[]{56, 3, 79, 78, 88, 73, 45, 62, 60, 44, 31, 63, 6, 64, 12, 77, 28, 86, 46, 72, 29, 41, 4, 54, 16, 1, 69, 18, 81, 91, 65, 21, 36, 30, 84, 71, 37, 40, 50, 90, 13, 15, 26, 8, 55, 48, 49, 83, 51, 76, 14, 59, 68, 19, 58, 53, 85, 47, 0, 25, 74, 20, 75, 82, 66, 43, 80, 17, 87, 5, 35, 70, 61, 89, 11, 92, 10, 39, 57, 23, 24, 52, 38, 27, 2, 67, 32, 22, 9, 7, 42, 34, 33, 8};
//        int[] tmSolutionLine5  = new int[]{53, 10, 63, 72, 78, 21, 36, 34, 22, 80, 48, 21, 12, 20, 74, 59, 6, 67, 32, 13, 71, 75, 3, 85, 7, 90, 35, 92, 33, 24, 89, 56, 26, 31, 82, 60, 79, 73, 86, 45, 18, 27, 42, 58, 87, 41, 49, 16, 54, 5, 62, 76, 51, 38, 43, 61, 39, 1, 65, 0, 44, 28, 83, 47, 29, 70, 17, 37, 77, 64, 46, 57, 88, 4, 23, 30, 11, 19, 50, 81, 15, 9, 84, 55, 2, 3, 66, 91, 68, 69, 40, 8, 52, 4};
//        int[] tmSolutionLine6  = new int[]{44, 86, 31, 69, 8, 24, 49, 21, 36, 39, 76, 82, 88, 5, 11, 77, 43, 45, 81, 13, 71, 75, 3, 85, 7, 90, 35, 42, 33, 89, 78, 84, 26, 48, 61, 14, 80, 0, 46, 16, 18, 70, 22, 79, 1, 19, 23, 4, 54, 62, 40, 83, 37, 20, 92, 53, 28, 63, 25, 11, 12, 56, 65, 47, 66, 15, 34, 41, 2, 51, 72, 30, 6, 50, 73, 55, 59, 29, 17, 58, 87, 52, 74, 27, 57, 67, 32, 60, 9, 64, 68, 91, 38, 10};
//        int[] tmSolutionLine7  = new int[]{33, 41, 0, 60, 49, 87, 72, 14, 2, 40, 69, 10, 5, 71, 55, 78, 32, 31, 8, 36, 35, 11, 57, 12, 38, 62, 34, 18, 39, 91, 21, 1, 63, 13, 80, 27, 68, 44, 46, 47, 82, 53, 79, 90, 26, 81, 7, 18, 66, 86, 9, 74, 61, 23, 77, 51, 58, 22, 3, 59, 75, 4, 88, 85, 19, 84, 64, 67, 48, 92, 73, 30, 45, 17, 25, 83, 37, 56, 50, 6, 54, 29, 28, 42, 43, 15, 70, 24, 52, 89, 76, 20, 65, 10};
//        int[] tmSolutionLine8  = new int[]{35, 24, 89, 69, 8, 16, 20, 82, 92, 71, 13, 78, 60, 67, 75, 72, 52, 66, 9, 73, 30, 77, 33, 25, 38, 62, 34, 18, 57, 17, 65, 47, 26, 48, 61, 14, 68, 51, 86, 45, 7, 70, 91, 40, 83, 90, 55, 41, 11, 0, 31, 37, 74, 64, 2, 80, 1, 15, 22, 4, 63, 85, 50, 21, 36, 27, 12, 19, 10, 5, 46, 84, 88, 39, 32, 53, 3, 44, 81, 6, 54, 29, 28, 42, 43, 76, 56, 58, 49, 23, 87, 79, 59, 9};
//        int[] tmSolutionLine9  = new int[]{77, 9, 44, 92, 42, 4, 72, 14, 2, 40, 69, 86, 56, 71, 20, 78, 80, 1, 8, 36, 68, 11, 35, 12, 48, 62, 76, 18, 81, 91, 70, 21, 61, 84, 33, 55, 37, 5, 50, 89, 13, 45, 26, 46, 41, 67, 49, 75, 15, 58, 7, 59, 6, 19, 87, 53, 85, 39, 22, 10, 74, 57, 65, 47, 66, 27, 34, 38, 79, 51, 88, 30, 83, 31, 0, 28, 29, 32, 23, 90, 16, 73, 3, 82, 64, 60, 17, 52, 24, 43, 54, 63, 25, 8};
//        int[] tmSolutionLine10  = new int[]{83, 49, 59, 56, 62, 8, 66, 61, 71, 18, 29, 78, 60, 51, 55, 34, 91, 23, 7, 72, 11, 90, 37, 88, 67, 45, 47, 9, 24, 19, 5, 84, 4, 33, 82, 17, 63, 30, 69, 27, 44, 35, 40, 3, 41, 86, 14, 75, 36, 0, 28, 74, 6, 64, 92, 80, 15, 39, 22, 10, 12, 89, 65, 21, 48, 32, 13, 57, 73, 1, 2, 76, 68, 38, 42, 52, 85, 77, 81, 43, 54, 31, 26, 58, 20, 46, 50, 16, 25, 70, 87, 53, 79, 4};
//        int[] tmSolutionLine11  = new int[]{44, 86, 31, 69, 8, 71, 20, 63, 57, 16, 29, 38, 60, 67, 54, 72, 52, 66, 9, 73, 30, 77, 33, 25, 78, 62, 34, 18, 81, 17, 65, 80, 26, 48, 47, 14, 68, 56, 46, 58, 84, 88, 74, 12, 41, 21, 27, 13, 50, 0, 89, 75, 6, 7, 87, 51, 85, 39, 22, 10, 92, 28, 83, 61, 70, 90, 91, 37, 3, 64, 32, 43, 59, 45, 42, 24, 5, 19, 4, 11, 53, 76, 23, 15, 49, 1, 36, 82, 2, 79, 40, 55, 35, 4};
//        int[] tmSolutionLine12  = new int[]{45, 35, 14, 44, 62, 8, 31, 61, 71, 18, 29, 66, 60, 9, 64, 26, 23, 86, 39, 75, 89, 15, 1, 22, 73, 38, 11, 42, 87, 85, 80, 90, 49, 84, 13, 55, 53, 12, 72, 16, 4, 59, 58, 52, 21, 34, 36, 32, 77, 47, 43, 20, 17, 6, 3, 82, 63, 79, 40, 51, 74, 69, 10, 91, 24, 2, 19, 46, 33, 5, 57, 68, 88, 76, 25, 83, 37, 56, 50, 54, 70, 30, 48, 28, 65, 0, 41, 78, 81, 67, 7, 92, 27, 11};
//        int[] tmSolutionLine13  = new int[]{24, 78, 40, 82, 48, 87, 8, 69, 89, 6, 71, 72, 0, 91, 86, 57, 55, 61, 14, 38, 26, 37, 44, 79, 75, 39, 3, 17, 33, 67, 62, 84, 63, 13, 80, 90, 76, 56, 46, 60, 18, 68, 52, 88, 1, 31, 50, 35, 59, 74, 34, 43, 4, 20, 77, 51, 58, 15, 32, 25, 16, 70, 83, 10, 27, 53, 47, 21, 54, 41, 36, 28, 81, 45, 42, 30, 5, 19, 2, 11, 92, 7, 23, 85, 49, 73, 22, 64, 66, 29, 12, 65, 9, 6};
//        int[] tmSolutionLine14  = new int[]{78, 13, 80, 25, 72, 89, 86, 31, 88, 16, 65, 38, 0, 12, 35, 39, 37, 53, 30, 36, 47, 33, 19, 77, 32, 45, 18, 34, 43, 44, 57, 12, 20, 14, 28, 42, 62, 85, 84, 63, 4, 1, 83, 23, 21, 46, 66, 76, 82, 60, 81, 75, 6, 7, 54, 61, 68, 71, 40, 51, 74, 69, 10, 91, 24, 2, 27, 49, 26, 92, 55, 5, 52, 50, 73, 64, 59, 29, 17, 58, 87, 22, 41, 15, 8, 79, 48, 67, 70, 56, 3, 11, 90, 9};
//        int[] tmSolutionLine15  = new int[]{56, 57, 79, 87, 32, 73, 45, 62, 83, 11, 77, 35, 61, 59, 27, 33, 28, 31, 48, 72, 24, 67, 89, 58, 16, 53, 6, 18, 14, 91, 65, 55, 63, 13, 80, 50, 68, 22, 46, 15, 9, 1, 39, 90, 92, 44, 76, 38, 26, 43, 78, 37, 70, 34, 52, 74, 21, 7, 20, 8, 23, 81, 54, 29, 84, 36, 0, 88, 64, 86, 82, 25, 60, 19, 17, 3, 47, 2, 12, 49, 71, 42, 30, 4, 69, 10, 40, 5, 66, 85, 75, 51, 41, 5};
//        int[] tmSolutionLine16  = new int[]{3, 87, 11, 58, 26, 75, 35, 50, 0, 13, 7, 84, 59, 67, 88, 90, 52, 66, 9, 73, 57, 77, 33, 12, 38, 62, 34, 18, 76, 72, 61, 36, 60, 41, 68, 39, 64, 8, 4, 83, 56, 48, 31, 78, 37, 27, 80, 14, 1, 79, 55, 5, 49, 23, 91, 71, 63, 53, 40, 51, 74, 69, 10, 92, 32, 2, 19, 22, 86, 16, 46, 30, 45, 17, 24, 82, 85, 44, 81, 6, 54, 29, 28, 42, 43, 15, 70, 25, 47, 89, 65, 20, 21, 5};
//        int[] tmSolutionLine17  = new int[]{64, 2, 85, 38, 77, 84, 67, 56, 92, 22, 61, 89, 28, 58, 42, 49, 52, 20, 62, 75, 54, 24, 51, 32, 83, 14, 34, 33, 37, 11, 1, 17, 40, 13, 80, 27, 68, 44, 46, 47, 82, 53, 79, 90, 26, 81, 7, 18, 66, 86, 50, 59, 35, 55, 45, 74, 88, 60, 0, 12, 39, 30, 3, 57, 48, 63, 65, 31, 69, 10, 8, 21, 5, 36, 9, 43, 6, 4, 91, 71, 19, 29, 15, 87, 76, 25, 78, 73, 23, 72, 16, 41, 70, 4};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//        tmSolution.add(tmSolutionLine15);
//        tmSolution.add(tmSolutionLine16);
//        tmSolution.add(tmSolutionLine17);
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //3号结果
//        List<int[]> tmSolution = new ArrayList<>();
//        int[] tmSolutionLine  = new int[]{1, 79, 25, 22, 68, 57, 44, 78, 35, 46, 19, 50, 59, 5, 61, 54, 40, 17, 81, 86, 33, 72, 7, 45, 51, 36, 65, 85, 43, 76, 12, 27, 66, 77, 63, 92, 84, 3, 38, 74, 58, 32, 53, 37, 42, 73, 10, 21, 90, 8, 89, 88, 20, 13, 91, 14, 70, 24, 48, 4, 31, 0, 28, 29, 67, 39, 18, 75, 87, 83, 64, 71, 34, 62, 6, 11, 55, 49, 26, 15, 52, 80, 2, 60, 41, 9, 23, 16, 47, 56, 82, 69, 30, 9};
//        int[] tmSolutionLine2  = new int[]{80, 13, 52, 28, 54, 14, 48, 0, 82, 12, 38, 43, 19, 36, 84, 10, 72, 75, 74, 64, 85, 77, 31, 23, 63, 44, 57, 78, 83, 76, 70, 60, 4, 21, 3, 45, 11, 17, 81, 68, 27, 9, 22, 59, 58, 18, 46, 33, 47, 5, 61, 50, 15, 35, 24, 6, 1, 30, 37, 16, 62, 20, 40, 29, 67, 39, 92, 66, 87, 49, 55, 41, 73, 90, 34, 2, 42, 8, 89, 7, 53, 51, 25, 56, 26, 91, 88, 69, 79, 32, 65, 86, 71, 4};
//        int[] tmSolutionLine3  = new int[]{81, 9, 50, 28, 73, 18, 17, 42, 58, 40, 72, 22, 59, 56, 76, 1, 2, 46, 74, 64, 85, 77, 75, 23, 63, 60, 65, 33, 21, 11, 82, 4, 90, 68, 7, 49, 19, 57, 52, 14, 32, 30, 26, 89, 48, 24, 43, 84, 78, 38, 61, 80, 0, 13, 87, 66, 83, 37, 36, 41, 12, 91, 5, 79, 53, 3, 9, 45, 34, 44, 6, 62, 16, 51, 69, 70, 29, 13, 71, 86, 15, 35, 88, 25, 55, 92, 27, 10, 47, 39, 31, 20, 54, 8};
//        int[] tmSolutionLine4  = new int[]{13, 33, 44, 56, 0, 9, 54, 53, 28, 4, 70, 64, 67, 47, 82, 8, 74, 55, 21, 83, 72, 92, 7, 38, 35, 25, 17, 31, 49, 87, 79, 19, 80, 34, 57, 1, 52, 32, 50, 68, 37, 30, 26, 89, 71, 48, 43, 84, 78, 85, 15, 11, 23, 46, 42, 36, 88, 24, 63, 16, 22, 51, 41, 69, 14, 2, 65, 60, 61, 20, 90, 29, 73, 12, 77, 81, 91, 3, 76, 66, 45, 75, 6, 5, 62, 39, 27, 10, 59, 40, 18, 86, 58, 9};
//        int[] tmSolutionLine5  = new int[]{9, 10, 85, 42, 68, 57, 75, 27, 39, 58, 13, 41, 8, 69, 56, 18, 6, 31, 2, 17, 38, 66, 83, 76, 24, 53, 65, 33, 21, 19, 82, 4, 90, 34, 7, 49, 28, 73, 52, 63, 32, 30, 26, 89, 14, 48, 43, 84, 78, 37, 71, 62, 11, 46, 1, 36, 88, 5, 54, 61, 44, 35, 91, 15, 3, 47, 80, 23, 22, 77, 67, 79, 72, 87, 60, 55, 59, 0, 16, 40, 29, 20, 81, 50, 74, 92, 12, 51, 86, 64, 17, 45, 25, 15};
//        int[] tmSolutionLine6  = new int[]{34, 25, 90, 24, 52, 79, 53, 23, 68, 67, 26, 73, 9, 61, 76, 49, 82, 29, 70, 62, 32, 59, 88, 65, 40, 91, 60, 86, 63, 3, 19, 48, 27, 81, 57, 30, 77, 22, 74, 54, 66, 85, 17, 4, 58, 31, 80, 38, 37, 11, 0, 1, 2, 13, 18, 92, 83, 12, 41, 51, 39, 56, 35, 7, 14, 64, 47, 75, 87, 10, 20, 36, 28, 15, 69, 8, 16, 43, 6, 21, 84, 42, 78, 33, 46, 7, 89, 72, 55, 45, 71, 44, 50, 5};
//        int[] tmSolutionLine7  = new int[]{83, 67, 0, 28, 6, 73, 75, 74, 39, 58, 13, 41, 84, 69, 11, 49, 36, 38, 2, 82, 62, 17, 34, 8, 77, 72, 76, 26, 52, 15, 9, 86, 60, 90, 21, 25, 44, 14, 32, 33, 57, 27, 1, 70, 71, 81, 46, 16, 47, 5, 61, 50, 35, 23, 42, 78, 88, 24, 63, 30, 12, 40, 91, 89, 92, 65, 18, 56, 87, 10, 64, 29, 31, 7, 22, 55, 43, 80, 66, 85, 51, 20, 4, 3, 68, 59, 45, 53, 19, 48, 37, 79, 54, 8};
//        int[] tmSolutionLine8  = new int[]{39, 45, 52, 12, 38, 5, 21, 37, 9, 15, 53, 10, 7, 13, 67, 14, 44, 16, 91, 89, 46, 70, 29, 69, 61, 25, 88, 86, 63, 22, 19, 48, 27, 81, 57, 30, 77, 32, 74, 54, 66, 85, 17, 4, 58, 84, 41, 82, 92, 11, 71, 72, 60, 73, 1, 6, 49, 43, 0, 35, 76, 65, 59, 28, 33, 18, 56, 23, 87, 64, 68, 80, 51, 40, 34, 2, 42, 50, 8, 78, 31, 62, 47, 24, 20, 83, 75, 79, 90, 26, 55, 3, 36, 15};
//        int[] tmSolutionLine9  = new int[]{14, 7, 61, 42, 55, 43, 59, 63, 40, 38, 67, 54, 15, 25, 20, 80, 82, 3, 16, 11, 47, 4, 31, 23, 84, 33, 5, 21, 83, 51, 70, 2, 48, 30, 69, 1, 60, 85, 28, 74, 78, 9, 8, 0, 71, 92, 44, 46, 19, 37, 13, 36, 26, 32, 49, 27, 88, 24, 17, 6, 22, 35, 91, 89, 50, 65, 18, 56, 87, 10, 64, 29, 73, 12, 53, 81, 86, 58, 76, 66, 45, 75, 77, 90, 62, 39, 52, 34, 79, 72, 41, 57, 68, 5};
//        int[] tmSolutionLine10  = new int[]{83, 88, 89, 69, 75, 26, 54, 53, 28, 4, 70, 63, 67, 47, 64, 8, 74, 65, 5, 84, 22, 7, 62, 77, 9, 33, 21, 48, 43, 71, 12, 10, 6, 34, 57, 1, 52, 32, 50, 79, 68, 20, 3, 37, 42, 73, 10, 72, 23, 80, 76, 24, 85, 13, 91, 14, 78, 45, 0, 11, 31, 51, 35, 29, 58, 39, 18, 66, 87, 61, 2, 41, 46, 17, 82, 90, 40, 36, 81, 19, 15, 30, 86, 25, 55, 92, 27, 44, 56, 59, 60, 38, 16, 7};
//        int[] tmSolutionLine11  = new int[]{40, 4, 21, 53, 66, 71, 52, 30, 46, 57, 85, 8, 9, 54, 67, 28, 91, 16, 6, 19, 37, 73, 7, 86, 92, 43, 48, 44, 33, 76, 12, 42, 88, 27, 63, 61, 81, 3, 38, 74, 58, 68, 56, 50, 24, 31, 80, 34, 79, 11, 14, 1, 0, 13, 47, 25, 83, 72, 36, 22, 39, 2, 23, 84, 90, 29, 59, 64, 17, 77, 20, 65, 69, 35, 78, 41, 45, 60, 82, 32, 5, 70, 89, 55, 49, 75, 62, 10, 15, 51, 18, 26, 87, 6};
//        int[] tmSolutionLine12  = new int[]{84, 59, 75, 43, 4, 73, 58, 63, 40, 30, 67, 54, 15, 25, 20, 80, 38, 3, 16, 86, 77, 37, 90, 91, 61, 83, 88, 66, 69, 11, 24, 12, 26, 10, 62, 39, 44, 32, 50, 68, 22, 51, 29, 74, 71, 48, 78, 19, 45, 85, 64, 18, 23, 46, 42, 21, 47, 81, 17, 76, 57, 2, 41, 56, 27, 60, 35, 53, 82, 70, 65, 1, 31, 7, 87, 55, 36, 0, 28, 13, 6, 49, 72, 52, 9, 92, 89, 5, 34, 79, 8, 14, 33, 6};
//        int[] tmSolutionLine13  = new int[]{4, 69, 51, 61, 71, 41, 64, 30, 46, 86, 85, 59, 9, 54, 16, 28, 82, 57, 6, 19, 37, 73, 7, 23, 40, 43, 48, 39, 45, 32, 12, 27, 66, 84, 63, 92, 81, 3, 38, 74, 58, 68, 56, 25, 24, 31, 80, 14, 79, 11, 21, 1, 0, 13, 47, 33, 83, 72, 36, 90, 77, 35, 91, 29, 44, 65, 18, 75, 87, 10, 62, 89, 49, 70, 34, 2, 42, 50, 8, 78, 15, 67, 22, 17, 26, 5, 88, 52, 55, 76, 60, 53, 20, 5};
//        int[] tmSolutionLine14  = new int[]{88, 46, 89, 86, 49, 48, 54, 84, 4, 61, 92, 24, 31, 80, 17, 43, 11, 67, 33, 12, 21, 50, 90, 18, 73, 15, 3, 28, 64, 25, 0, 87, 75, 55, 91, 22, 45, 20, 32, 78, 52, 63, 10, 6, 23, 77, 83, 59, 38, 37, 19, 81, 5, 51, 58, 13, 14, 42, 16, 76, 57, 2, 41, 56, 27, 60, 35, 53, 82, 70, 65, 1, 40, 9, 30, 36, 44, 26, 72, 47, 68, 39, 69, 79, 71, 8, 74, 29, 62, 66, 85, 34, 7, 7};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //4号结果
//        List<int[]> tmSolution = new ArrayList<>();
//        int[] tmSolutionLine  = new int[]{39, 75, 92, 9, 37, 17, 3, 18, 47, 84, 57, 11, 48, 63, 8, 28, 31, 26, 53, 65, 80, 61, 27, 13, 51, 50, 44, 76, 52, 74, 58, 64, 90, 1, 16, 70, 19, 86, 36, 55, 62, 38, 56, 46, 88, 79, 42, 0, 14, 43, 66, 67, 78, 12, 41, 89, 24, 85, 69, 2, 81, 10, 71, 4, 22, 32, 20, 60, 7, 15, 45, 5, 54, 21, 35, 40, 33, 49, 6, 68, 29, 34, 30, 91, 23, 73, 72, 25, 87, 83, 77, 82, 59, 12};
//        int[] tmSolutionLine2  = new int[]{60, 69, 34, 42, 52, 21, 63, 19, 28, 37, 87, 66, 11, 26, 53, 9, 73, 22, 84, 47, 68, 25, 30, 13, 24, 78, 44, 20, 51, 61, 71, 62, 90, 77, 16, 70, 92, 86, 36, 41, 58, 27, 3, 49, 8, 10, 14, 31, 74, 83, 88, 12, 45, 5, 72, 1, 32, 91, 38, 2, 43, 81, 75, 89, 56, 57, 48, 55, 39, 18, 35, 46, 67, 79, 33, 64, 80, 23, 15, 54, 6, 59, 82, 65, 85, 76, 4, 29, 0, 7, 40, 17, 50, 13};
//        int[] tmSolutionLine3  = new int[]{22, 44, 43, 74, 70, 56, 86, 30, 68, 21, 78, 62, 67, 0, 65, 14, 54, 79, 51, 80, 66, 10, 17, 3, 84, 39, 47, 52, 46, 49, 18, 11, 92, 76, 40, 26, 61, 2, 69, 5, 55, 53, 36, 27, 91, 87, 45, 71, 88, 35, 82, 58, 50, 8, 72, 1, 32, 37, 38, 12, 4, 81, 75, 89, 15, 57, 48, 25, 42, 41, 9, 31, 34, 77, 20, 23, 33, 13, 16, 63, 19, 73, 6, 83, 29, 59, 60, 85, 90, 24, 7, 28, 64, 11};
//        int[] tmSolutionLine4  = new int[]{30, 78, 20, 60, 6, 83, 32, 13, 91, 84, 57, 58, 48, 63, 79, 12, 24, 47, 11, 16, 18, 35, 15, 3, 31, 82, 74, 72, 76, 62, 54, 40, 85, 50, 52, 21, 70, 5, 86, 26, 71, 8, 27, 0, 88, 36, 7, 45, 49, 80, 38, 44, 59, 23, 41, 29, 34, 56, 64, 25, 66, 37, 46, 22, 17, 43, 87, 10, 9, 90, 42, 14, 61, 55, 89, 1, 51, 68, 77, 92, 69, 33, 2, 4, 39, 81, 75, 53, 65, 73, 67, 28, 19, 7};
//        int[] tmSolutionLine5  = new int[]{32, 69, 81, 83, 59, 28, 21, 85, 46, 77, 90, 12, 55, 75, 10, 82, 92, 41, 54, 22, 68, 86, 27, 13, 62, 15, 44, 1, 23, 74, 63, 64, 3, 14, 16, 70, 91, 31, 36, 84, 57, 11, 56, 73, 8, 50, 45, 43, 88, 35, 29, 2, 0, 42, 47, 26, 38, 80, 89, 25, 66, 6, 78, 65, 17, 4, 87, 52, 20, 40, 51, 30, 71, 67, 18, 39, 60, 61, 37, 48, 19, 76, 58, 33, 34, 24, 53, 5, 49, 7, 9, 79, 72, 7};
//        int[] tmSolutionLine6  = new int[]{21, 88, 3, 74, 59, 25, 23, 61, 89, 37, 91, 67, 80, 27, 1, 30, 16, 33, 46, 10, 4, 48, 85, 40, 47, 44, 63, 32, 36, 7, 13, 83, 2, 49, 26, 79, 35, 69, 20, 3, 75, 6, 22, 81, 29, 68, 71, 72, 15, 65, 9, 12, 70, 5, 76, 77, 56, 14, 28, 51, 50, 18, 92, 39, 86, 55, 41, 0, 58, 73, 45, 11, 64, 53, 54, 84, 42, 8, 90, 31, 19, 82, 78, 43, 66, 62, 24, 60, 52, 87, 17, 34, 57, 7};
//        int[] tmSolutionLine7  = new int[]{90, 72, 45, 32, 66, 21, 38, 43, 42, 89, 82, 84, 80, 50, 1, 30, 16, 33, 46, 10, 4, 48, 85, 40, 39, 44, 27, 34, 81, 25, 23, 73, 87, 26, 55, 0, 91, 2, 36, 61, 3, 17, 56, 31, 88, 79, 14, 11, 51, 83, 24, 12, 70, 5, 76, 77, 68, 19, 28, 13, 20, 18, 59, 49, 37, 74, 41, 53, 54, 67, 7, 47, 69, 8, 52, 15, 86, 29, 62, 6, 71, 60, 75, 57, 92, 64, 65, 22, 78, 9, 58, 78, 35, 8};
//        int[] tmSolutionLine8  = new int[]{60, 72, 48, 6, 90, 56, 55, 53, 34, 36, 84, 50, 30, 49, 2, 32, 18, 3, 54, 22, 29, 86, 27, 69, 62, 15, 44, 1, 58, 61, 43, 73, 75, 31, 17, 45, 59, 64, 74, 87, 63, 65, 39, 21, 88, 79, 14, 11, 51, 83, 24, 12, 70, 5, 76, 77, 68, 19, 28, 13, 20, 42, 47, 37, 89, 71, 26, 82, 33, 85, 38, 67, 92, 8, 10, 0, 25, 40, 57, 91, 9, 52, 78, 66, 23, 7, 81, 46, 80, 35, 41, 16, 4, 12};
//        int[] tmSolutionLine9  = new int[]{69, 17, 4, 79, 87, 72, 78, 46, 0, 71, 7, 12, 55, 14, 23, 82, 92, 77, 53, 65, 80, 61, 27, 13, 51, 50, 44, 76, 52, 74, 58, 64, 90, 1, 16, 70, 19, 86, 36, 41, 57, 11, 3, 73, 8, 10, 45, 43, 31, 91, 62, 2, 88, 68, 47, 75, 38, 22, 89, 63, 54, 5, 26, 39, 35, 20, 48, 18, 83, 9, 34, 33, 49, 29, 24, 56, 15, 9, 81, 60, 84, 21, 37, 85, 30, 25, 28, 66, 59, 42, 32, 6, 67, 4};
//        int[] tmSolutionLine10  = new int[]{16, 7, 26, 85, 10, 76, 77, 33, 63, 0, 92, 29, 69, 79, 28, 46, 53, 22, 84, 47, 68, 25, 30, 13, 24, 78, 8, 65, 64, 9, 55, 23, 3, 14, 67, 20, 80, 6, 51, 61, 39, 74, 37, 27, 57, 48, 11, 71, 88, 18, 82, 58, 50, 12, 72, 1, 32, 91, 38, 2, 43, 81, 75, 89, 17, 4, 87, 52, 42, 15, 45, 5, 41, 21, 35, 40, 54, 44, 90, 31, 19, 86, 83, 62, 70, 56, 62, 36, 34, 60, 49, 73, 59, 10};
//        int[] tmSolutionLine11  = new int[]{89, 17, 50, 91, 61, 26, 46, 19, 28, 37, 87, 53, 11, 84, 85, 9, 73, 7, 32, 70, 42, 0, 40, 51, 34, 44, 63, 21, 36, 8, 13, 83, 2, 76, 33, 79, 35, 78, 82, 3, 47, 6, 90, 81, 57, 27, 69, 62, 56, 65, 68, 71, 43, 67, 49, 88, 54, 25, 24, 30, 20, 18, 92, 39, 38, 80, 41, 16, 58, 75, 45, 64, 14, 1, 52, 15, 86, 29, 48, 60, 72, 74, 12, 66, 23, 77, 59, 55, 10, 5, 31, 4, 22, 6};
//        int[] tmSolutionLine12  = new int[]{78, 34, 37, 79, 81, 32, 26, 3, 27, 69, 35, 83, 92, 49, 0, 24, 70, 7, 1, 52, 62, 54, 33, 87, 68, 5, 20, 41, 91, 67, 65, 18, 63, 71, 44, 19, 51, 90, 57, 38, 17, 53, 36, 45, 21, 46, 73, 39, 28, 2, 88, 14, 9, 76, 47, 40, 13, 25, 80, 43, 56, 11, 82, 61, 85, 42, 58, 16, 84, 89, 8, 75, 50, 30, 64, 15, 86, 29, 48, 60, 72, 74, 12, 66, 23, 77, 59, 55, 10, 5, 31, 4, 22, 6};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //5号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//        int[] tmSolutionLine  = new int[]{22, 76, 72, 73, 27, 26, 17, 82, 39, 85, 56, 14, 6, 33, 58, 71, 23, 86, 46, 52, 64, 88, 81, 36, 31, 69, 57, 42, 3, 51, 45, 67, 4, 78, 11, 5, 59, 34, 12, 35, 25, 92, 61, 32, 65, 60, 1, 38, 50, 10, 55, 75, 79, 62, 87, 9, 68, 7, 0, 6, 37, 24, 48, 80, 28, 13, 54, 53, 83, 84, 19, 41, 66, 44, 91, 18, 47, 29, 21, 70, 77, 43, 2, 74, 49, 30, 90, 16, 63, 20, 89, 15, 40, 8};
//        int[] tmSolutionLine2  = new int[]{15, 12, 33, 16, 65, 51, 34, 37, 46, 49, 61, 10, 88, 87, 72, 19, 28, 67, 29, 63, 18, 43, 23, 47, 62, 17, 36, 56, 20, 5, 80, 4, 50, 24, 66, 42, 44, 38, 27, 83, 79, 59, 7, 85, 84, 86, 68, 30, 22, 77, 3, 26, 0, 57, 71, 60, 48, 52, 74, 6, 64, 53, 31, 78, 39, 8, 45, 91, 40, 21, 32, 81, 9, 25, 54, 82, 73, 70, 69, 14, 35, 13, 55, 75, 90, 76, 2, 11, 1, 41, 89, 92, 58, 8};
//        int[] tmSolutionLine3  = new int[]{70, 23, 8, 16, 65, 51, 34, 37, 81, 63, 91, 33, 39, 84, 20, 19, 11, 68, 62, 5, 44, 0, 86, 27, 80, 83, 76, 30, 66, 52, 75, 42, 4, 43, 48, 89, 40, 78, 73, 85, 79, 59, 7, 72, 71, 36, 1, 38, 53, 77, 3, 26, 22, 32, 47, 60, 90, 45, 74, 6, 64, 88, 57, 50, 82, 41, 49, 61, 13, 15, 9, 46, 35, 25, 54, 29, 31, 92, 69, 14, 10, 18, 24, 28, 17, 56, 87, 2, 67, 55, 58, 21, 12, 13};
//        int[] tmSolutionLine4  = new int[]{66, 51, 9, 58, 26, 0, 54, 28, 33, 34, 86, 84, 40, 62, 15, 83, 17, 29, 61, 25, 80, 63, 73, 76, 55, 65, 44, 47, 3, 56, 12, 67, 50, 60, 78, 43, 53, 39, 72, 85, 19, 16, 7, 18, 89, 36, 1, 38, 59, 13, 75, 71, 30, 32, 52, 24, 90, 45, 74, 6, 64, 88, 57, 4, 82, 41, 49, 37, 77, 22, 42, 48, 35, 79, 68, 2, 8, 21, 20, 81, 5, 23, 69, 11, 92, 27, 14, 87, 70, 10, 46, 31, 91, 6};
//        int[] tmSolutionLine5  = new int[]{85, 11, 68, 35, 22, 33, 5, 72, 46, 49, 61, 10, 88, 87, 58, 55, 79, 0, 41, 8, 16, 43, 31, 20, 73, 32, 56, 52, 75, 14, 57, 9, 12, 19, 42, 38, 18, 78, 44, 74, 86, 2, 83, 67, 76, 59, 70, 29, 28, 60, 6, 15, 71, 90, 48, 63, 62, 53, 27, 24, 51, 26, 30, 34, 25, 80, 54, 89, 65, 77, 66, 45, 81, 23, 91, 4, 69, 64, 1, 50, 21, 92, 47, 40, 13, 36, 3, 7, 39, 84, 82, 17, 37, 8};
//        int[] tmSolutionLine6  = new int[]{21, 56, 8, 84, 65, 51, 73, 10, 59, 63, 91, 33, 39, 68, 79, 19, 61, 88, 57, 82, 44, 0, 60, 16, 80, 86, 76, 20, 89, 78, 66, 18, 28, 38, 70, 25, 83, 27, 34, 62, 35, 32, 7, 72, 3, 36, 75, 45, 30, 77, 9, 52, 90, 4, 81, 14, 50, 42, 29, 37, 69, 6, 47, 74, 18, 12, 46, 85, 1, 17, 11, 31, 13, 55, 26, 87, 5, 67, 49, 43, 24, 48, 15, 2, 92, 22, 41, 54, 58, 23, 53, 64, 71, 9};
//        int[] tmSolutionLine7  = new int[]{22, 57, 15, 9, 28, 51, 86, 37, 67, 70, 91, 33, 39, 71, 69, 19, 31, 81, 26, 63, 18, 43, 23, 77, 17, 66, 36, 35, 75, 79, 89, 54, 12, 27, 60, 38, 41, 30, 85, 34, 44, 76, 83, 62, 29, 16, 64, 50, 88, 80, 40, 78, 48, 52, 90, 1, 42, 55, 11, 24, 73, 53, 49, 13, 25, 4, 3, 58, 72, 61, 65, 45, 74, 7, 68, 2, 8, 21, 20, 5, 59, 0, 32, 10, 92, 14, 82, 56, 6, 87, 47, 84, 46, 8};
//        int[] tmSolutionLine8  = new int[]{73, 40, 45, 55, 49, 87, 51, 21, 43, 82, 57, 66, 90, 83, 3, 38, 27, 5, 36, 88, 28, 54, 7, 61, 77, 63, 56, 31, 75, 71, 89, 48, 12, 19, 6, 58, 84, 78, 44, 79, 50, 2, 72, 67, 17, 59, 16, 65, 23, 80, 70, 41, 86, 34, 32, 42, 30, 4, 76, 24, 37, 39, 47, 74, 60, 1, 85, 62, 13, 15, 9, 14, 35, 25, 52, 29, 26, 92, 69, 46, 10, 18, 8, 81, 91, 22, 0, 33, 68, 20, 53, 64, 11, 9};
//        int[] tmSolutionLine9  = new int[]{39, 19, 41, 63, 65, 75, 90, 82, 33, 38, 59, 78, 85, 22, 26, 71, 84, 86, 81, 88, 28, 54, 2, 36, 49, 8, 57, 47, 91, 80, 89, 6, 43, 61, 73, 74, 21, 51, 53, 12, 4, 66, 7, 68, 40, 16, 83, 3, 72, 77, 27, 0, 48, 44, 56, 5, 50, 42, 23, 32, 45, 30, 20, 34, 60, 1, 62, 37, 13, 15, 9, 46, 35, 25, 52, 29, 31, 92, 69, 14, 10, 18, 24, 76, 17, 64, 70, 87, 67, 55, 58, 11, 79, 5};
//        int[] tmSolutionLine10  = new int[]{0, 46, 10, 29, 92, 55, 26, 65, 70, 90, 58, 78, 13, 59, 54, 62, 32, 52, 74, 17, 23, 27, 83, 64, 56, 51, 76, 33, 12, 7, 18, 14, 85, 75, 81, 43, 53, 28, 72, 60, 8, 73, 15, 44, 48, 68, 69, 31, 22, 61, 11, 49, 47, 42, 25, 38, 35, 91, 45, 24, 86, 5, 34, 57, 30, 89, 9, 1, 88, 6, 79, 4, 20, 37, 50, 36, 21, 66, 19, 40, 87, 3, 71, 16, 82, 80, 2, 67, 41, 39, 63, 84, 77, 14};
//        int[] tmSolutionLine11  = new int[]{78, 40, 51, 9, 77, 10, 1, 57, 36, 60, 14, 76, 84, 79, 59, 19, 74, 67, 28, 46, 18, 63, 73, 91, 64, 65, 3, 58, 56, 0, 45, 71, 2, 24, 81, 11, 44, 23, 8, 34, 27, 53, 33, 35, 26, 55, 47, 31, 22, 61, 38, 6, 17, 42, 25, 16, 7, 30, 70, 12, 72, 15, 52, 50, 82, 41, 49, 89, 62, 66, 29, 37, 90, 68, 85, 80, 69, 21, 20, 5, 88, 86, 13, 32, 39, 4, 92, 54, 48, 43, 75, 83, 87, 6};
//        int[] tmSolutionLine12  = new int[]{73, 40, 45, 55, 69, 75, 17, 82, 22, 36, 83, 14, 64, 33, 58, 71, 84, 86, 46, 52, 13, 88, 81, 87, 31, 8, 57, 47, 3, 56, 12, 67, 50, 60, 78, 43, 53, 39, 72, 85, 19, 16, 44, 1, 23, 68, 6, 29, 28, 77, 9, 15, 90, 5, 48, 63, 62, 26, 27, 24, 51, 41, 30, 34, 25, 80, 54, 89, 65, 20, 66, 4, 74, 76, 0, 2, 32, 11, 61, 21, 59, 38, 70, 42, 92, 35, 49, 79, 18, 10, 91, 7, 37, 8};
//        int[] tmSolutionLine13  = new int[]{69, 86, 12, 32, 31, 50, 80, 55, 23, 13, 44, 88, 85, 34, 18, 79, 62, 38, 33, 61, 6, 66, 26, 46, 57, 49, 42, 59, 74, 8, 60, 29, 11, 53, 48, 16, 28, 45, 25, 15, 77, 76, 47, 43, 72, 7, 67, 39, 64, 87, 70, 4, 9, 63, 14, 84, 89, 2, 54, 56, 40, 17, 36, 27, 78, 20, 92, 35, 30, 58, 19, 41, 21, 24, 5, 81, 83, 0, 3, 37, 90, 91, 10, 73, 71, 22, 82, 52, 68, 51, 65, 1, 75, 3};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //6号结果  int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//        int[] tmSolutionLine  = new int[]{34, 72, 7, 87, 2, 35, 76, 55, 22, 44, 51, 46, 52, 62, 70, 89, 9, 16, 85, 1, 28, 15, 64, 45, 88, 18, 79, 90, 68, 69, 6, 40, 58, 83, 57, 21, 43, 31, 49, 81, 0, 47, 42, 20, 60, 63, 91, 29, 37, 92, 75, 26, 74, 5, 14, 53, 71, 10, 27, 32, 48, 50, 80, 54, 78, 38, 66, 41, 82, 59, 8, 13, 77, 3, 84, 36, 65, 39, 56, 11, 86, 17, 4, 61, 33, 24, 12, 67, 73, 23, 25, 30, 19, 3};
//        int[] tmSolutionLine2  = new int[]{18, 53, 90, 91, 45, 11, 4, 52, 46, 79, 44, 73, 67, 50, 40, 56, 59, 27, 33, 88, 35, 75, 63, 24, 64, 74, 37, 71, 86, 49, 72, 1, 77, 68, 58, 66, 78, 41, 82, 55, 87, 34, 42, 22, 92, 39, 54, 65, 62, 9, 23, 80, 0, 32, 12, 5, 76, 19, 16, 17, 15, 31, 25, 38, 43, 81, 89, 21, 28, 10, 26, 61, 57, 84, 29, 13, 69, 48, 6, 8, 85, 36, 30, 70, 7, 60, 51, 47, 20, 3, 14, 83, 2, 7};
//        int[] tmSolutionLine3  = new int[]{85, 2, 55, 66, 46, 70, 39, 73, 86, 18, 3, 8, 20, 48, 76, 6, 60, 64, 63, 11, 7, 30, 41, 35, 58, 5, 56, 57, 74, 49, 72, 80, 77, 68, 88, 13, 14, 62, 37, 75, 65, 34, 42, 67, 71, 21, 33, 51, 53, 40, 79, 1, 0, 32, 12, 24, 19, 27, 16, 17, 10, 89, 25, 38, 43, 82, 83, 52, 90, 44, 26, 61, 81, 23, 59, 36, 69, 22, 15, 45, 84, 78, 4, 87, 54, 28, 31, 9, 92, 91, 29, 47, 50, 6};
//        int[] tmSolutionLine4  = new int[]{33, 48, 3, 45, 61, 52, 43, 78, 10, 63, 2, 12, 73, 83, 66, 47, 28, 68, 62, 51, 21, 11, 35, 88, 80, 5, 71, 34, 30, 44, 42, 7, 58, 84, 41, 75, 8, 87, 39, 26, 14, 76, 4, 36, 29, 49, 86, 1, 65, 64, 0, 16, 46, 85, 25, 69, 59, 40, 60, 17, 89, 9, 31, 23, 57, 38, 90, 6, 82, 13, 91, 70, 56, 92, 32, 72, 77, 67, 54, 27, 55, 79, 53, 50, 22, 18, 20, 74, 19, 24, 15, 81, 37, 6};
//        int[] tmSolutionLine5  = new int[]{46, 79, 39, 71, 68, 73, 55, 36, 7, 84, 2, 7, 15, 63, 85, 83, 75, 30, 53, 40, 61, 74, 81, 48, 86, 76, 72, 57, 67, 60, 64, 23, 58, 31, 1, 21, 62, 12, 28, 24, 17, 80, 4, 3, 35, 29, 32, 16, 65, 91, 13, 42, 19, 43, 14, 90, 50, 10, 27, 39, 38, 22, 8, 54, 78, 33, 66, 41, 82, 59, 69, 52, 37, 77, 25, 34, 70, 51, 11, 56, 0, 60, 44, 26, 49, 5, 88, 20, 18, 89, 47, 87, 9, 6};
//        int[] tmSolutionLine6  = new int[]{91, 18, 58, 21, 12, 24, 41, 50, 46, 79, 44, 73, 67, 52, 88, 56, 59, 63, 33, 66, 11, 75, 55, 61, 64, 74, 37, 71, 7, 15, 22, 10, 2, 51, 57, 92, 0, 60, 78, 49, 36, 65, 62, 13, 35, 45, 23, 40, 8, 86, 43, 42, 54, 53, 30, 81, 3, 9, 77, 17, 89, 27, 31, 38, 20, 85, 90, 6, 82, 83, 47, 26, 19, 32, 48, 5, 70, 34, 76, 29, 84, 25, 4, 87, 69, 28, 72, 16, 1, 80, 68, 39, 14, 8};
//        int[] tmSolutionLine7  = new int[]{48, 12, 10, 17, 3, 16, 45, 80, 55, 10, 73, 74, 35, 52, 65, 6, 75, 25, 29, 11, 72, 88, 81, 83, 7, 33, 92, 86, 15, 64, 82, 91, 38, 57, 8, 23, 40, 53, 43, 84, 19, 1, 0, 2, 68, 63, 78, 67, 56, 58, 4, 32, 62, 61, 50, 31, 14, 27, 9, 34, 66, 60, 39, 79, 77, 71, 26, 69, 44, 42, 36, 47, 5, 30, 59, 46, 76, 90, 24, 85, 49, 21, 28, 37, 70, 51, 13, 20, 18, 89, 41, 87, 54, 6};
//        int[] tmSolutionLine8  = new int[]{49, 67, 36, 12, 80, 74, 16, 73, 34, 54, 23, 3, 69, 78, 45, 17, 72, 64, 7, 19, 32, 0, 15, 76, 39, 60, 50, 14, 33, 8, 62, 40, 20, 22, 18, 47, 10, 42, 55, 37, 51, 31, 68, 2, 92, 29, 4, 63, 11, 87, 61, 66, 70, 53, 58, 65, 57, 41, 77, 48, 89, 27, 25, 38, 43, 82, 83, 52, 90, 44, 26, 81, 5, 30, 59, 46, 88, 91, 24, 9, 75, 21, 28, 79, 56, 71, 6, 35, 84, 13, 85, 86, 1, 4};
//        int[] tmSolutionLine9  = new int[]{59, 87, 42, 86, 44, 76, 12, 37, 2, 22, 3, 28, 35, 52, 73, 58, 69, 43, 60, 64, 47, 61, 5, 70, 13, 27, 92, 4, 24, 71, 78, 10, 34, 65, 8, 91, 45, 72, 16, 84, 33, 1, 0, 46, 56, 63, 40, 67, 20, 85, 36, 50, 54, 53, 30, 81, 66, 55, 77, 17, 89, 9, 31, 23, 57, 38, 90, 6, 82, 83, 15, 26, 19, 32, 48, 14, 29, 39, 7, 11, 88, 41, 79, 51, 49, 25, 68, 62, 80, 18, 75, 21, 74, 4};
//        int[] tmSolutionLine10  = new int[]{5, 18, 31, 72, 78, 6, 47, 54, 55, 10, 25, 57, 35, 52, 85, 83, 75, 11, 77, 33, 61, 74, 81, 38, 92, 76, 69, 7, 15, 73, 82, 34, 14, 23, 8, 91, 58, 53, 40, 3, 46, 86, 26, 4, 66, 87, 2, 1, 36, 84, 0, 79, 64, 13, 45, 68, 24, 27, 16, 17, 71, 59, 39, 20, 30, 60, 42, 43, 19, 67, 56, 63, 51, 12, 88, 90, 65, 32, 49, 22, 9, 80, 28, 37, 70, 29, 89, 50, 48, 62, 41, 44, 21, 9};
//        int[] tmSolutionLine11  = new int[]{78, 65, 29, 76, 36, 61, 92, 14, 0, 87, 67, 60, 69, 63, 11, 23, 42, 47, 22, 80, 72, 18, 68, 71, 8, 86, 3, 7, 33, 70, 39, 16, 49, 2, 9, 62, 58, 6, 19, 74, 31, 37, 64, 79, 50, 48, 73, 34, 45, 40, 10, 66, 54, 53, 32, 15, 57, 8, 77, 17, 89, 27, 25, 38, 43, 20, 12, 41, 90, 44, 26, 81, 5, 30, 59, 46, 88, 91, 55, 83, 75, 21, 85, 28, 51, 56, 52, 35, 84, 13, 82, 24, 1, 4};
//        int[] tmSolutionLine12  = new int[]{92, 19, 10, 13, 45, 5, 64, 15, 36, 29, 52, 14, 67, 55, 20, 41, 48, 78, 79, 63, 33, 46, 43, 85, 22, 50, 38, 56, 2, 47, 72, 1, 77, 68, 58, 30, 27, 7, 28, 35, 25, 21, 6, 4, 24, 84, 49, 39, 53, 75, 76, 73, 16, 23, 17, 86, 59, 9, 34, 57, 89, 12, 80, 44, 81, 42, 60, 18, 87, 37, 26, 69, 66, 40, 61, 71, 70, 51, 54, 3, 32, 91, 90, 74, 83, 62, 0, 31, 11, 82, 88, 8, 65, 8};
//        int[] tmSolutionLine13  = new int[]{87, 26, 30, 46, 71, 44, 92, 61, 36, 83, 2, 12, 69, 63, 11, 6, 75, 25, 29, 65, 7, 88, 81, 85, 23, 20, 68, 24, 16, 0, 72, 89, 58, 40, 70, 21, 18, 76, 49, 9, 47, 86, 4, 3, 35, 57, 39, 19, 90, 91, 13, 42, 1, 43, 14, 53, 50, 10, 27, 38, 48, 22, 8, 54, 78, 33, 66, 41, 82, 59, 73, 52, 37, 77, 17, 28, 56, 32, 31, 47, 74, 60, 67, 34, 80, 45, 79, 84, 62, 15, 5, 51, 55, 11};
//        int[] tmSolutionLine14  = new int[]{65, 78, 58, 59, 12, 16, 41, 76, 7, 79, 24, 73, 67, 52, 50, 37, 21, 63, 56, 66, 39, 75, 87, 11, 64, 74, 33, 35, 28, 8, 62, 3, 20, 22, 18, 30, 10, 42, 55, 47, 51, 31, 40, 2, 72, 68, 82, 84, 91, 4, 60, 88, 54, 69, 0, 81, 92, 27, 77, 43, 89, 9, 26, 23, 85, 38, 70, 44, 53, 83, 15, 6, 19, 32, 80, 14, 17, 49, 48, 61, 36, 34, 71, 90, 86, 45, 5, 57, 13, 29, 1, 25, 46, 9};
//        int[] tmSolutionLine15  = new int[]{88, 34, 83, 17, 44, 30, 87, 80, 55, 6, 73, 74, 35, 85, 36, 10, 49, 77, 14, 71, 23, 76, 91, 52, 66, 33, 92, 8, 15, 64, 82, 81, 38, 19, 86, 79, 65, 12, 28, 24, 43, 7, 84, 3, 39, 29, 32, 1, 53, 40, 21, 4, 16, 46, 62, 5, 60, 2, 63, 69, 41, 48, 47, 9, 31, 56, 25, 26, 13, 11, 67, 50, 78, 37, 51, 58, 42, 59, 27, 72, 0, 90, 45, 61, 70, 57, 68, 20, 18, 89, 22, 75, 54, 6};
//        int[] tmSolutionLine16  = new int[]{47, 28, 40, 13, 57, 77, 15, 79, 83, 53, 10, 92, 60, 32, 89, 6, 31, 68, 62, 51, 64, 11, 86, 54, 80, 5, 71, 34, 30, 67, 42, 7, 58, 4, 41, 75, 0, 87, 38, 55, 14, 16, 84, 69, 91, 49, 35, 1, 65, 63, 43, 46, 73, 74, 45, 25, 24, 82, 3, 17, 19, 70, 2, 20, 29, 50, 88, 52, 90, 44, 26, 61, 81, 23, 59, 72, 36, 22, 21, 76, 9, 66, 27, 37, 39, 8, 56, 85, 12, 78, 48, 18, 33, 5};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//        tmSolution.add(tmSolutionLine15);
//        tmSolution.add(tmSolutionLine16);
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //7号结果  int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//        int[] tmSolutionLine  = new int[]{39, 75, 92, 9, 37, 17, 3, 18, 47, 84, 57, 11, 48, 63, 8, 28, 31, 26, 53, 65, 80, 61, 27, 13, 51, 50, 44, 76, 52, 74, 58, 64, 90, 1, 16, 70, 19, 86, 36, 55, 62, 38, 56, 46, 88, 79, 42, 0, 14, 43, 66, 67, 78, 12, 41, 89, 24, 85, 69, 2, 81, 10, 71, 4, 22, 32, 20, 60, 7, 15, 45, 5, 54, 21, 35, 40, 33, 49, 6, 68, 29, 34, 30, 91, 23, 73, 72, 25, 87, 83, 77, 82, 59, 12};
//        int[] tmSolutionLine2  = new int[]{60, 69, 34, 42, 52, 21, 63, 19, 28, 37, 87, 66, 11, 26, 53, 9, 73, 22, 84, 47, 68, 25, 30, 13, 24, 78, 44, 20, 51, 61, 71, 62, 90, 77, 16, 70, 92, 86, 36, 41, 58, 27, 3, 49, 8, 10, 14, 31, 74, 83, 88, 12, 45, 5, 72, 1, 32, 91, 38, 2, 43, 81, 75, 89, 56, 57, 48, 55, 39, 18, 35, 46, 67, 79, 33, 64, 80, 23, 15, 54, 6, 59, 82, 65, 85, 76, 4, 29, 0, 7, 40, 17, 50, 13};
//        int[] tmSolutionLine3  = new int[]{22, 44, 43, 74, 70, 56, 86, 30, 68, 21, 78, 62, 67, 0, 65, 14, 54, 79, 51, 80, 66, 10, 17, 3, 84, 39, 47, 52, 46, 49, 18, 11, 92, 76, 40, 26, 61, 2, 69, 5, 55, 53, 36, 27, 91, 87, 45, 71, 88, 35, 82, 58, 50, 8, 72, 1, 32, 37, 38, 12, 4, 81, 75, 89, 15, 57, 48, 25, 42, 41, 9, 31, 34, 77, 20, 23, 33, 13, 16, 63, 19, 73, 6, 83, 29, 59, 60, 85, 90, 24, 7, 28, 64, 11};
//        int[] tmSolutionLine4  = new int[]{30, 78, 20, 60, 6, 83, 32, 13, 91, 84, 57, 58, 48, 63, 79, 12, 24, 47, 11, 16, 18, 35, 15, 3, 31, 82, 74, 72, 76, 62, 54, 40, 85, 50, 52, 21, 70, 5, 86, 26, 71, 8, 27, 0, 88, 36, 7, 45, 49, 80, 38, 44, 59, 23, 41, 29, 34, 56, 64, 25, 66, 37, 46, 22, 17, 43, 87, 10, 9, 90, 42, 14, 61, 55, 89, 1, 51, 68, 77, 92, 69, 33, 2, 4, 39, 81, 75, 53, 65, 73, 67, 28, 19, 7};
//        int[] tmSolutionLine5  = new int[]{32, 69, 81, 83, 59, 28, 21, 85, 46, 77, 90, 12, 55, 75, 10, 82, 92, 41, 54, 22, 68, 86, 27, 13, 62, 15, 44, 1, 23, 74, 63, 64, 3, 14, 16, 70, 91, 31, 36, 84, 57, 11, 56, 73, 8, 50, 45, 43, 88, 35, 29, 2, 0, 42, 47, 26, 38, 80, 89, 25, 66, 6, 78, 65, 17, 4, 87, 52, 20, 40, 51, 30, 71, 67, 18, 39, 60, 61, 37, 48, 19, 76, 58, 33, 34, 24, 53, 5, 49, 7, 9, 79, 72, 7};
//        int[] tmSolutionLine6  = new int[]{21, 88, 3, 74, 59, 25, 23, 61, 89, 37, 91, 67, 80, 27, 1, 30, 16, 33, 46, 10, 4, 48, 85, 40, 47, 44, 63, 32, 36, 7, 13, 83, 2, 49, 26, 79, 35, 69, 20, 3, 75, 6, 22, 81, 29, 68, 71, 72, 15, 65, 9, 12, 70, 5, 76, 77, 56, 14, 28, 51, 50, 18, 92, 39, 86, 55, 41, 0, 58, 73, 45, 11, 64, 53, 54, 84, 42, 8, 90, 31, 19, 82, 78, 43, 66, 62, 24, 60, 52, 87, 17, 34, 57, 7};
//        int[] tmSolutionLine7  = new int[]{90, 72, 45, 32, 66, 21, 38, 43, 42, 89, 82, 84, 80, 50, 1, 30, 16, 33, 46, 10, 4, 48, 85, 40, 39, 44, 27, 34, 81, 25, 23, 73, 87, 26, 55, 0, 91, 2, 36, 61, 3, 17, 56, 31, 88, 79, 14, 11, 51, 83, 24, 12, 70, 5, 76, 77, 68, 19, 28, 13, 20, 18, 59, 49, 37, 74, 41, 53, 54, 67, 7, 47, 69, 8, 52, 15, 86, 29, 62, 6, 71, 60, 75, 57, 92, 64, 65, 22, 78, 9, 58, 78, 35, 8};
//        int[] tmSolutionLine8  = new int[]{60, 72, 48, 6, 90, 56, 55, 53, 34, 36, 84, 50, 30, 49, 2, 32, 18, 3, 54, 22, 29, 86, 27, 69, 62, 15, 44, 1, 58, 61, 43, 73, 75, 31, 17, 45, 59, 64, 74, 87, 63, 65, 39, 21, 88, 79, 14, 11, 51, 83, 24, 12, 70, 5, 76, 77, 68, 19, 28, 13, 20, 42, 47, 37, 89, 71, 26, 82, 33, 85, 38, 67, 92, 8, 10, 0, 25, 40, 57, 91, 9, 52, 78, 66, 23, 7, 81, 46, 80, 35, 41, 16, 4, 12};
//        int[] tmSolutionLine9  = new int[]{69, 17, 4, 79, 87, 72, 78, 46, 0, 71, 7, 12, 55, 14, 23, 82, 92, 77, 53, 65, 80, 61, 27, 13, 51, 50, 44, 76, 52, 74, 58, 64, 90, 1, 16, 70, 19, 86, 36, 41, 57, 11, 3, 73, 8, 10, 45, 43, 31, 91, 62, 2, 88, 68, 47, 75, 38, 22, 89, 63, 54, 5, 26, 39, 35, 20, 48, 18, 83, 9, 34, 33, 49, 29, 24, 56, 15, 9, 81, 60, 84, 21, 37, 85, 30, 25, 28, 66, 59, 42, 32, 6, 67, 4};
//        int[] tmSolutionLine10  = new int[]{16, 7, 26, 85, 10, 76, 77, 33, 63, 0, 92, 29, 69, 79, 28, 46, 53, 22, 84, 47, 68, 25, 30, 13, 24, 78, 8, 65, 64, 9, 55, 23, 3, 14, 67, 20, 80, 6, 51, 61, 39, 74, 37, 27, 57, 48, 11, 71, 88, 18, 82, 58, 50, 12, 72, 1, 32, 91, 38, 2, 43, 81, 75, 89, 17, 4, 87, 52, 42, 15, 45, 5, 41, 21, 35, 40, 54, 44, 90, 31, 19, 86, 83, 62, 70, 56, 62, 36, 34, 60, 49, 73, 59, 10};
//        int[] tmSolutionLine11  = new int[]{89, 17, 50, 91, 61, 26, 46, 19, 28, 37, 87, 53, 11, 84, 85, 9, 73, 7, 32, 70, 42, 0, 40, 51, 34, 44, 63, 21, 36, 8, 13, 83, 2, 76, 33, 79, 35, 78, 82, 3, 47, 6, 90, 81, 57, 27, 69, 62, 56, 65, 68, 71, 43, 67, 49, 88, 54, 25, 24, 30, 20, 18, 92, 39, 38, 80, 41, 16, 58, 75, 45, 64, 14, 1, 52, 15, 86, 29, 48, 60, 72, 74, 12, 66, 23, 77, 59, 55, 10, 5, 31, 4, 22, 6};
//        int[] tmSolutionLine12  = new int[]{78, 34, 37, 79, 81, 32, 26, 3, 27, 69, 35, 83, 92, 49, 0, 24, 70, 7, 1, 52, 62, 54, 33, 87, 68, 5, 20, 41, 91, 67, 65, 18, 63, 71, 44, 19, 51, 90, 57, 38, 17, 53, 36, 45, 21, 46, 73, 39, 28, 2, 88, 14, 9, 76, 47, 40, 13, 25, 80, 43, 56, 11, 82, 61, 85, 42, 58, 16, 84, 89, 8, 75, 50, 30, 64, 15, 86, 29, 48, 60, 72, 74, 12, 66, 23, 77, 59, 55, 10, 5, 31, 4, 22, 6};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //8号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{50, 31, 15, 35, 11, 14, 45, 76, 60, 51, 92, 16, 27, 21, 37, 59, 13, 65, 87, 81, 79, 17, 2, 86, 84, 20, 75, 41, 64, 52, 80, 19, 22, 5, 34, 7, 89, 78, 72, 32, 74, 8, 90, 47, 57, 68, 25, 24, 12, 30, 48, 67, 85, 88, 38, 36, 71, 61, 18, 55, 29, 9, 46, 6, 77, 40, 82, 42, 1, 69, 63, 91, 0, 23, 44, 73, 3, 4, 53, 56, 26, 39, 83, 66, 70, 10, 58, 28, 49, 54, 33, 43, 62, 12};
//        int[] tmSolutionLine2  = new int[]{61, 87, 11, 33, 32, 84, 1, 54, 24, 85, 55, 35, 18, 30, 56, 79, 28, 3, 13, 14, 7, 88, 70, 62, 82, 39, 45, 10, 65, 73, 43, 15, 31, 41, 80, 29, 76, 26, 74, 63, 4, 53, 2, 75, 5, 58, 71, 9, 67, 52, 17, 66, 46, 51, 23, 40, 81, 22, 92, 50, 6, 69, 89, 25, 38, 90, 44, 21, 20, 64, 16, 59, 47, 83, 42, 72, 68, 37, 77, 49, 91, 78, 36, 34, 57, 0, 60, 8, 19, 86, 48, 27, 12, 7};
//        int[] tmSolutionLine3  = new int[]{11, 27, 91, 34, 20, 32, 72, 12, 64, 39, 10, 67, 73, 24, 44, 55, 88, 35, 80, 14, 45, 68, 7, 31, 57, 13, 82, 8, 5, 18, 33, 77, 71, 49, 21, 75, 30, 85, 48, 53, 62, 42, 84, 90, 51, 58, 87, 9, 74, 76, 22, 6, 0, 52, 28, 37, 70, 83, 69, 40, 65, 43, 60, 78, 26, 59, 81, 41, 12, 56, 1, 61, 47, 66, 3, 36, 29, 23, 17, 92, 38, 46, 79, 25, 54, 63, 86, 19, 15, 16, 2, 50, 89, 4};
//        int[] tmSolutionLine4  = new int[]{68, 8, 91, 22, 46, 65, 4, 12, 89, 55, 41, 67, 82, 24, 25, 31, 1, 81, 73, 69, 30, 17, 5, 33, 36, 43, 87, 10, 75, 90, 35, 51, 86, 39, 83, 7, 64, 19, 66, 40, 3, 79, 18, 53, 42, 23, 14, 56, 21, 60, 74, 26, 28, 32, 58, 71, 63, 13, 70, 92, 0, 15, 20, 34, 47, 77, 57, 6, 85, 45, 52, 38, 27, 59, 84, 48, 78, 54, 2, 49, 11, 72, 62, 9, 37, 88, 76, 29, 16, 80, 61, 50, 44, 12};
//        int[] tmSolutionLine5  = new int[]{60, 84, 80, 68, 0, 88, 67, 25, 78, 61, 20, 79, 21, 40, 39, 33, 1, 81, 73, 69, 30, 17, 75, 55, 36, 43, 87, 10, 22, 90, 35, 51, 86, 44, 83, 7, 64, 19, 66, 92, 24, 77, 16, 48, 46, 89, 3, 28, 2, 82, 32, 4, 12, 53, 52, 70, 74, 63, 8, 72, 34, 57, 76, 58, 71, 50, 11, 26, 27, 38, 5, 45, 62, 18, 29, 41, 23, 6, 15, 31, 65, 91, 42, 9, 37, 85, 47, 49, 14, 59, 13, 56, 54, 4};
//        int[] tmSolutionLine6  = new int[]{87, 31, 92, 60, 11, 14, 45, 84, 0, 51, 20, 79, 21, 40, 35, 33, 1, 16, 73, 69, 81, 56, 75, 55, 36, 66, 50, 41, 5, 19, 80, 77, 22, 25, 38, 7, 89, 78, 72, 26, 74, 8, 90, 88, 57, 68, 30, 24, 12, 65, 48, 67, 85, 91, 29, 28, 3, 10, 18, 6, 49, 13, 54, 27, 83, 76, 62, 70, 15, 86, 53, 44, 4, 34, 17, 47, 52, 64, 9, 39, 71, 32, 46, 63, 59, 37, 23, 61, 42, 82, 58, 2, 43, 6};
//        int[] tmSolutionLine7  = new int[]{60, 71, 31, 30, 59, 43, 7, 49, 54, 61, 55, 27, 83, 74, 72, 11, 33, 82, 84, 62, 63, 37, 65, 5, 78, 1, 67, 3, 19, 79, 2, 56, 28, 48, 4, 36, 47, 57, 39, 41, 75, 9, 18, 0, 35, 73, 14, 24, 77, 52, 13, 66, 46, 51, 23, 40, 81, 22, 92, 50, 6, 69, 89, 25, 38, 90, 44, 21, 20, 64, 16, 32, 80, 12, 29, 88, 87, 26, 34, 85, 91, 58, 8, 76, 70, 10, 68, 53, 15, 86, 17, 45, 42, 10};
//        int[] tmSolutionLine8  = new int[]{37, 78, 64, 28, 3, 17, 73, 84, 23, 25, 31, 62, 19, 5, 9, 39, 71, 16, 56, 81, 86, 46, 74, 18, 66, 13, 1, 24, 79, 11, 61, 15, 26, 29, 42, 51, 44, 65, 2, 55, 89, 33, 90, 88, 48, 6, 12, 45, 67, 50, 14, 52, 20, 0, 27, 47, 72, 63, 8, 92, 34, 57, 76, 58, 77, 83, 82, 53, 22, 38, 87, 32, 60, 7, 30, 40, 35, 68, 41, 21, 91, 43, 36, 4, 80, 10, 54, 49, 85, 59, 70, 69, 75, 6};
//        int[] tmSolutionLine9  = new int[]{42, 10, 3, 15, 16, 65, 4, 12, 89, 27, 41, 67, 82, 24, 5, 31, 1, 55, 73, 69, 25, 79, 48, 22, 51, 75, 57, 50, 61, 23, 58, 77, 87, 40, 6, 26, 43, 19, 53, 92, 68, 13, 59, 90, 35, 84, 8, 7, 0, 14, 81, 88, 78, 91, 38, 33, 74, 18, 11, 36, 28, 60, 63, 30, 86, 49, 21, 85, 20, 72, 45, 44, 64, 34, 17, 47, 52, 70, 9, 39, 71, 32, 66, 54, 80, 29, 2, 56, 76, 62, 46, 83, 37, 9};
//        int[] tmSolutionLine10  = new int[]{85, 61, 28, 39, 58, 68, 87, 54, 77, 27, 31, 60, 65, 29, 46, 23, 76, 47, 37, 11, 17, 73, 81, 78, 57, 13, 41, 82, 71, 79, 18, 38, 92, 44, 83, 7, 64, 19, 66, 3, 24, 63, 16, 33, 49, 5, 50, 9, 25, 51, 52, 55, 75, 69, 88, 67, 84, 12, 62, 22, 42, 8, 48, 59, 6, 72, 70, 14, 2, 56, 53, 0, 45, 89, 30, 40, 35, 15, 26, 21, 91, 43, 36, 4, 80, 10, 86, 90, 34, 20, 74, 1, 32, 8};
//        int[] tmSolutionLine11  = new int[]{10, 45, 80, 87, 41, 53, 28, 19, 37, 89, 31, 62, 61, 5, 9, 39, 71, 16, 56, 74, 86, 46, 81, 18, 11, 13, 1, 24, 79, 82, 3, 15, 26, 20, 63, 29, 90, 64, 27, 34, 57, 4, 44, 38, 68, 17, 91, 76, 6, 51, 52, 23, 75, 69, 83, 67, 84, 30, 55, 32, 65, 8, 47, 59, 25, 72, 70, 22, 2, 58, 48, 0, 42, 33, 12, 66, 35, 40, 88, 21, 50, 43, 36, 77, 73, 54, 92, 49, 60, 7, 85, 14, 78, 3};
//        int[] tmSolutionLine12  = new int[]{45, 46, 40, 55, 72, 23, 49, 82, 59, 21, 81, 88, 52, 71, 70, 91, 32, 4, 84, 44, 89, 68, 65, 5, 85, 17, 67, 3, 22, 79, 2, 10, 28, 64, 26, 53, 47, 61, 38, 41, 20, 9, 18, 0, 56, 73, 6, 92, 24, 57, 62, 74, 69, 19, 51, 76, 29, 90, 7, 35, 39, 13, 30, 80, 86, 34, 11, 33, 78, 75, 58, 16, 15, 25, 8, 48, 50, 37, 31, 63, 1, 60, 83, 87, 42, 36, 14, 77, 27, 54, 12, 43, 66, 9};
//        int[] tmSolutionLine13  = new int[]{18, 48, 66, 50, 4, 10, 70, 87, 56, 27, 85, 43, 45, 29, 41, 84, 76, 47, 62, 33, 17, 73, 81, 78, 57, 13, 3, 42, 90, 0, 2, 36, 35, 60, 77, 32, 79, 69, 16, 44, 64, 14, 1, 88, 52, 55, 6, 68, 67, 65, 58, 89, 11, 5, 82, 49, 92, 34, 51, 12, 30, 54, 39, 24, 74, 80, 31, 75, 40, 20, 72, 71, 28, 25, 8, 21, 9, 38, 26, 15, 91, 83, 46, 63, 59, 37, 23, 22, 19, 7, 61, 53, 86, 8};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //9号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{19, 12, 49, 25, 37, 44, 69, 65, 89, 84, 90, 13, 76, 28, 20, 8, 1, 3, 71, 56, 53, 85, 6, 58, 47, 63, 14, 32, 41, 92, 78, 55, 82, 21, 45, 26, 70, 4, 91, 57, 18, 33, 0, 52, 29, 87, 86, 30, 80, 36, 60, 81, 43, 75, 51, 54, 68, 27, 35, 62, 73, 64, 34, 66, 59, 79, 15, 48, 7, 16, 5, 9, 39, 11, 22, 42, 40, 72, 31, 24, 83, 23, 50, 88, 67, 77, 74, 10, 8, 46, 17, 2, 38, 5};
//        int[] tmSolutionLine2  = new int[]{44, 65, 28, 35, 88, 30, 17, 37, 25, 71, 31, 11, 4, 34, 14, 18, 68, 10, 77, 1, 21, 80, 55, 2, 90, 73, 54, 32, 41, 92, 78, 51, 74, 45, 6, 16, 82, 48, 3, 58, 12, 29, 0, 15, 64, 75, 62, 49, 86, 83, 56, 91, 40, 20, 5, 24, 23, 57, 7, 47, 42, 13, 69, 66, 27, 36, 50, 67, 53, 89, 76, 63, 33, 39, 43, 52, 38, 8, 59, 79, 26, 84, 9, 60, 19, 87, 46, 70, 81, 22, 85, 72, 61, 5};
//        int[] tmSolutionLine3  = new int[]{13, 75, 91, 68, 89, 10, 50, 16, 85, 82, 60, 17, 4, 34, 18, 38, 42, 59, 36, 67, 15, 3, 70, 1, 32, 84, 92, 41, 90, 30, 54, 5, 63, 51, 62, 46, 71, 0, 29, 56, 12, 25, 58, 78, 64, 81, 7, 47, 49, 8, 35, 33, 87, 20, 74, 24, 28, 57, 6, 37, 61, 13, 69, 66, 27, 55, 52, 40, 44, 76, 43, 14, 39, 86, 65, 83, 23, 22, 88, 48, 79, 80, 77, 2, 9, 19, 26, 73, 53, 72, 45, 21, 31, 5};
//        int[] tmSolutionLine4  = new int[]{71, 65, 58, 63, 88, 33, 17, 23, 29, 89, 60, 66, 8, 2, 38, 87, 41, 45, 12, 57, 21, 80, 74, 61, 10, 11, 82, 53, 4, 30, 56, 76, 9, 64, 20, 90, 16, 70, 5, 1, 32, 46, 72, 7, 37, 13, 54, 51, 22, 6, 86, 39, 62, 15, 36, 35, 25, 3, 19, 55, 79, 34, 81, 28, 75, 91, 40, 85, 84, 27, 83, 26, 43, 24, 48, 59, 92, 42, 31, 47, 18, 69, 78, 49, 67, 77, 73, 0, 68, 52, 14, 44, 50, 9};
//        int[] tmSolutionLine5  = new int[]{49, 15, 78, 36, 64, 75, 92, 51, 0, 44, 8, 83, 88, 25, 70, 37, 79, 73, 68, 13, 76, 43, 4, 5, 86, 53, 84, 38, 42, 24, 23, 1, 63, 14, 62, 46, 71, 57, 58, 81, 21, 7, 28, 48, 29, 87, 54, 47, 31, 35, 74, 6, 66, 11, 26, 45, 40, 55, 80, 69, 17, 3, 50, 19, 85, 89, 16, 56, 61, 82, 30, 22, 65, 67, 12, 27, 9, 59, 10, 91, 32, 34, 77, 2, 18, 39, 90, 20, 72, 33, 41, 52, 60, 9};
//        int[] tmSolutionLine6  = new int[]{0, 40, 77, 19, 88, 87, 11, 25, 26, 50, 2, 20, 92, 7, 72, 53, 23, 75, 79, 54, 62, 32, 21, 56, 61, 41, 69, 35, 90, 38, 91, 51, 74, 45, 6, 16, 82, 48, 3, 58, 10, 68, 84, 63, 78, 15, 8, 76, 39, 52, 47, 71, 73, 29, 36, 49, 67, 57, 81, 4, 42, 13, 59, 66, 27, 14, 46, 55, 24, 64, 85, 18, 43, 33, 65, 83, 34, 5, 30, 86, 28, 37, 70, 9, 80, 31, 60, 22, 1, 44, 89, 17, 12, 15};
//        int[] tmSolutionLine7  = new int[]{19, 12, 28, 35, 88, 75, 15, 60, 2, 54, 61, 5, 84, 78, 20, 89, 11, 21, 3, 56, 53, 34, 6, 58, 45, 41, 92, 68, 90, 50, 38, 51, 22, 71, 80, 46, 30, 40, 47, 37, 10, 17, 74, 76, 85, 25, 16, 70, 29, 8, 82, 0, 87, 72, 36, 49, 67, 57, 59, 4, 42, 13, 69, 66, 27, 18, 91, 55, 24, 64, 83, 26, 79, 33, 65, 43, 23, 63, 32, 77, 39, 1, 14, 86, 62, 81, 9, 31, 48, 7, 52, 44, 73, 4};
//        int[] tmSolutionLine8  = new int[]{23, 63, 52, 32, 51, 41, 85, 35, 64, 56, 76, 24, 92, 70, 58, 42, 68, 33, 80, 15, 38, 53, 57, 3, 11, 2, 84, 86, 46, 37, 87, 16, 82, 21, 45, 26, 17, 69, 40, 78, 14, 4, 74, 67, 27, 79, 54, 77, 89, 39, 7, 91, 73, 47, 36, 13, 49, 6, 19, 55, 72, 9, 81, 28, 75, 34, 62, 31, 5, 61, 25, 0, 60, 48, 43, 20, 59, 8, 83, 29, 65, 66, 10, 18, 1, 50, 90, 12, 44, 22, 88, 30, 71, 6};
//        int[] tmSolutionLine9  = new int[]{32, 67, 44, 28, 59, 30, 75, 42, 81, 74, 60, 66, 40, 56, 61, 88, 20, 41, 54, 71, 53, 85, 6, 58, 45, 4, 77, 83, 21, 33, 38, 12, 11, 78, 26, 18, 90, 31, 5, 1, 17, 43, 68, 63, 37, 25, 14, 57, 92, 9, 84, 0, 35, 72, 48, 73, 47, 76, 19, 64, 79, 65, 39, 29, 7, 27, 8, 91, 52, 50, 89, 49, 13, 10, 34, 46, 82, 3, 87, 55, 16, 69, 80, 24, 22, 2, 86, 67, 62, 23, 70, 36, 51, 12};
//        int[] tmSolutionLine10  = new int[]{70, 56, 30, 2, 72, 75, 1, 84, 59, 24, 60, 9, 47, 80, 16, 29, 32, 41, 14, 71, 53, 85, 6, 58, 45, 87, 23, 3, 17, 33, 38, 44, 11, 78, 26, 5, 18, 57, 66, 81, 21, 61, 68, 83, 37, 12, 54, 31, 92, 63, 90, 0, 19, 22, 15, 52, 82, 76, 77, 25, 46, 10, 4, 39, 27, 36, 42, 86, 73, 13, 79, 65, 34, 91, 88, 55, 20, 49, 48, 28, 50, 74, 67, 7, 51, 35, 43, 8, 64, 69, 40, 62, 89, 4};
//        int[] tmSolutionLine11  = new int[]{86, 66, 44, 59, 12, 30, 17, 37, 25, 38, 31, 11, 47, 80, 53, 14, 68, 10, 77, 1, 74, 34, 51, 2, 90, 73, 48, 32, 41, 5, 78, 55, 21, 23, 63, 29, 43, 40, 3, 84, 65, 20, 26, 22, 9, 39, 27, 16, 46, 83, 70, 35, 79, 85, 87, 36, 19, 57, 7, 0, 42, 72, 89, 82, 64, 50, 56, 49, 8, 6, 60, 24, 18, 28, 76, 58, 67, 81, 75, 92, 45, 4, 88, 15, 52, 71, 91, 13, 33, 54, 61, 62, 69, 3};
//        int[] tmSolutionLine12  = new int[]{52, 82, 24, 63, 29, 53, 64, 49, 20, 41, 89, 0, 14, 47, 66, 92, 65, 45, 76, 79, 85, 10, 74, 56, 8, 4, 77, 83, 21, 33, 38, 12, 11, 78, 26, 16, 18, 70, 5, 55, 15, 46, 80, 7, 37, 13, 54, 51, 22, 6, 86, 39, 62, 32, 36, 35, 25, 3, 19, 1, 57, 23, 81, 28, 75, 9, 40, 34, 84, 72, 73, 17, 42, 61, 43, 87, 58, 68, 31, 90, 2, 69, 67, 91, 71, 50, 59, 48, 44, 30, 60, 88, 27, 6};
//        int[] tmSolutionLine13  = new int[]{31, 12, 61, 82, 5, 77, 11, 81, 26, 20, 2, 86, 19, 7, 52, 32, 23, 75, 43, 54, 53, 34, 6, 58, 45, 41, 92, 68, 90, 50, 38, 51, 74, 71, 80, 46, 30, 40, 47, 37, 10, 17, 22, 76, 85, 39, 64, 78, 3, 83, 70, 0, 87, 72, 36, 49, 67, 57, 59, 4, 42, 13, 69, 66, 27, 18, 91, 24, 8, 21, 60, 65, 1, 89, 84, 15, 63, 79, 28, 62, 44, 56, 9, 88, 73, 14, 25, 48, 33, 55, 35, 29, 16, 8};
//        int[] tmSolutionLine14  = new int[]{10, 48, 16, 74, 49, 13, 83, 35, 64, 56, 76, 24, 92, 90, 58, 57, 51, 33, 78, 15, 19, 53, 68, 73, 11, 3, 84, 86, 46, 37, 52, 70, 82, 22, 45, 26, 85, 21, 40, 80, 14, 41, 38, 67, 27, 42, 54, 77, 31, 72, 79, 60, 43, 5, 32, 29, 28, 47, 59, 69, 71, 17, 34, 6, 91, 63, 2, 23, 36, 65, 81, 50, 12, 44, 9, 39, 25, 7, 18, 75, 20, 4, 0, 8, 66, 55, 1, 88, 30, 62, 89, 87, 61, 9};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //10号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{49, 32, 84, 53, 88, 58, 73, 63, 60, 59, 70, 22, 18, 69, 89, 80, 50, 15, 54, 67, 13, 2, 36, 83, 48, 11, 12, 74, 28, 86, 23, 1, 52, 68, 37, 66, 5, 47, 24, 85, 8, 79, 87, 45, 41, 9, 33, 10, 14, 64, 27, 3, 0, 35, 77, 43, 71, 38, 62, 19, 42, 30, 65, 55, 21, 91, 72, 51, 4, 44, 76, 90, 82, 26, 57, 39, 6, 78, 16, 46, 20, 61, 40, 81, 7, 25, 17, 92, 31, 34, 29, 75, 56, 8};
//        int[] tmSolutionLine2  = new int[]{24, 16, 37, 14, 13, 42, 0, 80, 53, 63, 60, 62, 52, 18, 30, 56, 78, 29, 55, 57, 22, 75, 44, 3, 8, 65, 6, 33, 74, 5, 77, 48, 72, 61, 25, 28, 86, 11, 50, 73, 15, 20, 84, 51, 41, 66, 2, 1, 34, 82, 19, 43, 38, 46, 9, 90, 23, 59, 71, 12, 76, 4, 92, 10, 47, 36, 69, 79, 89, 39, 35, 21, 49, 7, 85, 91, 70, 40, 54, 67, 81, 26, 64, 83, 31, 32, 68, 88, 87, 45, 58, 17, 27, 8};
//        int[] tmSolutionLine3  = new int[]{61, 50, 68, 14, 28, 75, 64, 34, 33, 37, 27, 81, 59, 69, 40, 41, 53, 23, 85, 3, 92, 87, 49, 35, 7, 78, 48, 6, 58, 52, 16, 32, 89, 54, 44, 39, 46, 15, 19, 24, 51, 36, 62, 65, 88, 57, 83, 1, 76, 9, 11, 67, 21, 47, 72, 63, 82, 79, 90, 4, 43, 10, 80, 17, 77, 60, 56, 31, 8, 13, 29, 25, 26, 22, 30, 86, 2, 42, 12, 71, 5, 73, 55, 84, 70, 91, 45, 20, 0, 74, 18, 38, 66, 8};
//        int[] tmSolutionLine4  = new int[]{53, 75, 59, 3, 74, 46, 18, 67, 80, 73, 71, 4, 90, 22, 8, 20, 52, 58, 78, 72, 81, 43, 84, 50, 54, 11, 14, 79, 88, 48, 76, 27, 1, 70, 45, 44, 65, 64, 24, 60, 87, 83, 21, 55, 32, 66, 16, 36, 23, 42, 19, 12, 38, 61, 77, 85, 89, 25, 62, 29, 5, 92, 51, 41, 49, 63, 15, 91, 37, 17, 86, 34, 40, 31, 2, 7, 28, 57, 30, 68, 82, 47, 69, 9, 35, 26, 13, 10, 6, 56, 39, 0, 33, 6};
//        int[] tmSolutionLine5  = new int[]{49, 32, 84, 58, 77, 16, 80, 42, 67, 73, 27, 78, 45, 69, 43, 41, 1, 17, 61, 89, 75, 25, 85, 35, 26, 14, 51, 6, 63, 52, 83, 20, 48, 50, 64, 11, 81, 24, 87, 18, 30, 2, 0, 60, 37, 88, 74, 40, 54, 82, 57, 31, 38, 46, 9, 90, 23, 59, 71, 12, 76, 4, 92, 10, 47, 36, 28, 79, 39, 5, 33, 86, 55, 62, 65, 91, 19, 15, 44, 53, 8, 29, 34, 13, 56, 22, 66, 21, 70, 68, 3, 7, 72, 8};
//        int[] tmSolutionLine6  = new int[]{2, 41, 4, 53, 31, 10, 0, 86, 63, 67, 28, 72, 73, 90, 5, 47, 70, 25, 85, 3, 92, 78, 7, 89, 14, 81, 48, 6, 58, 21, 16, 32, 35, 80, 44, 39, 46, 15, 19, 24, 51, 36, 62, 65, 88, 57, 83, 1, 76, 56, 11, 66, 38, 74, 77, 43, 71, 79, 8, 29, 75, 26, 42, 55, 18, 9, 27, 54, 20, 40, 59, 61, 30, 69, 91, 49, 37, 60, 12, 52, 33, 87, 45, 23, 22, 82, 50, 64, 68, 34, 84, 17, 13, 12};
//        int[] tmSolutionLine7  = new int[]{88, 25, 51, 83, 82, 40, 17, 53, 58, 32, 84, 28, 63, 91, 87, 37, 22, 43, 45, 57, 46, 24, 9, 62, 64, 75, 6, 14, 68, 41, 11, 30, 48, 85, 54, 81, 50, 89, 79, 60, 31, 2, 0, 55, 16, 73, 9, 21, 5, 70, 67, 29, 38, 61, 77, 90, 23, 59, 71, 12, 76, 4, 92, 10, 47, 36, 18, 80, 39, 35, 33, 69, 1, 86, 49, 65, 19, 15, 44, 74, 8, 20, 34, 13, 56, 78, 66, 42, 27, 52, 3, 7, 72, 4};
//        int[] tmSolutionLine8  = new int[]{33, 13, 38, 42, 20, 63, 7, 92, 67, 45, 27, 78, 44, 69, 43, 41, 1, 91, 61, 89, 75, 71, 85, 35, 14, 81, 51, 6, 22, 52, 83, 88, 3, 54, 29, 39, 46, 24, 19, 18, 60, 50, 56, 36, 32, 57, 16, 65, 76, 17, 53, 79, 21, 47, 72, 25, 9, 59, 90, 2, 40, 10, 80, 62, 23, 12, 11, 37, 48, 49, 5, 58, 82, 68, 8, 4, 66, 73, 34, 84, 74, 70, 30, 15, 86, 0, 87, 77, 64, 31, 28, 26, 55, 10};
//        int[] tmSolutionLine9  = new int[]{54, 30, 4, 26, 3, 31, 18, 88, 11, 73, 22, 58, 25, 21, 27, 80, 12, 75, 56, 69, 15, 90, 17, 29, 55, 92, 87, 76, 71, 0, 19, 37, 35, 59, 44, 39, 70, 52, 61, 10, 81, 40, 53, 46, 16, 24, 28, 49, 51, 65, 43, 68, 23, 11, 78, 33, 86, 14, 74, 41, 66, 45, 38, 50, 9, 36, 48, 79, 91, 89, 32, 72, 85, 57, 5, 1, 67, 60, 47, 34, 2, 42, 64, 7, 13, 82, 77, 83, 20, 63, 62, 84, 6, 8};
//        int[] tmSolutionLine10  = new int[]{6, 78, 3, 33, 64, 30, 18, 75, 80, 73, 71, 24, 5, 48, 8, 20, 61, 58, 2, 72, 81, 43, 57, 50, 54, 11, 83, 74, 88, 22, 76, 27, 1, 89, 45, 44, 65, 23, 4, 86, 42, 47, 90, 46, 10, 84, 28, 29, 51, 12, 52, 68, 16, 62, 26, 82, 67, 14, 13, 41, 66, 37, 85, 9, 56, 36, 69, 79, 39, 55, 35, 21, 49, 7, 60, 31, 70, 0, 38, 87, 53, 25, 92, 59, 91, 17, 19, 77, 15, 34, 63, 32, 40, 9};
//        int[] tmSolutionLine11  = new int[]{28, 66, 36, 54, 7, 18, 29, 43, 40, 50, 62, 74, 86, 51, 85, 41, 73, 35, 4, 57, 46, 24, 44, 3, 64, 76, 53, 13, 52, 21, 56, 32, 31, 80, 60, 19, 61, 17, 67, 33, 30, 81, 45, 91, 23, 16, 47, 87, 9, 70, 48, 65, 26, 5, 89, 90, 15, 38, 0, 39, 69, 68, 11, 78, 55, 34, 88, 37, 92, 71, 58, 1, 12, 10, 2, 72, 59, 14, 42, 27, 49, 79, 75, 8, 25, 82, 77, 83, 20, 63, 22, 84, 6, 8};
//        int[] tmSolutionLine12  = new int[]{92, 8, 44, 26, 3, 32, 35, 17, 49, 75, 77, 4, 59, 91, 85, 37, 22, 87, 45, 57, 46, 2, 19, 36, 54, 11, 12, 74, 28, 27, 23, 1, 88, 68, 41, 84, 55, 47, 24, 60, 71, 73, 80, 61, 64, 69, 33, 39, 43, 7, 58, 6, 78, 70, 21, 5, 65, 53, 0, 16, 48, 63, 52, 83, 62, 79, 56, 31, 18, 13, 29, 86, 50, 82, 90, 38, 67, 81, 14, 20, 10, 25, 34, 40, 51, 15, 66, 89, 9, 76, 42, 72, 30, 8};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//
//
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //11号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{5, 20, 51, 56, 6, 81, 28, 21, 41, 23, 40, 84, 66, 3, 33, 24, 54, 82, 19, 1, 31, 79, 65, 53, 49, 46, 85, 17, 10, 69, 67, 91, 87, 62, 64, 57, 72, 18, 76, 13, 16, 30, 0, 43, 59, 88, 77, 74, 11, 61, 12, 37, 2, 45, 73, 34, 26, 90, 55, 9, 4, 7, 71, 38, 8, 36, 86, 50, 52, 89, 29, 80, 48, 58, 78, 35, 60, 75, 22, 70, 83, 27, 32, 68, 63, 92, 44, 39, 15, 25, 42, 47, 14, 4};
//        int[] tmSolutionLine2  = new int[]{73, 19, 69, 30, 25, 80, 32, 62, 7, 43, 53, 74, 55, 50, 40, 23, 85, 39, 33, 78, 49, 71, 44, 63, 15, 13, 60, 35, 70, 65, 41, 11, 54, 2, 52, 34, 92, 51, 61, 28, 0, 9, 72, 17, 88, 83, 38, 86, 81, 48, 14, 24, 36, 64, 84, 20, 77, 42, 79, 27, 57, 3, 91, 22, 18, 56, 21, 82, 59, 31, 46, 5, 75, 26, 58, 4, 90, 16, 47, 67, 1, 45, 37, 66, 29, 68, 89, 6, 8, 76, 12, 87, 10, 5};
//        int[] tmSolutionLine3  = new int[]{14, 10, 82, 21, 3, 75, 40, 36, 50, 32, 9, 24, 44, 46, 23, 55, 43, 70, 63, 53, 90, 78, 80, 25, 4, 37, 92, 88, 7, 38, 81, 84, 87, 62, 28, 41, 54, 71, 77, 12, 8, 49, 67, 69, 76, 33, 60, 52, 22, 86, 79, 29, 0, 30, 61, 20, 17, 47, 58, 42, 73, 5, 18, 11, 15, 91, 64, 51, 31, 16, 59, 72, 56, 45, 2, 83, 27, 48, 57, 1, 74, 68, 39, 26, 65, 89, 85, 19, 6, 35, 66, 13, 34, 11};
//        int[] tmSolutionLine4  = new int[]{34, 58, 8, 72, 63, 25, 57, 5, 15, 47, 67, 83, 55, 23, 19, 35, 12, 39, 54, 21, 60, 85, 7, 68, 4, 9, 92, 1, 84, 73, 38, 11, 69, 42, 71, 50, 49, 22, 52, 28, 43, 24, 17, 16, 87, 70, 88, 48, 30, 3, 29, 0, 45, 65, 79, 20, 51, 82, 14, 26, 78, 6, 33, 64, 76, 56, 75, 59, 77, 31, 44, 91, 36, 89, 90, 18, 80, 40, 61, 81, 66, 27, 86, 74, 37, 46, 41, 13, 32, 62, 53, 10, 2, 10};
//        int[] tmSolutionLine5  = new int[]{64, 83, 42, 50, 86, 71, 48, 22, 78, 90, 4, 0, 2, 41, 14, 82, 76, 40, 38, 17, 53, 57, 60, 12, 3, 59, 1, 84, 6, 66, 58, 69, 25, 88, 9, 8, 63, 24, 31, 33, 51, 44, 7, 56, 70, 74, 35, 67, 45, 28, 19, 75, 79, 85, 43, 61, 54, 30, 47, 92, 65, 46, 55, 49, 81, 77, 18, 39, 68, 91, 23, 32, 89, 15, 16, 80, 72, 5, 21, 34, 87, 62, 29, 36, 26, 20, 37, 73, 27, 52, 10, 13, 11, 7};
//        int[] tmSolutionLine6  = new int[]{2, 34, 88, 7, 16, 38, 23, 33, 90, 10, 17, 57, 70, 69, 92, 26, 8, 40, 27, 62, 12, 39, 18, 60, 63, 85, 58, 73, 72, 52, 48, 13, 68, 44, 41, 21, 87, 15, 77, 80, 3, 43, 5, 31, 55, 66, 9, 42, 0, 86, 51, 36, 1, 67, 30, 20, 53, 4, 11, 56, 47, 65, 61, 82, 76, 50, 14, 24, 49, 37, 29, 64, 75, 54, 78, 81, 91, 28, 6, 71, 83, 79, 22, 19, 32, 35, 25, 59, 89, 45, 84, 74, 46, 7};
//        int[] tmSolutionLine7  = new int[]{11, 90, 69, 62, 25, 80, 43, 60, 7, 34, 53, 74, 55, 50, 40, 23, 85, 39, 4, 1, 52, 0, 82, 31, 66, 64, 67, 17, 73, 30, 18, 89, 12, 13, 8, 91, 28, 87, 59, 49, 45, 61, 41, 48, 88, 54, 38, 86, 65, 72, 14, 24, 36, 44, 79, 20, 77, 42, 84, 15, 6, 46, 16, 29, 33, 3, 47, 26, 71, 37, 81, 21, 19, 58, 78, 35, 2, 75, 22, 70, 83, 27, 32, 68, 63, 92, 5, 56, 57, 10, 9, 51, 76, 4};
//        int[] tmSolutionLine8  = new int[]{85, 75, 45, 69, 25, 22, 11, 74, 31, 48, 44, 6, 10, 41, 38, 63, 89, 28, 9, 78, 12, 56, 86, 88, 59, 64, 58, 18, 80, 52, 81, 60, 92, 27, 84, 68, 35, 87, 53, 65, 16, 57, 40, 7, 17, 39, 71, 54, 67, 0, 20, 33, 13, 72, 79, 90, 32, 8, 3, 50, 49, 21, 34, 5, 19, 29, 62, 73, 47, 76, 66, 23, 77, 55, 46, 26, 24, 30, 82, 4, 83, 1, 91, 14, 51, 61, 37, 70, 42, 43, 15, 36, 2, 9};
//        int[] tmSolutionLine9  = new int[]{47, 4, 70, 6, 25, 66, 43, 67, 20, 10, 48, 12, 46, 16, 77, 26, 21, 86, 85, 8, 11, 39, 52, 88, 84, 41, 15, 18, 34, 38, 17, 64, 40, 76, 53, 72, 63, 9, 65, 1, 92, 5, 57, 55, 75, 74, 28, 62, 30, 31, 37, 35, 29, 82, 59, 7, 49, 61, 90, 80, 14, 27, 50, 60, 69, 3, 33, 58, 45, 13, 73, 51, 68, 54, 78, 89, 91, 44, 42, 71, 23, 79, 22, 19, 2, 36, 83, 0, 81, 24, 56, 32, 87, 8};
//        int[] tmSolutionLine10  = new int[]{80, 32, 69, 29, 33, 13, 38, 28, 40, 60, 77, 87, 10, 3, 61, 24, 54, 76, 14, 81, 42, 55, 44, 53, 71, 82, 67, 92, 9, 43, 0, 41, 58, 39, 47, 70, 25, 73, 62, 12, 8, 49, 23, 50, 21, 5, 30, 22, 17, 56, 63, 88, 90, 4, 79, 34, 59, 89, 20, 75, 64, 78, 18, 74, 15, 36, 66, 65, 31, 16, 37, 72, 91, 86, 2, 83, 46, 48, 57, 1, 11, 68, 45, 26, 51, 27, 85, 19, 6, 35, 52, 84, 7, 8};
//        int[] tmSolutionLine11  = new int[]{32, 28, 6, 3, 54, 35, 74, 67, 90, 33, 49, 51, 16, 29, 22, 69, 8, 61, 27, 62, 12, 40, 86, 17, 63, 85, 58, 73, 79, 52, 48, 9, 19, 18, 41, 68, 87, 15, 77, 80, 13, 72, 5, 60, 55, 20, 2, 65, 78, 84, 11, 0, 50, 46, 59, 70, 21, 44, 47, 26, 25, 88, 53, 4, 81, 14, 83, 66, 38, 23, 7, 1, 82, 56, 71, 91, 43, 31, 34, 45, 75, 89, 24, 92, 10, 76, 30, 57, 36, 64, 37, 39, 42, 8};
//        int[] tmSolutionLine12  = new int[]{12, 59, 27, 65, 28, 25, 4, 73, 15, 34, 29, 0, 56, 79, 40, 92, 67, 80, 20, 13, 37, 46, 86, 52, 51, 57, 1, 74, 9, 39, 5, 54, 77, 66, 88, 24, 85, 55, 90, 62, 30, 38, 42, 22, 71, 48, 16, 64, 26, 43, 82, 69, 2, 8, 47, 76, 49, 36, 84, 41, 44, 60, 91, 11, 63, 50, 17, 61, 89, 78, 81, 21, 19, 58, 31, 35, 10, 75, 72, 70, 83, 68, 32, 23, 18, 6, 45, 3, 53, 87, 33, 7, 14, 10};
//        int[] tmSolutionLine13  = new int[]{81, 57, 17, 8, 21, 84, 28, 39, 74, 1, 69, 2, 63, 60, 19, 92, 34, 80, 45, 70, 31, 67, 86, 66, 48, 22, 13, 35, 10, 51, 64, 58, 18, 65, 41, 47, 88, 50, 56, 73, 91, 62, 85, 29, 87, 61, 32, 52, 59, 44, 75, 40, 38, 4, 68, 49, 46, 16, 9, 15, 6, 77, 42, 79, 33, 3, 43, 26, 71, 37, 0, 30, 27, 11, 7, 72, 82, 55, 54, 76, 5, 89, 14, 78, 53, 25, 83, 90, 20, 12, 23, 24, 36, 10};
//        int[] tmSolutionLine14  = new int[]{42, 65, 85, 46, 29, 15, 18, 21, 80, 13, 14, 6, 10, 41, 38, 63, 89, 28, 9, 78, 47, 87, 57, 32, 12, 44, 17, 39, 70, 3, 5, 54, 51, 66, 88, 24, 77, 55, 90, 62, 8, 49, 67, 69, 76, 33, 60, 52, 22, 86, 79, 30, 50, 16, 26, 59, 43, 72, 0, 2, 25, 74, 84, 4, 20, 53, 83, 11, 19, 23, 7, 1, 58, 81, 71, 91, 48, 31, 34, 45, 75, 35, 73, 92, 36, 56, 37, 40, 61, 27, 68, 82, 64, 3};
//        int[] tmSolutionLine15  = new int[]{36, 59, 28, 80, 25, 33, 70, 72, 31, 49, 32, 4, 66, 29, 22, 14, 62, 24, 27, 8, 12, 40, 86, 17, 63, 11, 58, 73, 79, 52, 81, 42, 92, 54, 9, 47, 69, 50, 53, 30, 75, 18, 64, 7, 38, 61, 71, 46, 15, 0, 91, 5, 77, 44, 16, 45, 13, 88, 3, 87, 39, 37, 34, 60, 67, 41, 83, 21, 10, 76, 84, 23, 2, 55, 82, 90, 51, 20, 57, 1, 74, 68, 48, 26, 65, 89, 85, 19, 6, 35, 78, 43, 56, 4};
//        int[] tmSolutionLine16  = new int[]{86, 30, 20, 6, 25, 36, 48, 16, 56, 68, 91, 39, 83, 17, 0, 8, 38, 84, 89, 43, 81, 79, 22, 11, 52, 41, 37, 18, 34, 58, 5, 64, 40, 76, 45, 72, 63, 13, 65, 29, 92, 12, 57, 55, 69, 28, 44, 62, 80, 31, 15, 35, 73, 60, 59, 7, 49, 61, 90, 66, 33, 27, 50, 67, 87, 3, 47, 51, 77, 85, 10, 88, 75, 19, 71, 78, 21, 23, 2, 46, 70, 54, 53, 9, 14, 82, 32, 4, 42, 74, 24, 1, 26, 11};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//        tmSolution.add(tmSolutionLine15);
//        tmSolution.add(tmSolutionLine16);
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);


        //12号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{19, 66, 68, 6, 7, 74, 38, 87, 9, 43, 53, 51, 2, 0, 64, 15, 50, 4, 10, 36, 70, 81, 20, 41, 26, 77, 25, 18, 39, 78, 67, 3, 65, 29, 71, 24, 91, 11, 82, 21, 27, 1, 46, 85, 73, 16, 56, 60, 32, 79, 12, 31, 30, 61, 47, 14, 90, 75, 35, 34, 5, 40, 63, 8, 55, 49, 84, 58, 42, 88, 48, 37, 92, 22, 57, 17, 62, 72, 28, 89, 13, 45, 52, 76, 83, 23, 86, 59, 80, 54, 69, 44, 33, 8};
//        int[] tmSolutionLine2  = new int[]{8, 65, 11, 91, 16, 28, 87, 35, 39, 21, 3, 25, 49, 77, 72, 89, 85, 26, 80, 22, 44, 81, 4, 56, 20, 82, 88, 33, 7, 62, 10, 1, 78, 60, 58, 47, 36, 90, 53, 19, 41, 17, 79, 57, 40, 51, 67, 46, 2, 48, 45, 31, 50, 30, 68, 86, 59, 73, 66, 0, 70, 12, 71, 75, 37, 74, 15, 13, 52, 27, 5, 42, 64, 76, 61, 38, 14, 24, 23, 43, 55, 6, 54, 9, 18, 32, 29, 84, 69, 92, 63, 34, 83, 8};
//        int[] tmSolutionLine3  = new int[]{51, 3, 29, 66, 53, 34, 45, 26, 91, 77, 72, 57, 83, 41, 56, 16, 2, 84, 13, 47, 32, 73, 20, 61, 69, 22, 39, 10, 78, 65, 37, 68, 88, 75, 74, 24, 19, 31, 36, 50, 59, 18, 9, 86, 40, 82, 85, 8, 67, 92, 23, 79, 89, 44, 15, 48, 5, 62, 87, 76, 30, 55, 63, 12, 35, 46, 25, 6, 33, 43, 60, 64, 28, 52, 42, 38, 54, 81, 71, 17, 0, 14, 4, 80, 58, 21, 1, 70, 27, 7, 90, 49, 11, 15};
//        int[] tmSolutionLine4  = new int[]{17, 52, 70, 39, 49, 26, 87, 71, 20, 1, 86, 21, 63, 51, 3, 24, 85, 4, 10, 36, 25, 23, 77, 78, 73, 29, 88, 31, 7, 81, 68, 65, 9, 35, 42, 22, 50, 84, 60, 47, 59, 56, 34, 37, 90, 69, 45, 64, 89, 46, 19, 18, 2, 44, 15, 48, 5, 62, 57, 0, 12, 40, 66, 80, 61, 74, 82, 13, 55, 32, 33, 8, 41, 14, 27, 75, 16, 43, 11, 54, 38, 91, 28, 76, 58, 67, 79, 92, 53, 83, 30, 6, 72, 6};
//        int[] tmSolutionLine5  = new int[]{28, 3, 84, 66, 53, 74, 55, 7, 6, 54, 75, 60, 2, 0, 87, 15, 50, 64, 48, 62, 56, 69, 68, 65, 63, 70, 34, 59, 44, 72, 85, 9, 22, 35, 18, 21, 25, 11, 36, 4, 12, 1, 46, 37, 83, 38, 17, 19, 5, 27, 43, 31, 71, 78, 47, 14, 77, 52, 91, 45, 32, 86, 23, 58, 8, 49, 39, 13, 81, 57, 41, 79, 92, 42, 10, 29, 20, 76, 82, 40, 26, 67, 30, 73, 89, 88, 24, 51, 33, 90, 61, 80, 16, 6};
//        int[] tmSolutionLine6  = new int[]{0, 65, 11, 91, 16, 28, 87, 35, 39, 21, 3, 25, 2, 78, 1, 15, 62, 64, 48, 69, 24, 18, 4, 75, 54, 83, 84, 88, 67, 47, 26, 36, 73, 63, 71, 8, 22, 33, 52, 7, 58, 44, 50, 86, 40, 38, 17, 19, 42, 60, 55, 13, 89, 6, 68, 9, 41, 70, 20, 49, 56, 51, 61, 66, 82, 76, 5, 80, 46, 92, 45, 23, 10, 77, 53, 31, 30, 81, 74, 59, 85, 90, 43, 29, 34, 57, 32, 14, 27, 79, 12, 72, 37, 9};
//        int[] tmSolutionLine7  = new int[]{52, 80, 42, 54, 0, 29, 85, 26, 36, 3, 12, 9, 2, 41, 56, 74, 89, 84, 13, 47, 44, 73, 20, 61, 59, 70, 62, 38, 31, 67, 27, 51, 22, 87, 57, 11, 19, 35, 15, 63, 17, 55, 46, 86, 40, 18, 32, 92, 25, 45, 58, 60, 79, 10, 77, 88, 16, 71, 50, 30, 34, 6, 4, 81, 53, 78, 23, 75, 7, 48, 90, 76, 24, 82, 68, 37, 5, 33, 83, 91, 6, 8, 72, 43, 39, 1, 14, 66, 69, 21, 49, 65, 28, 10};
//        int[] tmSolutionLine8  = new int[]{10, 66, 76, 8, 68, 6, 77, 42, 1, 80, 15, 57, 2, 41, 56, 74, 64, 29, 13, 47, 32, 84, 20, 61, 59, 70, 72, 91, 44, 12, 4, 60, 67, 5, 43, 14, 23, 87, 28, 65, 69, 18, 49, 17, 16, 35, 55, 92, 75, 24, 19, 25, 51, 0, 27, 79, 82, 26, 7, 52, 50, 54, 31, 81, 71, 53, 36, 45, 86, 11, 58, 34, 78, 40, 89, 73, 63, 30, 39, 9, 46, 62, 83, 48, 22, 85, 3, 90, 21, 37, 88, 38, 33, 3};
//        int[] tmSolutionLine9  = new int[]{74, 14, 49, 62, 25, 59, 86, 92, 70, 43, 20, 77, 56, 61, 23, 1, 13, 26, 80, 22, 44, 0, 65, 5, 29, 55, 39, 10, 15, 73, 58, 72, 69, 8, 18, 11, 91, 41, 21, 4, 16, 17, 79, 57, 51, 33, 67, 46, 28, 48, 45, 31, 32, 89, 85, 66, 40, 71, 87, 30, 34, 3, 27, 81, 53, 78, 19, 75, 7, 88, 63, 76, 24, 82, 68, 37, 50, 90, 83, 38, 6, 2, 36, 84, 64, 42, 52, 12, 9, 47, 60, 15, 54, 10};
//        int[] tmSolutionLine10  = new int[]{26, 50, 24, 39, 0, 91, 87, 90, 72, 1, 6, 45, 43, 65, 3, 70, 25, 4, 10, 23, 85, 18, 14, 41, 64, 34, 62, 38, 32, 67, 27, 51, 81, 16, 57, 11, 19, 5, 15, 63, 17, 55, 46, 86, 40, 44, 31, 71, 8, 88, 47, 21, 66, 83, 12, 77, 22, 48, 35, 79, 36, 13, 42, 30, 9, 89, 60, 58, 29, 92, 7, 37, 82, 2, 78, 69, 76, 52, 28, 74, 53, 59, 20, 49, 61, 75, 84, 80, 56, 54, 68, 33, 73, 7};
//        int[] tmSolutionLine11  = new int[]{46, 72, 27, 58, 38, 91, 76, 17, 80, 75, 81, 45, 2, 62, 0, 69, 60, 23, 71, 1, 30, 78, 89, 25, 32, 6, 5, 47, 43, 57, 64, 3, 29, 16, 74, 24, 19, 31, 36, 50, 59, 18, 9, 86, 40, 82, 85, 8, 67, 92, 68, 21, 66, 83, 12, 54, 7, 87, 77, 20, 70, 48, 79, 53, 51, 44, 34, 56, 42, 88, 41, 11, 49, 10, 14, 22, 65, 55, 13, 73, 26, 33, 39, 61, 28, 15, 4, 35, 90, 63, 37, 84, 52, 6};
//        int[] tmSolutionLine12  = new int[]{45, 15, 32, 64, 52, 16, 85, 71, 66, 82, 29, 9, 34, 63, 56, 74, 83, 59, 37, 20, 4, 28, 91, 75, 69, 72, 67, 88, 84, 47, 26, 36, 73, 1, 18, 8, 22, 33, 60, 24, 30, 62, 6, 0, 70, 77, 68, 51, 7, 23, 89, 25, 92, 79, 43, 86, 76, 2, 87, 90, 38, 3, 27, 81, 53, 78, 19, 54, 42, 50, 41, 11, 49, 10, 14, 35, 65, 55, 13, 58, 57, 80, 39, 61, 44, 5, 48, 40, 17, 21, 12, 46, 31, 5};
//        int[] tmSolutionLine13  = new int[]{30, 56, 25, 24, 37, 83, 84, 89, 69, 87, 36, 21, 63, 47, 64, 72, 81, 11, 60, 92, 10, 15, 90, 19, 20, 65, 16, 51, 28, 62, 80, 82, 1, 3, 4, 8, 22, 33, 52, 7, 58, 44, 50, 86, 40, 70, 31, 14, 49, 66, 45, 32, 68, 78, 17, 41, 5, 6, 23, 0, 18, 12, 88, 57, 35, 39, 85, 13, 29, 46, 73, 2, 48, 75, 43, 74, 59, 42, 9, 54, 38, 55, 61, 76, 91, 67, 79, 26, 53, 27, 34, 77, 71, 3};
//        int[] tmSolutionLine14  = new int[]{41, 75, 48, 84, 29, 78, 2, 88, 73, 23, 24, 62, 39, 64, 49, 57, 40, 3, 51, 12, 56, 69, 68, 65, 63, 70, 34, 59, 44, 12, 85, 22, 89, 35, 18, 21, 25, 11, 36, 4, 27, 1, 46, 37, 83, 38, 17, 19, 42, 60, 55, 13, 0, 5, 47, 79, 28, 26, 7, 52, 50, 54, 31, 81, 61, 53, 82, 45, 86, 32, 58, 8, 33, 30, 9, 77, 16, 43, 20, 66, 90, 91, 87, 76, 80, 67, 71, 92, 74, 15, 14, 10, 72, 6};
//        int[] tmSolutionLine15  = new int[]{33, 39, 20, 35, 22, 56, 83, 88, 82, 42, 72, 45, 7, 70, 52, 74, 80, 4, 47, 36, 85, 18, 37, 41, 61, 64, 62, 38, 32, 67, 43, 51, 81, 16, 57, 11, 19, 5, 14, 63, 17, 55, 46, 86, 44, 90, 31, 3, 78, 59, 10, 87, 66, 2, 8, 73, 54, 89, 26, 58, 13, 28, 50, 91, 65, 6, 24, 40, 75, 21, 92, 15, 23, 9, 48, 1, 77, 60, 69, 79, 0, 76, 34, 84, 29, 27, 68, 12, 53, 25, 49, 30, 71, 5};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//        tmSolution.add(tmSolutionLine15);
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);


        //13号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{63, 21, 61, 27, 49, 69, 62, 37, 85, 57, 53, 83, 71, 14, 73, 52, 3, 6, 31, 38, 42, 39, 18, 41, 59, 72, 84, 19, 75, 65, 80, 25, 24, 67, 44, 13, 81, 88, 86, 50, 51, 32, 91, 35, 5, 92, 11, 47, 34, 15, 30, 78, 29, 2, 87, 90, 20, 43, 55, 10, 22, 0, 28, 12, 36, 9, 79, 89, 8, 26, 23, 70, 77, 66, 16, 17, 7, 58, 1, 45, 82, 68, 40, 64, 33, 76, 60, 46, 56, 54, 4, 74, 48, 8};
//        int[] tmSolutionLine2  = new int[]{38, 56, 74, 90, 76, 75, 63, 4, 9, 81, 27, 20, 42, 14, 73, 52, 3, 64, 31, 54, 60, 39, 18, 68, 59, 72, 37, 19, 53, 65, 80, 86, 24, 2, 78, 67, 21, 88, 92, 50, 51, 57, 69, 35, 5, 25, 11, 47, 41, 15, 30, 55, 34, 46, 87, 84, 44, 83, 91, 62, 22, 0, 28, 12, 36, 70, 79, 89, 8, 26, 23, 85, 77, 66, 16, 45, 7, 58, 1, 10, 82, 40, 13, 33, 32, 48, 49, 17, 61, 71, 6, 29, 43, 3};
//        int[] tmSolutionLine3  = new int[]{82, 16, 33, 26, 77, 85, 63, 61, 74, 3, 29, 58, 30, 10, 59, 48, 4, 49, 39, 79, 75, 31, 6, 73, 18, 20, 56, 19, 57, 65, 89, 68, 69, 92, 38, 67, 41, 14, 52, 60, 15, 1, 22, 71, 81, 46, 12, 45, 76, 47, 36, 42, 34, 28, 90, 2, 27, 62, 35, 8, 64, 0, 80, 53, 12, 9, 87, 43, 25, 50, 55, 83, 21, 54, 72, 32, 23, 70, 40, 37, 17, 5, 24, 86, 66, 11, 91, 51, 84, 7, 44, 88, 78, 6};
//        int[] tmSolutionLine4  = new int[]{1, 15, 44, 41, 92, 19, 33, 71, 49, 37, 20, 0, 30, 32, 22, 48, 80, 75, 6, 28, 27, 58, 67, 69, 31, 86, 90, 34, 64, 5, 81, 84, 61, 14, 10, 51, 66, 40, 24, 16, 18, 23, 21, 2, 35, 12, 50, 3, 45, 60, 36, 29, 54, 91, 59, 17, 68, 8, 55, 13, 4, 56, 43, 53, 72, 39, 65, 89, 62, 52, 9, 42, 78, 46, 25, 38, 79, 70, 11, 82, 85, 74, 83, 87, 47, 57, 26, 76, 7, 63, 73, 77, 88, 16};
//        int[] tmSolutionLine5  = new int[]{88, 59, 87, 35, 83, 19, 84, 85, 82, 90, 72, 2, 30, 7, 86, 9, 77, 81, 31, 28, 5, 57, 56, 91, 24, 16, 53, 34, 73, 49, 80, 92, 79, 67, 55, 78, 65, 48, 20, 41, 18, 43, 74, 71, 66, 58, 0, 15, 33, 47, 69, 45, 44, 9, 25, 61, 62, 54, 70, 36, 39, 64, 11, 29, 4, 8, 21, 75, 46, 76, 6, 12, 42, 63, 27, 10, 22, 32, 14, 17, 3, 68, 89, 1, 52, 37, 50, 40, 38, 51, 13, 23, 60, 11};
//        int[] tmSolutionLine6  = new int[]{13, 91, 14, 32, 74, 37, 21, 81, 19, 3, 86, 92, 90, 63, 69, 67, 6, 30, 68, 31, 2, 82, 1, 59, 75, 10, 49, 88, 15, 42, 46, 61, 85, 56, 28, 17, 52, 48, 20, 54, 18, 43, 51, 77, 16, 57, 41, 45, 76, 47, 36, 33, 83, 12, 24, 71, 66, 72, 55, 8, 60, 0, 40, 53, 5, 39, 87, 89, 62, 58, 9, 73, 78, 80, 25, 22, 38, 50, 7, 70, 4, 34, 26, 84, 64, 79, 27, 29, 35, 65, 23, 44, 11, 5};
//        int[] tmSolutionLine7  = new int[]{69, 23, 24, 78, 30, 21, 66, 0, 29, 41, 27, 1, 65, 22, 92, 15, 57, 52, 49, 62, 47, 2, 79, 90, 31, 89, 63, 17, 73, 82, 56, 86, 67, 16, 38, 58, 68, 91, 81, 45, 61, 25, 80, 75, 83, 87, 33, 59, 5, 72, 19, 43, 77, 39, 13, 40, 18, 53, 48, 64, 85, 20, 74, 32, 60, 36, 3, 28, 50, 76, 55, 9, 71, 54, 84, 10, 12, 70, 46, 37, 8, 34, 11, 4, 42, 26, 35, 51, 7, 14, 44, 6, 88, 9};
//        int[] tmSolutionLine8  = new int[]{20, 74, 19, 90, 75, 48, 85, 22, 82, 76, 60, 41, 9, 58, 63, 77, 89, 61, 71, 6, 2, 57, 56, 34, 49, 16, 53, 69, 73, 31, 80, 92, 79, 67, 8, 13, 81, 65, 46, 12, 18, 38, 45, 68, 0, 33, 4, 64, 10, 11, 47, 15, 27, 14, 25, 21, 1, 30, 42, 35, 59, 29, 36, 3, 28, 32, 87, 43, 72, 50, 55, 83, 86, 54, 62, 39, 23, 70, 40, 37, 17, 5, 24, 78, 66, 11, 91, 51, 84, 7, 44, 52, 88, 9};
//        int[] tmSolutionLine9  = new int[]{63, 13, 33, 74, 68, 19, 81, 80, 79, 92, 23, 9, 73, 67, 37, 59, 30, 82, 21, 49, 6, 31, 65, 29, 14, 41, 75, 58, 3, 60, 46, 61, 85, 56, 77, 17, 52, 48, 20, 54, 18, 43, 51, 71, 16, 57, 84, 45, 76, 47, 36, 42, 83, 91, 24, 2, 66, 72, 55, 8, 64, 0, 40, 53, 5, 39, 87, 89, 62, 69, 22, 26, 78, 1, 25, 28, 38, 50, 7, 10, 4, 34, 11, 15, 27, 90, 32, 35, 12, 86, 44, 88, 70, 9};
//        int[] tmSolutionLine10  = new int[]{17, 11, 35, 39, 49, 16, 89, 61, 7, 48, 77, 83, 60, 23, 33, 52, 3, 22, 82, 92, 66, 90, 18, 78, 29, 58, 68, 19, 85, 65, 80, 44, 8, 28, 41, 67, 15, 25, 14, 50, 9, 43, 46, 63, 54, 30, 56, 47, 32, 73, 75, 1, 34, 31, 79, 40, 88, 20, 91, 74, 26, 10, 70, 71, 81, 45, 24, 62, 12, 36, 37, 53, 27, 87, 57, 84, 21, 42, 55, 0, 51, 2, 13, 59, 76, 5, 86, 38, 4, 69, 72, 6, 64, 12};
//        int[] tmSolutionLine11  = new int[]{43, 4, 10, 41, 90, 28, 63, 35, 56, 65, 61, 91, 15, 75, 53, 70, 54, 84, 81, 52, 22, 30, 29, 8, 66, 44, 2, 13, 64, 5, 72, 55, 23, 21, 60, 86, 24, 20, 59, 88, 17, 68, 40, 27, 9, 46, 12, 45, 76, 47, 36, 42, 34, 7, 77, 74, 48, 32, 0, 39, 11, 62, 26, 83, 33, 51, 71, 25, 18, 69, 80, 79, 38, 82, 50, 14, 85, 6, 57, 16, 19, 58, 73, 78, 3, 89, 67, 49, 1, 92, 37, 31, 87, 8};
//        int[] tmSolutionLine12  = new int[]{82, 83, 47, 74, 91, 57, 73, 58, 89, 15, 13, 52, 21, 32, 69, 11, 28, 23, 80, 20, 37, 11, 65, 70, 25, 19, 75, 62, 72, 76, 43, 68, 50, 17, 8, 67, 41, 1, 31, 60, 63, 54, 18, 71, 81, 39, 87, 45, 61, 46, 85, 16, 56, 59, 64, 84, 44, 22, 92, 24, 33, 34, 27, 40, 78, 86, 90, 79, 12, 36, 3, 5, 2, 30, 38, 55, 53, 0, 49, 10, 48, 66, 77, 29, 42, 26, 35, 51, 7, 14, 4, 6, 88, 9};
//        int[] tmSolutionLine13  = new int[]{79, 34, 63, 41, 30, 53, 4, 77, 40, 72, 22, 90, 21, 51, 6, 9, 28, 56, 7, 19, 37, 11, 65, 15, 25, 80, 62, 1, 85, 24, 92, 33, 81, 2, 67, 5, 87, 74, 20, 16, 18, 43, 10, 0, 50, 12, 84, 86, 69, 71, 36, 42, 83, 91, 59, 27, 68, 8, 55, 13, 26, 39, 70, 31, 3, 23, 49, 52, 60, 89, 48, 88, 66, 82, 57, 35, 61, 17, 46, 58, 78, 76, 75, 73, 14, 32, 47, 64, 29, 45, 38, 44, 54, 8};
//        int[] tmSolutionLine14  = new int[]{34, 32, 82, 68, 30, 54, 67, 50, 59, 41, 13, 52, 21, 51, 6, 9, 28, 23, 7, 19, 37, 11, 65, 88, 14, 79, 75, 62, 3, 73, 80, 45, 81, 56, 77, 17, 58, 33, 20, 29, 18, 43, 46, 63, 16, 90, 84, 61, 76, 47, 36, 42, 83, 91, 24, 2, 66, 49, 55, 8, 64, 0, 40, 53, 5, 39, 87, 89, 60, 69, 22, 26, 78, 1, 92, 57, 38, 25, 48, 10, 4, 71, 85, 15, 74, 72, 27, 35, 12, 86, 44, 31, 70, 11};
//        int[] tmSolutionLine15  = new int[]{75, 81, 91, 90, 84, 69, 73, 89, 53, 41, 68, 36, 21, 87, 51, 32, 8, 72, 29, 13, 63, 61, 56, 10, 35, 38, 47, 3, 42, 26, 40, 80, 22, 16, 65, 25, 58, 33, 44, 49, 18, 57, 64, 60, 54, 67, 19, 6, 62, 14, 52, 9, 39, 83, 1, 28, 66, 70, 86, 48, 46, 0, 45, 79, 77, 43, 92, 50, 24, 82, 88, 7, 78, 5, 30, 23, 59, 34, 15, 4, 85, 31, 37, 55, 71, 2, 27, 17, 12, 11, 20, 76, 74, 3};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//        tmSolution.add(tmSolutionLine15);
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //14号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{51, 68, 6, 13, 48, 20, 3, 28, 8, 11, 43, 63, 70, 77, 35, 15, 24, 45, 88, 78, 87, 46, 72, 14, 19, 9, 44, 54, 83, 0, 59, 82, 69, 17, 2, 42, 32, 81, 5, 91, 41, 22, 12, 61, 26, 85, 92, 34, 57, 65, 79, 67, 4, 71, 86, 36, 10, 52, 37, 30, 53, 21, 90, 31, 1, 38, 80, 62, 60, 25, 58, 73, 23, 75, 40, 76, 47, 27, 55, 64, 33, 39, 49, 50, 89, 84, 56, 18, 74, 7, 16, 66, 29, 9};
//        int[] tmSolutionLine2  = new int[]{66, 58, 12, 68, 15, 83, 71, 8, 85, 36, 64, 21, 45, 18, 89, 22, 23, 44, 26, 14, 13, 52, 56, 10, 19, 53, 90, 62, 39, 31, 33, 88, 65, 43, 37, 24, 81, 79, 57, 49, 77, 73, 87, 82, 74, 67, 11, 61, 38, 72, 70, 30, 34, 80, 63, 16, 1, 25, 2, 76, 69, 48, 60, 9, 75, 47, 54, 50, 86, 91, 4, 32, 35, 28, 78, 5, 84, 7, 3, 17, 0, 29, 6, 92, 20, 46, 27, 42, 59, 41, 51, 40, 55, 7};
//        int[] tmSolutionLine3  = new int[]{22, 66, 41, 21, 18, 35, 89, 73, 82, 77, 58, 9, 80, 44, 45, 54, 7, 16, 87, 69, 2, 38, 39, 85, 90, 55, 62, 53, 20, 42, 29, 19, 57, 51, 27, 3, 46, 8, 49, 15, 36, 88, 12, 79, 26, 11, 92, 4, 24, 34, 70, 30, 28, 71, 86, 67, 1, 43, 23, 32, 72, 6, 47, 17, 61, 33, 31, 25, 83, 14, 37, 0, 10, 64, 63, 60, 84, 65, 52, 48, 75, 40, 68, 78, 56, 81, 59, 5, 76, 91, 74, 50, 13, 8};
//        int[] tmSolutionLine4  = new int[]{13, 5, 56, 74, 64, 21, 47, 41, 3, 20, 10, 81, 57, 0, 52, 45, 29, 49, 87, 7, 22, 89, 88, 38, 1, 32, 35, 68, 63, 48, 86, 92, 60, 84, 73, 9, 70, 53, 72, 23, 18, 12, 91, 24, 30, 28, 16, 8, 90, 77, 2, 43, 37, 58, 11, 76, 54, 19, 36, 78, 44, 4, 66, 40, 46, 79, 65, 25, 83, 31, 75, 42, 61, 14, 51, 15, 62, 71, 39, 17, 33, 34, 6, 80, 55, 59, 27, 82, 26, 50, 85, 69, 67, 5};
//        int[] tmSolutionLine5  = new int[]{49, 87, 35, 32, 84, 20, 58, 28, 8, 11, 37, 24, 71, 18, 29, 19, 89, 31, 5, 39, 44, 43, 56, 42, 14, 53, 10, 4, 90, 59, 82, 6, 57, 51, 63, 40, 77, 81, 74, 52, 41, 22, 12, 65, 26, 85, 92, 61, 38, 72, 70, 30, 34, 80, 86, 16, 1, 25, 2, 76, 69, 48, 60, 9, 75, 47, 54, 50, 88, 91, 13, 78, 36, 64, 27, 83, 21, 79, 33, 67, 73, 66, 23, 62, 46, 0, 45, 68, 3, 17, 76, 55, 15, 11};
//        int[] tmSolutionLine6  = new int[]{49, 87, 35, 32, 84, 20, 58, 28, 2, 89, 74, 21, 50, 80, 6, 15, 10, 54, 8, 17, 43, 62, 4, 27, 13, 22, 36, 0, 85, 56, 90, 53, 91, 31, 57, 45, 7, 39, 86, 34, 46, 19, 64, 12, 13, 29, 1, 26, 92, 77, 9, 14, 79, 63, 67, 72, 52, 81, 51, 30, 3, 59, 70, 40, 82, 5, 44, 42, 83, 38, 75, 25, 88, 71, 68, 69, 78, 73, 76, 61, 37, 60, 18, 65, 16, 41, 55, 33, 48, 23, 66, 24, 47, 11};
//        int[] tmSolutionLine7  = new int[]{21, 17, 35, 39, 74, 22, 84, 20, 1, 60, 78, 75, 11, 79, 23, 88, 13, 44, 5, 18, 36, 50, 81, 10, 85, 53, 41, 61, 91, 31, 27, 86, 54, 43, 37, 24, 56, 47, 69, 45, 77, 16, 6, 76, 68, 29, 89, 87, 62, 92, 15, 46, 33, 65, 0, 67, 19, 73, 7, 30, 3, 64, 70, 40, 82, 58, 59, 42, 83, 38, 80, 57, 9, 32, 2, 55, 72, 8, 4, 71, 28, 66, 26, 34, 51, 14, 63, 25, 52, 90, 49, 12, 48, 4};
//        int[] tmSolutionLine8  = new int[]{74, 46, 60, 17, 44, 57, 15, 40, 59, 47, 32, 58, 89, 38, 36, 54, 76, 5, 11, 69, 49, 37, 39, 85, 90, 55, 62, 6, 80, 19, 68, 53, 29, 31, 2, 0, 48, 42, 56, 66, 16, 70, 34, 73, 87, 28, 78, 26, 92, 77, 9, 65, 79, 63, 67, 3, 52, 81, 51, 20, 72, 30, 24, 12, 13, 27, 64, 25, 83, 14, 33, 75, 22, 23, 45, 71, 1, 82, 8, 61, 35, 84, 18, 10, 41, 7, 88, 86, 4, 21, 43, 91, 50, 10};
//        int[] tmSolutionLine9  = new int[]{91, 47, 38, 36, 88, 81, 33, 49, 58, 79, 65, 70, 50, 80, 4, 39, 61, 66, 8, 40, 19, 62, 78, 27, 13, 22, 21, 64, 25, 68, 54, 69, 41, 23, 57, 0, 84, 7, 85, 76, 1, 89, 90, 12, 74, 60, 44, 26, 92, 77, 9, 32, 28, 71, 86, 30, 10, 55, 37, 67, 53, 48, 6, 17, 35, 82, 59, 42, 83, 31, 75, 87, 46, 14, 51, 15, 43, 73, 29, 24, 3, 56, 11, 45, 34, 18, 20, 63, 52, 2, 5, 72, 16, 4};
//        int[] tmSolutionLine10  = new int[]{49, 89, 5, 7, 31, 47, 30, 59, 41, 73, 92, 56, 39, 87, 16, 32, 3, 46, 20, 18, 36, 50, 72, 17, 38, 60, 44, 4, 90, 0, 82, 6, 71, 52, 63, 40, 51, 81, 74, 77, 10, 13, 9, 29, 64, 85, 2, 62, 42, 69, 67, 19, 34, 80, 86, 25, 11, 35, 37, 70, 78, 83, 43, 53, 45, 79, 54, 15, 88, 91, 26, 24, 27, 58, 1, 68, 65, 14, 8, 75, 84, 22, 61, 76, 12, 28, 55, 33, 48, 23, 66, 57, 21, 9};
//        int[] tmSolutionLine11  = new int[]{56, 59, 3, 46, 51, 82, 48, 63, 79, 42, 67, 78, 57, 64, 45, 24, 84, 49, 87, 11, 22, 18, 27, 7, 16, 65, 85, 54, 66, 5, 10, 8, 74, 29, 52, 4, 37, 81, 13, 80, 58, 0, 91, 70, 90, 62, 69, 26, 92, 77, 9, 30, 28, 71, 86, 44, 1, 43, 23, 32, 39, 6, 47, 17, 61, 33, 38, 25, 83, 31, 75, 20, 88, 73, 68, 89, 60, 55, 40, 21, 12, 14, 53, 35, 41, 19, 36, 2, 72, 34, 50, 15, 76, 4};
//        int[] tmSolutionLine12  = new int[]{35, 68, 3, 46, 10, 82, 48, 63, 79, 8, 67, 2, 7, 32, 64, 83, 84, 71, 87, 11, 22, 18, 50, 57, 16, 42, 85, 54, 66, 5, 51, 24, 74, 29, 78, 4, 49, 81, 13, 20, 14, 33, 91, 86, 80, 28, 72, 26, 92, 77, 89, 44, 0, 34, 12, 52, 39, 88, 36, 30, 53, 21, 90, 31, 1, 38, 58, 62, 60, 25, 59, 73, 23, 75, 40, 76, 47, 27, 55, 37, 61, 70, 65, 41, 43, 69, 56, 15, 19, 9, 45, 17, 6, 6};
//        int[] tmSolutionLine13  = new int[]{62, 67, 59, 56, 10, 24, 79, 4, 51, 70, 11, 21, 36, 18, 89, 22, 23, 64, 26, 78, 87, 52, 77, 66, 47, 37, 31, 53, 20, 42, 29, 57, 19, 60, 27, 3, 46, 8, 49, 90, 45, 88, 12, 65, 9, 81, 92, 33, 38, 72, 85, 14, 61, 80, 86, 30, 1, 25, 2, 76, 69, 48, 6, 17, 58, 35, 50, 73, 7, 74, 75, 71, 44, 13, 39, 34, 63, 32, 91, 68, 0, 54, 84, 41, 43, 16, 82, 15, 28, 55, 5, 83, 40, 10};
//
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //15号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{81, 64, 63, 50, 34, 8, 82, 41, 80, 0, 30, 88, 92, 4, 32, 22, 37, 24, 74, 79, 67, 27, 16, 31, 58, 46, 7, 69, 83, 18, 55, 35, 78, 21, 11, 14, 10, 91, 47, 75, 5, 57, 73, 1, 9, 52, 28, 71, 72, 64, 20, 61, 23, 40, 54, 3, 85, 62, 44, 43, 66, 42, 70, 68, 65, 6, 17, 2, 76, 84, 15, 19, 38, 87, 45, 53, 90, 33, 36, 49, 86, 89, 60, 48, 26, 51, 56, 13, 29, 12, 77, 59, 39, 4};
//        int[] tmSolutionLine2  = new int[]{10, 15, 14, 33, 17, 68, 37, 65, 80, 0, 76, 50, 19, 58, 82, 27, 64, 38, 26, 84, 18, 4, 40, 41, 13, 66, 44, 69, 48, 74, 78, 49, 90, 21, 47, 6, 73, 36, 72, 55, 45, 16, 62, 9, 79, 71, 88, 61, 28, 83, 57, 43, 81, 23, 77, 12, 24, 70, 3, 54, 59, 39, 42, 1, 5, 8, 53, 34, 30, 51, 75, 91, 60, 86, 7, 20, 87, 67, 56, 92, 46, 29, 32, 31, 89, 52, 2, 25, 35, 63, 11, 22, 85, 11};
//        int[] tmSolutionLine3  = new int[]{50, 64, 23, 66, 0, 6, 86, 49, 17, 20, 9, 69, 48, 63, 18, 67, 53, 16, 45, 84, 21, 15, 14, 92, 60, 75, 55, 26, 78, 88, 39, 61, 47, 54, 29, 76, 1, 10, 40, 90, 91, 32, 56, 11, 65, 42, 35, 33, 68, 52, 5, 71, 70, 22, 2, 3, 85, 62, 38, 37, 24, 72, 34, 31, 7, 87, 25, 82, 43, 28, 77, 83, 80, 79, 73, 8, 41, 46, 19, 81, 13, 74, 57, 44, 12, 30, 27, 36, 4, 58, 89, 59, 51, 6};
//        int[] tmSolutionLine4  = new int[]{23, 87, 72, 50, 83, 73, 8, 85, 63, 77, 29, 21, 79, 17, 59, 30, 64, 38, 55, 84, 18, 4, 9, 74, 43, 62, 33, 69, 68, 91, 78, 49, 90, 11, 47, 92, 53, 36, 5, 6, 45, 16, 24, 66, 41, 71, 60, 61, 14, 19, 57, 28, 81, 88, 26, 12, 48, 70, 37, 52, 0, 56, 58, 34, 35, 67, 75, 2, 10, 20, 46, 42, 86, 27, 76, 13, 22, 44, 1, 65, 32, 31, 3, 89, 39, 51, 7, 54, 25, 80, 40, 82, 15, 9};
//        int[] tmSolutionLine5  = new int[]{53, 21, 20, 66, 92, 0, 63, 4, 81, 68, 24, 46, 65, 17, 41, 33, 62, 90, 13, 12, 64, 38, 31, 87, 55, 59, 72, 56, 28, 50, 84, 82, 61, 54, 67, 45, 40, 85, 39, 44, 76, 1, 8, 11, 49, 73, 35, 34, 83, 27, 5, 71, 29, 6, 89, 36, 75, 60, 70, 3, 16, 10, 74, 22, 78, 43, 25, 19, 7, 91, 42, 2, 86, 52, 32, 69, 15, 77, 57, 47, 23, 26, 80, 18, 79, 88, 30, 51, 37, 14, 58, 9, 48, 7};
//        int[] tmSolutionLine6  = new int[]{8, 18, 27, 68, 51, 67, 80, 41, 30, 31, 81, 26, 91, 63, 0, 66, 53, 87, 70, 34, 79, 74, 85, 4, 43, 62, 44, 13, 64, 1, 23, 92, 84, 9, 71, 56, 50, 46, 15, 19, 24, 57, 73, 89, 42, 7, 82, 61, 72, 22, 20, 33, 28, 35, 54, 77, 60, 65, 47, 39, 45, 52, 76, 32, 90, 6, 11, 2, 48, 59, 37, 17, 78, 14, 49, 69, 40, 88, 21, 25, 16, 29, 75, 58, 36, 12, 86, 55, 38, 10, 18, 83, 3, 13};
//        int[] tmSolutionLine7  = new int[]{50, 65, 56, 21, 33, 3, 78, 72, 5, 48, 51, 42, 47, 26, 89, 14, 37, 8, 88, 82, 24, 74, 11, 4, 67, 84, 58, 49, 6, 76, 23, 38, 87, 7, 75, 10, 1, 32, 20, 80, 61, 18, 13, 69, 79, 73, 25, 36, 60, 27, 85, 71, 43, 2, 59, 0, 40, 62, 44, 31, 12, 15, 28, 46, 55, 83, 35, 57, 45, 22, 54, 63, 81, 16, 9, 64, 34, 17, 19, 52, 30, 90, 91, 29, 39, 41, 66, 68, 92, 70, 53, 77, 86, 9};
//        int[] tmSolutionLine8  = new int[]{3, 59, 24, 4, 18, 30, 60, 8, 26, 22, 51, 74, 13, 46, 41, 33, 62, 90, 36, 12, 64, 2, 15, 57, 17, 9, 79, 61, 28, 44, 53, 86, 78, 21, 67, 23, 71, 14, 68, 39, 76, 83, 73, 52, 80, 43, 35, 31, 40, 81, 37, 72, 20, 45, 66, 84, 89, 42, 38, 70, 16, 7, 54, 55, 69, 87, 25, 85, 5, 27, 34, 92, 82, 29, 50, 56, 11, 58, 88, 65, 49, 91, 32, 47, 75, 48, 6, 63, 0, 10, 19, 1, 77, 9};
//        int[] tmSolutionLine9  = new int[]{87, 49, 72, 50, 83, 73, 8, 85, 63, 77, 67, 57, 61, 21, 31, 47, 10, 92, 37, 38, 2, 79, 18, 14, 28, 4, 48, 29, 40, 22, 84, 58, 65, 74, 41, 51, 81, 35, 44, 1, 30, 43, 36, 59, 88, 46, 54, 80, 11, 82, 45, 71, 0, 6, 89, 13, 75, 62, 70, 3, 16, 27, 56, 20, 64, 5, 25, 19, 7, 91, 42, 17, 34, 52, 53, 69, 15, 12, 86, 33, 23, 26, 60, 55, 24, 68, 90, 39, 9, 66, 76, 32, 78, 9};
//        int[] tmSolutionLine10  = new int[]{6, 48, 54, 86, 51, 83, 59, 43, 30, 87, 69, 26, 91, 63, 0, 66, 53, 12, 70, 34, 79, 74, 85, 4, 41, 62, 44, 13, 64, 1, 23, 92, 84, 9, 90, 50, 20, 47, 60, 68, 19, 2, 10, 36, 81, 58, 17, 67, 7, 21, 28, 37, 57, 49, 40, 46, 65, 14, 88, 29, 73, 61, 39, 82, 8, 35, 52, 27, 33, 45, 32, 55, 24, 16, 18, 38, 76, 75, 42, 56, 22, 77, 71, 15, 31, 3, 89, 11, 80, 78, 5, 72, 25, 4};
//        int[] tmSolutionLine11  = new int[]{70, 77, 78, 74, 5, 12, 25, 51, 63, 2, 7, 10, 9, 57, 6, 30, 64, 38, 24, 91, 18, 1, 45, 59, 50, 55, 17, 82, 48, 47, 69, 79, 90, 21, 11, 23, 41, 14, 68, 39, 88, 83, 73, 26, 71, 43, 35, 31, 40, 81, 37, 72, 20, 76, 52, 84, 89, 42, 28, 27, 0, 56, 58, 34, 60, 67, 75, 66, 44, 13, 53, 16, 61, 80, 22, 87, 29, 3, 86, 65, 49, 62, 32, 15, 92, 4, 85, 19, 8, 54, 33, 46, 36, 8};
//        int[] tmSolutionLine12  = new int[]{69, 45, 85, 5, 71, 83, 59, 10, 63, 39, 31, 43, 79, 17, 6, 30, 64, 38, 55, 91, 18, 29, 40, 41, 74, 24, 22, 23, 68, 84, 78, 9, 90, 11, 47, 21, 73, 36, 72, 8, 66, 26, 13, 2, 77, 16, 60, 61, 70, 19, 57, 67, 7, 50, 81, 46, 44, 53, 89, 52, 0, 56, 58, 34, 35, 15, 75, 28, 3, 87, 14, 42, 62, 27, 76, 20, 88, 49, 1, 65, 32, 86, 48, 37, 92, 51, 80, 54, 4, 12, 33, 82, 25, 5};
//        int[] tmSolutionLine13  = new int[]{16, 13, 24, 30, 18, 76, 26, 87, 38, 60, 91, 31, 35, 58, 54, 48, 88, 50, 23, 62, 59, 85, 15, 57, 17, 9, 79, 41, 20, 28, 0, 66, 45, 61, 56, 3, 2, 82, 36, 7, 33, 78, 74, 5, 70, 32, 11, 27, 6, 81, 46, 80, 25, 10, 65, 72, 83, 63, 89, 12, 21, 75, 51, 77, 90, 43, 71, 49, 42, 68, 55, 84, 40, 44, 14, 4, 86, 52, 53, 37, 69, 8, 47, 29, 1, 64, 39, 34, 92, 19, 73, 22, 67, 7};
//        int[] tmSolutionLine14  = new int[]{60, 51, 16, 68, 84, 85, 40, 19, 17, 27, 90, 41, 44, 4, 30, 25, 61, 38, 36, 1, 88, 0, 33, 31, 79, 92, 58, 34, 8, 18, 55, 54, 78, 21, 71, 24, 66, 91, 47, 75, 35, 83, 73, 52, 9, 50, 28, 11, 72, 64, 87, 2, 26, 22, 49, 70, 53, 3, 67, 77, 57, 45, 80, 74, 65, 42, 82, 46, 48, 56, 10, 7, 15, 6, 81, 69, 20, 32, 23, 86, 14, 63, 43, 89, 62, 5, 76, 13, 29, 12, 37, 59, 39, 6};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);


        //16号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{62, 76, 28, 16, 11, 3, 84, 85, 10, 45, 41, 0, 80, 2, 65, 14, 64, 71, 55, 92, 57, 69, 54, 60, 8, 1, 12, 36, 89, 31, 72, 26, 5, 79, 21, 6, 4, 48, 32, 46, 38, 37, 13, 19, 58, 59, 90, 51, 52, 83, 91, 74, 82, 25, 53, 22, 42, 77, 27, 81, 78, 49, 7, 50, 35, 34, 43, 87, 70, 17, 67, 75, 47, 56, 20, 66, 88, 23, 63, 29, 68, 44, 15, 86, 39, 18, 9, 30, 40, 61, 33, 73, 24, 12};
//        int[] tmSolutionLine2  = new int[]{26, 83, 5, 16, 36, 63, 21, 2, 56, 50, 37, 82, 51, 9, 44, 59, 66, 31, 25, 58, 89, 43, 41, 90, 76, 57, 77, 85, 72, 8, 48, 60, 12, 45, 79, 92, 0, 30, 70, 39, 64, 20, 65, 61, 71, 78, 7, 3, 42, 38, 14, 86, 13, 80, 81, 75, 73, 28, 46, 32, 6, 22, 87, 54, 11, 84, 1, 91, 15, 62, 23, 24, 68, 67, 29, 53, 40, 52, 19, 76, 18, 17, 4, 47, 88, 33, 35, 55, 34, 10, 74, 69, 27, 4};
//        int[] tmSolutionLine3  = new int[]{80, 18, 23, 38, 24, 44, 19, 43, 33, 76, 88, 50, 41, 78, 91, 40, 31, 4, 58, 32, 63, 86, 82, 68, 54, 57, 52, 75, 28, 53, 42, 7, 25, 87, 16, 39, 62, 61, 26, 2, 0, 67, 13, 20, 14, 30, 3, 71, 37, 35, 15, 85, 51, 69, 60, 5, 55, 77, 21, 12, 45, 72, 56, 70, 49, 36, 89, 73, 17, 65, 34, 64, 48, 66, 79, 22, 47, 11, 84, 29, 92, 90, 74, 10, 81, 6, 9, 8, 27, 1, 59, 83, 46, 5};
//        int[] tmSolutionLine4  = new int[]{13, 23, 39, 59, 74, 56, 82, 52, 89, 49, 70, 50, 41, 78, 91, 40, 55, 62, 7, 9, 35, 85, 54, 60, 58, 10, 31, 20, 17, 66, 25, 37, 11, 87, 92, 45, 22, 86, 44, 16, 36, 68, 48, 42, 30, 90, 32, 26, 64, 83, 46, 76, 73, 71, 80, 53, 24, 77, 47, 0, 72, 19, 65, 75, 69, 61, 27, 57, 43, 18, 33, 21, 63, 15, 79, 12, 14, 28, 8, 84, 6, 2, 81, 88, 29, 4, 38, 51, 3, 34, 1, 67, 5, 4};
//        int[] tmSolutionLine5  = new int[]{4, 89, 66, 65, 57, 3, 40, 13, 38, 73, 15, 32, 63, 36, 70, 84, 69, 54, 9, 82, 41, 18, 7, 78, 56, 74, 31, 91, 39, 51, 25, 37, 11, 2, 16, 45, 22, 50, 44, 92, 52, 58, 20, 4, 14, 90, 68, 26, 64, 75, 6, 76, 49, 33, 80, 53, 24, 77, 47, 0, 72, 19, 48, 60, 55, 81, 42, 10, 67, 86, 28, 21, 35, 61, 79, 12, 30, 17, 23, 34, 43, 87, 71, 88, 85, 29, 62, 8, 27, 1, 59, 83, 46, 5};
//        int[] tmSolutionLine6  = new int[]{86, 59, 25, 43, 56, 7, 30, 22, 80, 73, 61, 15, 65, 2, 41, 14, 64, 79, 51, 92, 57, 69, 87, 60, 8, 70, 31, 36, 89, 45, 72, 26, 66, 81, 21, 6, 4, 48, 39, 52, 38, 62, 49, 76, 35, 78, 16, 90, 58, 83, 54, 37, 63, 71, 18, 53, 46, 67, 85, 17, 0, 23, 29, 11, 12, 9, 27, 68, 55, 44, 34, 75, 82, 19, 84, 91, 32, 47, 88, 42, 77, 28, 13, 50, 10, 24, 33, 5, 20, 40, 1, 3, 74, 8};
//        int[] tmSolutionLine7  = new int[]{1, 59, 62, 43, 56, 7, 30, 49, 64, 73, 2, 15, 65, 28, 41, 37, 17, 38, 76, 58, 16, 69, 87, 60, 8, 70, 31, 36, 89, 45, 78, 26, 63, 81, 54, 6, 50, 48, 39, 51, 33, 25, 86, 29, 4, 11, 34, 90, 88, 83, 46, 14, 57, 71, 80, 53, 24, 32, 68, 20, 91, 0, 44, 79, 22, 5, 27, 9, 55, 67, 10, 75, 19, 66, 77, 18, 52, 92, 61, 35, 21, 12, 13, 74, 82, 40, 47, 23, 72, 85, 42, 3, 84, 9};
//        int[] tmSolutionLine8  = new int[]{9, 27, 62, 43, 56, 82, 30, 32, 59, 7, 2, 15, 65, 28, 41, 1, 17, 72, 5, 38, 86, 52, 51, 29, 88, 40, 31, 42, 89, 12, 11, 18, 48, 77, 25, 37, 92, 68, 23, 10, 84, 85, 71, 61, 90, 60, 55, 26, 64, 75, 6, 76, 49, 33, 80, 57, 16, 81, 35, 91, 69, 4, 22, 36, 34, 63, 45, 87, 78, 79, 8, 47, 50, 14, 46, 73, 21, 39, 3, 67, 0, 24, 74, 19, 58, 44, 66, 53, 20, 83, 54, 13, 70, 9};
//        int[] tmSolutionLine9  = new int[]{33, 86, 48, 27, 26, 90, 74, 11, 77, 56, 37, 82, 36, 63, 83, 16, 44, 47, 62, 50, 8, 15, 41, 52, 24, 6, 55, 64, 28, 43, 12, 60, 57, 45, 78, 92, 0, 30, 70, 39, 66, 67, 65, 61, 71, 25, 7, 3, 42, 38, 19, 81, 31, 80, 88, 75, 68, 85, 46, 32, 72, 18, 20, 53, 84, 34, 1, 91, 2, 40, 23, 76, 35, 13, 29, 59, 14, 52, 21, 58, 22, 17, 4, 5, 79, 73, 87, 51, 10, 49, 54, 89, 86, 6};
//        int[] tmSolutionLine10  = new int[]{85, 12, 46, 81, 23, 74, 86, 51, 37, 28, 73, 92, 31, 42, 33, 75, 83, 20, 16, 91, 34, 17, 24, 10, 59, 80, 71, 19, 45, 65, 61, 7, 49, 32, 0, 14, 41, 87, 57, 2, 56, 67, 13, 4, 58, 30, 43, 78, 52, 27, 21, 35, 82, 70, 18, 22, 1, 36, 90, 79, 48, 26, 72, 50, 77, 40, 55, 54, 76, 39, 60, 44, 47, 84, 15, 5, 88, 68, 63, 29, 11, 69, 3, 53, 8, 6, 9, 25, 66, 38, 64, 62, 89, 4};
//        int[] tmSolutionLine11  = new int[]{72, 36, 10, 37, 82, 85, 25, 49, 79, 22, 46, 34, 24, 7, 87, 67, 90, 76, 56, 89, 45, 31, 74, 91, 86, 9, 69, 64, 26, 35, 58, 17, 52, 78, 77, 92, 71, 30, 73, 39, 66, 13, 65, 18, 70, 20, 75, 62, 42, 47, 83, 54, 32, 5, 44, 63, 16, 23, 0, 27, 29, 19, 11, 55, 48, 51, 60, 59, 43, 68, 33, 21, 41, 15, 53, 12, 14, 13, 8, 84, 6, 2, 81, 88, 50, 4, 38, 61, 3, 40, 1, 57, 28, 8};
//        int[] tmSolutionLine12  = new int[]{62, 2, 36, 51, 30, 69, 65, 56, 71, 38, 4, 90, 1, 83, 29, 52, 19, 64, 54, 91, 68, 7, 5, 57, 55, 72, 35, 66, 20, 89, 80, 60, 48, 12, 25, 37, 92, 31, 13, 44, 28, 88, 11, 16, 73, 79, 58, 59, 49, 84, 15, 63, 14, 81, 86, 6, 3, 75, 0, 27, 77, 10, 8, 78, 34, 33, 45, 87, 70, 17, 67, 47, 50, 85, 61, 23, 41, 22, 53, 9, 39, 43, 32, 74, 42, 46, 24, 82, 76, 26, 21, 40, 18, 3};
//        int[] tmSolutionLine13  = new int[]{76, 39, 11, 58, 9, 37, 59, 92, 16, 33, 24, 12, 88, 52, 49, 15, 45, 29, 17, 30, 8, 65, 38, 41, 57, 4, 55, 56, 61, 0, 54, 44, 78, 87, 36, 5, 89, 19, 21, 26, 43, 28, 62, 31, 35, 73, 42, 40, 83, 67, 77, 80, 6, 90, 50, 51, 68, 85, 81, 32, 72, 18, 46, 53, 84, 34, 1, 91, 2, 75, 23, 64, 48, 66, 79, 22, 82, 86, 63, 71, 3, 13, 70, 60, 69, 25, 27, 7, 10, 47, 20, 74, 14, 8};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);


        //17号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{90, 81, 60, 56, 31, 2, 63, 37, 18, 10, 14, 40, 35, 38, 75, 24, 12, 89, 32, 66, 73, 23, 19, 16, 53, 28, 80, 67, 20, 39, 30, 59, 58, 48, 85, 0, 1, 47, 17, 7, 76, 36, 64, 50, 45, 62, 22, 65, 51, 57, 49, 70, 55, 34, 46, 4, 26, 87, 79, 5, 86, 33, 29, 43, 13, 83, 8, 82, 74, 78, 91, 11, 92, 21, 84, 54, 6, 41, 9, 88, 71, 72, 15, 27, 42, 25, 69, 68, 52, 3, 44, 61, 77, 10};
//        int[] tmSolutionLine2  = new int[]{61, 28, 21, 84, 80, 53, 20, 50, 0, 19, 60, 35, 6, 68, 25, 5, 30, 27, 39, 75, 45, 17, 15, 89, 64, 62, 42, 23, 48, 92, 10, 49, 65, 37, 3, 51, 87, 88, 56, 71, 74, 82, 14, 36, 43, 66, 11, 32, 18, 76, 83, 70, 55, 34, 46, 90, 38, 57, 79, 77, 86, 33, 29, 26, 13, 22, 8, 85, 1, 24, 78, 9, 7, 81, 2, 59, 12, 63, 31, 4, 72, 40, 54, 47, 67, 69, 91, 41, 73, 52, 58, 16, 44, 8};
//        int[] tmSolutionLine3  = new int[]{72, 64, 63, 20, 0, 89, 40, 87, 73, 71, 67, 22, 13, 74, 41, 80, 76, 27, 88, 92, 45, 26, 15, 3, 77, 48, 90, 70, 25, 17, 36, 42, 46, 54, 59, 10, 57, 39, 56, 51, 7, 82, 16, 18, 29, 53, 62, 66, 14, 50, 75, 6, 23, 83, 60, 38, 49, 19, 44, 8, 12, 65, 86, 2, 61, 31, 81, 47, 85, 37, 21, 32, 84, 91, 78, 1, 24, 9, 43, 52, 79, 34, 4, 33, 30, 11, 35, 69, 5, 55, 68, 58, 28, 4};
//        int[] tmSolutionLine4  = new int[]{22, 15, 52, 84, 80, 53, 20, 50, 65, 0, 56, 92, 19, 63, 27, 89, 47, 8, 28, 58, 90, 42, 32, 13, 37, 79, 72, 34, 16, 51, 24, 21, 12, 44, 83, 10, 7, 39, 26, 5, 4, 14, 69, 88, 78, 66, 75, 74, 2, 85, 17, 6, 23, 54, 18, 57, 49, 41, 77, 91, 59, 30, 46, 87, 86, 48, 68, 82, 11, 60, 9, 40, 64, 25, 33, 61, 55, 81, 76, 29, 71, 45, 3, 73, 67, 70, 38, 43, 36, 1, 40, 31, 35, 9};
//        int[] tmSolutionLine5  = new int[]{56, 24, 70, 12, 71, 86, 20, 11, 0, 13, 5, 60, 21, 89, 30, 41, 6, 77, 15, 74, 80, 17, 73, 82, 55, 40, 22, 28, 3, 48, 18, 83, 64, 45, 39, 86, 78, 59, 33, 66, 10, 24, 49, 31, 72, 32, 67, 46, 14, 50, 75, 91, 23, 51, 37, 19, 54, 2, 9, 8, 25, 7, 69, 76, 62, 52, 43, 34, 38, 44, 27, 88, 58, 42, 79, 90, 63, 1, 16, 65, 53, 61, 85, 84, 92, 36, 35, 87, 68, 81, 47, 26, 29, 6};
//        int[] tmSolutionLine6  = new int[]{45, 20, 80, 57, 46, 81, 31, 19, 55, 13, 27, 36, 25, 5, 35, 41, 22, 77, 2, 3, 32, 26, 53, 30, 37, 21, 70, 47, 49, 63, 65, 74, 12, 48, 85, 44, 84, 56, 17, 7, 10, 0, 54, 91, 69, 83, 62, 66, 14, 50, 75, 6, 23, 4, 60, 71, 15, 87, 9, 8, 61, 76, 28, 51, 33, 52, 24, 34, 38, 86, 89, 88, 64, 42, 79, 16, 39, 1, 40, 82, 73, 59, 11, 78, 92, 90, 72, 67, 58, 68, 43, 29, 18, 9};
//        int[] tmSolutionLine7  = new int[]{84, 62, 17, 72, 48, 35, 38, 42, 81, 39, 90, 32, 10, 61, 50, 45, 52, 68, 3, 47, 73, 23, 4, 89, 79, 63, 80, 40, 30, 18, 36, 59, 58, 12, 85, 0, 25, 66, 28, 7, 76, 20, 64, 67, 24, 1, 22, 14, 51, 65, 49, 70, 46, 34, 37, 19, 54, 2, 9, 8, 86, 33, 29, 87, 13, 83, 5, 82, 74, 78, 91, 11, 92, 21, 41, 60, 69, 16, 88, 57, 55, 56, 71, 75, 44, 77, 53, 26, 31, 6, 27, 15, 43, 5};
//        int[] tmSolutionLine8  = new int[]{10, 9, 2, 85, 23, 74, 73, 17, 48, 61, 47, 89, 65, 80, 34, 40, 27, 7, 36, 64, 72, 67, 16, 92, 90, 39, 22, 53, 12, 21, 29, 49, 77, 37, 3, 51, 13, 18, 26, 5, 4, 14, 69, 88, 78, 66, 75, 50, 30, 87, 86, 33, 57, 91, 28, 63, 25, 60, 32, 56, 31, 19, 11, 20, 45, 24, 6, 38, 70, 71, 0, 44, 84, 79, 8, 41, 58, 54, 68, 42, 55, 83, 46, 43, 82, 35, 59, 76, 52, 1, 62, 81, 15, 9};
//        int[] tmSolutionLine9  = new int[]{67, 34, 5, 23, 72, 16, 77, 45, 71, 81, 19, 20, 6, 80, 87, 29, 26, 18, 92, 75, 78, 13, 37, 30, 90, 21, 70, 47, 49, 63, 65, 74, 12, 48, 85, 44, 84, 56, 17, 7, 10, 0, 54, 91, 69, 83, 62, 9, 14, 22, 32, 24, 52, 53, 60, 64, 57, 36, 2, 8, 61, 76, 15, 51, 33, 31, 50, 25, 38, 86, 89, 88, 1, 4, 28, 41, 58, 39, 68, 42, 55, 73, 46, 43, 82, 35, 3, 40, 11, 66, 79, 59, 27, 5};
//        int[] tmSolutionLine10  = new int[]{35, 19, 6, 57, 79, 61, 31, 46, 80, 13, 27, 36, 25, 5, 51, 41, 22, 77, 81, 3, 91, 17, 85, 82, 89, 40, 70, 28, 75, 34, 18, 44, 68, 83, 73, 42, 15, 59, 26, 87, 63, 65, 47, 64, 92, 4, 21, 16, 14, 50, 30, 0, 23, 49, 72, 71, 54, 66, 9, 8, 24, 7, 69, 10, 32, 60, 78, 33, 11, 48, 52, 88, 58, 86, 2, 37, 84, 90, 1, 39, 29, 38, 67, 74, 12, 56, 45, 76, 20, 62, 43, 53, 55, 7};
//        int[] tmSolutionLine11  = new int[]{35, 80, 54, 71, 27, 72, 70, 84, 86, 21, 90, 77, 10, 83, 50, 32, 78, 68, 88, 56, 52, 23, 19, 16, 53, 51, 66, 24, 20, 42, 73, 59, 31, 48, 85, 0, 43, 26, 87, 2, 76, 25, 74, 15, 37, 62, 22, 65, 28, 3, 49, 67, 55, 45, 46, 36, 47, 34, 79, 5, 58, 39, 29, 1, 13, 9, 8, 82, 64, 12, 91, 11, 17, 7, 75, 89, 44, 69, 33, 41, 14, 30, 92, 18, 40, 81, 63, 11, 38, 57, 60, 4, 61, 6};
//        int[] tmSolutionLine12  = new int[]{59, 17, 27, 0, 6, 4, 55, 22, 30, 49, 33, 86, 62, 36, 50, 84, 74, 9, 32, 76, 90, 24, 60, 44, 42, 13, 37, 21, 75, 81, 56, 5, 89, 41, 28, 15, 10, 53, 73, 47, 64, 43, 1, 69, 77, 57, 71, 19, 72, 2, 8, 35, 38, 23, 52, 68, 12, 3, 40, 88, 65, 91, 63, 61, 83, 16, 54, 58, 46, 48, 78, 29, 11, 70, 87, 14, 7, 25, 45, 79, 85, 39, 51, 34, 80, 31, 82, 26, 20, 67, 92, 18, 66, 8};
//        int[] tmSolutionLine13  = new int[]{64, 19, 80, 38, 74, 84, 9, 22, 35, 60, 57, 56, 54, 33, 85, 11, 87, 79, 30, 70, 7, 58, 76, 67, 65, 62, 42, 23, 48, 92, 10, 49, 77, 37, 3, 51, 13, 18, 26, 5, 4, 14, 69, 88, 78, 66, 21, 29, 68, 40, 6, 32, 55, 34, 46, 2, 28, 63, 25, 27, 86, 31, 44, 43, 61, 83, 1, 82, 75, 12, 52, 36, 89, 0, 47, 90, 91, 15, 39, 24, 8, 73, 71, 81, 50, 17, 59, 16, 20, 45, 72, 41, 53, 8};
//        int[] tmSolutionLine14  = new int[]{55, 71, 37, 27, 64, 52, 31, 81, 6, 89, 92, 30, 72, 76, 79, 47, 73, 17, 74, 34, 26, 1, 75, 42, 77, 57, 35, 28, 60, 43, 36, 67, 44, 21, 39, 38, 19, 61, 10, 62, 69, 8, 22, 11, 85, 32, 54, 15, 9, 4, 41, 33, 0, 2, 20, 68, 12, 3, 40, 86, 51, 91, 63, 65, 83, 16, 13, 58, 46, 48, 78, 29, 88, 70, 53, 14, 7, 25, 87, 66, 80, 50, 18, 56, 5, 23, 59, 49, 24, 84, 82, 45, 90, 4};
//        int[] tmSolutionLine15  = new int[]{91, 64, 18, 40, 79, 61, 31, 46, 80, 13, 27, 36, 25, 5, 51, 41, 22, 77, 81, 3, 32, 35, 53, 30, 57, 21, 70, 47, 49, 63, 65, 74, 12, 55, 85, 44, 84, 56, 88, 71, 83, 82, 14, 87, 92, 62, 11, 72, 10, 66, 60, 4, 48, 34, 19, 23, 42, 15, 9, 50, 54, 29, 78, 38, 2, 76, 0, 73, 45, 7, 52, 8, 28, 16, 43, 90, 37, 59, 68, 1, 6, 86, 58, 33, 20, 67, 26, 24, 17, 89, 75, 69, 39, 5};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//        tmSolution.add(tmSolutionLine15);
//
//
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //18号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{56, 60, 12, 13, 19, 47, 84, 36, 70, 6, 75, 61, 59, 66, 7, 1, 55, 77, 73, 24, 44, 39, 31, 89, 37, 30, 33, 79, 22, 16, 2, 49, 17, 27, 64, 0, 54, 8, 20, 40, 26, 3, 90, 72, 35, 25, 9, 41, 74, 58, 46, 87, 43, 4, 76, 80, 45, 88, 68, 18, 63, 50, 81, 65, 28, 14, 52, 71, 62, 78, 51, 21, 82, 69, 15, 11, 92, 91, 42, 85, 38, 32, 53, 5, 48, 83, 57, 34, 86, 67, 23, 10, 29, 11};
//        int[] tmSolutionLine2  = new int[]{86, 67, 75, 24, 35, 87, 28, 63, 89, 27, 59, 76, 14, 12, 88, 19, 90, 50, 34, 45, 41, 18, 84, 22, 80, 66, 30, 44, 48, 5, 8, 11, 10, 43, 42, 9, 81, 82, 52, 69, 2, 60, 83, 17, 49, 26, 38, 73, 0, 92, 4, 31, 62, 33, 32, 40, 6, 23, 79, 16, 58, 20, 61, 64, 54, 39, 74, 47, 51, 68, 21, 3, 91, 37, 15, 72, 56, 78, 1, 71, 55, 77, 13, 85, 46, 70, 29, 53, 65, 57, 25, 7, 36, 5};
//        int[] tmSolutionLine3  = new int[]{2, 53, 83, 16, 60, 66, 69, 92, 85, 23, 68, 15, 82, 79, 87, 4, 26, 75, 84, 72, 46, 88, 32, 52, 80, 27, 55, 36, 61, 35, 31, 34, 86, 7, 64, 30, 45, 0, 21, 90, 17, 73, 22, 51, 71, 8, 65, 63, 19, 9, 38, 81, 56, 33, 24, 47, 13, 28, 29, 5, 48, 12, 62, 14, 3, 74, 57, 40, 20, 78, 37, 1, 10, 25, 59, 42, 76, 39, 70, 67, 41, 49, 6, 89, 58, 44, 50, 43, 11, 54, 77, 18, 91, 7};
//        int[] tmSolutionLine4  = new int[]{42, 67, 70, 50, 21, 46, 68, 0, 88, 87, 55, 49, 18, 36, 75, 22, 11, 25, 23, 92, 51, 40, 66, 89, 77, 73, 61, 10, 56, 52, 27, 15, 16, 76, 53, 91, 32, 7, 72, 62, 4, 39, 71, 70, 38, 28, 14, 90, 43, 2, 48, 54, 37, 65, 83, 69, 3, 24, 9, 33, 30, 6, 74, 64, 19, 81, 45, 82, 47, 41, 78, 31, 86, 34, 80, 5, 58, 60, 29, 1, 17, 57, 12, 20, 59, 26, 84, 13, 44, 35, 63, 79, 85, 6};
//        int[] tmSolutionLine5  = new int[]{61, 59, 75, 71, 24, 38, 51, 48, 26, 90, 23, 12, 1, 27, 32, 10, 83, 85, 76, 20, 82, 21, 92, 67, 64, 58, 3, 89, 69, 87, 13, 39, 14, 49, 60, 33, 6, 54, 34, 50, 46, 18, 72, 8, 7, 57, 53, 84, 0, 37, 4, 88, 30, 41, 31, 2, 17, 52, 25, 16, 42, 73, 56, 66, 5, 68, 74, 81, 77, 78, 35, 15, 28, 19, 40, 55, 43, 70, 65, 45, 11, 47, 22, 79, 80, 9, 36, 44, 29, 62, 91, 86, 63, 7};
//        int[] tmSolutionLine6  = new int[]{7, 61, 27, 6, 54, 66, 69, 55, 53, 22, 49, 28, 83, 44, 0, 2, 65, 16, 31, 24, 36, 39, 4, 89, 37, 30, 33, 79, 48, 25, 23, 35, 9, 73, 92, 50, 76, 46, 42, 56, 52, 11, 71, 8, 38, 62, 14, 90, 43, 72, 87, 51, 10, 3, 21, 85, 77, 88, 64, 18, 63, 91, 81, 75, 45, 82, 74, 68, 47, 41, 78, 17, 86, 34, 80, 5, 58, 60, 29, 1, 15, 57, 12, 20, 59, 26, 84, 13, 19, 67, 70, 32, 40, 9};
//        int[] tmSolutionLine7  = new int[]{54, 91, 3, 67, 31, 58, 59, 86, 21, 71, 20, 14, 70, 12, 87, 4, 27, 75, 84, 37, 50, 63, 90, 53, 64, 66, 61, 69, 48, 18, 8, 34, 32, 43, 42, 9, 81, 82, 5, 10, 2, 60, 83, 11, 49, 26, 38, 73, 72, 41, 24, 0, 6, 39, 88, 85, 1, 23, 13, 57, 30, 89, 46, 74, 52, 68, 7, 56, 77, 78, 35, 15, 28, 19, 40, 55, 16, 22, 65, 45, 17, 47, 33, 79, 80, 25, 36, 44, 29, 62, 76, 92, 51, 11};
//        int[] tmSolutionLine8  = new int[]{20, 11, 37, 23, 73, 63, 69, 34, 24, 27, 59, 76, 14, 12, 88, 19, 25, 67, 2, 54, 16, 33, 3, 10, 26, 64, 90, 56, 50, 81, 5, 32, 60, 39, 55, 82, 31, 70, 52, 61, 36, 42, 22, 66, 91, 0, 9, 41, 13, 58, 46, 87, 43, 4, 77, 80, 45, 47, 68, 57, 86, 49, 30, 74, 85, 8, 18, 40, 62, 78, 44, 1, 17, 21, 83, 35, 89, 29, 72, 53, 28, 6, 15, 79, 48, 75, 84, 38, 65, 92, 51, 71, 7, 9};
//        int[] tmSolutionLine9  = new int[]{89, 73, 68, 27, 21, 18, 34, 3, 60, 91, 17, 38, 29, 79, 87, 4, 26, 75, 46, 37, 84, 92, 90, 82, 80, 66, 30, 44, 69, 5, 9, 71, 10, 43, 42, 45, 11, 39, 35, 88, 76, 63, 28, 50, 24, 33, 53, 49, 56, 40, 31, 61, 0, 14, 22, 2, 6, 86, 8, 58, 78, 20, 73, 65, 23, 36, 47, 74, 72, 15, 83, 16, 41, 48, 12, 52, 54, 62, 1, 77, 25, 59, 13, 57, 55, 67, 7, 51, 64, 19, 70, 32, 85, 11};
//        int[] tmSolutionLine10  = new int[]{32, 52, 82, 18, 20, 46, 1, 62, 15, 75, 48, 29, 38, 73, 57, 36, 88, 23, 37, 74, 40, 81, 80, 7, 31, 58, 79, 8, 69, 5, 9, 71, 44, 43, 42, 21, 11, 39, 35, 45, 17, 55, 28, 50, 63, 33, 53, 27, 0, 68, 4, 61, 30, 41, 64, 2, 26, 76, 10, 66, 16, 49, 56, 83, 91, 92, 24, 22, 13, 70, 34, 12, 72, 19, 90, 3, 84, 60, 65, 6, 14, 47, 78, 25, 86, 77, 54, 67, 89, 51, 87, 59, 85, 6};
//        int[] tmSolutionLine11  = new int[]{38, 84, 27, 6, 54, 66, 69, 55, 53, 22, 49, 28, 83, 44, 0, 2, 65, 16, 31, 24, 36, 39, 76, 89, 37, 30, 33, 79, 48, 25, 23, 35, 9, 73, 92, 50, 45, 46, 42, 26, 8, 51, 32, 85, 87, 17, 34, 12, 19, 86, 67, 81, 56, 63, 91, 58, 1, 90, 74, 43, 52, 75, 62, 14, 3, 18, 57, 40, 11, 78, 47, 64, 10, 15, 7, 88, 41, 5, 59, 77, 80, 71, 20, 29, 82, 13, 68, 72, 21, 4, 60, 70, 61, 14};
//        int[] tmSolutionLine12  = new int[]{63, 39, 87, 45, 21, 67, 79, 31, 71, 68, 26, 28, 3, 61, 37, 59, 27, 35, 23, 18, 52, 51, 65, 15, 41, 54, 47, 86, 24, 83, 0, 84, 58, 55, 43, 9, 72, 34, 2, 62, 4, 13, 33, 44, 89, 56, 14, 74, 70, 92, 48, 8, 49, 73, 91, 5, 81, 53, 69, 40, 77, 16, 11, 20, 66, 7, 50, 57, 42, 10, 29, 78, 36, 17, 22, 19, 90, 38, 80, 12, 76, 64, 1, 75, 88, 6, 46, 82, 30, 60, 25, 32, 85, 8};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //19号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{20, 52, 86, 80, 50, 42, 36, 46, 10, 22, 72, 68, 49, 31, 66, 67, 1, 57, 24, 26, 73, 12, 8, 65, 58, 14, 48, 60, 47, 4, 51, 75, 89, 9, 7, 61, 55, 30, 53, 74, 40, 6, 18, 3, 45, 34, 43, 39, 71, 90, 41, 77, 69, 27, 15, 88, 17, 59, 81, 35, 5, 2, 0, 70, 38, 82, 92, 91, 23, 87, 85, 62, 16, 21, 56, 78, 29, 25, 13, 44, 11, 84, 32, 79, 37, 28, 19, 63, 83, 64, 54, 76, 33, 9};
//        int[] tmSolutionLine2  = new int[]{61, 77, 83, 0, 41, 58, 50, 51, 68, 49, 92, 65, 79, 27, 10, 74, 71, 54, 75, 34, 48, 36, 8, 20, 38, 80, 30, 81, 2, 64, 70, 7, 88, 76, 53, 15, 84, 28, 16, 90, 5, 43, 45, 85, 66, 26, 62, 72, 14, 33, 29, 40, 6, 32, 46, 18, 21, 13, 39, 11, 47, 35, 42, 87, 69, 56, 23, 86, 91, 24, 82, 60, 31, 17, 1, 63, 78, 3, 57, 19, 12, 25, 67, 37, 52, 22, 73, 55, 44, 89, 9, 59, 4, 6};
//        int[] tmSolutionLine3  = new int[]{4, 0, 10, 14, 65, 55, 6, 53, 61, 86, 58, 49, 28, 80, 68, 67, 45, 57, 24, 20, 11, 13, 71, 84, 39, 8, 32, 79, 23, 88, 16, 73, 66, 27, 60, 22, 48, 72, 89, 38, 56, 64, 18, 30, 63, 46, 15, 1, 35, 90, 42, 77, 69, 9, 54, 87, 47, 59, 17, 29, 5, 21, 26, 70, 33, 75, 44, 41, 78, 76, 51, 40, 91, 2, 82, 52, 83, 81, 7, 92, 25, 3, 12, 50, 34, 43, 62, 37, 36, 19, 74, 31, 85, 7};
//        int[] tmSolutionLine4  = new int[]{64, 6, 76, 85, 56, 59, 33, 18, 77, 20, 52, 22, 45, 74, 36, 11, 38, 50, 16, 26, 73, 12, 8, 65, 14, 39, 48, 60, 47, 4, 51, 75, 89, 9, 7, 61, 55, 30, 53, 84, 25, 29, 54, 58, 92, 37, 40, 3, 35, 41, 67, 86, 43, 87, 5, 68, 27, 91, 57, 0, 49, 2, 31, 90, 13, 42, 80, 28, 88, 21, 19, 81, 82, 17, 1, 63, 79, 78, 32, 10, 46, 72, 66, 70, 34, 15, 62, 83, 71, 24, 44, 23, 69, 9};
//        int[] tmSolutionLine5  = new int[]{17, 90, 78, 59, 26, 9, 75, 80, 43, 60, 30, 71, 1, 31, 81, 79, 16, 61, 58, 70, 73, 36, 89, 34, 20, 8, 11, 92, 83, 88, 29, 54, 55, 7, 39, 22, 62, 14, 91, 42, 85, 24, 28, 65, 53, 25, 23, 72, 57, 67, 33, 40, 12, 13, 10, 5, 15, 77, 6, 64, 68, 37, 3, 87, 32, 38, 44, 52, 47, 69, 86, 50, 84, 66, 18, 27, 74, 76, 0, 82, 4, 2, 48, 51, 46, 19, 35, 21, 41, 63, 49, 45, 56, 13};
//        int[] tmSolutionLine6  = new int[]{17, 64, 8, 55, 84, 77, 78, 11, 1, 0, 70, 81, 57, 74, 30, 48, 14, 72, 49, 75, 87, 19, 85, 22, 47, 65, 12, 2, 29, 28, 13, 71, 40, 83, 68, 23, 42, 7, 36, 80, 61, 33, 20, 73, 16, 39, 32, 53, 4, 37, 91, 21, 31, 67, 82, 52, 54, 3, 5, 90, 76, 25, 43, 89, 58, 38, 44, 69, 88, 34, 9, 50, 56, 66, 18, 27, 41, 79, 92, 62, 6, 26, 86, 60, 59, 24, 10, 15, 35, 46, 63, 51, 45, 6};
//        int[] tmSolutionLine7  = new int[]{38, 92, 78, 59, 26, 9, 61, 80, 58, 60, 30, 71, 1, 31, 81, 79, 16, 54, 75, 70, 73, 36, 89, 34, 3, 83, 90, 7, 52, 64, 2, 51, 88, 76, 20, 41, 11, 28, 77, 43, 5, 22, 45, 19, 66, 25, 62, 72, 57, 33, 29, 40, 37, 32, 17, 18, 21, 13, 39, 67, 56, 86, 42, 87, 69, 68, 23, 12, 91, 24, 82, 44, 53, 10, 74, 65, 35, 85, 4, 27, 14, 55, 46, 63, 8, 49, 6, 15, 84, 47, 0, 48, 50, 6};
//        int[] tmSolutionLine8  = new int[]{28, 4, 90, 91, 33, 32, 86, 59, 49, 3, 61, 75, 76, 14, 89, 20, 63, 73, 82, 65, 87, 42, 85, 22, 12, 8, 74, 2, 13, 40, 16, 71, 53, 83, 68, 84, 37, 80, 55, 15, 45, 17, 48, 43, 19, 10, 67, 29, 7, 0, 35, 58, 30, 81, 79, 52, 21, 51, 36, 26, 44, 60, 6, 78, 47, 38, 77, 69, 88, 34, 9, 50, 57, 66, 18, 27, 41, 56, 92, 62, 46, 72, 31, 25, 5, 54, 70, 24, 39, 64, 11, 23, 1, 8};
//        int[] tmSolutionLine9  = new int[]{20, 71, 61, 80, 92, 52, 12, 46, 83, 65, 72, 50, 0, 51, 85, 67, 30, 57, 24, 64, 3, 36, 89, 22, 39, 86, 1, 79, 23, 88, 16, 73, 66, 27, 53, 15, 17, 28, 59, 54, 5, 35, 45, 19, 6, 2, 62, 38, 43, 33, 48, 14, 9, 90, 29, 18, 21, 42, 91, 75, 47, 81, 7, 13, 69, 56, 25, 44, 31, 84, 74, 60, 87, 26, 77, 63, 11, 41, 37, 68, 76, 78, 70, 34, 40, 32, 10, 49, 82, 4, 58, 55, 8, 14};
//        int[] tmSolutionLine10  = new int[]{25, 36, 85, 9, 65, 75, 28, 20, 31, 4, 66, 7, 63, 60, 46, 57, 83, 67, 19, 50, 73, 59, 42, 14, 76, 22, 64, 11, 72, 58, 12, 62, 48, 80, 86, 8, 79, 84, 68, 74, 1, 54, 18, 3, 45, 61, 43, 39, 71, 90, 16, 77, 33, 51, 78, 88, 17, 26, 81, 69, 5, 2, 0, 70, 38, 82, 44, 91, 52, 87, 27, 55, 41, 21, 56, 15, 29, 34, 13, 92, 53, 30, 32, 40, 37, 6, 49, 35, 47, 24, 89, 23, 10, 7};
//        int[] tmSolutionLine11  = new int[]{30, 68, 8, 28, 36, 71, 21, 32, 39, 61, 40, 11, 79, 0, 53, 65, 91, 31, 62, 23, 24, 76, 83, 10, 84, 14, 48, 60, 42, 88, 5, 75, 66, 27, 45, 47, 15, 72, 7, 2, 56, 64, 18, 41, 35, 46, 43, 85, 87, 90, 74, 77, 69, 9, 82, 4, 55, 59, 81, 29, 16, 51, 80, 70, 38, 89, 92, 6, 73, 49, 58, 34, 54, 17, 1, 63, 78, 3, 57, 19, 12, 25, 67, 37, 52, 22, 33, 44, 50, 20, 26, 86, 13, 6};
//        int[] tmSolutionLine12  = new int[]{3, 12, 75, 55, 84, 49, 20, 33, 72, 56, 89, 30, 46, 4, 90, 40, 77, 2, 87, 63, 61, 13, 73, 22, 32, 8, 11, 14, 7, 1, 16, 71, 27, 35, 50, 60, 66, 31, 24, 5, 45, 28, 38, 64, 19, 54, 67, 29, 42, 0, 83, 23, 43, 74, 79, 52, 21, 51, 36, 62, 44, 68, 6, 85, 47, 59, 58, 69, 88, 34, 9, 65, 57, 37, 92, 82, 41, 86, 18, 26, 10, 80, 78, 25, 15, 81, 70, 53, 39, 17, 76, 48, 91, 9};
//        int[] tmSolutionLine13  = new int[]{85, 13, 6, 29, 55, 4, 61, 45, 76, 56, 9, 79, 48, 66, 0, 57, 28, 71, 16, 21, 23, 39, 27, 33, 53, 88, 64, 83, 7, 36, 42, 8, 11, 10, 35, 18, 37, 80, 84, 50, 12, 1, 54, 89, 75, 22, 49, 47, 26, 92, 5, 20, 78, 74, 67, 60, 70, 72, 25, 81, 86, 34, 73, 38, 43, 14, 32, 24, 62, 44, 51, 40, 91, 2, 82, 52, 68, 87, 63, 30, 90, 46, 19, 15, 31, 59, 69, 77, 41, 58, 17, 3, 65, 4};
//        int[] tmSolutionLine14  = new int[]{46, 23, 17, 62, 5, 86, 20, 33, 72, 56, 89, 0, 1, 53, 88, 51, 21, 7, 41, 78, 8, 65, 27, 80, 45, 59, 6, 73, 16, 50, 79, 11, 14, 38, 61, 12, 83, 43, 22, 84, 25, 29, 54, 58, 92, 19, 40, 3, 74, 63, 26, 91, 75, 42, 49, 87, 64, 10, 85, 15, 57, 76, 2, 77, 24, 47, 36, 55, 90, 60, 81, 68, 34, 48, 39, 18, 4, 67, 31, 9, 35, 30, 44, 13, 52, 71, 66, 82, 32, 28, 70, 37, 69, 6};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //20号结果 int[] tmSolutionLine8  = new int[]{};
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{90, 22, 47, 70, 57, 72, 29, 27, 1, 71, 65, 37, 40, 15, 10, 7, 79, 54, 53, 62, 63, 66, 8, 73, 23, 86, 34, 68, 61, 35, 92, 44, 21, 60, 41, 4, 28, 38, 87, 85, 2, 76, 12, 78, 14, 84, 18, 56, 39, 89, 26, 49, 0, 43, 36, 82, 69, 30, 11, 24, 6, 3, 33, 17, 25, 48, 64, 13, 75, 83, 5, 52, 80, 74, 77, 32, 81, 88, 45, 9, 50, 59, 58, 67, 31, 16, 51, 91, 46, 19, 42, 20, 55, 7};
//        int[] tmSolutionLine2  = new int[]{73, 67, 47, 28, 56, 35, 0, 24, 74, 53, 30, 29, 40, 25, 38, 26, 84, 75, 79, 3, 19, 90, 6, 88, 65, 31, 68, 27, 80, 85, 16, 49, 8, 5, 18, 57, 55, 76, 9, 37, 60, 2, 39, 78, 63, 72, 11, 50, 62, 42, 23, 41, 12, 45, 36, 82, 15, 33, 4, 44, 58, 21, 13, 17, 1, 71, 81, 92, 20, 64, 10, 46, 22, 59, 89, 34, 43, 51, 83, 77, 70, 86, 54, 61, 69, 91, 66, 11, 32, 14, 52, 87, 48, 5};
//        int[] tmSolutionLine3  = new int[]{53, 91, 73, 42, 67, 6, 32, 62, 45, 47, 64, 17, 83, 60, 38, 43, 10, 29, 7, 89, 63, 49, 55, 52, 16, 75, 28, 37, 88, 9, 24, 65, 21, 77, 20, 8, 35, 5, 4, 85, 2, 27, 39, 78, 86, 84, 40, 70, 59, 48, 58, 26, 54, 23, 22, 1, 41, 15, 79, 30, 36, 68, 56, 72, 33, 51, 81, 76, 25, 57, 12, 11, 61, 34, 69, 31, 90, 80, 13, 46, 44, 82, 87, 71, 50, 66, 18, 14, 92, 19, 3, 74, 0, 13};
//        int[] tmSolutionLine4  = new int[]{43, 80, 90, 65, 0, 57, 55, 75, 31, 45, 3, 36, 88, 7, 64, 50, 53, 16, 18, 68, 19, 17, 73, 24, 89, 49, 11, 54, 42, 28, 61, 32, 82, 77, 35, 8, 66, 4, 87, 85, 2, 27, 39, 78, 86, 84, 40, 70, 59, 48, 72, 92, 25, 20, 46, 22, 12, 37, 63, 76, 34, 62, 69, 13, 79, 51, 81, 58, 91, 33, 29, 5, 26, 83, 10, 38, 15, 60, 71, 56, 52, 14, 44, 41, 23, 1, 6, 67, 21, 47, 74, 30, 9, 5};
//        int[] tmSolutionLine5  = new int[]{57, 40, 61, 37, 14, 6, 56, 62, 45, 92, 11, 68, 24, 34, 15, 77, 26, 17, 90, 71, 52, 0, 9, 46, 51, 28, 32, 7, 19, 69, 47, 73, 20, 43, 29, 70, 13, 16, 48, 25, 72, 79, 50, 35, 5, 78, 83, 38, 39, 84, 85, 30, 74, 8, 76, 89, 59, 60, 64, 33, 21, 12, 42, 53, 63, 18, 3, 27, 88, 55, 10, 44, 81, 54, 1, 22, 75, 87, 58, 66, 82, 86, 23, 31, 91, 41, 36, 4, 67, 2, 80, 65, 49, 5};
//        int[] tmSolutionLine6  = new int[]{19, 91, 73, 42, 14, 6, 56, 62, 45, 92, 11, 17, 58, 60, 38, 84, 10, 27, 7, 89, 44, 68, 55, 63, 16, 47, 64, 37, 83, 74, 24, 65, 21, 43, 8, 78, 41, 9, 52, 70, 34, 35, 15, 20, 13, 80, 12, 26, 75, 49, 51, 54, 25, 28, 46, 22, 87, 23, 4, 76, 39, 2, 90, 33, 79, 72, 57, 81, 36, 77, 40, 50, 5, 32, 67, 85, 3, 48, 61, 88, 69, 59, 71, 53, 82, 30, 0, 1, 31, 86, 18, 29, 66, 7};
//        int[] tmSolutionLine7  = new int[]{20, 7, 61, 43, 22, 79, 24, 10, 54, 82, 89, 55, 49, 66, 83, 75, 74, 71, 53, 41, 63, 46, 8, 4, 23, 64, 34, 31, 44, 19, 32, 59, 84, 2, 48, 68, 25, 47, 88, 42, 12, 0, 62, 57, 29, 67, 37, 80, 87, 92, 1, 77, 69, 6, 86, 35, 52, 50, 14, 17, 40, 21, 56, 28, 36, 51, 81, 58, 91, 70, 78, 5, 26, 16, 39, 38, 18, 76, 33, 9, 15, 30, 60, 13, 11, 73, 65, 85, 45, 3, 27, 72, 90, 5};
//        int[] tmSolutionLine8  = new int[]{74, 27, 10, 43, 22, 79, 24, 39, 89, 82, 48, 55, 49, 66, 83, 75, 54, 50, 53, 31, 92, 46, 85, 29, 51, 56, 32, 7, 19, 69, 47, 6, 73, 63, 64, 20, 41, 9, 52, 70, 34, 35, 15, 8, 13, 80, 12, 45, 16, 28, 71, 1, 38, 5, 58, 37, 90, 77, 26, 0, 42, 68, 57, 72, 33, 59, 81, 76, 40, 86, 25, 84, 61, 23, 11, 14, 62, 65, 78, 17, 44, 60, 3, 30, 67, 2, 18, 88, 87, 4, 91, 21, 36, 7};
//        int[] tmSolutionLine9  = new int[]{9, 70, 21, 58, 45, 49, 3, 61, 15, 69, 65, 39, 5, 84, 19, 7, 79, 71, 53, 41, 4, 46, 8, 63, 16, 47, 64, 31, 44, 83, 32, 59, 66, 2, 89, 68, 43, 40, 42, 85, 75, 57, 74, 18, 1, 50, 78, 30, 28, 20, 11, 82, 25, 62, 0, 90, 12, 38, 92, 76, 67, 13, 86, 80, 54, 60, 6, 81, 33, 91, 29, 35, 17, 27, 72, 37, 14, 26, 56, 10, 52, 88, 36, 51, 48, 87, 34, 22, 23, 55, 77, 73, 24, 6};
//        int[] tmSolutionLine10  = new int[]{79, 45, 51, 39, 80, 71, 59, 62, 87, 78, 86, 88, 19, 66, 10, 28, 91, 41, 31, 68, 77, 48, 1, 3, 60, 49, 27, 57, 43, 4, 70, 5, 85, 30, 7, 34, 24, 25, 33, 23, 38, 13, 15, 8, 75, 20, 12, 63, 22, 32, 52, 37, 18, 83, 6, 65, 35, 9, 61, 26, 0, 47, 74, 42, 44, 92, 53, 58, 14, 16, 90, 69, 2, 50, 84, 89, 56, 40, 54, 64, 36, 82, 46, 73, 17, 67, 55, 29, 11, 72, 81, 21, 76, 7};
//        int[] tmSolutionLine11  = new int[]{84, 66, 73, 42, 67, 6, 32, 70, 45, 91, 64, 17, 83, 31, 26, 86, 28, 41, 53, 62, 23, 51, 80, 44, 89, 10, 27, 4, 75, 57, 35, 49, 22, 37, 60, 20, 61, 68, 38, 56, 8, 25, 39, 1, 46, 55, 7, 90, 24, 40, 81, 65, 74, 11, 72, 88, 33, 78, 92, 48, 34, 87, 69, 85, 59, 63, 77, 76, 43, 2, 71, 13, 29, 21, 30, 58, 36, 18, 0, 50, 5, 82, 79, 54, 15, 19, 9, 47, 12, 52, 3, 16, 14, 8};
//        int[] tmSolutionLine12  = new int[]{6, 43, 63, 13, 31, 42, 17, 86, 3, 48, 0, 19, 83, 73, 52, 40, 64, 82, 54, 62, 23, 66, 80, 44, 89, 65, 27, 4, 29, 57, 35, 49, 25, 37, 60, 20, 61, 68, 75, 56, 8, 21, 39, 1, 46, 55, 7, 90, 24, 79, 77, 30, 74, 85, 72, 88, 45, 71, 92, 84, 34, 87, 69, 28, 36, 51, 81, 58, 91, 70, 78, 5, 26, 16, 10, 38, 18, 76, 33, 9, 50, 67, 15, 53, 41, 2, 14, 22, 47, 12, 11, 59, 32, 9};
//        int[] tmSolutionLine13  = new int[]{85, 9, 38, 25, 47, 22, 15, 76, 27, 78, 30, 37, 36, 34, 88, 77, 73, 39, 17, 28, 87, 43, 18, 42, 79, 20, 35, 74, 55, 41, 44, 15, 59, 81, 70, 12, 92, 26, 45, 80, 31, 72, 10, 29, 71, 63, 62, 52, 16, 83, 3, 49, 2, 57, 51, 4, 75, 1, 86, 48, 60, 19, 8, 53, 40, 5, 46, 58, 56, 82, 65, 68, 6, 13, 84, 89, 50, 24, 23, 64, 33, 90, 54, 61, 69, 91, 66, 11, 32, 14, 0, 21, 67, 7};
//        int[] tmSolutionLine14  = new int[]{14, 19, 26, 73, 90, 88, 86, 61, 48, 51, 65, 91, 40, 8, 37, 49, 87, 39, 17, 28, 32, 43, 66, 42, 79, 20, 31, 35, 55, 41, 44, 3, 84, 6, 22, 68, 27, 71, 34, 77, 69, 2, 24, 57, 5, 72, 11, 89, 62, 82, 23, 0, 60, 45, 83, 64, 15, 33, 4, 54, 58, 21, 13, 53, 47, 46, 59, 63, 7, 70, 78, 85, 92, 16, 10, 74, 18, 76, 36, 9, 50, 67, 52, 75, 30, 29, 81, 12, 56, 25, 38, 80, 1, 6};
//        int[] tmSolutionLine15  = new int[]{67, 31, 49, 91, 63, 56, 90, 65, 61, 75, 30, 2, 88, 7, 86, 50, 62, 48, 38, 71, 55, 40, 53, 1, 39, 92, 68, 21, 36, 79, 27, 84, 85, 82, 41, 4, 28, 11, 66, 23, 13, 32, 47, 14, 0, 44, 37, 52, 16, 78, 3, 15, 18, 45, 6, 80, 35, 9, 42, 26, 29, 12, 57, 72, 33, 25, 81, 76, 20, 60, 10, 46, 74, 59, 89, 34, 43, 51, 83, 77, 70, 64, 54, 22, 69, 73, 19, 24, 17, 87, 58, 5, 8, 8};
//        int[] tmSolutionLine16  = new int[]{77, 14, 81, 91, 89, 33, 3, 79, 15, 60, 65, 39, 5, 75, 27, 20, 54, 50, 53, 6, 17, 46, 69, 8, 78, 57, 25, 68, 64, 85, 86, 22, 74, 82, 70, 43, 92, 26, 12, 80, 31, 72, 10, 29, 71, 63, 9, 52, 16, 83, 62, 44, 21, 11, 76, 19, 24, 38, 0, 4, 18, 88, 48, 1, 49, 67, 87, 41, 2, 66, 45, 23, 90, 36, 58, 34, 32, 47, 73, 42, 28, 7, 37, 56, 13, 30, 35, 59, 84, 61, 55, 51, 40, 3};
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//        tmSolution.add(tmSolutionLine9);
//        tmSolution.add(tmSolutionLine10);
//        tmSolution.add(tmSolutionLine11);
//        tmSolution.add(tmSolutionLine12);
//        tmSolution.add(tmSolutionLine13);
//        tmSolution.add(tmSolutionLine14);
//        tmSolution.add(tmSolutionLine15);
//        tmSolution.add(tmSolutionLine16);
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);



//论文1号结果
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{5,6,7,8,9,0,3,1,10,2,11,12,13,14,4,15,16,17,18,19,20,5};
//        int[] tmSolutionLine2  = new int[]{11,7,6,9,8,5,12,4,13,3,2,10,1,0,14,15,16,17,18,19,20,11};
//        int[] tmSolutionLine3  = new int[]{5,4,6,3,7,2,8,9,0,10,11,12,13,14,1,15,16,17,18,19,20,5};
//        int[] tmSolutionLine4  = new int[]{0,1,2,9,10,3,11,4,12,5,7,8,6,13,14,15,16,17,18,19,20,9};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //论文2号结果
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{4,5,6,7,8,9,3,10,11,2,1,12,0,13,14,15,16,17,18,19,20,4};
//        int[] tmSolutionLine2  = new int[]{6,7,8,5,9,4,1,3,10,0,11,12,13,14,2,15,16,17,18,19,20,6};
//        int[] tmSolutionLine3  = new int[]{9,1,2,10,0,3,11,4,12,5,6,13,7,8,14,15,16,17,18,19,20,9};
//        int[] tmSolutionLine4  = new int[]{6,7,8,5,9,10,11,12,13,0,3,4,2,1,14,15,16,17,18,19,20,6};
//        int[] tmSolutionLine5  = new int[]{7,8,9,3,10,2,11,12,0,6,5,4,13,14,1,15,16,17,18,19,20,7};
//        int[] tmSolutionLine6  = new int[]{4,3,5,1,2,0,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,5};
//        int[] tmSolutionLine7  = new int[]{9,8,7,10,11,6,3,5,12,2,1,13,0,14,4,15,16,17,18,19,20,10};
//
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);

        //案例1号结果
//        List<int[]> tmSolution = new ArrayList<>();
//
//        int[] tmSolutionLine  = new int[]{5, 10, 15, 12, 20, 0, 16, 8, 1, 17, 7, 11, 18, 2, 19, 6, 14, 9, 13, 4, 3, 3};
//        int[] tmSolutionLine2  = new int[]{12, 2, 1, 20, 19, 4, 5, 6, 8, 7, 17, 0, 15, 10, 18, 16, 14, 9, 13, 11, 3, 3};
//        int[] tmSolutionLine3  = new int[]{0, 11, 3, 16, 10, 15, 1, 6, 8, 7, 17, 12, 20, 9, 19, 4, 14, 5, 13, 18, 2, 5};
//        int[] tmSolutionLine4  = new int[]{6, 2, 15, 8, 3, 0, 4, 5, 16, 10, 11, 17, 1, 9, 19, 20, 14, 7, 13, 18, 12, 5};
//        int[] tmSolutionLine5  = new int[]{8, 2, 20, 12, 4, 15, 3, 16, 10, 11, 7, 5, 17, 0, 6, 1, 14, 9, 13, 19, 18, 5};
//        int[] tmSolutionLine6  = new int[]{12, 20, 3, 1, 10, 15, 16, 6, 0, 17, 7, 11, 4, 9, 19, 8, 14, 5, 13, 18, 2, 8};
//        int[] tmSolutionLine7  = new int[]{20, 11, 3, 8, 10, 15, 1, 12, 16, 0, 9, 5, 4, 17, 18, 6, 14, 7, 13, 19, 2, 8};
//        int[] tmSolutionLine8  = new int[]{10, 2, 15, 18, 19, 16, 3, 8, 1, 20, 7, 5, 17, 0, 11, 4, 14, 6, 13, 12, 9, 7};
//
//
//        tmSolution.add(tmSolutionLine);
//        tmSolution.add(tmSolutionLine2);
//        tmSolution.add(tmSolutionLine3);
//        tmSolution.add(tmSolutionLine4);
//        tmSolution.add(tmSolutionLine5);
//        tmSolution.add(tmSolutionLine6);
//        tmSolution.add(tmSolutionLine7);
//        tmSolution.add(tmSolutionLine8);
//
//
//        List<Double> tmSolutionSufficiency = new ArrayList<>();
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//        tmSolutionSufficiency.add(0.0);
//
//
//        solutionPartSufficiency.set(0,tmSolutionSufficiency);
//        solutionList.set(0,tmSolution);
        /////--------------------------------------------------------------------


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

    public void evaluation(List<PassengerGroup> passengerGroups) {
        evaluateSolution(passengerGroups);
    }//进化

    public void evaluateSolution(List<PassengerGroup> passengerGroups) {

        List<Double> tmSolutionSufficiency = new ArrayList<>();
        List<List<Double>> tmSolutionPartSufficiency = new ArrayList<>();
        List<List<Float>> tmSolutionFrq = new ArrayList<>();

        List<List<Integer>> tmUnSerPassenger = new ArrayList<>();

        for(int s = 0;s<solutionList.size();s++){
            tmSolutionSufficiency.add(0.0);
            tmUnSerPassenger.add(new ArrayList<>());
            tmSolutionPartSufficiency.add(null);
            tmSolutionFrq.add(null);
        }

        EvaluateRunnable R1 = new EvaluateRunnable(1, 0, pointNum, passengerGroups,
                solutionList.subList(0,10), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R2 = new EvaluateRunnable(2, 10, pointNum, passengerGroups,
                solutionList.subList(10,20), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R3 = new EvaluateRunnable(3, 20, pointNum, passengerGroups,
                solutionList.subList(20,30), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R4 = new EvaluateRunnable(4, 30, pointNum, passengerGroups,
                solutionList.subList(30,40), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R5 = new EvaluateRunnable(5, 40, pointNum, passengerGroups,
                solutionList.subList(40,50), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R6 = new EvaluateRunnable(6, 50, pointNum, passengerGroups,
                solutionList.subList(50,60), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R7 = new EvaluateRunnable(7, 60, pointNum, passengerGroups,
                solutionList.subList(60,70), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R8 = new EvaluateRunnable(8, 70, pointNum, passengerGroups,
                solutionList.subList(70,80), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R9 = new EvaluateRunnable(9, 80, pointNum, passengerGroups,
                solutionList.subList(80,90), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);
        EvaluateRunnable R10 = new EvaluateRunnable(10, 90, pointNum, passengerGroups,
                solutionList.subList(90,100), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, pointReachabilitySet,tmUnSerPassenger);

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


        solutionPartSufficiency = null;
        solutionSufficiency = null;
        solutionFrq = null;
        unSerPassenger = null;
        unSerPassenger=tmUnSerPassenger;
        solutionFrq = tmSolutionFrq;
        solutionPartSufficiency = tmSolutionPartSufficiency;
        solutionSufficiency = tmSolutionSufficiency;//这里只需要存当前解的评价函数


    }

}
