package midireader.Temperley;

import static midireader.Temperley.Globals.*;
import static midireader.Temperley.Polyph.*;

class Timepoint {

	int time;
	int score[] = new int[10];
	int bestp[] = new int[10];
	int bests;
};

public class Segment {

	public static void reset()
	{
		timepoint = new Timepoint[100000];
	}

	static Timepoint timepoint[] = new Timepoint[100000];
	static int num_timepoints;

	static int myround(int x) {

		int pip;
		/* returns x rounded to the nearest piplength; if halfway between, round down */
		pip = x / piplength;
		if (x - (pip * piplength) <= piplength / 2) {
			return pip * piplength;
		} else {
			return (pip + 1) * piplength;
		}

	}

        @SuppressWarnings("empty-statement")
	static void adjust_notes() {

		/*
		Creates the note array, with notes in chronological order. Then makes the following adjustments to the notes:

		1. Shift all ontimes and offtimes to start at time zero
		2. Shift together ontimes <75 msec apart
		3. If note z2 ends <100 msec after note z's start, shift it to note z's start
		4. Round off all note ons and offs to the nearest pip
		5. If a note has length < one pip, make it one pip
		6. Find any duplicate note pairs (with the same ontime and pitch); mark one as invalid
		 */

		int z, n, earliest, z2, mass, numn;
		double avg;

		/* Create note array - notes in chronological order */

		for (z = 0; z < numnotes; z++) {
			znote[z].done = 0;
		}

		n = 0;
		while (true) {
			earliest = 1000000;
			for (z = 0; z < numnotes; z++) {
				if (znote[z].ontime < earliest && znote[z].done == 0) {
					earliest = znote[z].ontime;
				}
			}

			if (earliest == 1000000) {
				break;
			}

			for (z = 0; z < numnotes; z++) {
				if (znote[z].ontime == earliest) {
					if (note[n] == null) note[n] = new note_struct();
					note[n].ontime = znote[z].ontime;
					note[n].offtime = znote[z].offtime;
					note[n].pitch = znote[z].pitch;
					znote[z].done = 1;
					n++;
				}
			}
		}

		/* Now adjust all the notes to start at time 0 */

		earliest = 1000000;
		for (z = 0; z < numnotes; z++) {
			if (note[z].ontime < earliest) {
				earliest = note[z].ontime;
			}
		}

		//printf("offset = %d\n", earliest);
		if (earliest != 0) {
			for (z = 0; z < numnotes; z++) {
				note[z].ontime -= earliest;
				note[z].offtime -= earliest;
			}
		}

		/* Go through notes; when you find an undone note, look for all notes starting <75 msec after it, shift all of those ontimes to their average; those
		notes are now done. Then go on to the next undone note, repeat the process. */

		for (z = 0; z < numnotes; z++) {
			note[z].done = 0;
		}

		while (true) {
			earliest = 1000000;
			for (z = 0; z < numnotes; z++) {
				if (note[z].ontime < earliest && note[z].done == 0) {
					earliest = note[z].ontime;
				}
			}
			if (earliest == 1000000) {
				break;
			}

			for (z = 0; z < numnotes; z++) {
				if (note[z].ontime == earliest) {
					note[z].done = 1;
					mass = note[z].ontime;
					numn = 1;
					for (z2 = z; z2 < numnotes; z2++) {
						if (note[z2].ontime - note[z].ontime < 75) {
							note[z2].done = 1;
							mass += note[z2].ontime;
							numn++;
						}
					}
					avg = (double) (mass) / (double) (numn);
					for (z2 = z; z2 < numnotes; z2++) {
						if (note[z2].done == 1) {
							note[z2].ontime = (int) (avg);
							note[z2].done = 2;
						}
					}
					break;
				}
			}
		}

		/* If note z2 ends less than 100 msec after note z begins, move z2's end to z's start */

		for (z = 0; z < numnotes; z++) {
			for (z2 = 0; z2 < numnotes; z2++) {
				if (z == z2) {
					continue;
				}
				if (note[z2].offtime > note[z].ontime && note[z2].offtime - note[z].ontime <= 100) {
					note[z2].offtime = note[z].ontime;
				}
			}
		}
                
                /* Copy note array to note2, which is not rounded, for beat induction purposes*/
                for (z = 0; z < numnotes; z++) {
			//System.out.println("Note "+ note[z].ontime+ " " + note[z].offtime);
                        note2[z] = new note_struct();
			note2[z].ontime = note[z].ontime;
			note2[z].offtime = note[z].offtime;
			//note2[z].copyFrom(note[z]);
		}
                
		/* Now round them off to the nearest pip */
                
		for (z = 0; z < numnotes; z++) {
			note[z].ontime = myround(note[z].ontime);
			note[z].offtime = myround(note[z].offtime);
			//printf("Note %d %d %d\n", note[z].ontime, note[z].offtime, note[z].pitch);
		}

		/* If note's length is less than one pip, make it one pip */

		for (z = 0; z < numnotes; z++) {
			if (note[z].offtime - note[z].ontime < piplength) {
				note[z].offtime = note[z].ontime + piplength;
			}
		}

		/* Find any duplicate note pairs - notes with the same ontime and pitch. Mark the shorter one as invalid (or the second one, if equal length).  */

		for (z = 0; z < numnotes; z++) {
			note[z].valid = 1;
		}
		for (z = 0; z < numnotes; z++) {
			for (z2 = z + 1; z2 < numnotes; z2++) {
				if (note[z2].ontime == note[z].ontime && note[z2].pitch == note[z].pitch) {
					if (note[z].offtime > note[z2].offtime) {
						note[z2].valid = 0;
					} else {
						note[z].valid = 0;
					}
				}
			}
		}
                System.gc();

	}

