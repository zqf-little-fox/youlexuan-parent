package com.youlexuan.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.CONSTANT;
import com.youlexuan.entity.PageResult;
import com.youlexuan.mapper.TbOrderItemMapper;
import com.youlexuan.mapper.TbOrderMapper;
import com.youlexuan.order.service.OrderService;
import com.youlexuan.pojo.TbOrder;
import com.youlexuan.pojo.TbOrderExample;
import com.youlexuan.pojo.TbOrderItem;
import com.youlexuan.pojogroup.Cart;
import com.youlexuan.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(TbOrder order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(CONSTANT.CART_LIST_REDIS_KEY).get(order.getOrderId());
        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();
            System.out.println("商家sellerId：" + cart.getSellerId());
            TbOrder tborder = new TbOrder();// 新创建订单对象
            tborder.setOrderId(orderId);// 订单ID
            tborder.setUserId(order.getUserId());// 用户名
            tborder.setPaymentType(order.getPaymentType());// 支付类型
            tborder.setStatus("1");// 状态：未付款
            tborder.setCreateTime(new Date());// 订单创建日期
            tborder.setUpdateTime(new Date());// 订单更新日期
            tborder.setReceiverAreaName(order.getReceiverAreaName());// 地址
            tborder.setReceiverMobile(order.getReceiverMobile());// 手机号
            tborder.setReceiver(order.getReceiver());// 收货人
            tborder.setSourceType(order.getSourceType());// 订单来源
            tborder.setSellerId(cart.getSellerId());// 商家ID
            // 循环购物车明细
            double money = 0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);// 订单ID
                orderItem.setSellerId(cart.getSellerId());
                money += orderItem.getTotalFee().doubleValue();// 金额累加
                orderItemMapper.insert(orderItem);
            }
            tborder.setPayment(new BigDecimal(money));
            orderMapper.insert(tborder);
        }
        redisTemplate.boundHashOps(CONSTANT.CART_LIST_REDIS_KEY).delete(order.getUserId());
    }

    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public TbOrder findOne(Long orderId) {
        return orderMapper.selectByPrimaryKey(orderId);
    }

    @Override
    public void delete(Long[] orderIds) {
        if (orderIds != null) {
            for (Long orderId : orderIds) {
                orderMapper.deleteByPrimaryKey(orderId);
            }
        }
    }

    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        TbOrderExample example = new TbOrderExample();
        TbOrderExample.Criteria criteria = example.createCriteria();
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateOrderStauts(String out_trade_no, String transaction_id) {

    }
}
