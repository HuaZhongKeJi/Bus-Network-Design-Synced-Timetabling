package com.qz.baseClass.Passenger;

import com.qz.baseClass.Line.BRTLine;
import com.qz.baseClass.Line.BaseLine;
import com.qz.baseClass.Line.BusLineNew;
import com.qz.baseClass.Line.RailLine;
import com.qz.baseClass.Link.Link;
import com.qz.baseClass.Node.Node;

import java.util.*;

public class FindPath {

    List<BRTLine> BRTlines;
    List<RailLine> RailLines;
    List<BusLineNew> busLines;

    Map<Integer,Double> minPath;

    public FindPath(List<BRTLine> BRTlines, List<RailLine> RailLines, List<BusLineNew> busLines) {
        this.BRTlines = BRTlines;
        this.RailLines = RailLines;
        this.busLines = busLines;
        minPath = new HashMap<>();
    }

    //加自行车，最多换乘1次，否则不可达

    //寻找直达线路
    public List<Path> getPath1(OD od){
        minPath.put(od.id,999999.0);
        List<Node> boardingNodes = new ArrayList<>();
        List<Node> alightingNodes = new ArrayList<>();
        int startNodeId = od.start.id;
        int endNodeId = od.end.id;
        if(od.start.type<4){
            boardingNodes.add(od.start);
        }
        for(Node node : od.start.toNodes()){
            if(node.type<4&& Link.getLink(startNodeId,node.id).type==5){
                boardingNodes.add(node);
            }
        }

        if(od.end.type<4){
            alightingNodes.add(od.end);
        }
        for(Node node : od.end.fromNodes()){
            if(node.type<4&& Link.getLink(node.id,endNodeId).type==5){
                alightingNodes.add(node);
            }
        }

        List<BaseLine> lines = new ArrayList<>();
        lines.addAll(BRTlines);
        lines.addAll(RailLines);
        lines.addAll(busLines);

        List<Path> paths = new ArrayList<>();

        double minLen = 99999;

        for(BaseLine line : lines){
            for(Node ns:boardingNodes){
                for(Node ne:alightingNodes){
                    if(line.busNode.contains(ns)&&line.busNode.contains(ne)){
                        List<Link> links = new ArrayList<>(Collections.emptyList());
                        Map<Integer,Integer> linkLine = new HashMap<>();

                        int linkStart=-1;
                        int linkEnd=-1;
                        for(int i=0;i<line.links.size();i++){
                            Link link = line.links.get(i);
                            if(link.getStartNode().id == ns.id){
                                linkStart = i;
                            }
                            if(link.getEndNode().id == ne.id){
                                linkEnd = i;
                            }
                            if(linkStart!=-1&&linkEnd!=-1&&linkStart<=linkEnd){
                                break;
                            }
                        }

                        for(int i=linkStart;i<=linkEnd;i++){
                            Link link = line.links.get(i);
                            if(i==linkStart&&link.getStartNode().id!=startNodeId){
                                Link tmL = Link.getLink(startNodeId,link.getStartNode().id);
                                links.add(tmL);
                                linkLine.put(tmL.id,-1);
                            }
                            links.add(link);
                            linkLine.put(link.id,line.id);
                        }

                        if(ne.id!=endNodeId){
                            links.add(Link.getLink(ne.id,endNodeId));
                            linkLine.put(Link.getLink(ne.id,endNodeId).id,-1);
                        }

                        Path path = new Path(Path.tmId,od.start,od.end,links,linkLine);
                        if(path.getPathWeiLength()<minLen){
                            minLen = path.getPathWeiLength();
                        }
                        Path.tmId ++;
                        paths.add(path);
                    }
                }
            }
        }

        if(minPath.get(od.id)>minLen){
            minPath.put(od.id, minLen);
        }
        minLen = minPath.get(od.id);

        List<Path> delPath = new ArrayList<>();
        for(Path path : paths){
            if(path.getPathWeiLength()>minLen*1.5){
                delPath.add(path);
            }
        }
        paths.removeAll(delPath);
        return paths;
    }

