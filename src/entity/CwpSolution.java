package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2016/8/25 11:47.
 * Explain:
 */
public class CwpSolution {

    private Double cwpWorkTime;
    private List<List<CwpBlock>> cwpResult;

    public CwpSolution() {
        cwpWorkTime = 0.0;
        cwpResult = new ArrayList<>();
    }

    public Double getCwpWorkTime() {
        return cwpWorkTime;
    }

    public void setCwpWorkTime(Double cwpWorkTime) {
        this.cwpWorkTime = cwpWorkTime;
    }

    public List<List<CwpBlock>> getCwpResult() {
        return cwpResult;
    }

    public void setCwpResult(List<List<CwpBlock>> cwpResult) {
        this.cwpResult = cwpResult;
    }
}
