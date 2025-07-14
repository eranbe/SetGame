package com.eranbe.setgame.gemini;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.eranbe.setgame.game.Prompt;
import com.eranbe.setgame.model.SetCard;
import com.eranbe.setgame.model.SetGameSetsResponse;
import com.eranbe.setgame.util.TemplateReplacer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiInteractionManager {

    private final GeminiHintListener hintListener;
    private final GeminiExplanationListener explanationListener;
    private final ExecutorService executorService;
    private final Gson gson;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public GeminiInteractionManager(GeminiHintListener hintListener, GeminiExplanationListener explanationListener) {
        this.hintListener = hintListener;
        this.explanationListener = explanationListener;
        this.executorService = Executors.newSingleThreadExecutor();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void getHint(List<SetCard> currentBoardCards) {
        String boardJson = convertBoardToJson(currentBoardCards);
        if (boardJson == null) {
            hintListener.onHintError("שגיאה בהמרת הלוח ל-JSON");
            return;
        }

        executorService.execute(() -> {
            try {
                String prompt = TemplateReplacer.replacePlaceholders(Prompt.HINT_PROMPT,
                        Map.of("gameRules", Prompt.GAME_RULES,
                                "boardJson", boardJson,
                                "responseJsonSchema", Prompt.RESPONSE_JSON_SCHEMA));
                Log.d("GeminiPrompt", "Gemini prompt: " + prompt);
                GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d("GeminiHint", "Gemini raw response: " + result);
                        String cleanResult = result.contains("```json") && result.endsWith("```") ?
                                result.substring(result.indexOf("json")+4,
                                        result.lastIndexOf("}")+1).trim() : result.trim();
                        handleGeminiSetsResponse(cleanResult);
                    }

                    @Override
                    public void onError(Throwable error) { // השתמש ב-Throwable
                        Log.e("GeminiHint", "Error getting hint from Gemini (Throwable): " + error.getMessage(), error);
                        hintListener.onHintError("שגיאה בקבלת רמז מ-Gemini");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("GeminiHint", "Error getting hint from Gemini (Throwable): " + e.getMessage(), e);
                        hintListener.onHintError("שגיאה בקבלת רמז מ-Gemini");
                    }
                });
            } catch (Exception e) {
                Log.e("GeminiHint", "Error getting hint from Gemini (Executor): " + e.getMessage(), e);
                hintListener.onHintError("שגיאה בקבלת רמז מ-Gemini");
            }
        });
    }

    public void getNotASetExplanation(List<SetCard> cards) {
        executorService.execute(() -> {
            try {
                String prompt = TemplateReplacer.replacePlaceholders(Prompt.NOT_A_SET_PROMPT,
                        Map.of("card1", cards.get(0).toString(),
                                "card2", cards.get(1).toString(),
                                "card3", cards.get(2).toString()));
                Log.d("NotSetPrompt", "Not a Set prompt: " + prompt);

                GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        uiHandler.post(() -> {
                            Log.d("NotSetResponse", "Not a Set response: " + response);
                            explanationListener.onExplanationReady(response);
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) { // השתמש ב-Throwable
                        uiHandler.post(() -> {
                            Log.e("GeminiError", "Error from Gemini (Not a Set explanation): " + throwable.getMessage(), throwable);
                            explanationListener.onExplanationError("שגיאה בקבלת הסבר מג'מיני: " + throwable.getMessage());
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        uiHandler.post(() -> {
                            Log.e("GeminiError", "Error from Gemini (Not a Set explanation): " + e.getMessage(), e);
                            explanationListener.onExplanationError("שגיאה בקבלת הסבר מג'מיני: " + e.getMessage());
                        });
                    }
                });
            } catch (Exception e) {
                uiHandler.post(() -> {
                    Log.e("GeminiError", "Error during Not a Set explanation request (Executor): " + e.getMessage(), e);
                    explanationListener.onExplanationError("שגיאה בביצוע בקשת הסבר לג'מיני: " + e.getMessage());
                });
            }
        });
    }

    private String convertBoardToJson(List<SetCard> boardCards) {
        try {
            return gson.toJson(boardCards);
        } catch (Exception e) {
            Log.e("SetGameJSON", "Error converting board to JSON with Gson: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void handleGeminiSetsResponse(String jsonResponse) {
        try {
            SetGameSetsResponse response = gson.fromJson(jsonResponse, SetGameSetsResponse.class);

            if (response != null && response.getFoundSets() != null && !response.getFoundSets().isEmpty()) {
                List<List<SetCard>> allFoundSets = response.getFoundSets();
                List<SetCard> firstSet = allFoundSets.get(0); // Take the first set
                hintListener.onHintReady(firstSet);
            } else {
                hintListener.onHintReady(null); // No set found, or response was empty
                hintListener.onHintError(response != null ? response.getMessage() : "שגיאה בפענוח תשובת Gemini");
            }
        } catch (Exception e) {
            Log.e("SetGameActivity", "Failed to parse Gemini response: " + e.getMessage(), e);
            hintListener.onHintError("שגיאה בפענוח תשובת ג'מיני");
        }
    }

    public void shutdownExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}