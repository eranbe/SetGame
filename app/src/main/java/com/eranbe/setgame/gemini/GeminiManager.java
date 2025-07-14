package com.eranbe.setgame.gemini;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ImagePart;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.TextPart;

import com.eranbe.setgame.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import kotlin.Result;
import kotlin.Result.Failure;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GeminiManager {
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static GeminiManager instance;
    private final GenerativeModel gemini;

    private GeminiManager() {
        //init
        gemini = new GenerativeModel(
                "gemini-2.5-flash",
                API_KEY
        );
    }

    public static GeminiManager getInstance() {
        if (null == instance) {
            instance = new GeminiManager();
        }
        return instance;
    }


    public void sendMessage(String prompt, GeminiCallback callback) {
        gemini.generateContent(prompt,
                new Continuation<GenerateContentResponse>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object result) {
                        if (result instanceof Failure) {
                            callback.onError(((Failure) result).exception);
                        } else {
                            callback.onSuccess(((GenerateContentResponse) result).getText());
                        }
                    }
                }
        );
    }

    public void sendMessageWithPhoto(String prompt, Bitmap photo, GeminiCallback callback) {
        List<Part> parts = new ArrayList<Part>();
        parts.add(new TextPart(prompt));
        parts.add(new ImagePart(photo));
        Content[] content = new Content[1];
        content[0] = new Content(parts);

        gemini.generateContent(content,
                new Continuation<GenerateContentResponse>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object result) {
                        if (result instanceof Failure) {
                            callback.onError(((Failure) result).exception);
                        } else {
                            callback.onSuccess(((GenerateContentResponse) result).getText());
                        }
                    }
                }
        );
    }
}
