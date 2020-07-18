package com.oinotna.umbra.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class CircularAnimatedDrawable extends Drawable implements Animatable {
    private ValueAnimator mValueAnimatorAngle;
    private ValueAnimator mValueAnimatorSweep;
    private static final Interpolator ANGLE_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator SWEEP_INTERPOLATOR = new DecelerateInterpolator();
    private static final int ANGLE_ANIMATOR_DURATION = 1200;
    private static final int SWEEP_ANIMATOR_DURATION = 1200;
    private static final Float MIN_SWEEP_ANGLE = 30f;

    private final RectF fBounds = new RectF();
    private Paint mPaint;
    private View mAnimatedView;

    private float mBorderWidth;
    private float mCurrentGlobalAngle;
    private float mCurrentSweepAngle;
    //private float mCurrentGlobalAngleOffset;

    //private boolean mModeAppearing=false;
    private boolean mRunning;

    private ProgressButton.OnAnimationEndListener mListener;

    public void setOnAnimationEndListener(ProgressButton.OnAnimationEndListener listener){
        mListener=listener;
    }

    public CircularAnimatedDrawable(View view, float borderWidth, int arcColor) {
        mAnimatedView = view;

        mBorderWidth = borderWidth;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(borderWidth);
        mPaint.setColor(arcColor);

        setupAnimations();
    }


    private void setupAnimations() {
        mValueAnimatorAngle = ValueAnimator.ofFloat(0, 360f);
        mValueAnimatorAngle.setInterpolator(ANGLE_INTERPOLATOR);
        mValueAnimatorAngle.setDuration(ANGLE_ANIMATOR_DURATION);
        //mValueAnimatorAngle.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimatorAngle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentGlobalAngle = (float)animation.getAnimatedValue();
                mAnimatedView.invalidate(); //triggera onDraw
            }
        });
        mValueAnimatorAngle.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(mListener!=null)
                    mListener.onAnimationEnd();
                stop();
            }
        });


        mValueAnimatorSweep = ValueAnimator.ofFloat(0, 360f - 2 * MIN_SWEEP_ANGLE);
        mValueAnimatorSweep.setInterpolator(SWEEP_INTERPOLATOR);
        mValueAnimatorSweep.setDuration(SWEEP_ANIMATOR_DURATION);
        //mValueAnimatorSweep.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimatorSweep.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentSweepAngle = (float)animation.getAnimatedValue();
                invalidateSelf();
            }
        });
        mValueAnimatorSweep.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                //TODO: dovrei invertire l'animazione sweepangle

            }
        });
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        //stroke all'interno
        fBounds.left = bounds.left + mBorderWidth / 2f + .5f;
        fBounds.right = bounds.right - mBorderWidth / 2f - .5f;
        fBounds.top = bounds.top + mBorderWidth / 2f + .5f;
        fBounds.bottom = bounds.bottom - mBorderWidth / 2f - .5f;
    }

    public void start() {
        if (mRunning) {
            return;
        }

        mRunning = true;
        mValueAnimatorAngle.start();
        mValueAnimatorSweep.start();
    }

    public void stop() {
        if (!mRunning) {
            return;
        }

        mRunning = false;
        mValueAnimatorAngle.cancel();
        mValueAnimatorSweep.cancel();
    }
    @Override
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Method called when the drawable is going to draw itself.
     * @param canvas
     */
    @Override
    public void draw(Canvas canvas) {
        float startAngle = mCurrentGlobalAngle; //- mCurrentGlobalAngleOffset;
        float sweepAngle = mCurrentSweepAngle;
        //if (!mModeAppearing) {
            startAngle = startAngle + sweepAngle;
            sweepAngle = 360 - sweepAngle - MIN_SWEEP_ANGLE;
        /*} else {
            sweepAngle += MIN_SWEEP_ANGLE;
        }*/

        //Paint attribute decides the style of the Arc (Fill or Stroke etc).
        // Oval parameter decides the shape and size of the arc. Start angle (in degrees)
        // where the arc begins, Sweep Angle measures how long the arc will be drawn and
        // is always measured in clockwise.
        canvas.drawArc(fBounds, startAngle, sweepAngle, false, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
