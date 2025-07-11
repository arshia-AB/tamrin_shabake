package peer.app;

import common.models.Message;
import common.utils.JSONUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static peer.app.PeerApp.TIMEOUT_MILLIS;

public class P2PListenerThread extends Thread {
    private final ServerSocket serverSocket;

    public P2PListenerThread(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    private void handleConnection(Socket socket) throws Exception {
        socket.setSoTimeout(TIMEOUT_MILLIS);

        try {
            Message message = Message.readFrom(socket.getInputStream());

            if (message.getType() == Message.Type.download_request) {
                TorrentP2PThread torrentThread = new TorrentP2PThread(socket, message);
                torrentThread.start();
            } else {
                socket.close();
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            throw e;
        }
    }

    @Override
    public void run() {
        while (!PeerApp.isEnded()) {
            try {
                Socket socket = serverSocket.accept();
                handleConnection(socket);
            } catch (Exception e) {
                break;
            }
        }

        try {
            serverSocket.close();
        } catch (Exception ignored) {
        }
    }
}
