package test;

import process.CWP;
import test.view.CwpResultFrame;
import test.view.CwpResultInfo;
import test.view.CwpResultInfoProcess;
import test.view.CwpResultInfoTransform;
import utils.FileUtil;

import java.io.File;
import java.util.List;

/**
 * Created by csw on 2016/8/24 15:49.
 * Explain:
 */
public class Test9_07_2 {

    public static void main(String[] args) {

        String filePath = "toCwpData8.15/";
        String crane = FileUtil.readFileToString(new File(filePath + "crane.txt")).toString();
        String hatch = FileUtil.readFileToString(new File(filePath + "hatch.txt")).toString();
        String move = FileUtil.readFileToString(new File(filePath + "moves.txt")).toString();

        CWP cwp = new CWP();
        long dyStartTime = System.currentTimeMillis();
        cwp.initData(crane, hatch, move);
        cwp.divideCraneMoveRange();
        cwp.cwpSearch(0);
        long dyEndTime = System.currentTimeMillis();
        String str = cwp.writeResult();

        List<CwpResultInfo> cwpResultInfoList = CwpResultInfoProcess.getCwpResultInfo(str);
        List<CwpResultInfo> cwpResultInfoTransformList =  CwpResultInfoTransform.getTransformResult(cwpResultInfoList);
        CwpResultFrame cwpResultFrame = new CwpResultFrame(cwpResultInfoTransformList, cwp.getCwpData().cranes, null);
        cwpResultFrame.setVisible(true);

        System.out.println("The time of the cwp algorithm is: " + (dyEndTime - dyStartTime) + " ms");
    }
}
