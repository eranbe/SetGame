package com.eranbe.setgame.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateReplacer {

    // דפוס שמתאים ל-${key}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)\\}");

    /**
     * מחליף את המפתחות שבתוך המחרוזת לפי הערכים שבמפה.
     * אם מפתח לא נמצא במפה – הוא נשאר כמות שהוא.
     */
    public static String replacePlaceholders(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1); // לדוגמה "name"
            String replacement = values.getOrDefault(key, matcher.group(0)); // אם אין – השאר כמות שהוא
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
