package com.eranbe.setgame.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetDeckInitializer {

    public static List<SetCard> initializeAShuffledDeck() {
        List<SetCard> deck = new ArrayList<>();

        for (SetCard.Shape shape : SetCard.Shape.values()) {
            for (SetCard.Color color : SetCard.Color.values()) {
                for (SetCard.Shading shading : SetCard.Shading.values()) {
                    for (SetCard.Number number : SetCard.Number.values()) {
                        deck.add(new SetCard(shape, color, shading, number));
                    }
                }
            }
        }
        Collections.shuffle(deck); // מערבב את החבילה
        return deck;
    }

    public static void main(String[] args) {
        List<SetCard> setDeck = initializeAShuffledDeck();
        System.out.println("חבילת SET נוצרה בהצלחה! מספר קלפים: " + setDeck.size());

        // מדפיס כמה קלפים לדוגמה
        for (int i = 0; i < Math.min(10, setDeck.size()); i++) {
            System.out.println(setDeck.get(i));
        }
    }
}