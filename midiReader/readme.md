###Converting melody/chords in existing music to ragtime

Version 1 algorithm:

Each unique rhythm pattern in the ragpat dataset [Koops et al.] is given a frequency number that is its frequency 
over the total frequency of all patterns with that number of onsets. Patterns are half-measures
represented by strings of 16 I's and O's, which represent onsets and non-onsets, respectivally.

For each unique rhythmic half-measure of the song, a rhythmic pattern from the ragpat dataset is randomly
chosen, weighted by the pattern's frequency numbers. Both of these rhythms form a transformation rule to be applied to each occurrence of the original rhythm in the song.
Because our initial songs are so simple and because un-syncopated half-measures are so common in the dataset, 
rules that would change nothing are rewritten until they would. Additionally, rules that would shift onsets
by a total of 10 or more positions are rewritten to prevent changes too drastic such as a note at the
beginning of a half-measure being shifted to the end.