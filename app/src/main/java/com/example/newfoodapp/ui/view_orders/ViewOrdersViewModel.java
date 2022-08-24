package com.example.newfoodapp.ui.view_orders;

import com.example.newfoodapp.Model.Order;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewOrdersViewModel extends ViewModel {

    private MutableLiveData<List<Order>> mutableLiveDataOrderList ;


    public ViewOrdersViewModel() {
        mutableLiveDataOrderList = new MutableLiveData<>();


    }

    public MutableLiveData<List<Order>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<Order> orderList) {
     mutableLiveDataOrderList.setValue(orderList);


    }
}
