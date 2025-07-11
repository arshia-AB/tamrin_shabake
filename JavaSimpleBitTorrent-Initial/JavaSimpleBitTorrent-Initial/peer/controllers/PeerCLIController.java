package peer.controllers;

import common.models.Message;
import common.utils.FileUtils;
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
        Map<String, String> files = FileUtils.listFilesInFolder(PeerApp.getSharedFolderPath());

        if (files == null || files.isEmpty()) {
            return "Repository is empty.";
        }

        return FileUtils.getSortedFileList(files);
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

            P2TConnectionThread tracker = PeerApp.getP2TConnection();
            Message response = P2TConnectionController.sendFileRequest(tracker, fileName);

            String senderIP = (String) response.getFromBody("sender_ip");
            int senderPort = (int) response.getFromBody("sender_port");
            String md5 = (String) response.getFromBody("md5");

            if (senderIP == null || md5 == null) {
                return "Tracker response invalid.";
            }

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
