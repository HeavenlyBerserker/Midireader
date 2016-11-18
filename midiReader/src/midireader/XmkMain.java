

package midireader;


import midireader.DataStructs.BiHashMap;
import midireader.patternDataProcessing.rhythmFrequency;
import midireader.processingXmk.RhythmChanger;
import midireader.processingXmk.MeasureAnalyzer;
import midireader.processingXmk.xmPlayer;
import midireader.output.writeMidi;
import midireader.processingHumdrumMelisma.chordMaker;
import midireader.auxClasses.basicTransformations;
import midireader.inputHumdrumMelisma.MelismaReader;
import midireader.inputHumdrumMelisma.ChordAnalyzer;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sound.midi.MetaMessage;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import midireader.Temperley.Globals;
import midireader.auxClasses.fooCallers;
import midireader.inputHumdrumMelisma.readMidi;
import midireader.Temperley.ProbMelisma;
import static midireader.processingHumdrumMelisma.chordMaker.printF;
import static midireader.inputXmk.xmReader.xmRead;
import midireader.output.writeNotes;
import midireader.processingXmk.RhythmChanger2;
import midireader.processingXmk.syncopalooza;
import midireader.output.ChainOutputs;


public class XmkMain {
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
    public static BiHashMap<Integer, Integer, Integer> hash = new BiHashMap<Integer, Integer, Integer>();
    
