package midireader.Temperley;

import static midireader.Temperley.Globals.*;
import static midireader.Temperley.Polyph.*;

import static java.lang.Math.log;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


public class Streamer {


static void fill_segments() {

    /* Here we create "segment columns", an array indicating which pitches are present in a column: 3 if a pitch onset is there (black), 2 if a pitch
       continuation is there (blue), 1 if a pitch was recently there (grey). We also create "inotes" for each segment. */

    int s, i, n, x, ss, p, cost;
    for (s=0; s<=segtotal; s++) {
	for (i=0; i<1000; i++) { //100
	    segment[s].column[i]=0;
	}
    }

    for (s=0; s<=segtotal; s++) {
	segment[s].numblue = 0;
	for (x=0, n=0; x<numnotes; x++) {
	    if(n>=max_voices) continue;                      /* Don't let the number of black squares in a segment exceed max_voices. Excessive notes
								will just be ignored. */

	    /* If an ontime is between two segment boundaries, assume it's the first one. (This shouldn't happen, because
	       every ontime should define a segment.) If an offtime is between two boundaries, assume it's the second one (this might happen). */
	    if (note[x].ontime < segment[s].end && note[x].offtime > segment[s].start && note[x].valid!=0) {
		segment[s].inote[n]=note[x].pitch;
		if (note[x].ontime >= segment[s].start || (s>0 && segment[s-1].column[note[x].pitch]<2)) {
		    segment[s].column[note[x].pitch]=3;
		    /* Each note is marked with the number of the segment in which it begins - useful for printing out the
		       notes later */
		    note[x].segment = s;
		}
		else {
		    segment[s].column[note[x].pitch]=2;
		    segment[s].numblue++;
		}
		n++;
	    }
	}
	segment[s].numnotes=n;
	segment[s].numdark=n;    /* The number of black or blue squares in the segment */

	/*
	printf("Segment %d first pass numnotes = %d: ", s, segment[s].numnotes);
	for(x=40; x<100; x++) {
	    if(segment[s].column[x]>1) printf("|");
	    else printf(" ");
	}
	printf("\n");  */
    }

    for(p=0; p<1000; p++) { //100
	canceled[p]=0;
    }

    for (s=0; s<=segtotal; s++) {
	n=0;

	/* Now we add grey squares after each black square. */

	/* We're at some segment s and we're wondering whether its squares should be grey. We step back one segment (to ss)
	   and see if there are any black squares there; if so, we mark squares at s of the same pitch (if white) as grey. We continue this for
	   6 squares. */

	for(p=0; p<1000; p++) { //1000
	    if(segment[s].column[p]>0) continue;
	    cost=0;
	    for(ss=s-1; ss>=0; ss--) {
		if(segment[ss].column[p]==2) cost+=1;
		else if(segment[ss].column[p]<2) cost+=2;
		else if(segment[ss].column[p]==3) {
		    segment[s].inote[segment[s].numnotes+n]=p;
		    segment[s].column[p]=1;
		    n++;
		    break;
		}
		if(cost > 10) break;
	    }
	}

	/* printf("For segment %d, number of squares looked back = %d\n", s, s-ss); */
	segment[s].numnotes+=n;
	/* printf("Segment %d numnotes = %d\n", s, segment[s].numnotes);  */   /* numnotes = total number of black plus grey sqs */

	for(p=0; p<1000; p++) { //100
	    if(segment[s].column[p]>=2) {
		canceled[p+1]=1;
		canceled[p+2]=1;
		canceled[p-1]=1;
		canceled[p-2]=1;
	    }
	}
	for(p=0; p<1000; p++) { //100
	    if(segment[s].column[p]>=2) canceled[p]=0;
	}

	/* This routine eliminates any grey squares on pitches where there has been a note within one or two chromatic steps more
	   recently than a note of the pitch itself (but only in cases where max_grey would otherwise be exceeded) */

	if (segment[s].numnotes-segment[s].numdark > max_grey) {
	    //printf("max_grey exceeded by %d at seg %d; doing first heuristic\n", segment[s].numnotes-(segment[s].numdark+max_grey), s);
	    if(verbosity >= 2) System.out.printf("Doing note-canceling heuristic on segment %d (%d-note excess)\n", s, (segment[s].numnotes-segment[s].numdark)-max_grey);
	    n=0;
	    for(p=0; p<1000; p++) { //1000
		if (segment[s].column[p]==1 && segment[s].numnotes-(segment[s].numdark+n) > max_grey) {
		    if (canceled[p]==1) {
			segment[s].column[p]=0;
			n++;
		    }
		}
	    }
	}

	/* If the first heuristic didn't succeed in getting the number of grey squares down to max_grey, this second one throws out
	   grey squares, starting from the bottom, until max_grey is reached. */

	if (segment[s].numnotes-(segment[s].numdark+n) > max_grey) {
	    //printf("max_grey still exceeded by %d; doing second heuristic\n", segment[s].numnotes-(segment[s].numdark+n+max_grey));
	    if(verbosity >= 2 ) System.out.printf("max_grey still exceeded by %d; doing second heuristic\n", segment[s].numnotes-(segment[s].numdark+n+max_grey));
	    i=0;
	    for(p=0; p<1000; p++) { //100
		if (segment[s].column[p]==1 && segment[s].numnotes-(segment[s].numdark+n+i) > max_grey) {
		    segment[s].column[p]=0;
		    i++;
		}
	    }
	}
    }
}

/* Now we create snotes, which are like inotes except they're in ascending numerical order. We also eliminate canceled notes. */

static void create_snotes() {

    int s, i, min, prev, k, newnumnotes, p, lowest;
    int done[] = new int[1000]; //100

    for (s=0; s<=segtotal; s++) {
	newnumnotes=0;
	//printf("Segment %d snotes:\n", s);
	for(i=0; i<1000; i++) done[i]=0; //100
	for (k=0; k<segment[s].numnotes; k++) {
	    lowest = -1;
	    min=1000; //100
	    for (i=0; i<segment[s].numnotes; i++) {
		p=segment[s].inote[i];
		if(segment[s].column[p]>0 && p<min && done[i]==0) {
		    lowest = i;
		    min = p;
		}
	    }
	    if(lowest == -1) continue;
	    segment[s].snote[k]=min;
	    done[lowest]=1;
	    newnumnotes++;
	    //printf("%d c%d; ", min, segment[s].column[min]);
	}
	segment[s].numnotes=newnumnotes;
	//printf("\n");
	/* printf("Newnumotes = %d\n", newnumnotes); */
    }
}

static void print_segments() {
    int s, i, p;
    for(s=0; s<=segtotal; s++) {
	//System.out.printf("Segment %d contains black or grey squares at ", s);
	System.out.printf("%6d: ", s);
	for(p=24; p<=84; p++) {
	    if(segment[s].column[p]==3) System.out.printf("X ");
	    else if(segment[s].column[p]==2) System.out.printf("| ");
	    else if(segment[s].column[p]==1) System.out.printf(": ");
	    else System.out.printf("  ");
	}
	System.out.printf("\n");
    }
}


/* We're generating a particular column analysis. We're going through a column which has certain filled (black, blue, or grey)
   squares, generating an analysis for each subset of these squares. The filled squares are listed in segment[seg].snote[].
   numv is the number of voices in the analysis. v is the voice we're on this time through the function. m is the number
   of the square we're on, i.e. m=0 is the first filled square. (Going into the function for the first time, v and m are both -1.
   It's not until the second pass through the function, when v is 0, that a voice is actually assigned to
   a square.) */

static int generate_column_analyses(int m, int numv, int v) {

    int p, n, i, pok, aok, pp, ok;

    if(m>-1) prov_analysis[v]=segment[seg].snote[m];
    v++;
    if(v<numv) {
	for (p=m+1; p<segment[seg].numnotes; p++) {              /* Disallows collisions */
	    ok = generate_column_analyses(p, numv, v);
	    if(ok==0) return 0;
	}
    }

    else {
	aok=1;
	for (n=0; n<segment[seg].numnotes; n++) {
	    pok=0;
	    pp=segment[seg].snote[n];
	    if (segment[seg].column[pp]>2) {                     /* Now we check the analysis to make sure that all black squares
								     are covered. (Blue squares need not be covered!) */
		for(i=0; i<numv; i++) {
		    if (prov_analysis[i]==segment[seg].snote[n]) pok=1;
		}
		if(pok==0)aok=0;
	    }
	}

	if (aok==1) {
	    segment[seg].analgrey[a] = segment[seg].analblack[a] = segment[seg].analblue[a] = 0;
	    for(i=0; i<numv; i++) {
		segment[seg].analysis[a][i]=prov_analysis[i];
		if (segment[seg].column[prov_analysis[i]] == 1) segment[seg].analgrey[a]++;
		else if (segment[seg].column[prov_analysis[i]] == 2) segment[seg].analblue[a]++;
		else if (segment[seg].column[prov_analysis[i]] == 3) segment[seg].analblack[a]++;
	    }

	    /* Use this routine for printing out all the analyses at a given segment */

	    /*
	    if(seg==1) {
		printf("Analysis %d:", a);
		for(i=0; i<numv; i++) {
		    printf("%d ", prov_analysis[i]);
		}
		printf("\n");
		} */

	    segment[seg].analcard[a]=numv;
	    /* printf("Number of voices in analysis = %d\n", numv);  */

	    segment[seg].analscore[a] = (log(fp_note_prob) * segment[seg].analblack[a])
		+ (log(1.0-fp_note_prob) * (segment[seg].analcard[a]-segment[seg].analblack[a])) + (log(fp_note_cont) * segment[seg].analblue[a]);
	    /* ^ Note onset scores, non-onset scores, note continuation scores */

	    segment[seg].analscore[a] += log(fp_last_note_cont) * (segment[seg].numblue - segment[seg].analblue[a]);
	    /* ^ This line adds a P for any note continuation in a segment that is not in a stream - must be the last note of a stream */

	    a++;
	    if(a==MA) return 0;
	}
    }

    return 1;
}

static void column_analyses() {
    int numv, ok;
    for(seg=0; seg<=segtotal; seg++) {
	//printf("Segment %d:\n", seg);
	a=0;
	for(numv=0; numv<=max_voices; numv++) {
	    ok = generate_column_analyses(-1, numv, -1);
	    if(ok==0) {
		if(verbosity>0) System.out.printf("Warning: Max number of analyses exceeded\n");
		//exit(1);
	    }
	}

	segment[seg].numanal=a;
	//if(verbosity >= 2) printf("Number of analyses for segment %d = %d\n", seg, seg[seg].numanal);
	if(segment[seg].numanal==0) {
	    System.out.printf("First-pass streamer found no valid analyses for segment %d\n", seg);   /* Not sure if this would ever occur */
	    System.exit(1);
	}

    }
}


