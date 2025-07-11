package tracker.app;

import common.models.ConnectionThread;
import common.models.Message;
import tracker.controllers.TrackerConnectionController;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static tracker.app.TrackerApp.TIMEOUT_MILLIS;

public class PeerConnectionThread extends ConnectionThread {
    private HashMap<String, String> fileAndHashes = new HashMap<>();

    private String peerIp;
    private int listenPort;

    public PeerConnectionThread(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public boolean initialHandshake() {
        try {
            Message statusCommand = Message.createCommand("status");

            Message response = sendAndWaitForResponse(statusCommand, TIMEOUT_MILLIS);

            if (response != null &&
                    response.getType() == Message.Type.response &&
                    "status".equals(response.getFromBody("command")) &&
                    "ok".equals(response.getFromBody("response"))) {

                String peerIp = response.getFromBody("peer");
                int listenPort = (int) ((double) response.getFromBody("listen_port"));

                TrackerConnectionController.updatePeerInfo(this, peerIp, listenPort);

                refreshFileList();

                return true;
            } else {
                System.err.println("initialHandshake: invalid response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void refreshStatus() {
        try {
            Message statusCommand = Message.createCommand("status");

            Message response = sendAndWaitForResponse(statusCommand, TIMEOUT_MILLIS);

            if (response != null &&
                    response.getType() == Message.Type.response &&
                    "status".equals(response.getFromBody("command")) &&
                    "ok".equals(response.getFromBody("response"))) {

                String peerIp = response.getFromBody("peer");
                int listenPort = (int) ((double) response.getFromBody("listen_port"));

                TrackerConnectionController.updatePeerInfo(this, peerIp, listenPort);
            } else {
                System.err.println("refreshStatus: invalid response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshFileList() {
        try {
            Message fileListCommand = Message.createCommand("file_list");

            Message response = sendAndWaitForResponse(fileListCommand, TIMEOUT_MILLIS);

            if (response != null &&
                    response.getType() == Message.Type.response &&
                    "file_list".equals(response.getFromBody("command"))) {

                HashMap<String, String> files = response.getFromBody("files");

                if (files != null) {
                    fileAndHashes = new HashMap<>(files);
                } else {
                    System.err.println("refreshFileList: files not found in response");
                }
            } else {
                System.err.println("refreshFileList: invalid response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean handleMessage(Message message) {
        if (message.getType() == Message.Type.file_request) {
            sendMessage(TrackerConnectionController.handleCommand(message));
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        super.run();
        TrackerApp.removePeerConnection(this);
    }

    public Map<String, String> getFileAndHashes() {
        return Map.copyOf(fileAndHashes);
    }

    public String getPeerIp() {
        return peerIp;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setPeerIp(String peerIp) {
        this.peerIp = peerIp;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }
}
