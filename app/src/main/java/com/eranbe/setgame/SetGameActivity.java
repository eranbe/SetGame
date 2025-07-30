package com.eranbe.setgame;

import android.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ScrollView;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eranbe.setgame.R;
import com.eranbe.setgame.adapter.SetCardAdapter;
import com.eranbe.setgame.model.SetCard;

import com.eranbe.setgame.game.GameTimer;
import com.eranbe.setgame.game.SetGameManager;
import com.eranbe.setgame.game.SetGameListener;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class SetGameActivity extends AppCompatActivity
        implements SetGameListener, GameTimer.OnTimerUpdateListener {

    private SetCardAdapter adapter;
    private TextView cardsRemainingTextView;
    private TextView gameTimerTextView;
    private Button hintButton;

    private TextView setsFoundTextView;

    private CheckBox detailedExplanationCheckBox;

    private SetGameManager gameManager;
    private GameTimer gameTimer;

    final private List<SetCard> currentHintedSet = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_game);

        // 1. Initialize UI components
        RecyclerView recyclerView = findViewById(R.id.set_recycler_view);
        cardsRemainingTextView = findViewById(R.id.cards_remaining_text_view);
        gameTimerTextView = findViewById(R.id.game_timer_text_view);
        setsFoundTextView = findViewById(R.id.sets_found_text_view);
        Button restartGameButton = findViewById(R.id.restart_game_button);
        hintButton = findViewById(R.id.hint_button);
        detailedExplanationCheckBox = findViewById(R.id.detailed_explanation_checkbox);

        // 2. Initialize core game components
        // SetGameManager מקבל את this עבור SetGameListener
        gameManager = new SetGameManager(this);
        gameTimer = new GameTimer(this); // Pass activity as OnTimerUpdateListener

        // 3. Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new SetCardAdapter(this, gameManager.getCurrentBoardCards());
        recyclerView.setAdapter(adapter);

        // 4. Setup Listeners
        adapter.setOnCardClickListener((card, position) -> {
            gameManager.handleCardClick(card, position, detailedExplanationCheckBox.isChecked());
            // אין צורך ב-updateCurrentlySelectedCards או notifyItemChanged כאן,
            // onBoardChanged כבר מטפל בזה דרך gameManager.
        });
        restartGameButton.setOnClickListener(v -> {
            gameTimer.stopTimer();
            gameManager.restartGame();
            gameTimer.startTimer();
        });
        hintButton.setOnClickListener(v -> {
            hintButton.setEnabled(false); // Disable hint button when request is sent
            gameManager.requestGeminiHint(); // קוראים למנהל המשחק לבקש רמז
        });

        // 5. Start game components
        gameManager.loadInitialCards();
        gameTimer.startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameTimer.stopTimer();
        gameManager.shutdownExecutor(); // Manager will shut down its own GeminiInteractionManager
    }

    @Override
    public void onBoardChanged(List<SetCard> newBoardCards) {
        // בודקים אם יש סט מוצע כרגע ומעבירים אותו לאדפטר
        adapter.setCards(newBoardCards);
        adapter.setGeminiSuggestedSet(currentHintedSet); // מוודא שהרמז נשאר מוצג אם קיים
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCardsRemainingUpdated(int remainingCards) {
        runOnUiThread(() -> cardsRemainingTextView.setText(String.format(Locale.getDefault(),
                "קלפים בחבילה: %d", remainingCards)));
    }

    @Override
    public void onSetFound() {
        runOnUiThread(() -> {
            Toast.makeText(this, "מצאתם סט! כל הכבוד!", Toast.LENGTH_SHORT).show();
            // כשמוצאים סט, מנקים את הרמז אם היה מוצג
            currentHintedSet.clear();
            setsFoundTextView.setText(String.format(Locale.getDefault(), "סטים עד כה: %d",
                    gameManager.getSetsFound()));
            adapter.setGeminiSuggestedSet(currentHintedSet); // מעדכן את האדפטר לנקות את הרמז
        });
    }

    @Override
    public void onNotASet(String explanation, boolean isDetailedExplanationRequested) {
        runOnUiThread(() -> {
            if (isDetailedExplanationRequested) {
                TextView messageTextView = new TextView(this);
                messageTextView.setText(explanation);
                messageTextView.setMovementMethod(new ScrollingMovementMethod());
                messageTextView.setPadding(30, 20, 30, 20);

                ScrollView scrollView = new ScrollView(this);
                scrollView.addView(messageTextView);

                AlertDialog notFoundDialog = new AlertDialog.Builder(this)
                        .setTitle("זה לא סט...")
                        .setView(scrollView)
                        .setPositiveButton("הבנתי", (dialog, which) -> dialog.dismiss())
                        .show();
                // נסה למצוא את ה-TextView של הכותרת בתוך הדיאלוג
                TextView titleView = notFoundDialog.findViewById(android.R.id.title);
                if (titleView != null) {
                    titleView.setGravity(Gravity.RIGHT); // יישור לימין
                }
            } else {
                Toast.makeText(this, "זה לא סט... נסו שוב!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNoSetsOnBoard() {
        runOnUiThread(() -> Toast.makeText(this, "אין סטים על הלוח, מוסיף עוד 3 קלפים...", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDeckEmptyAndNoSets() {
        runOnUiThread(() -> {
            Toast.makeText(this, "המשחק הסתיים! אין יותר קלפים או סטים.", Toast.LENGTH_LONG).show();
            gameTimer.stopTimer();
        });
    }

    @Override
    public void onToastMessage(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onTimerUpdated(String formattedTime) {
        runOnUiThread(() -> gameTimerTextView.setText("זמן: " + formattedTime));
    }

    @Override
    public void onHintProvided(List<SetCard> hintedCards) {
        runOnUiThread(() -> {
            currentHintedSet.clear();
            if (hintedCards != null) {
                currentHintedSet.addAll(hintedCards);
            }
            adapter.setGeminiSuggestedSet(currentHintedSet);
            Toast.makeText(this, hintedCards != null && !hintedCards.isEmpty() ? "רמז נמצא!" : "ג'מיני לא מצא סטים כרגע.", Toast.LENGTH_SHORT).show();
            hintButton.setEnabled(true);
        });
    }

    public boolean isCardSelected(SetCard card) {
        return gameManager.isCardSelected(card);
    }
}