	static void make_timepoint_array() {

		int z, t, earliest;

		for (z = 0; z < numnotes; z++) {
			note[z].done = 0;
		}

		t = 0;
		while (true) {

			earliest = 1000000;
			for (z = 0; z < numnotes; z++) {
				if (note[z].done==0 && note[z].ontime < earliest) {//if (!note[z].done && note[z].ontime < earliest) {
					earliest = note[z].ontime;
				}
			}
			if (earliest == 1000000) {
				break;
			}
			if (timepoint[t] == null) 
                            timepoint[t] = new Timepoint();
			timepoint[t].time = earliest;
			t++;

			for (z = 0; z < numnotes; z++) {
				if (note[z].ontime == earliest) {
					note[z].done = 1;
				}
			}

		}

		/* t is the number of timepoints so far. Now find the last offtime in the piece, make that the last timepoint. */
		last_offtime = 0;
		for (z = 0; z < numnotes; z++) {
			if (note[z].offtime > last_offtime) {
				last_offtime = note[z].offtime;
			}
		}
                //added t>0
		if (t > 0 && last_offtime - timepoint[t - 1].time < 300) {
			if (timepoint[t] == null) timepoint[t] = new Timepoint();
			timepoint[t].time = timepoint[t - 1].time + 300;
		} else {
			if (timepoint[t] == null) timepoint[t] = new Timepoint();
			timepoint[t].time = last_offtime;
		}

		num_timepoints = t + 1;

		/*
		for(t=0; t<num_timepoints; t++) {
		printf("%d ", timepoint[t].time);
		}
		printf("\n"); */
	}

