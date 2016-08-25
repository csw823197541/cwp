package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2016/8/25 11:49.
 * Explain:
 */
public class CwpBlock {

    private boolean mTrueBlock;
    private String mCraneId;
    private String mHatchId;
    private Double mWorkStartTime;
    private Double mWorkEndTime;
    private Double mWorkCostTime;
    private Integer mMoveCount;
    List<Move> mMoves;

    public CwpBlock() {
        mTrueBlock = true;
        mWorkStartTime = 0.0;
        mWorkEndTime = 0.0;
        mWorkCostTime = 0.0;
        mMoveCount = 0;
        mMoves = new ArrayList<>();
    }

    public boolean ismTrueBlock() {
        return mTrueBlock;
    }

    public void setmTrueBlock(boolean mTrueBlock) {
        this.mTrueBlock = mTrueBlock;
    }

    public String getmCraneId() {
        return mCraneId;
    }

    public void setmCraneId(String mCraneId) {
        this.mCraneId = mCraneId;
    }

    public String getmHatchId() {
        return mHatchId;
    }

    public void setmHatchId(String mHatchId) {
        this.mHatchId = mHatchId;
    }

    public Double getmWorkStartTime() {
        return mWorkStartTime;
    }

    public void setmWorkStartTime(Double mWorkStartTime) {
        this.mWorkStartTime = mWorkStartTime;
    }

    public Double getmWorkEndTime() {
        return mWorkEndTime;
    }

    public void setmWorkEndTime(Double mWorkEndTime) {
        this.mWorkEndTime = mWorkEndTime;
    }

    public Integer getmMoveCount() {
        return mMoveCount;
    }

    public void setmMoveCount(Integer mMoveCount) {
        this.mMoveCount = mMoveCount;
    }

    public List<Move> getmMoves() {
        return mMoves;
    }

    public void setmMoves(List<Move> mMoves) {
        this.mMoves = mMoves;
    }
}
