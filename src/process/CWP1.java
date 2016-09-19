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
        int i = 0;
        for (Hatch hatch : cwpData.hatches) {
            cwpData.hatchIdxMap.put(hatch.getHatchId(), i++);
        }
        for (Move move : cwpData.moves) {
            cwpData.hatches.get(cwpData.hatchIdxMap.get(move.getHatchId())).mMoves.add(move);
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
        for (int i = 0; i < nh; i++) {
            Hatch hatch = cwpData.hatches.get(i);
            tmpMoveCount += hatch.hatchDynamic.mMoveCount;
            int meanL = (int) (mean - mean * 0.1);
            int meanR = (int) (mean + mean * 0.1);
            if (tmpMoveCount >= meanL && tmpMoveCount <= meanR) {
                craneHatchBlockMap.get(c).add(hatch);
                c++;
                tmpMoveCount = 0;
            }
            else if (tmpMoveCount > meanR) {
                Hatch hatchL = hatch.deepCopy();
                Hatch hatchR = hatch.deepCopy();
                hatchL.hatchDynamic.mMoveCount = hatch.hatchDynamic.mMoveCount - (tmpMoveCount - mean);
                hatchR.hatchDynamic.mMoveCount = tmpMoveCount - mean;
            }
            else {
                craneHatchBlockMap.get(c).add(hatch);
            }
        }
    }

    public CwpCSH cwpCraneSelectHatch() {
        //
        CwpCSH cwpCSH1 = new CwpCSH();
        int nc = cwpData.cranes.size();
        for (int i = 0; i < nc; i++) {
            List<Hatch> hatchList = craneHatchBlockMap.get(i);
            int maxMove = 0;
            int j = 0;
            boolean isFind = false;
            for (Hatch hatch : hatchList) {
                if (hatch.hatchDynamic.mMoveCount > maxMove) {
                    maxMove = hatch.hatchDynamic.mMoveCount;
                    j = cwpData.hatchIdxMap.get(hatch.getHatchId());
                    isFind = true;
                }
            }
            if (isFind) {
                cwpCSH.dpTraceBack.add(new Pair(i, j));
                cwpCSH1.dpTraceBack.add(new Pair(i, j));
            }
        }
        return cwpCSH1;
    }

    public void cwpWork() {
        while (isHatchHasMoves()) {
            cwpRealWork(cwpCraneSelectHatch());
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
        for (Pair pair : cwpCSH.dpTraceBack) {
            int i = (int) pair.first;
            int j = (int) pair.second;
            Crane crane = cwpData.cranes.get(i);
            Hatch hatch = cwpData.hatches.get(j);
            int curMoveIdx = hatch.hatchDynamic.mCurrentMoveIdx;
            List<Move> moveList = sortMoveByMoveOrder(hatch.getmMoves());
            for (int m = curMoveIdx; m < moveList.size(); m++) {
                Move move = moveList.get(m);
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
                hatch.hatchDynamic.mCurrentMoveIdx = m;
                crane.craneDynamic.mCurrentPosition = move.getHorizontalPosition();
                crane.craneDynamic.mCurrentWorkST = et;
            }
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
