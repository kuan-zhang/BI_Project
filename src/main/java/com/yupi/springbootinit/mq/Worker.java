package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*;

/**
 * ClassName: Worker
 * Package: com.yupi.springbootinit.mq
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/25 21:08
 * @Version 1.0
 */
public class Worker {
    private static final String TASK_QUEUE_NAME = "task_queue";


    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            String message = String.join(" ", argv);

            channel.basicPublish("", TASK_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
