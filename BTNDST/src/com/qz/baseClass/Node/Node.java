package com.qz.baseClass.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    public int id;

    /// 1 bus node; 2 BRT node; 3 Rail node; 4 bike node; 5 passenger node;
    public int type;
    List<Node> fromNodes;
    List<Node> toNodes;

    public static List<Node> nodes = new ArrayList<Node>();
    public static List<Node> busNodes = new ArrayList<Node>();
    public static List<Node> BRTNodes = new ArrayList<Node>();
    public static List<Node> RailNodes = new ArrayList<Node>();
    public static List<Node> bikeNodes = new ArrayList<Node>();
    public static List<Node> passengerNodes = new ArrayList<Node>();
    static Map<Integer,Node> idNodes = new HashMap<Integer,Node>();


    public Node(int id, int type) {
        this.id = id;
        this.type = type;
        nodes.add(this);
        fromNodes = new ArrayList<Node>();
        toNodes = new ArrayList<Node>();
        idNodes.put(this.id, this);
        if(type == 1){
            busNodes.add(this);
        }else if(type == 2){
            BRTNodes.add(this);
        }else if(type == 3){
            RailNodes.add(this);
        }else if(type == 4){
            bikeNodes.add(this);
        }else if(type == 5){
            passengerNodes.add(this);
        }
    }

    public List<Node> toNodes() {
        return toNodes;
    }
    public List<Node> fromNodes() {
        return fromNodes;
    }

    public int addFromNode(Node fromNode) {
        fromNodes.add(fromNode);
        return fromNodes.size();
    }
    public int addToNode(Node toNode) {
        toNodes.add(toNode);
        return toNodes.size();
    }

    static public Node getNode(int id) {
        return idNodes.get(id);
    }
}
