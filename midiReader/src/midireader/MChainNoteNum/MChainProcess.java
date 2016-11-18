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
import static midireader.inputXmk.xmReader.xmRead;
import midireader.output.writeMidi;
import midireader.patternDataProcessing.rhythmFrequency;
import midireader.processingHumdrumMelisma.chordMaker;
import midireader.processingXmk.MeasureAnalyzer;
import midireader.processingXmk.RhythmChanger2;
import midireader.processingXmk.syncopalooza;
import midireader.processingXmk.xmPlayer;

/**
 *
 * @author domini
 */
public class MChainProcess {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static ArrayList differences = new ArrayList();
    public static int GCD = 0;
    public static float resolution;
    public static float ppq;
    public static float MEASURES;
    public static float MM; //beats per minute from melisma
    public static int lines[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    
    public static void processingS1(String filename, boolean b, ArrayList<float[]> [][] chain) throws IOException{
        ArrayList<String[]> patternData = rhythmFrequency.readFile("input/InputV1/" + "lhlpatterns_depth_nots.csv");
        patternData = rhythmFrequency.changeToIO(patternData);

        System.out.println("\nMM " + MM);
        GCD = (int)(1000*60/(240*2));
        System.out.println("GCD " + GCD);
        resolution = GCD*4; //GCD*4; // (ticks/beat)
        
        //All input filenames here------------------------------------------------------------------------------------
        String fileName = "BethSonata1.1Allegro";
        String filenameHar = fileName + "_tsroot.txt";
        String filenameMel = "sonata01-1.notes";
        String inFolderN = "";
        String outFolderN = "";
        String file = "yankeeDb";
        String filenameXm = "input/xm/" + file + ".xmk";
        
        //Print Some info.
        System.out.println("Filename: " + fileName);
        
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
        System.out.println(MEASURES);
        for (int i=0; i<MEASURES; i++) {
            patterns.add(MeasureAnalyzer.getRhythm(notesXm,i,GCD));
            //System.out.println(MeasureAnalyzer.getRhythm(notesXm,i,GCD));
            lhloverall += MeasureAnalyzer.LHL(MeasureAnalyzer.getRhythm(notesXm,i,GCD));
            patternNums.add(MeasureAnalyzer.patternNums(basicTransformations.getHalfMeasure(notesXm,i),GCD,patterns.get(i),GCD*i*16));
            
        }
        lhloverall = lhloverall/MEASURES;
        System.out.println("Syncopation: " + lhloverall);
        //ArrayList<String[]> patterns2 = MeasureAnalyzer.measureFrequencies(patterns);
        
        //ArrayList<String> rules = RhythmChanger.makeRules(patterns,patternData);
        ArrayList<String> rules = syncopalooza.makeRules(patterns);
        System.out.println();
        notesXm = RhythmChanger2.changeSongSync(notesXm,patterns,rules,patternNums,0.1);
        
        ArrayList<String> patternsb = new ArrayList();
        ArrayList<ArrayList<Float>> patternNumsb = new ArrayList();
        System.out.println();
        for (int i=0; i<MEASURES; i++) {
            patternsb.add(MeasureAnalyzer.getRhythm(notesXm,i,GCD));
            patternNumsb.add(MeasureAnalyzer.patternNums(basicTransformations.getHalfMeasure(notesXm,i),GCD,patternsb.get(i),GCD*i*16));
        }
        ArrayList<String> rulesb = syncopalooza.makeRules(patternsb);
        notesXm = RhythmChanger2.changeSongSync(notesXm,patternsb,rulesb,patternNumsb,0.1);
        
        
        chords.addAll(notesXm);
        chordMaker.print(noteXm);
        writeMidi.write(chords, "output/xmk/palooza/" + outFolderN + file + ".mid");
    }
}
