package com.example.newfoodapp.Database;

import java.util.List;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {


              Flowable<List<CartItem>> getAllCart(String uid,String restaurantId);
              Single<Integer> countItemInCart(String uid,String restaurantId);
              Single<Double> sumPriceInCart(String uid,String restaurantId);
              Single<CartItem> getItemCart(String foodId,String uid,String restaurantId);
              Completable insertOrReplaceAll(CartItem... cartItems);

              Single<Integer> updateCartItems(CartItem cartItem);

              Single<Integer> deleteCartItems(CartItem cartItem);

                Single<Integer> cleanCart(String  uid,String restaurantId);

                Single<CartItem> getItemWithAllOptionCart(String uid ,String categoryId , String  foodId,String foodSize ,String foodAddon,String restaurantId);


}