	static void generate_transition_sides(int i, int m, int numv, int v, int side) {

		int p, vv, c, transgrey;
		v++;

		if (v < numv) {
			for (p = m + 1; p < segment[seg].analcard[i]; p++) {
				if (m > -1) {
					prov_trans[v] = segment[seg].analysis[i][m];
				}
				generate_transition_sides(i, p, numv, v, side);
			}
		} else {

			/* If we get here, we have a complete transition - still "provisional". If it's OK, write it as an ltransition (or rtransition) */
			//System.out.println("v=" + v + " seg=" + seg + " i=" + i + " m=" + m);
			if (m==-1)
			{
				prov_trans[v] = 8888;  //****
			} else
				prov_trans[v] = segment[seg].analysis[i][m];  /* Write the last voice or the provisional transition */

			/*  printf("transition %d:\n", t);  */

			if (side == 0) {
				transgrey = 0;
				for (vv = 1; vv <= numv; vv++) {
					if (segment[seg].column[prov_trans[vv]] < 3) {
						transgrey++;
					}
				}
				if (transgrey == segment[seg].analcard[i] - segment[seg].analblack[i]) {
					/* Only write the left transition if it covers all grey and blue squares in the analysis. */
					for (vv = 1; vv <= numv; vv++) {
						ltransition[i][t][vv - 1] = prov_trans[vv];
					}
					ltranscard[i][t] = numv;
					t++;
				}
			}

			c = 0;
			if (side == 1) {
				for (vv = 1; vv <= numv; vv++) {                            /* Only write the right transition if all the "must-be-continuation"
					squares in the analysis (blues or greys) are covered by it. */
					if (segment[seg].column[prov_trans[vv]] < 3) {
						c++;
					}
				}
				if (c == segment[seg].analgrey[i] + segment[seg].analblue[i]) {
					for (vv = 1; vv <= numv; vv++) {
						rtransition[i][t][vv - 1] = prov_trans[vv];
					}
					rtranscard[i][t] = numv;
					t++;
				}
			}
		}
	}

static void find_transitions(int i, int side) {

    /* We're generating transitions between two segments s and s+1. First we call this function
       with (global variable) seg=s, calling the function once with each left analysis i; then we do the same with seg=s+1, going through all the
       right analyses of that segment. */

    int numv, v, maxv;
    v=-1;
    /* printf("generating transitions...\n"); */

    maxv=segment[seg].analcard[i];
    /* printf("Left transitions for segment %d, analysis %d:\n", seg, a); */
    t=0;
    for(numv=0; numv<=maxv; numv++) {
	generate_transition_sides(i, -1, numv, v, side);
    }
    if(verbosity >= 2) System.out.printf("%d ", t);
    if(side==0)ltnum[i]=t;
    if(side==1)rtnum[i]=t;
}

static double fp_poisson(double c, int k) {

    int f=1, k2;

    for(k2=2; k2<=k; k2++) f *= k;
    /* ^ if k=0, f will be 1 (as it should be) */

    return pow(c, (double)(k)) * exp(-c) / (double)(f);
}

static double prox_penalty(int diff) {

    /* diff is the interval between two pitches (may be positive or negative) */
    double pvar = 10.0;
    return (exp( -pow( (double)(diff), 2.0) / (2.0 * pvar))) / (2.51 * sqrt(pvar));
}

static double prox_score(int s, int i, int lt, int j, int rt) {

    int v, diff, p;
    double score=0.0;
    for(v=0; v<ltranscard[i][lt]; v++) {
	p = rtransition[j][rt][v];
	if(segment[s].column[p] == 3) {
	    diff = rtransition[j][rt][v] - ltransition[i][lt][v];
	    score += log(prox_penalty(diff));
	}
    }
    return score;
}


static double note_end_score(int s, int i, int lt, int j, int rt, int mode) {

    /* s is the segment after the transition */
    int v, p, covered;
    double score=0.0;

    /* Add 1-fp_note_cont for each voice on a blue or black square that goes to a grey square */
    for(v=0; v<ltranscard[i][lt]; v++) {
	if(ltransition[i][lt][v] != rtransition[j][rt][v]) continue;
	p = ltransition[i][lt][v];
	if(segment[s-1].column[p]>=2 && segment[s].column[p]==1) {
	    score += log(1.0 - fp_note_cont);
	}
    }

    /* If (s-1, p) is black or blue and is not covered by the transition, it's the last note of a stream; if (s, p) is grey or white,
       add 1-fp_last_note_cont. */
    for(p=0; p<1000; p++) { //100
	if(segment[s-1].column[p] >= 2 && segment[s].column[p] < 2) {
	    covered=0;
	    for(v=0; v<ltranscard[i][lt]; v++) {
		if(ltransition[i][lt][v]==p) covered=1;
	    }
	    if(covered==0) {
		score += log(1.0-fp_last_note_cont);
		if(mode==1) System.out.printf("penalty for p = %d; ", p);
	    }
	}
    }

    return score;
}

static void evaluate_transitions(int i, int j) {

    /* We're taking analysis i of segment seg-1 and analysis j of segment seg, and finding the best transition between them. */

    int lt, rt, v, diff, good, no_good, vv, p, covered, tcard;
    double score, best_score, new_stream_score, stream_cont_score;

    no_good=0;
    for(v=0; v<segment[seg].analcard[j]; v++) {            /* If any of the voices in the right analysis has column value
							      <3, then there must be a voice in the left analysis at
							      the same pitch in order for there to be a valid transition */
	p=segment[seg].analysis[j][v];
	if(segment[seg].column[p]<3) {
	    good=0;
	    for(vv=0; vv<segment[seg-1].analcard[i]; vv++) {
		if(segment[seg-1].analysis[i][vv]==p) good=1;
	    }
	    if(good==0) no_good=1;
	}
    }
    if (no_good==1) {
	best_transition_score = -1000000.0;
    }

    else {

	/* If the above test is passed: look at each pair of transitions from that pair of analyses. */

	best_score = -1000000.0;
	for(lt=0; lt<ltnum[i]; lt++) {	               /* ltnum[i] is the number of transitions found for left-analysis i */
	    for(rt=0; rt<rtnum[j]; rt++) {
		no_good=0;

		if(ltranscard[i][lt]!=rtranscard[j][rt]) continue;
		tcard = ltranscard[i][lt];

		for(v=0; v<tcard; v++) {
		    /* If one of the voices in the right transition has column value < 3, then the same voice must be at that pitch
		       in the left transition. (You can't jump to a blue or grey square.) */
		    if(segment[seg].column[rtransition[j][rt][v]]<3 && ltransition[i][lt][v]!=rtransition[j][rt][v]) no_good=1;
		    /* If one of the left transition voices is followed by a blue square at the same pitch, the voice must stay there.
		       (You can't move to avoid a blue square.) */
		    if(segment[seg].column[ltransition[i][lt][v]]==2 && ltransition[i][lt][v]!=rtransition[j][rt][v]) no_good=1;
		}
		if(no_good==1) continue;

		/* printf("Possible transition at segment %d: a %d, t %d, to a %d, t %d. ", seg, la, lt, ra, rt); */
		score=0.0;

		new_stream_score = log(fp_poisson(fp_stream_prob, (segment[seg].analcard[j] - tcard)));

		/* Add on log(fp_stream_cont) for each stream that is continuing; add on log(1-fpstream_cont) for each stream that is not. */
		stream_cont_score = (tcard * log(fp_stream_cont)) + ((segment[seg-1].analcard[i] - tcard) * log(1.0-fp_stream_cont));

		score += new_stream_score + stream_cont_score + prox_score(seg, i, lt, j, rt) + note_end_score(seg, i, lt, j, rt, 0);

		/* printf("Score: %5.3f\n", score); */
		if (score > best_score) {
		    //if(seg==2) printf("Got here, old best_score = %5.3f, new score = %5.3f\n", best_score, score);
		    best_score=score;
		    best_lt=lt;
		    best_rt=rt;
		}
	    }
	}
	//if(seg==2) printf("best transition = %d, score = %5.3f\n", best_rt, best_score);

	best_transition_score=best_score;
	best_transcard = 0;                              /* If there are no active voices, there will be no good transitions, so best_transcard should be 0 */
	for(v=0; v<ltranscard[i][best_lt]; v++) {
	    best_rtransition[v]=rtransition[j][best_rt][v];
	    best_ltransition[v]=ltransition[i][best_lt][v];
	    best_transcard=ltranscard[i][best_lt];
	}
	/* printf("Best transition at segment %d, la=%d, ra=%d, with score %5.3f: ", seg, i, j, best_transition_score);
	   for(v=0; v<best_transcard; v++) {
	   printf("%d->%d ", best_ltransition[v], best_rtransition[v]);
	   }
	   printf("\n");  */
    }
}

static void analyze_piece() {

    /* The possible column analyses for all segments have already been generated. Now we go through
       and build the dynamic programming table. In the process, we generate transitions and find the
       best transition for each analysis pair. */

    int i, j, localv, a, v;
    double bestscore, current_score;

    localv = 0;

    /* First we do a special version of the search for segment 1 only */

    if(verbosity >= 2) System.out.printf("\nSegment 1 L:");
    seg=0;
    for(i=0; i<segment[0].numanal; i++) {
	find_transitions(i, 0);
    }
    if(verbosity >= 2) System.out.printf("\nSegment 1 R:");
    seg=1;
    for(j=0; j<segment[1].numanal; j++) {
	find_transitions(j, 1);
    }

    for(j=0; j<segment[1].numanal; j++) {
	bestscore = -1000000.0;
	for(i=0; i<segment[0].numanal; i++) {
	    evaluate_transitions(i,j);
	    current_score = segment[0].analscore[i] + segment[1].analscore[j] + best_transition_score + log(fp_poisson(firstseg_stream_prob, (segment[0].analcard[i])));
	    if(current_score > bestscore) {
		bestscore = current_score;
		best[1][j]=i;
		/* printf("The best i for j=%d is %d with score=%5.3f\n", j, best[1][j], bestscore); */
	    }
	}
	global_analysis[1][j]=bestscore;
    }

    /* Now we do the search for all subsequent segments */

    for(seg=2; seg<=segtotal; seg++) {
	if(verbosity >= 2) System.out.printf("\nSegment %d L:", seg);
	seg--;
	for(i=0; i<segment[seg].numanal; i++) {
	    find_transitions(i, 0);
	}
	seg++;
	if(verbosity >=2) System.out.printf("\nSegment %d R:", seg);
	for(j=0; j<segment[seg].numanal; j++) {
	    find_transitions(j, 1);
	}
	for(j=0; j<segment[seg].numanal; j++) {
	    bestscore = -1000000.0;
	    for(i=0; i<segment[seg-1].numanal; i++) {
		evaluate_transitions(i,j);
		if(global_analysis[seg-1][i] + segment[seg].analscore[j] + best_transition_score > bestscore) {
		    bestscore = global_analysis[seg-1][i] + segment[seg].analscore[j] + best_transition_score;
		    best[seg][j]=i;
		}
	    }
	    global_analysis[seg][j]=bestscore;
	}
    }
    if(verbosity >=2) System.out.printf("\n");

    /* Now we find the global analysis in the final segment with the best score; we trace that back,
       choosing the best analysis ("final[seg]") for each segment. (We have also have to generate and
       evaluate transitions again, but only the ones for the chosen analyses of each segment). */

    bestscore = -1000000.0;
    //System.out.println(segtotal);
    for(j=0; j<segment[segtotal].numanal; j++) {
	if(global_analysis[segtotal][j] > bestscore) {
	    bestscore=global_analysis[segtotal][j];
	    final_[segtotal]=j;
	}
    }
    for(seg=segtotal-1; seg>=0; seg--) {
	final_[seg]=best[seg+1][final_[seg+1]];
    }

    /* Print out final analysis */

    for(seg=0; seg<=segtotal; seg++) {

	if(localv >= 2) {
	    System.out.printf("Segment %d: ", seg);
	    if(segment[seg].numanal==0) {
		System.out.printf("No legal analyses found at this segment. ");
		continue;
	    }
	    System.out.printf(" Analysis %d: ", final_[seg]);
	    for(v=0; v<segment[seg].analcard[final_[seg]]; v++) {
		System.out.printf("%d ", segment[seg].analysis[final_[seg]][v]);
	    }
	    System.out.printf("\n");
	}

	if(seg<segtotal) {
	    find_transitions(final_[seg], 0);
	    seg++;
	    find_transitions(final_[seg], 1);
	    evaluate_transitions(final_[seg-1], final_[seg]);
	    for(v=0; v<best_transcard; v++) {
		final_ltransition[seg][v]=best_ltransition[v];
		final_rtransition[seg][v]=best_rtransition[v];
	    }
	    final_transcard[seg]=best_transcard;
	    seg--;
	}

	if(localv >= 2) {

	    a = final_[seg];

	    if(seg==0) System.out.printf("    initial stream score = %5.3f\n", log(fp_poisson(firstseg_stream_prob, (segment[seg].analcard[a]))));
	    System.out.printf("    local score = %5.3f: ", segment[seg].analscore[final_[seg]]);
	    System.out.printf("onset score = %5.3f; no-onset score = %5.3f; note cont score = %5.3f; ", (log(fp_note_prob) * segment[seg].analblack[a]),
		   (log(1.0-fp_note_prob) * (segment[seg].analcard[a]-segment[seg].analblack[a])), (log(fp_note_cont) * segment[seg].analblue[a]));
	    System.out.printf("last note cont score = %5.3f;\n", log(fp_last_note_cont) * (segment[seg].numblue - segment[seg].analblue[a]));
	    if(seg>0) System.out.printf("    global score so far: %5.3f\n", global_analysis[seg][final_[seg]]);

	    if(seg<segtotal) {
		System.out.printf("  Transitions: ");
		for(v=0; v<best_transcard; v++) {
		    System.out.printf("%d -> ", final_ltransition[seg+1][v]);
		    System.out.printf("%d; ", final_rtransition[seg+1][v]);
		}
		System.out.printf("\n");
		System.out.printf("    transition score: %5.3f: ", best_transition_score);
		System.out.printf("stream cont = %5.3f; ", (best_transcard * log(fp_stream_cont)) + ((segment[seg].analcard[a] - best_transcard) * log(1.0-fp_stream_cont)));
		System.out.printf("new streams = %5.3f; ", log(fp_poisson(fp_stream_prob, (segment[seg+1].analcard[final_[seg+1]] - best_transcard))));
		System.out.printf("prox score = %5.3f; ", prox_score(seg+1, final_[seg], best_lt, final_[seg+1], best_rt));
		System.out.printf("note end score = %5.3f; ", note_end_score(seg+1, final_[seg], best_lt, final_[seg+1], best_rt, 1));
	    }

	    System.out.printf("\n");
	}

    }
}

static void assign_voice_numbers() {

    int v=1, i, j, t, tt, set, z, f;
    for(i=0; i<segment[0].analcard[final_[0]]; i++) {
	segment[0].voice_number[i]=v;
	v++;
    }
    for(seg=1; seg<=segtotal; seg++) {
	/* System.out.printf("Segment %d: ", seg); */
	t=0;
	j=0;
	for(i=0; i<segment[seg].analcard[final_[seg]]; i++) {
	    set=0;
	    if (final_rtransition[seg][t] == segment[seg].analysis[final_[seg]][i]) {
                //System.out.println(seg);
		while(true) {
                    //error happens when j gets too big - added j > 10
		    if (j > 10 || final_ltransition[seg][t] == segment[seg-1].analysis[final_[seg-1]][j]) {
			segment[seg].voice_number[i] = segment[seg-1].voice_number[j];
			set=1;
			j++;
			break;
		    }
		    j++;
		}
		t++;
	    }
	    if (set==0) {
		segment[seg].voice_number[i]=v;
		v++;
		//System.out.printf("In seg %d, creating new voice %d\n", seg, segment[seg].voice_number[i]);
	    }
	}
	//System.out.printf("\n"); 

	/* To force voice numbers to a segment, do so here */
	/*    if(seg==690) {
	      segment[seg].voice_number[0]=32;
	      } */
    }

    /* Now each note gets assigned a stream number of the corresponding stream */

    for(z=0; z<numnotes; z++) {
	note[z].stream = 0;               /* First assign all notes to stream 0; only invalid notes will retain this. A note is invalid if (a) it's
					      a "duplicate" note (in which case it's already been marked invalid) or (b) if it was never assigned
					      a place in the analysis; in that case it will be marked invalid now (and not assigned to any stream). */
	if((note[z].valid==0)) continue;
	seg = note[z].segment;
	f = final_[seg];
	for(i=0; i<segment[seg].analcard[f]; i++) {
            //System.out.println(segment[seg].analysis[f][i] + " " + note[z].pitch);
	    if(segment[seg].analysis[f][i] == note[z].pitch) {
		note[z].stream = segment[seg].voice_number[i];
                //System.out.printf("Note %d %d %d %d\n", note[z].ontime, note[z].offtime, note[z].pitch, note[z].stream);
		break;
	    }
	}
	if(note[z].stream == 0) note[z].valid = 0;
    }

}

