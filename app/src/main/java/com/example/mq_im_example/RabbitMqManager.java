package com.example.mq_im_example;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMqManager {
    private static final String HOST = "20.120.25.102";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";
    private static Connection connection;
    private static Channel channel;
    private static String host = HOST;
    private static int port = PORT;
    private static String username = USERNAME;
    private static String password = PASSWORD;
    private static String vhost = "mq_im_example";

    public static void setConfig(String h, int p, String u, String pwd, String vhostName) {
        host = h;
        port = p;
        username = u;
        password = pwd;
        vhost = vhostName;
    }
    public static void setConfig(String h, int p, String u, String pwd) {
        setConfig(h, p, u, pwd, "mq_im_example");
    }
    public static void setConfig(String h, int p, String u, String pwd, String vhostName, boolean isImMode) {
        setConfig(h, p, u, pwd, vhostName);
        // 如需根据 isImMode 做特殊配置，可在此处扩展
    }
    public static void connect() throws IOException, TimeoutException {
        if (connection != null && connection.isOpen() && channel != null && channel.isOpen()) return;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vhost);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    // 修正：区分IM群聊和点对点
    public static void sendMessage(String queueOrExchange, String message, boolean isImMode) throws IOException, TimeoutException {
        connect();
        if (isImMode) {
            // IM群聊模式：fanout交换机
            channel.exchangeDeclare(queueOrExchange, BuiltinExchangeType.FANOUT, true);
            channel.basicPublish(queueOrExchange, "", null, message.getBytes());
        } else {
            // 点对点模式：普通队列
            channel.queueDeclare(queueOrExchange, false, false, false, null);
            channel.basicPublish("", queueOrExchange, null, message.getBytes());
        }
    }

    // 兼容旧接口
    public static void sendMessage(String queue, String message) throws IOException, TimeoutException {
        sendMessage(queue, message, false);
    }

    // 修正：区分IM群聊和点对点
    public static void receiveMessage(String queueOrExchange, DeliverCallback deliverCallback, boolean isImMode) throws IOException, TimeoutException {
        connect();
        if (isImMode) {
            // IM群聊模式：fanout交换机+临时队列
            channel.exchangeDeclare(queueOrExchange, BuiltinExchangeType.FANOUT, true);
            String queueName = channel.queueDeclare().getQueue(); // 临时队列
            channel.queueBind(queueName, queueOrExchange, "");
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } else {
            // 点对点模式：普通队列
            channel.queueDeclare(queueOrExchange, false, false, false, null);
            channel.basicConsume(queueOrExchange, true, deliverCallback, consumerTag -> {});
        }
    }

    // 兼容旧接口
    public static void receiveMessage(String queue, DeliverCallback deliverCallback) throws IOException, TimeoutException {
        receiveMessage(queue, deliverCallback, false);
    }

    public static void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) channel.close();
        if (connection != null && connection.isOpen()) connection.close();
    }
}
