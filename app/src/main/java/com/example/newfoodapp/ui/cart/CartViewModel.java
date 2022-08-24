package com.example.newfoodapp.ui.cart;

import android.content.Context;

import com.example.newfoodapp.Common.Common;
import com.example.newfoodapp.Database.CartDataSource;
import com.example.newfoodapp.Database.CartDatabase;
import com.example.newfoodapp.Database.CartItem;
import com.example.newfoodapp.Database.LocalCartDatabaseSource;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private CartDataSource cartDataSource;

    private MutableLiveData<List<CartItem>>  mutableLiveDataCartItems;

    public CartViewModel() {
        compositeDisposable = new CompositeDisposable();

    }

    public void initCartDataSource(Context context){

        cartDataSource = new LocalCartDatabaseSource(CartDatabase.getInstance(context).cartDAO());

    }

    public void onStop(){

        compositeDisposable.clear();
    }


    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItems() {
        if (mutableLiveDataCartItems==null)
            mutableLiveDataCartItems = new MutableLiveData<>();

        getAllCartItems();



        return mutableLiveDataCartItems;
    }

    private void getAllCartItems() {

        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(),
                Common.currentRestaurant.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {

                    mutableLiveDataCartItems.setValue(cartItems);

                }, throwable -> {
                        mutableLiveDataCartItems.setValue(null);
                }));

    }
}
