package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.pay.service.AliPayService;
import com.youlexuan.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService payService;

    @Autowired
    private IdWorker idWorker;

    /**
     * 演示调用支付宝接口返回支付的url链接
     * @return
     */
    @RequestMapping("/creatNative")
    public Map createNative() {
        String out_trade_no = idWorker.nextId()+"";
        String total_amount = "1";
        return payService.createNative(out_trade_no,total_amount);
    }
}
