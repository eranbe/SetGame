package com.eranbe.setgame.model;

import java.util.List;

public class SetGameSetsResponse {
    private List<List<SetCard>> foundSets;
    private String message;

    public SetGameSetsResponse() {}

    public List<List<SetCard>> getFoundSets() { return foundSets; }
    public void setFoundSets(List<List<SetCard>> foundSets) { this.foundSets = foundSets; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}