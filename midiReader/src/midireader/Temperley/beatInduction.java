
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
        double delta = 25; //25 tick range?
        double maxDist = 1001;
        
        //for each pair of onset times ti < tj
        for (int i=0; i<numnotes; i++) {
            for (int j=0; j<numnotes; j++) {
                double interval = notes[j].ontime-notes[i].ontime;
                if (interval > delta && interval < maxDist) {
                System.out.println(interval);
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
        
        for (int i=0; i<clusters.size(); i++) {
            System.out.println(i + " " + clusters.get(i).average + " " + clusters.get(i).number);
        }
        double out = 55;
        return out;
    }
}

class cluster {
    double average; //average length of intervals in this cluster
    int number; //number of intervals in this cluster
    
}