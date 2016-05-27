package midireader;

/**
 *
 * @author Bilbo
 */
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
    
    
    public static void write(ArrayList<float[]> notes) {
    System.out.println("midifile begin ");
	try
	{
//****  Create a new MIDI sequence with 24 ticks per beat  ****
		Sequence s = new Sequence(javax.sound.midi.Sequence.PPQ,24);

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
        byte[] bt = {0x02, (byte)0x00, 0x00};
		mt.setMessage(0x51 ,bt, 3);
		me = new MidiEvent(mt,(long)0);
		t.add(me);
//****  set track name (meta event)  ****
		mt = new MetaMessage();
		String TrackName = new String("midifile track");
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
		mm.setMessage(0xC0, 0x00, 0x00);
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
	} //try
		catch(Exception e)
	{
		System.out.println("Exception caught " + e.toString());
	} //catch
    System.out.println("midifile end ");
} //main
    
    
    public static ArrayList<float[]> readMidi(Sequence sequence) {
        
        ArrayList<float[]> notes = new ArrayList();
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
                                System.out.println(notes.get(j)[0] + " " + notes.get(j)[1] + " " + notes.get(j)[2]);
                                found = 1;
                            }
                        }
                    } else {
                        //System.out.println("Command:" + sm.getCommand());
                    }
                } else {
                    //System.out.println("Other message: " + message.getClass());
                }
            }
            //System.out.println();
        }
    return notes;
    }
    
    //returns some sort of rhythm structure
    public static ArrayList<float[]> syncopate(ArrayList<float[]> notelist) {
        return notelist;
    }
    
    //applies rhythm structure to notelist and returns modified notelist
    public static ArrayList<float[]> changeRhythm(ArrayList<float[]> notelist, ArrayList<float[]> rhythmlist) {
        return notelist;
    }
    
    public static void main(String[] args) throws Exception {
        
        
        ArrayList<float[]> notes = readMidi(MidiSystem.getSequence(new File("sample.mid")));
        write(notes);
        //ArrayList<float[]> rhythm = syncopate(notes);
        //ArrayList<float[]> synco = changeRhythm(notes,rhythm);
    }
}