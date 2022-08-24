package com.example.newfoodapp.Callback;

import com.example.newfoodapp.Database.CartItem;
import com.example.newfoodapp.Model.CategoryModel;
import com.example.newfoodapp.Model.FoodModel;

public interface ISearchCategoryCallbackListener {

    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotFound(String message);
}
