package com.oinotna.umbra.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.oinotna.umbra.R;

public class ProgressButton extends MaterialButton {
    private GradientDrawable mGradientDrawable;
    private CircularAnimatedDrawable mAnimatedDrawable;

    private OnAnimationEndListener mListener;

    public interface OnAnimationEndListener{
        void onAnimationEnd();
    }

    public void setOnAnimationEndListener(OnAnimationEndListener listener){
        mListener=listener;
    }

    private enum State {
        PROGRESS_IDLE, PROGRESS_CIRCLE, IDLE, CIRCLE
    }

    private State mState;

    private  boolean mIsMorphingInProgress;

    private AnimatorSet mMorphingAnimatorSet;

    public ProgressButton(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ProgressButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mGradientDrawable = (GradientDrawable)
                ContextCompat.getDrawable(context, R.drawable.shape_default);

        setBackground(mGradientDrawable);
        mState=State.IDLE;
    }

    private int mStartingWidth;
    private int mStartingHeight;

    private void setupAnimation(int initialCornerRadius, int finalCornerRadius, int initialWidth, int toWidth, int initialHeight, int toHeight){
        //Creo le animazioni
        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(mGradientDrawable,
                        "cornerRadius",
                        initialCornerRadius,
                        finalCornerRadius);

        ValueAnimator widthAnimation = ValueAnimator.ofInt(initialWidth, toWidth);
        widthAnimation.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = val;
            setLayoutParams(layoutParams);
        });

        ValueAnimator heightAnimation = ValueAnimator.ofInt(initialHeight, toHeight);
        heightAnimation.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.height = val;
            setLayoutParams(layoutParams);
        });

        ProgressButton bt=this;

        //creo l'animator set che gestisce le animazioni
        mMorphingAnimatorSet = new AnimatorSet();
        mMorphingAnimatorSet.setDuration(300);
        mMorphingAnimatorSet.playTogether(cornerAnimation, widthAnimation, heightAnimation);
        mMorphingAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsMorphingInProgress = false;
                if(mState==State.PROGRESS_IDLE){
                    mState=State.CIRCLE;

                }
                else{
                    bt.setText(R.string.search_button);
                    mState=State.IDLE;
                    setClickable(true);
                }
            }
        });

    }

    /**
     * Method called to start the animation.
     * Morphs in to a ball and then starts a loading spinner.
     */
    public void startAnimation(){
        if(mState != State.IDLE){
            return;
        }

        mIsMorphingInProgress = true;

        //creo i valori per le animazioni

        int arcWidth = 15;
        if(mAnimatedDrawable==null) {
            mAnimatedDrawable = new CircularAnimatedDrawable(this,
                    arcWidth,
                    Color.WHITE);
            mAnimatedDrawable.setOnAnimationEndListener(mListener);
        }

        mStartingWidth = getWidth();
        mStartingHeight=getHeight();

        int initialWidth = getWidth();
        int initialHeight = getHeight();

        int toWidth = this.getHeight();
        int toHeight=toWidth;

        int initialCornerRadius = 10;
        int finalCornerRadius = 1000;

        this.setText(null);
        setClickable(false);

        mState = State.PROGRESS_IDLE;

        setupAnimation(initialCornerRadius, finalCornerRadius, initialWidth, toWidth, initialHeight, toHeight);
        mMorphingAnimatorSet.start();

    }

    public void startEndAnimation(){
        if(mState != State.CIRCLE){
            return;
        }

        mIsMorphingInProgress = true;

        //creo i valori per le animazioni
        int initialWidth = getWidth();
        int toWidth=mStartingWidth;
        int initialHeight = getHeight();
        int toHeight=mStartingHeight;
        int initialCornerRadius = 1000;
        int finalCornerRadius = 10;

        mState = State.PROGRESS_CIRCLE;

        setupAnimation(initialCornerRadius, finalCornerRadius, initialWidth, toWidth, initialHeight, toHeight);
        mMorphingAnimatorSet.start();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if (mState == State.CIRCLE && !mIsMorphingInProgress) {
            drawIndeterminateProgress(canvas); //loading spinner
        }
    }

    private void drawIndeterminateProgress(Canvas canvas) {
        if (!mAnimatedDrawable.isRunning()) {

            int left = 0;
            int right = getWidth();
            int bottom = getHeight();
            int top = 0;

            mAnimatedDrawable.setBounds(left, top, right, bottom);
            mAnimatedDrawable.setCallback(this);
            mAnimatedDrawable.start();
        } else {
            mAnimatedDrawable.draw(canvas);
        }
    }
}
