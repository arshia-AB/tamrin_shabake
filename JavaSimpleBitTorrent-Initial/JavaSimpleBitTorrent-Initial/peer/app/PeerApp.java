package peer.app;

import common.models.Message;
import common.utils.JSONUtils;
import common.utils.MD5Hash;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class PeerApp {
    public static final int TIMEOUT_MILLIS = 500;

    private static String peerIP;
    private static int peerPort;
    private static String sharedFolderPath;

    private static P2TConnectionThread trackerConnectionThread;
    private static P2PListenerThread p2pListenerThread;
    private static final List<TorrentP2PThread> torrentThreads = new ArrayList<>();

    private static final Map<String, List<String>> sentFiles = new HashMap<>();
    private static final Map<String, List<String>> receivedFiles = new HashMap<>();

    private static boolean exitFlag = false;

    public static boolean isEnded() {
        return exitFlag;
    }

    public static void initFromArgs(String[] args) throws Exception {
        if (args.length < 3)
            throw new IllegalArgumentException("Usage: <self ip:port> <tracker ip:port> <shared_folder_path>");

        String[] selfParts = args[0].split(":");
        peerIP = selfParts[0];
        peerPort = Integer.parseInt(selfParts[1]);

        String[] trackerParts = args[1].split(":");
        String trackerIP = trackerParts[0];
        int trackerPort = Integer.parseInt(trackerParts[1]);

        sharedFolderPath = args[2].trim();

        Socket trackerSocket = new Socket(trackerIP, trackerPort);
        trackerConnectionThread = new P2TConnectionThread(trackerSocket);
        p2pListenerThread = new P2PListenerThread(peerPort);
    }

    public static void endAll() {
        exitFlag = true;
        try {
            if (trackerConnectionThread != null) trackerConnectionThread.end();
        } catch (Exception ignored) {
        }

        for (TorrentP2PThread t : torrentThreads) {
            try {
                t.end();
            } catch (Exception ignored) {
            }
        }
        torrentThreads.clear();

        sentFiles.clear();
        receivedFiles.clear();
    }

    public static void connectTracker() {
        if (trackerConnectionThread != null && !trackerConnectionThread.isAlive()) {
            trackerConnectionThread.start();
        }
    }

    public static void startListening() {
        if (p2pListenerThread != null && !p2pListenerThread.isAlive()) {
            p2pListenerThread.start();
        }
    }

    public static void removeTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
        if (torrentP2PThread != null) {
            torrentThreads.remove(torrentP2PThread);
            torrentP2PThread.end();
        }
    }

    public static void addTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
        if (torrentP2PThread != null && !torrentThreads.contains(torrentP2PThread)) {
            torrentThreads.add(torrentP2PThread);
        }
    }

    public static String getSharedFolderPath() {
        return sharedFolderPath;
    }

    public static void addSentFile(String receiver, String fileNameAndHash) {
        sentFiles.computeIfAbsent(receiver, k -> new ArrayList<>()).add(fileNameAndHash);
    }

    public static void addReceivedFile(String sender, String fileNameAndHash) {
        receivedFiles.computeIfAbsent(sender, k -> new ArrayList<>()).add(fileNameAndHash);
    }

    public static String getPeerIP() {
        return peerIP;
    }

    public static int getPeerPort() {
        return peerPort;
    }

    public static Map<String, List<String>> getSentFiles() {
        return new HashMap<>(sentFiles);
    }

    public static Map<String, List<String>> getReceivedFiles() {
        return new HashMap<>(receivedFiles);
    }

    public static P2TConnectionThread getP2TConnection() {
        return trackerConnectionThread;
    }

    public static boolean requestDownload(String senderIP, int senderPort, String filename, String md5) throws Exception {

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", filename);
        map.put("md5", md5);
        map.put("receiver_ip", peerIP);
        map.put("receiver_port", peerPort);
        Message downloadMessage = new Message(map, Message.Type.download_request);
        try {
            Socket socket = new Socket(senderIP, senderPort);
            socket.setSoTimeout(TIMEOUT_MILLIS);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(JSONUtils.toJson(downloadMessage));
            out.flush();

            File sharedFile = new File(sharedFolderPath + File.separator + filename);
            InputStream in = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(sharedFile.getPath());
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            String hash = MD5Hash.HashFile(sharedFile.getPath());
            if (!hash.equals(md5)) {
                sharedFile.delete();
                return false;
            }
            addReceivedFile(senderIP + ":" + senderPort, filename + " " + md5);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
