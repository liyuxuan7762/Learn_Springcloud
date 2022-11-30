package cn.itcast.order.service;

import cn.itcast.feign.client.UserClient;
import cn.itcast.feign.pojo.User;
import cn.itcast.order.mapper.OrderMapper;
import cn.itcast.order.pojo.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private UserClient userClient;

    public Order queryOrderById(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);
        String url = "http://userservice/user/" + order.getUserId();
        User user = restTemplate.getForObject(url, User.class);
        // 4.返回
        order.setUser(user);
        return order;
    }

    public Order queryOrderByIdWithFeign(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);
        User user = (User) userClient.getUserById(order.getUserId());
        // 4.返回
        order.setUser(user);
        return order;
    }
}
