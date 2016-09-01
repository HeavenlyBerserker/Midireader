
package midireader.processingXmk;

import midireader.processingXmk.MeasureAnalyzer;
import midireader.auxClasses.basicTransformations;
import java.util.ArrayList;
import static midireader.XmkMain.GCD;
import static midireader.XmkMain.MEASURES;
import static midireader.auxClasses.basicTransformations.getHalfMeasure;

public class RhythmChanger {
    //creates a set of rules to be applied to the song in changeSong. 
    public static ArrayList<String> makeRules(ArrayList<String> patterns, ArrayList<String[]> patternData) {
        ArrayList<String> rules = new ArrayList();
        int startingnum = 0;
        
        for (int i=0; i<MEASURES; i++) {
            int size = (patterns.get(i).length() - patterns.get(i).replace("I", "").length());
            //float randomnum = (float)Math.random();
            //float curnum = lines[size];
            int flag = 0;
            for (int j=0; j<rules.size(); j++) {
                if (patterns.get(i).equals( (rules.get(j)).substring(0,16) )) {
                    flag = 1;
                }
            }
            double randy;
            while (flag == 0) {
                randy = Math.random();
                double curnum = 0;
                for (int j=0; j<patternData.size(); j++) { //lines[size]
                    if (Float.parseFloat(patternData.get(j)[0]) == (patterns.get(i).length() - patterns.get(i).replace("I", "").length()) ) { //if same number of I's
                        if (randy < curnum+Float.parseFloat(patternData.get(j)[1])) {
                            //System.out.println(curnum + " " + randy);
                            if (MeasureAnalyzer.onsetDistance(patternData.get(j)[2], patterns.get(i)) < 8) { //if onsets aren't moved too much
                                if (!patternData.get(j)[2].equals(patterns.get(i)))  {//if not equal
                                    rules.add(patterns.get(i) + " " + patternData.get(j)[2]);

                                    //System.out.println(curnum + " " + randy);
                                    System.out.println("Rule added: "+ patterns.get(i) + " " + patternData.get(j)[2]);
                                    flag = 1;
                                    break;
                                }
                                else { //skip rules that would not change anything

                                    //System.out.println(curnum + " " + randy);
                                    //randy = Math.random();
                                    //System.out.println("Rule not added: "+ patterns.get(i) + " " + patternData.get(j)[2]);
                                    if (Math.random() > 0.95) flag = 1;
                                    break;
                                }
                            }
                        }
                        curnum += Float.parseFloat(patternData.get(j)[1]);
                    }
                }
            }
        }
        return rules;
    }
    
    //changes each half-measure of the song to a new rhythm defined by the list of rules and returns entire song
    public static ArrayList<float[]> changeSong(ArrayList<float[]> notes, ArrayList<String> pattern, ArrayList<String> rules, ArrayList<ArrayList<Float>> patternNums) {
        ArrayList<float[]> output = new ArrayList();
        String newSequence;

        for (int i=0; i<MEASURES; i++) {

            newSequence = pattern.get(i);
            for (int j=0; j<rules.size(); j++) {
                if (newSequence.equals((rules.get(j)).substring(0,16))) {
                    newSequence = rules.get(j).substring(17,33);
                    break;
                }
                //if measure contains the first part of a rule and is similar to 
                else if (MeasureAnalyzer.rhythmSimilarity(newSequence, (rules.get(j)).substring(0,16)) > 0.8) {
                    String rule = rules.get(j).substring(0,16);
                    int count = newSequence.length() - newSequence.replace("I", "").length(); //count number of I's
                    int count2 = rule.length() - rule.replace("I", "").length();
                    if (count > count2) {
                        //applies rule to part of newSequence and then adds the other part back
                        String newSequence2 = addMeasure(subtractMeasure(newSequence,rule),rules.get(j).substring(17,33));
                        if (newSequence2.length() - newSequence2.replace("I", "").length() - count == 0) { //only if we haven't deleted notes
                            newSequence = newSequence2;
                            break;
                        }
                    }
                }
            }
            output.addAll(changeRhythm(basicTransformations.getHalfMeasure(notes,i),newSequence,(float)GCD*i*16,patternNums.get(i)));
            //System.out.println(newSequence);
        }
        return output;
    }
    
    /*Function: Change Rhythm---------------------------------------------------------
    Combines a list of notes and a rhythmic sequence to produce an arraylist of notes with desired rhythm
    Input: List of notes in a half measure, rhythm string in format "IOOIOIO" where 'I' denotes the onset of a note, start time in ticks
    Output: List of notes with new rhythm
    */
    public static ArrayList<float[]> changeRhythm(ArrayList<float[]> notes, String rhythmlist, float timestart, ArrayList<Float> patternNums) {
        ArrayList<float[]> notes2 = new ArrayList();
        if (notes.size() > 0) {
            int j=0;
            float currtime = timestart;
            float[] currnote = notes.get(0);
            int currplace = 0;
            currnote[1] = currtime;
            for (int i=0; i<rhythmlist.length(); i++) {
                if (rhythmlist.charAt(i) == 'I') {
                    if (patternNums.size() > currplace) {
                        int numofnotes = Math.round(patternNums.get(currplace));
                        
                        currplace++;
                        float currtime2 = 0;
                        for (int k=0; k<numofnotes; k++){
                            if (notes.size()>j) {
                                currtime2 = patternNums.get(currplace);
                                //System.out.println(currplace + " " + currtime2 + " " + notes.get(j)[0]);
                                currnote = notes.get(j);
                                currnote[1] = currtime;
                                currnote[2] = currtime+currtime2;
                                notes2.add(currnote);
                                j++;
                                currplace++;
                            }
                            
                        }
                    }
                }
                currtime += GCD;
            }
            currnote[2] = currtime;
            notes2.add(currnote);
        }
        return notes2;
    }
    
    //subtracts I's in pattern from I's in measure
    public static String subtractMeasure(String measure, String pattern) {
        String output = "";
        for (int i=0; i<16; i++) {
            if (measure.charAt(i) == 'I' && pattern.charAt(i) != 'I') {
                output += 'I';
            }
            else {
                output += '.';
            }
        }
        return output;
    }
    
    public static String addMeasure(String measure, String pattern) {
        String output = "";
        for (int i=0; i<16; i++) {
            if (measure.charAt(i) == 'I' || pattern.charAt(i) == 'I') {
                output += 'I';
            }
            else {
                output += '.';
            }
        }
        return output;
    }
}