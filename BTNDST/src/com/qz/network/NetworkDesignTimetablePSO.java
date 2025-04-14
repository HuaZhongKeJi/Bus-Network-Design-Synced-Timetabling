package com.qz.network;

import com.qz.DE.DE;
import com.qz.PBGA.Chromosome;
import com.qz.PSO.PSO;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Node.Node;
import com.qz.tool.BaseInfo;
import com.qz.tool.IniData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkDesignTimetablePSO {
    //测试一下GIT222222
    //线路用一个字符串表达，线网用一个字符串list表达

    public static void main(String args[]) {

        IniData.IniDataFromCsv();

        PSO pso = new PSO(100);
        pso.ini();
        List<Integer> loopR = new ArrayList<>();
        List<Double> costR = new ArrayList<>();
        List<Integer> UnSignedPassenger = new ArrayList<>();

        double tmCost = 0;
        int tmF = 0;

        for(int i=0;i<100;i++){
            System.out.println("Loop: " +  i);
            pso.evaluation();

            pso.psoUpdate(100,i);

            pso.mutation();


            loopR.add(i);
            costR.add(PSO.BestCost);
            UnSignedPassenger.add(PSO.PassengerNotAsignRes);

            if(tmCost == PSO.BestCost){
                tmF ++;
            }else{
                tmCost = PSO.BestCost;
                tmF = 0;
            }

            System.out.println("tmF: " +  tmF);
            System.out.println("BEST: " +  PSO.BestCost);

        }

        List<BusLineNew> BestBusLine = PSO.BestBusLine;
        double BestCost = PSO.BestCost;
        Chromosome BestSolution = PSO.BestSolution;


        try{
            File file =new File("./resultPSO.txt");
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
