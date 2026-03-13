package com.example.quickbazaar.BazaarViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quickbazaar.BazaarModel.CartItem;
import com.example.quickbazaar.BazaarModel.Product;
import com.example.quickbazaar.BazaarRepository.CartRepository;

import java.util.List;

public class CartViewModel extends ViewModel {
    private final CartRepository cartRepository;
    private final LiveData<List<CartItem>>cartItem;
    private final MutableLiveData<Double> totalAmount;


    // constructor
    public CartViewModel(){
        cartRepository = new CartRepository();
        cartItem = cartRepository.getCartItem();
        totalAmount = new MutableLiveData<>();
    }
    public LiveData<List<CartItem>>getCartItem(){
        return cartItem;
    }
    public LiveData<Double>getTotalAmount(){
        return totalAmount;

    }
    public void CalculateTotalList(List<CartItem> items){
        double total = 0.0;
        for (CartItem item : items){
            total += item.getTotalPrice();
        }
        totalAmount.setValue(total);
    }
    // add to cart
    public void AddToCart(Product product){
        cartRepository.addToCart(product);
    }
    // updating the Quantity
    public void UpdateQuantity(String productId , int newQuantity){
        cartRepository.UpdateQuantity(productId, newQuantity);
    }

    // clear or delete the cart item
    public void ClearCart(){
        cartRepository.clearCart();
    }


}
