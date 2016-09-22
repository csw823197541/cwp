package entity;

import java.util.*;

/**
 * Created by csw on 2016/8/24 22:35.
 * Explain:
 */
public class CwpData {

    public Integer cwpBranchWidth;
    public Integer cwpBranchLimit;

    public List<Crane> cranes;
    public List<Hatch> hatches;
    public List<Move> moves;
    public CwpSolution cwpCurSolution;
    public CwpSolution cwpBestSolution;

    public Map<String, Integer> hatchIdxMap;

    public Map<Integer, Set<Double>> hatchIdxPosMap;

    public CwpData() {
        cranes = new ArrayList<>();
        hatches = new ArrayList<>();
        moves = new ArrayList<>();
        cwpCurSolution = new CwpSolution();
        cwpBestSolution = new CwpSolution();
        hatchIdxMap = new HashMap<>();
        hatchIdxPosMap = new HashMap<>();
    }
}
