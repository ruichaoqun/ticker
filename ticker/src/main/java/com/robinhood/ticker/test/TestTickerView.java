package com.robinhood.ticker.test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.robinhood.ticker.TickerDrawMetrics;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description.</p>
 *
 * <b>Maintenance History</b>:
 * <table>
 * 		<tr>
 * 			<th>Date</th>
 * 			<th>Developer</th>
 * 			<th>Target</th>
 * 			<th>Content</th>
 * 		</tr>
 * 		<tr>
 * 			<td>2018-06-28 08:56</td>
 * 			<td>Rui chaoqun</td>
 * 			<td>All</td>
 *			<td>Created.</td>
 * 		</tr>
 * </table>
 */
public class TestTickerView extends View {

    private List<TestTickerColumn> mTestTickerColumns = new ArrayList<>();
    private int mNumber = 0;
    private ValueAnimator mValueAnimator = ValueAnimator.ofFloat(1);
    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    TickerDrawMetrics mTickerDrawMetrics = new TickerDrawMetrics(mTextPaint);
    private final Rect viewBounds = new Rect();
    private Paint mPaint = new Paint();

    public TestTickerView(Context context) {
        this(context,null);
    }

    public TestTickerView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TestTickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTextPaint.setTextSize(100);
        mTickerDrawMetrics.invalidate();
        mValueAnimator.setDuration(2000);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.setStartDelay(0);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 0, size = mTestTickerColumns.size(); i < size; i++) {
                    final TestTickerColumn column = mTestTickerColumns.get(i);
                    column.setAnimatorProgress(animation.getAnimatedFraction());
                }
                invalidate();
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                for (int i = 0; i < mTestTickerColumns.size(); i++) {
                    final TestTickerColumn column = mTestTickerColumns.get(i);
                    column.onAnimationEnd();
                }
            }
        });

    }

    public void setText(String s){
        int number = 0;
        if(!TextUtils.isEmpty(s)){
            try {
                number = Integer.valueOf(s);
            }catch (NumberFormatException e){

            }
        }
        setNumber(number,true);
    }

    public void setNumber(int number,boolean animator) {
        if (mNumber == number) {
            return;
        }

        final boolean isIncrease;
        if (mNumber < number) {
            isIncrease = true;
        } else {
            isIncrease = false;
        }
        mNumber = number;

        // First remove any zero-width columns
        for (int i = 0; i < mTestTickerColumns.size(); ) {
            final TestTickerColumn tickerColumn = mTestTickerColumns.get(i);
            if (tickerColumn.getCurrentWidth() > 0) {
                i++;
            } else {
                mTestTickerColumns.remove(i);
            }
        }

        char[] chars = String.valueOf(number).toCharArray();
        if(mTestTickerColumns.size() >= chars.length){
            for (int i = 0; i < mTestTickerColumns.size(); i++) {
                if(i < mTestTickerColumns.size() - chars.length){
                    mTestTickerColumns.get(i).setTargetChar(TestTickerColumn.EMPTY_CHAR,isIncrease);
                }else{
                    mTestTickerColumns.get(i).setTargetChar(chars[i - (mTestTickerColumns.size() - chars.length)],isIncrease);
                }
            }
        }else{
            for (int i = 0; i < chars.length; i++) {
                mTestTickerColumns.add(i,new TestTickerColumn(chars[i],isIncrease,mTickerDrawMetrics));
            }
        }

        if(animator){
            mValueAnimator.start();
        }else{
            for (int i = 0, size = mTestTickerColumns.size(); i < size; i++) {
                final TestTickerColumn column = mTestTickerColumns.get(i);
                column.setAnimatorProgress(1);
            }
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        viewBounds.set(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(),
                height - getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0,getHeight()/2,getWidth(),getHeight()/2,mPaint);
        canvas.drawLine(getWidth()/2,0,getWidth()/2,getHeight(),mPaint);
        canvas.save();
        realignAndClipCanvasForGravity(canvas);
        canvas.translate(0f, mTickerDrawMetrics.getCharBaseline());
        for (int i = 0, size = mTestTickerColumns.size(); i < size; i++) {
            final TestTickerColumn column = mTestTickerColumns.get(i);
            column.draw(canvas, mTextPaint);
            canvas.translate(column.getCurrentWidth(), 0f);
        }

        canvas.restore();
    }

    private void realignAndClipCanvasForGravity(Canvas canvas) {
        float currentWidth = 0;
        for (int i = 0, size = mTestTickerColumns.size(); i < size; i++) {
            currentWidth += mTestTickerColumns.get(i).getCurrentWidth();
        }
        final float currentHeight = mTickerDrawMetrics.getCharHeight();
        realignAndClipCanvasForGravity(canvas, viewBounds, currentWidth, currentHeight);
    }

    // VisibleForTesting
    static void realignAndClipCanvasForGravity(Canvas canvas, Rect viewBounds,
                                               float currentWidth, float currentHeight) {
        final int availableWidth = viewBounds.width();
        final int availableHeight = viewBounds.height();

        float translationX = 0;
        float translationY = 0;
        translationY = viewBounds.top + (availableHeight - currentHeight) / 2f;
        translationX = viewBounds.left + (availableWidth - currentWidth) / 2f;
        canvas.translate(translationX ,translationY);
        canvas.clipRect(0f, 0f, currentWidth, currentHeight);
    }
}