    //寻找共享单车线路
    public List<Path> getPath2(OD od){
        List<Node> boardingNodes = new ArrayList<>();
        List<Node> alightingNodes = new ArrayList<>();
        int startNodeId = od.start.id;
        int endNodeId = od.end.id;
        if(od.start.type==4){
            boardingNodes.add(od.start);
        }
        for(Node node : od.start.toNodes()){
            if(node.type==4&& Link.getLink(startNodeId,node.id).type==5){
                boardingNodes.add(node);
            }
        }

        if(od.end.type==4){
            alightingNodes.add(od.end);
        }
        for(Node node : od.end.fromNodes()){
            if(node.type==4&& Link.getLink(node.id,endNodeId).type==5){
                alightingNodes.add(node);
            }
        }

        List<Path> paths = new ArrayList<>();
        double minLen = 99999;

        for(Node ns:boardingNodes){
            for(Node ne:alightingNodes){
                Link link = Link.getLink(ns.id,ne.id);
                if(link!=null&&link.type==4){
                    List<Link> links = new ArrayList<>(Collections.emptyList());
                    Map<Integer,Integer> linkLine = new HashMap<>();

                    if(link.getStartNode().id!=startNodeId){
                        Link tmL = Link.getLink(startNodeId,link.getStartNode().id);
                        links.add(tmL);
                        linkLine.put(tmL.id,-1);
                    }

                    links.add(link);
                    linkLine.put(link.id,-1);

                    if(ne.id!=endNodeId){
                        links.add(Link.getLink(ne.id,endNodeId));
                        linkLine.put(Link.getLink(ne.id,endNodeId).id,-1);
                    }

                    Path path = new Path(Path.tmId,od.start,od.end,links,linkLine);
                    if(path.getPathWeiLength()<minLen){
                        minLen = path.getPathWeiLength();
                    }
                    Path.tmId ++;
                    paths.add(path);
                }
            }
        }

        if(minPath.get(od.id)>minLen){
            minPath.put(od.id, minLen);
        }
        minLen = minPath.get(od.id);

        List<Path> delPath = new ArrayList<>();
        for(Path path : paths){
            if(path.getPathWeiLength()>minLen*1.5){
                delPath.add(path);
            }
        }
        paths.removeAll(delPath);
        return paths;
    }

