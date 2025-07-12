package peer.app;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class TorrentP2PThread extends Thread {
    private final Socket socket;
    private final File file;
    private final String receiver;
    private final BufferedOutputStream dataOutputStream;

    public TorrentP2PThread(Socket socket, File file, String receiver) throws IOException {
        this.socket = socket;
        this.file = file;
        this.receiver = receiver;
        this.dataOutputStream = new BufferedOutputStream(socket.getOutputStream());
        PeerApp.addTorrentP2PThread(this);
    }

    @Override
    public void run() {
        try (var fileInputStream = new java.io.BufferedInputStream(new java.io.FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            dataOutputStream.flush();


            String md5 = common.utils.MD5Hash.HashFile(file.getPath());

            PeerApp.addSentFile(receiver, file.getName() + " " + md5);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                dataOutputStream.close();
                socket.close();
            } catch (Exception ignored) {
            }
            PeerApp.removeTorrentP2PThread(this);
        }
    }

    public void end() {
        try {
            dataOutputStream.close();
            socket.close();
        } catch (Exception e) {
        }
    }
}
