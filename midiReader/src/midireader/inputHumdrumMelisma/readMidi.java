/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.inputHumdrumMelisma;

import java.util.ArrayList;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import midireader.XmkMain;

/**
 *
 * @author domini
 */
public class readMidi {

    /*Function: Read Midi---------------------------------------------------------
    Reads a midi file and returns an list of values about the notes
    Input: Midi sequence
    Output: List of notes (note, onset, offset)
     */
    public static ArrayList<float[]> readMidi(Sequence sequence) {
        ArrayList<float[]> notes = new ArrayList();
        XmkMain.resolution = sequence.getResolution();
        XmkMain.ppq = sequence.PPQ;
        System.out.println("Resolution = " + XmkMain.resolution);
        int trackNumber = 0;
        for (Track track : sequence.getTracks()) {
            trackNumber++;
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                float time = event.getTick();
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == XmkMain.NOTE_ON) {
                        int key = sm.getData1();
                        float[] notey = {(float) key, time, 0}; //note, starttime, stoptime
                        notes.add(notey);
                        //int octave = (key / 12)-1;
                        //int note = key % 12;
                        //int velocity = sm.getData2();
                    } else if (sm.getCommand() == XmkMain.NOTE_OFF) {
                        int key = sm.getData1();
                        int found = 0;
                        for (int j = notes.size() - 1; j >= 0; j--) {
                            if (found == 0 && notes.get(j)[0] == (float) key) {
                                float[] notey = notes.get(j);
                                notey[2] = time; //-notes.get(j)[1];
                                notes.set(j, notey);
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
    
}
