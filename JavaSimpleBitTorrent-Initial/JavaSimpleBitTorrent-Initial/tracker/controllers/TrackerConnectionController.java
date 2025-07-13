package tracker.controllers;

import common.models.Message;
import common.utils.JSONUtils;
import tracker.app.PeerConnectionThread;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static tracker.app.TrackerApp.TIMEOUT_MILLIS;


public class TrackerConnectionController {
    private static final Map<String, PeerConnectionThread> peers = new ConcurrentHashMap<>();


    public static Message handleCommand(Message message) {
        try {

            String fileName = message.getFromBody("name");
//            String json = JSONUtils.toJson(message);
//            System.out.println("JSON message: " + json);
            if (fileName == null) {
                return createErrorResponse("invalid_request");
            }

            Map<String, List<String>> hashToPeers = new HashMap<>();

            for (PeerConnectionThread peer : peers.values()) {
                Map<String, String> files = peer.getFileAndHashes();
                String hash = files.get(fileName);
                if (hash != null) {
                    hashToPeers.computeIfAbsent(hash, k -> new ArrayList<>()).add(peer.getPeerIp() + ":" + peer.getListenPort());
                }
            }

            if (hashToPeers.isEmpty()) {
                return createErrorResponse("not_found");
            }

            if (hashToPeers.size() > 1) {
                return createErrorResponse("multiple_hash");
            }

            String md5Hash = hashToPeers.keySet().iterator().next();
            List<String> peerList = hashToPeers.get(md5Hash);

            String peerInfo = peerList.get(new Random().nextInt(peerList.size()));
            String[] parts = peerInfo.split(":");
            String peerIP = parts[0];
            int peerPort = Integer.parseInt(parts[1]);

            HashMap<String, Object> body = new HashMap<>();
            body.put("response", "peer_found");
            body.put("md5", md5Hash);
            body.put("peer_have", peerIP);
            body.put("peer_port", peerPort);

            return new Message(body, Message.Type.response);

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("server_error");
        }
    }


    public static Map<String, List<String>> getSends(PeerConnectionThread connection) {
        Message request = Message.createCommand("get_sends");
        try {
            Message response = connection.sendAndWaitForResponse(request, TIMEOUT_MILLIS);
            Map<String, List<String>> sentFiles = response.getFromBody("sent_files");
            System.out.println("DEBUG: getSends response sent_files: " + sentFiles);
            return sentFiles == null ? new HashMap<>() : sentFiles;
        } catch (Exception e) {
            System.err.println("DEBUG: Request Timed out.");
            return new HashMap<>();
        }
    }


    public static Map<String, List<String>> getReceives(PeerConnectionThread connection) {
        try {
            Message msg = Message.createCommand("get_receives");
            Message response = connection.sendAndWaitForResponse(msg, TIMEOUT_MILLIS);

            if (response != null && "get_receives".equals(response.getFromBody("command"))) {
                return response.getFromBody("received_files");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }


    public static void updatePeerInfo(PeerConnectionThread peer, String ip, int port) {
        peer.setPeerIp(ip);
        peer.setListenPort(port);
        String key = ip + ":" + port;
        peers.put(key, peer);
    }


    private static Message createErrorResponse(String errorMsg) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("response", "error");
        body.put("error", errorMsg);
        return new Message(body, Message.Type.response);
    }
}
