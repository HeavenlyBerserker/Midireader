/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader;

/**
 *
 * @author Bilbo
 */
import java.io.File;
import java.util.ArrayList;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MidiReader {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    public static ArrayList<float[]> readMidi(Sequence sequence) {
        
        ArrayList<float[]> notes = new ArrayList();
        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
            //System.out.println("Track " + trackNumber + ": size = " + track.size());
            //System.out.println();
            for (int i=0; i < track.size(); i++) { 
                MidiEvent event = track.get(i);
                //System.out.print("@" + event.getTick() + " ");
                float time = event.getTick();
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    //System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        float notey[] = {(float)key,time,0}; //note, starttime, duration
                        notes.add(notey);
                        //System.out.println(notey[0] + " " + notey[1]);
                        //int octave = (key / 12)-1;
                        //int note = key % 12;
                        //String noteName = NOTE_NAMES[note];
                        //int velocity = sm.getData2();
                        //System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int found = 0;
                        for (int j=notes.size()-1; j>=0; j--) {
                            if (found == 0 && notes.get(j)[0] == (float)key) {
                                float notey[] = notes.get(j);
                                notey[2] = time-notes.get(j)[1];
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
    public static ArrayList<float[]> syncopate(ArrayList<float[]> notelist) {
        return notelist;
    }
    
    public static ArrayList<float[]> changeRhythm(ArrayList<float[]> notelist, ArrayList<float[]> rhythmlist) {
        return notelist;
    }
    
    public static void main(String[] args) throws Exception {
        ArrayList<float[]> notes = readMidi(MidiSystem.getSequence(new File("sample.mid")));
        ArrayList<float[]> rhythm = syncopate(notes);
        ArrayList<float[]> synco = changeRhythm(notes,rhythm);
    }
}