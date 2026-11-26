package com.maryam.aicareerassistant.network;

import com.maryam.aicareerassistant.model.GeminiRequest;
import com.maryam.aicareerassistant.model.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit interface for Gemini API.
 * Base URL: https://generativelanguage.googleapis.com/
 * "gemini-flash-latest" always points to the current Flash model, so the
 * app does not break when Google retires an older version.
 */
public interface ApiService {

    @POST("v1beta/models/gemini-flash-latest:generateContent")
    Call<GeminiResponse> generateContent(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}