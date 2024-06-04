package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sun.tools.jdeprscan.scan.Scan;

import java.util.Scanner;

/**
 * ClassName: DirectProducer
 * Package: com.yupi.springbootinit.mq
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/25 22:01
 * @Version 1.0
 */
public class DirectProducer {
    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            Scanner scanner=new Scanner(System.in);
            while(scanner.hasNext()){
                String message=scanner.nextLine();
                channel.basicPublish(EXCHANGE_NAME, "xiaoyu", null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + "xiaoyu" + "':'" + message + "'");
            }


        }
    }
}
