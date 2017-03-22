/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.output;

import java.io.File;
import java.util.ArrayList;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import midireader.XmkMain;

/**
 *
 * @author domini
 */
public class writeMidi {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;

    public static void write(ArrayList<float[]> notes, String filename) {
        System.out.println("midifile begin ");
        try {
            //****  Create a new MIDI sequence with 24 ticks per beat  ****
            Sequence s = new Sequence(Sequence.PPQ, (int) XmkMain.resolution);
            //****  Obtain a MIDI track from the sequence  ****
            Track t = s.createTrack();
            //****  General MIDI sysex -- turn on General MIDI sound set  ****
            byte[] b = {(byte) 240, 126, 127, 9, 1, (byte) 247};
            SysexMessage sm = new SysexMessage();
            sm.setMessage(b, 6);
            MidiEvent me = new MidiEvent(sm, (long) 0);
            t.add(me);
            //****  set tempo (meta event)  ****
            MetaMessage mt = new MetaMessage();
            byte[] bt = {6, (byte) 0, 0};
            mt.setMessage(81, bt, 3);
            me = new MidiEvent(mt, (long) 0);
            t.add(me);
            //****  set track name (meta event)  ****
            mt = new MetaMessage();
            String TrackName = "midifile track";
            mt.setMessage(3, TrackName.getBytes(), TrackName.length());
            me = new MidiEvent(mt, (long) 0);
            t.add(me);
            //****  set omni on  ****
            ShortMessage mm = new ShortMessage();
            mm.setMessage(176, 125, 0);
            me = new MidiEvent(mm, (long) 0);
            t.add(me);
            //****  set poly on  ****
            mm = new ShortMessage();
            mm.setMessage(176, 127, 0);
            me = new MidiEvent(mm, (long) 0);
            t.add(me);
            //****  set instrument to Piano  ****
            mm = new ShortMessage();
            mm.setMessage(198, 15, 5);
            me = new MidiEvent(mm, (long) 0);
            t.add(me);
            
            int volume = 70;
            for (int i = 0; i < notes.size(); i++) {
                float[] note = notes.get(i);
                //****  note on  ****
                if(note[0] > 66)volume = 70+(int)(1.5*((int)note[0]-66));
                else volume = 60;
                mm = new ShortMessage();
                mm.setMessage(NOTE_ON, (int) note[0], volume);
                me = new MidiEvent(mm, (long) note[1]); //time on
                t.add(me);
                //****  note off  ****
                mm = new ShortMessage();
                mm.setMessage(NOTE_OFF, (int) note[0], 64);
                me = new MidiEvent(mm, (long) note[2]); //time off
                t.add(me);
            }
            //****  set end of track (meta event) 19 ticks later  ****
            mt = new MetaMessage();
            byte[] bet = {}; // empty array
            mt.setMessage(47, bet, 0);
            me = new MidiEvent(mt, (long) 140);
            t.add(me);
            //****  write the MIDI sequence to a MIDI file  ****
            File f = new File(filename);
            MidiSystem.write(s, 1, f);
            System.out.println("Midifile write successful: " + filename);
        } //try //try
        catch (Exception e) {
            System.out.println("Exception caught " + e.toString());
        } //catch
        System.out.println("midifile end ");
    } //main
    
}
