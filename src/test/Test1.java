package test;

import process.CWP1;
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
public class Test1 {

    public static void main(String[] args) {

        String filePath = "data8.15/";
        String crane = FileUtil.readFileToString(new File(filePath + "crane.txt")).toString();
        String hatch = FileUtil.readFileToString(new File(filePath + "hatch.txt")).toString();
        String move = FileUtil.readFileToString(new File(filePath + "moves.txt")).toString();

        CWP1 cwp = new CWP1();
        cwp.initData(crane, hatch, move);
        cwp.craneHatchBlock();
        cwp.cwpWork();
        String str = cwp.writeResult();

        List<CwpResultInfo> cwpResultInfoList = CwpResultInfoProcess.getCwpResultInfo(str);
        List<CwpResultInfo> cwpResultInfoTransformList =  CwpResultInfoTransform.getTransformResult(cwpResultInfoList);
        CwpResultFrame cwpResultFrame = new CwpResultFrame(cwpResultInfoTransformList, cwp.getCwpData().cranes, null);
        cwpResultFrame.setVisible(true);
    }
}
