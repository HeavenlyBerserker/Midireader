

package midireader;


import java.io.File;
import java.util.ArrayList;
import javax.sound.midi.MetaMessage;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

public class MidiReader {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static ArrayList differences = new ArrayList();
    public static int GCD = 0;
    public static float resolution;
    public static float tempo;
    public static float MEASURES;
    
    public static void write(ArrayList<float[]> notes) {
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
		File f = new File("output.mid");
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
    public static ArrayList<float[]> gcds(ArrayList<float[]> notes) {
        /* Calculating the lengths of each note in the song
        Change this section if it is ------expressive performance----- we are dealing with.
        */
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
        System.out.println("GCD = " + GCD);
        
        /*Sorting notes by starting time
        
        */
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
    }
    
    public static String rhythIO(ArrayList<float[]> notesrest) {
        String io = "";
        for(int i=0; i < notesrest.size(); i++) {
            if(notesrest.get(i)[0] == -1){
                for(int j=0; j < notesrest.get(i)[1]; j++){
                    io += ".";
                }
            }
            else{
                io += "I";
                for(int j=0; j < notesrest.get(i)[1]-1; j++){
                    io += ".";
                }
            }
        }
        return io;
    }
        
    public static ArrayList<float[]> silences(ArrayList<float[]> notes) {
        // Note silences is an array list of format = {note number, unit duration}
        // -1 is the note is a rest
        // unit duration is note_duration/GCD
        // *Does not work with polyphonic melodies
        ArrayList<float[]> notessilences = new ArrayList();
        int count = 0;
        if(notes.get(0)[1] != 0){
            float[] s = {-1, (int)notes.get(0)[1]/GCD};
            notessilences.add(s);
            //System.out.println(notessilences.get(count)[0] + " " + notessilences.get(count++)[1]);
        }
        for(int i=0; i < notes.size()-1; i++) {
            //if(notes.get(i)[2] < notes.get(i+1)[1]){
                float[] s = {(int)notes.get(i)[0], ((int)notes.get(i)[2]-(int)notes.get(i)[1])/GCD};
                notessilences.add(s);
                //System.out.println(notessilences.get(count)[0] + " " + notessilences.get(count++)[1]);
                float[] s2 = {-1, ((int)notes.get(i+1)[1]-notes.get(i)[2])/GCD};
                notessilences.add(s2);
                //System.out.println(notessilences.get(count)[0] + " " + notessilences.get(count++)[1]);
            //}
            // Does not work with polyphonic melodies.
            /*
            else if(notes.get(i)[2] > notes.get(i+1)[1]){
                System.out.println("Error: Melody is not monophonic.");
                break;
                }
            else{
                float[] s = {(int)notes.get(i)[0], ((int)notes.get(i)[2]-(int)notes.get(i)[1])/GCD};
                notessilences.add(s);
                //System.out.println(notessilences.get(count)[0] + " " + notessilences.get(count++)[1]);
                }*/
        }
        return notessilences;
    }
    
    /*Function: Change Rhythm---------------------------------------------------------
    Combines a list of notes and a rhythmic sequence to produce an arraylist of notes with desired rhythm
    Input: List of notes in a half measure, rhythm string in format "IOOIOIO" where 'I' denotes the onset of a note, start time in ticks
    Output: List of notes with new rhythm
    */
    public static ArrayList<float[]> changeRhythm(ArrayList<float[]> notes, String rhythmlist, float timestart) {
        ArrayList<float[]> notes2 = new ArrayList();
        int j=0;
        float currtime = timestart;
        float[] currnote = notes.get(j);
        currnote[1] = currtime;
        for (int i=0; i<rhythmlist.length(); i++) {
            if (notes.size()>j) {
                if (rhythmlist.charAt(i) == 'I') {
                    currnote[2] = currtime;
                    notes2.add(currnote);
                    currnote = notes.get(j);
                    currnote[1] = currtime;
                    j++;
                }
            }
            currtime += GCD;
        }
        currnote[2] = currtime;
        notes2.add(currnote);
        return notes2;
    }
    
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
    
    //changes each half-measure of the song to a new rhythm defined by the list of rules and returns entire song
    public static ArrayList<float[]> changeSong(ArrayList<float[]> notes, ArrayList<String> pattern, ArrayList<String> rules) {
        ArrayList<float[]> output = new ArrayList();
        String newSequence;

        System.out.println(MEASURES + " measures");
        for (int i=0; i<MEASURES; i++) {

            newSequence = pattern.get(i);
            for (int j=0; j<rules.size(); j++) {
                 if (newSequence.equals((rules.get(j)).substring(0,16))) {
                     newSequence = rules.get(j).substring(17,33);
                }
            }
            output.addAll(changeRhythm(getHalfMeasure(notes,i),newSequence,(float)GCD*i*16));
            System.out.println(newSequence);
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
    
    //creates a set of rules to be applied to the song in changeSong. Right now just handmade rules.
    public static ArrayList<String> makeRules(ArrayList<String> patterns) {
        ArrayList<String> rules = new ArrayList();
        rules.add("I.I...I.I...I.I. .II....II....I.I");
        rules.add("..I.I..II.I...I. .IIIIII.........");
        return rules;
    }
    
    public static void main(String[] args) throws Exception {
        
        //Melody processing
        String pattern;
        ArrayList<float[]> notesrests = new ArrayList();
        //ArrayList<float[]> notes = MelismaReader.readFile("sonata01-1.notes");
        ArrayList<float[]> notes = readMidi(MidiSystem.getSequence(new File("Hello.mid")));
        notes = gcds(notes);
        
        //GCD = 60 ;
        //resolution = 240;
        //System.out.println(MidiSystem.getSequence(new File("sample.mid")));
        notesrests = silences(notes);
        pattern = rhythIO(notesrests);
        ArrayList<String> patterns = getPatterns(pattern);
        ArrayList<String> rules = makeRules(patterns);
        //notes = changeSong(notes,patterns,rules);
        
        /*
        ArrayList<float[]> chordList = new ArrayList();
        chordList = ChordAnalyzer.chordNotes(chordList, "sonata01-1_tsroot.txt");
        ArrayList<float[]> chordsWrite = new ArrayList();

        float ts = 4/4 - (float)0.001;
        float speed = 1000;
        chordsWrite = chordMaker.chordMake(chordList, ts, speed);
        chordsWrite.addAll(notes);*/
        
        write(notes);
        
        System.out.println(MeasureAnalyzer.getOverallSimilarity(notes,0,3,GCD));
        
    }
}