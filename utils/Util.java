package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import VPTree.VPTreeBySampleTester;
import evaluation.Settings;

public class Util {

    public static void writeFile(String setInfo, VPTreeBySampleTester t) {
        try {
            File writeName = new File(Settings.data + "_out.txt");
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName, true);
                    BufferedWriter out = new BufferedWriter(writer)) {
                if (setInfo.length() > 0) {
                    out.newLine();
                    out.newLine();
                    out.write(setInfo);
                }
                out.newLine();
                out.write("DF_NodeAccess=" + Arrays.toString(t.DF_NodeAccess));
                out.newLine();
                out.write("BF_NodeAccess=" + Arrays.toString(t.BF_NodeAccess));
                out.newLine();
                out.write("DF_FIFO_NodeAccess=" + Arrays.toString(t.DF_FIFO_NodeAccess));
                out.newLine();
                out.write("BF_FIFO_NodeAccess=" + Arrays.toString(t.BF_FIFO_NodeAccess));
                out.newLine();
                out.write("DF_LRU_NodeAccess=" + Arrays.toString(t.DF_LRU_NodeAccess));
                out.newLine();
                out.write("BF_LRU_NodeAccess=" + Arrays.toString(t.BF_LRU_NodeAccess));
                out.newLine();
                out.write("DF_LFU_NodeAccess=" + Arrays.toString(t.DF_LFU_NodeAccess));
                out.newLine();
                out.write("BF_LFU_NodeAccess=" + Arrays.toString(t.BF_LFU_NodeAccess));
                out.newLine();
                out.write("DF_Best_NodeAccess=" + Arrays.toString(t.DF_Best_NodeAccess));
                out.newLine();
                out.write("BF_Best_NodeAccess=" + Arrays.toString(t.BF_Best_NodeAccess));
                out.newLine();
                out.write("DF_Global_Object_NodeAccess="
                        + Arrays.toString(t.DF_Global_Object_NodeAccess));
                out.newLine();
                out.write("BF_Global_Object_NodeAccess="
                        + Arrays.toString(t.BF_Global_Object_NodeAccess));
                out.newLine();
                out.write("DF_Global_queryPoints_NodeAccess="
                        + Arrays.toString(t.DF_Global_queryPoints_NodeAccess));
                out.newLine();
                out.write("BF_Global_queryPoints_NodeAccess="
                        + Arrays.toString(t.BF_Global_queryPoints_NodeAccess));
                out.newLine();

                out.newLine();
                // the number of calculation time
                out.write("DF_CalcCount=" + Arrays.toString(t.DF_CalcCount));
                out.newLine();
                out.write("BF_CalcCount=" + Arrays.toString(t.BF_CalcCount));
                out.newLine();
                out.write("DF_FIFO_CalcCount=" + Arrays.toString(t.DF_FIFO_CalcCount));
                out.newLine();
                out.write("BF_FIFO_CalcCount=" + Arrays.toString(t.BF_FIFO_CalcCount));
                out.newLine();
                out.write("DF_LRU_CalcCount=" + Arrays.toString(t.DF_LRU_CalcCount));
                out.newLine();
                out.write("BF_LRU_CalcCount=" + Arrays.toString(t.BF_LRU_CalcCount));
                out.newLine();
                out.write("DF_LFU_CalcCount=" + Arrays.toString(t.DF_LFU_CalcCount));
                out.newLine();
                out.write("BF_LFU_CalcCount=" + Arrays.toString(t.BF_LFU_CalcCount));
                out.newLine();
                out.write("DF_Best_CalcCount=" + Arrays.toString(t.DF_Best_CalcCount));
                out.newLine();
                out.write("BF_Best_CalcCount=" + Arrays.toString(t.BF_Best_CalcCount));
                out.newLine();

                out.write("DF_Global_Object_CalcCount="
                        + Arrays.toString(t.DF_Global_Object_CalcCount));
                out.newLine();
                out.write("BF_Global_Object_CalcCount="
                        + Arrays.toString(t.BF_Global_Object_CalcCount));
                out.newLine();
                out.write("DF_Global_queryPoints_CalcCount="
                        + Arrays.toString(t.DF_Global_queryPoints_CalcCount));
                out.newLine();
                out.write("BF_Global_queryPoints_CalcCount="
                        + Arrays.toString(t.BF_Global_queryPoints_CalcCount));
                out.newLine();
                out.newLine();
                // the search time
                out.newLine();
                out.write("DF_Time=" + Arrays.toString(t.DF_Time));
                out.newLine();
                out.write("BF_Time=" + Arrays.toString(t.BF_Time));
                out.newLine();
                out.write("DF_FIFO_Time=" + Arrays.toString(t.DF_FIFO_Time));
                out.newLine();
                out.write("BF_FIFO_Time=" + Arrays.toString(t.BF_FIFO_Time));
                out.newLine();
                out.write("DF_LRU_Time=" + Arrays.toString(t.DF_LRU_Time));
                out.newLine();
                out.write("BF_LRU_Time=" + Arrays.toString(t.BF_LRU_Time));
                out.newLine();
                out.write("DF_LFU_Time=" + Arrays.toString(t.DF_LFU_Time));
                out.newLine();
                out.write("BF_LFU_Time=" + Arrays.toString(t.BF_LFU_Time));
                out.newLine();
                out.write("DF_Best_Time=" + Arrays.toString(t.DF_Best_Time));
                out.newLine();
                out.write("BF_Best_Time=" + Arrays.toString(t.BF_Best_Time));
                out.newLine();

                out.write("DF_Global_Object_Time=" + Arrays.toString(t.DF_Global_Object_Time));
                out.newLine();
                out.write("BF_Global_Object_Time=" + Arrays.toString(t.BF_Global_Object_Time));
                out.newLine();
                out.write("DF_Global_queryPoints_Time="
                        + Arrays.toString(t.DF_Global_queryPoints_Time));
                out.newLine();
                out.write("BF_Global_queryPoints_Time="
                        + Arrays.toString(t.BF_Global_queryPoints_Time));

                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
