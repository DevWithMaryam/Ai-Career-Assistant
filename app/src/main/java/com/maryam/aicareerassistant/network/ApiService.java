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
 */
public interface ApiService {

    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    Call<GeminiResponse> generateContent(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}