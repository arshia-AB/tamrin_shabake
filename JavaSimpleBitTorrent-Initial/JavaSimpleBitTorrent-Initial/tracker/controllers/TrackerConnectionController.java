package tracker.controllers;

import common.models.Message;
import tracker.app.PeerConnectionThread;

import java.util.List;
import java.util.Map;

public class TrackerConnectionController {
	public static Message handleCommand(Message message) {
		// TODO: Handle incoming peer-to-tracker commands
		// 1. Validate message type and content
		// 2. Find peers having the requested file
		// 3. Check for hash consistency
		// 4. Return peer information or error
		throw new UnsupportedOperationException("handleCommand not implemented yet");
	}

	public static Map<String, List<String>> getSends(PeerConnectionThread connection) {
		// TODO: Get list of files sent by a peer
		// 1. Build command message
		// 2. Send message and wait for response
		// 3. Parse and return sent files map
		throw new UnsupportedOperationException("getSends not implemented yet");
	}

	public static Map<String, List<String>> getReceives(PeerConnectionThread connection) {
		// TODO: Get list of files received by a peer
		// 1. Build command message
		// 2. Send message and wait for response
		// 3. Parse and return received files map
		throw new UnsupportedOperationException("getReceives not implemented yet");
	}
}
