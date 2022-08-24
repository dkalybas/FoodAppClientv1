package com.example.newfoodapp.ui.restaurant;

import android.view.View;

import com.example.newfoodapp.Callback.IRecyclerClickListener;
import com.example.newfoodapp.Callback.IRestaurantCallbackListener;
import com.example.newfoodapp.Common.Common;
import com.example.newfoodapp.Model.RestaurantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RestaurantViewModel extends ViewModel implements IRestaurantCallbackListener {
    // TODO: Implement the ViewModel
        private MutableLiveData<List<RestaurantModel>> restaurantListMutable ;
        private MutableLiveData<String> messageError = new MutableLiveData<>();
        private IRestaurantCallbackListener listener;


    public RestaurantViewModel() {

        listener = this;
    }


    public MutableLiveData<List<RestaurantModel>> getRestaurantListMutable() {
        if (restaurantListMutable==null){
            
            restaurantListMutable = new MutableLiveData<>();
            loadRestaurantFromFirebase();
            
        }
        
        
        return restaurantListMutable;
    }

    private void loadRestaurantFromFirebase() {

        List<RestaurantModel> restaurantModels = new ArrayList<>();
        DatabaseReference restaurantRef = FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF);

        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    for(DataSnapshot restaurantSnapShot:snapshot.getChildren()){

                        RestaurantModel restaurantModel = restaurantSnapShot.getValue(RestaurantModel.class);
                        restaurantModel.setUid(restaurantSnapShot.getKey());
                        restaurantModels.add(restaurantModel);
                    }
                    if (restaurantModels.size() > 0 )
                        listener.onRestaurantLoadSuccess(restaurantModels);
                    else
                        listener.onRestaurantLoadFailed(" Restaurant List Empty");

                }else{

                    listener.onRestaurantLoadFailed("Restaurant List does not exist ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList) {

        restaurantListMutable.setValue(restaurantModelList);

    }

    @Override
    public void onRestaurantLoadFailed(String message) {

        messageError.setValue(message);


    }
}