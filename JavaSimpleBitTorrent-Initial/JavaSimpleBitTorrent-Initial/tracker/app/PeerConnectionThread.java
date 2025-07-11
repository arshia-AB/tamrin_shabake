package tracker.app;

import common.models.ConnectionThread;
import common.models.Message;
import tracker.controllers.TrackerConnectionController;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static tracker.app.TrackerApp.TIMEOUT_MILLIS;

public class PeerConnectionThread extends ConnectionThread {
	private HashMap<String, String> fileAndHashes;

	public PeerConnectionThread(Socket socket) throws IOException {
		super(socket);
	}

	@Override
	public boolean initialHandshake() {
		try {
			// TODO: Implement initial handshake
			// Refresh peer status (IP and port), Get peer's file list, Add connection to tracker's connection list
			throw new UnsupportedOperationException("Initial handshake not implemented yet");
		} catch (Exception e) {
			return false;
		}
	}

	public void refreshStatus() {
		// TODO: Implement status refresh
		// Send status command and update peer's IP and port and wait for response
		// then update peer's IP and port
		throw new UnsupportedOperationException("Status refresh not implemented yet");
	}

	public void refreshFileList() {
		// TODO: Implement file list refresh
		// Request and update peer's file list
		throw new UnsupportedOperationException("File list refresh not implemented yet");
	}

	@Override
	protected boolean handleMessage(Message message) {
		if (message.getType() == Message.Type.file_request) {
			sendMessage(TrackerConnectionController.handleCommand(message));
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		super.run();
		TrackerApp.removePeerConnection(this);
	}

	public Map<String, String> getFileAndHashes() {
		return Map.copyOf(fileAndHashes);
	}
}
