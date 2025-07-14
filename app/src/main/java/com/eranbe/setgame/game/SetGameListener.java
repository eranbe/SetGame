package com.eranbe.setgame.game;

import com.eranbe.setgame.model.SetCard;
import java.util.List;

public interface SetGameListener {
    void onBoardChanged(List<SetCard> newBoardCards);
    void onCardsRemainingUpdated(int remainingCards);
    void onSetFound();
    void onNotASet(String explanation, boolean isDetailedExplanationRequested);
    void onNoSetsOnBoard();
    void onDeckEmptyAndNoSets();
    void onToastMessage(String message); // General purpose toast messages
    void onHintProvided(List<SetCard> suggestedSet);
}