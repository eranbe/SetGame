package com.eranbe.setgame.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetChecker {

    /**
     * בודק אם שלושה קלפי Set מהווים סט חוקי.
     * סט חוקי הוא כזה שבו עבור כל אחד מארבעת המאפיינים (צורה, צבע, הצללה, מספר),
     * הערכים של שלושת הקלפים הם או כולם זהים, או כולם שונים.
     *
     * @param card1 הקלף הראשון.
     * @param card2 הקלף השני.
     * @param card3 הקלף השלישי.
     * @return true אם הקלפים מהווים סט, false אחרת.
     */
    public static boolean isSet(SetCard card1, SetCard card2, SetCard card3) {
        // ודא שאין קלפי null
        if (card1 == null || card2 == null || card3 == null) {
            return false;
        }

        // ודא שאין קלפים כפולים
        // למרות שהלוגיקה של בחירת קלפים באדפטר אמורה למנוע זאת,
        // זו הגנה נוספת.
        if (card1.equals(card2) || card1.equals(card3) || card2.equals(card3)) {
            return false;
        }

        // בודק כל מאפיין בנפרד
        boolean shapeCheck = checkProperty(card1.getShape(), card2.getShape(), card3.getShape());
        boolean colorCheck = checkProperty(card1.getColor(), card2.getColor(), card3.getColor());
        boolean shadingCheck = checkProperty(card1.getShading(), card2.getShading(), card3.getShading());
        boolean numberCheck = checkProperty(card1.getNumber(), card2.getNumber(), card3.getNumber());

        // אם כל הבדיקות עברו, זהו סט
        return shapeCheck && colorCheck && shadingCheck && numberCheck;
    }

    /**
     * מתודת עזר גנרית לבדיקת מאפיין בודד.
     * היא מקבלת שלושה ערכים מטיפוס T (שייצג Enum כמו Shape, Color וכו'),
     * ובודקת אם הם כולם זהים או כולם שונים.
     *
     * @param <T> סוג המאפיין (לדוגמה, SetCard.Shape, SetCard.Color).
     * @param val1 הערך של המאפיין בקלף הראשון.
     * @param val2 הערך של המאפיין בקלף השני.
     * @param val3 הערך של המאפיין בקלף השלישי.
     * @return true אם הערכים כולם זהים או כולם שונים, false אחרת.
     */
    private static <T> boolean checkProperty(T val1, T val2, T val3) {
        // האם כולם זהים?
        if (val1.equals(val2) && val2.equals(val3)) {
            return true;
        }

        // האם כולם שונים?
        // דרך יעילה לבדוק אם כולם שונים היא להכניס אותם ל-HashSet
        // ולבדוק אם גודל ה-HashSet הוא 3.
        Set<T> uniqueValues = new HashSet<>(Arrays.asList(val1, val2, val3));
        return uniqueValues.size() == 3;
    }


    /**
     * מוצא את כל הסטים החוקיים מתוך רשימת קלפים נתונה.
     *
     * @param cards רשימת הקלפים שעל הלוח.
     * @return רשימה של רשימות, כאשר כל רשימה פנימית מכילה 3 קלפים המהווים סט.
     */
    public static List<List<SetCard>> findSetsOnBoard(List<SetCard> cards) {
        List<List<SetCard>> foundSets = new ArrayList<>();
        int n = cards.size();

        // בודק כל קומבינציה של 3 קלפים
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                for (int k = j + 1; k < n; k++) {
                    SetCard card1 = cards.get(i);
                    SetCard card2 = cards.get(j);
                    SetCard card3 = cards.get(k);

                    if (isSet(card1, card2, card3)) {
                        foundSets.add(List.of(card1, card2, card3));
                    }
                }
            }
        }
        return foundSets;
    }
}