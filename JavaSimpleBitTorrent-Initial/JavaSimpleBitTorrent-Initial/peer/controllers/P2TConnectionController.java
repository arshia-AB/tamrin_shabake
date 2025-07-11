package peer.controllers;

import common.models.Message;
import peer.app.P2TConnectionThread;
import peer.app.PeerApp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class P2TConnectionController {

    public static Message handleCommand(Message message) {
        String cmd = message.getFromBody("command");
        switch (cmd) {
            case "status":
                return status();
            case "get_files_list":
                return getFilesList();
            case "get_sends":
                return getSends();
            case "get_receives":
                return getReceives();
            default:
                throw new IllegalStateException("Unexpected value: " + message.getFromBody("command").toString());
        }
    }

    public static Message status() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("command", "status");
        map.put("response", "ok");
        map.put("peer", PeerApp.getPeerIP());
        map.put("listen_port", PeerApp.getPeerPort());
        return new Message(map,Message.Type.response);
    }

    public static Message getFilesList() {
        File folder = new File(PeerApp.getSharedFolderPath());
        File[] files = folder.listFiles();

        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    fileNames.add(f.getName());
                }
            }
        }

        HashMap<String, Object> body = new HashMap<>();
        body.put("files", fileNames);
        return new Message(body, Message.Type.response);
    }

    private static Message getSends() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("sent", PeerApp.getSentFiles());
        return new Message(body, Message.Type.response);
    }

    private static Message getReceives() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("received", PeerApp.getReceivedFiles());
        return new Message(body, Message.Type.response);
    }

    public static Message sendFileRequest(P2TConnectionThread tracker, String fileName) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "file_request");
        map.put("name", fileName);
        Message response = tracker.sendAndWaitForResponse(new Message(map, Message.Type.file_request), PeerApp.TIMEOUT_MILLIS);
        if (response == null || response.getType() != Message.Type.response) {
            throw new Exception("error request file: " + fileName);
        } else {
            return response;
        }

    }
}
