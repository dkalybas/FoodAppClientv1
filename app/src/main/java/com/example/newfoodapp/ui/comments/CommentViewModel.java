package com.example.newfoodapp.ui.comments;

import com.example.newfoodapp.Model.CommentModel;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CommentViewModel extends ViewModel {

    private MutableLiveData<List<CommentModel>>  mutableLiveDataFoodList;

    public CommentViewModel() {
        mutableLiveDataFoodList = new MutableLiveData<>();
    }


    public MutableLiveData<List<CommentModel>> getMutableLiveDataFoodList() {
        return mutableLiveDataFoodList;
    }

    public void setCommentList(List<CommentModel>   commentList){

            mutableLiveDataFoodList.setValue(commentList);


    }


}
