package com.example.newfoodapp.Callback;

import com.example.newfoodapp.Model.BestDealModel;
import com.example.newfoodapp.Model.PopularCategoryModel;

import java.util.List;

public interface IBestDealCallbackListener {

    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);






}
