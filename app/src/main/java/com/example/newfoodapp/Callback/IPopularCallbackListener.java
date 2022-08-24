package com.example.newfoodapp.Callback;

import com.example.newfoodapp.Model.PopularCategoryModel;

import java.util.List;

public interface IPopularCallbackListener {


    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels);
    void onPopularLoadFailed(String message);





}
