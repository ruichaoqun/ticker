package com.robinhood.ticker.test;

import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.Log;

import com.robinhood.ticker.TickerDrawMetrics;

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
 * 			<td>2018-06-28 08:57</td>
 * 			<td>Rui chaoqun</td>
 * 			<td>All</td>
 *			<td>Created.</td>
 * 		</tr>
 * </table>
 */
public class TestTickerColumn {
    static final char EMPTY_CHAR = (char) 0;
    private char[] mChars = new char[21];
    private char mOriginalChar = EMPTY_CHAR;
    private char mCurrentChar = EMPTY_CHAR;
    private char mTargetChar = EMPTY_CHAR;
    private int mOriginalIndex;
    private int mCurrentIndex;
    private int mTargetIndex;
    private boolean mIsIncrease;
    private char[] mcurrentCharList;

    private TickerDrawMetrics mMetrics;
    private float mCurrentWidth, mTargetWidth, mOriginalWidth;
    private int mTrueIndex;
    private float mTrueOffset;
    private float charHeight;

    public TestTickerColumn(char targetChar, boolean isIncrease, TickerDrawMetrics metrics) {
        mIsIncrease = isIncrease;
        mMetrics = metrics;
        init();
        setTargetChar(targetChar, isIncrease);
    }

    private void init() {
        String numbers = "0123456789";
        char[] chars = numbers.toCharArray();
        mChars[0] = EMPTY_CHAR;
        for (int i = 0; i < numbers.length(); i++) {
            mChars[1 + i] = chars[i];
            mChars[11 + i] = chars[i];
        }
    }

    /**
     * 设置目标字符
     * @param targetChar
     */
    public void setTargetChar(char targetChar, boolean isIncrease) {
        this.mTargetChar = targetChar;
        this.mIsIncrease = isIncrease;
        mOriginalWidth = mCurrentWidth;
        mTargetWidth = mMetrics.getCharWidth(targetChar);
        caculateTargetIndex();
    }

    public float getCurrentWidth() {
        return mCurrentWidth;
    }

    private void caculateTargetIndex() {
        if (mCurrentChar == EMPTY_CHAR && mTargetChar == EMPTY_CHAR) {
            mCurrentIndex = 0;
            mTargetIndex = 0;
            mcurrentCharList = new char[]{EMPTY_CHAR};
        } else if (mCurrentChar == EMPTY_CHAR && mTargetChar != EMPTY_CHAR) {
            mCurrentIndex = 0;
            mTargetIndex = mTargetChar - '0' + 1;
            mcurrentCharList = new char[mTargetIndex - mCurrentIndex + 1];
            for (int i = mCurrentIndex; i <= mTargetIndex; i++) {
                mcurrentCharList[i - mCurrentIndex] = mChars[i];
            }
        } else if (mCurrentChar != EMPTY_CHAR && mTargetChar == EMPTY_CHAR) {
            mCurrentIndex = mCurrentChar - '0' + 1;
            mTargetIndex = 0;
            mcurrentCharList = new char[]{mCurrentChar, EMPTY_CHAR};
        } else {
            mCurrentIndex = mCurrentChar - '0' + 1;
            mTargetIndex = mTargetChar - '0' + 1;
            if (mIsIncrease) {
                if (mCurrentIndex > mTargetIndex) {
                    mTargetIndex += 10;
                }
                mcurrentCharList = new char[mTargetIndex - mCurrentIndex + 1];
                for (int i = mCurrentIndex; i <= mTargetIndex; i++) {
                    mcurrentCharList[i - mCurrentIndex] = mChars[i];
                }
            } else {
                if (mCurrentIndex < mTargetIndex) {
                    mCurrentIndex += 10;
                }
                mcurrentCharList = new char[mCurrentIndex - mTargetIndex + 1];
                for (int i = mCurrentIndex; i >= mTargetIndex; i--) {
                    mcurrentCharList[mCurrentIndex - i] = mChars[i];
                }
            }
        }
    }

    public void setAnimatorProgress(float progress) {
        if (progress == 1f) {
            this.mCurrentChar = this.mTargetChar;
        }

        mCurrentWidth = mOriginalWidth + (mTargetWidth - mOriginalWidth) * progress;
        final float charHeight = mMetrics.getCharHeight();
        final float totalHeight = charHeight * Math.abs(mCurrentIndex - mTargetIndex);
        final float currentBase = progress * totalHeight;

        final int isIncrease = mIsIncrease ? 1 : -1;
        mTrueIndex = (int) (currentBase / charHeight);
        this.charHeight = charHeight;
        mTrueOffset = (currentBase % charHeight) * isIncrease;
    }

    void onAnimationEnd() {
        final float currentTargetWidth = mMetrics.getCharWidth(mTargetChar);
        if (mCurrentWidth == mTargetWidth && mTargetWidth != currentTargetWidth) {
            this.mCurrentWidth = this.mTargetWidth = currentTargetWidth;
        }
        mOriginalChar = mCurrentChar = mTargetChar;
    }

    public void draw(Canvas canvas, TextPaint textPaint) {
        draw(canvas, textPaint, mTrueIndex, mTrueOffset);
        if (mIsIncrease) {
            draw(canvas, textPaint, mTrueIndex + 1, mTrueOffset - charHeight);
            draw(canvas, textPaint, mTrueIndex - 1, mTrueOffset + charHeight);
        } else {
            draw(canvas, textPaint, mTrueIndex - 1, mTrueOffset - charHeight);
            draw(canvas, textPaint, mTrueIndex + 1, mTrueOffset + charHeight);
        }
    }

    private void draw(Canvas canvas, TextPaint textPaint, int trueIndex, float trueOffset) {
        if (trueIndex >= 0 && trueIndex < mcurrentCharList.length) {
            canvas.drawText(mcurrentCharList, trueIndex, 1, 0f, trueOffset, textPaint);
        }
    }
}
