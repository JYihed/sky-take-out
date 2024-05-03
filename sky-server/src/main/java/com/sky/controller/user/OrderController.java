package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户端订单相关接口")
@RequestMapping("/user/order")
@RestController("userOrderController")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private WebSocketServer webSocketServer;


    /**
     * 用户下单
     * @param submitDTO
     * @return
     */
    @ApiOperation("用户下单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO submitDTO){
        OrderSubmitVO submitVO = orderService.submitOrder(submitDTO);
        Map map =new HashMap();
        map.put("type",1);
        map.put("orderId",submitVO.getId());
        map.put("content","订单号:"+submitVO.getOrderNumber());

        String s = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(s);
        log.info("发了");
        return Result.success(submitVO);
    }

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @ApiOperation("查询历史订单")
    @GetMapping("//historyOrders")
    public Result<PageResult> page(int page, int pageSize, Integer status){
        PageResult pageResult = orderService.pageQueryUser(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable("id") Long id){
        OrderVO orderVO = orderService.getOrderById(id);
        return Result.success(orderVO);
    }


}
