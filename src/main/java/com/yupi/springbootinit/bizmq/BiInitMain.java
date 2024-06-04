package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * ClassName: BiInitMain
 * Package: com.yupi.springbootinit.bizmq
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/26 16:26
 * @Version 1.0
 */
@Component
public class BiInitMain {
    public static void main(String[] args) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME=BiMqConstant.BI_EXCHANGE;
            channel.exchangeDeclare(EXCHANGE_NAME,"direct");
            String queueName=BiMqConstant.BI_QUEUE_NAME;
            channel.queueDeclare(queueName,true,false,false,null);
            channel.queueBind(queueName,EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

    }
}
