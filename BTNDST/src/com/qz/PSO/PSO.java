package com.qz.PSO;

import com.qz.PBGA.Chromosome;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Node.Node;
import com.qz.baseClass.Passenger.Passenger;
import com.qz.tool.IniData;
import com.qz.tool.MatOperate;

import java.util.*;

public class PSO {

    public static Double PassengerCost = -9999999999999999999999999.0;
    public static Double OperatorCost = -9999999999999999999999999.0;
    public static float[] bestSoluBRTLaneFeq;
    public static int PassengerNotAsignRes;
    public static List<BusLineNew> BestBusLine;
    public static Chromosome BestSolution;
    private List<Chromosome> solutionList;//解集
    public static Double b = 0.8;//片段适应度衰减系数
    public static Double BestCost = -9999999999999999999999999.0;
    public static Double totalTraveltime = 9999999999999999999999999999.0;
    public static Double traveledPassenger = 9999999999999999999999999999.0;
    public static Double unservedPassenger = 9999999999999999999999999999.0;
    public static Double transferNumber = 9999999999999999999999999999.0;
    public static Double routeNumber = 9999999999999999999999999999.0;
    public static Double operatingCost = 9999999999999999999999999999.0;
    private List<List<Passenger>> unSerPassenger;
    private List<List<Float>> solutionFrq;//线路的发车频率

    public static List<Double> liziMaxFitness;//粒子最优适应度
    public static List<Chromosome> liziBestSolution;//例子最优解
    public static List<List<double[]>> liziSpeed;//例子速度
    //public static List<int[]> bestSolution;//全局最优解

    public static List<List<Double>> solutionPartSufficiency;//解集中片段的适应度
    private List<Double> solutionSufficiency;//解的适应度函数


    private final int pointNum;
    private final int populationSize;//种群规模

    public PSO(int population_size) {

        liziSpeed = new ArrayList<>();
        liziMaxFitness = new ArrayList<>();
        liziBestSolution = new ArrayList<>();
        BestSolution = null;

        solutionList = new ArrayList<>();
        solutionSufficiency = new ArrayList<>();
        solutionPartSufficiency = new ArrayList<>();

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
            List<List<Integer>> tmSolution = new ArrayList<>();
            List<List<Integer>> tmSolution2 = new ArrayList<>();
            List<double[]> tmSpeed = new ArrayList<>();
            List<Double> tmSolutionSufficiency = new ArrayList<>();

            Random random = new Random();
            int randomNumber = random.nextInt(IniData.maxLineNum - IniData.minLineNum + 1) + IniData.minLineNum;

            for (int j = 0; j < randomNumber; j++) {//------------------------------------------------------------
                List<Integer> tmSolutionLine = new ArrayList<>();
                List<Integer> tmSolutionLine2 = new ArrayList<>();
                double[] tmSpeedLine = new double[pointNum + 1];
                for (int k = 0; k <= pointNum; k++) {
                    tmSpeedLine[k] = 0.0;
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

                for(int l=0;l<=pointNum;l++){
                    tmSolutionLine2.add(tmSolutionLine.get(l));
                }

                tmSolution2.add(tmSolutionLine2);
                tmSolution.add(tmSolutionLine);
                tmSpeed.add(tmSpeedLine);
                tmSolutionSufficiency.add(0.0);
            }//------------------------------------------------------------
            Chromosome tC = new Chromosome(tmSolution2,pointNum);
            liziBestSolution.add(tC);
            liziMaxFitness.add(-999999999999.0);
            liziSpeed.add(tmSpeed);
            Chromosome tC2 = new Chromosome(tmSolution,pointNum);
            solutionList.add(tC2);
            solutionSufficiency.add(0.0);
            solutionPartSufficiency.add(tmSolutionSufficiency);
        }
    }

    public List<Integer> genNewRoute(){
        List<Integer> tm1 = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < pointNum; i++) {
            tm1.add(i);
        }

