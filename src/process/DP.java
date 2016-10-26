package process;

import entity.Crane;
import entity.DPResult;
import entity.Hatch;
import entity.Pair;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2016/8/24 15:07.
 * Explain:
 */
public class DP {

    public DPResult cwpKernel(List<Crane> cranes, List<Hatch> hatches, DPResult dpResult) {

        int nc = cranes.size();
        int nh = hatches.size();
        if (nc <= 0 || nh <= 0) {

        }

        DPResult[][] dp = new DPResult[nc][nh];
        for (int i = 0; i < nc; i++) {
            for (int j = 0; j < nh; j++) {
                dp[i][j] = new DPResult();
            }
        }

//        changeDynamicMoveCount(hatches);

        //change the hatchDynamic mMoveCount of last selected hatch
        DPResult dpResult_copy = dpResult.deepCopy();
        List<Pair> trace_back = dpResult_copy.dpTraceBack;
        if (!trace_back.isEmpty()) {
            //get out method
            int nr = trace_back.size();
            for (int t = 0; t < nr; t++) {
                Hatch hatch = hatches.get((Integer) trace_back.get(t).second);
                if (hatch.hatchDynamic.mMoveCount != 0) {
                    hatch.hatchDynamic.mMoveCountDY = 1000;
                }
            }
        }

        if (hatches.get(0).hatchDynamic.mMoveCountDY != 0) {
            dp[0][0].dpMoveCount = hatches.get(0).hatchDynamic.mMoveCountDY;
            dp[0][0].dpDistance = Math.abs(cranes.get(0).craneDynamic.mCurrentPosition - hatches.get(0).hatchDynamic.mCurrentWorkPosition);
            dp[0][0].dpTraceBack.add(new Pair(0, 0));
        } else {
            dp[0][0].dpDistance = 0.0;
        }
        for (int i = 1; i < nc; i++) {
            DPResult cur_dp = new DPResult();
            cur_dp.dpMoveCount = hatches.get(0).hatchDynamic.mMoveCountDY;
            cur_dp.dpDistance = Math.abs(cranes.get(i).craneDynamic.mCurrentPosition - hatches.get(0).hatchDynamic.mCurrentWorkPosition);
            if (better(cur_dp, dp[i - 1][0])) {
                dp[i][0] = cur_dp.deepCopy();
                dp[i][0].dpTraceBack.add(new Pair(i, 0));
            } else {
                dp[i][0] = dp[i - 1][0].deepCopy();
            }
        }
        for (int j = 1; j < nh; j++) {
            DPResult cur_dp = new DPResult();
            cur_dp.dpMoveCount = hatches.get(j).hatchDynamic.mMoveCountDY;
            cur_dp.dpDistance = Math.abs(cranes.get(0).craneDynamic.mCurrentPosition - hatches.get(j).hatchDynamic.mCurrentWorkPosition);
            if (better(cur_dp, dp[0][j - 1])) {
                dp[0][j] = cur_dp.deepCopy();
                dp[0][j].dpTraceBack.add(new Pair(0, j));
            } else {
                dp[0][j] = dp[0][j - 1].deepCopy();
            }
        }

        for (int i = 1; i < nc; i++) {
            Crane crane = cranes.get(i);
            for (int j = crane.craneDynamic.mMoveRangeFrom; j < crane.craneDynamic.mMoveRangeTo + 1; j++) {
                // if crane[i] is in hatch[j]
                DPResult cur_dp = new DPResult();
//                int k = j;
                int k = cranes.get(i - 1).craneDynamic.mMoveRangeTo;
                for (; k >= cranes.get(i - 1).craneDynamic.mMoveRangeFrom && hatches.get(j).hatchDynamic.mCurrentWorkPosition - 2 * cranes.get(i).getSafeSpan() <= hatches.get(k).hatchDynamic.mCurrentWorkPosition; k--)
                    ;
                if (k < cranes.get(i - 1).craneDynamic.mMoveRangeFrom) {
                    cur_dp.dpMoveCount = hatches.get(j).hatchDynamic.mMoveCountDY;
                    cur_dp.dpDistance = Math.abs(cranes.get(i).craneDynamic.mCurrentPosition - hatches.get(j).hatchDynamic.mCurrentWorkPosition);
                } else {
                    cur_dp.dpMoveCount = hatches.get(j).hatchDynamic.mMoveCountDY + dp[i - 1][k].dpMoveCount;
                    cur_dp.dpDistance = Math.abs(cranes.get(i).craneDynamic.mCurrentPosition - hatches.get(j).hatchDynamic.mCurrentWorkPosition) + dp[i - 1][k].dpDistance;
                    cur_dp.dpTraceBack = dp[i - 1][k].dpTraceBack;
                }

                // dp compare ?
                DPResult tmp_dp = new DPResult();

//                if (better(dp[i][j - 1], dp[i - 1][j])) {
//                    tmp_dp = dp[i][j - 1].deepCopy();
//                } else {
//                    tmp_dp = dp[i - 1][j].deepCopy();
//                }
//
//                if (better(cur_dp, tmp_dp)) {
//                    dp[i][j] = cur_dp.deepCopy();
//                    dp[i][j].dpTraceBack.add(new Pair(i, j));
//                } else {
//                    dp[i][j] = tmp_dp.deepCopy();
//                }

                if (j > crane.craneDynamic.mMoveRangeFrom) {
                    tmp_dp = dp[i][j - 1].deepCopy();
                    if (better(cur_dp, tmp_dp)) {
                        dp[i][j] = cur_dp.deepCopy();
                        if (hatches.get(j).hatchDynamic.mMoveCount > 0) {
                            dp[i][j].dpTraceBack.add(new Pair(i, j));
                        }
                    } else {
                        dp[i][j] = tmp_dp.deepCopy();
                    }
                } else {
                    dp[i][j] = cur_dp.deepCopy();
                    if (hatches.get(j).hatchDynamic.mMoveCount > 0) {
                        dp[i][j].dpTraceBack.add(new Pair(i, j));
                    }
                }
            }
        }

        dpResult = dp[nc - 1][nh - 1].deepCopy();
        return dpResult;
    }

    private void changeDynamicMoveCount(List<Hatch> hatches) {
        for (int j = 0; j < hatches.size(); j++) {
            Hatch hatch = hatches.get(j);
            if (hatch.getMoveCount() != 0) {
                if (j == 0) {
                    hatch.hatchDynamic.mMoveCountDY = 2 * hatch.hatchDynamic.mMoveCount + hatches.get(j + 1).hatchDynamic.mMoveCount;
                } else if (j == hatches.size() - 1) {
                    hatch.hatchDynamic.mMoveCountDY = 2 * hatch.hatchDynamic.mMoveCount + hatches.get(j - 1).hatchDynamic.mMoveCount;
                } else {
                    hatch.hatchDynamic.mMoveCountDY = hatches.get(j - 1).hatchDynamic.mMoveCount + 2 * hatch.hatchDynamic.mMoveCount + hatches.get(j + 1).hatchDynamic.mMoveCount;
                }
            }
        }
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
