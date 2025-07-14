package com.eranbe.setgame.gemini;

public interface GeminiExplanationListener {
    void onExplanationReady(String explanation);
    void onExplanationError(String errorMessage);
}
