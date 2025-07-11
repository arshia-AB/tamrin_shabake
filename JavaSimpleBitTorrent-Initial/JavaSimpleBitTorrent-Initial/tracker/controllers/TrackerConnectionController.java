package tracker.controllers;

import common.models.Message;
import tracker.app.PeerConnectionThread;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrackerConnectionController {
	private static final Map<String, PeerConnectionThread> peers = new ConcurrentHashMap<>();


	public static Message handleCommand(Message message) {
		try {
			String fileName = message.getFromBody("file_name");
			String fileHash = message.getFromBody("file_hash");

			if (fileName == null || fileHash == null) {
				return createErrorResponse("Invalid request: missing file_name or file_hash");
			}

			List<Map<String, Object>> matchingPeers = new ArrayList<>();

			for (PeerConnectionThread peer : peers.values()) {
				Map<String, String> files = peer.getFileAndHashes();
				String hash = files.get(fileName);
				if (hash != null && hash.equals(fileHash)) {
					Map<String, Object> peerInfo = new HashMap<>();
					peerInfo.put("ip", peer.getPeerIp());
					peerInfo.put("listen_port", peer.getListenPort());
					matchingPeers.add(peerInfo);
				}
			}

			HashMap<String, Object> body = new HashMap<>();
			body.put("command", "file_request");
			body.put("response", "ok");
			body.put("peers", matchingPeers);

			return new Message(body, Message.Type.response);
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("Server error: " + e.getMessage());
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
		body.put("message", errorMsg);
		return new Message(body, Message.Type.response);
	}
}
