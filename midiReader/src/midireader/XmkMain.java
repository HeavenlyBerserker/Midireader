

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
import midireader.MChainNoteNum.MChainProcess;
import midireader.MChainNoteNum.MChainRead;
import midireader.Temperley.Globals;
import midireader.auxClasses.FunctionCallers;
import midireader.inputHumdrumMelisma.readMidi;
import midireader.Temperley.ProbMelisma;
import static midireader.inputXmk.xmReader.xmRead;
import static midireader.auxClasses.basicTransformations.getHalfMeasure;
import static midireader.auxClasses.basicTransformations.getHalfMeasure;
import static midireader.processingHumdrumMelisma.chordMaker.printF;
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
        
        //Uncomment the following line to run note analysis
        //FunctionCallers.noteAnalysis("input/InputV1/notefiles", "table");
        
        //------Reads transition probabilities from file-------------------
        //Reading chainOutput (toggle second arg for print or not)
        ArrayList<float[]> [][] chain = MChainRead.readChainOutput("output/ChainOutput.csv", false);
        //-----------------------------------------------------------------
        
        //-------------Version 1 activation-------------------
        FunctionCallers.V1Call("ronda44",0.3f);
        //----------------------------------------------------
        
        //-------------Version 2 activation-------------------
        //Line 1 prints out results, line 2 doesn't.
        //MChainProcess.processingS1("yankeeDb", true, chain);
        MChainProcess. processingS1("ronda44", false, chain);
        //----------------------------------------------------
        
        //-------------Syncopalooza activation-------------------
        //Input file location: "input/xm/" + filePath + ".xmk"
        FunctionCallers.SyncoCall("ronda44",0.3f);
        //----------------------------------------------------
        
    }

}