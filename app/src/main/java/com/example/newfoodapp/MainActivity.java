package com.example.newfoodapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import android.Manifest;
import android.accounts.Account;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newfoodapp.Common.Common;
import com.example.newfoodapp.Model.UserModel;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;

   private static int APP_REQUEST_CODE= 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener  listener;
    private AlertDialog dialog ;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener!=null){
           firebaseAuth.removeAuthStateListener(listener);
           compositeDisposable.clear();
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init(){


        Places.initialize(this,BuildConfig.GoogleAPIKEY);
        placesClient = Places.createClient(this);

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        userRef= FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
        firebaseAuth=firebaseAuth.getInstance();


        dialog= new SpotsDialog.Builder().setCancelable(false).setContext(this).build();


        listener = firebaseAuth -> {

            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {

                            FirebaseUser user  = firebaseAuth.getCurrentUser();
                            if(user!=null){
                                checkUserFromFirebase(user);
                            }else{
                                phoneLogin();

                            }

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this,"You must enable this permission to use this app",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    }).check();



        };





    }

    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();

        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    Toast.makeText(MainActivity.this,"You already got registered ",Toast.LENGTH_SHORT);

                    UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    goToHomeActivity(userModel);


                }else {
                            showRegisterDialog(user);

                    }
                dialog.dismiss();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                dialog.dismiss();

                Toast.makeText(MainActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT);

            }
        });


    }

    private void  showRegisterDialog(FirebaseUser user){

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register ") ;
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText myName =  (EditText)itemView.findViewById(R.id.myName);
        EditText myPhone = (EditText)itemView.findViewById(R.id.myPhone);
      //  EditText myAddress = (EditText)itemView.findViewById(R.id.myAddress);
        TextView txt_address_detail = (TextView) itemView.findViewById(R.id.txt_address_detail);//na ksanado ti paizei


        places_fragment = (AutocompleteSupportFragment)getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                placeSelected = place;
                txt_address_detail.setText(place.getAddress());

            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MainActivity.this,""+status.getStatusMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        //set
        myPhone.setText(user.getPhoneNumber());

        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("REGISTER ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {



                if (placeSelected!=null) {
                    if (TextUtils.isEmpty(myName.getText().toString())) {

                        Toast.makeText(MainActivity.this, "Please enter your name ", Toast.LENGTH_SHORT).show();
                        return;
                    }



                    UserModel userModel = new UserModel();
                    userModel.setUid(user.getUid());
                    userModel.setName(myName.getText().toString());
                    userModel.setAddress(txt_address_detail.getText().toString());
                    userModel.setPhone(myPhone.getText().toString());
                    userModel.setLat(placeSelected.getLatLng().latitude);
                    userModel.setLng(placeSelected.getLatLng().longitude);

                    userRef.child(user.getUid()).setValue(userModel)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        dialog.dismiss();

                                        Toast.makeText(MainActivity.this, "Congratulations!!! You got successfully registered!!!", Toast.LENGTH_SHORT);
                                        goToHomeActivity(userModel);

                                    }
                                }
                            });

                }else{


                    Toast.makeText(MainActivity.this,"Please select address", Toast.LENGTH_SHORT).show();
                }


            }
        });


        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.show();


    }


    private void goToHomeActivity(UserModel userModel) {


//        FirebaseInstanceId.getInstance()
//                .getInstanceId()
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                }).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//            @Override
//            public void onComplete(@NonNull Task<InstanceIdResult> task) {
//
//            }
//        });
//
//



        Common.currentUser = userModel; // Important    you always need assign value before its use
        startActivity(new Intent(MainActivity.this,HomeActivity.class));
        finish();




    }


    private void phoneLogin() {


        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.logo)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers).build(),APP_REQUEST_CODE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) {

            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            } else {

                Toast.makeText(this, "Failed to sign in ! :( ", Toast.LENGTH_SHORT).show();


            }



        }

    }}