    //一次换成
    public List<Path> getPath3(OD od){
        List<Node> boardingNodes = new ArrayList<>();
        List<Node> alightingNodes = new ArrayList<>();
        int startNodeId = od.start.id;
        int endNodeId = od.end.id;
        if(od.start.type<4){
            boardingNodes.add(od.start);
        }
        for(Node node : od.start.toNodes()){
            if(node.type<4&& Link.getLink(startNodeId,node.id).type==5){
                boardingNodes.add(node);
            }
        }

        if(od.end.type<4){
            alightingNodes.add(od.end);
        }
        for(Node node : od.end.fromNodes()){
            if(node.type<4&& Link.getLink(node.id,endNodeId).type==5){
                alightingNodes.add(node);
            }
        }

        List<BaseLine> lines = new ArrayList<>();
        lines.addAll(BRTlines);
        lines.addAll(RailLines);
        lines.addAll(busLines);

        List<Path> paths = new ArrayList<>();

        Map<Integer,List<Node>> transferPoint1 = new HashMap<>();//lineid,站点list,第一个站点就是上车站点
        Map<Integer,List<Node>> transferPoint2 = new HashMap<>();

        for(Node ns:boardingNodes){
            for(BaseLine line : lines){
                if(line.busNode.contains(ns)){
                    List<Node> nodes = new ArrayList<>();
                    nodes.addAll(line.busNode);
                    nodes.remove(ns);
                    nodes.add(0,ns);
                    transferPoint1.put(line.id,nodes);
                }
            }
        }

        for(Node ne:alightingNodes){
            for(BaseLine line : lines){
                if(line.busNode.contains(ne)){
                    List<Node> nodes = new ArrayList<>();
                    nodes.addAll(line.busNode);
                    nodes.remove(ne);
                    nodes.add(0,ne);
                    transferPoint2.put(line.id,nodes);
                }
            }
        }

        List<String> transferNode = new ArrayList<>();//lin1Id//lin2Id//transN1//TransN2
        for (Map.Entry<Integer, List<Node>> entry1 : transferPoint1.entrySet()) {
            Integer line1Id = entry1.getKey();
            List<Node> line1Nodes = entry1.getValue();
            // 处理每个键值对的逻辑
            for (Map.Entry<Integer, List<Node>> entry2 : transferPoint2.entrySet()) {
                Integer line2Id = entry2.getKey();
                List<Node> line2Nodes = entry2.getValue();
                // 处理每个键值对的逻辑
                if(!Objects.equals(line1Id, line2Id)){
                    for(int n1N=1;n1N<line1Nodes.size();n1N++){
                        Node n1 = line1Nodes.get(n1N);
                        for(int n2N=1;n2N<line2Nodes.size();n2N++){
                            Node n2 = line2Nodes.get(n2N);
                            if(n1.toNodes().contains(n2)&&Link.getLink(n1.id,n2.id).type==5){
                                String s = line1Id+"//"+line2Id+"//"+n1.id+"//"+n2.id;
                                transferNode.add(s);
                            }
                        }
                    }
                }
            }
        }

        double minLen = 99999;
        for(String s : transferNode){
            String[] tms = s.split("//");
            int Line1Id = Integer.parseInt(tms[0]);
            int Line2Id = Integer.parseInt(tms[1]);
            int Node1Id = Integer.parseInt(tms[2]);
            int Node2Id = Integer.parseInt(tms[3]);

            List<Link> links = new ArrayList<>(Collections.emptyList());
            Map<Integer,Integer> linkLine = new HashMap<>();

            Node line1SNode = transferPoint1.get(Line1Id).get(0);
            Node line2SNode = transferPoint2.get(Line2Id).get(0);

            if(line1SNode.id!=startNodeId){
                Link l1 = Link.getLink(startNodeId,transferPoint1.get(Line1Id).get(0).id);
                links.add(l1);
                linkLine.put(l1.id,-1);
            }

            BaseLine line1 = null;
            BaseLine line2 = null;
            for(BaseLine line : lines){
                if(line.id==Line1Id){
                    line1 = line;
                }else if(line.id==Line2Id){
                    line2 = line;
                }
            }

            int linkStart=-1;
            int linkEnd=-1;
            for(int i=0;i<line1.links.size();i++){
                Link link = line1.links.get(i);
                if(link.getStartNode().id == line1SNode.id){
                    linkStart = i;
                }
                if(link.getEndNode().id == Node1Id){
                    linkEnd = i;
                }
                if(linkStart!=-1&&linkEnd!=-1&&linkStart<=linkEnd){
                    break;
                }
            }

            for(int i=linkStart;i<=linkEnd;i++){
                Link link = line1.links.get(i);
                links.add(link);
                linkLine.put(link.id,Line1Id);
            }

            if(Node1Id!=Node2Id){
                Link link = Link.getLink(Node1Id,Node2Id);
                links.add(link);
                linkLine.put(link.id,-1);
            }

            linkStart=-1;
            linkEnd=-1;
            for(int i=0;i<line2.links.size();i++){
                Link link = line2.links.get(i);
                if(link.getStartNode().id == Node2Id){
                    linkStart = i;
                }
                if(link.getEndNode().id == line2SNode.id){
                    linkEnd = i;
                }
                if(linkStart!=-1&&linkEnd!=-1&&linkStart<=linkEnd){
                    break;
                }
            }

            int endNodeid = 0;
            for(int i=linkStart;i<=linkEnd;i++){
                Link link = line2.links.get(i);
                endNodeid = link.getEndNode().id;
                links.add(link);
                linkLine.put(link.id,Line2Id);
            }

            if(endNodeid!=endNodeId){
                links.add(Link.getLink(endNodeid,endNodeId));
                linkLine.put(Link.getLink(endNodeid,endNodeId).id,-1);
            }

            Path path = new Path(Path.tmId,od.start,od.end,links,linkLine);
            if(path.getPathWeiLength()<minLen){
                minLen = path.getPathWeiLength();
            }
            Path.tmId ++;
            paths.add(path);
        }

        if(minPath.get(od.id)>minLen){
            minPath.put(od.id, minLen);
        }
        minLen = minPath.get(od.id);

        List<Path> delPath = new ArrayList<>();
        for(Path path : paths){
            if(path.getPathWeiLength()>minLen*1.5){
                delPath.add(path);
            }
        }
        paths.removeAll(delPath);
        return paths;
    }

