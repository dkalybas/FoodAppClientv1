package com.example.newfoodapp.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalCartDatabaseSource  implements  CartDataSource{


    private CartDAO cartDAO;

    public LocalCartDatabaseSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Flowable<List<CartItem>> getAllCart(String uid,String restaurantId) {
        return cartDAO.getAllCart(uid,restaurantId);
    }

    @Override
    public Single<Integer> countItemInCart(String uid,String restaurantId) {
        return cartDAO.countItemInCart(uid,restaurantId);
    }

    @Override
    public Single<Double> sumPriceInCart(String uid,String restaurantId) {
        return cartDAO.sumPriceInCart(uid,restaurantId);
    }

    @Override
    public Single<CartItem> getItemCart(String foodId, String uid,String restaurantId) {
        return cartDAO.getItemCart(foodId,uid,restaurantId);
    }

    @Override
    public Completable insertOrReplaceAll(CartItem... cartItems) {
        return cartDAO.insertOrReplaceAll(cartItems);
    }

    @Override
    public Single<Integer> updateCartItems(CartItem cartItem) {
        return cartDAO.updateCartItems(cartItem);
    }

    @Override
    public Single<Integer> deleteCartItems(CartItem cartItem) {
        return cartDAO.deleteCartItems(cartItem);
    }

    @Override
    public Single<Integer> cleanCart(String uid,String restaurantId) {
        return cartDAO.cleanCart(uid,restaurantId);
    }

    @Override
    public Single<CartItem> getItemWithAllOptionCart(String uid,String categoryId, String foodId, String foodSize, String foodAddon,String restaurantId) {
        return cartDAO.getItemWithAllOptionCart(uid,categoryId,foodId,foodSize,foodAddon,restaurantId);
    }


}
