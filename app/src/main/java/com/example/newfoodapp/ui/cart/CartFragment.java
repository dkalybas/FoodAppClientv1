package com.example.newfoodapp.ui.cart;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newfoodapp.Adapter.MyCartAdapter;
import com.example.newfoodapp.BuildConfig;
import com.example.newfoodapp.Callback.ILoadTimeFromFirebaseListener;
import com.example.newfoodapp.Callback.ISearchCategoryCallbackListener;
import com.example.newfoodapp.Common.Common;
import com.example.newfoodapp.Common.MySwipeHelper;
import com.example.newfoodapp.Database.CartDataSource;
import com.example.newfoodapp.Database.CartDatabase;
import com.example.newfoodapp.Database.CartItem;
import com.example.newfoodapp.Database.LocalCartDatabaseSource;
import com.example.newfoodapp.EventBus.CounterCartEvent;
import com.example.newfoodapp.EventBus.HideFABCart;
import com.example.newfoodapp.EventBus.MenuItemBack;
import com.example.newfoodapp.EventBus.UpdateItemInCart;
import com.example.newfoodapp.Model.AddonModel;
import com.example.newfoodapp.Model.CategoryModel;
import com.example.newfoodapp.Model.FCMSendData;
import com.example.newfoodapp.Model.FoodModel;
import com.example.newfoodapp.Model.Order;
import com.example.newfoodapp.Model.SizeModel;
import com.example.newfoodapp.R;
import com.example.newfoodapp.Remote.IFCMService;
import com.example.newfoodapp.Remote.RetroFitFCMClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener, ISearchCategoryCallbackListener, TextWatcher {

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    private BottomSheetDialog addonBottomSheetDialog;
    private ChipGroup chip_group_addon,chip_group_user_selected_addon;
    private EditText edt_search ;


    private ISearchCategoryCallbackListener searchFoodCallbackListener;

    private CompositeDisposable  compositeDisposable = new CompositeDisposable();
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;



    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    IFCMService ifcmService;
    ILoadTimeFromFirebaseListener listener;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;

    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick(){

        AlertDialog.Builder builder =  new AlertDialog.Builder(getContext());
        builder.setTitle("One more step !");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order,null);

      //  EditText my_address = (EditText)view.findViewById(R.id.my_address) ;
        EditText my_comment = (EditText)view.findViewById(R.id.my_comment) ;
        TextView txt_address = (TextView)view.findViewById(R.id.txt_address_detail) ;
        RadioButton rdi_home=(RadioButton)view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address=(RadioButton)view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_cod=(RadioButton)view.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree=(RadioButton)view.findViewById(R.id.rdi_braintree);
        RadioButton rdi_ship_to_this=(RadioButton)view.findViewById(R.id.rdi_ship_this_address);

            //DATA
       // my_address.setText(Common.currentUser.getAddress());//By default we select home address which is user's address by whom is already saved!!!
        places_fragment = (AutocompleteSupportFragment)getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                placeSelected = place;
                txt_address.setText(place.getAddress());

            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getContext(),""+status.getStatusMessage(),Toast.LENGTH_SHORT).show();
            }
        });


        //DATA
        txt_address.setText(Common.currentUser.getAddress());//By default we select home address which is user's address by whom is already saved!!!
      //Events
               rdi_home.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        txt_address.setText(Common.currentUser.getAddress());
                        txt_address.setVisibility(View.VISIBLE);
                        places_fragment.setHint(Common.currentUser.getAddress());
                    }
            });
               rdi_other_address.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){

                txt_address.setVisibility(View.VISIBLE);


            }
        });
                rdi_ship_to_this.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){

                        fusedLocationProviderClient.getLastLocation()
                                .addOnFailureListener(e->{
                                    Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    txt_address.setVisibility(View.GONE);

                                })
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                String coordinates = new StringBuilder()
                                        .append(task.getResult().getLatitude())
                                        .append("/")
                                        .append(task.getResult().getLongitude()).toString();

                                Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(),
                                        task.getResult().getLongitude()));

                               Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                                   @Override
                                   public void onSuccess(String s) {

                                      // my_address.setText(coordinates);
                                       txt_address.setText(s);
                                       txt_address.setVisibility(View.VISIBLE);
                                       places_fragment.setHint(s);

                                   }

                                   @Override
                                   public void onError(Throwable e) {

                                      // my_address.setText(coordinates);
                                       txt_address.setText(e.getMessage());
                                       txt_address.setVisibility(View.VISIBLE);
                                   }
                               });

                            }
                        });

                    }
                });


