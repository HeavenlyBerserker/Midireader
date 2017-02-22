package midireader.Temperley;

import midireader.Temperley.Globals.*;
import midireader.Temperley.Polyph.*;

import static midireader.Temperley.Metharm.*;
import static midireader.Temperley.Segment.*;
import static midireader.Temperley.Streamer.*;

import java.io.*;
import java.util.Scanner;
import static midireader.Temperley.Globals.piplength;
import static midireader.Temperley.Polyph.globseglength;
import static midireader.Temperley.Polyph.segtotal;

public class ProbMelisma {

	public static void analyzeRag(String input_file, int[] temp) {
		int n = 0, j;
		Snote sn;
		Stream s;

		String parameter_file = input_file; //char *parameter_file = NULL, *input_file = NULL;
		BufferedReader in_file;//FILE *in_file;
		String line;// char line[] = new char[1000];
		String noteword; //char noteword[] = new char[10];
		char junk[] = new char[10];

		int param_file_specified = 0;
                Globals.reset();
                Polyph.reset2();
                Segment.reset();

		Globals.verbosity = 0;   /* set verbosity */ //i added globals.
                /*
		for (j = 0; j < argv.length; j++) {
                    if (argv[j].equals("-p")) {
                            parameter_file = argv[j + 1];
                            param_file_specified = 1;
                            j++;
                    } else if (argv[j].equals("-v")) {
                            j++;
                            Globals.verbosity = Integer.parseInt(argv[j]); //sscanf(argv[j], "%d", &verbosity); //added globals.
                    } else if (input_file == null) {
                            /* assume it's a file 
                            input_file = argv[j];
                            System.out.print("egg");
                    }
		}*/

		//read_parameter_file (parameter_file, param_file_specified);

		try {

			if (input_file != null) {
				in_file = new BufferedReader(new FileReader(input_file));  //fopen(input_file, "r");


			} else {
				in_file = new BufferedReader(new InputStreamReader(System.in));
			}

			if (Globals.tactus_min < 6) { //added globals.
				System.out.printf("Error: tactus_min must be at least 6\n");
				System.exit(1);
			}

                        Polyph.znote[n] = new note_struct(); //phantom note
                        Polyph.znote[n].ontime = 0;
                        Polyph.znote[n].offtime = 1;
                        Polyph.znote[n].pitch = 24;
                        
			n = 1; //n=0 for no phantom note
			while ((line = in_file.readLine()) != null) {  //while (fgets(line, sizeof(line), in_file) !=NULL) {            /* read in Notes */
				if (line.charAt(0) == '\n' || line.charAt(0) == '%') {
					continue;
				}

				//System.out.println(line);

				Scanner scanner = new Scanner(line);

				if (!scanner.hasNext()) {
					continue;
				}

				noteword = scanner.next();  //)sscanf(line, "%s", noteword);
				if (!noteword.equals("Note")) {
					//System.out.println("Bad line found in input: " + line);
					continue;
				}

				Polyph.znote[n] = new note_struct(); //added polyph. and below

				Polyph.znote[n].ontime = scanner.nextInt();
				Polyph.znote[n].offtime = scanner.nextInt();
				Polyph.znote[n].pitch = scanner.nextInt();

				/*if(sscanf (line, "%s %d %d %d %s", noteword, &znote[n].ontime, &znote[n].offtime, &znote[n].pitch, junk) != 4) {
				//printf("Bad line found in input: %s", line);
				continue;
				}
				sscanf (line, "%s %d %d %d", noteword, &znote[n].ontime, &znote[n].offtime, &znote[n].pitch);
				//printf("Note %d %d %d\n", znote[n].ontime, znote[n].offtime, znote[n].pitch);*/
				n++;
			}


		} catch (FileNotFoundException e) {
                    
			System.out.printf("I can't open that file\n");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Error reading file");
			System.exit(1);
		}


		Polyph.numnotes = n; //added Polyph.

		if (Globals.verbosity > 0) {
			System.out.printf("Creating segments and streams...\n");
		}

		adjust_notes();

		create_segments();

		create_streams();
                //System.out.println(globseglength);
                if (segtotal <= 0) //If there's a song with no notes
                    return;
                //System.out.println(segtotal);
                //display_streams();

		/*
		printf("Notes as output by streamer:\n");
		for(n=0; n<numnotes; n++) {
		printf("Note %d %d %d %d\n", note[n].ontime, note[n].offtime, note[n].pitch, note[n].stream);
		}  */

		create_streamlists();

		//print_streams(Polyph.streamlist, -1, -1, 1);
                
                MonophonicStreams.selectStreams(temp);
                
		
                // Delete the following '/*' to see the full output with X's. Takes ~30 more seconds/song. 
                
                // For reference: We needed this in case the song had a pickup so we could start our 16 pip selections at the true start time with 
                // rests before the first onset.
                
                // (There's 2 of the max level beats in each of our 16 pip selections, so we can't assume a pickup is in the first or second half)
                
                // I can't find any examples of songs with short pickups so I'm not sure how it assigns the beginning anyway. It might handle them
                // for us. 
                
                /*
                
>>>>>>> 16f9aaa7a4d260a98cb5d6fab28a946101de6f34
                
		make_profiles();

		create_pipstreams();
                
		if (Globals.verbosity >= 0) {
			System.out.printf("Creating subspans...\n");
		}

		create_subspans();
                
		
		if (Globals.verbosity >= 0) {
			System.out.printf("Creating tactus_spans...\n");
		}


		create_tactus_spans();
                
		if (Globals.verbosity >= 0) {
			System.out.printf("Pruning tactus_spans...\n");
		}


		prune_tactus_spans();

		if (Globals.verbosity >= 0) {
			System.out.printf("Doing tactus search...\n");
		}

		make_tables();

		traceback();

		if (Globals.verbosity >= 0) {
			System.out.printf("Finishing up...\n");
		}

		find_upper_level();

		final_meter();

                graphic_display();
                /*
		stream_probs();
9
		if (Globals.verbosity > 0) {
			graphic_display();
		}

		if (Globals.verbosity == -1) {
			meter_stats();
		}
		if (Globals.verbosity == -2) {
			print_chords();
		}
                */
	}
}
