package com.qz.tool;


import com.qz.baseClass.BusLine;
import com.qz.baseClass.PassengerGroup;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ExcelUtil {

    public static boolean readExcel(String path){

        File f=new File(path);
        try {
            BaseInfo.setTrsPunish(15);
            BaseInfo.setLongWalkPunish(30);
            BaseInfo.setShortWalkPunish(10);


            Workbook book=Workbook.getWorkbook(f);//
            Sheet sheet1=book.getSheet("Info"); //基础数据

            BaseInfo.setPointNum(Integer.parseInt(sheet1.getCell(1, 0).getContents()));
            BaseInfo.setShortWalk(Integer.parseInt(sheet1.getCell(1, 1).getContents()));
            BaseInfo.setLongWalk(Integer.parseInt(sheet1.getCell(1, 2).getContents()));
            BaseInfo.setBRTNum(Integer.parseInt(sheet1.getCell(1, 3).getContents()));
            BaseInfo.setBRTFeq(Double.parseDouble(sheet1.getCell(1, 4).getContents()));
            BaseInfo.setPassengerPointNum(Integer.parseInt(sheet1.getCell(1, 5).getContents()));
            BaseInfo.setMinFeq(Double.parseDouble(sheet1.getCell(1, 6).getContents()));
            BaseInfo.setMaxFeq(Double.parseDouble(sheet1.getCell(1, 7).getContents()));
            BaseInfo.setMinLineNum(Integer.parseInt(sheet1.getCell(1, 8).getContents()));
            BaseInfo.setMaxLineNum(Integer.parseInt(sheet1.getCell(1, 9).getContents()));
            BaseInfo.setMinLinePointNum(Integer.parseInt(sheet1.getCell(1, 10).getContents()));
            BaseInfo.setMaxLinePointNum(Integer.parseInt(sheet1.getCell(1, 11).getContents()));
            BaseInfo.setBRTCommonNum(Integer.parseInt(sheet1.getCell(1, 12).getContents()));
            BaseInfo.setBusCapacity(Integer.parseInt(sheet1.getCell(1, 13).getContents()));
            BaseInfo.setBRTCapacity(Integer.parseInt(sheet1.getCell(1, 14).getContents()));
            BaseInfo.setCb(Integer.parseInt(sheet1.getCell(1, 15).getContents()));
            BaseInfo.setCp(Integer.parseInt(sheet1.getCell(1, 16).getContents()));
            BaseInfo.setMaxTravelTime(Integer.parseInt(sheet1.getCell(1, 17).getContents()));
            BaseInfo.setMaxFrequency(Integer.parseInt(sheet1.getCell(1, 18).getContents()));

            Sheet sheet=book.getSheet("drive_time");
            int[][] pointDriveTime = new int[BaseInfo.getPointNum()][BaseInfo.getPointNum()];
            for(int i=0;i<BaseInfo.getPointNum();i++){
                for(int j=0;j<BaseInfo.getPointNum();j++){

                    pointDriveTime[i][j] = Integer.parseInt(sheet.getCell(j, i).getContents());

                }
            }
            BaseInfo.setPointDriveTime(pointDriveTime);

            sheet=book.getSheet("walk_time");
            int[][] pointWalkTime = new int[BaseInfo.getPointNum()][BaseInfo.getPointNum()];
            for(int i=0;i<BaseInfo.getPointNum();i++){
                for(int j=0;j<BaseInfo.getPointNum();j++){

                    pointWalkTime[i][j] = Integer.parseInt(sheet.getCell(j, i).getContents());

                }
            }
            BaseInfo.setPointWalkTime(pointWalkTime);

            sheet=book.getSheet("link_type");
            int[][] linkType = new int[BaseInfo.getPointNum()][BaseInfo.getPointNum()];
            for(int i=0;i<BaseInfo.getPointNum();i++){
                for(int j=0;j<BaseInfo.getPointNum();j++){

                    linkType[i][j] = Integer.parseInt(sheet.getCell(j, i).getContents());

                }
            }
            BaseInfo.setLinkType(linkType);

            sheet=book.getSheet("BRT_common_route");
            int[][] BRT_common_route = new int[BaseInfo.getPointNum()][BaseInfo.getPointNum()];
            for(int i=0;i<BaseInfo.getPointNum();i++){
                for(int j=0;j<BaseInfo.getPointNum();j++){

                    BRT_common_route[i][j] = Integer.parseInt(sheet.getCell(j, i).getContents());

                }
            }
            BaseInfo.setBRTCommonRoute(BRT_common_route);

            /******************************************************************/

            sheet=book.getSheet("BRT_common_route_BijMax");//这里要直接给出最大的发车频率
            float[] BRT_common_route_BijMax = new float[BaseInfo.getBRTCommonNum()];
            for(int i=0;i<BaseInfo.getBRTCommonNum();i++){
                //BRT_common_route_BijMax[i] = Integer.parseInt(sheet.getCell(1, i).getContents());
                BRT_common_route_BijMax[i] = BaseInfo.getMaxFrequency();
            }
            BaseInfo.setBRTCommonRouteBijMax(BRT_common_route_BijMax);




            //初始化BRT线路
            for(int i=1;i<=BaseInfo.getBRTNum();i++){
                sheet=book.getSheet("BRT_route_"+i);

                int[][] tmBRTLine = new int[BaseInfo.getPointNum()][BaseInfo.getPointNum()];
                for(int j=0;j<BaseInfo.getPointNum();j++){
                    for(int k=0;k<BaseInfo.getPointNum();k++){

                        tmBRTLine[j][k] = Integer.parseInt(sheet.getCell(k, j).getContents());

                    }
                }
                int startPoint = Integer.parseInt(sheet1.getCell(1, 18+i).getContents());
                int endPoint = Integer.parseInt(sheet1.getCell(2, 18+i).getContents());
                float frq = Float.parseFloat(sheet1.getCell(3, 18+i).getContents());

                BusLine tmBusLine = new BusLine(BaseInfo.getPointNum(),startPoint,endPoint,tmBRTLine,i,0);
                tmBusLine.setFreq(frq);
                BaseInfo.getBRTLine().add(tmBusLine);
            }

            sheet=book.getSheet("Qk");
            Sheet sheets=book.getSheet("passenger-point");
            Sheet sheet2=book.getSheet("unserviced_punish");
            int tmPassengerGroupId = 0;
            List<PassengerGroup> passengerGroups = new ArrayList<>();
            for(int i=0;i<BaseInfo.getPassengerPointNum()-1;i++){
                for(int j=i+1;j<BaseInfo.getPassengerPointNum();j++){
                    //双倍客流测试BRT专用道***************************************************************************************************
                    int passengerNum = Integer.parseInt(sheet.getCell(j, i).getContents());
                    //双倍客流测试BRT专用道***************************************************************************************************
                    if(passengerNum>0){
                        int[] start_point_dist = new int[BaseInfo.getPointNum()];
                        int[] end_point_dist = new int[BaseInfo.getPointNum()];
                        for(int k=0;k<BaseInfo.getPointNum();k++){
                            start_point_dist[k] = Integer.parseInt(sheets.getCell(k, i).getContents());
                            end_point_dist[k] = Integer.parseInt(sheets.getCell(k, j).getContents());
                        }
                        PassengerGroup passengerGroup = new PassengerGroup(BaseInfo.getPointNum(),start_point_dist,end_point_dist, passengerNum,BaseInfo.getShortWalk(), BaseInfo.getLongWalk(),++tmPassengerGroupId,Integer.parseInt(sheet2.getCell(j, i).getContents()));
                        passengerGroups.add(passengerGroup);
                    }
                }
            }
            passengerGroups.sort(new Comparator<PassengerGroup>() {//按照为满足的惩罚成本排序，方便后面客流分配
                @Override
                public int compare(PassengerGroup p1, PassengerGroup p2) {
                    return p2.getUnservicedPunish() - p1.getUnservicedPunish();
                }
            });

            BaseInfo.setPassengerGroups(passengerGroups);
            //setPointDriveTime

//            for(int i=0;i<sheet.getRows();i++){
//                for(int j=0;j<sheet.getColumns();j++){
//                    Cell cell=sheet.getCell(j, i); //获得单元格
//                    System.out.print(cell.getContents()+" ");
//                }
//                System.out.print("\n");
//            }


        } catch (BiffException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

}




//    public static void main(String[] args) throws Exception {
//        //参数里的5表示有效行数从第5行开始
//        List<StudentInfo> studentInfos = ExcelUtil.parseFromExcel("C:\\Users\\unive\\Desktop\\StudentInfo.xlsx", 5,
//                StudentInfo.class);
//        for (int i = 0; i < studentInfos.size(); i++) {
//            System.err.println(studentInfos.get(i).toString());
//        }
//    }
