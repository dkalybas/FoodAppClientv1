package com.example.newfoodapp.Callback;

import com.example.newfoodapp.Model.Order;

public interface ILoadTimeFromFirebaseListener {

    void onLoadTimeSuccess(Order order, long estimateTimeInMs);
    void onLoadTimeFailed(String message);

}
