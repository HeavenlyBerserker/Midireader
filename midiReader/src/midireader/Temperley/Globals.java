package midireader.Temperley;

public class Globals {
/* general parameters */

static int verbosity = 0;
static int piplength=50;

/* first-pass streamer parameters - 6 distributions */

static int max_voices = 5;                /* The maximum number of black squares in a segment; also the maximum number of squares in an analysis. */
static int max_grey = 6;                  /* The maximum number of grey squares in a segment */
static int max_collisions = 1;            /* Permit collisions only in analyses with this number of voices or fewer. (If 1, disallow all collisions.) */

static double fp_note_prob = 0.4;                /* The prob of a note-onset in a square */
static double fp_note_cont = 0.8;
static double fp_last_note_cont = 0.5;      /* The P that the last note of a stream will continue into the next square */
static double fp_stream_cont = 0.95;      /* The P that a stream will continue into the next square */
static double fp_stream_prob = .0001;
static double firstseg_stream_prob = 2.0;

/* metharm parameters - 44 distributions */

static int tactus_min=7;
static int tactus_max=24;
                      /*   1   2   3   4   5   6   7     8    9   10   11   12   13   14   15   16   17   18   19   20   21   22    23      24   */
static double init_tactus[] = new double[] {.0, .0, .0, .0, .0, .0, .001, .05, .08, .08, .08, .08, .08, .08, .08, .08, .08, .08, .07, .05, .02, .008, .0009, .0001};
                      /*   8 through 21 is .99, 7 through 22 is .999 */
//double init_tactus[24] = {.0, .0, .0, .0, .0, .0, .001, .05, .14, .14, .14, .12, .10, .08, .06, .05, .04, .03, .02, .01, .01, .008, .0009, .0001};

static double another = 0.95;
static double lower_duple_score = 0.8;
//double lower_duple_score = 1.0;

/*                             un          pre    post   both   */
static double L0_anchor_score[][]= new double[][] {{0.00000001, 0.1}, {0.001, 0.1}};

/*                                   un        post   pre   both */
static double L1_duple_anchor_score[][]= new double[][] {{0.000001, 0.2}, {0.01, 0.4}};

/*                              un-anchored:   00   01        10      11 post-anchored:  00    01     10      11 */
static double L1_triple_anchor_score[][][][]= new double[][][][]
//                                         {{ {{1.0, 0.0001}, {0.0001, 0.0001}},        {{0.5, 0.45}, {0.0001, 0.05}} },
                                           {{ {{1.0, 0.000001}, {0.000001, 0.000001}},        {{0.5, 0.45}, {0.0001, 0.05}} },
/*                              pre-anchored:  00    01      10    11    both-anchored:  00   01     10    11 */
//                                         { {{0.999, 0.001}, {0.005, 0.0001}},             {{0.2, 0.6}, {0.02, 0.18}} }};
                                           { {{0.999, 0.0001}, {0.005, 0.0001}},             {{0.2, 0.6}, {0.02, 0.18}} }};

static double nonbeat_note_score = .00000000001;
//double nonbeat_note_score = 0.0;

static double tactus_reg_score[] = new double[] {0.32, 0.29, 0.05, 0.00001, 0.0000000001};
//double tactus_reg_score[5] = {0.32, 0.29, 0.05, 0.00001, 0.0};

static double lower_reg_score[] = new double[] {0.8, 0.1, 0.0001};    /* lower_reg_score[x] = P of diff of x between intervals. Used for both L0 and L1. */
//double lower_reg_score[3] = {0.6, 0.2, 0.0001};    /* lower_reg_score[x] = P of diff of x between intervals. Used for both L0 and L1. */
static double init_phase_score[] = new double[] {0.6, 0.2, 0.1, 0.0001, 0.0999};

//proximity_profile is set with a normal distribution
static double prox_var = 10.0;
static double harmony_profile[] = new double[] {0.24, 0.02, 0.04, 0.16, 0.18, 0.04, 0.02, 0.18, 0.02, 0.04, 0.04, 0.02};  /* .24 + (2 * .18) + .16 + (4 * .04) + (4 * .02) */
//double harmony_profile[12] = {0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08};

static double raw_tactus_note_score = 0.6;
static double L2_note_score = 0.7;
//double L3_note_score = 0.9;
static double L3_note_score = 0.7;

static double raw_harm_change = 0.6;
static double L2_harm_change = 0.5;
static double L3_harm_change = 0.7;
static double fifths_move = 0.5;     /* The P that, if the harmony changes, it's a move by fifths (up or down - so P(desc. fifth) would be fifths_move / 2 ) */
//double fifths_move = 0.1818;
static double anch_penalty[] = new double[] {1.0, .0001, .0001, 1.0, 1.0, .0001, .0001, 1.0, .0001, .0001, .1, .0001};
static double cont = 0.5;
static double nct_cont = .01;
static double unanch_nct_cont = .0001;

static double stream_prob = 0.1;
static double stream_cont = 0.95;

static public void reset()
	{

verbosity = 0;
 piplength=50;

/* first-pass streamer parameters - 6 distributions */

max_voices = 5;                /* The maximum number of black squares in a segment; also the maximum number of squares in an analysis. */
 max_grey = 6;                  /* The maximum number of grey squares in a segment */
 max_collisions = 1;            /* Permit collisions only in analyses with this number of voices or fewer. (If 1, disallow all collisions.) */

 fp_note_prob = 0.4;                /* The prob of a note-onset in a square */
 fp_note_cont = 0.8;
 fp_last_note_cont = 0.5;      /* The P that the last note of a stream will continue into the next square */
 fp_stream_cont = 0.95;      /* The P that a stream will continue into the next square */
fp_stream_prob = .0001;
 firstseg_stream_prob = 2.0;

/* metharm parameters - 44 distributions */

 tactus_min=7;
 tactus_max=24;
                      /*   1   2   3   4   5   6   7     8    9   10   11   12   13   14   15   16   17   18   19   20   21   22    23      24   */
 init_tactus = new double[] {.0, .0, .0, .0, .0, .0, .001, .05, .08, .08, .08, .08, .08, .08, .08, .08, .08, .08, .07, .05, .02, .008, .0009, .0001};
                      /*   8 through 21 is .99, 7 through 22 is .999 */
//double init_tactus[24] = {.0, .0, .0, .0, .0, .0, .001, .05, .14, .14, .14, .12, .10, .08, .06, .05, .04, .03, .02, .01, .01, .008, .0009, .0001};

 another = 0.95;
 lower_duple_score = 0.8;
//double lower_duple_score = 1.0;

/*                             un          pre    post   both   */
 L0_anchor_score= new double[][] {{0.00000001, 0.1}, {0.001, 0.1}};

/*                                   un        post   pre   both */
L1_duple_anchor_score= new double[][] {{0.000001, 0.2}, {0.01, 0.4}};

/*                              un-anchored:   00   01        10      11 post-anchored:  00    01     10      11 */
 L1_triple_anchor_score= new double[][][][]
//                                         {{ {{1.0, 0.0001}, {0.0001, 0.0001}},        {{0.5, 0.45}, {0.0001, 0.05}} },
                                           {{ {{1.0, 0.000001}, {0.000001, 0.000001}},        {{0.5, 0.45}, {0.0001, 0.05}} },
/*                              pre-anchored:  00    01      10    11    both-anchored:  00   01     10    11 */
//                                         { {{0.999, 0.001}, {0.005, 0.0001}},             {{0.2, 0.6}, {0.02, 0.18}} }};
                                           { {{0.999, 0.0001}, {0.005, 0.0001}},             {{0.2, 0.6}, {0.02, 0.18}} }};

 nonbeat_note_score = .00000000001;
//double nonbeat_note_score = 0.0;

 tactus_reg_score = new double[] {0.32, 0.29, 0.05, 0.00001, 0.0000000001};
//double tactus_reg_score[5] = {0.32, 0.29, 0.05, 0.00001, 0.0};

 lower_reg_score = new double[] {0.8, 0.1, 0.0001};    /* lower_reg_score[x] = P of diff of x between intervals. Used for both L0 and L1. */
//double lower_reg_score[3] = {0.6, 0.2, 0.0001};    /* lower_reg_score[x] = P of diff of x between intervals. Used for both L0 and L1. */
 init_phase_score = new double[] {0.6, 0.2, 0.1, 0.0001, 0.0999};

//proximity_profile is set with a normal distribution
prox_var = 10.0;
 harmony_profile = new double[] {0.24, 0.02, 0.04, 0.16, 0.18, 0.04, 0.02, 0.18, 0.02, 0.04, 0.04, 0.02};  /* .24 + (2 * .18) + .16 + (4 * .04) + (4 * .02) */
//double harmony_profile[12] = {0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08};

raw_tactus_note_score = 0.6;
L2_note_score = 0.7;
//double L3_note_score = 0.9;
 L3_note_score = 0.7;

raw_harm_change = 0.6;
 L2_harm_change = 0.5;
 L3_harm_change = 0.7;
 fifths_move = 0.5;     /* The P that, if the harmony changes, it's a move by fifths (up or down - so P(desc. fifth) would be fifths_move / 2 ) */
//double fifths_move = 0.1818;
 anch_penalty = new double[] {1.0, .0001, .0001, 1.0, 1.0, .0001, .0001, 1.0, .0001, .0001, .1, .0001};
 cont = 0.5;
 nct_cont = .01;
 unanch_nct_cont = .0001;

 stream_prob = 0.1;
 stream_cont = 0.95;
 System.gc();
}

}
