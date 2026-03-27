package com.example.interviewace.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.interviewace.Repository.InterviewRepository;
import com.example.interviewace.model.SessionItem;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProgressViewModel extends ViewModel {

   private final InterviewRepository repository;
   // Mutable Live data
    private final MutableLiveData<List<SessionItem>> sessions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProgressViewModel() {
        repository =  new InterviewRepository();
        LoadSessions();
    }

    private void LoadSessions() {
        String userId = FirebaseAuth.getInstance().getUid();

        if(userId == null){
            errorMessage.setValue("User Not Logged in");
            return;
        }
        isLoading.setValue(true);
        repository.getSessions(userId, new InterviewRepository.RepositoryCallback<List<SessionItem>>() {
            @Override
            public void onSuccess(List<SessionItem> data) {
                isLoading.setValue(false);
                // sorting sessions by date in ascending order
                Collections.reverse(data);
                sessions.setValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                     isLoading.setValue(false);
                     errorMessage.setValue(e.getMessage());

            }
        });

    }
    public LiveData<List<SessionItem>> getSessions(){
        return sessions;
    }
    public LiveData<Boolean> getIsLoading(){
        return isLoading;
    }
    public LiveData<String> getErrorMessage(){
        return errorMessage;
    }

}
