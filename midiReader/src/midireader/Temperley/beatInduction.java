
package midireader.Temperley;
import java.util.ArrayList;
import static java.lang.Math.abs;
import static midireader.Temperley.Polyph.numnotes;

/* implementation of algorithm described in Beat Tracking with Musical Knowledge by Simon Dixon and Emilios Cambouropoulos */

public class beatInduction {
    
    static double induceBeat(note_struct[] notes, int segtotal, int finalend, note_struct[] firstNote) {
        
        ArrayList<cluster> clusters = new ArrayList();
        double delta = 4; 
        double maxDist = 351+delta;
        
        //for each pair of onset times ti < tj
        for (int i=0; i<numnotes; i++) {
            for (int j=0; j<numnotes; j++) {
                double interval = notes[j].ontime-notes[i].ontime;
                if (interval > delta && interval < maxDist) {
                //System.out.println(interval);
                    double minimum = maxDist;
                    cluster minClust = null;
                    for (int k=0; k<clusters.size(); k++) {
                        if (abs(clusters.get(k).average - interval) < minimum) {
                            minimum = abs(clusters.get(k).average - interval);
                            minClust = clusters.get(k);
                        }
                    }
                    if (minimum < delta) {
                        minClust.average = (minClust.number*minClust.average + interval)/(minClust.number+1);
                        minClust.number++;
                    }
                    else {
                        cluster newClust = new cluster();
                        newClust.average = interval;
                        newClust.number = 1;
                        clusters.add(newClust);
                    }
                }
            }
        }
        //merge duplicate clusters
        /*
        for (int k=0; k<0; k++) {
            for (int i=0; i<clusters.size(); i++) {
                for (int j=i+1; j<clusters.size(); j++) {
                    if (abs(clusters.get(i).average-clusters.get(j).average) < delta) {
                        clusters.get(i).average = (clusters.get(i).average*clusters.get(i).number + clusters.get(j).average*clusters.get(j).number)/(clusters.get(i).number+clusters.get(j).number);
                        clusters.remove(j);
                    }
                }
            }
        }*/
        
        double out=0,maxDeviation=0, dev;
        ArrayList<cluster> deviations = new ArrayList();
        double thisavg;
        for (int i=0; i<clusters.size(); i++) { //find cluster with highest standard deviation
            thisavg = clusters.get(i).average;
            for (float j=-16; j<=16; j+=0.1) {
                dev = getDeviation(thisavg+j,segtotal,finalend,firstNote);
                //System.out.println(i + " " + clusters.get(i).average + " " + dev);
                if (dev > maxDeviation) {
                    maxDeviation = dev;
                    out = thisavg+j;
                }
            }
        }
        
        /*
        while (true) {
            if (similar(clusters,out/2) > 0) {
                out = similar(clusters,out/2);
            }
            else if (similar(clusters,out/3) > 0) {
                out = similar(clusters,out/3);
            }
            else break;
        }
        
        if ((out % 25) <= 1) {
            out -= (out % 25);
        }
        else if ((out % 25) >= 24) {
            out += 25-(out % 25);
        }
        else if (Math.abs(out-166.666667) < 2) {
            out = 166.65;
        }*/
        //if (out > 250) out /= 2;
        System.out.println("Induced beat: " + out + " with SD of " + maxDeviation);
        return out;
    }

    private static double similar(ArrayList<cluster> c, double d) {
        for (int i=0; i<c.size(); i++) {
            if (Math.abs(c.get(i).average - d) < 1) {
                return c.get(i).average;
            }
        }
        return 0;
    }
    
static double getDeviation(double average, int segtotal, int finalend, note_struct[] firstNote) {

        float num[] = new float[16];
        int total = 0;
        
        for (int seg=0; seg<=segtotal; seg++) {
            if (firstNote[seg] != null) {
                //System.out.println(firstNote[seg].ontime);
                num[(int)((firstNote[seg].ontime+10)/average % 16)] ++;
                total++;
            }
        }
        
        if (total == 0) {
            return 0;
        }
        float avg = 0;
        for (int j=0; j<16; j++) {
            avg += num[j]/total;
            num[j] = num[j]/total;
            //System.out.print(num[j] + " ");
        }
        //System.out.println();
        avg = avg/16;
        double variance = 0;
        
        for (int j=0; j<16; j++) {
            variance += Math.pow(num[j]-avg,2)/16;
        }
        return Math.sqrt(variance);
        //return 0;
    }
}

class cluster {
    double average; //average length of intervals in this cluster
    int number; //number of intervals in this cluster
}

