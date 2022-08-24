package com.example.newfoodapp.Callback;

import com.example.newfoodapp.Model.CommentModel;

import java.util.List;

public interface ICommentCallbackListener {

    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);


}
