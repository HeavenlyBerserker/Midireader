/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.MChainNoteNum;

import java.io.IOException;
import java.util.ArrayList;
import static midireader.XmkMain.GCD;
import static midireader.XmkMain.MEASURES;
import static midireader.XmkMain.MM;
import static midireader.XmkMain.resolution;
import midireader.auxClasses.basicTransformations;
import static midireader.auxClasses.basicTransformations.getHalfMeasure;
import midireader.inputHumdrumMelisma.ChordAnalyzer;
import midireader.inputHumdrumMelisma.MelismaReader;
import static midireader.inputXmk.xmReader.xmRead;
import midireader.output.writeMidi;
import midireader.patternDataProcessing.rhythmFrequency;
import midireader.processingHumdrumMelisma.chordMaker;
import midireader.processingXmk.MeasureAnalyzer;
import midireader.processingXmk.RhythmChanger;
import midireader.processingXmk.RhythmChanger2;
import midireader.processingXmk.syncopalooza;
import static midireader.processingXmk.syncopalooza.desynch;
import static midireader.processingXmk.syncopalooza.resynch;
import midireader.processingXmk.xmPlayer;
import java.util.Random;

/**
 *
 * @author domini
 */
public class MChainProcess {
    
    public static void processingS1(String file, boolean b, ArrayList<float[]> [][] chain, float prob) throws IOException{
        
        
        
        //System.out.println("\nMM " + MM);
        GCD = (int)(1000*60/(240*2));
        //System.out.println("GCD " + GCD);
        resolution = GCD*4; //GCD*4; // (ticks/beat)
        
        //All input filenames here------------------------------------------------------------------------------------
        String filenameXm = "input/xm/" + file + ".xmk";
        
        System.out.print("--------------------------------\n"
                + "Version 2 run on file : " + filenameXm + "\n"
                + "--------------------------------\n");
       
        
        ArrayList<float[]> noteXmRead = new ArrayList();
        noteXmRead = xmRead(filenameXm);
        ArrayList<float[]> chords = xmPlayer.xmPlay(noteXmRead,1);
        ArrayList<float[]> notesXm = xmPlayer.xmPlay(noteXmRead,0);
        
        GCD = (int)(60000/(noteXmRead.get(0)[2]*4));
        ArrayList<String> patterns = new ArrayList();
        ArrayList<ArrayList<Float>> patternNums = new ArrayList();
        float lhloverall = 0;
        
        
        //chordMaker.print(notesXm);
        //chordMaker.print(noteXmRead);
        ArrayList<float[]> noteXm = new ArrayList();
        noteXm = xmPlayer.xmPlay(noteXmRead, 1);
        
        
        MEASURES = basicTransformations.measures(notesXm);
        //System.out.println(MEASURES);
        for (int i=0; i<MEASURES; i++) {
            patterns.add(MeasureAnalyzer.getRhythm(notesXm,i,GCD));
            //System.out.println(MeasureAnalyzer.getRhythm(notesXm,i,GCD));
            lhloverall += MeasureAnalyzer.LHL(MeasureAnalyzer.getRhythm(notesXm,i,GCD));
            patternNums.add(MeasureAnalyzer.patternNums(basicTransformations.getHalfMeasure(notesXm,i),GCD,patterns.get(i),GCD*i*16));
        }
        lhloverall = lhloverall/MEASURES;
        //System.out.println("Syncopation: " + lhloverall);
        //ArrayList<String[]> patterns2 = MeasureAnalyzer.measureFrequencies(patterns);
        
        //ArrayList<String> rules = RhythmChanger.makeRules(patterns,patternData);
        ArrayList<String> rules = makeRules(patterns, chain);
        //System.out.println();
        notesXm = RhythmChanger2.changeSongSync(notesXm,patterns,rules,patternNums,prob);
        
        //printL("Rules:",rules);
        //printL("\nPatterns: ", patterns);
        
        chords.addAll(notesXm);
        //chordMaker.print(noteXm);
        writeMidi.write(chords, "output/xmk/V2.0/" + file + ".mid");
        
    }
    
    private static void printL(String s, ArrayList<String> a){
        System.out.println(s);
        for(int i = 0; i < a.size(); i++){
            System.out.println(a.get(i));
        }
    }
    
    private static ArrayList<String> makeRules(ArrayList<String> patterns, ArrayList<float[]> [][] chain) {
        ArrayList<String> rules = new ArrayList();
        Random rand = new Random();

        //Create random rule for first measure
        int size = (patterns.get(0).length() - patterns.get(0).replace("I", "").length());
        float sum = 0;
        for(int i = 0; i < 17; i++){
            for(int j = 0; j < chain[i][size].size(); j++){
                sum += chain[i][size].get(j)[1];
            }
        }
        //System.out.println("sum " + sum);
        
        float finalX = sum * rand.nextFloat();
        
        sum = 0;
        int b = 0;
        for(int i = 0; i < 17; i++){
            for(int j = 0; j < chain[i][size].size(); j++){
                sum += chain[i][size].get(j)[1];
                if(sum >= finalX){
                    rules.add(patterns.get(0) + " " + KeytoIO(chain[i][size].get(j)[0]));
                    b = 1;
                    break;
                }
            }
            if(b == 1) break;
        }
        //
        
        //All other non-first measure rules
        for (int i=1; i<patterns.size(); i++) {
            //# of zeroes in previous pattern
            int size1 = (patterns.get(i-1).length() - patterns.get(i-1).replace("I", "").length());
            //# of zeroes in current pattern
            size = (patterns.get(i).length() - patterns.get(i).replace("I", "").length());
            
            int flag = 0;
            for (int j=0; j<rules.size(); j++) {
                //If first measure pattern, do nothing
                if (patterns.get(i).equals( (rules.get(j)).substring(0,16) )) {
                    flag = 1;
                }
            }
            if (flag == 0) {
                //Adding rules
                float ra = rand.nextFloat();
                float c = 0;
                for(int j = 0; j < chain[size1][size].size(); j++){
                    c += chain[size1][size].get(j)[1];
                    if(c >= ra){
                        rules.add(patterns.get(i) + " " + KeytoIO(chain[size1][size].get(j)[0]));
                        break;
                    }
                }
            }
        }
        
        //printL("Rules:",rules);
        
        return rules;
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
    
    
    private static String KeytoIO(int key){
        String s = "";
        
        String number = Integer.toBinaryString(key);
        
        //System.out.println("BinaryString [" + number + "]");
        
        for(int i = 0; i < number.length(); i++){
            if(number.charAt(i) == '1')
                s += "I";
            else
                s += "O";
        }
        
        while(s.length() < 16){
            s = "O" + s;
        }
        return s;
    }
    
    private static String KeytoIO(float key){
        String s = "";
        
        int k = (int)key;
        String number = Integer.toBinaryString(k);
        
        //System.out.println("BinaryString [" + number + "]");
        
        for(int i = 0; i < number.length(); i++){
            if(number.charAt(i) == '1')
                s += "I";
            else
                s += "O";
        }
        
        while(s.length() < 16){
            s = "O" + s;
        }
        
        return s;
    }
}
