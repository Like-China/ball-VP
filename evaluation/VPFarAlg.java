package evaluation;

import java.util.ArrayList;
import VPTree.*;
import Distance.*;

public class VPFarAlg {
    /// query, double[]base set at each timestamp, we update them at each timestampe
    public ArrayList<double[]> qData = new ArrayList<>();
    public ArrayList<double[]> dbData = new ArrayList<>();
    l2Distance dist = new l2Distance();
    // index construction time / filtering time
    public long cTime = 0;
    public double fTime = 0;
    // the number of node accesses
    public int searchCount = 0;

    public String info = null;
    public int sampleNB;

    public VPFarAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData, int sampleNB) {
        this.qData = qData;
        this.dbData = dbData;
        this.sampleNB = sampleNB;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<double[]> nnSearch() {
        long t1 = System.currentTimeMillis();
        VPTreeByFarest vp = new VPTreeByFarest(dbData, dist);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

        ArrayList<double[]> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : qData) {
            res.add(vp.searchOneNN(q));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        int n = qData.size();
        info = String.format(
                "**VPFarTree**\nnn construct time / mean search time / nn mean node accesses / calc times:\n%8dms \t%8.3fms \t%8d \t%8d",
                cTime, fTime / n, vp.searchCount / n, vp.searchCount / n);
        System.out.println(info);
        searchCount = vp.searchCount / n;

        return res;
    }

    public ArrayList<double[]> rangeSearch(double range) {
        long t1 = System.currentTimeMillis();
        VPTreeByFarest vp = new VPTreeByFarest(dbData, dist);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

        ArrayList<double[]> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : qData) {
            res.addAll(vp.searchRange(q, range));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        System.out.println("VP range-Search result size: " + res.size());
        searchCount = vp.searchCount / qData.size();
        return res;
    }

}