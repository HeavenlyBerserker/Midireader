/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.auxClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import midireader.XmkMain;
import midireader.inputHumdrumMelisma.readMidi;
import midireader.processingHumdrumMelisma.chordMaker;

import java.io.File; 
import java.io.FileOutputStream; 
import java.io.IOException; 
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
import java.util.Map.Entry; 
import java.util.Stack; 
import java.util.concurrent.ConcurrentHashMap; 
 
import javax.sound.midi.InvalidMidiDataException; 
import javax.sound.midi.MetaMessage; 
import javax.sound.midi.MidiDevice; 
import javax.sound.midi.MidiDevice.Info; 
import javax.sound.midi.MidiEvent; 
import javax.sound.midi.MidiMessage; 
import javax.sound.midi.MidiSystem; 
import javax.sound.midi.MidiUnavailableException; 
import javax.sound.midi.Receiver; 
import javax.sound.midi.Sequence; 
import javax.sound.midi.Sequencer; 
import javax.sound.midi.ShortMessage; 
import javax.sound.midi.Synthesizer; 
import javax.sound.midi.SysexMessage; 
import javax.sound.midi.Track; 
import javax.sound.midi.Transmitter; 
import midireader.Temperley.ProbMelisma;
import static midireader.XmkMain.hash;
import midireader.inputHumdrumMelisma.MelismaReader;
import midireader.output.ChainOutputs;


/**
 *
 * @author domini
 */
public class fooCallers {
    
    public static ArrayList<float[]> midiToNotes(String filename) throws IOException, InvalidMidiDataException{
            ArrayList<float[]> readingMid = new ArrayList();
            Sequence sequence = MidiSystem.getSequence(new File("input/midiNotes/" + filename));
            readingMid = readMidi.readMidi(sequence);
            //chordMaker.printF(readingMid);
            
            for(int i = 0; i < readingMid.size(); i++){
                if(readingMid.get(i)[0] == 0){
                    readingMid.remove(i);
                    i--;
                }
            }
            
            for(int i = 0; i < readingMid.size(); i++){
                float[] temp = readingMid.get(i);
                float min = readingMid.get(i)[1];
                int ind = i;
                for(int j = i; j < readingMid.size(); j++){
                    if(min > readingMid.get(j)[1]){
                        min = readingMid.get(j)[1];
                        ind = j;
                    }
                }
                readingMid.set(i, readingMid.get(ind));
                readingMid.set(ind, temp);
            }
            /*
            float[] temp = readingMid.remove(0);
            readingMid.add(temp);*/
            
            return readingMid;
    }
    
    public static boolean noteAnalysis(String path, String tname) throws IOException, InvalidMidiDataException{
        boolean t = true;
        
        List<String> files = new ArrayList<>();
        Path dir = Paths.get(path);
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
            try {
                ProbMelisma.analyzeRag(files.get(i), temp);
                if(temp[0] > 0){
                    zamtcnt++;
                    zamt[i] = temp[0];
                }
                System.out.println("Success");
                successes++;
            }
            catch (Exception e) {
                System.out.println(e);
                errors.append("------------------Exception " + err + "-------------------\n " + files.get(i) + "\n" + e + "\n");
                err += 1;
            }
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
        hash.writeToCsv(tname);
        
        ArrayList<float[]> [][] chainByLength = new ArrayList[17][17];
        
        hash.MCAnalyze(chainByLength);
        
        ChainOutputs chainwrite = new ChainOutputs();
        chainwrite.MCOutput(chainByLength, "ChainOutput", "");
        
        return t;
    }
    
    
}
