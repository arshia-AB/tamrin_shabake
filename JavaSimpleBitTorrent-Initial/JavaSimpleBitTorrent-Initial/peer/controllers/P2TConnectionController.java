package peer.controllers;

import com.google.gson.Gson;
import common.models.Message;
import peer.app.P2TConnectionThread;
import peer.app.PeerApp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static peer.app.PeerApp.getSentFiles;

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
        return new Message(map, Message.Type.response);
    }

    public static Message getFilesList() {
        File folder = new File(PeerApp.getSharedFolderPath());
        File[] files = folder.listFiles();

        HashMap<String, String> fileAndHashes = new HashMap<>();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    try {
                        String hash = common.utils.MD5Hash.HashFile(f.getAbsolutePath());
                        fileAndHashes.put(f.getName(), hash);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        HashMap<String, Object> body = new HashMap<>();
        body.put("command", "get_files_list");
        body.put("response", "ok");
        body.put("files", fileAndHashes);
        return new Message(body, Message.Type.response);
    }


    private static Message getSends() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("command", "get_sends");
        body.put("response", "ok");
        body.put("sent_files", getSentFiles());

        return new Message(body, Message.Type.response);
    }

    private static Message getReceives() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("command", "get_receives");
        body.put("response", "ok");
        body.put("received_files", PeerApp.getReceivedFiles());
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
