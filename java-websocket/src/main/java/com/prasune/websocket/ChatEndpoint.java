package com.prasune.websocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.prasune.websocket.model.Message;

@ServerEndpoint(value="/chat/{username}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndpoint {

	private static ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<>();
 
    @OnOpen
    public void onOpen(
      Session session, 
      @PathParam("username") String username) throws IOException, EncodeException {
  
    	session.getUserProperties().put("username", username);
        userSessions.put(session.getId(), session);
 
        Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");
        broadcast(message);
    }
 
    @OnMessage
    public void onMessage(Session session, Message message) 
      throws IOException, EncodeException {
  
        message.setFrom((String) session.getUserProperties().get("username"));
        broadcast(message);
    }
 
    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
  
        userSessions.remove(session.getId());
        Message message = new Message();
        message.setFrom((String) session.getUserProperties().get("username"));
        message.setContent("Disconnected!");
        broadcast(message);
    }
 
    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    	throwable.printStackTrace();
    }
 
    private static void broadcast(Message message) 
      throws IOException, EncodeException {
  
    	userSessions.forEach((id, session) -> {
    		synchronized (session) {
                try {
                    session.getBasicRemote().
                      sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
