package com.example.newfoodapp.Callback;

import com.example.newfoodapp.Model.Order;

import java.util.List;

public interface ILoadOrderCallbackListener {

        void onLoadOrderSuccess(List<Order> orderList);
        void onLoadOrderFailed(String message);

}
