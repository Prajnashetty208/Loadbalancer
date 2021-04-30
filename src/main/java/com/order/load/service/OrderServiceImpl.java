package com.order.load.service;

import com.order.load.model.Order;
import com.order.load.repository.OrderRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService{

    private DiscoveryClient discoveryClient;

    private RestTemplate restTemplate;

    @Autowired
    private OrderRepo orderDAO;

    @Override
    public Order save(Order obj) {
        //finds all existing orders using discovery client
        //discoveryClient();
        loadBalancedUsingRestTemplate();
        return this.orderDAO.save(obj);
    }

    @Override
    public Set<Order> findAll() {
        System.out.println("findAll");
        return new HashSet<>(this.orderDAO.findAll());
    }

    @Override
    public Order findById(Long id) {
        return this.orderDAO.findById(id)
                .orElseThrow(OrderServiceImpl::invalidOrderId);
    }

    private static IllegalArgumentException invalidOrderId() {
        return new IllegalArgumentException("Invalid Order Id");
    }

    @Override
    public void deleteOrder(Long id) {
        this.orderDAO.deleteById(id);
    }

    private void loadBalancedUsingRestTemplate() {
        Object response = this.restTemplate.getForEntity("http://orderservice/api/order/getOrder",
                null, Object.class);

    }

    private void discoveryClient(){
        List<ServiceInstance> services = this.discoveryClient.getInstances("ORDERSERVICE");
        System.out.println("Services = "+ services);
        if (services != null && services.size() > 0 ) {
            //invoke find all
            String uri = services.get(0).getUri().toString() + "/api/order/getOrder";
            Object response = this.restTemplate.getForEntity(uri, null, Object.class);
            System.out.println("Response = "+ response);
        }
    }

}
