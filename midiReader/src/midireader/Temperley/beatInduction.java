
package midireader.Temperley;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import java.util.ArrayList;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static midireader.Temperley.Polyph.numnotes;

/* implementation of algorithm described in Beat Tracking with Musical Knowledge by Simon Dixon and Emilios Cambouropoulos */

public class beatInduction {
    
    static double induceBeat(note_struct[] notes) {
        
        ArrayList<cluster> clusters = new ArrayList();
        double delta = 30; 
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
        for (int k=0; k<0; k++) {
            for (int i=0; i<clusters.size(); i++) {
                for (int j=i+1; j<clusters.size(); j++) {
                    if (abs(clusters.get(i).average-clusters.get(j).average) < delta) {
                        clusters.get(i).average = (clusters.get(i).average*clusters.get(i).number + clusters.get(j).average*clusters.get(j).number)/(clusters.get(i).number+clusters.get(j).number);
                        clusters.remove(j);
                    }
                }
            }
        }
        
        double out=0,maxnum=0;
        
        for (int i=0; i<clusters.size(); i++) {
            //System.out.println(i + " " + clusters.get(i).average + " " + clusters.get(i).number);
            if (clusters.get(i).number > maxnum) {
                maxnum = clusters.get(i).number;
                out = clusters.get(i).average;
            }
        }
        //out = Math.round(out);
        //out  = clusters.get(0).average;
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
        }
        //if (out > 250) out /= 2;
        System.out.println("Induced beat: " + out);
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
}

class cluster {
    double average; //average length of intervals in this cluster
    int number; //number of intervals in this cluster
    
}