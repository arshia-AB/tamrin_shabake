package tracker.controllers;

import tracker.app.TrackerApp;
import common.models.CLICommands;
import common.models.ConnectionThread;
import common.utils.FileUtils;
import tracker.app.PeerConnectionThread;
import tracker.app.TrackerApp;

import java.util.*;
import java.util.regex.Matcher;

public class TrackerCLIController {
    public static String processCommand(String command) {
        Matcher matcher = null;
        if ((matcher = TrackerCommands.LIST_PEERS.getMatcher(command)).matches()) {
            return listPeers();
        } else if ((matcher = TrackerCommands.END.getMatcher(command)).matches()) {
            return endProgram();
        } else if ((matcher = TrackerCommands.LIST_FILES.getMatcher(command)).matches()) {
            return listFiles(matcher.group("ip"), Integer.parseInt(matcher.group("port")));
        } else if ((matcher = TrackerCommands.REFRESH_FILES.getMatcher(command)).matches()) {
            return refreshFiles();
        } else if ((matcher = TrackerCommands.RESET_CONNECTIONS.getMatcher(command)).matches()) {
            return resetConnections();
        } else if ((matcher = TrackerCommands.GET_SENDS.getMatcher(command)).matches()) {
            return getSends(matcher.group("ip"), Integer.parseInt(matcher.group("port")));
        } else if ((matcher = TrackerCommands.GET_RECEIVES.getMatcher(command)).matches()) {
            return getReceives(matcher.group("ip"), Integer.parseInt(matcher.group("port")));
        }

        return CLICommands.invalidCommand;
    }

    private static String getReceives(String ip, int port) {
        PeerConnectionThread peer = TrackerApp.getConnectionByIpPort(ip, port);
        if (peer == null) {
            return "Peer not found.";
        }
        Map<String, List<String>> receives = TrackerConnectionController.getReceives(peer);
        if (receives.isEmpty()) {
            return "No files received by " + ip + ":" + port;
        }
        return sort(receives);
    }


    private static String getSends(String ip, int port) {
        PeerConnectionThread peer = TrackerApp.getConnectionByIpPort(ip, port);
        if (peer == null) {
            return "Peer not found.";
        }
        Map<String, List<String>> sends = TrackerConnectionController.getSends(peer);
        if (sends.isEmpty()) {
            return "No files sent by " + ip + ":" + port;
        }
        return sort(sends);
    }

    private static String listFiles(String ip, int port) {
        PeerConnectionThread connection = TrackerApp.getConnectionByIpPort(ip, port);

        if (connection == null) {
            return "Peer not found.";
        }

        if (connection.getFileAndHashes().isEmpty()) {
            return "Repository is empty.";
        }

        return FileUtils.getSortedFileList(connection.getFileAndHashes());
    }

    private static String listPeers() {
        StringBuilder result = new StringBuilder();
        for (PeerConnectionThread connection : TrackerApp.getConnections()) {
            result.append(connection.getOtherSideIP()).append(":").append(connection.getOtherSidePort()).append("\n");
        }
        if (result.toString().isEmpty()) return "No peers connected.";
        return result.toString().trim();
    }

    private static String resetConnections() {
        for (int i = TrackerApp.getConnections().size() - 1; i > -1; i--) {
            PeerConnectionThread connection = TrackerApp.getConnections().get(i);
            connection.refreshStatus();
        }

        refreshFiles();
        return "";
    }

    private static String refreshFiles() {
        for (int i = TrackerApp.getConnections().size() - 1; i > -1; i--) {
            TrackerApp.getConnections().get(i).refreshFileList();
        }

        return "";
    }

    private static String endProgram() {
        TrackerApp.endAll();
        return "";
    }

    private static String sort(Map<String, List<String>> filesList) {
        List<Map.Entry<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filesList.entrySet()) {
            for (String fileAndHash : entry.getValue()) {
                list.add(new AbstractMap.SimpleEntry<>(entry.getKey(), fileAndHash));
            }
        }
        list.sort((entry1, entry2) -> {
            int valueCompare = entry1.getValue().compareTo(entry2.getValue());
            if (valueCompare != 0) {
                return valueCompare;
            }
            return entry1.getKey().compareTo(entry2.getKey());
        });

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : list) {
            result.append(entry.getValue()).append(" - ").append(entry.getKey()).append("\n");
        }
        return result.toString().trim();
    }

}
