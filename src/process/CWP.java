package process;

import entity.CWPData;
import entity.DPResult;

/**
 * Created by csw on 2016/8/24 15:08.
 * Explain:
 */
public class CWP {

    private CWPData cwpData;
    private DPResult dpResult;

    public CWP() {
        cwpData = new CWPData();
        dpResult = new DPResult();
    }

    public void initData(String craneJsonStr, String hatchJsonStr, String moveJsonStr) {

        cwpData.cranes = InitData.initCrane(craneJsonStr);
        cwpData.hatches = InitData.initHatch(hatchJsonStr);
        cwpData.moves = InitData.initMove(moveJsonStr);
    }

    public void cwpKernel() {
        DP dp = new DP();
        dp.cwpKernel(cwpData.cranes, cwpData.hatches, dpResult);
    }



    public CWPData getCwpData() {
        return cwpData;
    }

    public void setCwpData(CWPData cwpData) {
        this.cwpData = cwpData;
    }

    public DPResult getDpResult() {
        return dpResult;
    }

    public void setDpResult(DPResult dpResult) {
        this.dpResult = dpResult;
    }
}
