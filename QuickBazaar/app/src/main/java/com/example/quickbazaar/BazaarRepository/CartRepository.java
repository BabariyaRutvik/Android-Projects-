package com.example.quickbazaar.BazaarRepository;

import androidx.lifecycle.MutableLiveData;

import com.example.quickbazaar.BazaarModel.CartItem;
import com.example.quickbazaar.BazaarModel.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<List<CartItem>> cartItemLiveData;

    public CartRepository() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        cartItemLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<CartItem>> getCartItem() {
        String userId = firebaseAuth.getUid();

        if (userId != null) {
            firestore.collection("users")
                    .document(userId)
                    .collection("cart")
                    .addSnapshotListener(((value, error) -> {
                        if (error != null) {
                            return;
                        }
                        List<CartItem> items = new ArrayList<>();
                        if (value != null) {
                            for (QueryDocumentSnapshot documentSnapshot : value) {
                                CartItem item = documentSnapshot.toObject(CartItem.class);
                                items.add(item);
                            }
                        }
                        cartItemLiveData.postValue(items);
                    }));
        }
        return cartItemLiveData;
    }

    public void addToCart(Product product) {
        String userId = firebaseAuth.getUid();
        if (userId == null) {
            return;
        }
        DocumentReference cartReference = firestore.collection("users")
                .document(userId)
                .collection("cart")
                .document(product.getId());

        cartReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                CartItem existingItem = documentSnapshot.toObject(CartItem.class);
                if (existingItem != null) {
                    existingItem.setQuantity(existingItem.getQuantity() + 1);
                    cartReference.set(existingItem);
                } else {
                    CartItem newItem = new CartItem(product, 1);
                    cartReference.set(newItem);
                }
            } else {
                CartItem newItem = new CartItem(product, 1);
                cartReference.set(newItem);
            }
        });
    }

    public void UpdateQuantity(String productId, int newQuantity) {
        String userId = firebaseAuth.getUid();
        if (userId == null) {
            return;
        }

        DocumentReference itemRef = firestore.collection("users")
                .document(userId)
                .collection("cart")
                .document(productId);

        if (newQuantity <= 0) {
            itemRef.delete();
        } else {
            itemRef.update("quantity", newQuantity);
        }
    }

    public void clearCart() {
        String userId = firebaseAuth.getUid();
        if (userId == null) {
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        documentSnapshot.getReference().delete();
                    }
                });
    }
}
