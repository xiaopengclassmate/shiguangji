package com.foodtime.controller.admin;

import com.aliyuncs.http.HttpRequest;
import com.foodtime.result.Result;
import com.foodtime.service.ReportService;
import com.foodtime.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/admin/report")
@Api(tags = "数据统计")
public class ReportController {
    @Autowired
    private ReportService reportService;
    @ApiOperation("营业额数据统计")
    @GetMapping("/turnoverStatistics")

    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("营业额数据统计：{}到{}", begin, end);
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }
    @ApiOperation("用户数据统计")
    @GetMapping("/userStatistics")
   public Result<UserReportVO> userStatistisics(
           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
   ){
        log.info("用户数据统计：{}到{}", begin, end);
        return Result.success(reportService.getUserStatistics(begin, end));
   }
   @ApiOperation("订单数据统计")
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin
           ,@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单数据统计：{}到{}", begin, end);
        return Result.success(reportService.getOrdersStatistics(begin, end));
   }

   @ApiOperation("销量Top10")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin
           ,@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("销量Top10：{}到{}", begin, end);
        return Result.success(reportService.getTop10(begin, end));
   }
   @ApiOperation("导出数据")
   @GetMapping("/export")
   public void export(HttpServletResponse  response){
        reportService.exportBusinessData(response);

   }
}
