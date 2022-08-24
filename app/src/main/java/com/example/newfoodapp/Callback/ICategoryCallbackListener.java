package com.example.newfoodapp.Callback;

import com.example.newfoodapp.Model.BestDealModel;
import com.example.newfoodapp.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {

    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);




}
