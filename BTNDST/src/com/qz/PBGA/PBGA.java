package com.qz.PBGA;

import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Node.Node;
import com.qz.baseClass.Passenger.Passenger;
import com.qz.baseClass.PassengerGroup;
import com.qz.tool.EvaluateRunnableNetworkFrequency;
import com.qz.tool.EvaluateRunnableNetworkTimetable;
import com.qz.tool.IniData;
import com.qz.tool.MatOperate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PBGA {
    private final int pointNum;
    private final double mortalityRate = 0.15;//淘汰率
    private final int populationSize;//种群规模

    public static Double b = 0.8;//片段适应度衰减系数
    private Double c1 = 0.1;//基因交叉概率
    private Double c2 = 0.1;//基因交叉概率

    private Double C1 = 0.5;//染色体交叉概率
    private Double C2 = 0.5;//染色体交叉概率

    private Double m1 = 0.5;//变异概率(仅有片段变异)

    private Double m2 = 0.5;//变异概率

    private double rateOfA = 0.5;//后代ABC占比
    private double rateOfB = 0.3;

    private double sumSolutionSufficiency = 0;
    private double avgSolutionSufficiency = 0;
    private double maxSolutionSufficiency = 0;

    private double sumSolutionPartSufficiency = 0;
    private double avgSolutionPartSufficiency = 0;
    private double maxSolutionPartSufficiency = 0;

    private List<Chromosome> solutionList;//解集
    private List<List<Float>> solutionFrq;//解集
    private List<List<Passenger>> unSerPassenger;//解集

    public static List<List<Double>> solutionPartSufficiency;//解集中片段的适应度
    private List<Double> solutionSufficiency;//解的适应度函数

    public Chromosome getBestSolution() {
        return BestSolution;
    }

    public int getPassengerNotAsignRes() {
        return PassengerNotAsignRes;
    }

    public void setPassengerNotAsignRes(int passengerNotAsignRes) {
        PassengerNotAsignRes = passengerNotAsignRes;
    }

    public static int PassengerNotAsignRes;
    //public static List<int[]> BestSolution;
    public static Chromosome BestSolution;
    public static List<BusLineNew> BestBusLine;
    public static Double BestCost = -9999999999999999999999999.0;

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
        PBGA.totalTraveltime = totalTraveltime;
    }

    public static Double getTraveledPassenger() {
        return traveledPassenger;
    }

    public static void setTraveledPassenger(Double traveledPassenger) {
        PBGA.traveledPassenger = traveledPassenger;
    }

    public static Double getUnservedPassenger() {
        return unservedPassenger;
    }

    public static void setUnservedPassenger(Double unservedPassenger) {
        PBGA.unservedPassenger = unservedPassenger;
    }

    public static Double getTransferNumber() {
        return transferNumber;
    }

    public static void setTransferNumber(Double transferNumber) {
        PBGA.transferNumber = transferNumber;
    }

    public static Double getRouteNumber() {
        return routeNumber;
    }

    public static void setRouteNumber(Double routeNumber) {
        PBGA.routeNumber = routeNumber;
    }

    public static Double getOperatingCost() {
        return operatingCost;
    }

    public static void setOperatingCost(Double operatingCost) {
        PBGA.operatingCost = operatingCost;
    }



    public static Double PassengerCost = -9999999999999999999999999.0;
    public static Double OperatorCost = -9999999999999999999999999.0;

    //需要记录没有被用到的片段，着力进行裁剪-----------------------------------------------------------------------------------------------！！！！！！！！！！！！！！！！！！

    public List<BusLineNew> getBestBusLine() {
        return BestBusLine;
    }

    public double getBestCost() {
        return BestCost;
    }

    public PBGA(int population_size) {
        solutionList = new ArrayList<>();
        solutionSufficiency = new ArrayList<>();
        //pointReachabilitySet = new ArrayList<>();
        solutionPartSufficiency = new ArrayList<>();

        pointNum = Node.BRTNodes.size()+Node.busNodes.size();
        populationSize = population_size;
        //生成可达矩阵集合

    }

    public void selectSolution() {//根据适应度函数，选择算子,遗传
        MatOperate.QuickSortPbga(solutionSufficiency, 0, solutionSufficiency.size() - 1, solutionList, solutionPartSufficiency, solutionFrq,unSerPassenger);
        int mortalityNum = (int) (mortalityRate * populationSize);

        List<Chromosome> newSolutionList = new ArrayList<>();
        List<List<Double>> newSolutionPartSufficiency = new ArrayList<>();
        List<Double> tmsolutionSufficiency = new ArrayList<>();
        List<List<Float>> newSolutionFrq = new ArrayList<>();
        List<List<Passenger>> newUnSerPassenger = new ArrayList<>();

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


        for (int i = 0; i < populationSize; i++) {

            //随机选两个比好坏
            double ram1 = Math.random();
            double ram2 = Math.random();
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

                if (ram < ts) {//轮盘赌，选择了j
                    List<List<Integer>> tmSolu = new ArrayList<>();
                    for (int st = 0; st < solutionList.get(tmdj).getLineNum(); st++) {
                        List<Integer> tmSolu1 = new ArrayList<>();
                        for (int ss = 0; ss < pointNum + 1; ss++) {
                            tmSolu1.add(ss, solutionList.get(tmdj).getLineCode(st).get(ss));
                        }
                        tmSolu.add(tmSolu1);
                    }
                    tmsolutionSufficiency.add(tmSolutionSufficiency);
                    Chromosome newC = new Chromosome(tmSolu,pointNum);
                    newSolutionList.add(newC);
                    sumSolutionSufficiency += tmSolutionSufficiency;
                    if (tmSolutionSufficiency > maxSolutionSufficiency) {
                        maxSolutionSufficiency = tmSolutionSufficiency;
                    }

                    List<Float> tmSolutionFrq = new ArrayList<>();
                    List<Double> tmSolutionPartSufficiency = new ArrayList<>();

                    List<Passenger> tmUnSerPassenger = new ArrayList<>(unSerPassenger.get(tmdj));

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

        //线路交叉
        while (!tmL.isEmpty()) {
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
                    int Cnum = (int) (Math.random() * IniData.minLineNum);
                    for(int ssc = 0;ssc < Cnum;ssc ++){
                        int cs1 = (int) (Math.random() * solutionList.get(ms1).getLineNum());
                        int cs2 = (int) (Math.random() * solutionList.get(ms2).getLineNum());

                        List<Integer> tmSolu = solutionList.get(ms1).getLineCode(cs1);
                        solutionList.get(ms1).updateRoute(cs1,solutionList.get(ms2).getLineCode(cs2));
                        solutionList.get(ms2).updateRoute(cs2,tmSolu);


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



        //站点交叉
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < solutionPartSufficiency.get(i).size(); j++) {
                double partSufficiency = solutionPartSufficiency.get(i).get(j);
                double Pc = 0;//交叉概率
                if (partSufficiency > avgSolutionPartSufficiency) {
                    Pc = c1 * (maxSolutionPartSufficiency - partSufficiency) / (maxSolutionPartSufficiency - avgSolutionPartSufficiency);
                } else {
                    Pc = c2;
                }
                double ramPt = Math.random();
                if (ramPt < Pc) {//需要交叉,随机选择一个解进行交叉，基因组交叉

                    int tVariation = (int) (Math.random() * populationSize);
                    int tVariation2 = (int) (Math.random() * solutionList.get(tVariation).getLineNum());

                    Random df = new Random();
                    int ran1 = df.nextInt(pointNum + 1);
                    int ran2 = df.nextInt(pointNum + 1);
                    int tmStart = Math.min(ran1, ran2);
                    int tmEnd = Math.max(ran1, ran2);

                    for (int tm = tmStart; tm <= tmEnd; tm++) {
                        int tmSwInt = solutionList.get(i).getLineCode(j).get(tm);
                        int tmSwInt2 = solutionList.get(tVariation).getLineCode(tVariation2).get(tm);
                        int bk = 0;
                        if(tm == pointNum){
                            solutionList.get(i).setLineStopPir(j,tm,tmSwInt2);
                            solutionList.get(tVariation).setLineStopPir(tVariation2,tm,tmSwInt);
                        }else{
                            for(int sss = 0;sss<pointNum;sss++){
                                if(solutionList.get(i).getLineCode(j).get(sss) == tmSwInt2){
                                    solutionList.get(i).setLineStopPir(j,sss,tmSwInt);
                                    solutionList.get(i).setLineStopPir(j,tm,tmSwInt2);
                                    break;
                                }
                            }
                            for(int sss = 0;sss<pointNum;sss++){
                                if(solutionList.get(tVariation).getLineCode(tVariation2).get(sss) == tmSwInt){
                                    solutionList.get(tVariation).setLineStopPir(tVariation2,sss,tmSwInt2);
                                    solutionList.get(tVariation).setLineStopPir(tVariation2,tm,tmSwInt);
                                    break;
                                }
                            }
                        }

                    }
                }

            }
        }


        //变异
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

            double ramPt1 = Math.random();//变异位点
            double ramPt2 = Math.random();//删除
            double ramPt3 = Math.random();//插入（插入为服务乘客起点的线路）

            if (ramPt3 < Pm && solutionList.get(i).getLineNum()<IniData.maxLineNum&& !unSerPassenger.get(i).isEmpty()) {//插入一个新解，随机生成的解


                int tmPs = (int) (Math.random() * unSerPassenger.get(i).size());
                Passenger passenger = unSerPassenger.get(i).get(tmPs);
                List<Integer> startPoint = passenger.getBoardingNode();

                List<Integer> tm1 = new ArrayList<>();

                for (int is = 1; is < pointNum; is++) {
                    tm1.add(is);
                }
                List<Integer> tmSolutionLine = new ArrayList<>();
                for (int k = 0; k <= pointNum; k++) {
                    if(k==startPoint.get(0)){
                        tmSolutionLine.add(0);
                    }else if(k==pointNum){
                        tmSolutionLine.add(df.nextInt(IniData.maxLineNodeNum - IniData.minLineNodeNum + 1) + IniData.minLineNodeNum);
                    }else{
                        tmSolutionLine.add(tm1.remove(df.nextInt(tm1.size())));
                    }
                }

                solutionList.get(i).addLine(tmSolutionLine);
                solutionPartSufficiency.get(i).add(0.0);
                solutionFrq.get(i).add(0.5F);
            }

            if (ramPt2 < Pm && solutionList.get(i).getLineNum()>IniData.minLineNum) {//删除一个差的解---------------------删除所有未用到的解

                delInt.clear();
                for (int j = 0; j < solutionPartSufficiency.get(i).size(); j++) {

                    if (solutionFrq.get(i).get(j) == 0) {
                        delInt.add(j);
                    }

                }
                if (delInt.isEmpty()) {
                    int tmWorst = (int) (Math.random() * solutionPartSufficiency.get(i).size());
                    solutionFrq.get(i).remove(tmWorst);
                    solutionPartSufficiency.get(i).remove(tmWorst);
                    solutionList.get(i).removeLine(tmWorst);
                } else {
                    solutionFrq.get(i).remove(delInt.size() - 1);
                    solutionPartSufficiency.get(i).remove(delInt.size() - 1);
                    solutionList.get(i).removeLine(delInt.size() - 1);
                }

            }

            if (ramPt1 < Pm) {//站点变异

                double ramPt12 = Math.random();
                List<BusLineNew> tmBusLine = solutionList.get(i).deCode();
                //随机取一个变异
                int ttVariation = (int) (Math.random() * solutionList.get(i).getLineNum());
                boolean swF = false;
                if (ramPt12 > 0.7) {//缩短线路

                    for(int tmpLineNum = 0;tmpLineNum<solutionList.get(i).getLineNum()&&!swF;tmpLineNum++){

                        BusLineNew tBusLine = tmBusLine.get(tmpLineNum);
                        List<Node> RpointList = tBusLine.getBusNode();//线路站点集合
                        for(int pN = 0;pN<RpointList.size()-2;pN++){
                            int stop1 = RpointList.get(pN).id;
                            int stop2 = RpointList.get(pN+1).id;
                            int stop3 = RpointList.get(pN+2).id;
                            if(Node.getNode(stop1).toNodes().contains(Node.getNode(stop3))){//判断是否存在跨区相连
                                int tmpS3Po = solutionList.get(i).getLineStopPirByStopID(tmpLineNum,stop3);
                                solutionList.get(i).setLineStopPirByStopID(tmpLineNum,stop3,solutionList.get(i).getLineStopPirByStopID(tmpLineNum,stop2));
                                solutionList.get(i).setLineStopPirByStopID(tmpLineNum,stop2, tmpS3Po);

                                for(int tmpNumSeq = 0;tmpNumSeq<pointNum;tmpNumSeq++){
                                    if(solutionList.get(i).getLineStopPirByStopID(tmpLineNum,tmpNumSeq) > tmpS3Po){
                                        solutionList.get(i).setLineStopPirByStopID(tmpLineNum,tmpNumSeq,solutionList.get(i).getLineStopPirByStopID(tmpLineNum,tmpNumSeq)-1);
                                    }
                                }
                                solutionList.get(i).setLineStopPirByStopID(tmpLineNum,stop2, pointNum-1);
                                solutionList.get(i).setLineStopPirByStopID(tmpLineNum,pointNum,solutionList.get(i).getLineStopPirByStopID(tmpLineNum,pointNum) - 1);
                                swF = true;
                            }
                        }
                    }

                }
                if(!swF){
                    if (ramPt12 > 0.6) {//变异其中一个点
                        BusLineNew tBusLine = tmBusLine.get(ttVariation);
                        if(tBusLine.getBusNode().isEmpty()){
                            int iii = 0;
                        }
                        int ttVariation2 = (int) (Math.random() * tBusLine.getBusNode().size());
                        int variationPoint = tBusLine.getBusNode().get(ttVariation2).id;
                        //随机选择一个变异位点------variationPoint
                        int variationPoint2 = 0;
                        int variationValue = pointNum-1;//(int) (Math.random() * pointNum);
                        for (int az = 0; az < pointNum; az++) {
                            if (solutionList.get(i).getLineStopPir(ttVariation,az) == variationValue) {
                                variationPoint2 = az;
                                break;
                            }
                        }
                        int tmSwInt = solutionList.get(i).getLineStopPirByStopID(ttVariation,variationPoint);
                        solutionList.get(i).setLineStopPirByStopID(ttVariation,variationPoint ,solutionList.get(i).getLineStopPir(ttVariation,variationPoint2));
                        solutionList.get(i).setLineStopPir(ttVariation,variationPoint2,tmSwInt);
                    } else if(ramPt12<0.3){//变异长度---------------//并且替换起点
                        double ramPtX = Math.random();
                        if(ramPtX>0.5){
                            solutionList.get(i).setLineStopPir(ttVariation,pointNum,solutionList.get(i).getLineStopPir(ttVariation,pointNum) +1);
                        }else{
                            solutionList.get(i).setLineStopPir(ttVariation,pointNum,solutionList.get(i).getLineStopPir(ttVariation,pointNum) -1);
                        }
                    }else{//替换起点并长度减一
                        BusLineNew tBusLine = tmBusLine.get(ttVariation);
                        if(tBusLine.getBusNode().size()>1){
                            int variationPoint = tBusLine.getBusNode().get(0).id;
                            int variationPoint2 = tBusLine.getBusNode().get(1).id;
                            int tmSwInt = solutionList.get(i).getLineStopPirByStopID(ttVariation,variationPoint);
                            solutionList.get(i).setLineStopPirByStopID(ttVariation,variationPoint,solutionList.get(i).getLineStopPirByStopID(ttVariation,variationPoint2));
                            solutionList.get(i).setLineStopPirByStopID(ttVariation,variationPoint2,tmSwInt);

                            solutionList.get(i).setLineStopPir(ttVariation,pointNum,solutionList.get(i).getLineStopPir(ttVariation,pointNum)-1);
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
            List<List<Integer>> tmSolution = new ArrayList<>();
            List<Double> tmSolutionSufficiency = new ArrayList<>();

            Random random = new Random();
            int randomNumber = random.nextInt(IniData.maxLineNum - IniData.minLineNum + 1) + IniData.minLineNum;

            for (int j = 0; j < randomNumber; j++) {
                List<Integer> tmSolutionLine = new ArrayList<>();
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
                tmSolution.add(tmSolutionLine);
                tmSolutionSufficiency.add(0.0);
            }

            Chromosome newC = new Chromosome(tmSolution,pointNum);
            solutionList.add(newC);
            solutionSufficiency.add(0.0);
            solutionPartSufficiency.add(tmSolutionSufficiency);
        }


    }

    public void evaluation(boolean withTimeTable) {
        evaluateSolution(withTimeTable);
    }//进化

    public void evaluateSolution(boolean withTimeTable) {

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


        if(withTimeTable){
            EvaluateRunnableNetworkTimetable R1 = new EvaluateRunnableNetworkTimetable(1, 0,solutionList.subList(0,10), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq,tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R2 = new EvaluateRunnableNetworkTimetable(2, 10,solutionList.subList(10,20), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R3 = new EvaluateRunnableNetworkTimetable(3, 20,solutionList.subList(20,30), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R4 = new EvaluateRunnableNetworkTimetable(4, 30,solutionList.subList(30,40), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R5 = new EvaluateRunnableNetworkTimetable(5, 40,solutionList.subList(40,50), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R6 = new EvaluateRunnableNetworkTimetable(6, 50,solutionList.subList(50,60), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R7 = new EvaluateRunnableNetworkTimetable(7, 60,solutionList.subList(60,70), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R8 = new EvaluateRunnableNetworkTimetable(8, 70,solutionList.subList(70,80), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R9 = new EvaluateRunnableNetworkTimetable(9, 80,solutionList.subList(80,90), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkTimetable R10 = new EvaluateRunnableNetworkTimetable(10, 90,solutionList.subList(90,100), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);

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
        }else{
            EvaluateRunnableNetworkFrequency R1 = new EvaluateRunnableNetworkFrequency(1, 0,solutionList.subList(0,10), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq,tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R2 = new EvaluateRunnableNetworkFrequency(2, 10,solutionList.subList(10,20), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R3 = new EvaluateRunnableNetworkFrequency(3, 20,solutionList.subList(20,30), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R4 = new EvaluateRunnableNetworkFrequency(4, 30,solutionList.subList(30,40), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R5 = new EvaluateRunnableNetworkFrequency(5, 40,solutionList.subList(40,50), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R6 = new EvaluateRunnableNetworkFrequency(6, 50,solutionList.subList(50,60), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R7 = new EvaluateRunnableNetworkFrequency(7, 60,solutionList.subList(60,70), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R8 = new EvaluateRunnableNetworkFrequency(8, 70,solutionList.subList(70,80), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R9 = new EvaluateRunnableNetworkFrequency(9, 80,solutionList.subList(80,90), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
            EvaluateRunnableNetworkFrequency R10 = new EvaluateRunnableNetworkFrequency(10, 90,solutionList.subList(90,100), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);

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
