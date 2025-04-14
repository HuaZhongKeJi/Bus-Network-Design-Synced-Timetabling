package com.qz.tool;

import com.qz.PBGA.Chromosome;
import com.qz.baseClass.Passenger.Passenger;

import java.util.List;

public class MatOperate {
    static public int[][] MatMul(int[][] a,int[][] b){
        int aRowNum = a.length;
        int aColNum = a[0].length;
        int bRowNum = b.length;
        int bColNum = b[0].length;
        if(aColNum!=bRowNum){
            return null;
        }

        int[][] c=new int[aRowNum][bColNum];

        for(int i=0;i<aRowNum;i++){
            for(int j=0;j<bColNum;j++){
                int tmpS = 0;
                for(int k=0;k<aColNum;k++){
                    tmpS += a[i][k]*b[k][j];
                }
                c[i][j] = tmpS;
            }
        }

        return c;
    }


    static public int[] MatMul(int[] a,int[][] b){
        int aColNum = a.length;
        int bRowNum = b.length;
        int bColNum = b[0].length;
        if(aColNum!=bRowNum){
            return null;
        }

        int[] c=new int[bColNum];

        for(int j=0;j<bColNum;j++){
            int tmpS = 0;
            for(int k=0;k<aColNum;k++){
                tmpS += a[k]*b[k][j];
            }
            c[j] = tmpS;
        }

        return c;
    }

    static public void MatMul(int[] a,int[][] b,int [] res){
        int aColNum = a.length;
        int bRowNum = b.length;
        int bColNum = b[0].length;
        if(aColNum!=bRowNum){
            return ;
        }


        for(int j=0;j<bColNum;j++){
            int tmpS = 0;
            for(int k=0;k<aColNum;k++){
                tmpS += a[k]*b[k][j];
            }
            res[j] = tmpS;
        }

    }

    static public int[][] ZeroMat(int i,int j){
        int[][] tmMat = new int[i][j];
        for(int ti=0;ti<i;ti++){
            for(int tj=0;tj<j;tj++){
                tmMat[ti][tj]=0;
            }
        }
        return tmMat;
    }

    static public int[][] NumMat(int i,int j,int num){
        int[][] tmMat = new int[i][j];
        for(int ti=0;ti<i;ti++){
            for(int tj=0;tj<j;tj++){
                tmMat[ti][tj]=num;
            }
        }
        return tmMat;
    }

    static public void ZeroMat(int i,int j,int[][] res){
        for(int ti=0;ti<i;ti++){
            for(int tj=0;tj<j;tj++){
                res[ti][tj]=0;
            }
        }
    }

    static public int[][] IMat(int i){
        int[][] tmMat = new int[i][i];
        for(int ti=0;ti<i;ti++){
            for(int tj=0;tj<i;tj++){
                if(ti==tj){
                    tmMat[ti][tj]=1;
                }else{
                    tmMat[ti][tj]=0;
                }

            }
        }
        return tmMat;
    }

    static public int[][] OneMat(int i,int j){
        int[][] tmMat = new int[i][j];
        for(int ti=0;ti<i;ti++){
            for(int tj=0;tj<j;tj++){
                tmMat[ti][tj]=1;
            }
        }
        return tmMat;
    }

    public static void QuickSortPbga(List<Double> solutionSufficiency, int left, int right, List<Chromosome> solutionList, List<List<Double>> solutionPartSufficiency, List<List<Float>> solutionFrq, List<List<Passenger>> unSerPassenger) {
        //如果left等于right，即数组只有一个元素，直接返回
        if(left>=right) {
            return;
        }
        //设置最左边的元素为基准值
        Double key=solutionSufficiency.get(left);
        Chromosome key2 = solutionList.get(left);
        List<Double> key3 = solutionPartSufficiency.get(left);
        List<Float> key4 = solutionFrq.get(left);
        List<Passenger> key5 = unSerPassenger.get(left);

        //数组中比key小的放在左边，比key大的放在右边，key值下标为i
        int i=left;
        int j=right;
        while(i<j){
            //j向左移，直到遇到比key小的值
            while(solutionSufficiency.get(j)>=key && i<j){
                j--;
            }
            //i向右移，直到遇到比key大的值
            while(solutionSufficiency.get(i)<=key && i<j){
                i++;
            }
            //i和j指向的元素交换
            if(i<j){
                Double temp=solutionSufficiency.get(i);
                solutionSufficiency.set(i,solutionSufficiency.get(j));
                solutionSufficiency.set(j,temp);

                Chromosome temp2 = solutionList.get(i);
                solutionList.set(i,solutionList.get(j));
                solutionList.set(j,temp2);

                List<Double> temp3 = solutionPartSufficiency.get(i);
                solutionPartSufficiency.set(i,solutionPartSufficiency.get(j));
                solutionPartSufficiency.set(j,temp3);

                List<Float> temp4 = solutionFrq.get(i);
                solutionFrq.set(i,solutionFrq.get(j));
                solutionFrq.set(j,temp4);

                List<Passenger> temp5 = unSerPassenger.get(i);
                unSerPassenger.set(i,unSerPassenger.get(j));
                unSerPassenger.set(j,temp5);

            }
        }
        solutionSufficiency.set(left,solutionSufficiency.get(i));
        solutionSufficiency.set(i,key);

        solutionList.set(left,solutionList.get(i));
        solutionList.set(i,key2);

        solutionPartSufficiency.set(left,solutionPartSufficiency.get(i));
        solutionPartSufficiency.set(i,key3);

        solutionFrq.set(left,solutionFrq.get(i));
        solutionFrq.set(i,key4);

        unSerPassenger.set(left,unSerPassenger.get(i));
        unSerPassenger.set(i,key5);



        QuickSortPbga(solutionSufficiency,left,i-1,solutionList,solutionPartSufficiency,solutionFrq,unSerPassenger);
        QuickSortPbga(solutionSufficiency,i+1,right,solutionList,solutionPartSufficiency,solutionFrq,unSerPassenger);
    }


