package com.eranbe.setgame.adapter;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.eranbe.setgame.R;
import com.eranbe.setgame.SetGameActivity;
import com.eranbe.setgame.model.SetCard;

import java.util.ArrayList;
import java.util.List;

public class SetCardAdapter extends RecyclerView.Adapter<SetCardAdapter.SetCardViewHolder> {

    private List<SetCard> cards;
    private OnCardClickListener onCardClickListener;
    private final SetGameActivity activityContext;
    private final List<SetCard> geminiSuggestedSet = new ArrayList<>();

    public interface OnCardClickListener {
        void onCardClick(SetCard card, int position);
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.onCardClickListener = listener;
    }

    public SetCardAdapter(SetGameActivity activityContext, List<SetCard> cards) {
        this.activityContext = activityContext;
        this.cards = cards != null ? cards : new ArrayList<>();
    }

    /**
     * מגדיר את הסט שג'מיני מצא ומסמן אותו ויזואלית.
     * @param suggestedSet רשימת 3 הקלפים המהווים סט שג'מיני מצא, או null/רשימה ריקה כדי לבטל סימון.
     */
    public void setGeminiSuggestedSet(List<SetCard> suggestedSet) {
        this.geminiSuggestedSet.clear();
        if (suggestedSet != null) {
            this.geminiSuggestedSet.addAll(suggestedSet);
        }
        notifyDataSetChanged(); // רענן את כל ה-RecyclerView כדי שהסימון יופיע/ייעלם
    }

    // זו המתודה שתקרא כשיהיו קלפים חדשים להצגה
    public void setCards(List<SetCard> newCards) {
        this.cards = newCards != null ? newCards : new ArrayList<>();
        notifyDataSetChanged(); // מעדכן את ה-RecyclerView שהנתונים השתנו
    }

    @NonNull
    @Override
    public SetCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_set_card, parent, false);
        return new SetCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetCardViewHolder holder, int position) {
        SetCard currentCard = cards.get(position);

        // **לוגיקת סימון הקלף על ידי המשתמש:**
        if (activityContext.isCardSelected(currentCard)) {
            holder.itemView.setBackgroundResource(R.drawable.card_background_selected_shape);
        } else {
            // ודא שזהו אותו רקע שמוגדר ב-item_set_card.xml
            holder.itemView.setBackgroundResource(R.drawable.card_background_shape);
        }

        // **לוגיקת סימון סט מ-Gemini עם אייקון הנורה:**
        if (geminiSuggestedSet.contains(currentCard)) {
            holder.geminiIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.geminiIndicator.setVisibility(View.GONE);
        }

        // הגדרת הצורות בהתאם למספר הקלף
        ImageView[] symbols = {holder.symbol1, holder.symbol2, holder.symbol3};

        // קביעת צבע עבור כל הסמלים בקלף
        int colorResId;
        switch (currentCard.getColor()) {
            case RED:    colorResId = R.color.set_red; break;
            case GREEN:  colorResId = R.color.set_green; break;
            case PURPLE: colorResId = R.color.set_purple; break;
            default:     colorResId = R.color.set_red; break; // ברירת מחדל
        }
        int actualColor = ContextCompat.getColor(holder.itemView.getContext(), colorResId);

        // קביעת צורה והצללה (ה-drawable עצמו)
        Drawable shapeDrawable = null;
        switch (currentCard.getShape()) {
            case DIAMOND:
                switch (currentCard.getShading()) {
                    case OPEN: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_diamond_outline); break;
                    case SOLID: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_diamond_solid); break;
                    case STRIPED: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_diamond_striped); break;
                }
                break;
            case SQUIGGLE:
                switch (currentCard.getShading()) {
                    case OPEN: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_pentagram_outline); break;
                    case SOLID: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_pentagram_solid); break;
                    case STRIPED: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_pentagram_striped); break;
                }
                break;
            case OVAL:
                switch (currentCard.getShading()) {
                    case OPEN: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_oval_outline); break;
                    case SOLID: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_oval_solid); break;
                    case STRIPED: shapeDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_shape_oval_striped); break;
                }
                break;
        }

        // לולאה להגדרת נראות הסמלים והצבת ה-drawable וה-tint
        for (int i = 0; i < symbols.length; i++) {
            if (i < currentCard.getNumber().ordinal() + 1) { // ordinal() 0-based, +1 for 1,2,3
                // הגדר את הסמל כגלוי
                symbols[i].setVisibility(View.VISIBLE);

                // הגדר את ה-drawable והצבע
                if (shapeDrawable != null) {
                    symbols[i].setImageDrawable(shapeDrawable);
                    symbols[i].setColorFilter(new PorterDuffColorFilter(actualColor, PorterDuff.Mode.SRC_IN));
                    symbols[i].setImageAlpha(255); // וודא שקיפות מלאה (לא עמעום)
                }
            } else {
                // הגדר את הסמל כבלתי נראה (invisible) אך שומר מקום
                symbols[i].setVisibility(View.INVISIBLE);
            }
        }

        // טיפול בקליקים על קלפים
        holder.itemView.setOnClickListener(v -> {
            if (onCardClickListener != null) {
                onCardClickListener.onCardClick(currentCard, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public static class SetCardViewHolder extends RecyclerView.ViewHolder {
        ImageView symbol1, symbol2, symbol3;
        ImageView geminiIndicator;

        public SetCardViewHolder(@NonNull View itemView) {
            super(itemView);
            symbol1 = itemView.findViewById(R.id.card_symbol_1);
            symbol2 = itemView.findViewById(R.id.card_symbol_2);
            symbol3 = itemView.findViewById(R.id.card_symbol_3);
            geminiIndicator = itemView.findViewById(R.id.gemini_set_indicator);
        }
    }
}