	static void display_streams() {

		int prev, twodigit, z, nv, collision, p;
		String numstring;
		char display[][] = new char[10000][1001]; //used to be 97

		System.out.printf("                            C1          C2          C3          C4          C5          C6          C7\n");

		for (seg = 0; seg <= segtotal; seg++) {
			for (p = 24; p <= 96; p++) {
				if (p % 12 == 0) {
					display[seg][p] = '.';
				} else {
					display[seg][p] = ' ';
				}
			}
		}

		for (z = 0; z < numnotes; z++) {
			if (note[z].stream == 0) {
				continue;            /* Skip invalid notes */
			}
			seg = note[z].segment;
			p = note[z].pitch;
			numstring = String.valueOf(note[z].stream); //****System.out.sprintf(numstring, "%d", note[z].stream);
			if (note[z].stream < 10) {
				display[seg][p] = numstring.charAt(0);//strncpy(&  display[seg][p], numstring, 1);
			} else {
				display[seg][p] = numstring.charAt(0);//strncpy(&  display[seg][p], numstring, 2);
				display[seg][p+1] = numstring.charAt(1);
			}

			for (seg = note[z].segment + 1; seg <= segtotal; seg++) {
				if (note[z].offtime > segment[seg].start) {
					display[seg][p] = '|';
				}
			}
		}
		for (seg = 0; seg <= segtotal; seg++) {
			System.out.printf("Seg %4d: (%5d)", seg, segment[seg].start);
			for (p = 24; p <= 96; p++) {
				System.out.printf("%c", display[seg][p]);
			}
			System.out.printf("\n");
		}

	}


static void  create_streams() {

    fill_segments();
    create_snotes();
    //print_segments();
    column_analyses();
    if (segtotal <= 0)
        return;
    analyze_piece();
    assign_voice_numbers();

}

}
