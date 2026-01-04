package com.usst.adfluxbackend.websocket;

import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/ad-debug")
@Component
public class AdDebugWebSocket {

    // 存储所有连接的管理员 Session
    private static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("管理员已连接监控面板: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 可以处理来自前端的消息，例如控制调试开关
        System.out.println("收到前端消息: " + message);
    }

    /**
     * 广播消息给所有管理员
     */
    public static void sendDebugInfo(Object message) {
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}