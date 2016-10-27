/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import midireader.inputHumdrumMelisma.readMidi;
import midireader.processingHumdrumMelisma.chordMaker;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author domini
 */
public class writeNotes {
    public static void writeNotes(String filename, ArrayList<float[]> notes) throws IOException, InvalidMidiDataException{
            //PrintWriter writer = new PrintWriter("outpute/notes/" +filename + ".notes", "UTF-8");
            
            /*
List<String> lines = new ArrayList<>();
            for(int i = 0; i < notes.size(); i++){
                String r = "note " + (int)notes.get(i)[1] + " " + (int)notes.get(i)[2] + " " + (int)notes.get(i)[0];
                lines.add(r);
            }
            Path file = Paths.get("outpute/notes/" +filename + ".notes", "UTF-8");
            Files.write(file, lines);*/
            
            String content = "";
            for(int i = 0; i < notes.size(); i++){
                content = content + "Note " + (int)notes.get(i)[1] + " " + (int)notes.get(i)[2] + " " + (int)notes.get(i)[0] + "\n";
            }
            try {

			File file = new File("output/notes/" +filename + ".notes");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
