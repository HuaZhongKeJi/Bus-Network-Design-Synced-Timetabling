package com.qz.PBGA;

import com.qz.baseClass.Line.BaseLine;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chromosome {
    public List<List<Integer>> solution;
    int pointNum;
    public int busTravelTime;
    public double passengerTravelTime;
    public double syTimes;
    public int unServedPassengers;
    public int bunchingTimes;

    public Chromosome(List<List<Integer>> solution, int pointNum){
        this.solution = solution;
        this.pointNum = pointNum;
    }
    public void addLine(List<Integer> line){
        solution.add(line);
    }
    public void removeLine(int lineNum){
        solution.remove(lineNum);
    }
    public void updateRoute(int routeNum, List<Integer> newRoute){
        solution.set(routeNum, newRoute);
    }
    public void setLineStopPir(int routeNum,int stopNum,int stopPir){
        solution.get(routeNum).set(stopNum, stopPir);
    }
    public int getLineStopPir(int routeNum,int stopNum){
        return solution.get(routeNum).get(stopNum);
    }

    public void setLineStopPirByStopID(int routeNum,int stopID,int stopPir){
        List<Node> candidateNodes = new ArrayList<>();
        candidateNodes.addAll(Node.busNodes);
        candidateNodes.addAll(Node.BRTNodes);
        int i=0;
        for(;i<candidateNodes.size();i++){
            if(candidateNodes.get(i).id == stopID){
                break;
            }
        }

        solution.get(routeNum).set(i, stopPir);
    }

    public int getLineStopPirByStopID(int routeNum,int stopID){
        List<Node> candidateNodes = new ArrayList<>();
        candidateNodes.addAll(Node.busNodes);
        candidateNodes.addAll(Node.BRTNodes);

        if(stopID==candidateNodes.size()){
            return solution.get(routeNum).get(stopID);
        }

        int i=0;
        for(;i<candidateNodes.size();i++){
            if(candidateNodes.get(i).id == stopID){
                break;
            }
        }

        return solution.get(routeNum).get(i);
    }

    public int getLineNum() {
            return solution.size();
    }
    public List<Integer> getLineCode(int lineNum) {
        if(lineNum>=solution.size()){
            return null;
        }
        return solution.get(lineNum);
    }
    public List<BusLineNew> deCode() {
        List<BusLineNew> tmBusLineList = new ArrayList<>();//不能等于
        //站点优先级
        for (List<Integer> nodesPrior : solution) {
            List<Node> candidateNodes = new ArrayList<>();
            candidateNodes.addAll(Node.busNodes);
            candidateNodes.addAll(Node.BRTNodes);
            int linePointNum = nodesPrior.get(pointNum);

            // nodeID-nodePriority
            Map<Integer, Integer> pointPriority = new HashMap<>();
            for (int j = 0; j < candidateNodes.size(); j++) {
                pointPriority.put(candidateNodes.get(j).id, nodesPrior.get(j));
            }
            List<Node> nodeList = candidateNodes;
            List<Node> routeNodeList = new ArrayList<>();
            while (linePointNum > 0) {
                int maxP = -1;
                Node node = null;
                for (Node n : nodeList) {
                    if (candidateNodes.contains(n) && pointPriority.get(n.id) > maxP && !routeNodeList.contains(n)) {
                        node = n;
                        maxP = pointPriority.get(n.id);
                    }
                }
                if (node != null) {
                    routeNodeList.add(node);
                } else {
                    break;
                }
                nodeList = node.toNodes();
                linePointNum--;
            }
            BusLineNew busLine = new BusLineNew(BaseLine.tmID, routeNodeList);
            BaseLine.tmID++;
            tmBusLineList.add(busLine);
        }
        return tmBusLineList;
    }
}
