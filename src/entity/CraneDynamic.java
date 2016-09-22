package entity;

/**
 * Created by csw on 2016/8/25 8:23.
 * Explain:
 */
public class CraneDynamic {

    public Double mCurrentPosition;
    public Integer mCurrentWorkST;

    public Integer mMoveRangeFrom;
    public Integer mMoveRangeTo;

    public CraneDynamic() {
        mCurrentWorkST = 0;
    }

    public Integer getmCurrentWorkST() {
        return mCurrentWorkST;
    }

    public void setmCurrentWorkST(Integer mCurrentWorkST) {
        this.mCurrentWorkST = mCurrentWorkST;
    }

    public Double getmCurrentPosition() {
        return mCurrentPosition;
    }

    public void setmCurrentPosition(Double mCurrentPosition) {
        this.mCurrentPosition = mCurrentPosition;
    }
}
