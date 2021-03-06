
package midireader.Temperley;

import java.util.ArrayList;
import java.util.EmptyStackException;
import static midireader.Temperley.Polyph.*;
import static midireader.Temperley.beatInduction.getDeviation;
import midireader.XmkMain;


public class MonophonicStreams {
    
    static ArrayList<float[]> selectStreams(int [] temp) { //runs skyline algorithm on the Temperley note streams by their average pitch

    float[] min = new float[(int) 10000];
    float[] max = new float[(int) 10000];
    note_struct[] min2 = new note_struct[(int) 10000];
    note_struct[] max2 = new note_struct[(int) 10000];
    float[] average = new float[(int) 10000]; //these used to be 500
    
    
    for (int i=0; i<10000; i++) {
        average[i] = 0;     //average pitch of this stream
        min[i] = 0;         //starttime of this stream
        max[i] = 0;         //endtime of this stream
        min2[i] = note[0];
        max2[i] = note[0];
    }
    
    for (int z = 0; z < numnotes; z++) {
        if (note[z].stream == 0) {
            continue;
        }
        if (segment[note[z].segment].start <= segment[min2[note[z].stream].segment].start || min2[note[z].stream].stream != note[z].stream) {
            //min started as the first note, but this is okay because we set it to a note in this stream if it is ever not a note in current stream
            //at which point it will be minimized
            min2[note[z].stream] = note[z];
        }
        if (segment[note[z].segment].start > segment[max2[note[z].stream].segment].start) {
            max2[note[z].stream] = note[z];
        }
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
            //if(sn.ontime < s.start) s.start=sn.ontime;
            //System.out.println(sn.ontime);
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
    //System.out.println("Real start:");
    //System.out.println(realstart);
    //System.out.println(segment[0].start);
    //System.out.println(globseglength);
    
    
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
    
    //ArrayList<Float> thissky = getSkyline(size,realend,numofstreams,min,max,average);
    
    char[][] thissky = getSkyline2(numofstreams,min2,max2,average);
    
    ArrayList<float[]> notes = new ArrayList();
    /*
    for (int i=1; i<thissky.size()-1; i+=2) { //for each substream in thissky
        ArrayList<float[]> thesenotes = getNotes(thissky.get(i), thissky.get(i+1), thissky.get(i+2), realstart);
       
        notes.addAll(thesenotes);
    }*/
    
    int zamt = 0;
    //BiHashMap<Integer, Integer, Integer> hash = new BiHashMap<Integer, Integer, Integer>();
    for (int i=0; i<thissky.length-1; i++) { 
        int[] arr = key(String.valueOf(thissky[i]), String.valueOf(thissky[i+1]));
        System.out.println(String.valueOf(thissky[i]) + " " + String.valueOf(thissky[i+1]) + " " + arr[0] + " " + arr[1]);
        if(arr[0] == 0 && arr[1] == 0){
            zamt++;
        }
        if(!XmkMain.hash.containsKeys(arr[0], arr[1])){
            XmkMain.hash.put(arr[0], arr[1], 1);
        }
        else{
            int r = XmkMain.hash.get(arr[0], arr[1]);
            XmkMain.hash.put(arr[0], arr[1], r+1);
        }
    }
    /*
    for (int i=0; i<(realend-realstart)/(16*globseglength); i++) { 
        //System.out.println(MeasureAnalyzer.getRhythm(notes, i, (float)globseglength));
        if(i < (realend-realstart)/(16*globseglength)-1){
            int[] arr = key(MeasureAnalyzer.getRhythm(notes, i, (float)globseglength), MeasureAnalyzer.getRhythm(notes, i+1, (float)globseglength));
            System.out.println(MeasureAnalyzer.getRhythm(notes, i, (float)globseglength) + " " + MeasureAnalyzer.getRhythm(notes, i+1, (float)globseglength) + " " + arr[0] + " " + arr[1]);
            if(arr[0] == 0 && arr[1] == 0){
                zamt++;
            }
            if(!XmkMain.hash.containsKeys(arr[0], arr[1])){
                XmkMain.hash.put(arr[0], arr[1], 1);
            }
            else{
                int r = XmkMain.hash.get(arr[0], arr[1]);
                XmkMain.hash.put(arr[0], arr[1], r+1);
            }
        }
    }*/
    temp[0] = zamt;
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
    
    //returns an arraylist containing notes given a stream, starting segment, and ending segment
    static ArrayList<float[]> getNotes2(float index, float start, float stop) {
    ArrayList<float[]> output = new ArrayList();
        
    for (int z = 0; z < numnotes; z++) {
        if (note[z].stream != index || note[z].segment < start || note[z].segment > stop) {
            continue;
        }
        int ontime = segment[note[z].segment].start;
        int offtime = segment[note[z].segment].end;
        float[] floaty =  {(float)note[z].pitch, (float)ontime, (float)offtime};
        output.add(floaty);
        }
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
    
    
    static char[][] getSkyline2(float numofstreams, note_struct[] min2, note_struct[] max2, float[] average) {
        
        //Create arrays to store skyline - mysky stores the height at each segment, and mystream the best stream at each segment
        float[] mysky = new float[segtotal+1];
        float[] mystream = new float[segtotal+1];
        for (int i=0; i<segtotal+1; i++) {
            mysky[i] = 0;
            mystream[i] = 0;
        }
        
        //Maximize the height of the skyline for each segment, where height refers to a stream's average pitch
        float height;
        for (int i=0; i<numofstreams; i++) {
            height = average[i];
            for (int j = min2[i].segment; j <= max2[i].segment; j++) {
                if (height > mysky[j]) {
                    mysky[j] = height;
                    mystream[j] = i;
                }
            }
        }
        
        //Create an array of each segment's melodic note, should one exist
        note_struct[] firstNote = new note_struct[numnotes];
        for (int z = 0; z < segtotal+1; z++) {
            firstNote[z] = null;
        }
        int finalend = 0;
        for (int z = 0; z < numnotes; z++) {
            //System.out.println(note[z].stream);
            //System.out.println(mystream[note[z].segment]);
            if (note[z].stream == mystream[note[z].segment]) {
                //System.out.println(note[z].ontime);
                firstNote[note[z].segment] = note2[z];
                if (note2[z].offtime > finalend) {
                    finalend = note2[z].offtime;
                }
            }
        }
        
        //Find the average length of a segment
        int averagelen=0;
        for (int seg=0; seg<=segtotal; seg++) {
            averagelen += segment[seg].end-segment[seg].start;
        }
        averagelen /= segtotal;
        
        globseglength = beatInduction.induceBeat(note2,segtotal,finalend,firstNote);
        
        
        if (globseglength == 0) {
            System.out.println("Induced beat of 0");
            throw new EmptyStackException();
        }
        if (getDeviation(globseglength,segtotal,finalend,firstNote) < 0.04) {
            System.out.println("Poorly induced beat of " + globseglength);
            throw new EmptyStackException();
        }
        if (getDeviation(globseglength,segtotal,finalend,firstNote) > 0.12) {
            System.out.println("Streamer program failure");
            throw new EmptyStackException();
        }
        System.out.println("Induced beat of " + globseglength);
        
        
        int offset = induceOffset(firstNote);
        System.out.println("Induced offset of " + offset + " beats");
        
        int size = (int)((finalend+10+globseglength*offset)/(16*globseglength)+1);
        char[][] rhythm= new char[size][16];
        for (int i=0; i<size; i++) {
            for (int j=0; j<16; j++) 
                rhythm[i][j] = 'O';
        }
        for (int seg=0; seg<=segtotal; seg++) {
            if (firstNote[seg] != null) { 
                //System.out.println(firstNote[seg].ontime);
                rhythm[(int)((firstNote[seg].ontime+10+globseglength*offset)/(16*globseglength))][(int)((firstNote[seg].ontime+10+globseglength*offset)/globseglength % 16)] = 'I';
            }
        }
        
        /*
        System.out.println("N: " +numnotes);
        for (int z=0; z<numnotes; z++) {
            //if (firstNote[seg] != null) {
            //System.out.println(note2[seg].ontime);
            rhythm[(int)((note2[z].ontime+10)/(16*globseglength))][(int)((note2[z].ontime+10)/globseglength % 16)] = 'I';
            //}
        }*/
        
        //print rhythm array
        /*
        for (int i=0; i<segtotal/16; i++) {
            for (int j=0; j<16; j++) {
                System.out.print(rhythm[i][j]);
            }
            System.out.println();
        }
        System.out.println();*/
        
        return rhythm;
    }
    
    
    static int induceOffset(note_struct[] firstNote) {
        
        float num[] = new float[16];
        int total = 0;
        int weights[] = {5,1,2,1,3,1,2,1,4,1,2,1,3,1,2,1};
        int offset = 0;
        for (int seg=0; seg<=segtotal; seg++) {
            if (firstNote[seg] != null) {
                //System.out.println(firstNote[seg].ontime);
                num[(int)((firstNote[seg].ontime+10)/globseglength % 16)] ++;
                total++;
            }
        }
        
        double currscore = 0, maxscore = 0;
        for (int i=0; i<16; i++) {
            currscore = 0;
            for (int j=0; j<16; j++) {
                currscore += weights[(j+i) % 16]*num[j];
            }
            if (currscore > maxscore) {
                maxscore = currscore;
                offset = i;
            }
        }
        
        
        return offset;
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
