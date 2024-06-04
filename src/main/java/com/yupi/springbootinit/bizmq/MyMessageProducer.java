package com.yupi.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ClassName: MyMessageProducer
 * Package: com.yupi.springbootinit.bizmq
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/26 16:00
 * @Version 1.0
 */
@Component
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;
    public void sendMessage(String exchange,String routingKey,String message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}
