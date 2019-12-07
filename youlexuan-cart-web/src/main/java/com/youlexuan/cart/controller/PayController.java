package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.entity.Result;
import com.youlexuan.pay.service.AliPayService;
import com.youlexuan.pay.service.PayLogService;
import com.youlexuan.pojo.TbPayLog;
import com.youlexuan.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService payService;

    @Reference
    private PayLogService payLogService;

    @Autowired
    private IdWorker idWorker;

    /**
     * 演示调用支付宝接口返回支付的url链接
     * @return
     */
    @RequestMapping("/creatNative")
    public Map createNative() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = payLogService.searchPayLogByUserId(userId);
        if(tbPayLog != null){
            String out_trade_no = tbPayLog.getOutTradeNo() + "";
            String total_amount = tbPayLog.getTotalFee()/100 + "";
            return payService.createNative(out_trade_no, total_amount);
        }else{
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        int x = 1;
        while (true){
            Map map = payService.queryPayStatus(out_trade_no);
            Object tradestatus = map.get("tradestatus");
            if(tradestatus!=null){
                if("TRADE_SUCCESS".equals(tradestatus)){
                    payLogService.updateOrderStatus(out_trade_no, (String) map.get("trade_no"));
                    // 如果支付成功，需要修改订单状态为已支付、还要修改支付日志状态为已支付
                    return  new Result(true,"支付成功");
                }
                if("TRADE_FINISHED".equals(tradestatus)){
                    payLogService.updateOrderStatus(out_trade_no, (String) map.get("trade_no"));
                    return  new Result(true,"交易结束，不可退款");
                }
                if("TRADE_CLOSED".equals(tradestatus)){
                    return  new Result(false,"未付款交易超时关闭，或支付完成后全额退款");
                }
            }

            x++;
            if(x>100){
                return  new Result(false,"支付超时");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
