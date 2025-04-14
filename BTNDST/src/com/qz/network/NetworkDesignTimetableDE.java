package com.qz.network;

import com.qz.DE.DE;
import com.qz.PBGA.Chromosome;
import com.qz.PBGA.PBGA;
import com.qz.baseClass.BusLine;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Node.Node;
import com.qz.tool.BaseInfo;
import com.qz.tool.EvaluateNetwork;
import com.qz.tool.IniData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkDesignTimetableDE {
    //测试一下GIT222222
    //线路用一个字符串表达，线网用一个字符串list表达

    public static void main(String args[]) {

        IniData.IniDataFromCsv();

        DE de = new DE(100);
        de.ini();
        List<Integer> loopR = new ArrayList<>();
        List<Double> costR = new ArrayList<>();
        List<Integer> UnSignedPassenger = new ArrayList<>();

        double tmCost = 0;
        int tmF = 0;

        for(int i=0;i<100;i++){
            System.out.println("Loop: " +  i);
            de.evaluateSolution();
            de.deMutation();
            de.deCrossover();
            de.evaluateVSolution();
            de.selection();

            loopR.add(i);
            costR.add(DE.BestCost);
            UnSignedPassenger.add(DE.PassengerNotAsignRes);

            if(tmCost == DE.BestCost){
                tmF ++;
            }else{
                tmCost = DE.BestCost;
                tmF = 0;
            }

            System.out.println("tmF: " +  tmF);
            System.out.println("BEST: " +  DE.BestCost);

        }

        List<BusLineNew> BestBusLine = DE.BestBusLine;
        double BestCost = DE.BestCost;
        Chromosome BestSolution = DE.BestSolution;


        try{
            File file =new File("./resultDE.txt");
            if(file.exists()) file.delete();
            file.createNewFile();
            FileWriter fileWritter = new FileWriter(file.getName(),true);
            fileWritter.write(costR.toString());
            fileWritter.write("\r\n");
            fileWritter.write("busTravelTime：");
            fileWritter.write(BestSolution.busTravelTime+"");
            fileWritter.write("\r\n");

            fileWritter.write("\r\n");
            fileWritter.write("passengerTravelTime：");
            fileWritter.write(BestSolution.passengerTravelTime+"");
            fileWritter.write("\r\n");

            fileWritter.write("\r\n");
            fileWritter.write("syTimes：");
            fileWritter.write(BestSolution.syTimes+"");
            fileWritter.write("\r\n");

            fileWritter.write("\r\n");
            fileWritter.write("unServedPassengers：");
            fileWritter.write(BestSolution.unServedPassengers+"");
            fileWritter.write("\r\n");

            fileWritter.write("\r\n");
            fileWritter.write("染色体：");
            fileWritter.write("\r\n");
            for (List<Integer> ints : BestSolution.solution) {
                fileWritter.write(ints.toString());
                fileWritter.write("\r\n");
            }
            fileWritter.write("\r\n");
            fileWritter.write("公交线路：");
            fileWritter.write("\r\n");
            for (BusLineNew bl : BestBusLine) {
                for(Node node:bl.busNode){
                    fileWritter.write(node.id+",");
                }
                fileWritter.write("\r\n");
            }

            fileWritter.close();
        }catch(IOException e){
            e.printStackTrace();
        }

    }



}