    //bike+一次换成
    public List<Path> getPath4(OD od){
        List<Node> boardingNodes = new ArrayList<>();
        List<Node> alightingNodes = new ArrayList<>();
        int startNodeId = od.start.id;
        int endNodeId = od.end.id;
        if(od.start.type==4){
            boardingNodes.add(od.start);
        }
        for(Node node : od.start.toNodes()){
            if(node.type==4&& Link.getLink(startNodeId,node.id).type==5){
                boardingNodes.add(node);
            }
        }

        if(od.end.type<4){
            alightingNodes.add(od.end);
        }
        for(Node node : od.end.fromNodes()){
            if(node.type<4&& Link.getLink(node.id,endNodeId).type==5){
                alightingNodes.add(node);
            }
        }

        List<String> bikingToNode = new ArrayList<>();//出发bikeid//到达bikeid//公交站点id
        for(Node ns:boardingNodes){
            for(Node toBikeNode:ns.toNodes()){
                if(toBikeNode.type==4){
                    for(Node alightingNode:toBikeNode.toNodes()){
                        if(alightingNode.type<=4){
                            String s = ns.id+"//"+toBikeNode.id+"//"+alightingNode.id;
                            bikingToNode.add(s);
                        }
                    }
                }
            }
        }

        List<BaseLine> lines = new ArrayList<>();
        lines.addAll(BRTlines);
        lines.addAll(RailLines);
        lines.addAll(busLines);

        List<Path> paths = new ArrayList<>();

        Map<String,List<Node>> transferPoint1 = new HashMap<>();//lineid//bikingToNodeSeq,站点list,第一个站点就是上车站点
        Map<Integer,List<Node>> transferPoint2 = new HashMap<>();

        int seq = 0;
        for(String nodeIds:bikingToNode){
            String[] nodeids = nodeIds.split("//");
            int Node1Id = Integer.parseInt(nodeids[2]);
            Node ns = Node.getNode(Node1Id);
            for(BaseLine line : lines){
                if(line.busNode.contains(ns)){
                    List<Node> nodes = new ArrayList<>();
                    nodes.addAll(line.busNode);
                    nodes.remove(ns);
                    nodes.add(0,ns);
                    transferPoint1.put(line.id+"//"+seq,nodes);
                }
            }
            seq++;
        }

        for(Node ne:alightingNodes){
            for(BaseLine line : lines){
                if(line.busNode.contains(ne)){
                    List<Node> nodes = new ArrayList<>();
                    nodes.addAll(line.busNode);
                    nodes.remove(ne);
                    nodes.add(0,ne);
                    transferPoint2.put(line.id,nodes);
                }
            }
        }

        List<String> transferNode = new ArrayList<>();//ts//lin2Id//transN1//TransN2
        for (Map.Entry<String, List<Node>> entry1 : transferPoint1.entrySet()) {
            String ts = entry1.getKey();
            Integer line1Id = Integer.parseInt(ts.split("//")[0]);
            List<Node> line1Nodes = entry1.getValue();
            // 处理每个键值对的逻辑
            for (Map.Entry<Integer, List<Node>> entry2 : transferPoint2.entrySet()) {
                Integer line2Id = entry2.getKey();
                List<Node> line2Nodes = entry2.getValue();
                // 处理每个键值对的逻辑
                if(!Objects.equals(line1Id, line2Id)){
                    for(int n1N=1;n1N<line1Nodes.size();n1N++){
                        Node n1 = line1Nodes.get(n1N);
                        for(int n2N=1;n2N<line2Nodes.size();n2N++){
                            Node n2 = line2Nodes.get(n2N);
                            if(n1.toNodes().contains(n2)&&Link.getLink(n1.id,n2.id).type==5){
                                String s = ts+"//"+line2Id+"//"+n1.id+"//"+n2.id;
                                transferNode.add(s);
                            }
                        }
                    }
                }
            }
        }

        double minLen = 99999;
        for(String s : transferNode){
            String[] tms = s.split("//");
            int Line1Id = Integer.parseInt(tms[0]);
            int bikeNodeSeq = Integer.parseInt(tms[1]);
            int Line2Id = Integer.parseInt(tms[2]);
            int Node1Id = Integer.parseInt(tms[3]);
            int Node2Id = Integer.parseInt(tms[4]);

            String ts = bikingToNode.get(bikeNodeSeq);//出发bikeid//到达bikeid//公交站点id
            String[] tsArray = ts.split("//");
            int startBikeNodeId = Integer.parseInt(tsArray[0]);
            int endBikeNodeId = Integer.parseInt(tsArray[1]);
            int busNodeId = Integer.parseInt(tsArray[2]);

            List<Link> links = new ArrayList<>(Collections.emptyList());
            Map<Integer,Integer> linkLine = new HashMap<>();

            Node line1SNode = transferPoint1.get(Line1Id+"//"+bikeNodeSeq).get(0);
            Node line2ENode = transferPoint2.get(Line2Id).get(0);

            if(startBikeNodeId!=startNodeId){
                Link l1 = Link.getLink(startNodeId,startBikeNodeId);
                links.add(l1);
                linkLine.put(l1.id,-1);
            }

            Link l1 = Link.getLink(startBikeNodeId,endBikeNodeId);
            links.add(l1);
            linkLine.put(l1.id,-1);

            Link l2 = Link.getLink(endBikeNodeId,busNodeId);
            links.add(l2);
            linkLine.put(l2.id,-1);

            BaseLine line1 = null;
            BaseLine line2 = null;
            for(BaseLine line : lines){
                if(line.id==Line1Id){
                    line1 = line;
                }else if(line.id==Line2Id){
                    line2 = line;
                }
            }

            int linkStart=-1;
            int linkEnd=-1;
            for(int i=0;i<line1.links.size();i++){
                Link link = line1.links.get(i);
                if(link.getStartNode().id == line1SNode.id){
                    linkStart = i;
                }
                if(link.getEndNode().id == Node1Id){
                    linkEnd = i;
                }
                if(linkStart!=-1&&linkEnd!=-1&&linkStart<=linkEnd){
                    break;
                }
            }

            for(int i=linkStart;i<=linkEnd;i++){
                Link link = line1.links.get(i);
                links.add(link);
                linkLine.put(link.id,Line1Id);
            }

            if(Node1Id!=Node2Id){
                Link link = Link.getLink(Node1Id,Node2Id);
                links.add(link);
                linkLine.put(link.id,-1);
            }

            linkStart=-1;
            linkEnd=-1;
            for(int i=0;i<line2.links.size();i++){
                Link link = line2.links.get(i);
                if(link.getStartNode().id == Node2Id){
                    linkStart = i;
                }
                if(link.getEndNode().id == line2ENode.id){
                    linkEnd = i;
                }
                if(linkStart!=-1&&linkEnd!=-1&&linkStart<=linkEnd){
                    break;
                }
            }

            int endNodeid = 0;
            for(int i=linkStart;i<=linkEnd;i++){
                Link link = line2.links.get(i);
                endNodeid = link.getEndNode().id;
                links.add(link);
                linkLine.put(link.id,Line2Id);
            }

            if(endNodeid!=endNodeId){
                links.add(Link.getLink(endNodeid,endNodeId));
                linkLine.put(Link.getLink(endNodeid,endNodeId).id,-1);
            }

            Path path = new Path(Path.tmId,od.start,od.end,links,linkLine);
            if(path.getPathWeiLength()<minLen){
                minLen = path.getPathWeiLength();
            }
            Path.tmId ++;
            paths.add(path);
        }

        if(minPath.get(od.id)>minLen){
            minPath.put(od.id, minLen);
        }
        minLen = minPath.get(od.id);

        List<Path> delPath = new ArrayList<>();
        for(Path path : paths){
            if(path.getPathWeiLength()>minLen*1.5){
                delPath.add(path);
            }
        }
        paths.removeAll(delPath);
        return paths;
    }

