package peer.controllers;

import peer.app.PeerApp;

public class PeerCLIController {
	public static String processCommand(String command) {
		// TODO: Process Peer CLI commands
		// 1. Check command type (END_PROGRAM, DOWNLOAD, LIST)
		// 2. Call appropriate handler
		// 3. Return result or error message
		throw new UnsupportedOperationException("processCommand not implemented yet");
	}

	private static String handleListFiles() {
		// TODO: Handle list files command
		throw new UnsupportedOperationException("handleListFiles not implemented yet");
	}

	private static String handleDownload(String command) {
		// TODO: Handle download command
		// Send file request to tracker
		// Get peer info and file hash
		// Request file from peer
		// Return success or error message
		throw new UnsupportedOperationException("handleDownload not implemented yet");
	}

	public static String endProgram() {
		PeerApp.endAll();
		return "";
	}
}
