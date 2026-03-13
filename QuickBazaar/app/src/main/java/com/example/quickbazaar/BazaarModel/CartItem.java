package com.example.quickbazaar.BazaarModel;

import java.io.Serializable;

public class CartItem implements Serializable {
   private Product product;
   private int quantity;


   public CartItem(){

   }
   // parameterized Constructor
    public CartItem(Product product , int quantity){
       this.product = product;
       this.quantity = quantity;

    }
    // getter and setter

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public double getTotalPrice(){
       if (product != null){
           return product.getDiscountPrice() * quantity;

       }
       return 0.0;
    }
}
