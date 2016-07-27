

package midireader;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.sound.midi.MetaMessage;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import static midireader.chordMaker.printF;
import static midireader.xmReader.xmRead;

public class MidiReader {
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
    
    public static void write(ArrayList<float[]> notes, String filename) {
    System.out.println("midifile begin ");
	try
	{
    //****  Create a new MIDI sequence with 24 ticks per beat  ****
                    Sequence s = new Sequence(javax.sound.midi.Sequence.PPQ,(int)resolution);

    //****  Obtain a MIDI track from the sequence  ****
                    Track t = s.createTrack();

    //****  General MIDI sysex -- turn on General MIDI sound set  ****
                    byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
                    SysexMessage sm = new SysexMessage();
                    sm.setMessage(b, 6);
                    MidiEvent me = new MidiEvent(sm,(long)0);
                    t.add(me);
    //****  set tempo (meta event)  ****
                    MetaMessage mt = new MetaMessage();
                    byte[] bt = {0x06, (byte)0x00, 0x00};
                    mt.setMessage(0x51 ,bt, 3);
                    me = new MidiEvent(mt,(long)0);
                    t.add(me);
    //****  set track name (meta event)  ****
                    mt = new MetaMessage();
                    String TrackName = "midifile track";
                    mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
                    me = new MidiEvent(mt,(long)0);
                    t.add(me);
    //****  set omni on  ****
                    ShortMessage mm = new ShortMessage();
                    mm.setMessage(0xB0, 0x7D,0x00);
                    me = new MidiEvent(mm,(long)0);
                    t.add(me);
    //****  set poly on  ****
                    mm = new ShortMessage();
                    mm.setMessage(0xB0, 0x7F,0x00);
                    me = new MidiEvent(mm,(long)0);
                    t.add(me);
    //****  set instrument to Piano  ****
                    mm = new ShortMessage();
                    mm.setMessage(0xC6, 15, 0x05);
                    me = new MidiEvent(mm,(long)0);
                    t.add(me);

                    for (int i=0; i<notes.size();i++) {
                        float[] note = notes.get(i);

                        //****  note on  ****
                        mm = new ShortMessage();
                        mm.setMessage(NOTE_ON,(int)note[0],0x60); 
                        me = new MidiEvent(mm,(long)note[1]); //time on
                        t.add(me);
                        //****  note off  ****
                        mm = new ShortMessage();
                        mm.setMessage(NOTE_OFF,(int)note[0],0x40);
                        me = new MidiEvent(mm,(long)note[2]); //time off
                        t.add(me);
                    }

    //****  set end of track (meta event) 19 ticks later  ****
                    mt = new MetaMessage();
            byte[] bet = {}; // empty array
                    mt.setMessage(0x2F,bet,0);
                    me = new MidiEvent(mt, (long)140);
                    t.add(me);

    //****  write the MIDI sequence to a MIDI file  ****
                    File f = new File(filename);
                    MidiSystem.write(s,1,f);
            } //try //try
                    catch(Exception e)
            {
                    System.out.println("Exception caught " + e.toString());
            } //catch
        System.out.println("midifile end ");
    } //main
    
