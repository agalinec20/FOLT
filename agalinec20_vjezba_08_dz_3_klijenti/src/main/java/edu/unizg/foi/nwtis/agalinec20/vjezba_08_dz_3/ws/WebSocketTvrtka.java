package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.ws;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.ServisTvrtkaKlijent;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.GlobalniPodaci;

@ApplicationScoped
@ServerEndpoint("/ws/tvrtka")
public class WebSocketTvrtka {

    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    @Inject
    @RestClient
    private ServisTvrtkaKlijent servisTvrtka;

    @Inject
    private GlobalniPodaci globalni;

    public static void send(String poruka) {
        for (Session sess : sessions) {
            if (sess.isOpen()) {
                try {
                    sess.getBasicRemote().sendText(poruka);
                } catch (IOException ignored) {}
            }
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        sessions.add(session);
        sendStatusToAll();
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        sendStatusToAll();
    }

    private void sendStatusToAll() {
        String statusFlag;
        try {
            int code = servisTvrtka.headPosluzitelj().getStatus();
            statusFlag = (code == 200) ? "RADI" : "NE RADI";
        } catch (Exception e) {
            statusFlag = "NE RADI";
        }
        int brojObracuna = globalni.getBrojObracuna();
        String internaPoruka = globalni.getPoruka();
        if (internaPoruka == null) {
            internaPoruka = "";
        }
        String tekst = String.join(";", statusFlag,
                                    String.valueOf(brojObracuna),
                                    internaPoruka);
        for (Session sess : sessions) {
            if (sess.isOpen()) {
                try {
                    sess.getBasicRemote().sendText(tekst);
                } catch (IOException ignored) {}
            }
        }
    }
}
