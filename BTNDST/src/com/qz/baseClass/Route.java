package com.qz.baseClass;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public int pointNum;
    private int startPoint;
    private int endPoint;



    public int[][] routeMat;
    private List<Integer> pointList;
    private String routeString;
    public Route(int point_num,int start_point,int end_point,int[][] route){
        pointNum = point_num;
        startPoint = start_point;
        endPoint = end_point;

        routeMat = new int[point_num][point_num];
        for(int i=0;i<point_num;i++){
            for(int j=0;j<point_num;j++){
                routeMat[i][j] = route[i][j];
            }
        }

        pointList = new ArrayList<>();
        pointList.add(startPoint);
        //遍历route生成route字符串
        int tmpPoint = startPoint;
        routeString = "|"+Integer.toString(startPoint)+"|";

        while(tmpPoint!=endPoint){
            for(int j=0;j<pointNum;j++){
                if(routeMat[tmpPoint][j]==1 && !routeString.contains("|"+j+"|")){
                    routeMat[j][tmpPoint]=1;
                    tmpPoint = j;
                    pointList.add(j);

                    routeString += (j+"|");
                }
            }
        }

    }

    public List<Integer> getPointList() {
        return pointList;
    }

    public int getStartPoint(){
        return startPoint;
    }

    public int getEndPoint() {
        return endPoint;
    }

    public String getRouteString(){
        return routeString;
    }

    public int[][] getRoute() {
        return routeMat;
    }


}
