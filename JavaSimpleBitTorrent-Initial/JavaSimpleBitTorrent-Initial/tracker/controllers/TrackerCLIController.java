package tracker.controllers;

import tracker.app.TrackerApp;

public class TrackerCLIController {
	public static String processCommand(String command) {
		// TODO: Process tracker CLI commands
		// 1. Check command type (END_PROGRAM, REFRESH_FILES, RESET_CONNECTIONS, LIST_PEERS, LIST_FILES, GET_RECEIVES, GET_SENDS)
		// 2. Call appropriate handler
		// 3. Return result or error message
		throw new UnsupportedOperationException("processCommand not implemented yet");
	}

	private static String getReceives(String command) {
		// TODO: Get list of files received by a peer
		throw new UnsupportedOperationException("getReceives not implemented yet");
	}

	private static String getSends(String command) {
		// TODO: Get list of files sent by a peer
		throw new UnsupportedOperationException("getSends not implemented yet");
	}

	private static String listFiles(String command) {
		// TODO: List files of a peer (do not send command, use the cached list)
		throw new UnsupportedOperationException("listFiles not implemented yet");
	}

	private static String listPeers() {
		// TODO: List all connected peers
		throw new UnsupportedOperationException("listPeers not implemented yet");
	}

	private static String resetConnections() {
		// TODO: Reset all peer connections
		// Refresh status and file list for each peer
		throw new UnsupportedOperationException("resetConnections not implemented yet");
	}

	private static String refreshFiles() {
		// TODO: Refresh file lists for all peers
		throw new UnsupportedOperationException("refreshFiles not implemented yet");
	}

	private static String endProgram() {
		TrackerApp.endAll();
		return "";
	}
}
