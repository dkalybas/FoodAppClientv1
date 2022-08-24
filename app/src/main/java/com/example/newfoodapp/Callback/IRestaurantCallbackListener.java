package com.example.newfoodapp.Callback;

import com.example.newfoodapp.Model.CategoryModel;
import com.example.newfoodapp.Model.RestaurantModel;

import java.util.List;

public interface IRestaurantCallbackListener {

    void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList);
    void onRestaurantLoadFailed(String message);




}
