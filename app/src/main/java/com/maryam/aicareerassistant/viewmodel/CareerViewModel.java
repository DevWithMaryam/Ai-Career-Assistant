package com.maryam.aicareerassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.maryam.aicareerassistant.repository.GeminiRepository;
import com.maryam.aicareerassistant.utils.Resource;

/**
 * Shared ViewModel for all AI-powered features:
 * Resume Analysis, Skill Gap, Roadmap, Interview Questions.
 * "Shared" means it survives across fragments within the same Activity.
 */
public class CareerViewModel extends ViewModel {

    private final GeminiRepository geminiRepository;

    private final MutableLiveData<Resource<String>> analysisResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> skillGapResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> roadmapResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> interviewResult = new MutableLiveData<>();

    // Holds current session data (passed between fragments)
    private String currentResumeText = null;
    private String currentTargetCareer = null;
    private String currentMissingSkills = null;

    public CareerViewModel() {
        geminiRepository = new GeminiRepository();
    }

    // --- Getters for LiveData ---
    public LiveData<Resource<String>> getAnalysisResult() { return analysisResult; }
    public LiveData<Resource<String>> getSkillGapResult() { return skillGapResult; }
    public LiveData<Resource<String>> getRoadmapResult() { return roadmapResult; }
    public LiveData<Resource<String>> getInterviewResult() { return interviewResult; }

    // --- Session data getters/setters ---
    public String getCurrentResumeText() { return currentResumeText; }
    public void setCurrentResumeText(String text) { this.currentResumeText = text; }

    public String getCurrentTargetCareer() { return currentTargetCareer; }
    public void setCurrentTargetCareer(String career) { this.currentTargetCareer = career; }

    public String getCurrentMissingSkills() { return currentMissingSkills; }
    public void setCurrentMissingSkills(String skills) { this.currentMissingSkills = skills; }

    // --- AI Actions ---
    public void analyzeResume(String resumeText) {
        if (resumeText == null || resumeText.trim().isEmpty()) {
            analysisResult.setValue(Resource.error(
                    "No resume text found. Please upload your resume first."));
            return;
        }
        currentResumeText = resumeText;
        geminiRepository.analyzeResume(resumeText, analysisResult);
    }

    public void getSkillGap(String targetCareer) {
        if (currentResumeText == null || currentResumeText.trim().isEmpty()) {
            skillGapResult.setValue(Resource.error(
                    "No resume text found. Please upload your resume first."));
            return;
        }
        currentTargetCareer = targetCareer;
        geminiRepository.getSkillGap(currentResumeText, targetCareer, skillGapResult);
    }

    public void getRoadmap() {
        if (currentTargetCareer == null || currentMissingSkills == null) {
            roadmapResult.setValue(Resource.error(
                    "Please complete Skill Gap analysis first."));
            return;
        }
        geminiRepository.getRoadmap(currentTargetCareer, currentMissingSkills, roadmapResult);
    }

    public void getInterviewQuestions() {
        if (currentResumeText == null || currentTargetCareer == null) {
            interviewResult.setValue(Resource.error(
                    "Please complete Resume Analysis and Career Selection first."));
            return;
        }
        geminiRepository.getInterviewQuestions(
                currentTargetCareer, currentResumeText, interviewResult);
    }
}