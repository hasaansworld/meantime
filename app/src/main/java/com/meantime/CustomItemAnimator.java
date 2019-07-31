package com.meantime;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;


public class CustomItemAnimator extends DefaultItemAnimator {

    int height = 0;
    public static final Interpolator COLLAPSE_INTERPOLATOR = new AccelerateInterpolator(3f);
    public static final int COLLAPSE_ANIM_DURATION = 600;

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        final View itemView = holder.itemView;
        height = itemView.getHeight();
        //TextView date = ((ScheduleAdapter.ViewHolder)holder).date;
        //date.setAlpha(0);
        AnimatorSet set = new AnimatorSet();

        LayoutParamHeightAnimator animHeight = LayoutParamHeightAnimator.collapse(itemView);
        animHeight.setDuration(COLLAPSE_ANIM_DURATION).setInterpolator(COLLAPSE_INTERPOLATOR);

        set.play(animHeight);

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatchChangeStarting(holder, false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                dispatchChangeFinished(holder, false);
            }
        });

        set.start();

        return false;
    }

}