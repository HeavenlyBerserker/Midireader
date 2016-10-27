package midireader.Temperley;

public class Polyph {

/* general parameters */

//int verbosity;
//int piplength;
static double final_score;

/* streamer structures */

static final int MAXV = 10;                   /* The maximum number of squares in an analysis - EVER. This is used to
                                     define the arrays. */
static final int  MA = 1000   ;                /* The maximum number of analyses for a segment */
static final int MT = 256 ;                   /* The maximum number of transitions for an analysis */
static final int MC = 4   ;                   /* The maximum number of voices in an analysis in which collisions are allowed
				   (if the analysis has MC or fewer voices, allow collisions) */
static final int MS = 100000 ;                  /* The maximum number of segments allowed. (WARNING: Setting this
				     number is greater than 1000 may exceed the maximum array
				     size allowed on some UNIX machines. */
//int max_voices;
//int max_grey;
//int max_collisions;
//double fp_note_prob;
//double fp_last_note_cont;
//double fp_note_cont;
//double fp_stream_cont;
//double fp_stream_prob;
//double firstseg_stream_prob;

static char noteword[] = new char[10];
static int display_command;
static int numnotes, numbeats;
static int total_duration;
static int final_timepoint;
static int segment_beat_level;
static double seglength;
static double globseglength;

static note_struct znote[] = new note_struct[100000];     /* Notes as input - not necessarily chronological */
static note_struct note[] = new note_struct[100000];      /* Notes sorted chronologically */

static segment_struct segment[] = new segment_struct[MS+100];        /* An array storing the notes in each segment. */
static int segtotal;              /* total number of segments - 1 */

static int canceled[] = new int[1000];    /* This value is 1 for a pitch if another pitch a half-step or whole-step away has occurred more
			   recently than the pitch itself */

static int prov_analysis[] = new int[MAXV+1];

/* The arrays below are only kept for a given pair of segments. Then the data is just overwritten for the next pair of
   segments. */

static int prov_trans[] = new int[MAXV+1];
static int ltransition[][][] = new int[MA][MT][MAXV];            /* All the transitions for the left side of an analysis pair.
					      [analysis number][transition number][pitches of transition] */

static int ltranscard[][] = new int[MA][MT];                 /* The cardinality of each transition */

static int rtransition[][][] = new int[MA][MT][MAXV];
static int rtranscard[][]= new int[MA][MT];

/* The arrays below keep data for all segments (the entire piece) */

static int best_lt, best_rt;
static double best_transition_score;
static int best_ltransition[] = new int[MAXV];
static int best_rtransition[] = new int[MAXV];
static int best_transcard;

static int seg, t, a;
static int ltnum[] = new int[MA], rtnum[] = new int[MA];                      /* The number of transitions for each analysis */

static int best[][] = new int[MS][MA];                              /* The best prior analysis for each analysis */
static double global_analysis[][] = new double[MS][MA];
static int final_[] = new int[MS*10];
static int final_ltransition[][] = new int[MS*10][MAXV];
static int final_rtransition[][] = new int[MS*10][MAXV];
static int final_transcard[] = new int[MS];

static Subtactus  subt[][];

static Tactus  tactus[][];

static int tp[] = new int[100000];             /* list of tactus pips: tp[0] = first tactus pip, etc. */
static int num_tactus_beats;

static upper_class upper[] = new upper_class[10000];

static Stream streamlist;

static Pip pip[];

static int num_pips;
static int last_offtime;

/* metharm parameters and globals */

static double tactus_profile[][] = new double[40][40];
//static double init_tactus[] = new double[24]; /* 7 through 24 */
//static int tactus_min;
//static int tactus_max;
//static double another;
//static double lower_duple_score;

//static double L0_anchor_score[][] = new double[2][2];
//static double L1_duple_anchor_score[][] = new double[2][2];
//static double L1_triple_anchor_score[][][][] = new double[2][2][2][2];
//static double nonbeat_note_score;

//static double tactus_reg_score[] = new double[5];
//static double lower_reg_score[] = new double[3];
//static double init_phase_score[] = new double[5];

static double proximity_profile[] = new double[1000]; //100
//static double prox_var;
//static double harmony_profile[] = new double[12];

static double adjusted_profile[] = new double[1000]; //100

//static double raw_tactus_note_score;
//static double raw_harm_change;
//static double L2_note_score;
//static double L3_note_score;
//static double L2_harm_change;
//static double L3_harm_change;
//static double fifths_move;
//static double anch_penalty[] = new double[12];
//static double cont, nct_cont, unanch_nct_cont;

// double stream_prob;
//static double stream_cont;

static int first_tactus, last_tactus;
static int bestd, best_ph;

public static void reset()
	{

 streamlist = new Stream();
 noteword = new char[10];
 
 znote = new note_struct[100000];     /* Notes as input - not necessarily chronological */
 note = new note_struct[100000];      /* Notes sorted chronologically */

 segment = new segment_struct[MS+100];        /* An array storing the notes in each segment. */


 canceled = new int[1000];    /* This value is 1 for a pitch if another pitch a half-step or whole-step away has occurred more
			   recently than the pitch itself */

 prov_analysis = new int[MAXV+1];

/* The arrays below are only kept for a given pair of segments. Then the data is just overwritten for the next pair of
   segments. */

prov_trans = new int[MAXV+1];
 ltransition = new int[MA][MT][MAXV];            /* All the transitions for the left side of an analysis pair.
					      [analysis number][transition number][pitches of transition] */

 ltranscard = new int[MA][MT];                 /* The cardinality of each transition */

 rtransition = new int[MA][MT][MAXV];
 rtranscard= new int[MA][MT];

/* The arrays below keep data for all segments (the entire piece) */

 best_ltransition = new int[MAXV];
 best_rtransition = new int[MAXV];

 ltnum = new int[MA]; rtnum = new int[MA];                      /* The number of transitions for each analysis */

best = new int[MS][MA];                              /* The best prior analysis for each analysis */
global_analysis = new double[MS][MA];
final_ = new int[MS*10];
final_ltransition = new int[MS*10][MAXV];
final_rtransition = new int[MS*10][MAXV];
 final_transcard = new int[MS];

tp = new int[100000];             /* list of tactus pips: tp[0] = first tactus pip, etc. */


 upper = new upper_class[10000];



 tactus_profile= new double[40][40];
//static double init_tactus[] = new double[24]; /* 7 through 24 */
//static int tactus_min;
//static int tactus_max;
//static double another;
//static double lower_duple_score;

//static double L0_anchor_score[][] = new double[2][2];
//static double L1_duple_anchor_score[][] = new double[2][2];
//static double L1_triple_anchor_score[][][][] = new double[2][2][2][2];
//static double nonbeat_note_score;

//static double tactus_reg_score[] = new double[5];
//static double lower_reg_score[] = new double[3];
//static double init_phase_score[] = new double[5];

proximity_profile = new double[1000]; //100
//static double prox_var;
//static double harmony_profile[] = new double[12];

 adjusted_profile = new double[1000]; //100

//static double raw_tactus_note_score;
//static double raw_harm_change;
//static double L2_note_score;
//static double L3_note_score;
//static double L2_harm_change;
//static double L3_harm_change;
//static double fifths_move;
//static double anch_penalty[] = new double[12];
//static double cont, nct_cont, unanch_nct_cont;

// double stream_prob;
//static double stream_cont;

}

}

