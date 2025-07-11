package tracker.controllers;

import common.models.CLICommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TrackerCommands implements CLICommands {
	END("exit"),
	LIST_PEERS("^\\s*list_peers\\s*$"),
	LIST_FILES("^\\s*list_files\\s*(?<ip>\\S+):(?<port>\\d+)\\s*$"),
	REFRESH_FILES("^\\s*refresh_files\\s*$"),
	GET_SENDS("^\\s*get_sends\\s*(?<ip>\\S+):(?<port>\\d+)\\s*$"),
	GET_RECEIVES("^\\s*get_receives\\s*(?<ip>\\S+):(?<port>\\d+)\\s*$"),
	RESET_CONNECTIONS("^\\s*reset_connections\\s*$");

	private final String regex;

	TrackerCommands(String regex) {
		this.regex = regex;
	}

	@Override
	public Matcher getMatcher(String input) {
		return Pattern.compile(regex).matcher(input);
	}
}
