package process;

import entity.Crane;
import entity.DPResult;
import entity.Hatch;
import entity.Pair;

import java.util.List;

/**
 * Created by csw on 2016/8/24 15:07.
 * Explain:
 */
public class DP {

    public int cwpKernel(List<Crane> cranes, List<Hatch> hatches, DPResult dpResult) {

        int nc = cranes.size();
        int nh = hatches.size();
        if (nc <= 0 || nh <= 0) {

        }

        DPResult[][] dp = new DPResult[nc][nh];
        for (int i = 0; i< nc; i++) {
            for (int j= 0; j < nh; j++) {
                dp[i][j] = new DPResult();
            }
        }

        if (hatches.get(0).hatchDynamic.mMoveCount != 0) {
            dp[0][0].dpMoveCount = hatches.get(0).hatchDynamic.mMoveCount;
            dp[0][0].dpDistance = Math.abs(cranes.get(0).craneDynamic.mCurrentPosition - hatches.get(0).hatchDynamic.mCurrentWorkPosition);
            dp[0][0].dpTraceBack.add(new Pair(0, 0));
        } else {
            dp[0][0].dpDistance = 0.0;
        }
        for (int i = 1; i < nc; i++) {
            DPResult cur_dp = new DPResult();
            cur_dp.dpMoveCount = hatches.get(0).hatchDynamic.mMoveCount;
            cur_dp.dpDistance = Math.abs(cranes.get(i).craneDynamic.mCurrentPosition - hatches.get(0).hatchDynamic.mCurrentWorkPosition);
            if (better(cur_dp, dp[i - 1][0])) {
                dp[i][0] = cur_dp;
                dp[i][0].dpTraceBack.add(new Pair(i, 0));
            } else {
                dp[i][0] = dp[i - 1][0];
            }
        }
        for (int j = 1; j < nh; j++) {
            DPResult cur_dp = new DPResult();
            cur_dp.dpMoveCount = hatches.get(j).hatchDynamic.mMoveCount;
            cur_dp.dpDistance = Math.abs(cranes.get(0).craneDynamic.mCurrentPosition - hatches.get(j).hatchDynamic.mCurrentWorkPosition);
            if (better(cur_dp, dp[0][j - 1])) {
                dp[0][j] = cur_dp;
                dp[0][j].dpTraceBack.add(new Pair(0, j));
            } else {
                dp[0][j] = dp[0][j - 1];
            }
        }

        for (int i = 1; i < nc; i++) {
            for (int j = 1; j < nh; j++) {
                // if crane[i] is in hatch[j]
                DPResult cur_dp = new DPResult();
                int k = j;
                for (; k >= 0 && hatches.get(j).hatchDynamic.mCurrentWorkPosition - cranes.get(i).getSafeSpan() <= hatches.get(k).hatchDynamic.mCurrentWorkPosition; k--)
                    ;
                if (k < 0) {
                    cur_dp.dpMoveCount = hatches.get(j).hatchDynamic.mMoveCount;
                    cur_dp.dpDistance = Math.abs(cranes.get(i).craneDynamic.mCurrentPosition - hatches.get(j).hatchDynamic.mCurrentWorkPosition);
                } else {
                    cur_dp.dpMoveCount = hatches.get(j).hatchDynamic.mMoveCount + dp[i - 1][k].dpMoveCount;
                    cur_dp.dpDistance = Math.abs(cranes.get(i).craneDynamic.mCurrentPosition - hatches.get(j).hatchDynamic.mCurrentWorkPosition) + dp[i - 1][k].dpDistance;
                    cur_dp.dpTraceBack = dp[i - 1][k].dpTraceBack;
                }

                // dp compare
                DPResult tmp_dp = new DPResult();
                if (better(dp[i][j - 1], dp[i - 1][j])) {
                    tmp_dp = dp[i][j - 1];
                } else {
                    tmp_dp = dp[i - 1][j];
                }
                if (better(cur_dp, tmp_dp)) {
                    dp[i][j] = cur_dp;
                    dp[i][j].dpTraceBack.add(new Pair(i, j));
                } else {
                    dp[i][j] = tmp_dp;
                }
            }
        }
        dpResult = dp[dp.length - 1][dp.length - 1];
        return dpResult.dpTraceBack.size();


    }

    private boolean better(DPResult cur_dp, DPResult dpResult) {
        if (cur_dp.dpMoveCount > dpResult.dpMoveCount) {
            return true;
        } else if (cur_dp.dpMoveCount == dpResult.dpMoveCount) {
            if (cur_dp.dpDistance < dpResult.dpDistance) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
//        return cur_dp.dpMoveCount > dpResult.dpMoveCount ? true : (cur_dp.dpMoveCount == dpResult.dpMoveCount ? cur_dp.dpDistance < dpResult.dpDistance : false);
    }
}
