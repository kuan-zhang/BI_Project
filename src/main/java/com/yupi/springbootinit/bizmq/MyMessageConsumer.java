package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * ClassName: MyMessageConsumer
 * Package: com.yupi.springbootinit.bizmq
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/26 16:04
 * @Version 1.0
 */
@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;
  @RabbitListener(queues = {"xiaoyu_queue"},ackMode = "MANUAL")
  @SneakyThrows
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
      log.info("receiveMessage message={}",message);
      channel.basicAck(deliveryTag,false);
  }
}
