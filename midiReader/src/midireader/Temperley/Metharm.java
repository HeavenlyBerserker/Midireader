package midireader.Temperley;

import static midireader.Temperley.Globals.*;
import static midireader.Temperley.Polyph.*;

import static java.lang.Math.abs;
import static java.lang.Math.log;

public final class Metharm {

	static void add_to_stream(Snote sn) {

		Stream s;
		Stream s2;
		Snote sn2;
		int set;

		s = streamlist;
		set = 0;

		while (true) {
			if (s == null) {
				break;
			}
			if (s.index == sn.stream) {
				sn2 = s.sn;
				s.sn = sn;
				sn.next = sn2;
				sn.prev = sn2.prev;
				sn2.prev = sn;
				set = 1;
				break;
			} else {
				s = s.next;
			}
		}

		if (set == 0) {
			s = new Stream();//s = malloc(sizeof(struct Stream));
			s.sn = sn;
			sn.next = null;
			sn.prev = null;
			s.index = sn.stream;
			s.next = streamlist;
			//System.out.printf("Creating stream with index %d for Note [%d] %d %d %d\n", s.index, sn.ontime, sn.offtime, sn.pitch);
			streamlist = s;
		}
	}

	static void create_snote(int n) {

		/* Create notes; offset each ontime and offtime by (tactus_max-1) * piplength */
		Snote sn;
		sn = new Snote();//sn = malloc(sizeof(struct Snote));
		sn.pitch = note[n].pitch;
		sn.ontime = note[n].ontime + ((tactus_max - 1) * piplength);
		sn.offtime = note[n].offtime + ((tactus_max - 1) * piplength);
		sn.stream = note[n].stream;

		//if(sn.ontime == 17400) sn.stream = 3;

		//System.out.printf("Note [%d] %d %d %d %d\n", n, note[n].pitch, note[n].ontime, note[n].offtime, note[n].stream);
		add_to_stream(sn);
	}

	static void create_streamlists() {

		/* We've created a linked list of streams. Now we put the notes into the streams. */

		int n;
		Stream s;
		Snote sn;

		streamlist = null;

		for (n = numnotes - 1; n >= 0; n--) {
			if ((note[n].valid == 0)) {
				continue;//if(!(note[n].valid)) continue;
			}
			create_snote(n);
		}

		s = streamlist;

		//print_streams(s, 0, -1, 1);

		/* Set the start and end of each stream, also the "prev" */
		while (s != null) {
			sn = s.sn;
			s.start = 10000000; //s.start = 100000; 
			s.end = -1;
			while (sn != null) {
				if (sn.ontime < s.start) {
					s.start = sn.ontime;
				}
				if (sn.ontime > s.end) {
					s.end = sn.ontime;
				}
				sn = sn.next;
			}
			s = s.next;
		}

	}


