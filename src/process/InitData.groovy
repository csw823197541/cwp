package process;

import entity.Crane
import entity.Hatch
import entity.Move
import entity.WorkTimeRange
import groovy.json.JsonSlurper

import java.text.SimpleDateFormat

/**
 * Created by csw on 2016/8/24 21:33.
 * Explain:
 */
public class InitData {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    public static List<Crane> initCrane(String jsonStr) {
        List<Crane> craneList = new ArrayList<>();
        boolean isError = false;
        try{
            def root = new JsonSlurper().parseText(jsonStr);
            assert root instanceof List;

            root.each { it ->
                Crane crane = new Crane();
                assert it instanceof Map
                crane.currentPosition = it.CURRENTPOSITION;
                crane.craneDynamic.mCurrentPosition = it.CURRENTPOSITION;
                crane.disEff20 = it.DISCHARGEEFFICIENCY20;
                crane.disEff40 = it.DISCHARGEEFFICIENCY40;
                crane.disEffTwin = it.DISCHARGEEFFICIENCYTWIN;
                crane.disEffTdm = it.DISCHARGEEFFICIENCYTDM;
                crane.craneId = it.ID;
                crane.loadEff20 = it.LOADINGEFFICIENCY20;
                crane.loadEff40 = it.LOADINGEFFICIENCY40;
                crane.loadEffTwin = it.LOADINGEFFICIENCYTWIN;
                crane.loadEffTdm = it.LOADINGEFFICIENCYTDM;
                crane.moveRangeFrom = it.MOVINGRANGEFROM;
                crane.moveRangeTo = it.MOVINGRANGETO;
                crane.craneName = it.NAME;
                crane.safeSpan = it.SAFESPAN;
                crane.craneSeq = it.SEQ;
                crane.speed = it.SPEED;
                crane.width = it.WIDTH;

                def workTimes = it.WORKINGTIMERANGES;
                assert workTimes instanceof List
                List<WorkTimeRange> workTimeRangeList = new ArrayList<WorkTimeRange>()
                workTimes.each {time->
                    WorkTimeRange workTimeRange = new WorkTimeRange()
                    assert time instanceof Map
                    workTimeRange.id = time.ID
                    if(time.WORKSTARTTIME)
                        workTimeRange.workStartTime = sdf.parse(new String(time.WORKSTARTTIME))
                    if(time.WORKENDTIME)
                        workTimeRange.workEndTime = sdf.parse(new String(time.WORKENDTIME))
                    workTimeRangeList.add(workTimeRange)
                }
                crane.workTimeRanges = workTimeRangeList

                craneList.add(crane);
            }
        }catch (Exception e){
            System.out.println("parsing crane, json data exception!")
            isError = true;
            e.printStackTrace()
        }
        if(isError) {
            System.out.println("crane init failure!")
            return null;
        }else {
            System.out.println("crane init success! the number of cranes is: " + craneList.size())
            return craneList
        }
    }

    public static List<Hatch> initHatch(String jsonStr) {
        List<Hatch> hatchList = new ArrayList<>();
        boolean isError = false;
        try{
            def root = new JsonSlurper().parseText(jsonStr);
            assert root instanceof List;

            root.each { it ->
                Hatch hatch = new Hatch();
                assert it instanceof Map
                hatch.horizontalStartPosition = it.HORIZONTALSTARTPOSITION;
                hatch.hatchDynamic.mCurrentWorkPosition = it.HORIZONTALSTARTPOSITION;
                hatch.hatchId = it.ID;
                hatch.vesselId = it.VESSELID;
                hatch.length = it.LENGTH;
                hatch.moveCount = it.MOVECOUNT;
                hatch.hatchDynamic.mMoveCount = it.MOVECOUNT;
                hatch.hatchDynamic.mCurrentMoveIdx = 0;
                hatch.hatchNo = it.NO;
                hatch.hatchSeq = it.SEQ;

                def workTimes = it.WORKINGTIMERANGES;
                assert workTimes instanceof List
                List<WorkTimeRange> workTimeRangeList = new ArrayList<WorkTimeRange>()
                workTimes.each {time->
                    WorkTimeRange workTimeRange = new WorkTimeRange()
                    assert time instanceof Map
                    workTimeRange.id = time.ID
                    if(time.WORKSTARTTIME)
                        workTimeRange.workStartTime = sdf.parse(new String(time.WORKSTARTTIME))
                    if(time.WORKENDTIME)
                        workTimeRange.workEndTime = sdf.parse(new String(time.WORKENDTIME))
                    workTimeRangeList.add(workTimeRange)
                }
                hatch.workTimeRanges = workTimeRangeList

                hatchList.add(hatch);
            }
        }catch (Exception e){
            System.out.println("parsing hatch, json data exception!")
            isError = true;
            e.printStackTrace()
        }
        if(isError) {
            System.out.println("hatch init failure!")
            return null;
        }else {
            System.out.println("hatch init success! the number of hatches is: " + hatchList.size())
            return hatchList
        }
    }

    public static List<Move> initMove(String jsonStr) {
        List<Move> moveList = new ArrayList<>();
        boolean isError = false;
        try{
            def root = new JsonSlurper().parseText(jsonStr);
            assert root instanceof List;

            root.each { it ->
                Move move = new Move();
                assert it instanceof Map
                move.moveOrder = it.CWPWORKMOVENUM;
                move.deck = it.DECK;
                move.globalPriority = it.GLOBALPRIORITY;
                move.hatchId = it.HATCHID;
                move.horizontalPosition = it.HORIZONTALPOSITION;
                move.LD = it.LD;
                move.moveType = it.MOVETYPE;
                moveList.add(move);
            }
        }catch (Exception e){
            System.out.println("parsing move, json data exception!")
            isError = true;
            e.printStackTrace()
        }
        if(isError) {
            System.out.println("move init failure!")
            return null;
        }else {
            System.out.println("move init success! the number of moves is: " + moveList.size())
            return moveList
        }
    }
}
