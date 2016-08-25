package process;

import entity.*;

import java.util.*;

/**
 * Created by csw on 2016/8/24 15:08.
 * Explain:
 */
public class CWP {

    private CwpData cwpData;
    private DPResult dpResult;

    public CWP() {
        cwpData = new CwpData();
        dpResult = new DPResult();
    }

    public void initData(String craneJsonStr, String hatchJsonStr, String moveJsonStr) {

        List<Crane> inputCranes = sortCraneByPosition(InitData.initCrane(craneJsonStr));
        for (int k = 0; k < 4; k++) {
            cwpData.cranes.add(inputCranes.get(k));
        }

        cwpData.hatches = sortHatchById(InitData.initHatch(hatchJsonStr));
        cwpData.moves = InitData.initMove(moveJsonStr);

        int i = 0;
        for (Hatch hatch : cwpData.hatches) {
            cwpData.hatchIdxMap.put(hatch.getHatchId(), i++);
        }
        for (Move move : cwpData.moves) {
            cwpData.hatches.get(cwpData.hatchIdxMap.get(move.getHatchId())).mMoves.add(move);
        }
    }

    private List<Hatch> sortHatchById(List<Hatch> hatches) {
        Collections.sort(hatches, new Comparator<Hatch>() {
            @Override
            public int compare(Hatch o1, Hatch o2) {
                return o1.getHatchId().compareTo(o2.getHatchId());
            }
        });
        return hatches;
    }

    private List<Crane> sortCraneByPosition(List<Crane> cranes) {
        Collections.sort(cranes, new Comparator<Crane>() {
            @Override
            public int compare(Crane o1, Crane o2) {
                return o1.craneDynamic.mCurrentPosition > o2.craneDynamic.mCurrentPosition ? 1 :
                        (o1.craneDynamic.mCurrentPosition == o2.craneDynamic.mCurrentPosition ? 0 : -1);
            }
        });
        return cranes;
    }

    public void cwpKernel() {
        DP dp = new DP();
        dp.cwpKernel(cwpData.cranes, cwpData.hatches, dpResult);
    }

    public void cwpSearch(int depth) {
        boolean isFinish = true;
        for (int i = 0; i < cwpData.hatches.size(); i++) {
            if (cwpData.hatches.get(i).hatchDynamic.mMoveCount != 0) {
                isFinish = false;
            }
        }
        if (isFinish) {
            //save best solution
            if (cwpData.cwpBestSolution.getCwpWorkTime() == 0 ||
                    cwpData.cwpCurSolution.getCwpWorkTime() < cwpData.cwpBestSolution.getCwpWorkTime()) {
                cwpData.cwpBestSolution = cwpData.cwpCurSolution;
            }
        }
        //search width
        int branch_width = 1;
        if (depth < cwpData.cwpBranchLimit) {
            branch_width = cwpData.cwpBranchWidth;
        }

        List<DPResult> dp_Results = new ArrayList<>();
        for (int i = 0; i < branch_width; i++) {
            dp_Results.add(new DPResult());
        }
        DP dp = new DP();
        dp.cwpKernel(cwpData.cranes, cwpData.hatches, dp_Results.get(0));
        branch_width = Math.min(branch_width, dp_Results.get(0).dpTraceBack.size());

        if (branch_width > 1) {//?
            List<Pair> mc = new ArrayList<>();//<number of hatch, moveCount>
            for (int j = 0; j < dp_Results.get(0).dpTraceBack.size(); j++) {
                int h = (int) dp_Results.get(0).dpTraceBack.get(j).second;
                mc.add(new Pair(h, cwpData.hatches.get(h).hatchDynamic.mMoveCount));
            }
            Collections.sort(mc, new Comparator<Pair>() {
                @Override
                public int compare(Pair o1, Pair o2) {
//                    return Integer.valueOf((int) o1.second).compareTo(Integer.valueOf((int) o2.second));
                    return (int) o1.second > (int) o2.second ? 1 : (o1.second == (int) o2.second ? 0 : -1);
                }
            });
            for (int j = 1; j < branch_width; j++) {
                cwpData.hatches.get((int) mc.get(j - 1).first).hatchDynamic.mMoveCount = 0;
                dp.cwpKernel(cwpData.cranes, cwpData.hatches, dp_Results.get(j));
                cwpData.hatches.get((int) mc.get(j - 1).first).hatchDynamic.mMoveCount = (Integer) mc.get(j - 1).second;
            }
        }

        for (int i = 0; i < branch_width; i++) {

            //copy
            List<CraneDynamic> tmp_crane_dy = new ArrayList<>();
            for (int j = 0; j < cwpData.cranes.size(); j++) {
                CraneDynamic craneDynamic = new CraneDynamic();
                craneDynamic.setmCurrentPosition(cwpData.cranes.get(j).craneDynamic.mCurrentPosition);
                tmp_crane_dy.add(craneDynamic);
            }
            List<HatchDynamic> tmp_hatch_dy = new ArrayList<>();
            for (int j = 0; j < cwpData.hatches.size(); j++) {
                HatchDynamic hatchDynamic = new HatchDynamic();
                hatchDynamic.setmMoveCount(cwpData.hatches.get(j).hatchDynamic.mMoveCount);
                hatchDynamic.setmCurrentMoveIdx(cwpData.hatches.get(j).hatchDynamic.mCurrentMoveIdx);
                hatchDynamic.setmCurrentWorkPosition(cwpData.hatches.get(j).hatchDynamic.mCurrentWorkPosition);
                tmp_hatch_dy.add(hatchDynamic);
            }
            CwpSolution tmp_solution = cwpData.cwpCurSolution;

            //recursive
            List<CwpBlock> cwp_block = new ArrayList<>();
            Double time = cwpRealWork(dp_Results.get(i), cwpData.cwpCurSolution.getCwpWorkTime(),
                    cwpData.cranes, cwpData.hatches, cwp_block);
            cwpData.cwpCurSolution.getCwpResult().add(cwp_block);//?
            cwpData.cwpCurSolution.setCwpWorkTime(cwpData.cwpCurSolution.getCwpWorkTime() + time);
            cwpSearch(depth + 1);

            //recovery data
            for (int k = 0; k < tmp_crane_dy.size(); k++) {
                cwpData.cranes.get(k).craneDynamic = tmp_crane_dy.get(k);
            }
            for (int k = 0; k < tmp_hatch_dy.size(); k++) {
                cwpData.hatches.get(k).hatchDynamic = tmp_hatch_dy.get(k);
            }
            cwpData.cwpCurSolution = tmp_solution;
        }

    }

    private Double cwpRealWork(DPResult dpResult, Double cwpWorkTime, List<Crane> cranes, List<Hatch> hatches, List<CwpBlock> cwp_block) {
        return null;
    }


    public CwpData getCwpData() {
        return cwpData;
    }

    public void setCwpData(CwpData cwpData) {
        this.cwpData = cwpData;
    }

    public DPResult getDpResult() {
        return dpResult;
    }

    public void setDpResult(DPResult dpResult) {
        this.dpResult = dpResult;
    }
}
