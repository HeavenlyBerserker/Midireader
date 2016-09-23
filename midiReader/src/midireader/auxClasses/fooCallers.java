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
}
