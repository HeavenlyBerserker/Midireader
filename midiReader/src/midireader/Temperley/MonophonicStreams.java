
package midireader.Temperley;

import java.util.ArrayList;
import static midireader.Temperley.Polyph.*;
import midireader.DataStructs.BiHashMap;
import midireader.XmkMain;

import midireader.processingXmk.MeasureAnalyzer;

public class MonophonicStreams {
    
    static ArrayList<float[]> selectStreams( ) { //runs skyline algorithm on the Temperley note streams by their average pitch

    float[] min = new float[(int) 1000];
    float[] max = new float[(int) 1000];
    float[] average = new float[(int) 1000]; //these used to be 500
    for (int i=0; i<1000; i++) {
        average[i] = 0;     //average pitch
        min[i] = 0;         //starttime of this stream
        max[i] = 0;         //endtime of this stream
    }
    
    Snote  sn;
    Stream s=streamlist;
    int starttime = -1;
    int endtime = -1;
    
    float realstart = 10000;
    float realend = 0;
    
    while(true) { //get info about all the streams and store in arrays
	if(s==null) break;
	//System.out.printf("Interpretted Stream %d: ", s.index);
	sn = s.sn;
        float pitchcounter = 0;
        float notecounter = 0;
        //System.out.println(s.index + " " + s.start);
	while(true) {
	    if(sn==null) break;
	    if(sn.ontime < starttime) continue;
	    if(endtime != -1 && sn.ontime > endtime) break;
            pitchcounter += sn.pitch;
            notecounter++;
	    //System.out.printf("[%d %d %d] ", sn.ontime, sn.offtime, sn.pitch);
	    sn=sn.next;
	}
        average[s.index] = pitchcounter/notecounter;
        min[s.index] = s.start;
        max[s.index] = s.end;
        if (s.start < realstart) 
            realstart = s.start;
        if (s.end > realend) 
            realend = s.end;
	//System.out.printf("%f %f %f\n", min[s.index], average[s.index], max[s.index]);
	s = s.next;
    }
    
    float size = realend;
    
    
    //for (int i=0; i<500; i++) {
        //System.out.println(i + " " + average[i]+" " + min[i] + " " +max[i]);
    //}
    
    int numofstreams=0;
    for (int i=1; i<1000; i++) {
        if (average[i] == 0) {
            numofstreams = i-1;
            break;
        }
    }
    
    ArrayList<Float> thissky = getSkyline(size,realend,numofstreams,min,max,average);
    
    ArrayList<float[]> notes = new ArrayList();
    for (int i=1; i<thissky.size()-1; i+=2) { //for each substream in thissky
        ArrayList<float[]> thesenotes = getNotes(thissky.get(i), thissky.get(i+1), thissky.get(i+2), realstart);
        notes.addAll(thesenotes);
    }
    

    
    //BiHashMap<Integer, Integer, Integer> hash = new BiHashMap<Integer, Integer, Integer>();
    
    for (int i=0; i<(realend-realstart)/(16*globseglength); i++) { 
        //System.out.println(MeasureAnalyzer.getRhythm(notes, i, (float)globseglength));
        if(i < (realend-realstart)/(16*globseglength)-1){
            int[] arr = key(MeasureAnalyzer.getRhythm(notes, i, (float)globseglength), MeasureAnalyzer.getRhythm(notes, i+1, (float)globseglength));
            //System.out.println(MeasureAnalyzer.getRhythm(notes, i, (float)globseglength) + " " + MeasureAnalyzer.getRhythm(notes, i+1, (float)globseglength) + " " + arr[0] + " " + arr[1]);
            if(!XmkMain.hash.containsKeys(arr[0], arr[1])){
                XmkMain.hash.put(arr[0], arr[1], 1);
            }
            else{
                int r = XmkMain.hash.get(arr[0], arr[1]);
                XmkMain.hash.put(arr[0], arr[1], r+1);
            }
        }
    }
    //System.out.println("###########################################################################");
    
    //XmkMain.hash.printMap();
    
    return notes;
}
    
    
    
    //returns an arraylist of notes from a stream between left and right timesteps
    static ArrayList<float[]> getNotes(float left, float index, float right, float start) {
        ArrayList<float[]> output = new ArrayList();
        
        Stream s = streamlist;
        while (s.index != index) {
            if (s.next != null)
                s = s.next;
            else
                break;
        }
        Snote sn = s.sn;
        
	while(true) {
	    if(sn==null) break;
	    if(sn.ontime < left) {
                sn = sn.next;
                continue;
            }
	    if(sn.offtime > right) break;
	    //System.out.printf("[%d %d %d] ", sn.ontime, sn.offtime, sn.pitch);
            float[] floaty =  {(float)sn.pitch, (float)sn.ontime-start, (float)sn.offtime-start};
            //System.out.print(sn.pitch + " " + sn.ontime + " " + sn.offtime + "  ");
            output.add(floaty);
	    sn=sn.next;
	}
        //System.out.print("\n");
        
    
        return output;
    }
    
    
    
    static ArrayList<Float> getSkyline(float size, float realend, float numofstreams, float[] min, float[] max, float[] average) {
        ArrayList<Float> skyline = new ArrayList();
        float[] mysky = new float[(int) size];
        float[] mystream = new float[(int) size];
        for (int i=0; i<realend; i++) {
            mysky[i] = 0;
            mystream[i] = 0;
        }
        
        float left = 0;
        float height = 0;
        float right = 0;
        for (int i=0; i<numofstreams; i++) {
            left = min[i];
            height = average[i];
            right = max[i];
            for (int j = (int) (left); j < (int) (right); j++) {
                if (height > mysky[j]) {
                    mysky[j] = height;
                    mystream[j] = i;
                }
            }
        }
        int cnt = 0;
        skyline.add(mystream[cnt]); //,cnt+1+min});
        skyline.add((float)cnt + 1 + 0);
        cnt++;
        while (cnt < size - 1) {
            while (cnt < size - 1 && mystream[cnt] == mystream[cnt + 1]) {
                cnt++;
            }
            //System.out.print(mystream[cnt]+" "+(cnt+1+0)+" ");
            skyline.add(mystream[cnt]); //,cnt+1+min});
            skyline.add((float)cnt + 1 + 0);
            cnt++;
        }
        if (cnt == size - 1) {
            //System.out.print(mystream[(int)size-1]+" "+realend);
            skyline.add(mystream[(int) size - 1]);
            skyline.add(realend);
        }

        return skyline;
    }
    
    /*-------------------------------------------
    Hashmap functions
    -------------------------------------------*/
    static int[] key(String a, String b) {
        int ak  = 0 , bk = 0;
        for(int i = a.length()-1; i >=0; i--){
            if(a.charAt(i) == 'I'){
                ak += Math.pow(2,a.length()-1 -i);
            }
            if(b.charAt(i) == 'I'){
                bk += Math.pow(2,a.length()-1 -i);
            }
        }
        
        int[] keys = {ak,bk};
        return keys;
    }
}
