package com.example.interviewace.model;

import java.util.List;

public class FeedbackResponse {
    private int overallScore;
    private int technicalAccuracy;
    private int communication;
    private int confidence;
    private int completeness;
    private int examples;
    private String resultMessage;
    private List<String> correctPoints;
    private List<String> missingPoints;
    private String suggestedAnswer;

    public FeedbackResponse() {
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public int getTechnicalAccuracy() {
        return technicalAccuracy;
    }

    public void setTechnicalAccuracy(int technicalAccuracy) {
        this.technicalAccuracy = technicalAccuracy;
    }

    public int getCommunication() {
        return communication;
    }

    public void setCommunication(int communication) {
        this.communication = communication;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public int getCompleteness() {
        return completeness;
    }

    public void setCompleteness(int completeness) {
        this.completeness = completeness;
    }

    public int getExamples() {
        return examples;
    }

    public void setExamples(int examples) {
        this.examples = examples;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public List<String> getCorrectPoints() {
        return correctPoints;
    }

    public void setCorrectPoints(List<String> correctPoints) {
        this.correctPoints = correctPoints;
    }

    public List<String> getMissingPoints() {
        return missingPoints;
    }

    public void setMissingPoints(List<String> missingPoints) {
        this.missingPoints = missingPoints;
    }

    public String getSuggestedAnswer() {
        return suggestedAnswer;
    }

    public void setSuggestedAnswer(String suggestedAnswer) {
        this.suggestedAnswer = suggestedAnswer;
    }
}