    //一次换成+bike
    public List<Path> getPath5(OD od){
        List<Node> boardingNodes = new ArrayList<>();
        List<Node> alightingNodes = new ArrayList<>();
        int startNodeId = od.start.id;
        int endNodeId = od.end.id;
        if(od.start.type<4){
            boardingNodes.add(od.start);
        }
        for(Node node : od.start.toNodes()){
            if(node.type<4&& Link.getLink(startNodeId,node.id).type==5){
                boardingNodes.add(node);
            }
        }

        if(od.end.type==4){
            alightingNodes.add(od.end);
        }
        for(Node node : od.end.fromNodes()){
            if(node.type==4&& Link.getLink(node.id,endNodeId).type==5){
                alightingNodes.add(node);
            }
        }

        List<String> bikingToNode = new ArrayList<>();//到达bikeid//出发bikeid//下车公交站点id
        for(Node ns:alightingNodes){
            for(Node toBikeNode:ns.toNodes()){
                if(toBikeNode.type==4){
                    for(Node alightingNode:toBikeNode.fromNodes()){
                        if(alightingNode.type<=4){
                            String s = ns.id+"//"+toBikeNode.id+"//"+alightingNode.id;
                            bikingToNode.add(s);
                        }
                    }
                }
            }
        }

        List<BaseLine> lines = new ArrayList<>();
        lines.addAll(BRTlines);
        lines.addAll(RailLines);
        lines.addAll(busLines);

        List<Path> paths = new ArrayList<>();

        Map<Integer,List<Node>> transferPoint1 = new HashMap<>();
        Map<String,List<Node>> transferPoint2 = new HashMap<>();//lineid//bikingToNodeSeq,站点list,第一个站点就是上车站点

        for(Node ns:boardingNodes){
            for(BaseLine line : lines){
                if(line.busNode.contains(ns)){
                    List<Node> nodes = new ArrayList<>();
                    nodes.addAll(line.busNode);
                    nodes.remove(ns);
                    nodes.add(0,ns);
                    transferPoint1.put(line.id,nodes);
                }
            }
        }

        int seq = 0;
        for(String nodeIds:bikingToNode){
            String[] nodeids = nodeIds.split("//");
            int Node1Id = Integer.parseInt(nodeids[2]);
            Node ns = Node.getNode(Node1Id);
            for(BaseLine line : lines){
                if(line.busNode.contains(ns)){
                    List<Node> nodes = new ArrayList<>();
                    nodes.addAll(line.busNode);
                    nodes.remove(ns);
                    nodes.add(0,ns);
                    transferPoint2.put(line.id+"//"+seq,nodes);
                }
            }
            seq++;
        }


        List<String> transferNode = new ArrayList<>();//ts//lin2Id//transN1//TransN2
        for (Map.Entry<Integer, List<Node>> entry1 : transferPoint1.entrySet()) {
            Integer line1Id = entry1.getKey();
            List<Node> line1Nodes = entry1.getValue();
            // 处理每个键值对的逻辑
            for (Map.Entry<String, List<Node>> entry2 : transferPoint2.entrySet()) {
                String ts = entry2.getKey();
                Integer line2Id  = Integer.parseInt(ts.split("//")[0]);
                List<Node> line2Nodes = entry2.getValue();
                // 处理每个键值对的逻辑
                if(!Objects.equals(line1Id, line2Id)){
                    for(int n1N=1;n1N<line1Nodes.size();n1N++){
                        Node n1 = line1Nodes.get(n1N);
                        for(int n2N=1;n2N<line2Nodes.size();n2N++){
                            Node n2 = line2Nodes.get(n2N);
                            if(n1.toNodes().contains(n2)&&Link.getLink(n1.id,n2.id).type==5){
                                String s = line1Id+"//"+ts+"//"+n1.id+"//"+n2.id;
                                transferNode.add(s);
                            }
                        }
                    }
                }
            }
        }

        double minLen = 99999;
        for(String s : transferNode){
            String[] tms = s.split("//");
            int Line1Id = Integer.parseInt(tms[0]);
            int Line2Id = Integer.parseInt(tms[1]);
            int bikeNodeSeq = Integer.parseInt(tms[2]);
            int Node1Id = Integer.parseInt(tms[3]);
            int Node2Id = Integer.parseInt(tms[4]);

            String ts = bikingToNode.get(bikeNodeSeq);//到达bikeid//出发bikeid//下车公交站点id
            String[] tsArray = ts.split("//");
            int startBikeNodeId = Integer.parseInt(tsArray[0]);
            int endBikeNodeId = Integer.parseInt(tsArray[1]);
            int busNodeId = Integer.parseInt(tsArray[2]);

            List<Link> links = new ArrayList<>(Collections.emptyList());
            Map<Integer,Integer> linkLine = new HashMap<>();

            Node line1SNode = transferPoint1.get(Line1Id).get(0);
            Node line2ENode = transferPoint2.get(Line2Id+"//"+bikeNodeSeq).get(0);

            if(line1SNode.id!=startNodeId){
                Link l1 = Link.getLink(startNodeId,transferPoint1.get(Line1Id).get(0).id);
                links.add(l1);
                linkLine.put(l1.id,-1);
            }

            BaseLine line1 = null;
            BaseLine line2 = null;
            for(BaseLine line : lines){
                if(line.id==Line1Id){
                    line1 = line;
                }else if(line.id==Line2Id){
                    line2 = line;
                }
            }

            /*Node line1SNode = transferPoint1.get(Line1Id+"//"+bikeNodeSeq).get(0);

            if(startBikeNodeId!=startNodeId){
                Link l1 = Link.getLink(startNodeId,startBikeNodeId);
                links.add(l1);
                linkLine.put(l1.id,-1);
            }

            Link l1 = Link.getLink(startBikeNodeId,endBikeNodeId);
            links.add(l1);
            linkLine.put(l1.id,-1);

            Link l2 = Link.getLink(endBikeNodeId,busNodeId);
            links.add(l2);
            linkLine.put(l2.id,-1);*/



            int linkStart=-1;
            int linkEnd=-1;
            for(int i=0;i<line1.links.size();i++){
                Link link = line1.links.get(i);
                if(link.getStartNode().id == line1SNode.id){
                    linkStart = i;
                }
                if(link.getEndNode().id == Node1Id){
                    linkEnd = i;
                }
                if(linkStart!=-1&&linkEnd!=-1&&linkStart<=linkEnd){
                    break;
                }
            }

            for(int i=linkStart;i<=linkEnd;i++){
                Link link = line1.links.get(i);
                links.add(link);
                linkLine.put(link.id,Line1Id);
            }

            if(Node1Id!=Node2Id){
                Link link = Link.getLink(Node1Id,Node2Id);
                links.add(link);
                linkLine.put(link.id,-1);
            }

            linkStart=-1;
            linkEnd=-1;
            for(int i=0;i<line2.links.size();i++){
                Link link = line2.links.get(i);
                if(link.getStartNode().id == Node2Id){
                    linkStart = i;
                }
                if(link.getEndNode().id == line2ENode.id){
                    linkEnd = i;
                }
                if(linkStart!=-1&&linkEnd!=-1&&linkStart<=linkEnd){
                    break;
                }
            }

            int endNodeidTm = 0;
            for(int i=linkStart;i<=linkEnd;i++){
                Link link = line2.links.get(i);
                endNodeidTm = link.getEndNode().id;
                links.add(link);
                linkLine.put(link.id,Line2Id);
            }


            Link l1 = Link.getLink(endNodeidTm,endBikeNodeId);
            links.add(l1);
            linkLine.put(l1.id,-1);

            Link l2 = Link.getLink(endBikeNodeId,startBikeNodeId);
            links.add(l2);
            linkLine.put(l2.id,-1);

            if(startBikeNodeId!=endNodeId){
                links.add(Link.getLink(startBikeNodeId,endNodeId));
                linkLine.put(Link.getLink(startBikeNodeId,endNodeId).id,-1);
            }

            Path path = new Path(Path.tmId,od.start,od.end,links,linkLine);
            if(path.getPathWeiLength()<minLen){
                minLen = path.getPathWeiLength();
            }
            Path.tmId ++;
            paths.add(path);
        }

        if(minPath.get(od.id)>minLen){
            minPath.put(od.id, minLen);
        }
        minLen = minPath.get(od.id);

        List<Path> delPath = new ArrayList<>();
        for(Path path : paths){
            if(path.getPathWeiLength()>minLen*1.5){
                delPath.add(path);
            }
        }
        paths.removeAll(delPath);
        return paths;
    }
}