	static void print_streams( Stream  s, int starttime, int endtime, int printmode) {

    /* Print stream list, including all notes with ontimes from
       starttime to endtime inclusive; if endtime is -1, assume no end
       limit. If printmode == 0, all streams on one line; if 1, one stream per line */

     Snote  sn;

    while(true) {
	if(s==null) break;
	System.out.printf("Stream %d: ", s.index);
	sn = s.sn;
	while(true) {
	    if(sn==null) break;
	    if(sn.ontime < starttime) continue;
	    if(endtime != -1 && sn.ontime > endtime) break;
	    System.out.printf("[%d %d %d] ", sn.ontime, sn.offtime, sn.pitch);
	    sn=sn.next;
	}
	if(printmode==0) System.out.printf("; ");
	else System.out.printf("\n");
	s = s.next;
    }
    System.out.printf("\n");
}

static void make_profiles() {

     Stream  s;
     Snote  sn;

    int prev_p, h, i, j;
    double mass;
    double raw_tactus_profile[] = new double[40];
    double taper_factor[] = new double[18];
                         /*   7      8    9    10   11   12   13   14   15    16   17    18   19    20   21   22   23   24 */
    //double taper_factor[18] = {0.001, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.95, 0.9, 0.85, 0.8, 0.75, 0.7, 0.6, 0.4, 0.1, 0.001};

    /* Make proximity profile */

    for(i=0; i<100; i++) {
	proximity_profile[i] = (Math.exp( -Math.pow( ((double)(i)-50.0), 2.0) / (2.0 * prox_var))) / (2.51 * Math.sqrt(prox_var));
	// Use this line to make a flat proximity profile
	//proximity_profile[i] = 0.1;
    }

    s = streamlist;

    while(true) {
	if(s==null) break;
	sn = s.sn;
	prev_p = -1;
	while(true) {
	    if(sn==null) break;
	    for(h=0; h<12; h++) {
		make_adjusted_profile(prev_p, h, sn);
	    }
	    prev_p = sn.pitch;
	    sn=sn.next;
	}
	s = s.next;
    }

    /* make tactus_reg_profile */

    for(i=tactus_min; i<=tactus_max; i++) {
	taper_factor[i-7] = (5.0 * Math.abs((Math.log( (double)(i)/(double)(tactus_min) )))) + .001;
	if(taper_factor[i-7] > 1.0) taper_factor[i-7] = 1.0;
	taper_factor[i-7] *= (5.0 * Math.abs((Math.log( (double)(i)/(double)(tactus_max) )))) + .001;
	if(taper_factor[i-7] > 1.0) taper_factor[i-7] = 1.0;
    }

    for(j=tactus_min; j<=tactus_max; j++) {
	mass = 0.0;
	for(i=tactus_min; i<=tactus_max; i++) {
	    if(Math.abs(i-j) < 4) raw_tactus_profile[i] = tactus_reg_score[ Math.abs(i-j) ];
	    else raw_tactus_profile[i] = tactus_reg_score[4];
	    raw_tactus_profile[i] *= taper_factor[i-7];
	    mass += raw_tactus_profile[i];
	}

	//System.out.printf("j=%2d: ", j);
	for(i=tactus_min; i<=tactus_max; i++) {
	    tactus_profile[j][i] = raw_tactus_profile[i] / mass;
	    //System.out.printf("%5.3f ", tactus_profile[j][i]);
	}
	//System.out.printf("\n");

    }
}


static void make_adjusted_profile(int prev_p, int h,  Snote  sn) {

    /* Make an adjusted pitch profile, indicating the P for each pitch given the root h and previous pitch prev_p; this is the product
       of the harmony profile and proximity profile, normalized to sum to 1. For the first note, simply use the harmony profile.
       (We could have a "range profile" at least for the first note, rather than an even distribution across all 100 pitches...) */

    int i, p, range_position, start_position, j, anch, hr;
    double raw_profile[] = new double[100];
    double mass;

    if(sn.pitch < 0 || sn.pitch >= 100) {
	System.out.printf("Pitch out of range\n");
	System.exit(1);
    }

    mass = 0.0;
    for(p=0; p<100; p++) {
	if(prev_p == -1) raw_profile[p] = harmony_profile[((p+24)-h)%12];
	else {
	    i = p-prev_p;
	    if(i < -50) i = -50;
	    if(i > 49) i = 49;
	    raw_profile[p] = harmony_profile[((p+24)-h)%12] * proximity_profile[i+50];
	}
	mass += raw_profile[p];
    }
    /* Now normalize the profile values to sum to 1 */
    for(p=0; p<100; p++) {
	adjusted_profile[p] = raw_profile[p] / mass;
    }

    sn.hp_score[h] = adjusted_profile[sn.pitch];

    hr = ((sn.pitch+24)-h) % 12;
    if(sn.next == null) {
	sn.hp_score[h] *= anch_penalty[hr];
    }
    else {
	i = Math.abs(sn.pitch - sn.next.pitch);
	if(i > 2) {
	    sn.hp_score[h] *= anch_penalty[hr];
	}
    }

    //if(sn.ontime == 17750) System.out.printf("Pitch prob: pitch = %d, prev pitch = %d, root = %d, prox_profile = %5.3f, harm_profile = %5.3f, raw = %5.3f, adj = %5.3f\n", sn.pitch, prev_p, h, proximity_profile[(sn.pitch-prev_p)+50], harmony_profile[((sn.pitch+24)-h)%12], raw_profile[sn.pitch], sn.hp_score[h]);

    /*
    System.out.printf("Pitch:      ");
    for(p=60; p<=72; p++) System.out.printf("%6d ", p);
    System.out.printf("\nharmony profile:");
    for(p=60; p<=72; p++) {
    System.out.printf("%6.3f ", harmony_profile[((p+24)-k)%12]);

    System.out.printf("\nProximity:  ");
    for(p=60; p<=72; p++) System.out.printf("%6.3f ", proximity_profile[(p-prev_p) + 50]);
    System.out.printf("\nRaw:        ");
    for(p=60; p<=72; p++) System.out.printf("%6.3f ", raw_profile[p]); */

    /*
    System.out.printf("Prev_pitch=%d, root=%d: Adjusted profile: ", prev_p, h);
    for(p=60; p<=72; p++) System.out.printf("%6.3f ", adjusted_profile[p]);
    System.out.printf("\n"); */

}


static void create_pipstreams() {

     Stream  s;
     Stream  ps;
     Snote sn;
    int p;

    /* Find the last timepoint in the piece */
    last_offtime=0;
    s = streamlist;
    while(s!=null) {
	sn = s.sn;
	while(sn!=null) {
	    if(sn.offtime > last_offtime) last_offtime = sn.offtime;
	    sn=sn.next;
	}
	s=s.next;
    }

    /* Create pip array. The number of the last pip in the piece is (last offtime pip + (tactus max - 1). So we need one more pip than that. */
    /* We assume last_offtime has already been quantized to a pip (in segment.c) */

    num_pips = (last_offtime / piplength) + (tactus_max - 1) + 1;
    //printf("last offtime = %d; number of pips = %d\n", last_offtime, num_pips);
    pip = new Pip[num_pips];//pip = malloc(num_pips * sizeof(struct Pip));

    /* For each pip p, create a "pip streamlist" - a list of all streams containing only notes on or after p */

    for(p=0; p<num_pips; p++) {
			if (pip[p]==null) pip[p] = new Pip();
	pip[p].stream = null;
	pip[p].hasnote = 0;
	s=streamlist;
	while(s!=null) {
	    //printf("Doing stream %d\n", s.index);
	    sn = s.sn;
	    while(sn!=null) {
		/* Find the earliest note that starts on or after the pip time */
		if(sn.ontime >= p * piplength) break;
		sn=sn.next;
	    }
	    if(sn==null) {  /* The stream contains no notes on or after p; don't add this stream to the pip streamlist. */
		s=s.next;
		continue;
	    }
	    ps = new Stream();//ps = malloc(sizeof(struct Stream));
	    ps.sn = sn;
	    if(sn.ontime == p * piplength) pip[p].hasnote = 1; /* if the first note of the stream starts right at p, hasnote = 1 */
	    if(pip[p].stream != null) ps.next = pip[p].stream;
	    else ps.next = null;
	    ps.index = s.index;
	    ps.cs = s;
	    pip[p].stream = ps;

	    s=s.next;
	}
	//printf("%d: %d; ", p, pip[p].hasnote);
    }

    /* For each pip, print the first note of each pipstream */
    /*
    for(p=0; p<num_pips; p++) {
	printf("Pip %d (time = %d): ", p, p*piplength);
	ps = pip[p].stream;
	while(ps!=null) {
	    printf("S%d: N(%d %d); ", ps.index, ps.sn.pitch, ps.sn.ontime);
	    ps=ps.next;
	}
	printf("\n");
	} */
}


static void create_subspans() {

     Stream  ps;
     Snote  sn;
    int a, b, c, span_notes, beatnotes, bestc, numstreams, best_root, h, diff;
    double score, best_score;

    /* Allocate a 2-D array of subtactus intervals */

    subt = new Subtactus[num_pips][];//subt = malloc(num_pips * sizeof(struct Subtactus *));
    for(a = 0; a < num_pips; a++) {
	subt[a] = new Subtactus[tactus_max];//subt[a] = malloc((tactus_max) * sizeof(struct Subtactus));
    }

    for(a=0; a<num_pips; a++) {

	for(b=2; b<tactus_max-1; b++) {

	    if(a+b >= num_pips) continue;

			if (subt[a][b] == null) subt[a][b] = new Subtactus();
	    subt[a][b].valid = 1;

	    //printf("Interval %d-%d (%d-%d): ", a, (a+b), a*piplength, (a+b)*piplength);
	    //print_streams(ps, 0, ((a+b)-1)*piplength, 0);

	    bestc = -1;
	    best_score = -1000000.0;

	    /* Count notes within the span (total across all streams); also count active streams in span; an active stream is one with a note
	       beginning within the span. Also set t1 and t2 for each stream, indicating note status of initial and final pip in span. Note
	       that t1 is set to 0 even if the stream is not in existence at the initial pip. */

	    /* To find all PRESENT streams in a span, we must check if end > a and start < a+b. (Some streams in a's list may not be present in span.)
	       To find all events in a span, we can look at all streams in a's list and find events before a+b. */

	    span_notes=0;
	    numstreams=0;

	    ps = streamlist;
	    while(ps!=null) {
		ps.t1 = ps.t2 = 0;
		if(ps.start < (a+b)*piplength && ps.end > a*piplength) numstreams++;
		ps = ps.next;
	    }

	    ps = pip[a].stream;

	    while(ps!=null) {

		sn = ps.sn;

		if(sn.ontime == a * piplength) {
		    ps.cs.t1 = 1;
		    sn = sn.next;
		}

		while(sn!=null) {
		    if(sn.ontime >= (a+b) * piplength) {
			if(sn.ontime == (a+b) * piplength) ps.cs.t2 = 1;
			break;
		    }
		    //if(a==13 && b==4) printf("Found note in stream %d, at ontime %d\n", ps.index, sn.ontime);
		    span_notes++;
		    sn = sn.next;
		}
		ps = ps.next;
	    }

	    /* At this point you could say, e.g.: If span_notes > 0, and for all streams, ps.cs.t1 and t2 = 0, then forget this span and continue */

	    /* Now consider all positions for the L0 beat */

	    for(c=1; c<b; c++) {

		/* Regularity score */
		//diff = (int)(fabs(((double)(b) / 2.0) - (double)(c)));
		diff = Math.abs(c - (b / 2));
		if(diff<=2) score = Math.log(lower_reg_score[diff]);      /* initialize "score" here */
		else continue;                                       /* if abs((b/2)-c) > 2, skip over this possible c (P=0) */

		beatnotes=0;
		ps = pip[a+1].stream;

		/* Score for notes on L0 beat */
		while(ps!=null) {
		    if(ps.cs.start >= (a+b)*piplength) {
			ps=ps.next;
			continue;
		    }
		    sn = ps.sn;
		    /* For each stream in (a+1)'s pip_streamlist, see if there's a note starting at c */
		    //if(a==8 & b==9) printf("stream %d: t1 = %d, t2 = %d; ps.sn.ontime = %d\n", ps.index, ps.cs.t1, ps.cs.t2, ps.sn.ontime);
		    while(sn != null) {
			if(sn.ontime > (a+c) * piplength) {
			    score += Math.log(1.0 - L0_anchor_score[ps.cs.t1][ps.cs.t2]);
			    break;
			}
			else if(sn.ontime == (a+c) * piplength) {
			    score += Math.log(L0_anchor_score[ps.cs.t1][ps.cs.t2]);
			    beatnotes++;
			    break;
			}
			sn = sn.next;
		    }
		    ps = ps.next;
		}

		/* Score for notes not on beats. */
		score += Math.log(nonbeat_note_score) * (span_notes - beatnotes);
		/* Score for nonbeat pips with no notes: number of these = (num streams * num nonbeat pips) - num nonbeat notes */
		score += Math.log(1.0 - nonbeat_note_score) * ((numstreams * (b-1)) - (span_notes - beatnotes));

		//printf("  c=%d: beatnotes = %d; nonbeatnotes = %d\n", c, beatnotes, span_notes - beatnotes);

		if(score > best_score) {
		    bestc = c;
		    best_score = score;
		}
	    }
	    //printf("L1 span a=%d, b=%d: %d span notes, %d beat notes; bestc = %d with score = %.20f\n", a, b, span_notes, beatnotes, bestc, best_score);

	    /* If bestc = 1, then no valid c was found (either because all analyses had P = 0, or because the span was so short that there were no possible c's).
	       In that case, declare the subspan invalid. */

	    if(bestc == -1) subt[a][b].valid = 0;
	    else {
		subt[a][b].bestc = bestc;
		subt[a][b].rscore = best_score;
	    }

	    /* Calculate pitch scores */

	    best_root = -1;
	    best_score = -1000000.0;
	    for(h=0; h<12; h++) {
		score = 0.0;             /* initialize "score" here */
		ps = pip[a+1].stream;
		while(ps!=null) {
		    sn = ps.sn;
		    while(sn!=null) {
			if(sn.ontime >= (a+b) * piplength) break;
			score += Math.log(sn.hp_score[h]);
			sn=sn.next;
		    }
		    ps=ps.next;
		}

		subt[a][b].pscore[h] = score;

		/*
		printf("Root = %d: score = %.6f; ", h, score);
		if(score > best_score) {
		    best_root = h;
		    best_score = score;
		    } */
	    }
	    //printf("best root = %d\n", best_root);
	}
    }
}

static double cont_score(int a, int b, int h) {

    /* For every note that starts within (a, a+b): the P of continuing to the end of the span (or to the next onset, whichever comes first) is 1.
       After that, for each tactus span it continues into, there's a score of cont. When it stops and doesn't enter another span, there's a score
       of 1.0-cont. We factor in "1.0-cont" at the interval that a span stops in, and cont if it continues into the next span. If a note enters the
       span from the previous span and is an unanchored NCT, we factor in an additional penalty. */

     Stream  s;
     Snote sn;
    double score;
    int rel;

    score = 0.0;
    s = streamlist;

    while(s!=null) {
	if(s.start > (a+b) * piplength) {
	    s=s.next;
	    continue;
	}

	sn = s.sn;
	while(sn!=null) {

	    if(sn.ontime < (a+b)*piplength) {     /* A note begins before the end of the span - and possibly before its beginning.
						      (These are the only notes we have to consider) */
		if(sn.offtime > a*piplength && sn.offtime <= (a+b) * piplength) {  /* It ends within the span (possibly at the end) */
		    if(sn.next == null) score += Math.log(1.0 - cont);                   /* It's the last note of the stream: factor in 1-cont */
		    else if(sn.next.ontime > (a+b) * piplength) score += Math.log(1.0 - cont);  /* The next note starts after the end of the span: factor in 1-cont */
		    /* else the next note starts within the span; in this case, factor in nothing. */
		}
		if(sn.offtime > (a+b)*piplength) {  /* The note ends after the end of the span: factor in cont */
		    score += Math.log(cont);
		}
	    }

	    if(sn.ontime < a*piplength && sn.offtime > a*piplength) {  /* A note enters the span from the previous span. */
		rel = ((sn.pitch+24) - h) % 12;
		/* If the note has an NCT relationship with the root, and is unanchored, add in an extra penalty. Really, we should add in a BONUS for such notes NOT
		   continuing into the span, as well. */
		if(!(rel == 0 || rel == 3 || rel == 4 || rel == 7)) {
		    if(sn.next == null) score += Math.log(unanch_nct_cont);
		    else if(Math.abs(sn.pitch - sn.next.pitch) > 2) score += Math.log(unanch_nct_cont);
		    else score += Math.log(nct_cont);
		}
	    }
	    if(sn.offtime > (a+b) * piplength) break;   /* We've got to a note whose offtime is after the end of the span; we're done with this stream. */
	    sn=sn.next;

	}
	s=s.next;
    }

    return score;
}

static void create_tactus_spans() {

    int a, b, c, c1, c2, h, bestc, bestc1, bestc2, c1_note, c2_note, diff, diff1, diff2;
    double score, best_score, x;
    double L1_duple_note_score[] = new double[30], L1_triple_note_score[][] = new double[30][30];
     Stream  ps;
     Snote  sn;

    tactus = new Tactus[num_pips][];//tactus = malloc(num_pips * sizeof(struct Tactus *));
    for(a = 0; a < num_pips; a++) {
	tactus[a] = new Tactus[tactus_max+1];//tactus[a] = malloc((tactus_max+1) * sizeof(struct Tactus));
    }
    System.out.println(num_pips);
    for(a=0; a<num_pips; a++) {
        //System.out.println(a);

	for(b=tactus_min; b<=tactus_max; b++) {

	    if(a+b >= num_pips) continue;

			if (tactus[a][b]==null) tactus[a][b] = new Tactus();
	    tactus[a][b].valid=1;

	    ps = streamlist;
	    while(ps!=null) {
		ps.t1 = ps.t2 = 0;
		ps = ps.next;
	    }
	    ps = pip[a].stream;
	    while(ps!=null) {
		if(ps.sn.ontime == a * piplength) ps.cs.t1 = 1;
		ps = ps.next;
	    }
	    ps = pip[a+b].stream;
	    while(ps!=null) {
		if(ps.sn.ontime == (a+b) * piplength) ps.cs.t2 = 1;
		ps = ps.next;
	    }

	    for(h=0; h<12; h++) {

		bestc = -1;
		best_score = -1000000.0;

		/* Find best duple division */

		for(c=2; c<b-1; c++) {

		    if(subt[a][c].valid==0) continue;

		    /* Regularity score */
		    //diff = (int)(fabs(((double)(b) / 2.0) - (double)(c)));
		    diff = Math.abs(c - (b / 2));
		    if(diff<= 2) score = Math.log(lower_reg_score[diff]);              /* initialize "score" here */

		    else continue;

		    /* Factor in the rscores and pscores for the two L1 spans */
		    score += subt[a][c].pscore[h] + subt[a][c].rscore + subt[a+c][b-c].pscore[h] + subt[a+c][b-c].rscore;

		    /* For streams that end at a+c, we're not adding in any note scores for the (a+c, a+b) span; we really should. Similarly, for
		     streams that begin at c, the subspan (a, a+c) is not counted. */

		    //printf("Tactus int %d-%d, h=%d, c=%d: %.6f %.6f %.6f %.6f\n", a, a+b, h, c, subt[a][c].pscore[h], subt[a][c].rscore, subt[a+c][b-c].pscore[h], subt[a+c][b-c].rscore);

		    /* Factor in the note scores for the notes on the L1 beat */
		    L1_duple_note_score[c] = 0.0;
		    ps = pip[a+c].stream;
		    while(ps!=null) {
			if(ps.cs.start >= (a+b) * piplength) {
			    ps = ps.next;
			    continue;
			}
			if(ps.sn.ontime == (a+c) * piplength) {
			    L1_duple_note_score[c] += Math.log(L1_duple_anchor_score[ps.cs.t1][ps.cs.t2]);
			    L1_duple_note_score[c] += Math.log(ps.sn.hp_score[h]);
			}
			else L1_duple_note_score[c] += Math.log(1.0 - L1_duple_anchor_score[ps.cs.t1][ps.cs.t2]);
			//printf("%.20f %.20f\n", L1_duple_anchor_score[ps.cs.t1][ps.cs.t2], ps.sn.hp_score[h]);
			ps = ps.next;
		    }
		    score += L1_duple_note_score[c];

		    if(score > best_score) {
			bestc = c;
			best_score = score;
		    }
		}

		//printf("Tactus int %d-%d, h = %d, best c = %d, best duple score = %.3f\n", a, a+b, h, bestc, best_score);
		/*
		if(a==7 && b==10 && h==0) {
		    printf("Tactus int %d-%d, h = %d, duple: best c = %d, best score = %.80f\n", a, a+b, h, bestc, best_score);
		    printf("  L1_note_score = %.10f\n", L1_duple_note_score[bestc]);
		    printf("  1st L1 span (%d-%d): bestc = %d, rs = %.6f, ps = %.6f\n  2nd L1 span (%d-%d): bestc = %d, rs = %.20f, ps = %.6f\n", a, a+bestc, subt[a][bestc].bestc, subt[a][bestc].rscore, subt[a][bestc].pscore[h], a+bestc, a+b, subt[a+bestc][b-bestc].bestc, subt[a+bestc][b-bestc].rscore, subt[a+bestc][b-bestc].pscore[h]);
		    }   */

		/* If bestc = -1, then no good c was found (probably because all analyses had P=0). In that case, declare the tactus span invalid. */
		if(bestc == -1) tactus[a][b].valid = 0;
		else {
		    tactus[a][b].local_score[0][h] = best_score;
		    tactus[a][b].bestc[h] = bestc;
		}

		/* Find best triple division */

		bestc1 = bestc2 = -1;
		best_score = -1000000.0;

		for(c1=2; c1<b-3; c1++) {

		    if(subt[a][c1].valid==0) continue;

		    /* Regularity score */

		    //diff1 = (int)(fabs(((double)(b)/3.0) - (double)(c1)));
		    x = (double)(b)/3.0;
		    if(x - (int)(x) < (int)(x)+1 - x) diff1 = abs(c1 - (b / 3));
		    else diff1 = abs(c1 - ((b/3) + 1));
		    if(diff1 > 2) continue;

		    for(c2=c1+2; c2<b-1; c2++) {

			if(subt[a+c1][c2-c1].valid==0) continue;

			/* Regularity score */
			score = log(lower_reg_score[diff1]);       /* first triple beat (initialize score) */

			//diff2 = (int)(fabs(((double)(b) * 2.0/3.0) - (double)(c2)));  /* second triple beat */
			x = (double)(b) * 2.0/3.0;
			if(x - (int)(x) < (int)(x)+1 - x) diff2 = abs(c2 - (b*2/3));
			else diff2 = abs(c2 - ((b*2/3) + 1));

			if(diff2<= 2) score += log(lower_reg_score[diff2]);
			else continue;

			/* Factor in the rscores and pscores for the three L1 spans */
			score += subt[a][c1].pscore[h] + subt[a][c1].rscore + subt[a+c1][c2-c1].pscore[h] + subt[a+c1][c2-c1].rscore + subt[a+c2][b-c2].pscore[h] + subt[a+c2][b-c2].rscore;

			//printf("%.6f %.6f %.6f %.6f\n", subt[a][c].pscore[h], subt[a][c].rscore, subt[a+c][b-c].pscore[h], subt[a+c][b-c].rscore);

			/* Factor in the note scores for the notes on the L1 beats */

			L1_triple_note_score[c1][c2] = 0.0;
			ps = pip[a+c1].stream;
			while(ps!=null) {
			    if(ps.cs.start >= (a+b) * piplength) {
				ps = ps.next;
				continue;
			    }
			    c1_note = c2_note = 0;
			    sn = ps.sn;
			    while(sn!=null) {
				if(sn.ontime == (a+c1) * piplength) {
				    c1_note = 1;
				    L1_triple_note_score[c1][c2] += log(sn.hp_score[h]);
				}
				if(sn.ontime == (a+c2) * piplength) {
				    c2_note = 1;
				    L1_triple_note_score[c1][c2] += log(sn.hp_score[h]);
				}
				if(sn.ontime > (a+c2) * piplength) break;
				sn = sn.next;
			    }
			    L1_triple_note_score[c1][c2] += log(L1_triple_anchor_score[ps.cs.t1][ps.cs.t2][c1_note][c2_note]);
			    //if(a==20 && b==14 && c1==4 && c2==9) printf("triple anchor score [%d][%d][%d][%d] = %.10f\n", ps.cs.t1, ps.cs.t2, c1_note, c2_note, L1_triple_anchor_score[ps.cs.t1][ps.cs.t2][c1_note][c2_note]);
			    ps = ps.next;
			}

			score += L1_triple_note_score[c1][c2];

			if(score > best_score) {
			    bestc1 = c1;
			    bestc2 = c2;
			    best_score = score;
			}
		    }
		}

		//printf("Tactus int %d-%d, h = %d, best c = %d, best triple score = %.80f\n", a, a+b, h, bestc, best_score);
		if(bestc1 == -1 | bestc2 == -1) tactus[a][b].valid = 0;
		else {
		    tactus[a][b].local_score[1][h] = best_score;
		    tactus[a][b].bestc1[h] = bestc1;
		    tactus[a][b].bestc2[h] = bestc2;
		}

		/* Now we've computed local scores for the duple and triple version of each tactus interval, given root h. Now find the continuation scores. */
		tactus[a][b].local_score[0][h] += cont_score(a, b, h);
		tactus[a][b].local_score[1][h] += cont_score(a, b, h);

	    }
	}
    }
}

static void prune_tactus_spans() {

    /* If a tactus span contains no note at either boundary span in any stream, but notes in between, declare it invalid (valid = 0).
       If a tactus span contains no primary chord tones of a particular root, declare that root invalid for that span (validh[h] = 0). */

    int a, b, c, h, pc;
    int pc_tally[] = new int[12];
    int tactus_spans=0, invalid_spans=0, hspans=0, invalid_hspans=0;
     Stream  ps;
     Snote  sn;

    for(a=0; a<num_pips; a++) {
	for(b=tactus_min; b<=tactus_max; b++) {
	    if(a+b >= num_pips) continue;
	    tactus[a][b].numnotes = 0;
	    tactus_spans++;

	    if((pip[a].hasnote==0) && (pip[a+b].hasnote==0)) {
		for(c=1; c<b; c++) {
		    if(pip[a+c].hasnote!=0) {
			tactus[a][b].valid=0;
			break;
		    }
		}
	    }
	    if(tactus[a][b].valid == 0) {
		//if(a == 38 && b == 10) printf("It was pruned!!\n");
		invalid_spans++;
		continue;
	    }

	    for(pc=0; pc<12; pc++) pc_tally[pc] = 0;

	    ps = pip[a].stream;

	    while(ps!=null) {
		sn = ps.sn;
		while(sn!=null) {
		    if(sn.ontime >= (a+b) * piplength) break;
		    pc_tally[sn.pitch % 12]++;
		    tactus[a][b].numnotes++;
		    sn=sn.next;
		}
		ps=ps.next;
	    }
	    for(h=0; h<12; h++) {
		hspans++;
		if(tactus[a][b].numnotes > 0 && pc_tally[h % 12] == 0 && pc_tally[(h+3) % 12] == 0 && pc_tally[(h + 4) % 12] == 0 && pc_tally[(h+7) % 12] == 0) {
		    tactus[a][b].validh[h] = 0;
		    invalid_hspans++;
		}
		else tactus[a][b].validh[h] = 1;
	    }
	}
    }

    //printf("Tactus spans = %d, invalid spans = %d, hspans = %d, invalid hspans = %d\n", tactus_spans, invalid_spans, hspans, invalid_hspans);
}


static double tactus_reg(int a, int b) {

    return log(tactus_profile[b][a]);

}

static double tactus_score(int p, int p2, int h, int h2) {

    /* Assign a note score for pip p for all streams present at p (a possible tactus beat). Present streams are those that end on or
       after p (these are exactly the ones in p's list) and that begin before p2 (the following tactus beat). */

     Stream  ps;
    double score;
    int interval;

    score = 0.0;
    ps = pip[p].stream;
    while(ps != null) {

	if(ps.cs.start >= p2 * piplength) {
	    ps = ps.next;
	    continue;
	}
	if(ps.sn.ontime == p * piplength) {
	    score += log(raw_tactus_note_score);
	    score += log(ps.sn.hp_score[h]);
	    //if(p==209 && (h==2 || h==7)) printf("pitch=%d, h=%d, score=%.3f\n", ps.sn.pitch, h, log(ps.sn.hp_score[h]));
	}
	else score += log(1.0 - raw_tactus_note_score);

	ps = ps.next;
    }

    if(h2 == -1) score += log(1.0 / 12.0);
    else if(h == h2) score += log(1.0 - raw_harm_change);
    else {
	score += log(raw_harm_change);
	interval = ((h+12)-h2) % 12;
	if(interval==7 || interval==5) score += log(fifths_move / 2.0);
	/* Divide up remaining mass among remaining 9 roots */
	else score += log((1.0 - fifths_move) / 9.0);
    }

    return score;
}

static double adj_tactus_score(int t, int t2, int ph, int h, int h2) {

    /* Like "tactus_score" above, but now we're including a possible upper level (indicated by ph).
       t is the index number of a tactus pip, tp[t] is the pip, e.g. tp[0] is the first tactus beat; t2 is the following tactus pip.
       harm_change = 1 if t's harmony is the same as t-1's, 0 otherwise. */

     Stream  ps;
    double score = 0.0;
    int n=0, interval;

    ps = pip[tp[t]].stream;

    /* Check all streams active during this tactus (i.e. ones in tp[t]'s streamlist that start before tp[t2]); see if they have a note at tp[t]. */
    while(ps!=null) {

	if(ps.cs.start >= tp[t2] * piplength) {
	    ps = ps.next;
	    continue;
	}

	if(ps.sn.ontime == tp[t] * piplength) {
	    /* There's a note on the beat */
	    if(ph == 0 || ph == 2) {
		/* It's an L3 beat */

		score += log(L3_note_score);
		score += log(ps.sn.hp_score[h]);
	    }
	    else {
		score += log(L2_note_score);
		score += log(ps.sn.hp_score[h]);
	    }
	}
	else {
	    /* There's no note on the beat */
	    if(ph == 0 || ph == 2) score += log(1.0 - L3_note_score);
	    else score += log(1.0 - L2_note_score);
	}
	ps = ps.next;
    }

    /* Harmonic change scores */

    if(h2 == -1) {
	score += log(1.0 / 12.0);                 /* P of first harmony: uniform across all 12 */
    }

    else if(h == h2) {
	if(ph == 0 || ph == 2) score += log(1.0 - L3_harm_change);
	else score += log(1.0 - L2_harm_change);
    }

    else {
	if(ph == 0 || ph == 2) score += log(L3_harm_change);
	else score += log(L2_harm_change);

	interval = ((h+12)-h2) % 12;
	if(interval == 5 || interval == 7)  score += log(fifths_move / 2.0);
	else score += log((1.0 - fifths_move) / 9.0);

    }

    //printf("t = %d, ph = %d, hc = %d: Upper score = %.3f\n", t, ph, harm_change, score);

    return score;
}


static void make_tables() {

    int a, b, k, bestk, besth2, h, h2, d, first_table;
    double score, best_score;
     Stream  ps;

    /* The first onset is at pip tactus_max-1. The first tactus beat can be anywhere from pip 0 to pip tactus_max-1. The second
       tactus beat must be at tactus_max or later. (In other words: the first onset must be within the first tactus interval and
       not at its endpoint.) We enforce this by starting a at tactus_max.
          Certain a's (those between tmax + tmin and 2 * tmax) could possibly be either the second tactus or the third
       tactus. For the first table, we prohibit a-k >= tactus_max; for subsequent tables, we prohibit a-k < tactus_max.  */

    if(num_pips < 48) {   /* why 48? */
	System.out.printf("Only %d pips in piece - not enough pips to do analysis\n", num_pips);
	System.exit(1);
    }

    for(a=tactus_max; a<num_pips; a++) {
	for(b=tactus_min; b<=tactus_max; b++) {

	    //printf("Here's the score: %.3f\n", tactus[43][20].score[2]);
	    if(a+b >= num_pips) continue;

	    if((tactus[a][b].valid==0)) {
		for(d=0; d<2; d++) {
		    for(h=0; h<12; h++) tactus[a][b].score[d][h] = -1000000.0;
		}
		continue;
	    }

	    for(d=0; d<2; d++) {

		for(h=0; h<12; h++) {

		    if((tactus[a][b].validh[h]==0)) {
			tactus[a][b].score[d][h] = -1000000.0;
			continue;
		    }

		    best_score = -1000000.0;

		    for(k=tactus_min; k<=tactus_max; k++) {

			if(a < tactus_max + tactus_min) first_table = 1;
			else if(a >= tactus_max * 2) first_table = 0;
			else {
			    if(a-k < tactus_max) first_table = 1;
			    else first_table = 0;
			}

			if((tactus[a-k][k].valid==0)) continue;
			//if(abs(b-k) > 2) continue;

			for(h2=0; h2<12; h2++) {

			    if((tactus[a-k][k].validh[h2]==0)) continue;

			    score = 0.0;          /* initialize "score" */
			    if(first_table!=0) {
				if(d==0) score += log(lower_duple_score);               /* Initial scores for duple vs. triple lower level */
				else score += log(1.0 - lower_duple_score);
				score += tactus_score(a-k, a, h2, -1);
				score += log(init_tactus[k-1]);   //****
				score += tactus[a-k][k].local_score[d][h2] + tactus[a][b].local_score[d][h];
			    }
			    else score += tactus[a-k][k].score[d][h2] + tactus[a][b].local_score[d][h];

			    score += tactus_reg(b, k);
			    score += tactus_score(a, a+b, h, h2);
			    score += log(another);

			    //if(a==63 && b==20 && h==4) printf("a=%d, b=%d, h=%d: k=%d, h2=%d, score = %.3f\n", a, b, h, k, h2, score);

			    /* What do we do about note scores for the final pip? At present, nothing. (We could just say, the P of note-onsets
			       at the final pip is 0!) */

			    //if(a==209 && b==9 && k==9) printf("a=%d, b=%d, h=%d, k=%d, h2=%d: score T1A = %.3f, score T1B = %.3f, note score = %.3f, score = %.3f\n", a, b, h, k, h2, tactus[a-k][k].score[h2], tactus[a][b].local_score[h], tactus_note_score(a, h), score);

			    if(score > best_score) {
				tactus[a][b].score[d][h] = best_score = score;
				tactus[a][b].bestk[d][h] = k;
				tactus[a][b].besth2[d][h] = h2;
			    }
			}
		    }
		    if(best_score == -1000000.0) {
			tactus[a][b].score[d][h] = -1000000.0;
			//printf("Warning: At %d, %d, %d: no good k or h found\n", a, b, h);
			//exit(1);
		    }
		    //else printf("a=%d, b=%d, h=%d: best k = %d; score = %.3f; besth2 = %d\n", a, b, h, tactus[a][b].bestk[d][h], best_score, tactus[a][b].besth2[d][h]);

		    //if(a < 20) printf("%d ", besth2);
		}
	    }
	}
    }
}

static void traceback() {

    int a, b, p, h, j, k, besth=0, bestk=0, besta=0, besth2, d, prev, t;
    double best_score;

    /* The last note offtime is at last_pip - (tactus_max - 1). We want to allow the last offtime to be anywhere in a tactus interval of any size,
       except at the very beginning. */
    /* Search for the best final a and k. Search a in the range (last_pip - (tactus_max - 1), last_pip), that is, back to and including the last
       offtime. Then choose k such that ((a-k)+1, a) includes the last offtime: that is, skip cases where a-k > last_pip - (tactus_max - 1). */

    best_score = -1000000.0;
    for(a=(num_pips - tactus_max); a<num_pips; a++) {
	for(d = 0; d<2; d++) {
	    for(k = tactus_min; k <= tactus_max; k++) {
		if(a-k > num_pips - tactus_max) continue;

		for(h=0; h<12; h++) {

		    /* First we need to factor in the P of NOT generating another tactus beat after this one */
		    tactus[a-k][k].score[d][h] += log(1.0-another);

		    //if(a == 391 && k == 18) printf("h = %d, score = %.3f\n", h, tactus[a-k][k].score[d][h]);
		    if(tactus[a-k][k].score[d][h] > best_score) {
			besta = a;
			besth = h;
			bestk = k;
			bestd = d;
			best_score = tactus[a-k][k].score[d][h];
		    }
		}
	    }
	}
    }

    if(best_score == -1000000.0) {
	System.out.printf("No valid global analysis found\n");
	System.exit(1);
    }
    final_score = best_score;
    last_tactus = besta;
    pip[last_tactus].besth = besth;

    if(verbosity>=0) System.out.printf("Raw final metharm score = %.3f\n", final_score);

    /* besta is the last tactus beat; bestd is the best division, duple (0) or triple (1) */

    if(verbosity >= 0) System.out.printf("Best final tactus: (%d, %d), score = %.3f, best d = %d, best h = %d\n", besta-bestk, bestk, best_score, bestd, besth);

    for(a=(num_pips - tactus_max); a<num_pips; a++) {
	for(d = 0; d<2; d++) {
	    for(k = tactus_min; k <= tactus_max; k++) {
		if(a-k > num_pips - tactus_max) continue;
		for(h=0; h<12; h++) {
		    //if(a == besta && h == besth && k == bestk && d == bestd) continue;
		    if(abs(bestk - k) < 4) continue;
		    if(verbosity >= 0) if(tactus[a-k][k].score[d][h] > best_score - 3.0) System.out.printf("   Contender: (%d, %d), score = %.3f, best d = %d, best h = %d\n", a-k, k, tactus[a-k][k].score[d][h], h, d);
		}
	    }
	}
    }

    pip[besta].next_tactus = -1; /* Not really needed */
    b=bestk;
    j = besta-b;

    while(true) {
	//if(j >= tactus_max) printf("Making tactus at %d; best h = %d, best k = %d\n", j, besth, tactus[j][b].bestk[bestd][besth]);
	//else printf("Making first tactus at %d; best h = %d\n", j, besth);
	pip[j].next_tactus = j+b;
	pip[j].besth = besth;
	if(j<tactus_max) break;
	besth2 = tactus[j][b].besth2[bestd][besth];
	b = tactus[j][b].bestk[bestd][besth];
	besth = besth2;
	j = j-b;
    }

    first_tactus = j;

    tp[0] = prev = first_tactus;
    tp[1] = p = pip[prev].next_tactus;
    t = 2;

    //printf("Tactus beats:\n");
    while(true) {
	h = pip[prev].besth;
	//if(bestd==0) printf("%d (h=%d, bestc=%d)\n", prev * piplength, h, tactus[prev][p-prev].bestc[h]);
	//else printf("%d (h=%d, bestc1=%d, bestc2=%d)\n", prev * piplength, h, tactus[prev][p-prev].bestc1[h], tactus[prev][p-prev].bestc2[h]);
	if(p == last_tactus) break;
	prev = p;
	tp[t] = p = pip[p].next_tactus;
	t++;
    }
    //printf("%d (final tactus)\n", p * piplength);
    num_tactus_beats = t;
    //printf("Num tactus beats = %d\n", t);

}

static int convert_phase(int ph) {

    int ph2;
    if(ph < 2) {
	if((num_tactus_beats-1) % 2 == 0) ph2 = (ph + 1) % 2;
	else ph2 = ph;
    }
    else {
	if((num_tactus_beats-1) % 3 == 2) ph2 = ((((ph-2) + 2) % 3) + 2);
	else if((num_tactus_beats-1) % 3 == 0) ph2 = ((((ph-2) + 1) % 3) + 2);
	else ph2 = ph;
    }
    return ph2;
}

static void find_upper_level() {

    int t, ph, harm_change, h, h2, besth=0, a, b;
    double score, best_score;

    /* ph: 0 = first duple beat; 1 = second duple beat; 2 = first triple beat; 3 = second triple beat; 4 = third triple beat */

    for(ph=0; ph<5; ph++) {
	for(h=0; h<12; h++) {
		if (upper[0] == null) upper[0] = new upper_class();
	    upper[0].score[ph][h] = adj_tactus_score(0, 1, ph, h, -1) + log(init_phase_score[ph]);
	    a = tp[0];
	    b = tp[1] - tp[0];
	    upper[0].score[ph][h] += tactus[a][b].local_score[bestd][h];
	}
    }

    for(t=1; t<(num_tactus_beats-1); t++) {

	a = tp[t];
	b = tp[t+1] - tp[t];

	for(ph=0; ph<5; ph++) {
	    for(h=0; h<12; h++) {
		best_score = -1000000.0;

		for(h2=0; h2<12; h2++) {

		    score = adj_tactus_score(t, t+1, ph, h, h2);
		    score += tactus[a][b].local_score[bestd][h];

		    if(ph<2) score += upper[t-1].score[(ph+1)%2][h2];
		    else score += upper[t-1].score[ (((ph-2)+2)%3) + 2 ][h2];

		    //if(t==1) printf("t=%d, ph=%d, h=%d, h2=%d: prev score = %.3f, local span score = %.3f, note score = %.3f, score = %.3f\n", t, ph, h, h2, upper[t-1].score[(ph+1)%2][h2], tactus[a][b].local_score[bestd][h], adj_tactus_score(t, t+1, ph, h, h2), score);

		    if(score > best_score) {
			best_score = score;
			if (upper[t]==null) upper[t] = new upper_class();
			upper[t].score[ph][h] = score;
			upper[t].besth2[ph][h] = h2;
		    }
		}
	    }
	}
    }

    t--;

    /* Now t = num_tactus_beats-2. Find the best final analysis (phase and root). */

    best_score = -1000000.0;
    for(ph=0; ph<5; ph++) {
	for(h=0; h<12; h++) {
	    if(upper[t].score[ph][h] > best_score) {
		best_ph = ph;
		besth = h;
		best_score = upper[t].score[ph][h];
	    }
	}
    }

    /* best_ph indicates the phase of the LAST beat. Now report the phase scores, but converting each phase to represent the phase of the first beat.
       Then for the best phase, set best_ph to the phase of the first beat. */

    if(verbosity>=0) {
	System.out.printf("Phase scores (with best h=%d):\n", besth);
	for(ph=0; ph<5; ph++) {
	    System.out.printf("Phase = %d: score = %.3f\n", convert_phase(ph), upper[t].score[ph][besth]);
	}
	System.out.printf("Best phase = %d\n", convert_phase(best_ph));
    }
    best_ph = convert_phase(best_ph);

    /* Set the phase for each tactus pip */

    for(t=0; t<num_tactus_beats; t++) {
	if(best_ph < 2) {
	    if(t%2 == best_ph) pip[tp[t]].ph = 0;
	    else pip[tp[t]].ph = 1;
	}
	else {
	    /* (3 - (best_ph-2)) is the first upper level beat. */
	    if(t%3 == (3 - (best_ph-2)) % 3) pip[tp[t]].ph = 2;
	    else if(t%3 == ((3 - (best_ph-2))+1) % 3) pip[tp[t]].ph = 3;
	    else if(t%3 == ((3 - (best_ph-2))+2) % 3) pip[tp[t]].ph = 4;
	}
    }
    /* Trace back the harmony; besth (set above) is the best h of the final span. */

		if (upper[tp[num_tactus_beats-1]] == null) upper[tp[num_tactus_beats-1]] = new upper_class();
    pip[tp[num_tactus_beats-1]].besth = upper[tp[num_tactus_beats-1]].besth = besth;

    for(t=num_tactus_beats-2; t>=0; t--) {
	pip[tp[t]].besth = upper[t].besth = besth;
	if(t==0) break;
	//printf("ph=%d; besth=%d; ", pip[tp[t]].ph, besth);
	besth = upper[t].besth2[pip[tp[t]].ph][besth];
    }

    /*
    printf("New harmonic analysis:\n");
    for(t=0; t<num_tactus_beats-1; t++) {
	printf("pip %d, phase %d: root %d; ", tp[t], pip[tp[t]].ph, upper[t].besth);
    }
    printf("\n");
    */

    /* Now we calculate the final score. We start with best_score, the best upper-level score calculated above, which considers the
       local scores, tactus note scores, and harmonic change scores (the latter two are both in adj_tactus_score); then add in the
       lower_duple score, initial tactus score, "another", and reg_scores. */

    final_score = best_score;
    if(bestd==0) final_score += log(lower_duple_score);
    else final_score += log(1.0 - lower_duple_score);
    final_score += log(init_tactus[tp[1]-tp[0]]);
    for(t=1; t<num_tactus_beats-1; t++) {
	final_score += tactus_reg(tp[t+1]-tp[t], tp[t]-tp[t-1]);
	final_score += log(another);
    }
    final_score += log(1.0 - another);
    if(verbosity>=0) System.out.printf("Adjusted final metharm score = %.3f\n", final_score);
}

static void print_root(int h) {

    if(h==0) System.out.printf("C ");
    if(h==1) System.out.printf("Db");
    if(h==2) System.out.printf("D ");
    if(h==3) System.out.printf("Eb");
    if(h==4) System.out.printf("E ");
    if(h==5) System.out.printf("F ");
    if(h==6) System.out.printf("F#");
    if(h==7) System.out.printf("G ");
    if(h==8) System.out.printf("Ab");
    if(h==9) System.out.printf("A ");
    if(h==10) System.out.printf("Bb");
    if(h==11) System.out.printf("B ");
}

static int lof(int h) {
    return ((h * 7) + 6) % 12;
}

static void final_meter() {

    /* Set pip[].final_beatlevel for all pips: -1 = nonbeat_note; 0 = L0, etc. */

    int p, prev, b, h, L1_pip, L1_pip1, L1_pip2, t;

    for(p=0; p<num_pips; p++) {
	pip[p].final_beatlevel = -1;
    }

    pip[first_tactus].final_beatlevel = 2;
    prev = first_tactus;
    p = pip[first_tactus].next_tactus;

    while(true) {
	h = pip[prev].besth;
	if(bestd==0) {
	    L1_pip = prev + tactus[prev][p-prev].bestc[h];
	    pip[L1_pip].final_beatlevel = 1;
	    pip[prev + subt[prev][L1_pip-prev].bestc].final_beatlevel = 0;
	    //printf("L0 pip(%d, %d) = %d\n", prev, L1_pip-prev, prev + subt[prev][L1_pip-prev].bestc);
	    pip[L1_pip + subt[L1_pip][p-L1_pip].bestc].final_beatlevel = 0;
	}

	else {
	    L1_pip1 = prev + tactus[prev][p-prev].bestc1[h];
	    L1_pip2 = prev + tactus[prev][p-prev].bestc2[h];
	    pip[L1_pip1].final_beatlevel = 1;
	    pip[L1_pip2].final_beatlevel = 1;
	    pip[prev + subt[prev][(L1_pip1)-prev].bestc].final_beatlevel = 0;
	    pip[L1_pip1 + subt[L1_pip1][L1_pip2-L1_pip1].bestc].final_beatlevel = 0;
	    pip[L1_pip2 + subt[L1_pip2][p-L1_pip2].bestc].final_beatlevel = 0;
	}

	pip[p].final_beatlevel = 2;
	if(p == last_tactus) break;
	prev = p;
	p = pip[p].next_tactus;
    }

    for(t=0; t<num_tactus_beats; t++) {
	if(pip[tp[t]].ph == 0 || pip[tp[t]].ph == 2) pip[tp[t]].final_beatlevel++;
    }

}

static double poisson(double c, int k) {

    int f=1, k2;

    for(k2=2; k2<=k; k2++) f *= k;
    /* ^ if k=0, f will be 1 (as it should be) */

    return Math.pow(c, (double)(k)) * Math.exp(-c) / (double)(f);
}

static void stream_probs() {

    int t, t2, numstreams_starting;
     Stream  s;
    double stream_score;

    stream_score = 0.0;
    for(t=0; t<(num_tactus_beats-1); t++) {
	numstreams_starting = 0;
	s = streamlist;
	while(s!=null) {
	    if(s.start >= tp[t] * piplength && s.start < tp[t+1] * piplength) {
		numstreams_starting++;
		for(t2=t; t2<num_tactus_beats; t2++) {
		    if(s.end <= tp[t2] * piplength) break;
		}
		/* now t2 is the final tactus of the stream. */
		stream_score += (t2-t) * log(stream_cont);      /* t2-t2 could be zero. */
		stream_score += log(1.0 - stream_cont);
	    }
	    s = s.next;
	}
	if(t<=1) stream_score += log(poisson(firstseg_stream_prob, numstreams_starting));
	else stream_score += log(poisson(stream_prob, numstreams_starting));

	//printf("t = %d, numstreams starting = %d, score = %.3f\n", t, numstreams_starting, poisson(numstreams_starting));
    }
    final_score += stream_score;
    if(verbosity >= 0) {
	System.out.printf("stream score = %.3f\n", stream_score);
	System.out.printf("Total final score = %.3f\n", final_score);
    }
}


static void graphic_display() {

    int p, b, h, currenth=0, i;
     Stream  s;
     Snote  sn;

    /* We create an array pip[].row[]. If there is a note at a pitch, we assign the voice number of the note to that cell of the array. If there is
       a continuation, we assign the voice number + 100. (What happens if there are more than 100 streams?) */

    for(p=0; p<num_pips; p++) {

	for(i=0; i<100; i++) 
            pip[p].row[i]=0;
	s = streamlist;
	while(s!=null) {
	    sn=s.sn;
	    while(sn!=null) {
		if(sn.ontime == p*piplength) {
		    if(sn.pitch < 0 || sn.pitch >= 100) {
			System.out.printf("Pitch out of range!\n");
			System.exit(1);
		    }
		    if(pip[p].final_beatlevel == -1) {
		        System.out.printf("Nonbeat note in final analysis: Note %d %d %d\n", sn.ontime, sn.offtime, sn.pitch);
		    }
		    else pip[p].row[sn.pitch] = s.index;

		}
		else {
		    if(sn.ontime < p*piplength && sn.offtime > p*piplength && pip[p].final_beatlevel >= 0) {
			pip[p].row[sn.pitch] = s.index + 100;
		    }
		}
		sn = sn.next;
	    }
	    s = s.next;
	}
    }

    /* We don't initialize currenth because we assume the first beat is a tactus beat */
    for(p=0; p<=last_tactus; p++) {
	if(pip[p].final_beatlevel > 1) currenth = pip[p].besth;
	if(pip[p].final_beatlevel > -1) {
	    System.out.printf("%5d (%4d) ", p*piplength, p);

	    for(h=0; h<lof(currenth); h++) System.out.printf(" ");
	    print_root(currenth);
	    for(h=lof(currenth)+1; h<12; h++) System.out.printf(" ");

	    for(b=0; b<=3; b++) {
		if(b<=pip[p].final_beatlevel) System.out.printf("x ");
		else System.out.printf("  ");
	    }
	    for(i=30; i<90; i++) {
		//if(pip[p].row[i] > 0 && pip[p].row[i] != 100) {
		if(pip[p].row[i] > 0) {
		    if(pip[p].row[i] < 100) {
			System.out.printf("%d", pip[p].row[i]);
			if(pip[p].row[i] > 9) i++;
		    }
		    else System.out.printf("|");

		}
		else if(i%12==0 || (pip[p].final_beatlevel > 1 && i%2==0)) System.out.printf(".");
		else System.out.printf(" ");
	    }
	    System.out.printf("\n");
	}
    }
}

static void print_chords() {

    int t, r=0, prev, startpip, start, end, r2;

    /* We want to output chord-spans, with timing aligned with the input files. We assume that the first
       chord span begins at the first note onset. We have to undo the offset by (tactus_max-1)*pip_length added earlier.
       Warning: This only works if the input files all start at time 0. */

    startpip = tactus_max - 1;
    prev = pip[tp[0]].besth;
    for(t=1; t<num_tactus_beats; t++) {
        if(t<(num_tactus_beats-1)) r = pip[tp[t]].besth;
	if(r != prev || t==num_tactus_beats-1) {
	    start = (startpip * piplength) - ((tactus_max-1) * piplength);
	    end = (tp[t] * piplength) - ((tactus_max-1) * piplength);
	    r2 = ((prev + 2) * 7) % 12;
	    System.out.printf("Chord %d %d %d\n", start, end, r2);
	    startpip = tp[t];
	}
	prev = r;
    }

}

static void meter_stats() {

    int t, p, firstt, lastt, ntb, upper, lower, upper_ph, pickups;
    double avg_tactus;

    ntb = num_tactus_beats-1;
    firstt = 0;
    if(pip[tp[0]].hasnote==0) {
	ntb--;
	firstt = 1;
    }
    lastt = num_tactus_beats-2;
    for(t=num_tactus_beats-3; t>=0;t--) {   //****//for(t=num_tactus_beats-3; t--; t>=0) {   //****
	if(pip[tp[t]].hasnote==0) {
	    ntb--;
	    lastt = t;
	}
	else break;
    }

    /* Now ntb is the number of tactus beats from the first filled tactus to the last, inclusive. Subtract one to get the number of tactus INTERVALS. */
    ntb--;

    avg_tactus = (double)((tp[lastt] - tp[firstt]) * piplength) / (double)(ntb);

    if(bestd==0) lower = 2;
    else lower = 3;
    if(pip[tp[0]].ph < 2) upper = 2;
    else upper = 3;
    if(pip[tp[0]].hasnote==1) pickups = 0;
    else {
	pickups = 0;
	/* assume that the second tactus beat has a note */
	for(p=tp[1]-1; p>tp[0]; p--) {
	    if(pip[p].hasnote==1) {
		pickups++;
	    }
	}
    }
    if(pip[tp[0]].hasnote==1) {
	upper_ph = pip[tp[0]].ph;
    }
    else upper_ph = pip[tp[1]].ph;
    if(upper_ph >= 2) upper_ph -= 2;
    System.out.printf("%4.0f %d %d %d %d\n", avg_tactus, lower, upper, pickups, upper_ph);
}


}