	static void create_segments() {

		int z, t, s, local_score, score, interval, prev, seglength, bests=0, best_score, bestp=0, n, asl, nsegs;
                seglength = 0;

		/* We want to find the best segmentation of the piece into short segments, from 150 to 400 ms. Every event onset must be
		a segment beginning, but longer IOI's may be divided into multiple segments. We want to choose a segment length
		that roughly corresponds to a metrical level. If IOIs are mostly say 400 or multiples thereof, we don't want to
		choose a segment length of 300, because then a note value of 700-800 msec will sometimes be divided in two and sometimes in three.
		So we do a dynamic programming search.  We go through the timepoints. For each timepoint interval, we consider possible
		segment lengths, from 3 (150 ms) through 9 (450 ms), and assign a cost for how well that segment length fits the interval; we
		also assign a cost for switching segment lengths. */

		/* First make an array of timepoints for all onsets in the piece */

		make_timepoint_array();

		for (t = 1; t < num_timepoints; t++) {

			/* Go through and find the best segment length for each timepoint interval (IOI). (A segment can be 3 to 9 pips.) */
			for (s = 3; s < 9; s++) {
				seglength = s * piplength;
				interval = timepoint[t].time - timepoint[t - 1].time;

				/* Now we calculate the cost of an imperfect segment length. */
				/* To be fancier, you could say: if the log ratio between interval and rounded-down-interval (int - (int % seg)) is greater than between
				rounded-up interval (((int - (int % seg)) + seg) and interval... */

				if (interval < seglength) {
					local_score = seglength - interval;
				} else if (interval % seglength < seglength / 2) {
					local_score = interval % seglength;
				} else {
					local_score = seglength - (interval % seglength);
				}

				if (t == 1) {
					timepoint[1].score[s] = local_score + (Math.abs(seglength - 300) / 3);
				} else {
					best_score = 10000000;
					for (prev = 3; prev < 9; prev++) {
						score = timepoint[t - 1].score[prev] + local_score;
						if (prev != s) {
							score += 500;                         /* penalty for changing segment lengths */
						}
						score += Math.abs(seglength - 300) / 3;                    /* we favor segment lengths around 300 ms */
						if (score < best_score) {
							bestp = prev;
							best_score = score;
						}
					}
					//printf("t %d, s %d: bestp = %d\n", t, s, bestp);
					timepoint[t].bestp[s] = bestp;
					timepoint[t].score[s] = best_score;
				}
			}
		}

		/* The traceback */

		best_score = 10000000;
		for (s = 3; s < 9; s++) {
			if (timepoint[num_timepoints - 1].score[s] < best_score) {
				bests = s;
				best_score = timepoint[num_timepoints - 1].score[s];
			}
		}

		t = num_timepoints - 1;
		while (t > 1) {
			timepoint[t].bests = bests;
			bests = timepoint[t].bestp[bests];
			t--;
		}

		timepoint[1].bests = bests;

		/*
		printf("First time = %d\n", timepoint[0].time);
		for(t=1; t<num_timepoints; t++) {
		printf("Timepoint %d: time %d, best s = %d\n", t, timepoint[t].time, timepoint[t].bests);
		} */

		/* Now, when creating segments for a particular time interval (t-1, t), look at timepoint[t].bests; that's the approximate
		segment length (in pips).  Then take minimum of (int - (int % seg)) and ((int + seg) - (int % seg)); that's the rounded
		interval; divide by seg to see how many segs to generate; divide the interval EVENLY into that many segs. (But then we
		round the segment boundaries to pips.)
		 */

		s = 0;
		for (t = 1; t < num_timepoints; t++) {
			asl = timepoint[t].bests * piplength;                   /* asl = approximate segment length */
			interval = timepoint[t].time - timepoint[t - 1].time;

			if (interval < asl) {
				nsegs = 1;
			} else if (interval % asl < asl / 2) {
				nsegs = (interval - (interval % asl)) / asl;
			} else {
				nsegs = ((interval + asl) - (interval % asl)) / asl;
			}

			seglength = interval / nsegs;

			//printf("Timepoint %d (%d-%d): asl = %d, numsegs = %d\n", t, timepoint[t-1].time, timepoint[t].time, asl, nsegs);
			for (n = 0; n < nsegs; n++) {
				if (segment[s] == null) segment[s] = new segment_struct();
				segment[s].start = myround(timepoint[t - 1].time + (n * seglength));
				if (n > 0) {
					segment[s - 1].end = myround(timepoint[t - 1].time + (n * seglength));
					//printf("Segment %d starts at %d and ends at %d\n", s-1, segment[s-1].start, segment[s-1].end);
				}
				s++;
			}
			segment[s - 1].end = timepoint[t].time;
			//printf("Segment %d starts at %d and ends at %d\n", s-1, segment[s-1].start, segment[s-1].end);
		}

		segtotal = s - 1;
	}
}
