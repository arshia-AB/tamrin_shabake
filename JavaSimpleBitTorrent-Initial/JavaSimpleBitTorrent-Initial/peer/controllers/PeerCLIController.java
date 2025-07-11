package peer.controllers;

import common.models.Message;
import peer.app.P2TConnectionThread;
import peer.app.PeerApp;

import java.util.List;
import java.util.Map;

public class PeerCLIController {

	public static String processCommand(String command) {
		try {
			if (command.equalsIgnoreCase("END_PROGRAM")) {
				return endProgram();
			} else if (command.equalsIgnoreCase("LIST")) {
				return handleListFiles();
			} else if (command.toUpperCase().startsWith("DOWNLOAD ")) {
				return handleDownload(command);
			} else {
				return "Unknown command.";
			}
		} catch (Exception e) {
			return "Error processing command: " + e.getMessage();
		}
	}

	private static String handleListFiles() {
		try {
			Message response = P2TConnectionController.getFilesList();
			List<String> files = (List<String>) response.getFromBody("files");

			if (files == null || files.isEmpty()) {
				return "No files found.";
			}

			StringBuilder sb = new StringBuilder("Files:\n");
			for (String file : files) {
				sb.append("- ").append(file).append("\n");
			}
			return sb.toString();
		} catch (Exception e) {
			return "Failed to get files list: " + e.getMessage();
		}
	}

	private static String handleDownload(String command) {
		try {
			String[] parts = command.split("\\s+", 2);
			if (parts.length < 2) {
				return "Usage: DOWNLOAD <filename>";
			}
			String fileName = parts[1].trim();
			if (fileName.isEmpty()) {
				return "Invalid file name.";
			}

			// 1. Send request to tracker
			P2TConnectionThread tracker = PeerApp.getP2TConnection();
			Message response = P2TConnectionController.sendFileRequest(tracker, fileName);

			// 2. Tracker must return: sender_ip, sender_port, md5
			String senderIP = (String) response.getFromBody("sender_ip");
			int senderPort = (int) response.getFromBody("sender_port");
			String md5 = (String) response.getFromBody("md5");

			if (senderIP == null || md5 == null) {
				return "Tracker response invalid.";
			}

			// 3. Request file from peer
			boolean success = PeerApp.requestDownload(senderIP, senderPort, fileName, md5);
			if (success) {
				return "Download completed successfully.";
			} else {
				return "Download failed: File verification error.";
			}
		} catch (Exception e) {
			return "Download failed: " + e.getMessage();
		}
	}

	public static String endProgram() {
		PeerApp.endAll();
		return "Program terminated.";
	}
}
