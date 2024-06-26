package com.sky.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getTimeList(begin,end);

        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnover = (turnover == null ? 0.0 : turnover);
            turnoverList.add(turnover);
        }

        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        String list = StringUtils.join(dateList, ",");
        String tlist = StringUtils.join(turnoverList, ",");

        turnoverReportVO.setDateList(list);
        turnoverReportVO.setTurnoverList(tlist);
        return turnoverReportVO;
    }


    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getTimeList(begin,end);

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map tmap = new HashMap();
            Map nmap = new HashMap();

            tmap.put("end",endTime);
            Integer tuser = userMapper.countByMap(tmap);
            if(tuser == null){
                tuser=0;
            }
            totalUserList.add(tuser);

            nmap.put("begin",beginTime);
            nmap.put("end",endTime);
            Integer nuser = userMapper.countByMap(nmap);
            if(nuser == null){
                nuser=0;
            }
            newUserList.add(nuser);

        }

        UserReportVO userReportVO = new UserReportVO();

        String dlist = StringUtils.join(dateList, ",");
        String nlist = StringUtils.join(newUserList, ",");
        String tlist = StringUtils.join(totalUserList,",");

        userReportVO.setDateList(dlist);
        userReportVO.setTotalUserList(tlist);
        userReportVO.setNewUserList(nlist);

        return userReportVO;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getorderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getTimeList(begin,end);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> vaildOrderCountList = new ArrayList<>();


        for (LocalDate date : dateList) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            Integer oderCount = orderMapper.countByMap(map);
            orderCountList.add(oderCount);

            map.put("status",Orders.COMPLETED);
            Integer vailOderCount = orderMapper.countByMap(map);
            vaildOrderCountList.add(vailOderCount);


        }


        OrderReportVO orderReportVO = new OrderReportVO();

        String dlist = StringUtils.join(dateList, ",");
        String olist = StringUtils.join(orderCountList, ",");
        String vlist = StringUtils.join(vaildOrderCountList, ",");

        orderReportVO.setDateList(dlist);
        orderReportVO.setOrderCountList(olist);
        orderReportVO.setValidOrderCountList(vlist);

        Integer orderCount = orderCountList.stream().reduce(Integer::sum).get();
        orderReportVO.setTotalOrderCount(orderCount);
        Integer vorderCount = vaildOrderCountList.stream().reduce(Integer::sum).get();
        orderReportVO.setValidOrderCount(vorderCount);

        Double ordercomplete = (double) vorderCount/orderCount;
        if(vorderCount == 0){
            ordercomplete = 0.0;
        }
        orderReportVO.setOrderCompletionRate(ordercomplete);

        return orderReportVO;
    }


    /**
     * 销量top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesList = orderMapper.getSalesList(beginTime, endTime);
        List<String> nameList = salesList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = salesList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());


//        List<String> nameList = new ArrayList<>();
//        List<String> numberList = new ArrayList<>();
//
//        for (GoodsSalesDTO good : salesList) {
//            nameList.add(good.getName());
//            numberList.add(String.valueOf(good.getNumber()));
//        }

        String naList = StringUtils.join(nameList, ",");
        String nuList = StringUtils.join(numberList, ",");

        SalesTop10ReportVO salesVO = new SalesTop10ReportVO();
        salesVO.setNameList(naList);
        salesVO.setNumberList(nuList);

        return salesVO;
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            XSSFSheet sheet1 = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet1.getRow(1).getCell(1).setCellValue("时间："+dateBegin+"至"+dateEnd);


            XSSFRow row = sheet1.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());


            row = sheet1.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            for (int i = 0; i < 30 ; i++){
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO dayData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet1.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dayData.getTurnover());
                row.getCell(3).setCellValue(dayData.getValidOrderCount());
                row.getCell(4).setCellValue(dayData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dayData.getUnitPrice());
                row.getCell(6).setCellValue(dayData.getNewUsers());
            }

            ServletOutputStream out = response.getOutputStream();

            excel.write(out);
            out.close();
            excel.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private List<LocalDate> getTimeList(LocalDate begin, LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