    public static void QuickSortNew(List<Double> solutionSufficiency, int left, int right, List<Chromosome> solutionList, List<List<Double>> solutionPartSufficiency, List<List<Float>> solutionFrq, List<List<Integer>> unSerPassenger) {
        //如果left等于right，即数组只有一个元素，直接返回
        if(left>=right) {
            return;
        }
        //设置最左边的元素为基准值
        Double key=solutionSufficiency.get(left);
        Chromosome key2 = solutionList.get(left);
        List<Double> key3 = solutionPartSufficiency.get(left);
        List<Float> key4 = solutionFrq.get(left);
        List<Integer> key5 = unSerPassenger.get(left);

        //数组中比key小的放在左边，比key大的放在右边，key值下标为i
        int i=left;
        int j=right;
        while(i<j){
            //j向左移，直到遇到比key小的值
            while(solutionSufficiency.get(j)>=key && i<j){
                j--;
            }
            //i向右移，直到遇到比key大的值
            while(solutionSufficiency.get(i)<=key && i<j){
                i++;
            }
            //i和j指向的元素交换
            if(i<j){
                Double temp=solutionSufficiency.get(i);
                solutionSufficiency.set(i,solutionSufficiency.get(j));
                solutionSufficiency.set(j,temp);

                Chromosome temp2 = solutionList.get(i);
                solutionList.set(i,solutionList.get(j));
                solutionList.set(j,temp2);

                List<Double> temp3 = solutionPartSufficiency.get(i);
                solutionPartSufficiency.set(i,solutionPartSufficiency.get(j));
                solutionPartSufficiency.set(j,temp3);

                List<Float> temp4 = solutionFrq.get(i);
                solutionFrq.set(i,solutionFrq.get(j));
                solutionFrq.set(j,temp4);

                List<Integer> temp5 = unSerPassenger.get(i);
                unSerPassenger.set(i,unSerPassenger.get(j));
                unSerPassenger.set(j,temp5);

            }
        }
        solutionSufficiency.set(left,solutionSufficiency.get(i));
        solutionSufficiency.set(i,key);

        solutionList.set(left,solutionList.get(i));
        solutionList.set(i,key2);

        solutionPartSufficiency.set(left,solutionPartSufficiency.get(i));
        solutionPartSufficiency.set(i,key3);

        solutionFrq.set(left,solutionFrq.get(i));
        solutionFrq.set(i,key4);

        unSerPassenger.set(left,unSerPassenger.get(i));
        unSerPassenger.set(i,key5);



        QuickSortNew(solutionSufficiency,left,i-1,solutionList,solutionPartSufficiency,solutionFrq,unSerPassenger);
        QuickSortNew(solutionSufficiency,i+1,right,solutionList,solutionPartSufficiency,solutionFrq,unSerPassenger);
    }

