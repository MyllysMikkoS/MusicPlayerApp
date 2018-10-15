package com.propro.musicplayerapp;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class MediaButtons extends View {

    // progressBar count 0-100
    public float progress = 60;

    // the number of slice
    private int mSlices = 4;

    // the angle of each slice
    private int degreeStep = 360 / 8;

    private float mOuterRadius;
    private float mInnerRadius;

    // using radius square to prevent square root calculation
    private float outerRadiusSquare;
    private float innerRadiusSquare;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mProgressBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCircleShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mSliceOval = new RectF();

    private float innerRadiusRatio = 0.55F;

    private int mCenterX;
    private int mCenterY;

    private OnSliceClickListener mOnSliceClickListener;
    private int mTouchSlop;

    private boolean mPressed;
    private boolean mMoved;
    private int mInsideProgressbar = 0;
    private int finally_up = 0;
    private float mLatestDownX;
    private float mLatestDownY;

    public interface OnSliceClickListener{
        void onSlickClick(int slicePosition, float progress);
    }

    public MediaButtons(Context context){
        this(context, null);
    }

    public MediaButtons(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public MediaButtons(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();

        mPaint.setStrokeWidth(0);
        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);
        setLayerType(LAYER_TYPE_SOFTWARE, mProgressPaint);
        setLayerType(LAYER_TYPE_SOFTWARE, mProgressBGPaint);
        setLayerType(LAYER_TYPE_SOFTWARE, mCircleShadowPaint);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setMaskFilter(new BlurMaskFilter(120, BlurMaskFilter.Blur.INNER));
        mProgressBGPaint.setStyle(Paint.Style.FILL);
        mProgressBGPaint.setMaskFilter(new BlurMaskFilter(240, BlurMaskFilter.Blur.INNER));
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setMaskFilter(new BlurMaskFilter(120, BlurMaskFilter.Blur.INNER));
        mCircleShadowPaint.setStyle(Paint.Style.FILL);
        mCircleShadowPaint.setMaskFilter(new BlurMaskFilter(40, BlurMaskFilter.Blur.INNER));
    }

    public void setOnSliceClickListener(OnSliceClickListener onSliceClickListener){
        mOnSliceClickListener = onSliceClickListener;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        mCenterX = w / 2;
        mCenterY = h / 2;

        mOuterRadius = mCenterX > mCenterY ? mCenterY : mCenterX;
        mInnerRadius = mOuterRadius * innerRadiusRatio;

        outerRadiusSquare = mOuterRadius * mOuterRadius;
        innerRadiusSquare = mInnerRadius * mInnerRadius;

        mSliceOval.left = mCenterX - mOuterRadius;
        mSliceOval.right = mCenterX + mOuterRadius;
        mSliceOval.top = mCenterY - mOuterRadius;
        mSliceOval.bottom = mCenterY + mOuterRadius;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        float currX = event.getX();
        float currY = event.getY();

        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mLatestDownX = currX;
                mLatestDownY = currY;
                mPressed = true;
                mMoved = false;
                finally_up = 0;
                mInsideProgressbar = 0;
                // -- DEBUG TEXT -- Log.d("ACTION: ", "DOWN");

                // location variables
                int dx;
                int dy;
                int distanceSquare;

                // if progressbar clicked
                dx = (int) currX - mCenterX;
                dy = (int) currY - mCenterY;
                distanceSquare = dx * dx + dy * dy;

                if(distanceSquare > innerRadiusSquare && distanceSquare < outerRadiusSquare){
                    double angle = Math.atan2(dy, dx);

                    // if angle is on negative side (on progressBar field) save new progress
                    if(angle < 0){
                        progress = (float) (100 - (angle / -(Math.PI))*100);

                        // draw new progress
                        this.invalidate();
                    }
                    else {
                        if(Math.abs(currX - mLatestDownX) > mTouchSlop || Math.abs(currY - mLatestDownY) > mTouchSlop) mPressed = false;
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mMoved = true;
                finally_up = 0;
                // -- DEBUG TEXT -- Log.d("ACTION: ", "MOVE");
                if(mPressed){
                    dx = (int) currX - mCenterX;
                    dy = (int) currY - mCenterY;
                    distanceSquare = dx * dx + dy * dy;

                    // on the outer ring
                    if(distanceSquare > innerRadiusSquare && distanceSquare < outerRadiusSquare){
                        double angle = Math.atan2(dy, dx);

                        // if angle is on negative side (on progressBar field) save new progress
                        if(angle < 0){
                            progress = (float) (100 - (angle / -(Math.PI))*100);
                            mInsideProgressbar = 1;
                            // draw new progress
                            this.invalidate();
                        }
                        else {
                            if (mInsideProgressbar != 1) {
                                if (Math.abs(currX - mLatestDownX) > mTouchSlop || Math.abs(currY - mLatestDownY) > mTouchSlop)
                                    mPressed = false;
                                break;
                            }
                        }
                    }
                    // in play button
                    else if (distanceSquare < innerRadiusSquare){
                        if (mInsideProgressbar != 1) {
                            if (Math.abs(currX - mLatestDownX) > mTouchSlop || Math.abs(currY - mLatestDownY) > mTouchSlop)
                                mPressed = false;
                            break;
                        }
                    }
                }
            case MotionEvent.ACTION_UP:

                /* ------- SLICE INDEXES EXPLAINED ---------
                // -3 = something else
                // -2 = playButton
                // -1 = progressBar
                // 0 = nextSongButton
                // 1 = shuffleButton
                // 2 = repeatButton
                // 3 = previousSongButton
                   ----------------------------------------- */
                finally_up++;
                // -- DEBUG TEXT -- Log.d("ACTION: ", "UP " + String.valueOf(finally_up) + " " + mMoved);
                if(mPressed){
                    dx = (int) currX - mCenterX;
                    dy = (int) currY - mCenterY;
                    distanceSquare = dx * dx + dy * dy;

                    //get the angle to detect which slice is currently being click
                    double angle = Math.atan2(dy, dx);
                    double rawSliceIndex = -3;

                    // if the distance between touchpoint and centerpoint is smaller than
                    // outerRadius and longer than innerRadius, then we're in the clickable area
                    if(distanceSquare > innerRadiusSquare && distanceSquare < outerRadiusSquare){

                        // -- DEBUG TEXT -- Log.d("Angle: ", String.valueOf(angle));

                        if(angle >= 0 && angle < Math.PI/4){
                            rawSliceIndex = 0;
                        }else if(angle >= Math.PI/4 && angle < Math.PI/2){
                            rawSliceIndex = 1;
                        }else if(angle >= Math.PI/2 && angle < Math.PI*0.75){
                            rawSliceIndex = 2;
                        }else if(angle >= Math.PI*0.75 && angle < Math.PI){
                            rawSliceIndex = 3;
                        }else if(angle < 0 && angle > -(Math.PI)){
                            rawSliceIndex = -1;
                        }

                        // Update when no progressbar is touched
                        if(mOnSliceClickListener != null && rawSliceIndex != -1 && mInsideProgressbar != 1){
                            mOnSliceClickListener.onSlickClick((int) rawSliceIndex, progress);
                        }
                        // If progressbar is touched then update only after movement ends
                        else if(mOnSliceClickListener != null && rawSliceIndex == -1 && mInsideProgressbar == 1 &&
                                ((finally_up == 1 && !mMoved) || (finally_up == 2 && mMoved))){
                            mOnSliceClickListener.onSlickClick(-1, progress);
                        }
                        // If progressbar is touched but movement ends outside progressbar
                        else if(mOnSliceClickListener != null && (angle >= 0 && angle < Math.PI) && mInsideProgressbar == 1 &&
                                ((finally_up == 1 && !mMoved) || (finally_up == 2 && mMoved))){
                            mOnSliceClickListener.onSlickClick( -1, progress);
                        }
                    }
                    // if the distance between touchpoint and centerpoint is smaller than innerRadius, the playButton is clicked
                    else if (distanceSquare < innerRadiusSquare && mInsideProgressbar != 1){
                        if(mOnSliceClickListener != null){
                            mOnSliceClickListener.onSlickClick(-2, progress);
                        }
                    }
                    else {
                        // If progressbar is touched then update only after movement ends
                        if(mOnSliceClickListener != null && mInsideProgressbar == 1 &&
                                ((finally_up == 1 && !mMoved) || (finally_up == 2 && mMoved))){
                            mOnSliceClickListener.onSlickClick(-1, progress);
                        }
                    }
                    break;
                }
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas){
        int startAngle = 0;

        // draw background
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.lowerHalfCircle));
        canvas.drawArc(mSliceOval, 0, 180, true, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.progressBarBG));
        canvas.drawArc(mSliceOval, -180, 180, true, mPaint);

        // Draw shadow
        mProgressBGPaint.setColor(ContextCompat.getColor(getContext(), R.color.progressBarBGShadow));
        canvas.drawArc(mSliceOval,-180, 360, true, mProgressBGPaint);

        /*mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        canvas.drawArc(mSliceOval, 0, 360, true, mPaint);*/

        // draw progressBar
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.progressBar));
        canvas.drawArc(mSliceOval, -180, progress*1.8f, true, mPaint);

        // Draw shadow
        mProgressPaint.setColor(ContextCompat.getColor(getContext(), R.color.progressBarShadow));
        canvas.drawArc(mSliceOval, -180, progress*1.8f, true, mProgressPaint);

        /*mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        canvas.drawArc(mSliceOval, -180, progress*1.8f, true, mPaint);*/

        // draw slice
        for(int i = 0; i < mSlices; i++){
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.sliceColor));
            int start = startAngle + 1;
            int sweep = degreeStep - 2;
            if (i == 0) {
                start = startAngle + 2;
                sweep = degreeStep - 3;
            }
            else if (i == 1 || i == 2){
                start = startAngle + 1;
                sweep = degreeStep - 2;
            }
            if (i == 3) {
                start = startAngle + 1;
                sweep = degreeStep - 3;
            }
            canvas.drawArc(mSliceOval, start, sweep, true, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.TRANSPARENT);
            canvas.drawArc(mSliceOval, startAngle, degreeStep, true, mPaint);

            // Draw shadow
            mShadowPaint.setColor(ContextCompat.getColor(getContext(), R.color.buttonShadow));
            canvas.drawArc(mSliceOval, start, sweep, true, mShadowPaint);

            startAngle += degreeStep;
        }

        //draw center circle
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.middleCircle));
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius, mPaint);
        /*mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius, mPaint);*/

        // Draw shadow
        mCircleShadowPaint.setColor(ContextCompat.getColor(getContext(), R.color.middleCircleShadow));
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius, mCircleShadowPaint);
    }
}
