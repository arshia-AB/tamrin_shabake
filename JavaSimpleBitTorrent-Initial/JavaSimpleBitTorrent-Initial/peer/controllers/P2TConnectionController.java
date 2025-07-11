package peer.controllers;

import common.models.Message;
import peer.app.P2TConnectionThread;

public class P2TConnectionController {
	public static Message handleCommand(Message message) {
		// TODO: Handle incoming tracker-to-peer commands
		// 1. Parse command from message
		// 2. Call appropriate handler (status, get_files_list, get_sends, get_receives)
		// 3. Return response message
		throw new UnsupportedOperationException("handleCommand not implemented yet");
	}

	private static Message getReceives() {
		// TODO: Return information about received files
		throw new UnsupportedOperationException("getReceives not implemented yet");
	}

	private static Message getSends() {
		// TODO: Return information about sent files
		throw new UnsupportedOperationException("getSends not implemented yet");
	}

	public static Message getFilesList() {
		// TODO: Return list of files in shared folder
		throw new UnsupportedOperationException("getFilesList not implemented yet");
	}

	public static Message status() {
		// TODO: Return peer status information
		throw new UnsupportedOperationException("status not implemented yet");
	}

	public static Message sendFileRequest(P2TConnectionThread tracker, String fileName) throws Exception {
		// TODO: Send file request to tracker and handle response
		// 1. Build request message
		// 2. Send message and wait for response
		// 3. raise exception if error or return response
		throw new UnsupportedOperationException("sendFileRequest not implemented yet");
	}
}
