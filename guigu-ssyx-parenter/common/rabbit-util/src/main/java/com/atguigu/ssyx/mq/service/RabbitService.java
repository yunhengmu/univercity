package com.atguigu.ssyx.mq.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ClassName: RabbitService
 * Package: com.atguigu.ssyx.mq.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/31 11:32
 * @Version 1.0
 */
@Service
public class RabbitService {
    // 引入操作rabbitmq的模板
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean sendMessage(String exchange,String routingKey, Object message){
       rabbitTemplate.convertSendAndReceive(exchange,routingKey,message);
       return true;
    }

    /*
    * 发送消息
    * @param exchange 交换机
    * @param routingKey 路由器
    * @param message 消息
    * @return
    * */
    public boolean sendDelayMessage(String exchange,String routingKey, Object message, int delayTime){
        rabbitTemplate.convertSendAndReceive(exchange,routingKey,message,new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //  设置一个延迟时间
                message.getMessageProperties().setDelay(delayTime*1000);
                return message;
            }
        });
        return true;
    }



}