        List<Integer> tmSolutionLine = new ArrayList<>();
        for (int k = 0; k <= pointNum; k++) {
            Random df = new Random();
            if (k == pointNum) {
                tmSolutionLine.add(df.nextInt(IniData.maxLineNodeNum - IniData.minLineNodeNum + 1) + IniData.minLineNodeNum);
            } else {
                tmSolutionLine.add(tm1.remove(df.nextInt(tm1.size())));
            }
        }
        return tmSolutionLine;
    }

    public void psoUpdate(int maxLoop,int nowLoop){
        double w = 0.5*((double)maxLoop-(double)nowLoop)/((double)maxLoop)+0.4;
        double stepSize = 2;
        //double stepSize = 3*((double)maxLoop-(double)nowLoop)/((double)maxLoop)+2;
        for(int i=0;i<solutionList.size();i++){
            List<List<Integer>> tmSolution = solutionList.get(i).solution;

            //线路数计算
            double routeNum =  stepSize*Math.random()*(liziBestSolution.get(i).solution.size()-tmSolution.size())
                    +stepSize*Math.random()*(BestSolution.solution.size()-tmSolution.size());

            if(routeNum<-1){
                routeNum = -1;
            }else if(routeNum>1){
                routeNum = 1;
            }
            routeNum+=solutionList.get(i).solution.size();
            int finRouteNum = (int)Math.round(routeNum);
            finRouteNum = Math.max(finRouteNum,IniData.minLineNum);
            finRouteNum = Math.min(finRouteNum,IniData.maxLineNum);

            //一条条线路计算
            //for(int j=0;j<tmSolution.size();j++){
            for(int j=0;j<finRouteNum;j++){
                if((j>=liziSpeed.get(i).size()&&j>=liziBestSolution.get(i).solution.size()&&j>=BestSolution.solution.size())||j>=tmSolution.size()){
                    if(j>=tmSolution.size()){
                        solutionList.get(i).solution.add(genNewRoute());
                        solutionPartSufficiency.get(i).add(-999.0);
                        solutionFrq.get(i).add(0.0f);
                    }else{
                        solutionList.get(i).solution.set(j,genNewRoute());
                    }
                }else{
                    Map<Integer,Double> tmNewSolu = new HashMap<>();
                    double[] Vi = new double[pointNum + 1];
                    for(int k=0;k<=pointNum;k++){
                        Vi[k] = 0;
                        if(liziSpeed.get(i).size()>j){
                            Vi[k] = w*liziSpeed.get(i).get(j)[k];
                        }
                        if(liziBestSolution.get(i).solution.size()>j){
                            Vi[k] += stepSize*Math.random()*(liziBestSolution.get(i).solution.get(j).get(k)-tmSolution.get(j).get(k));
                        }
                        if(BestSolution.solution.size()>j){
                            Vi[k] += stepSize*Math.random()*(BestSolution.solution.get(j).get(k)-tmSolution.get(j).get(k));
                        }
                        tmNewSolu.put(k,tmSolution.get(j).get(k) + Vi[k]);
                    }
                    if(liziSpeed.get(i).size()<=j){
                        liziSpeed.get(i).add(Vi);
                    }else{
                        liziSpeed.get(i).set(j,Vi);
                    }

                    List<Map.Entry<Integer, Double>> sortedEntries = new ArrayList<>(tmNewSolu.entrySet());
                    sortedEntries.sort(Map.Entry.comparingByValue());

                    List<Integer> tnewSolu = new ArrayList<>();
                    for(int k=0;k<pointNum+1;k++){
                        tnewSolu.add(0);
                    }
                    int tk = 0;

                    for (Map.Entry<Integer, Double> entry : sortedEntries) {
                        if(entry.getKey() != pointNum){
                            tnewSolu.set(entry.getKey(),tk);
                            tk ++;
                        }else{
                            int routeNums = tmSolution.get(j).get(pointNum);
                            if(entry.getValue()>routeNums && routeNums<IniData.maxLineNodeNum){
                                routeNums ++;
                            }else if(entry.getValue()<routeNums && routeNums>IniData.minLineNodeNum){
                                routeNums --;
                            }
                            //Math.max((int) Math.round(entry.getValue()),BaseInfo.getMinLinePointNum());
                            //pointNum = Math.min(pointNum,BaseInfo.getMaxLinePointNum());
                            tnewSolu.set(entry.getKey(),routeNums);
                        }
                    }
                    solutionList.get(i).solution.set(j,tnewSolu);//赋值新解
                }
            }
        }
    }

    //线路长度必须变异，要不不会找到好的解
    public void mutation(){
        //子解变异
        List<Integer> delInt = new ArrayList<>();
        Random df = new Random();
        for (int i = 0; i < populationSize; i++) {
            double Sufficiency = solutionSufficiency.get(i);
            double Pm = 0.1;//变异概率
            double Pm2 = 0.3;//变异概率
            /*if (Sufficiency > avgSolutionSufficiency) {
                Pm = m1 * (maxSolutionSufficiency - Sufficiency) / (maxSolutionSufficiency - avgSolutionSufficiency);
            } else {
                Pm = m2;
            }*/

            double ramPt1 = java.lang.Math.random();//变异位点

            double ramPt2 = java.lang.Math.random();//删除
            double ramPt3 = java.lang.Math.random();//插入（插入为服务乘客起点的线路）

            if (ramPt3 < Pm && solutionList.get(i).solution.size()<IniData.maxLineNum&&unSerPassenger.get(i).size()>0) {//插入一个新解，随机生成的解

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

            if (ramPt2 < Pm2 && solutionList.get(i).solution.size()>IniData.minLineNum) {//删除一个差的解---------------------删除所有未用到的解

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
                    solutionList.get(i).removeLine(tmWorst);
                } else {
                    solutionFrq.get(i).remove(delInt.size() - 1);
                    solutionPartSufficiency.get(i).remove(delInt.size() - 1);
                    solutionList.get(i).removeLine(delInt.size() - 1);
                }

            }

            if (ramPt1 < Pm) {//子解变异

                double ramPt12 = java.lang.Math.random();
                List<BusLineNew> tmBusLine = solutionList.get(i).deCode();
                //随机取一个变异
                int ttVariation = (int) (Math.random() * solutionList.get(i).solution.size());
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
    }

    public boolean cheak(){
        for(int i=0;i<solutionList.size();i++){
            if(solutionList.get(i).solution.size()!=solutionPartSufficiency.get(i).size()){
                return false;
            }
        }
        return true;
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

        EvaluateRunnableNetworkTimetablePSO R1 = new EvaluateRunnableNetworkTimetablePSO(1, 0,
                solutionList.subList(0,10), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R2 = new EvaluateRunnableNetworkTimetablePSO(2, 10,
                solutionList.subList(10,20), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R3 = new EvaluateRunnableNetworkTimetablePSO(3, 20,
                solutionList.subList(20,30), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R4 = new EvaluateRunnableNetworkTimetablePSO(4, 30,
                solutionList.subList(30,40), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R5 = new EvaluateRunnableNetworkTimetablePSO(5, 40,
                solutionList.subList(40,50), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R6 = new EvaluateRunnableNetworkTimetablePSO(6, 50,
                solutionList.subList(50,60), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R7 = new EvaluateRunnableNetworkTimetablePSO(7, 60,
                solutionList.subList(60,70), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R8 = new EvaluateRunnableNetworkTimetablePSO(8, 70,
                solutionList.subList(70,80), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R9 = new EvaluateRunnableNetworkTimetablePSO(9, 80,
                solutionList.subList(80,90), tmSolutionSufficiency,tmSolutionPartSufficiency, tmSolutionFrq, tmUnSerPassenger);
        EvaluateRunnableNetworkTimetablePSO R10 = new EvaluateRunnableNetworkTimetablePSO(10, 90,
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


        solutionPartSufficiency = null;
        solutionSufficiency = null;
        unSerPassenger = null;
        unSerPassenger=tmUnSerPassenger;
        solutionFrq = null;
        solutionFrq = tmSolutionFrq;

        solutionPartSufficiency = tmSolutionPartSufficiency;
        solutionSufficiency = tmSolutionSufficiency;//这里只需要存当前解的评价函数


    }

}
