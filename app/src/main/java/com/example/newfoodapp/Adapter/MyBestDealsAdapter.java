package com.example.newfoodapp.Adapter;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.asksira.loopingviewpager.LoopingViewPager;
import com.bumptech.glide.Glide;
import com.example.newfoodapp.EventBus.BestDealItemClick;
import com.example.newfoodapp.Model.BestDealModel;
import com.example.newfoodapp.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyBestDealsAdapter extends LoopingPagerAdapter<BestDealModel> {

  // Context context;

 //List<BestDealModel> itemList;

    @BindView(R.id.img_best_deal)
    ImageView img_best_deal;
    @BindView(R.id.txt_best_deal)
    TextView text_best_deal;

    Unbinder unbinder;


    public MyBestDealsAdapter(Context context, List<BestDealModel> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    @Override
    protected View inflateView(int i, ViewGroup viewGroup, int i1) {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deal_item,viewGroup,false);



    }

    @Override
    protected void bindView(View view, int i, int i1) {

       unbinder  = ButterKnife.bind(this,view);
       //Set data
       Glide.with(view).load(itemList.get(i).getImage()).into(img_best_deal);
        text_best_deal.setText(itemList.get(i).getName());



        view.setOnClickListener(v -> {

            EventBus.getDefault().postSticky(new BestDealItemClick(itemList.get(i1)));

        });


    }




}
