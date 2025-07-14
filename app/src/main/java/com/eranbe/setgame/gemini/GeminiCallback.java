package com.eranbe.setgame.gemini;

public interface GeminiCallback {
    public void onSuccess(String result);
    public void onError(Throwable error);

    void onError(Exception e);
}
