package com.propro.musicplayerapp;

import android.content.Context;
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
    private RectF mSliceOval = new RectF();

    private float innerRadiusRatio = 0.55F;

    // color for your slice
    private int[] colors = new int[]{Color.GRAY};

    private int mCenterX;
    private int mCenterY;

    private OnSliceClickListener mOnSliceClickListener;
    private int mTouchSlop;

    private boolean mPressed;
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

                            // draw new progress
                            this.invalidate();
                        }
                        else {
                            if(Math.abs(currX - mLatestDownX) > mTouchSlop || Math.abs(currY - mLatestDownY) > mTouchSlop) mPressed = false;
                            break;
                        }
                    }
                    // in play button
                    else if (distanceSquare < innerRadiusSquare){
                        if(Math.abs(currX - mLatestDownX) > mTouchSlop || Math.abs(currY - mLatestDownY) > mTouchSlop) mPressed = false;
                        break;
                    }
                }
            case MotionEvent.ACTION_UP:

                /* ------- SLICE INDEXES EXPLAINED ---------
                // -2 = playButton
                // -1 = progressBar
                // 0 = nextSongButton
                // 1 = shuffleButton
                // 2 = repeatButton
                // 3 = previousSongButton
                   ----------------------------------------- */
                if(mPressed){
                    dx = (int) currX - mCenterX;
                    dy = (int) currY - mCenterY;
                    distanceSquare = dx * dx + dy * dy;

                    // if the distance between touchpoint and centerpoint is smaller than outerRadius and longer than innerRadius, then we're in the clickable area
                    if(distanceSquare > innerRadiusSquare && distanceSquare < outerRadiusSquare){

                        //get the angle to detect which slice is currently being click
                        double angle = Math.atan2(dy, dx);
                        double rawSliceIndex = -1;
                        Log.d("Angle: ", String.valueOf(angle));

                        if(angle >= 0 && angle < Math.PI/4){
                            rawSliceIndex = 0;
                        }else if(angle >= Math.PI/4 && angle < Math.PI/2){
                            rawSliceIndex = 1;
                        }else if(angle >= Math.PI/2 && angle < Math.PI*0.75){
                            rawSliceIndex = 2;
                        }else if(angle >= Math.PI*0.75 && angle < Math.PI){
                            rawSliceIndex = 3;
                        }

                        if(mOnSliceClickListener != null){
                            mOnSliceClickListener.onSlickClick((int) rawSliceIndex, progress);
                        }
                    }
                    // if the distance between touchpoint and centerpoint is smaller than innerRadius, the playButton is clicked
                    else if (distanceSquare < innerRadiusSquare){
                        if(mOnSliceClickListener != null){
                            mOnSliceClickListener.onSlickClick(-2, progress);
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
        mPaint.setColor(Color.BLACK);
        canvas.drawArc(mSliceOval, 0, 180, true, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        canvas.drawArc(mSliceOval, -180, 180, true, mPaint);

        /*mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        canvas.drawArc(mSliceOval, 0, 360, true, mPaint);*/

        // draw progressBar
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#592e00"));
        canvas.drawArc(mSliceOval, -180, progress*1.8f, true, mPaint);

        /*mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        canvas.drawArc(mSliceOval, -180, progress*1.8f, true, mPaint);*/

        // draw slice
        for(int i = 0; i < mSlices; i++){
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(colors[i % colors.length]);
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

            startAngle += degreeStep;
        }

        //draw center circle
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius, mPaint);
        /*mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius, mPaint);*/
    }
}
