package test;

import process.CWP;
import process.CWP1;
import utils.FileUtil;

import java.io.File;

/**
 * Created by csw on 2016/8/24 15:49.
 * Explain:
 */
public class Test {

    public static void main(String[] args) {

        String filePath = "toCwpData8.26/";
        String crane = FileUtil.readFileToString(new File(filePath + "crane.txt")).toString();
        String hatch = FileUtil.readFileToString(new File(filePath + "hatch.txt")).toString();
        String move = FileUtil.readFileToString(new File(filePath + "moves.txt")).toString();

        CWP cwp = new CWP();
        cwp.initData(crane, hatch, move);
        cwp.cwpKernel();

    }
}
