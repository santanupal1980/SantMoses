# SantMoses: an alternative version of Moses formatted phrase table creation.
This is a log-linear phrase scoring method with more features can be applied.

Linguistic phrases or example based phrases can be incorporated within the phrase table with two steps:

1. Add linguistic phrases/ EBMT phrases with moses phrase extract file or run "PhraseExtraction.java" and add thereafter. (e.g. Developer can produce custom phrases (by his own algorithm) and append with the phrase extracted file (extract.direct and extract.inv file) along with the alignment and training files. This code will produce moses formated phrase table in step 2.)
2. run phrase scoring code to produce phrase table. The output phrase table is the same format as given by Moses toolkit. Run Moses decoder with this output phrase table.


#Each java file has full description how they runs and how they works.

#If you are using this code please cite:

@inproceedings{pal2013impact,
  title={Impact of Linguistically Motivated Shallow Phrases in PB-SMT},
  author={Pal, Santanu and Hasanuzzaman, Mahammed and Naskar, Sudip
Kumar and Bandyopadhyay, Sivaji},
  booktitle={ICON 2013},
  year={2013},

organization={https://www.researchgate.net/publication/263405764\_Impact
\_of\_Linguistically\_Motivated\_Shallow\_Phrases\_in\_PB-SMT}
}
