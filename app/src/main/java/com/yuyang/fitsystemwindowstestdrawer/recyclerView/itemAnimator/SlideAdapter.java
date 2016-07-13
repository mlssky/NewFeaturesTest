package com.yuyang.fitsystemwindowstestdrawer.recyclerView.itemAnimator;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.yuyang.fitsystemwindowstestdrawer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuyang on 2016/7/12.
 */
public class SlideAdapter extends RecyclerView.Adapter {
    public static final int NORMAL = 1000;
    public static final int SLIDE = 2000;
    private int mState = NORMAL;
    private List<ItemBean> mItemBeans;
    private List<SlideViewHolder> mSlideViewHolders = new ArrayList<>();

    public void setItemBeans(List<ItemBean> beans) {
        mItemBeans = beans;
        notifyDataSetChanged();
    }

    public void openItemAnimation() {
        mState = SLIDE;
        for (SlideViewHolder holder : mSlideViewHolders) {
            holder.openItemAnimation();
        }
    }

    public void closeItemAnimation() {
        mState = NORMAL;
        for (SlideViewHolder holder : mSlideViewHolders) {
            holder.closeItemAnimation();
        }
    }

    @Override
    public SlideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SlideViewHolder viewHolder = new SlideViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide_animator_layout, parent, false));
        mSlideViewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((SlideViewHolder) holder).bind(mItemBeans.get(position));
    }

    @Override
    public int getItemCount() {
        return mItemBeans == null ? 0 : mItemBeans.size();
    }

    private class SlideViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private SlideRelativeLayout mSlideRelativeLayout;
        private CheckBox mCheckBox;
        private ItemBean mItemBean;

        public SlideViewHolder(View itemView) {
            super(itemView);
            mSlideRelativeLayout = (SlideRelativeLayout) itemView.findViewById(R.id.item_root);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.item_checkbox);
            itemView.setOnClickListener(this);
        }

        public void bind(ItemBean itemBean) {
            mItemBean = itemBean;
            mCheckBox.setChecked(itemBean.isChecked());
            switch (mState) {
                case NORMAL:
                    mSlideRelativeLayout.close();
                    break;

                case SLIDE:
                    mSlideRelativeLayout.open();
                    break;
            }
        }

        public void openItemAnimation() {
            mSlideRelativeLayout.openAnimator();
        }

        public void closeItemAnimation() {
            mSlideRelativeLayout.closeAnimator();
        }

        public void setCheckBox() {
            mCheckBox.setChecked(!mCheckBox.isChecked());
            mItemBean.setChecked(mCheckBox.isChecked());
        }

        @Override
        public void onClick(View v) {
            setCheckBox();
        }
    }
}
