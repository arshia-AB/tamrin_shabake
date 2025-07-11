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
		// TODO: Implement file transfer
		// 1. Open file input stream
		// 2. Read file in chunks and send to peer
		// 3. Flush and close output stream
		// 4. Update sent files list with file name and MD5 hash

		try {
			socket.close();
		} catch (Exception e) {}

		PeerApp.removeTorrentP2PThread(this);
	}

	public void end() {
		try {
			dataOutputStream.close();
			socket.close();
		} catch (Exception e) {}
	}
}
