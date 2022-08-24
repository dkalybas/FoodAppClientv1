package com.example.newfoodapp.ui.view_orders;

import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newfoodapp.Adapter.MyOrdersAdapter;
import com.example.newfoodapp.Callback.ILoadOrderCallbackListener;
import com.example.newfoodapp.Common.Common;
import com.example.newfoodapp.Common.MySwipeHelper;
import com.example.newfoodapp.Database.CartDataSource;
import com.example.newfoodapp.Database.CartDatabase;
import com.example.newfoodapp.Database.CartItem;
import com.example.newfoodapp.Database.LocalCartDatabaseSource;
import com.example.newfoodapp.EventBus.CounterCartEvent;
import com.example.newfoodapp.EventBus.MenuItemBack;
import com.example.newfoodapp.Model.Order;
import com.example.newfoodapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {

    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    private Unbinder unbinder;
    private AlertDialog dialog;

    private ViewOrdersViewModel viewOrdersViewModel;
    private ILoadOrderCallbackListener listener;

    public static ViewOrdersFragment newInstance() {
        return new ViewOrdersFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        viewOrdersViewModel = ViewModelProviders.of(this).get(ViewOrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_orders,container,false);
        unbinder = ButterKnife.bind(this,root);

        initViews(root);
        loadOrdersFromFirebase();

        viewOrdersViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(),orderList -> {

            MyOrdersAdapter adapter = new MyOrdersAdapter(getContext(),orderList);
            recycler_orders.setAdapter(adapter);


        });



        return root;
    }

    private void loadOrdersFromFirebase() {
        List<Order> orderList = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentRestaurant.getUid())
                .child(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot orderSnapShot:snapshot.getChildren()){

                            Order order = orderSnapShot.getValue(Order.class);
                            order.setOrderNumber(orderSnapShot.getKey());
                            orderList.add(order);

                        }

                        listener.onLoadOrderSuccess(orderList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    private void initViews(View root) {

        cartDataSource = new LocalCartDatabaseSource(CartDatabase.getInstance(getContext()).cartDAO());


        listener = this;


        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_orders,250) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Cancel Order",30,0, Color.parseColor("#FF3C30"),

                        pos -> {

                          Order orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);
                         if (orderModel.getOrderStatus()==0){
                            //  if (true){
               androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
               builder.setTitle("Cancel Order ")
                       .setMessage("Do you really want to cancel this order ?    ")
                       .setNegativeButton("NO", (dialogInterface, which) -> dialogInterface.dismiss())
                       .setPositiveButton("YES", (dialogInterface, which) -> {
                           Map<String ,Object> update_data = new HashMap<>();
                           update_data.put("orderStatus",-1); // Canceling order
                           FirebaseDatabase.getInstance()
                                   .getReference(Common.ORDER_REF)
                                   .child(orderModel.getOrderNumber())
                                   .updateChildren(update_data)
                                   .addOnFailureListener(e -> Toast.makeText(getContext(), "failed!!! "+e.getMessage(),Toast.LENGTH_SHORT).show())
                                   .addOnSuccessListener(aVoid -> {

                                       orderModel.setOrderStatus(-1);
                                       ((MyOrdersAdapter)recycler_orders.getAdapter()).setItemAtPosition(pos,orderModel);
                                       recycler_orders.getAdapter().notifyItemChanged(pos);
                                       Toast.makeText(getContext(),"Cancel order successfully",Toast.LENGTH_SHORT).show();


                                   })     ;
                       });
                        androidx.appcompat.app.AlertDialog dialog = builder.create();
                        dialog.show();

                          }else {

                              Toast.makeText(getContext(),new StringBuilder("Your order was changed to ")
                              .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                              .append("so you cannot cancel it !!!"),Toast.LENGTH_SHORT).show();

                          }

                        }));

                buf.add(new MyButton(getContext(),"Repeat Order",30,0, Color.parseColor("#5d4037"),

                        pos -> {

                            Order orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);

                          //  Toast.makeText(getContext(), "Repeat Order Click!!!", Toast.LENGTH_SHORT).show();

                            dialog.show();

                            //We clear all the item on cart 1st
                            cartDataSource.cleanCart(Common.currentUser.getUid(),
                                    Common.currentRestaurant.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                                //After we clean cart we add new items

                                            CartItem[] cartItems = orderModel.getCartItemList().toArray(new CartItem[orderModel.
                                                    getCartItemList().size()]);

                                            // Adding new
                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(()->    {
                                                    dialog.dismiss();
                                                Toast.makeText(getContext(), "All items added successfully", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));// Count fab button here

                                              },throwable -> {

                                                dialog.dismiss();
                                                Toast.makeText(getContext(), " "+throwable.getMessage() , Toast.LENGTH_SHORT).show();

                                            })
                                            );


                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                            dialog.dismiss();
                                            Toast.makeText(getContext(),"[Error] "+e.getMessage(),Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }));



            }
        };
      //  mySwipeHelper();

           // mySwiperHelper.
    }


    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {
        dialog.dismiss();
        viewOrdersViewModel.setMutableLiveDataOrderList(orderList);


    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {


        compositeDisposable.clear();

        super.onStop();


    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
