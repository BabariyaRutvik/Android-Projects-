package com.example.interviewace.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.interviewace.Repository.InterviewRepository;
import com.example.interviewace.model.AnswerModel;
import com.example.interviewace.model.QuestionItem;
import com.example.interviewace.model.RoleItem;
import com.example.interviewace.model.SessionItem;

import java.util.List;

public class InterviewViewModel extends ViewModel {

    private final InterviewRepository repository;
    private final MutableLiveData<List<QuestionItem>> questions = new MutableLiveData<>();
    private final MutableLiveData<Integer> customQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAnswerSaved = new MutableLiveData<>();
    private final MutableLiveData<List<SessionItem>> sessions = new MutableLiveData<>();
    private final MutableLiveData<List<RoleItem>> roles = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadNotificationsCount = new MutableLiveData<>(0);

    public InterviewViewModel() {
        repository = new InterviewRepository();
    }

    public LiveData<List<QuestionItem>> getQuestions() {
        return questions;
    }

    public LiveData<Integer> getCustomQuestionIndex() {
        return customQuestionIndex;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsAnswerSaved() {
        return isAnswerSaved;
    }

    public LiveData<List<SessionItem>> getSessions() {
        return sessions;
    }

    public LiveData<List<RoleItem>> getRoles() {
        return roles;
    }

    public LiveData<Integer> getUnreadNotificationsCount() {
        return unreadNotificationsCount;
    }

    public void setCustomQuestionIndex(int index) {
        customQuestionIndex.setValue(index);
    }

    public void LoadQuestions(String role, String difficulty) {
        isLoading.setValue(true);
        repository.getQuestions(role, difficulty,
                new InterviewRepository.RepositoryCallback<List<QuestionItem>>() {
                    @Override
                    public void onSuccess(List<QuestionItem> data) {
                        isLoading.setValue(false);
                        questions.setValue(data);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        isLoading.setValue(false);
                        errorMessage.setValue(e.getMessage());
                    }
                });
    }

    public void NextQuestion() {
        Integer current = customQuestionIndex.getValue();
        List<QuestionItem> questionItems = questions.getValue();

        if (current != null && questionItems != null && current < questionItems.size() - 1) {
            customQuestionIndex.setValue(current + 1);
        }
    }

    public void submitAnswer(AnswerModel model) {
        isLoading.setValue(true);
        repository.saveAnswer(model, new InterviewRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isLoading.setValue(false);
                isAnswerSaved.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                isAnswerSaved.setValue(false);
                errorMessage.setValue("Failed to save answer: " + e.getMessage());
            }
        });
    }

    public void saveSession(SessionItem sessionItem) {
        repository.saveSession(sessionItem, new InterviewRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Session saved successfully
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to save session: " + e.getMessage());
            }
        });
    }

    public void loadSessions(String userId) {
        isLoading.setValue(true);
        repository.getSessions(userId, new InterviewRepository.RepositoryCallback<List<SessionItem>>() {
            @Override
            public void onSuccess(List<SessionItem> data) {
                isLoading.setValue(false);
                sessions.setValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load sessions: " + e.getMessage());
            }
        });
    }

    public void loadRoles() {
        isLoading.setValue(true);
        repository.getRoles(new InterviewRepository.RepositoryCallback<List<RoleItem>>() {
            @Override
            public void onSuccess(List<RoleItem> data) {
                isLoading.setValue(false);
                roles.setValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load roles: " + e.getMessage());
            }
        });
    }

    public void loadUnreadNotificationsCount(String userId) {
        repository.getUnreadNotificationsCount(userId, new InterviewRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                unreadNotificationsCount.setValue(count);
            }

            @Override
            public void onFailure(Exception e) {
                // Silently handle error for count
            }
        });
    }
}