    public static void main(String[] args) throws Exception {
        
        List<String> files = new ArrayList<>();
        
        Path dir = Paths.get("input/InputV1/TestFiles");
        
        MelismaReader.getFileNames(files, dir);
        int successes = 0;
        
        //Zeroes analysis
        StringBuilder zeroes = new StringBuilder();
        int[] zamt = new int[files.size()];
        int zamtcnt = 0;
        int[] temp = new int[1];
        for(int i=0; i<zamt.length; i++){
            zamt[i] = 0;
        }
        
        //Error report
        StringBuilder errors = new StringBuilder();
        errors.append("Error Report"+ "\n");
        int err = 0;
        
        for (int i=0; i<files.size(); i++) {
            System.out.println(i + "/" + files.size() + " Curr file: " + files.get(i));
            //try {
                ProbMelisma.analyzeRag(files.get(i), temp);
                if(temp[0] > 0){
                    zamtcnt++;
                    zamt[i] = temp[0];
                }
                System.out.println("Success");
                successes++;
            //}
            //catch (Exception e) {
            //    System.out.println(e);
            //    errors.append("------------------Exception " + err + "-------------------\n " + files.get(i) + "\n" + e + "\n");
            //    err += 1;
            //}
        }
        
        //Error report
        System.out.println("Files successfully analyzed: "+successes+ "/" +files.size());
        errors.append("Error count: " + err + "\n");
        errors.append("Files successfully analyzed: "+successes+ "/" +files.size());
        
        //Zeroes analysis
        zeroes.append("Files with zeroes count: " + zamtcnt + "\n");
        for(int i=0; i<zamt.length; i++){
            if(zamt[i] > 0){
                zeroes.append("[" + zamt[i] + "] " + files.get(i) + "\n");
            }
        }
        zeroes.append("Files successfully analyzed: "+successes+ "/" +files.size());
        
        
        hash.writeToError("Exceptions", errors, "Exceptions Done");
        hash.writeToError("Zeroes", zeroes, "Zeroes Done");
        //hash.printMap();
        hash.writeToCsv("table");
        
        ArrayList<float[]> [][] chainByLength = new ArrayList[17][17];
        
        hash.MCAnalyze(chainByLength);
        
        ChainOutputs chainwrite = new ChainOutputs();
        chainwrite.MCOutput(chainByLength, "ChainOutput", "");
        
        /*
         syncopalooza.resynch(syncopalooza.desynch(syncopalooza.resynch("OOOOIOIOIOOIOOOO")));
        //input pattern data
   
        ArrayList<String[]> patternData = rhythmFrequency.readFile("input/V1 Input/" + "lhlpatterns_depth_nots.csv");
        patternData = rhythmFrequency.changeToIO(patternData);

        //All input filenames here------------------------------------------------------------------------------------
        String fileName = "BethSonata1.1Allegro";
        String filenameHar = fileName + "_tsroot.txt";
        String filenameMel = "sonata01-1.notes";
        String inFolderN = "";
        String outFolderN = "";
        
        //Print Some info.
        System.out.println("Filename: " + fileName);
        
        //-----------------------------------------MidiToNote------------------------------------------------
        ArrayList<float[]> readingMid = new ArrayList();
        readingMid = fooCallers.midiToNotes("bach.annamin.mid");
        //chordMaker.printF(readingMid);
        writeNotes.writeNotes("bach.annamin", readingMid);
        
        //-----------------------------------------No input under here------------------------------------------------
        //Chord processing-----------------------------------------------------------------------
        System.out.print("Measures not adding up to 1 (Please check): --------------------------------");
        ArrayList<float[]> chordList = new ArrayList();
        int[] timeSig = {0,0,0};
        chordList = ChordAnalyzer.chordNotes(chordList, "input/V1 Input/" + inFolderN + filenameHar, timeSig);
        ArrayList<float[]> chordsWrite;
        float ts = 4/4 - (float)0.001;
        int bpm = timeSig[2];
        float speed = 59520/120*4;
        //ChordAnalyzer.printArray(timeSig);
        //chordMaker.printF(chordList);
        chordsWrite = chordMaker.chordMake(chordList, ts, speed);
        //chordMaker.print(chordsWrite);
        System.out.print("\n-----------------------------------------------------------------------------\nBPM from Humdrum: " + bpm);
        
        
        //Melody processing-----------------------------------------------------------------------
        ArrayList<float[]> notes = MelismaReader.readFile("input/V1 Input/" + inFolderN + filenameMel);
        //ArrayList<float[]> notes = readMidi(MidiSystem.getSequence(new File("op01n02b.mid")));
        
        System.out.println("\nMM " + MM);
        GCD = (int)(1000*60/(240*2));
        System.out.println("GCD " + GCD);
        resolution = GCD*4; //GCD*4; // (ticks/beat)
        MEASURES = basicTransformations.measures(notes);
        System.out.println(MEASURES + " measures");
        
        //notes = gcds(notes);
        //notes = melodyChanger.makeMonophonic(notes);
        
        /*
        ArrayList<String> patterns = new ArrayList();
        ArrayList<ArrayList<Float>> patternNums = new ArrayList();
        float lhloverall = 0;
        for (int i=0; i<MEASURES; i++) {
            patterns.add(MeasureAnalyzer.getRhythm(notes,i,GCD));
            lhloverall += MeasureAnalyzer.LHL(MeasureAnalyzer.getRhythm(notes,i,GCD));
            patternNums.add(MeasureAnalyzer.patternNums(getHalfMeasure(notes,i),GCD,patterns.get(i),GCD*i*16));
        }
        lhloverall = lhloverall/MEASURES;
        System.out.println("Syncopation: " + lhloverall);
        //ArrayList<String[]> patterns2 = MeasureAnalyzer.measureFrequencies(patterns);
        ArrayList<String> rules = RhythmChanger.makeRules(patterns,patternData);
        notes = RhythmChanger.changeSong(notes,patterns,rules,patternNums);
        
        //Merging Melody and harmony---------------------------------------------------------------
        notes.addAll(chordsWrite);
        //chordMaker.print(chordsWrite);
        //notes = offsetSong(notes,60);
        writeMidi.write(notes, "output/" + outFolderN + "ZTest" + filenameHar.substring(0, filenameHar.length()-4) + ".mid");
        
        //System.out.println(MeasureAnalyzer.getOverallSimilarity(notes,7,8,GCD));
        
        
        ArrayList<float[]> noteXmRead = new ArrayList();
        String file = "yankeeDb";
        String filenameXm = "input/xm/" + file + ".xmk";
        noteXmRead = xmRead(filenameXm);
        ArrayList<float[]> chords = xmPlayer.xmPlay(noteXmRead,1);
        ArrayList<float[]> notesXm = xmPlayer.xmPlay(noteXmRead,0);
        
        GCD = (int)(60000/(noteXmRead.get(0)[2]*4));
        ArrayList<String> patterns = new ArrayList();
        ArrayList<ArrayList<Float>> patternNums = new ArrayList();
        float lhloverall = 0;
        
        
        //chordMaker.print(notesXm);
        /*
        chordMaker.print(noteXmRead);
        ArrayList<float[]> noteXm = new ArrayList();
        noteXm = xmPlayer.xmPlay(noteXmRead);
        
        MEASURES = basicTransformations.measures(notesXm);
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
        //chordMaker.print(noteXm);
        writeMidi.write(chords, "output/xmk/palooza/" + outFolderN + file + ".mid");
        
        /*
        
        study when passing tones occur
            separate melody
            identify chords
            to find passing tones - look at every 3 note block of melody where 2 outer are consonant with each other and dissonant with middle
            determine how frequent, how often chromatic, compare to other genres
        */
    }

}