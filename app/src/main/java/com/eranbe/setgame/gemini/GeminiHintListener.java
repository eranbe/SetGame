package com.eranbe.setgame.gemini;

import com.eranbe.setgame.model.SetCard;
import java.util.List;

public interface GeminiHintListener {
    void onHintReady(List<SetCard> suggestedSet);
    void onHintError(String errorMessage);
}