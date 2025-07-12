package tracker.controllers;

import common.models.Message;
import common.utils.JSONUtils;
import tracker.app.PeerConnectionThread;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class TrackerConnectionController {
    private static final Map<String, PeerConnectionThread> peers = new ConcurrentHashMap<>();


    public static Message handleCommand(Message message) {
        try {

            String fileName = message.getFromBody("name");
            String json = JSONUtils.toJson(message);
            System.out.println("JSON message: " + json);
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
        try {
            Message msg = Message.createCommand("get_sends");
            Message response = connection.sendAndWaitForResponse(msg, 3000);

            if (response != null && "get_sends".equals(response.getFromBody("command"))) {
                return response.getFromBody("files");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }


    public static Map<String, List<String>> getReceives(PeerConnectionThread connection) {
        try {
            Message msg = Message.createCommand("get_receives");
            Message response = connection.sendAndWaitForResponse(msg, 3000);

            if (response != null && "get_receives".equals(response.getFromBody("command"))) {
                return response.getFromBody("files");
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
