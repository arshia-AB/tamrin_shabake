package peer.controllers;

import common.models.Message;
import common.utils.FileUtils;
import peer.app.P2TConnectionThread;
import peer.app.PeerApp;
import common.models.CorruptedFileException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class PeerCLIController {

    public static String processCommand(String command) {
        try {
            Matcher matcher = null;
            if (PeerCommands.END.matches(command)) {
                return endProgram();
            } else if (PeerCommands.LIST.matches(command)) {
                return handleListFiles();
            } else if ((matcher = PeerCommands.DOWNLOAD.getMatcher(command)).matches()) {
                return handleDownload(matcher.group("name"));
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

    private static String handleDownload(String fileName) {
        try {
            if (FileUtils.listFilesInFolder(PeerApp.getSharedFolderPath()).containsKey(fileName)) {
                return "You already have the file!";
            }
            Message response = P2TConnectionController.sendFileRequest(PeerApp.getP2TConnection(), fileName);

            if (response == null) {
                return "No response from tracker!";
            }

            Object respType = response.getFromBody("response");
            if ("error".equals(respType)) {
                String error = (String) response.getFromBody("error");


                if ("not_found".equals(error)) {
                    return "No peer has the file!";
                } else if ("multiple_hash".equals(error)) {
                    return "Multiple hashes found!";
                } else {
                    return "Unknown error from tracker: " + error;
                }
            }

            String peerIP = (String) response.getFromBody("peer_have");
            int peerPort = response.getIntFromBody("peer_port");
            String md5 = (String) response.getFromBody("md5");

            boolean downloadSuccess = PeerApp.requestDownload(peerIP, peerPort, fileName, md5);

            if (downloadSuccess) {
                return "File downloaded successfully: " + fileName;
            } else {
                return "No peer has the file!";

//                return "The file has been downloaded from peer but is corrupted!";
            }

        } catch (Exception e) {
//            e.printStackTrace();
            return "No peer has the file!";
        }
    }

    public static String endProgram() {
        PeerApp.endAll();
        return "";

    }
}