class note_struct {
  int ontime;
  int offtime;
  int duration;
  int pitch;
  int segment;
  int voice_number;
  int stream;
  int valid;
  byte done;
};

/* In what follows, a black square in a segment is a note onset; a blue square is a note continuation; a grey square is one that could be a rest in
   a stream */

class segment_struct {
  int start;
  int end;
  int duration;
  int inote[];
  int snote[];
  int numnotes;        /* number of black, blue, or grey squares in the segment */
  int numdark;         /* number of black or blue squares */
  int numblue;
  int column[];
  int numanal;
  int analysis[][];
  int analcard[];
  int analblack[];
  int analblue[];
  int analgrey[];
  double analscore[];
  int voice_number[];

	segment_struct()
	{
		inote = new int[2000];
		snote = new int[2000];
		column = new int[1000];
		analysis = new int[Polyph.MA][Polyph.MAXV+100];
		analcard = new int[Polyph.MA];
		analblack = new int[Polyph.MA];
		analblue = new int[Polyph.MA];
		analgrey = new int[Polyph.MA];
		analscore = new double[Polyph.MA];
		voice_number = new int[Polyph.MAXV*10];
	}

};


/* metharm structures */

class Snote {

	int pitch;
	int ontime;
	int offtime;
	int stream;
	double hp_score[];
	Snote next;
	Snote prev;

	public Snote() {
		hp_score = new double[12];
	}
};

class Stream {
	int index;
	Snote sn;     /* The first note in the stream */
	Stream next;
	int start, end;        /* Ontimes of first and last notes; set for complete streams only */
	Stream cs; /* for partial streams, a pointer to the complete stream that this is part of */
	int t1, t2;             /* temporary variables indicating note status of start and end of beat interval during search */
};

class Pip {
    Stream stream;
    byte hasnote;
    int next_tactus;
    int besth;
    int ph;
    int final_beatlevel;
    int row[]= new int[1000]; //100
};

class Subtactus {
    int bestc;
    int valid;
    double rscore;
    double pscore[] = new double[12];
};



class Tactus {
    double local_score[][] = new double[2][12];
    byte valid;
    int numnotes;
    byte validh[] = new byte[12];
    int bestk[][] = new int[2][12];
    int besth2[][] = new int[2][12];
    int bestc[] = new int[12];
    int bestc1[] = new int[12];
    int bestc2[] = new int[12];
    double score[][] = new double[2][12];
};



class upper_class {
    double score[][] = new double[5][12];
    int besth2[][] = new int[5][12];
    int besth;
}
