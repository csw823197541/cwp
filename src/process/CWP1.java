package process;

import entity.*;

import java.util.*;

/**
 * Created by csw on 2016/8/26 8:39.
 * Explain:
 */
public class CWP1 {

    private CwpData cwpData;
    private CwpCSH cwpCSH;
    private Map<Integer, List<Hatch>> craneHatchBlockMap;

    private Map<Integer, List<CwpBlock>> cwpResult;

    public CWP1() {
        cwpData = new CwpData();
        cwpCSH = new CwpCSH();
        craneHatchBlockMap = new HashMap<>();
        cwpResult = new HashMap<>();
    }

    public void initData(String craneJsonStr, String hatchJsonStr, String moveJsonStr) {

        //
        List<Crane> inputCranes = sortCraneByPosition(InitData.initCrane(craneJsonStr));
        for (int k = 0; k < 4; k++) {
            cwpData.cranes.add(inputCranes.get(k));
        }

        //
        cwpData.hatches = sortHatchById(InitData.initHatch(hatchJsonStr));
        cwpData.moves = InitData.initMove(moveJsonStr);

        //
        int j = 0;
        for (Hatch hatch : cwpData.hatches) {
            cwpData.hatchIdxMap.put(hatch.getHatchId(), j++);
        }
        for (Move move : cwpData.moves) {
            cwpData.hatches.get(cwpData.hatchIdxMap.get(move.getHatchId())).mMoves.add(move);
        }

        //
        j = 0;
        for (Hatch hatch : cwpData.hatches) {
            List<Move> moveList = hatch.getmMoves();
            this.sortMoveByMoveOrder(moveList);
            Set<Double> posSet = new HashSet();
            for (Move move : moveList) {
                posSet.add(move.getHorizontalPosition());
            }
            cwpData.hatchIdxPosMap.put(j++, posSet);
        }

        //
        for (int c = 0; c < cwpData.cranes.size(); c++) {
            craneHatchBlockMap.put(c, new ArrayList<Hatch>());
            cwpResult.put(c, new ArrayList<CwpBlock>());
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

    public void craneHatchBlock() {
        int nc = cwpData.cranes.size();
        int nh = cwpData.hatches.size();

        //
        int mean = 0;
        int allMoveCount = 0;
        for (Hatch hatch : cwpData.hatches) {
            allMoveCount += hatch.hatchDynamic.mMoveCount;
        }
        mean = allMoveCount / nc;

        //
        int c = 0;
        int tmpMoveCount = 0;
        for (int j = 0; j < nh; j++) {
            Hatch hatch = cwpData.hatches.get(j);
            tmpMoveCount += hatch.hatchDynamic.mMoveCount;
            int meanL = (int) (mean - mean * 0.1);
            int meanR = (int) (mean + mean * 0.1);
            if (tmpMoveCount >= meanL && tmpMoveCount <= meanR) {
                craneHatchBlockMap.get(c).add(hatch);
                Crane crane = cwpData.cranes.get(c);
//                crane.craneDynamic.mMoveRangeTo = hatch.getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeTo = j;
                int cc = craneHatchBlockMap.get(c).size();
//                crane.craneDynamic.mMoveRangeFrom = cwpData.hatches.get(j + 1 - cc).getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeFrom = j + 1 - cc;
                c++;
                tmpMoveCount = 0;
            }
            else if (tmpMoveCount > meanR) {
                hatch.hatchDynamic.mMoveCountL = hatch.hatchDynamic.mMoveCount - (tmpMoveCount - mean);
                hatch.hatchDynamic.mMoveCountR = tmpMoveCount - mean;
                craneHatchBlockMap.get(c).add(hatch);
                Crane crane = cwpData.cranes.get(c);
//                crane.craneDynamic.mMoveRangeTo = hatch.getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeTo = j;
                int cc = craneHatchBlockMap.get(c).size();
//                crane.craneDynamic.mMoveRangeFrom = cwpData.hatches.get(j + 1 - cc).getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeFrom = j + 1 - cc;
                c++;
                tmpMoveCount = hatch.hatchDynamic.mMoveCountR;
            }
            else {
                craneHatchBlockMap.get(c).add(hatch);
                Crane crane = cwpData.cranes.get(c);
//                crane.craneDynamic.mMoveRangeTo = hatch.getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeTo = j;
                int cc = craneHatchBlockMap.get(c).size();
//                crane.craneDynamic.mMoveRangeFrom = cwpData.hatches.get(j + 1 - cc).getHorizontalStartPosition();
                crane.craneDynamic.mMoveRangeFrom = j + 1 - cc;
            }
        }
    }

    public CwpCSH cwpCraneSelectHatch() {
        //
        CwpCSH cwpCSH1 = new CwpCSH();
        int nc = cwpData.cranes.size();
        for (int i = 0; i < nc; i++) {
            Crane crane = cwpData.cranes.get(i);
            int maxMove = 0;
            int j = -1;
            boolean isFind = false;
            boolean isSafeSpanNotEnough = false;
            int jj = -1;
            double lastPos = crane.craneDynamic.mCurrentPosition;
            Double selectPos = -1.0;
            for (int k = crane.craneDynamic.mMoveRangeFrom; k <= crane.craneDynamic.mMoveRangeTo; k++) {
                Hatch hatch = cwpData.hatches.get(k);
                if (hatch.hatchDynamic.mMoveCount > maxMove) {
                    selectPos = hatch.getmMoves().get(hatch.hatchDynamic.mCurrentMoveIdx).getHorizontalPosition();
                    if (this.isCraneSafe(i, selectPos)) {//
                        isFind = true;
                        maxMove = hatch.hatchDynamic.mMoveCount;
                        j = k;
                    } else {
                        isSafeSpanNotEnough = true;
                        jj = k;
                    }
                }
            }
            if (isFind) {
                int lastHatchIdx = this.findHatchIdxByPosition(lastPos);
                if (lastHatchIdx != -1 && lastHatchIdx <= crane.craneDynamic.mMoveRangeTo) {
                    Hatch lastHatch = cwpData.hatches.get(lastHatchIdx);
                    if (lastHatch.hatchDynamic.mMoveCount > 0) {
                        if (this.isCraneSafe(i, lastPos)) {
                            cwpCSH.dpTraceBack.add(new Pair(i, lastHatchIdx));
                            cwpCSH1.dpTraceBack.add(new Pair(i, lastHatchIdx));
                            crane.craneDynamic.mCurrentPosition = lastPos;
                        }
//                        else {
//                            cwpCSH.dpTraceBack.get(i - 1).setSecond(-1);
//                            cwpCSH1.dpTraceBack.get(i - 1).setSecond(-1);
//                            //
//                            cwpCSH.dpTraceBack.add(new Pair(i, lastHatchIdx));
//                            cwpCSH1.dpTraceBack.add(new Pair(i, lastHatchIdx));
//                            crane.craneDynamic.mCurrentPosition = lastPos;
//                        }
                    } else {
                        cwpCSH.dpTraceBack.add(new Pair(i, j));
                        cwpCSH1.dpTraceBack.add(new Pair(i, j));
                        crane.craneDynamic.mCurrentPosition = selectPos;
                    }
                } else {
                    cwpCSH.dpTraceBack.add(new Pair(i, j));
                    cwpCSH1.dpTraceBack.add(new Pair(i, j));
                    crane.craneDynamic.mCurrentPosition = selectPos;
                }
            } else {
                cwpCSH.dpTraceBack.add(new Pair(i, -1));
                cwpCSH1.dpTraceBack.add(new Pair(i, -1));
//                if (isSafeSpanNotEnough) {
//                    cwpCSH.dpTraceBack.get(i - 1).setSecond(-1);
//                    cwpCSH1.dpTraceBack.get(i - 1).setSecond(-1);
//                    //
//                    cwpCSH.dpTraceBack.add(new Pair(i, jj));
//                    cwpCSH1.dpTraceBack.add(new Pair(i, jj));
//                    crane.craneDynamic.mCurrentPosition = selectPos;
//                } else {
//                    cwpCSH.dpTraceBack.add(new Pair(i, -1));
//                    cwpCSH1.dpTraceBack.add(new Pair(i, -1));
//                }
            }
        }
        return cwpCSH1;
    }

    private boolean isCraneSafe(int i, double position) {
        boolean isSafe = true;
        if (i > 0) {
            Crane crane = cwpData.cranes.get(i);
            Crane craneL = cwpData.cranes.get(i - 1);
            double craneLPos = craneL.craneDynamic.mCurrentPosition;
            int craneLInHatchIdx = this.findHatchIdxByPosition(craneLPos);
            if (craneLInHatchIdx != -1) {
                Hatch craneLInHatch = cwpData.hatches.get(craneLInHatchIdx);
                if (craneLInHatch.hatchDynamic.mMoveCount > 0) {
                    craneLPos = craneLInHatch.getmMoves().get(craneLInHatch.hatchDynamic.mCurrentMoveIdx).getHorizontalPosition().doubleValue();
                    if (position - craneLPos < 2*crane.getSafeSpan().doubleValue()) {
                        isSafe = false;
                    }
                }
            }
        }
        return isSafe;
    }

    private int findHatchIdxByPosition(Double currentPos) {
        int hatchIdx = -1;
        for (int j = 0; j < cwpData.hatches.size(); j++) {
            if (cwpData.hatchIdxPosMap.get(j).contains(currentPos)) {
                hatchIdx = j;
            }
        }
        return hatchIdx;
    }

    public void cwpWork() {
        int n = 0;
//        while(n < 100) {
        while (isHatchHasMoves()) {
            cwpRealWork(cwpCraneSelectHatch());
            n++;
        }
    }

    private boolean isHatchHasMoves() {
        boolean isHasMove = false;
        for (Hatch hatch : cwpData.hatches) {
            if (hatch.hatchDynamic.mMoveCount > 0) {
                isHasMove = true;
            }
        }
        return isHasMove;
    }

    private void cwpRealWork(CwpCSH cwpCSH) {

        //
        int min_time = Integer.MAX_VALUE;

        int cn = cwpData.cranes.size();
        CwpBlock[] cwpBlocks = new CwpBlock[cn];
        for (int i = 0; i < cn; i++) {
            cwpBlocks[i] = new CwpBlock();
        }
        for (int k = 0; k < cwpCSH.dpTraceBack.size(); k++) {
            Pair pair = cwpCSH.dpTraceBack.get(k);
            int i = (int) pair.first;
            int j = (int) pair.second;
            if (j != -1) {
                Crane crane = cwpData.cranes.get(i);
                Hatch hatch = cwpData.hatches.get(j);
                int curMoveIdx = hatch.hatchDynamic.mCurrentMoveIdx;
                List<Move> moveList = this.sortMoveByMoveOrder(hatch.getmMoves());
                double last_Position = moveList.get(curMoveIdx).getHorizontalPosition();
                for (int m = curMoveIdx; m < moveList.size(); m++) {
                    Move move = moveList.get(m);
                    if (last_Position != move.getHorizontalPosition().doubleValue() || m >= hatch.hatchDynamic.mMoveCountL) {
                        break;
                    }
                    cwpBlocks[k].setmWorkCostTime(cwpBlocks[k].getmWorkCostTime() + this.costTime(crane, move.getMoveType()));
                }
                min_time = Math.min(min_time, cwpBlocks[k].getmWorkCostTime());
            }
        }

        //
        for (int k = 0; k < cwpCSH.dpTraceBack.size(); k++) {
            Pair pair = cwpCSH.dpTraceBack.get(k);
            int i = (int) pair.first;
            int j = (int) pair.second;
            if (j != -1) {
                Crane crane = cwpData.cranes.get(i);
                Hatch hatch = cwpData.hatches.get(j);
                int curMoveIdx = hatch.hatchDynamic.mCurrentMoveIdx;
                List<Move> moveList = this.sortMoveByMoveOrder(hatch.getmMoves());
                cwpBlocks[k].setmWorkCostTime(0);
                for (int m = curMoveIdx; m < moveList.size(); m++) {
                    Move move = moveList.get(m);
                    cwpBlocks[k].setmWorkCostTime(cwpBlocks[k].getmWorkCostTime() + this.costTime(crane, move.getMoveType()));
                    if (cwpBlocks[k].getmWorkCostTime() <= min_time) {
                        CwpBlock cwpBlock = new CwpBlock();
                        cwpBlock.setmVesselId(hatch.getVesselId());
                        cwpBlock.setmCraneId(crane.getCraneId());
                        cwpBlock.setmHatchId(hatch.getHatchId());

                        String moveType = move.getMoveType();
                        String bayId = "";
                        if ("2".equals(moveType) || "3".equals(moveType)) {
                            bayId = String.valueOf((j + 1) * 4 - 2);
                        } else {
                            if (move.getHorizontalPosition() - hatch.getHorizontalStartPosition() == Double.valueOf(14)/4) {
                                bayId = String.valueOf((j + 1) * 4 - 3);
                            }
                            else if (move.getHorizontalPosition() - hatch.getHorizontalStartPosition() == Double.valueOf(14)*3/4){
                                bayId = String.valueOf((j + 1) * 4 - 1);
                            }
                            else {
                                bayId = String.valueOf((j + 1) * 4 - 2);
                            }
                        }
                        cwpBlock.setmHatchBayId(bayId);//

                        int workTime = costTime(crane, moveType);
                        int st = crane.craneDynamic.mCurrentWorkST;
                        int et = st + workTime;
                        cwpBlock.setmWorkStartTime(st);//
                        cwpBlock.setmWorkEndTime(et);//

                        cwpBlock.setmMoveCount(1);
                        cwpBlock.setmStartMoveId(move.getMoveOrder());
                        cwpBlock.setmRealWorkStartTime(st);//
                        cwpBlock.setmMoveType(move.getMoveType());
                        cwpBlock.setmLD(move.getLD());
                        cwpBlock.setmCranePosition(move.getHorizontalPosition());
                        cwpResult.get(i).add(cwpBlock);

                        hatch.hatchDynamic.mMoveCount -= 1;
                        hatch.hatchDynamic.mCurrentMoveIdx = m + 1;
                        crane.craneDynamic.mCurrentPosition = move.getHorizontalPosition();
                        crane.craneDynamic.mCurrentWorkST = et;
                    } else {
                        if (m >= hatch.hatchDynamic.mMoveCountL) {
                            hatch.hatchDynamic.mMoveCountL = hatch.getMoveCount();
                            crane.craneDynamic.mMoveRangeTo = j - 1;
                            cwpData.cranes.get(i + 1).craneDynamic.mMoveRangeFrom = j;
                            break;
                        }
                        break;
                    }
                }
            }
        }
        //
        int MaxST = 0;
        for (Crane crane1 : cwpData.cranes) {
            if (crane1.craneDynamic.mCurrentWorkST > MaxST) {
                MaxST = crane1.craneDynamic.mCurrentWorkST;
            }
        }
        for (Crane crane1 : cwpData.cranes) {
            crane1.craneDynamic.mCurrentWorkST = MaxST;
        }
    }

    private int costTime(Crane crane, String moveType) {

        return 126;
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

    public String writeResult() {
        return WriteResult.write(cwpResult);
    }

    public CwpData getCwpData() {
        return cwpData;
    }

    public void setCwpData(CwpData cwpData) {
        this.cwpData = cwpData;
    }
}
