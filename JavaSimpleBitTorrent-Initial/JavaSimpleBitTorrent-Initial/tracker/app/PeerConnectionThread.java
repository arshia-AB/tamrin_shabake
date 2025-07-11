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

                setOtherSideIP(peerIp);
                setOtherSidePort(listenPort);
                this.setPeerIp(peerIp);
                this.setListenPort(listenPort);

                TrackerConnectionController.updatePeerInfo(this, peerIp, listenPort);
                TrackerApp.addPeerConnection(this);

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

    public boolean refreshFileList() {
        try {
            HashMap<String, Object> body = new HashMap<>();
            body.put("command", "get_files_list");

            Message response = sendAndWaitForResponse(new Message(body, Message.Type.command), TIMEOUT_MILLIS);

            if (response == null) {
                System.err.println("refreshFileList: response is null");
                this.end();
                return false;
            }

            Object respValue = response.getFromBody("response");
            if (respValue == null) {
                System.err.println("refreshFileList: response field is missing");
                this.end();
                return false;
            }

            if (!"ok".equals(String.valueOf(respValue))) {
                System.err.println("refreshFileList: response not ok, got: " + respValue);
                this.end();
                return false;
            }

            Object filesObj = response.getFromBody("files");
            if (filesObj == null) {
                System.err.println("refreshFileList: files field is missing");
                return false;
            }

            if (filesObj instanceof Map<?, ?> filesMap) {
                synchronized (this.fileAndHashes) {
                    this.fileAndHashes.clear();
                    for (Map.Entry<?, ?> entry : filesMap.entrySet()) {
                        if (entry.getKey() instanceof String key && entry.getValue() instanceof String value) {
                            this.fileAndHashes.put(key, value);
                        }
                    }
                }
                return true;
            } else {
                System.err.println("refreshFileList: files is not a Map");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.end();
            return false;
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