//               rdi_cod.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked){
//                my_address.setText(Common.currentUser.getAddress());
//
//            }
//        });
//               rdi_braintree.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked){
//                my_address.setText(Common.currentUser.getAddress());
//
//            }
//        });



        builder.setView(view);
        builder.setNegativeButton("NO", (dialog, which) -> {

                dialog.dismiss();

        }).setPositiveButton("YES", (dialog, which) -> {

            //        Toast.makeText(getContext(),"Implement Late!",Toast.LENGTH_SHORT).show();

            if (rdi_cod.isChecked())
                paymentCOD(txt_address.getText().toString(),my_comment.getText().toString());



        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void paymentCOD(String address, String comment) {

        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(),
                Common.currentRestaurant.getUid())
               .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {
            //Once we have all the cartItems then we will calculate total price
            cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                    Common.currentRestaurant.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Double>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Double totalPrice) {
                            double finalPrice = totalPrice;
                            Order order = new Order();
                            order.setUserId(Common.currentUser.getUid());
                            order.setUserName(Common.currentUser.getName()) ;
                            order.setUserPhone(Common.currentUser.getPhone());
                            order.setShippingAddress(address);
                            order.setComment(comment);

                            if (currentLocation != null){

                                order.setLat(currentLocation.getLatitude());
                                order.setLng(currentLocation.getLongitude());
                            }else {

                                order.setLat(-0.1f);
                                order.setLng(-0.1f);

                            }

                            order.setCartItemList(cartItems);
                            order.setTotalPayment(totalPrice);
                            order.setDiscount(0);// It wills be modified late with discount
                            order.setFinalPayment(finalPrice);
                            order.setCod(true);
                            order.setTransactionId("Cash on Delivery");

                            // Now sumbiting order Object into Firebase
                           // writeOrderToFirebase(order);
                          syncLocalTimeWithGlobalTime(order);


                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!e.getMessage().contains("Query returned empty result set "))
                            Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }, throwable->{

            //Toast.makeText(getContext(),""+throwable.getMessage(),Toast.LENGTH_SHORT).show();

        }));




    }

    private void syncLocalTimeWithGlobalTime(Order order) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimateServerTimeMs = System.currentTimeMillis()+offset; //offset is missing time between local time and server time
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                Date resultDate = new Date(estimateServerTimeMs);
                Log.d("TESTING DATE",""+sdf.format(resultDate));

                listener.onLoadTimeSuccess(order,estimateServerTimeMs);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    listener.onLoadTimeFailed(error.getMessage());
            }
        });

    }

    private void writeOrderToFirebase(Order order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentRestaurant.getUid())
                .child(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {

                    Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                }).addOnCompleteListener(task -> {

                        //Writing successfull

                    cartDataSource.cleanCart(Common.currentUser.getUid(),
                            Common.currentRestaurant.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {


                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    Map<String,String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTI_TITLE,"New Order");
                                    notiData.put(Common.NOTI_CONTENT," You have a new order from "+Common.currentUser.getPhone());

                                    FCMSendData sendData = new FCMSendData(Common.createTopicOrder(),notiData);

                                 compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                 .observeOn(AndroidSchedulers.mainThread())
                                 .subscribe(fcmResponse -> {


                                     Toast.makeText(getContext(),"Order has been placed successfully !!!",Toast.LENGTH_SHORT).show();
                                  //   EventBus.getDefault().postSticky(new CounterCartEvent(true));


                                 }, throwable -> {
                                     Toast.makeText(getContext(),"Order has been submitted but failed to send notification" +
                                             " !!!",Toast.LENGTH_SHORT).show();
                                //      EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                 }));




                                    Toast.makeText(getContext(),"Order has been placed successfully !!!",Toast.LENGTH_SHORT).show();
                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));



                                       }

                                @Override
                                public void onError(Throwable e) {

                                   // Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });

                });

    }

    private String getAddressFromLatLng(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result="";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);
            if (addressList!=null&&addressList.size()>0)
            {
                Address address = addressList.get(0);  // always get first item
                StringBuilder sb= new StringBuilder(address.getAddressLine(0));
                result= sb.toString();
            }else {

                result="Address does not FOUND !!!";
            }


        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }

        return result;
    }

   // ;


    private Unbinder unbinder;
    private MyCartAdapter adapter;



    private CartViewModel cartViewModel;

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        cartViewModel= ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart,container,false);


        ifcmService = RetroFitFCMClient.getInstance().create(IFCMService.class);

        listener = this;

        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(getViewLifecycleOwner(), cartItems -> {

            if (cartItems==null||cartItems.isEmpty()){

                recycler_cart.setVisibility(View.GONE);
                group_place_holder.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.VISIBLE);


            }else {
                recycler_cart.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_cart.setVisibility(View.GONE);

               //  MyCartAdapter
                        adapter = new MyCartAdapter(getContext(),cartItems);
                recycler_cart.setAdapter(adapter);

            }


        });



        unbinder = ButterKnife.bind(this,root);
         initViews();
         initLocation();
      return root;




    }

    private void initLocation() {

        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());

    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();

            }
        };


    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }

    private void initViews() {

        initPlaceClient();

        searchFoodCallbackListener = this ;


        setHasOptionsMenu(true);

        cartDataSource = new LocalCartDatabaseSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwiperHelper = new MySwipeHelper(getContext(),recycler_cart,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Delete",30,0, Color.parseColor("#FF3C30"),

                        pos -> {

                            //Toast.makeText(getContext(),"Delete item CLick!!!",Toast.LENGTH_SHORT).show();

                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItems(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                          adapter.notifyItemRemoved(pos);
                                          sumAllItemInCart(); // Updated total price here!!!
                                          EventBus.getDefault().postSticky(new CounterCartEvent(true));// Updating Fab
                                            Toast.makeText(getContext(),"Delete item successfully happened ",Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                           // Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));

                buf.add(new MyButton(getContext(),"Update",30,0, Color.parseColor("#5D4037"),

                        pos -> {

                        CartItem cartItem = adapter.getItemAtPosition(pos);
                        FirebaseDatabase.getInstance()
                                .getReference(Common.CATEGORY_REF)
                                .child(cartItem.getCategoryId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.exists()){

                                            CategoryModel categoryModel = snapshot.getValue(CategoryModel.class);
                                            searchFoodCallbackListener.onSearchCategoryFound(categoryModel,cartItem);

                                        }else {
                                            searchFoodCallbackListener.onSearchCategoryNotFound("Food not Found");
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                        searchFoodCallbackListener.onSearchCategoryNotFound(error.getMessage());

                                    }
                                });


                        }));

            }
        };


        sumAllItemInCart();

        //AddOns
        addonBottomSheetDialog  = new BottomSheetDialog(getContext(),R.style.DialogStyle);
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display,null);
        chip_group_addon  = (ChipGroup)layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText)layout_addon_display.findViewById(R.id.my_search);
        addonBottomSheetDialog.setContentView(layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener(dialog -> {
            displayUserSelectedAddon(chip_group_user_selected_addon);
            calculateTotalPrice();

        });

    }

    private void initPlaceClient() {

        Places.initialize(getContext(), BuildConfig.GoogleAPIKEY);
        placesClient = Places.createClient(getContext());



    }

    private void displayUserSelectedAddon(ChipGroup chip_group_user_selected_addon) {

        if (Common.selectedFood.getUserSelectedAddon()!=null && Common.selectedFood.getUserSelectedAddon().size() >0    ){
          chip_group_user_selected_addon.removeAllViews();
            for (AddonModel addonModel :Common.selectedFood.getUserSelectedAddon()){

                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    if (isChecked){
                        if (Common.selectedFood.getUserSelectedAddon()== null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_user_selected_addon.addView(chip); }

        }
        else
            chip_group_user_selected_addon.removeAllViews();


    }

    private void sumAllItemInCart() {

        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                Common.currentRestaurant.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        txt_total_price.setText(new StringBuilder("Total : $").append(aDouble));

                    }

                    @Override
                    public void onError(Throwable e) {
                                if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });


    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {

        menu.findItem(R.id.action_settings).setVisible(false); // Hiding Homse  Menu that is already inflated
        super.onPrepareOptionsMenu(menu);


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.action_clear_cart){

            cartDataSource.cleanCart(Common.currentUser.getUid(),
                    Common.currentRestaurant.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(getContext(),"Clear Cart Succsessfully happened !!",Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));


                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

                    return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }


    @Override
    public void onStop() {


        EventBus.getDefault().postSticky(new HideFABCart(false));
        cartViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        if (fusedLocationProviderClient!=null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();


        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient!=null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper());

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event){

        if (event.getCartItem()!=null){

            //We save the state of recycler view here  !

            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState); //Fixing error of refreshing recycler view after updating

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(),"[UPDATE CART]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });



        }



    }

    private void calculateTotalPrice() {

        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                Common.currentRestaurant.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {

                        txt_total_price.setText(new StringBuilder("Total : $")
                        .append(Common.formatPrice(price)));

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty result set "))
                            Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                              }
                });







    }

    @Override
    public void onLoadTimeSuccess(Order order, long estimateTimeInMs) {

        order.setCreateDate(estimateTimeInMs);
        order.setOrderStatus(0);
        writeOrderToFirebase(order);

    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(),""+message,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {

        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();

    }

    @Override
    public void onSearchCategoryFound(CategoryModel categoryModel,CartItem cartItem) {
        FoodModel foodModel = Common.findFoodInListById(categoryModel,cartItem.getFoodId());

        if (foodModel!=null){
            showUpdateDialog(cartItem,foodModel);
        }else   {

            Toast.makeText(getContext(),"Food Id not found ",Toast.LENGTH_SHORT).show();

        }

    }

    private void showUpdateDialog(CartItem cartItem, FoodModel foodModel) {
        Common.selectedFood = foodModel;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_update_cart,null);
        builder.setView(itemView);

        //View
        Button btn_ok = (Button)itemView.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button)itemView.findViewById(R.id.btn_cancel);

        RadioGroup rdi_group_size = (RadioGroup)itemView.findViewById(R.id.rdi_group_size);

        chip_group_user_selected_addon = (ChipGroup)itemView.findViewById(R.id.chip_group_user_selected_addon);
        ImageView img_add_on = (ImageView)itemView.findViewById(R.id.img_add_addon);
        img_add_on.setOnClickListener(v -> {
                if (foodModel.getAddon()!=null){

                    displayAddonList();
                    addonBottomSheetDialog.show();
                }
        });




        //Size of AddOn here
        if (foodModel.getSize()!=null){
            for (SizeModel sizeModel: foodModel.getSize()){
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked)
                            Common.selectedFood.setUserSelectedSize(sizeModel);
                        calculateTotalPrice();
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(sizeModel.getName());
                radioButton.setTag(sizeModel.getPrice());

                rdi_group_size.addView(radioButton);

            }

            if(rdi_group_size.getChildCount()>0){

                RadioButton radioButton = (RadioButton)rdi_group_size.getChildAt(0);  // Getting 1st radio button
                radioButton.setChecked(true);

            }


        }

        //Addons
        displayAlreadySelectedAddon(chip_group_user_selected_addon,cartItem);

        //Showing Dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        //Customization of dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        //Event
        btn_ok.setOnClickListener(v -> {
                    //Deleting items from Cart
            cartDataSource.deleteCartItems(cartItem)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                // Here we update infromation of new price and infos on cart
                            if (Common.selectedFood.getUserSelectedAddon()!=null)
                                cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
                            else
                                cartItem.setFoodAddon("Default");
                            if (Common.selectedFood.getUserSelectedSize()!=null)
                                cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
                            else
                                cartItem.setFoodSize("Default");

                            cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(),
                                    Common.selectedFood.getUserSelectedAddon()));

                            //Insert New
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem).
                                    subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(()-> {

                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));  // Re-counting cart
                                                calculateTotalPrice();
                                                dialog.dismiss();
                                                Toast.makeText(getContext(),"Update Cart Succesfull",Toast.LENGTH_SHORT).show();
                                            },throwable -> {

                                                Toast.makeText(getContext(),"" +throwable.getMessage() ,Toast.LENGTH_SHORT).show();
                                            })


                            );




                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

        });
        btn_cancel.setOnClickListener(v -> {

            dialog.dismiss();
        });

    }

    private void displayAlreadySelectedAddon(ChipGroup chip_group_user_selected_addon, CartItem cartItem) {

        if (cartItem.getFoodAddon() != null && !cartItem.getFoodAddon().equals("Default")){

            List<AddonModel> addonModels = new Gson().fromJson(cartItem.getFoodAddon(),
                    new TypeToken<List<AddonModel>>(){}.getType());
            Common.selectedFood.setUserSelectedAddon(addonModels);
            chip_group_user_selected_addon.removeAllViews();

            // Adding all view extras on cart
            for (AddonModel addonModel :addonModels){ // we got addonModels as we want to get waht user already got at local cart

                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));

                chip.setClickable(false);
                chip.setOnCloseIconClickListener(v -> {

                    // Removes addons when user selects to deletet them
                    chip_group_user_selected_addon.removeView(v);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();

                });

                chip_group_user_selected_addon.addView(chip);
            }



        }




    }

    private void displayAddonList() {

            if (Common.selectedFood.getAddon()!= null && Common.selectedFood.getAddon().size() > 0){

                chip_group_addon.clearCheck();
                chip_group_addon.removeAllViews();

                edt_search.addTextChangedListener(this);

                // Adding all view extras on cart
                for (AddonModel addonModel :Common.selectedFood.getAddon()){

                    Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                    chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                    .append(addonModel.getPrice()).append(")"));
                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {

                        if (isChecked){
                                if (Common.selectedFood.getUserSelectedAddon()== null)
                                    Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                                Common.selectedFood.getUserSelectedAddon().add(addonModel);



                        }



                    });
                        chip_group_addon.addView(chip);
                }



            }



    }

    @Override
    public void onSearchCategoryNotFound(String message) {
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();
        for (AddonModel addonModel :Common.selectedFood.getAddon()){

            if (addonModel.getName().toLowerCase().contains(s.toString().toLowerCase())){

                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    if (isChecked){
                        if (Common.selectedFood.getUserSelectedAddon()== null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);



                    }



                });
                chip_group_addon.addView(chip);

            }

        }


    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
