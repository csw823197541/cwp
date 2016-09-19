package entity;

import java.io.Serializable;

/**
 * Created by csw on 2016/8/25 8:21.
 * Explain:
 */
public class HatchDynamic implements Serializable{

    public Double mCurrentWorkPosition;
    public Integer mMoveCount;
    public Integer mCurrentMoveIdx;

    public HatchDynamic() {
        mMoveCount = 0;
        mCurrentMoveIdx = 0;
        mCurrentWorkPosition = 0.0;
    }

    public Double getmCurrentWorkPosition() {
        return mCurrentWorkPosition;
    }

    public void setmCurrentWorkPosition(Double mCurrentWorkPosition) {
        this.mCurrentWorkPosition = mCurrentWorkPosition;
    }

    public Integer getmMoveCount() {
        return mMoveCount;
    }

    public void setmMoveCount(Integer mMoveCount) {
        this.mMoveCount = mMoveCount;
    }

    public Integer getmCurrentMoveIdx() {
        return mCurrentMoveIdx;
    }

    public void setmCurrentMoveIdx(Integer mCurrentMoveIdx) {
        this.mCurrentMoveIdx = mCurrentMoveIdx;
    }
}
