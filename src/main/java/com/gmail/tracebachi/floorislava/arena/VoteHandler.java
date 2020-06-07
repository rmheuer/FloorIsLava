package com.gmail.tracebachi.floorislava.arena;

import java.util.HashMap;
import java.util.Map;

public class VoteHandler {

    private final Map<String, VoteType> votes;

    public VoteHandler() {
        this.votes = new HashMap<>();
    }

    public void addVoteForPerks(String playerName) {
        this.votes.put(playerName, VoteType.YES_PERKS);
    }

    public void addVoteForNoPerks(String playerName) {
        this.votes.put(playerName, VoteType.NO_PERKS);
    }

    public VoteType choose() {
        int perks = 0, noPerks = 0;
        for (VoteType type : votes.values()) {
            if (type == VoteType.YES_PERKS) perks++;
            else noPerks++;
        }
        if (perks > noPerks) return VoteType.YES_PERKS;
        else return VoteType.NO_PERKS;
    }

    public void resetVotes() {
        this.votes.clear();
    }

    public void removeVoteFor(String playerName) {
        this.votes.remove(playerName);
    }

    public enum VoteType {
        YES_PERKS, NO_PERKS
    }
}
