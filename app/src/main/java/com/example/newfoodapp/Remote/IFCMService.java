package com.example.newfoodapp.Remote;

import android.database.Observable;

import com.example.newfoodapp.Model.FCMResponse;
import com.example.newfoodapp.Model.FCMSendData;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {



    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAh2U-jKI:APA91bHQY5PwEIcIa4o93PHcGYXdEavjL5PBZjle0KWRDcxliCI3WoBcYSa0MqhyxfjIzBt-tCN7ejO8Q8gKm8v-5569g5Hc6udnWnPT2OgcjSJgGx2UlH2MW2vBzJd2BCLdOvSIv8ns"

    })
    @POST("fcm/send")
    io.reactivex.Observable<FCMResponse> sendNotification(@Body FCMSendData body);








}
