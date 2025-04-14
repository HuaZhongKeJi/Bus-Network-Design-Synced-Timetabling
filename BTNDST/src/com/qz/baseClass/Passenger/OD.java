package com.qz.baseClass.Passenger;

import com.qz.baseClass.Line.BRTLine;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Line.RailLine;
import com.qz.baseClass.Node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OD {
    static public List<OD> ods = new ArrayList<OD>();
    public Node start;
    public Node end;
    public int id;
    public Map<Integer,List<Passenger>> passengers;//time,passenger
    public Map<Integer,Path> paths = new HashMap<>();//path id,Path
    static public int tmId = 1;

    /// 高峰客流断面
    public int maxPassenger =0;
    public OD(int id,Node start,Node end,Map<Integer,List<Passenger>> passengers){
        this.id=id;
        this.start = start;
        this.end = end;
        this.passengers = passengers;
        passengers.forEach((key, value) -> {
            /*if(value.size()>maxPassegner){
                maxPassegner = value.size();
            }*/
            for(Passenger passenger : value){
                passenger.OD = id;
            }
        });
    }

    public void getPath(List<BRTLine> BRTlines, List<RailLine> RailLines, List<BusLineNew> busLines){
        FindPath findPath = new FindPath(BRTlines, RailLines, busLines);
        List<Path> paths = findPath.getPath1(this);
        paths.addAll(findPath.getPath2(this));
        paths.addAll(findPath.getPath3(this));
        paths.addAll(findPath.getPath4(this));
        paths.addAll(findPath.getPath5(this));
        for(Path path : paths){
            this.paths.put(path.id, path);
        }
    }

    public Path getShortestPath(List<BRTLine> BRTlines, List<RailLine> RailLines, List<BusLineNew> busLines){
        FindPath findPath = new FindPath(BRTlines, RailLines, busLines);
        List<Path> paths = findPath.getPath1(this);
        paths.addAll(findPath.getPath2(this));
        paths.addAll(findPath.getPath3(this));
        paths.addAll(findPath.getPath4(this));
        paths.addAll(findPath.getPath5(this));

        Path p = null;
        double minLength = 99999999999.0;
        for(Path path : paths){
            double length = path.getPathWeiLength();
            if(length<minLength){
                p = path;
                minLength = length;
            }
        }
        return p;
    }

}
