package com.foodtime.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


import com.foodtime.dto.GoodsSalesDTO;
import com.foodtime.entity.Orders;
import com.foodtime.mapper.OrderMapper;
import com.foodtime.mapper.UserMapper;
import com.foodtime.service.ReportService;
import com.foodtime.vo.*;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
  private UserMapper userMapper;
    @Autowired
    private WorkspaceServiceImpl workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //先把begin和end每一天查询出来然后装到一个list里面

        List<LocalDate> dateList=new ArrayList<>();

        dateList.add(begin);
        //.plusDays()加一然后进行循环
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList=new ArrayList<>();
        for (LocalDate date: dateList){
            //查询date日期的相对应的营业额：状态为已完成的订单合计
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);


            //select sum(amount) from orders where status=4 and order_time between ? and ?

            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            turnover=turnover==null?0.0:turnover;
            turnoverList.add(turnover);


        }
       /* StringUtils.join(dateList,",");*/
       return TurnoverReportVO
               .builder()
               .dateList(StringUtils.join(dateList,","))
               .turnoverList(StringUtils.join(turnoverList,","))
               .build();


    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //把begin和end everyday查询出来然后放到list里面
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        //.plusDays()加一然后进行循环
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);

        }
        //新增的用户数量  select count(id) from user where create_time <? and create_time > ?

        List<Integer> newUserList=new ArrayList<>();
        //存放每天的总用户数量 select count(id) from user where create_time < ?
List<Integer> totalUserList=new ArrayList<>();
        for (LocalDate date: dateList){
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
       Map map =new HashMap();
       map.put("end",endTime);

       Integer totalUser=userMapper.countByMap(map);

       map.put("begin",beginTime);
       Integer newUser=userMapper.countByMap(map);

     totalUserList.add(totalUser);
     newUserList.add(newUser);

        }

        //存放每天的总用户数量 select count(id) from user where create_time < ?
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //把begin和end everyday查询出来然后放到list里面
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        //.plusDays()加一然后进行循环
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
   //订单完成的数量  select count(id) from orders where order_time <? and order_time > ? and status = 5
        List<Integer> orderCountList=new ArrayList<>();

        //每日有效订单数量  select count(id) from orders where order_time <? and order_time > ? and status = 5
        List< Integer> orderValidList=new ArrayList<>();

        for (LocalDate date: dateList){
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);

         Integer orderCount=getOrderCount(beginTime,endTime,null);
         //每天的有效订单数量
            Integer orderValid=getOrderCount(beginTime,endTime,Orders.COMPLETED);

            //

         orderValidList.add(orderValid);
         orderCountList.add(orderCount);


        }


        //计算单位时间内的订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).orElse(0);

        //计算单位时间内的有效订单总数
        Integer orderValidCount = orderValidList.stream().reduce(Integer::sum).orElse(0);


        //订单完成率
        Double orderCompletionRate =0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = orderValidCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCountList(StringUtils.join(orderValidList,","))
                .orderCompletionRate(orderCompletionRate)
                .validOrderCount(orderValidCount)
                .build();


        //有效订单int



    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {

            LocalDateTime beginTime=LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(end, LocalTime.MAX);

            List<GoodsSalesDTO> list = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = list.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String namelist = StringUtils.join(names, ",");

        List<Integer> numbers = list.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO.builder()
                .nameList(namelist)
                .numberList(numberList)
                .build();


    }
//导出数据报表
    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("end", end);
        map.put("begin", begin);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

}
