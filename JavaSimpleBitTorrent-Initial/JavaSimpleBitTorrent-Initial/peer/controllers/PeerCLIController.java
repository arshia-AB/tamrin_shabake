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

    private static String handleDownload(String name) {
        if (FileUtils.listFilesInFolder(PeerApp.getSharedFolderPath()).containsKey(name)) {
            return "You already have the file!";
        }
        try {
            Message response = P2TConnectionController.sendFileRequest(PeerApp.getP2TConnection(), name);

            System.out.println("Download response body: " + response.getFromBody("response"));


            Object responseStatus = response.getFromBody("response");
            if (responseStatus == null) {
                return "Invalid response from peer: missing 'response' field";
            }
            if ("error".equals(responseStatus)) {
                Object error = response.getFromBody("error");
                if (error == null) {
                    return "Error from peer but no error details provided.";
                }
                switch (error.toString()) {
                    case "not_found":
                        return "No peer has the file!";
                    case "multiple_hash":
                        return "Multiple hashes found!";
                    default:
                        return "Peer returned error: " + error.toString();
                }
            }
            if (!"ok".equals(responseStatus)) {
                return "Unexpected response status: " + responseStatus.toString();
            }

            String hash = response.getFromBody("md5").toString();
            String IP = response.getFromBody("peer_have").toString();
            int port = response.getIntFromBody("peer_port");

            if (!PeerApp.requestDownload(IP, port, name, hash)) {
                return "The file has been downloaded from peer but is corrupted!";
            } else {
                return "File downloaded successfully: " + name;
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
