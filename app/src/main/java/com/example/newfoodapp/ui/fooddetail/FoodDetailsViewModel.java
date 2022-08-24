package com.example.newfoodapp.ui.fooddetail;

import com.example.newfoodapp.Common.Common;
import com.example.newfoodapp.Model.CommentModel;
import com.example.newfoodapp.Model.FoodModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FoodDetailsViewModel extends ViewModel {


        private MutableLiveData<FoodModel> mutableLiveDataFood;
        private MutableLiveData<CommentModel> mutableLiveDataComment;


      public void setCommentModel(CommentModel commentModel){

          if (mutableLiveDataComment!=null)
              mutableLiveDataComment.setValue(commentModel);

      }

    public MutableLiveData<CommentModel> getMutableLiveDataComment() {


        return mutableLiveDataComment;


    }

    public FoodDetailsViewModel(){
        mutableLiveDataComment = new MutableLiveData<>();
    }

    public MutableLiveData<FoodModel> getMutableLiveData() {
        if(mutableLiveDataFood==null)
            mutableLiveDataFood = new MutableLiveData<>();
        mutableLiveDataFood.setValue(Common.selectedFood);

        return mutableLiveDataFood;
    }


    public void setFoodModel(FoodModel foodModel) {

          if(mutableLiveDataFood!=null)
              mutableLiveDataFood.setValue(foodModel);

    }
}
