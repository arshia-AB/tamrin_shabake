package peer.controllers;

import common.models.CLICommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PeerCommands implements CLICommands {
    END("exit"),
    DOWNLOAD("^\\s*download\\s*(?<name>\\S+.*\\S+)\\s*$"),
    LIST("^\\s*list\\s*$");

    private final String regex;

    PeerCommands(String regex) {
        this.regex = regex;
    }

    @Override
    public Matcher getMatcher(String input) {
        return Pattern.compile(regex).matcher(input);
    }
}
