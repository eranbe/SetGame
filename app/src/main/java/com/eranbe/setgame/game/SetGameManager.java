package com.eranbe.setgame.game;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.eranbe.setgame.gemini.GeminiExplanationListener;
import com.eranbe.setgame.gemini.GeminiHintListener;
import com.eranbe.setgame.gemini.GeminiInteractionManager;
import com.eranbe.setgame.model.SetCard;
import com.eranbe.setgame.model.SetChecker;
import com.eranbe.setgame.model.SetDeckInitializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetGameManager implements GeminiExplanationListener, GeminiHintListener {

    private final SetGameListener listener;
    private final ExecutorService executorService;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final GeminiInteractionManager geminiInteractionManager;

    private final List<SetCard> selectedCards = new ArrayList<>();
    private final List<SetCard> currentBoardCards = new ArrayList<>();
    private List<SetCard> deck;

    public SetGameManager(SetGameListener listener) {
        this.listener = listener;
        this.executorService = Executors.newSingleThreadExecutor();
        this.geminiInteractionManager = new GeminiInteractionManager(this, this);
    }

    public List<SetCard> getCurrentBoardCards() {
        return currentBoardCards;
    }

    public boolean isCardSelected(SetCard card) {
        return selectedCards.contains(card);
    }

    public void restartGame() {
        Log.d("SetGameDebug", "--- התחלת משחק מחדש ---");
        selectedCards.clear();
        currentBoardCards.clear();
        deck = null;

        listener.onToastMessage("המשחק התחיל מחדש!");
        loadInitialCards();
        Log.d("SetGameDebug", "--- סיום משחק מחדש ---");
    }

    public void loadInitialCards() {
        executorService.execute(() -> {
            deck = SetDeckInitializer.initializeAShuffledDeck();
            uiHandler.post(() -> listener.onCardsRemainingUpdated(deck.size()));

            List<SetCard> initialBoardCards = new ArrayList<>();
            for (int i = 0; i < 12 && !deck.isEmpty(); i++) {
                initialBoardCards.add(deck.remove(0));
            }

            uiHandler.post(() -> {
                this.currentBoardCards.clear();
                this.currentBoardCards.addAll(initialBoardCards);
                listener.onBoardChanged(this.currentBoardCards);
                listener.onCardsRemainingUpdated(deck.size());
            });
        });
    }

    public void handleCardClick(SetCard clickedCard, int position,
                                boolean isDetailedExplanationRequested) {
        if (selectedCards.contains(clickedCard)) {
            selectedCards.remove(clickedCard);
        } else {
            if (selectedCards.size() < 3) {
                selectedCards.add(clickedCard);
            } else {
                listener.onToastMessage("ניתן לבחור עד 3 קלפים בלבד!");
            }
        }
        listener.onBoardChanged(currentBoardCards);

        if (selectedCards.size() == 3) {
            SetCard card1 = selectedCards.get(0);
            SetCard card2 = selectedCards.get(1);
            SetCard card3 = selectedCards.get(2);

            boolean isCurrentSelectionASet = SetChecker.isSet(card1, card2, card3);

            if (isCurrentSelectionASet) {
                listener.onSetFound();
                handleSetFound();
            } else {
                if (isDetailedExplanationRequested) {
                    listener.onToastMessage("מבקש מג'מיני הסבר...");
                    geminiInteractionManager.getNotASetExplanation(selectedCards);
                } else {
                    uiHandler.post(() -> {
                        listener.onNotASet("זה לא סט... נסו שוב!", false);
                        clearSelection();
                    });
                }
            }
        }
    }

    public void requestGeminiHint() {
        listener.onToastMessage("מבקש רמז מג'מיני..."); // הודעת ביניים למשתמש
        geminiInteractionManager.getHint(currentBoardCards);
    }

    @Override
    public void onExplanationReady(String explanation) {
        listener.onNotASet(explanation, true);
        clearSelection();
    }

    @Override
    public void onExplanationError(String errorMessage) {
        listener.onNotASet("זה לא סט... שגיאה בקבלת הסבר מג'מיני: " + errorMessage, true);
        clearSelection();
    }

    @Override
    public void onHintReady(List<SetCard> suggestedSet) {
        // העברת הרמז הלאה ל-SetGameListener (ה-Activity)
        listener.onHintProvided(suggestedSet);
    }

    @Override
    public void onHintError(String errorMessage) {
        // העברת שגיאת הרמז הלאה ל-SetGameListener (ה-Activity)
        listener.onToastMessage("שגיאה בקבלת רמז: " + errorMessage);
        listener.onHintProvided(null); // אופציונלי: לנקות את הרמז הקודם אם יש שגיאה
    }

    private void handleSetFound() {
        uiHandler.post(() -> {
            Log.d("SetGameDebug", "--- התחלת handleSetFound ---");
            List<Integer> positionsToReplace = new ArrayList<>();
            for (SetCard selectedCard : selectedCards) {
                int index = currentBoardCards.indexOf(selectedCard);
                if (index != -1) {
                    positionsToReplace.add(index);
                }
            }
            Collections.sort(positionsToReplace);

            currentBoardCards.removeAll(selectedCards);

            if (currentBoardCards.size() == 9 && deck.size() >= 3) {
                for (int index : positionsToReplace) {
                    if (!deck.isEmpty()) {
                        currentBoardCards.add(index, deck.remove(0));
                    } else {
                        Log.w("SetGameDebug", "אין מספיק קלפים בחבילה למילוי כל המקומות.");
                        break;
                    }
                }
            } else {
                while (currentBoardCards.size() < 12 && !deck.isEmpty()) {
                    currentBoardCards.add(deck.remove(0));
                }
            }

            selectedCards.clear();
            listener.onBoardChanged(currentBoardCards);
            listener.onCardsRemainingUpdated(deck.size());

            checkForSetsOnBoard();
            Log.d("SetGameDebug", "--- סיום handleSetFound ---");
        });
    }

    public void checkForSetsOnBoard() {
        uiHandler.post(() -> {
            List<List<SetCard>> foundSets = SetChecker.findSetsOnBoard(currentBoardCards);
            if (foundSets.isEmpty()) {
                if (!deck.isEmpty()) {
                    if (currentBoardCards.size() < 15) {
                        listener.onNoSetsOnBoard();
                        addThreeMoreCardsToBoard();
                    } else {
                        listener.onToastMessage("אין סטים זמינים בלוח, אך הלוח מלא.");
                    }
                } else {
                    listener.onDeckEmptyAndNoSets();
                }
            } else {
                Log.d("SetGameDebug", "checkForSetsOnBoard: נמצאו " + foundSets.size() + " סטים על הלוח.");
                String message = foundSets.size() == 1 ?
                        "יש עוד סט אחד על הלוח!" :
                        String.format("יש עוד %d סטים על הלוח!", foundSets.size());
                listener.onToastMessage(message);
            }
        });
    }

    private void addThreeMoreCardsToBoard() {
        uiHandler.post(() -> {
            Log.d("SetGameDebug", "--- התחלת addThreeMoreCardsToBoard ---");
            if (deck.isEmpty()) {
                listener.onToastMessage("אין יותר קלפים בחבילה!");
                Log.w("SetGameDebug", "ניסיון להוסיף קלפים אך החפיסה ריקה.");
                return;
            }

            List<SetCard> cardsToAdd = new ArrayList<>();
            int cardsAddedCount = 0;
            for (int i = 0; i < 3; i++) {
                if (!deck.isEmpty()) {
                    cardsToAdd.add(deck.remove(0));
                    cardsAddedCount++;
                } else {
                    Log.w("SetGameDebug", "אין מספיק קלפים בחביסה להוספת 3. נוספו רק " + cardsAddedCount);
                    break;
                }
            }
            currentBoardCards.addAll(cardsToAdd);
            listener.onBoardChanged(currentBoardCards);
            listener.onCardsRemainingUpdated(deck.size());

            Log.d("SetGameDebug", "נוספו " + cardsAddedCount + " קלפים. סה\"כ בלוח: " + currentBoardCards.size());
            Log.d("SetGameDebug", "--- סיום addThreeMoreCardsToBoard ---");

            checkForSetsOnBoard();
        });
    }

    private void clearSelection() {
        selectedCards.clear();
        listener.onBoardChanged(currentBoardCards);
    }

    public void shutdownExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        geminiInteractionManager.shutdownExecutor();
    }
}