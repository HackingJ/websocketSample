
package duck.moon.sample.component;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import duck.moon.sample.bean.SampleService;

@ServerEndpoint(value = "/websockets", configurator=MdConfigurator.class)
@Component
public class MdServerEndpoint {
  @Autowired
  private SampleService sampleService;
  
  @OnOpen
  public void onOpen(Session session, EndpointConfig config) {
    System.out.println("Open websocket session!");
    sampleService.printSample();
  }
  
  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    System.out.println("Close websocket session");
  }
  
  @OnError
  public void onError(Session session, Throwable thr) {
    System.out.println("error occured");
  }
  
  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
    System.out.println("receive! " + message);
    session.getBasicRemote().sendText("It is server message!");
  }
}
