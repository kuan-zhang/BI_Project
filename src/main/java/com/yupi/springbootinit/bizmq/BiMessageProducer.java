package com.yupi.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ClassName: BiMessageProducer
 * Package: com.yupi.springbootinit.bizmq
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/26 16:27
 * @Version 1.0
 */
@Component
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;
    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE,BiMqConstant.BI_ROUTING_KEY,message);
    }
}