    public static void QuickSort(List<Double> solutionSufficiency, int left, int right,List<List<int[]>> solutionList,List<List<Double>> solutionPartSufficiency,List<List<Float>> solutionFrq,List<List<Integer>> unSerPassenger) {
        //如果left等于right，即数组只有一个元素，直接返回
        if(left>=right) {
            return;
        }
        //设置最左边的元素为基准值
        Double key=solutionSufficiency.get(left);
        List<int[]> key2 = solutionList.get(left);
        List<Double> key3 = solutionPartSufficiency.get(left);
        List<Float> key4 = solutionFrq.get(left);
        List<Integer> key5 = unSerPassenger.get(left);

        //数组中比key小的放在左边，比key大的放在右边，key值下标为i
        int i=left;
        int j=right;
        while(i<j){
            //j向左移，直到遇到比key小的值
            while(solutionSufficiency.get(j)>=key && i<j){
                j--;
            }
            //i向右移，直到遇到比key大的值
            while(solutionSufficiency.get(i)<=key && i<j){
                i++;
            }
            //i和j指向的元素交换
            if(i<j){
                Double temp=solutionSufficiency.get(i);
                solutionSufficiency.set(i,solutionSufficiency.get(j));
                solutionSufficiency.set(j,temp);

                List<int[]> temp2 = solutionList.get(i);
                solutionList.set(i,solutionList.get(j));
                solutionList.set(j,temp2);

                List<Double> temp3 = solutionPartSufficiency.get(i);
                solutionPartSufficiency.set(i,solutionPartSufficiency.get(j));
                solutionPartSufficiency.set(j,temp3);

                List<Float> temp4 = solutionFrq.get(i);
                solutionFrq.set(i,solutionFrq.get(j));
                solutionFrq.set(j,temp4);

                List<Integer> temp5 = unSerPassenger.get(i);
                unSerPassenger.set(i,unSerPassenger.get(j));
                unSerPassenger.set(j,temp5);

            }
        }
        solutionSufficiency.set(left,solutionSufficiency.get(i));
        solutionSufficiency.set(i,key);

        solutionList.set(left,solutionList.get(i));
        solutionList.set(i,key2);

        solutionPartSufficiency.set(left,solutionPartSufficiency.get(i));
        solutionPartSufficiency.set(i,key3);

        solutionFrq.set(left,solutionFrq.get(i));
        solutionFrq.set(i,key4);

        unSerPassenger.set(left,unSerPassenger.get(i));
        unSerPassenger.set(i,key5);



        QuickSort(solutionSufficiency,left,i-1,solutionList,solutionPartSufficiency,solutionFrq,unSerPassenger);
        QuickSort(solutionSufficiency,i+1,right,solutionList,solutionPartSufficiency,solutionFrq,unSerPassenger);
    }

    public static void QuickSort2(List<Double> solutionSufficiency, int left, int right,List<List<Integer>> solutionList,List<List<Double>> solutionPartSufficiency,List<List<Float>> solutionFrq) {
        //如果left等于right，即数组只有一个元素，直接返回
        if(left>=right) {
            return;
        }
        //设置最左边的元素为基准值
        Double key=solutionSufficiency.get(left);
        List<Integer> key2 = solutionList.get(left);
        List<Double> key3 = solutionPartSufficiency.get(left);
        List<Float> key4 = solutionFrq.get(left);
        //数组中比key小的放在左边，比key大的放在右边，key值下标为i
        int i=left;
        int j=right;
        while(i<j){
            //j向左移，直到遇到比key小的值
            while(solutionSufficiency.get(j)>=key && i<j){
                j--;
            }
            //i向右移，直到遇到比key大的值
            while(solutionSufficiency.get(i)<=key && i<j){
                i++;
            }
            //i和j指向的元素交换
            if(i<j){
                Double temp=solutionSufficiency.get(i);
                solutionSufficiency.set(i,solutionSufficiency.get(j));
                solutionSufficiency.set(j,temp);

                List<Integer> temp2 = solutionList.get(i);
                solutionList.set(i,solutionList.get(j));
                solutionList.set(j,temp2);

                List<Double> temp3 = solutionPartSufficiency.get(i);
                solutionPartSufficiency.set(i,solutionPartSufficiency.get(j));
                solutionPartSufficiency.set(j,temp3);

                List<Float> temp4 = solutionFrq.get(i);
                solutionFrq.set(i,solutionFrq.get(j));
                solutionFrq.set(j,temp4);
            }
        }
        solutionSufficiency.set(left,solutionSufficiency.get(i));
        solutionSufficiency.set(i,key);

        solutionList.set(left,solutionList.get(i));
        solutionList.set(i,key2);

        solutionPartSufficiency.set(left,solutionPartSufficiency.get(i));
        solutionPartSufficiency.set(i,key3);

        solutionFrq.set(left,solutionFrq.get(i));
        solutionFrq.set(i,key4);

        QuickSort2(solutionSufficiency,left,i-1,solutionList,solutionPartSufficiency,solutionFrq);
        QuickSort2(solutionSufficiency,i+1,right,solutionList,solutionPartSufficiency,solutionFrq);
    }
}
