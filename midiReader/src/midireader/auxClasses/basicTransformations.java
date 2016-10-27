/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.auxClasses;

import java.util.ArrayList;
import midireader.XmkMain;

/**
 *
 * @author domini
 */
public class basicTransformations {

    //returns # of measures in song
    public static int measures(ArrayList<float[]> notes) {
        return (int) ((notes.get(notes.size() - 1)[2] + XmkMain.GCD) / (XmkMain.GCD * 16));
    }

    //returns arraylist of pattern strings, each a half measure of the song
    public static ArrayList<String> getPatterns(String pattern) {
        ArrayList<String> output = new ArrayList();
        String thisSeq;
        for (int i = 0; i < pattern.length() - 15; i += 16) {
            thisSeq = pattern.substring(i, i + 16);
            output.add(thisSeq);
            //System.out.println(thisSeq);
        }
        return output;
    }

    //shifts all onsets/offsets in a note list by some amount of ticks
    public static ArrayList<float[]> offsetSong(ArrayList<float[]> notes, float ticks) {
        ArrayList<float[]> output = new ArrayList();
        for (int i = 0; i < notes.size(); i++) {
            float[] curnote = {notes.get(i)[0], notes.get(i)[1] + ticks, notes.get(i)[2] + ticks};
            output.add(curnote);
        }
        return output;
    }

    /*Function: GCD and sorting---------------------------------------------------------
    Computes GCDs and sorts notes by time
    Input: List of notes
    Output: Sorted list, GCD by global variable
     */
    /*
    public static ArrayList<float[]> gcds(ArrayList<float[]> notes) {
    //Calculating the lengths of each note in the song
    //Change this section if it is ------expressive performance----- we are dealing with.
    double dmin = Double.POSITIVE_INFINITY;
    for(int i=0; i < notes.size(); i++) {
    differences.add(notes.get(i)[2] - notes.get(i)[1]);
    //System.out.println(differences.get(i));
    if(i == 0){
    dmin = (int)(float)differences.get(i);
    }
    }
    int min = (int)dmin;
    for(int i = min; i > 0; i--) {
    boolean gcd = true;
    for(int j=0; j < notes.size(); j++) {
    if((int)(float)differences.get(j)%i == 0){}
    else{
    gcd = false;
    }
    }
    if(gcd){
    GCD = i;
    break;
    }
    }
    //System.out.println("GCD = " + GCD);
    //Sorting notes by starting time
    ArrayList<float[]> sNotes = new ArrayList();
    int si = notes.size();
    for(int i=0; i < si; i++) {
    dmin = Double.POSITIVE_INFINITY;
    int minind = 0;
    for(int j=0; j < notes.size(); j++) {
    if((double)notes.get(j)[1] < dmin){
    dmin = (double)notes.get(j)[1];
    minind = j;
    }
    }
    float[] n = {(float)notes.get(minind)[0], (float)notes.get(minind)[1], (float)notes.get(minind)[2]};
    sNotes.add(n);
    notes.remove(minind);
    //System.out.println(sNotes.get(i)[0] + " " + sNotes.get(i)[1] + " " + sNotes.get(i)[2]);
    }
    return sNotes;
    }*/
    //returns a single half-measure of the song's notes in ArrayList format
    public static ArrayList<float[]> getHalfMeasure(ArrayList<float[]> notes, int measureNumber) {
        ArrayList<float[]> output = new ArrayList();
        float timestart = measureNumber * XmkMain.GCD * 16;
        float timestop = (measureNumber + 1) * XmkMain.GCD * 16;
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i)[1] >= timestart && notes.get(i)[1] <= timestop) {
                output.add(notes.get(i));
            }
        }
        return output;
    }
    
}
