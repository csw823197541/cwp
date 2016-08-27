package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2016/8/24 16:15.
 * Explain:
 */
public class DPResult {

    public int dpMoveCount;
    public Double dpDistance;
    public List<Pair> dpTraceBack;

    public DPResult() {
        dpMoveCount = 0;
        dpDistance = Double.MAX_VALUE;
        dpTraceBack = new ArrayList<>();
    }

    public int getDpMoveCount() {
        return dpMoveCount;
    }

    public void setDpMoveCount(int dpMoveCount) {
        this.dpMoveCount = dpMoveCount;
    }

    public Double getDpDistance() {
        return dpDistance;
    }

    public void setDpDistance(Double dpDistance) {
        this.dpDistance = dpDistance;
    }

    public List<Pair> getDpTraceBack() {
        return dpTraceBack;
    }

    public void setDpTraceBack(List<Pair> dpTraceBack) {
        this.dpTraceBack = dpTraceBack;
    }
}


