package com.example.interviewace.Repository;


import androidx.annotation.NonNull;

import com.example.interviewace.model.AnswerModel;
import com.example.interviewace.model.NotificationItem;
import com.example.interviewace.model.QuestionItem;
import com.example.interviewace.model.RoleItem;
import com.example.interviewace.model.SessionItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class InterviewRepository {
    private final FirebaseFirestore firestore;
    private final CollectionReference questionCollections;
    private final CollectionReference answerCollections;
    private final CollectionReference sessionCollections;
    private final CollectionReference roleCollections;
    private final CollectionReference notificationCollections;

    public InterviewRepository() {
        firestore = FirebaseFirestore.getInstance();
        questionCollections = firestore.collection("questions");
        answerCollections = firestore.collection("answers");
        sessionCollections = firestore.collection("sessions");
        roleCollections = firestore.collection("Role");
        notificationCollections = firestore.collection("notifications");
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }

    public void getRoles(RepositoryCallback<List<RoleItem>> callback) {
        roleCollections.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<RoleItem> roles = task.getResult().toObjects(RoleItem.class);
                    callback.onSuccess(roles);
                } else {
                    callback.onFailure(task.getException() != null ? task.getException() : new Exception("Failed to Fetch Roles"));
                }
            }
        });
    }

    public void getQuestions(String role, String difficulty, RepositoryCallback<List<QuestionItem>> callback) {
        String queryRole = role.toLowerCase().trim();
        
        // Clean the role name for robust matching (remove slashes, spaces, hyphens)
        String roleClean = queryRole.replace("/", "").replace(" ", "").replace("-", "");

        if (roleClean.contains("uiux")) {
            queryRole = "uiux";
        } else if (queryRole.contains("product")) {
            queryRole = "pm";
        } else if (queryRole.contains("data")) {
            queryRole = "datascientist";
        } else if (queryRole.contains("android")) {
            queryRole = "android";
        } else {
            // Fallback: use first word for other roles like "Java", "Backend", etc.
            queryRole = queryRole.split(" ")[0];
        }

        questionCollections
                .whereEqualTo("role", queryRole)
                .whereEqualTo("difficulty", difficulty.toLowerCase())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<QuestionItem> questions = task.getResult().toObjects(QuestionItem.class);
                            callback.onSuccess(questions);
                        } else {
                            callback.onFailure(task.getException() != null ? task.getException() : new Exception("Failed to Fetch Question"));
                        }
                    }
                });
    }

    public void saveAnswer(AnswerModel answerModel, RepositoryCallback<Void> callback) {
        answerCollections.add(answerModel)
                .addOnSuccessListener(documentReference -> {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    public void saveSession(SessionItem sessionItem, RepositoryCallback<Void> callback) {
        sessionCollections.add(sessionItem)
                .addOnSuccessListener(documentReference -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void getSessions(String userId, RepositoryCallback<List<SessionItem>> callback) {
        sessionCollections
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<SessionItem> sessions = task.getResult().toObjects(SessionItem.class);
                        callback.onSuccess(sessions);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException() : new Exception("Failed to fetch sessions"));
                    }
                });
    }

    public void getUnreadNotificationsCount(String userId, RepositoryCallback<Integer> callback) {
        notificationCollections
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        callback.onSuccess(task.getResult().size());
                    } else {
                        callback.onSuccess(0);
                    }
                });
    }

    public void SaveAnswerToRoom(AnswerModel model) {
        // Implementation for local storage
    }
}
