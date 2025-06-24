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
    public static void connect() throws IOException, TimeoutException {
        if (connection != null && connection.isOpen()) return;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vhost);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public static void sendMessage(String queue, String message) throws IOException, TimeoutException {
        connect();
        channel.queueDeclare(queue, false, false, false, null);
        channel.basicPublish("", queue, null, message.getBytes());
    }

    public static void receiveMessage(String queue, DeliverCallback deliverCallback) throws IOException, TimeoutException {
        connect();
        channel.queueDeclare(queue, false, false, false, null);
        channel.basicConsume(queue, true, deliverCallback, consumerTag -> {});
    }

    public static void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) channel.close();
        if (connection != null && connection.isOpen()) connection.close();
    }
}
