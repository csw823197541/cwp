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

    private Map<Integer, List<Hatch>> craneMoveFromToMap;

    public CWP() {
        cwpData = new CwpData();
        dpResult = new DPResult();
        craneMoveFromToMap = new HashMap<>();
    }

    public void initData(String craneJsonStr, String hatchJsonStr, String moveJsonStr) {

        List<Crane> inputCranes = sortCraneByPosition(InitData.initCrane(craneJsonStr));
        int craneSize = inputCranes.size() >= 4 ? 4 : inputCranes.size();
        for (int k = 0; k < craneSize; k++) {
            cwpData.cranes.add(inputCranes.get(k));
        }

        cwpData.hatches = sortHatchById(InitData.initHatch(hatchJsonStr));
        cwpData.moves = InitData.initMove(moveJsonStr);

        int i = 0;
        for (Hatch hatch : cwpData.hatches) {
            cwpData.hatchIdxMap.put(hatch.getHatchId(), i++);
        }

        sortMoveByMoveOrder(cwpData.moves);
        for (Move move : cwpData.moves) {
            cwpData.hatches.get(cwpData.hatchIdxMap.get(move.getHatchId())).mMoves.add(move);
        }

        //init hatchDynamic.mCurrentWorkPosition
        for (Hatch hatch : cwpData.hatches) {
            if (hatch.hatchDynamic.mMoveCount != 0) {
                hatch.hatchDynamic.mCurrentWorkPosition = hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx).getHorizontalPosition();
            }
        }

        //init crane move in hatch from where to where
        for (int c = 0; c < cwpData.cranes.size(); c++) {
            craneMoveFromToMap.put(c, new ArrayList<Hatch>());
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

    private List<Move> sortMoveByMoveOrder(List<Move> moves) {
        Collections.sort(moves, new Comparator<Move>() {
            @Override
            public int compare(Move o1, Move o2) {
                return o1.getMoveOrder().compareTo(o2.getMoveOrder());
            }
        });
        return moves;
    }

    //test DP
    public void cwpKernel() {
        DP dp = new DP();
        dp.cwpKernel(cwpData.cranes, cwpData.hatches, dpResult);
    }

    //divide the crane move range
    public void craneMoveRange() {
        int nc = cwpData.cranes.size();
        int nh = cwpData.hatches.size();

        int mean = 0;
        int allMoveCount = 0;
        for (Hatch hatch : cwpData.hatches) {
            allMoveCount += hatch.hatchDynamic.mMoveCount;
        }
        mean = allMoveCount / nc;

        int c = 0;
        int tmpMoveCount = 0;
        for (int j = 0; j < nh; j++) {
            Hatch hatch = cwpData.hatches.get(j);
            tmpMoveCount += hatch.hatchDynamic.mMoveCount;
            int meanL = (int) (mean - mean * 0.1);
            int meanR = (int) (mean + mean * 0.1);
            if (tmpMoveCount >= meanL && tmpMoveCount <= meanR) {
                if (c == cwpData.cranes.size()) {
                    c = cwpData.cranes.size() - 1;
                }
                craneMoveFromToMap.get(c).add(hatch);
                Crane crane = cwpData.cranes.get(c);
//                crane.craneDynamic.mMoveRangeTo = hatch.getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeTo = j;
                int cc = craneMoveFromToMap.get(c).size();
//                crane.craneDynamic.mMoveRangeFrom = cwpData.hatches.get(j + 1 - cc).getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeFrom = j + 1 - cc;
                c++;
                tmpMoveCount = 0;
            }
            else if (tmpMoveCount > meanR) {
                if (c == cwpData.cranes.size()) {
                    c = cwpData.cranes.size() - 1;
                }
                hatch.hatchDynamic.mMoveCountL = hatch.hatchDynamic.mMoveCount - (tmpMoveCount - mean);
                hatch.hatchDynamic.mMoveCountR = tmpMoveCount - mean;
                craneMoveFromToMap.get(c).add(hatch);
                Crane crane = cwpData.cranes.get(c);
//                crane.craneDynamic.mMoveRangeTo = hatch.getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeTo = j;
                int cc = craneMoveFromToMap.get(c).size();
//                crane.craneDynamic.mMoveRangeFrom = cwpData.hatches.get(j + 1 - cc).getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeFrom = j + 1 - cc;
                c++;
                tmpMoveCount = hatch.hatchDynamic.mMoveCountR;
            }
            else {
                if (c == cwpData.cranes.size()) {
                    c = cwpData.cranes.size() - 1;
                }
                craneMoveFromToMap.get(c).add(hatch);
                Crane crane = cwpData.cranes.get(c);
//                crane.craneDynamic.mMoveRangeTo = hatch.getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeTo = j;
                int cc = craneMoveFromToMap.get(c).size();
//                crane.craneDynamic.mMoveRangeFrom = cwpData.hatches.get(j + 1 - cc).getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeFrom = j + 1 - cc;
            }
        }
    }

    public void cwpSearch(int depth) {
        boolean isFinish = true;
        for (int j = 0; j < cwpData.hatches.size(); j++) {
            if (cwpData.hatches.get(j).hatchDynamic.mMoveCount != 0) {
                isFinish = false;
            }
        }
        if (isFinish) {
            //save best solution
            if (cwpData.cwpBestSolution.getCwpWorkTime() == 0 ||
                    cwpData.cwpCurSolution.getCwpWorkTime() < cwpData.cwpBestSolution.getCwpWorkTime()) {
                cwpData.cwpBestSolution = cwpData.cwpCurSolution.deepCopy();
            }
        }
        //search width
        int branch_width = 1;
        if (depth < cwpData.cwpBranchLimit) {
            branch_width = cwpData.cwpBranchWidth;
        }

        List<DPResult> dp_Results = new ArrayList<>();

//        for (int i = 0; i < branch_width; i++) {
//            dp_Results.add(new DPResult());
//        }


        DPResult dpResultLast = new DPResult();
        dpResultLast = dpResult.deepCopy();

        //
        DP dp = new DP();
        dpResult = dp.cwpKernel(cwpData.cranes, cwpData.hatches, dpResult);

        DPResult dpResultCur = new DPResult();
        dpResultCur = dpResult.deepCopy();

        //
        int nc = cwpData.cranes.size();
        int dpLastSize = dpResultLast.dpTraceBack.size();
        int dpCurSize = dpResultCur.dpTraceBack.size();
        int[] costTime = new int[nc];
        for (int i = 0; i < nc; i++) {
            int curHatchIdx = findHatchIdxByCraneIdx(i, dpResultCur);
            if (curHatchIdx != -1)
                costTime[i] = countWorkTime(cwpData.cranes.get(i), cwpData.hatches.get(curHatchIdx));
        }
        for (int i = 0; i < nc; i++) {
            int lastHatchIdx = findHatchIdxByCraneIdx(i, dpResultLast);
            int curHatchIdx = findHatchIdxByCraneIdx(i, dpResultCur);
            if (lastHatchIdx != -1 && curHatchIdx != -1) {
                if (lastHatchIdx != curHatchIdx) {
                    Hatch lastHatch = cwpData.hatches.get(lastHatchIdx);
                    Hatch curHatch = cwpData.hatches.get(curHatchIdx);
                    if (lastHatch.hatchDynamic.mMoveCount > 0) {
                        int leftHatchIdx = findHatchIdxByCraneIdx(i - 1 , dpResultCur);
                        int rightHatchIdx = findHatchIdxByCraneIdx(i + 1, dpResultCur);
                        if (leftHatchIdx == -1 && rightHatchIdx != -1) {

                        } else if (leftHatchIdx != -1 && rightHatchIdx != -1) {
                            dpResultCur.dpTraceBack.remove(i);
                        } else if (leftHatchIdx != -1 && rightHatchIdx == -1) {

                        }
                    }
                } else {

                }
            }
        }

        dp_Results.add(dpResultCur);
        dpResult = dpResultCur.deepCopy();

        branch_width = Math.min(branch_width, dp_Results.get(0).dpTraceBack.size());

//        if (depth > 50) {
//            branch_width = 0;
//        }

//        if (branch_width > 1) {//?
//            List<Pair> mc = new ArrayList<>();//<number of hatch, moveCount>
//            for (int j = 0; j < dp_Results.get(0).dpTraceBack.size(); j++) {
//                int h = (int) dp_Results.get(0).dpTraceBack.get(j).second;
//                mc.add(new Pair(h, cwpData.hatches.get(h).hatchDynamic.mMoveCount));
//            }
//            Collections.sort(mc, new Comparator<Pair>() {
//                @Override
//                public int compare(Pair o1, Pair o2) {
////                    return Integer.valueOf((int) o1.second).compareTo(Integer.valueOf((int) o2.second));
//                    return (int) o1.second > (int) o2.second ? 1 : (o1.second == (int) o2.second ? 0 : -1);
//                }
//            });
//            for (int j = 1; j < branch_width; j++) {
//                cwpData.hatches.get((int) mc.get(j - 1).first).hatchDynamic.mMoveCount = 0;
//                dp.cwpKernel(cwpData.cranes, cwpData.hatches, dp_Results.get(j));
//                cwpData.hatches.get((int) mc.get(j - 1).first).hatchDynamic.mMoveCount = (Integer) mc.get(j - 1).second;
//            }
//        }

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
            CwpSolution tmp_solution = cwpData.cwpCurSolution.deepCopy();

            //recursive
            List<CwpBlock> cwp_block = new ArrayList<>();
            Integer time = cwpRealWork(dp_Results.get(i), cwpData.cwpCurSolution.getCwpWorkTime(),
                    cwpData.cranes, cwpData.hatches, cwp_block);
            cwpData.cwpCurSolution.getCwpResult().add(cwp_block);//?
            cwpData.cwpCurSolution.setCwpWorkTime(cwpData.cwpCurSolution.getCwpWorkTime() + time);
            cwpSearch(depth + 1);//?

            //recovery data
            for (int k = 0; k < tmp_crane_dy.size(); k++) {
                cwpData.cranes.get(k).craneDynamic = tmp_crane_dy.get(k).deepCopy();
            }
            for (int k = 0; k < tmp_hatch_dy.size(); k++) {
                cwpData.hatches.get(k).hatchDynamic = tmp_hatch_dy.get(k).deepCopy();
            }
            cwpData.cwpCurSolution = tmp_solution.deepCopy();
        }

    }

    private int countWorkTime(Crane crane, Hatch hatch) {
        int min_time = 0;
        Double last_position = hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx).
                getHorizontalPosition();
        for (int k = hatch.hatchDynamic.mCurrentMoveIdx; k < hatch.getMoveCount(); k++) {
            if (hatch.getmMoves().get(k).getHorizontalPosition().doubleValue() != last_position.doubleValue()) {
                break;
            }
            min_time += cost(crane, hatch.getmMoves().get(k).getMoveType());
        }

        return min_time;
    }

    private int findHatchIdxByCraneIdx(int craneIdx, DPResult dpResult) {
        int hatchIdx = -1;
        List<Pair> dpTraceBack = dpResult.dpTraceBack;
        for (int i = 0; i < dpTraceBack.size(); i++) {
            if (craneIdx == (int) dpTraceBack.get(i).first) {
                hatchIdx = (int) dpTraceBack.get(i).second;
            }
        }
        return hatchIdx;
    }

    private Integer cwpRealWork(DPResult dpResult, Integer start_time, List<Crane> cranes,
                               List<Hatch> hatches, List<CwpBlock> cwp_block) {

        List<Pair> trace_back = dpResult.dpTraceBack;
        if (trace_back.isEmpty()) {
            //get out method
        }
        int nr = trace_back.size();

        //init cwp block & find min work time
        for (int i = 0; i < nr; i++) {
            cwp_block.add(new CwpBlock());
        }
        int min_time = Integer.MAX_VALUE;
        for (int t = 0; t < nr; t++) {
            Crane crane = cranes.get((Integer) trace_back.get(t).first);
            Hatch hatch = hatches.get((Integer) trace_back.get(t).second);

            cwp_block.get(t).setmCraneId(crane.getCraneId());
            cwp_block.get(t).setmHatchId(hatch.getHatchId());
            cwp_block.get(t).setmWorkStartTime(start_time); //crane work startTime

            if (hatch.hatchDynamic.mMoveCount == 0) {
                cwp_block.get(t).setmTrueBlock(false);
                continue;
            }
            Double last_position = hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx).
                    getHorizontalPosition();
            for (int k = hatch.hatchDynamic.mCurrentMoveIdx; k < hatch.getMoveCount(); k++) {
                if (hatch.getmMoves().get(k).getHorizontalPosition().doubleValue() != last_position.doubleValue()) {
                    break;
                }
                cwp_block.get(t).setmWorkCostTime(cwp_block.get(t).getmWorkCostTime() + cost(crane, hatch.getmMoves().get(k).getMoveType()));
            }

            min_time = Math.min(min_time, cwp_block.get(t).getmWorkCostTime());
        }

        //work
        for (int t = 0; t < nr; t++) {
            CwpBlock cwpBlock = cwp_block.get(t);

            if (!cwpBlock.ismTrueBlock()) {
                continue;
            }
            Crane crane = cranes.get((Integer) trace_back.get(t).first);
            Hatch hatch = hatches.get((Integer) trace_back.get(t).second);
            crane.craneDynamic.mCurrentPosition = hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx).getHorizontalPosition();

            Move move = null;
            cwpBlock.setmWorkCostTime(0);
            for (; hatch.hatchDynamic.mCurrentMoveIdx < hatch.getMoveCount(); hatch.hatchDynamic.mCurrentMoveIdx++) {
                int cur_cost = cost(crane, hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx).getMoveType());
                if (cwpBlock.getmWorkCostTime() + cur_cost > min_time) {
                    break;
                }

                cwpBlock.setmWorkCostTime(cwpBlock.getmWorkCostTime() + cur_cost);
                cwpBlock.setmMoveCount(cwpBlock.getmMoveCount() + 1);
                cwpBlock.getmMoves().add(hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx));
                move = hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx);
            }
            cwpBlock.setmWorkEndTime(cwpBlock.getmWorkStartTime() + cwpBlock.getmWorkCostTime());

            cwpBlock.setmVesselId(hatch.getVesselId());
            cwpBlock.setmCraneId(crane.getCraneId());
            cwpBlock.setmHatchId(hatch.getHatchId());
            String moveType = move.getMoveType();
            String bayId = "";
            int j = (int) trace_back.get(t).second;
            if ("2".equals(moveType) || "3".equals(moveType)) {
                bayId = String.valueOf((j + 1) * 4 - 2);
            } else {
                if (move.getHorizontalPosition().doubleValue() - hatch.getHorizontalStartPosition().doubleValue() == Double.valueOf(14)/4) {
                    bayId = String.valueOf((j + 1) * 4 - 3);
                }
                else if (move.getHorizontalPosition().doubleValue() - hatch.getHorizontalStartPosition().doubleValue() == Double.valueOf(14)*3/4){
                    bayId = String.valueOf((j + 1) * 4 - 1);
                }
                else {
                    bayId = String.valueOf((j + 1) * 4 - 2);
                }
            }
            cwpBlock.setmHatchBayId(bayId);
            cwpBlock.setmStartMoveId(move.getMoveOrder() - cwpBlock.getmMoveCount() + 1);
            cwpBlock.setmRealWorkStartTime(start_time);//
            cwpBlock.setmMoveType(moveType);
            cwpBlock.setmLD(move.getLD());
            cwpBlock.setmCranePosition(move.getHorizontalPosition());

            hatch.hatchDynamic.mMoveCount = hatch.getMoveCount() - hatch.hatchDynamic.mCurrentMoveIdx;
            hatch.hatchDynamic.mMoveCountDY = hatch.hatchDynamic.mMoveCount;
            if (hatch.hatchDynamic.mMoveCount != 0) {
                hatch.hatchDynamic.mCurrentWorkPosition = hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx).getHorizontalPosition();
            }
        }
        return min_time;
    }

    public String writeResult() {
        return WriteResult.write1(cwpData.cwpBestSolution.getCwpResult());
    }

    private Integer cost(Crane crane, String moveType) {
        return 3600/32;
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