    /*Function: Read Midi---------------------------------------------------------
    Reads a midi file and returns an list of values about the notes
    Input: Midi sequence
    Output: List of notes (note, onset, offset)
    */
    public static ArrayList<float[]> readMidi(Sequence sequence) {
        ArrayList<float[]> notes = new ArrayList();
        resolution = sequence.getResolution();
        ppq = sequence.PPQ;
        System.out.println("Resolution = " + resolution);
        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
            for (int i=0; i < track.size(); i++) { 
                MidiEvent event = track.get(i);
                float time = event.getTick();
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        float notey[] = {(float)key,time,0}; //note, starttime, stoptime
                        notes.add(notey);
                        //int octave = (key / 12)-1;
                        //int note = key % 12;
                        //int velocity = sm.getData2();
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int found = 0;
                        for (int j=notes.size()-1; j>=0; j--) {
                            if (found == 0 && notes.get(j)[0] == (float)key) {
                                float notey[] = notes.get(j);
                                notey[2] = time;//-notes.get(j)[1];
                                notes.set(j,notey);
                                //System.out.println(notes.get(j)[0] + " " + notes.get(j)[1] + " " + notes.get(j)[2]);
                                found = 1;
                            }
                        }
                    }
                }
            }
        }
        
    return notes;
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
        float timestart = measureNumber*GCD*16;
        float timestop = (measureNumber+1)*GCD*16;
        for (int i=0; i<notes.size(); i++) {
            if (notes.get(i)[1] >= timestart && notes.get(i)[1] <=timestop) {
                output.add(notes.get(i));
            }
        }
        return output;
    }
    
    //returns # of measures in song
    public static int measures(ArrayList<float[]> notes) {
        return (int)((notes.get(notes.size()-1)[2]+GCD)/(GCD*16));
    }
    
    //returns arraylist of pattern strings, each a half measure of the song
    public static ArrayList<String> getPatterns(String pattern) {
        ArrayList<String> output = new ArrayList();
        String thisSeq;
        for (int i=0; i<pattern.length()-15; i+= 16) {
            thisSeq = pattern.substring(i,i+16);
            output.add(thisSeq);
            //System.out.println(thisSeq);
        }
        return output;
    }
    
    
    
    
    //shifts all onsets/offsets in a note list by some amount of ticks
    public static ArrayList<float[]> offsetSong( ArrayList<float[]> notes, float ticks) {
        ArrayList<float[]> output = new ArrayList();
        for (int i=0; i< notes.size(); i++) {
            float[] curnote = {notes.get(i)[0],notes.get(i)[1]+ticks,notes.get(i)[2]+ticks};
            output.add(curnote);
        }
        return output;
    }
    
    
    public static void main(String[] args) throws Exception {

        //input pattern data
        ArrayList<String[]> patternData = rhythmFrequency.readFile("input/" + "lhlpatterns_depth_nots.csv");
        patternData = rhythmFrequency.changeToIO(patternData);

        //All input filenames here------------------------------------------------------------------------------------
        String fileName = "BethSonata1.1Allegro";
        String filenameHar = fileName + "_tsroot.txt";
        String filenameMel = "sonata01-1.notes";
        String inFolderN = "";
        String outFolderN = "";
        
        //Print Some info.
        System.out.println("Filename: " + fileName);
        
        
        //-----------------------------------------No input under here------------------------------------------------
        //Chord processing-----------------------------------------------------------------------
        System.out.print("Measures not adding up to 1 (Please check): --------------------------------");
        ArrayList<float[]> chordList = new ArrayList();
        int[] timeSig = {0,0,0};
        chordList = ChordAnalyzer.chordNotes(chordList, "input/" + inFolderN + filenameHar, timeSig);
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
        ArrayList<float[]> notes = MelismaReader.readFile("input/" + inFolderN + filenameMel);
        //ArrayList<float[]> notes = readMidi(MidiSystem.getSequence(new File("op01n02b.mid")));
        
        System.out.println("\nMM " + MM);
        GCD = (int)(1000*60/(240*4));
        System.out.println("GCD " + GCD);
        resolution = 1000; //GCD*4; // (ticks/beat)
        MEASURES = measures(notes);
        System.out.println(MEASURES + " measures");
        
        //notes = gcds(notes);
        notes = melodyChanger.makeMonophonic(notes);
        
        
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
        write(notes, "output/" + outFolderN + "ZTest" + filenameHar.substring(0, filenameHar.length()-4) + ".mid");
        
        //System.out.println(MeasureAnalyzer.getOverallSimilarity(notes,7,8,GCD));
        
        
        ArrayList<float[]> noteXmRead = new ArrayList();
        String filenameXm = "input/xm/odeToJoy.xmk";
        noteXmRead = xmRead(filenameXm);
        
        ArrayList<float[]> noteXm = new ArrayList();
        noteXm = xmPlayer.xmPlay(noteXmRead);
        
        //chordMaker.print(noteXm);
        write(noteXm, "output/xmk/" + outFolderN + "odeToJoy.mid");
    }
}