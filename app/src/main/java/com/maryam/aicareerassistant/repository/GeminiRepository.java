package com.maryam.aicareerassistant.repository;

import androidx.lifecycle.MutableLiveData;

import com.maryam.aicareerassistant.BuildConfig;
import com.maryam.aicareerassistant.model.GeminiRequest;
import com.maryam.aicareerassistant.model.GeminiResponse;
import com.maryam.aicareerassistant.network.ApiClient;
import com.maryam.aicareerassistant.network.ApiService;
import com.maryam.aicareerassistant.utils.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Single source of truth for all Gemini AI API calls.
 * Converts raw API responses into Resource<String> LiveData events.
 */
public class GeminiRepository {

    private final ApiService apiService;
    private final String apiKey;

    public GeminiRepository() {
        apiService = ApiClient.getInstance();
        apiKey = BuildConfig.GEMINI_API_KEY;
    }

    /**
     * Core method — sends any prompt to Gemini and posts result to LiveData.
     * All feature-specific methods (analyzeResume, getSkillGap, etc.) call this.
     */
    public void generate(String prompt, MutableLiveData<Resource<String>> result) {
        result.setValue(Resource.loading());

        GeminiRequest request = new GeminiRequest(prompt);

        apiService.generateContent(apiKey, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String text = response.body().getFirstText();
                    if (text != null && !text.isEmpty()) {
                        result.postValue(Resource.success(text));
                    } else {
                        result.postValue(Resource.error("Empty response from AI. Please try again."));
                    }
                } else {
                    String errorMsg = "AI request failed";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = "AI error: " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    result.postValue(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                result.postValue(Resource.error("Network error: " + t.getMessage()));
            }
        });
    }

    public void analyzeResume(String resumeText, MutableLiveData<Resource<String>> result) {
        generate(com.maryam.aicareerassistant.utils.PromptBuilder
                .buildResumeAnalysisPrompt(resumeText), result);
    }

    public void getSkillGap(String resumeText, String targetCareer,
                            MutableLiveData<Resource<String>> result) {
        generate(com.maryam.aicareerassistant.utils.PromptBuilder
                .buildSkillGapPrompt(resumeText, targetCareer), result);
    }

    public void getRoadmap(String targetCareer, String missingSkills,
                           MutableLiveData<Resource<String>> result) {
        generate(com.maryam.aicareerassistant.utils.PromptBuilder
                .buildRoadmapPrompt(targetCareer, missingSkills), result);
    }

    public void getInterviewQuestions(String targetCareer, String resumeText,
                                      MutableLiveData<Resource<String>> result) {
        generate(com.maryam.aicareerassistant.utils.PromptBuilder
                .buildInterviewPrompt(targetCareer, resumeText), result);
    }
}