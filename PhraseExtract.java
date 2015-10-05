import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;


/**
 * 
 */

/**
 * @author santanu
 *
 */
public class PhraseExtract {

	// HPhraseVertex represents a point in the alignment matrix
	  public class HPhraseVertex extends Pair<Integer, Integer>{

		public HPhraseVertex(Integer first, Integer second) {
			super(first, second);
			// TODO Auto-generated constructor stub
		}}
	  

	// Phrase represents a bi-phrase; each bi-phrase is defined by two points in the alignment matrix:
	// bottom-left and top-right
	  public class HPhrase extends Pair<HPhraseVertex, HPhraseVertex> {

		public HPhrase(HPhraseVertex first, HPhraseVertex second) {
			super(first, second);
			// TODO Auto-generated constructor stub
		}}

	// HPhraseVector is a vector of HPhrases
	  public class HPhraseVector extends Vector < HPhrase >{}

	// SentenceVertices represents, from all extracted phrases, all vertices that have the same positioning
	// The key of the map is the English index and the value is a set of the source ones
	  public class HSentenceVertices extends HashMap<Integer, Set<Integer> > {
		  public HSentenceVertices() {
			// TODO Auto-generated constructor stub
		}
	  }

	

	
	ArrayList<PhraseExtract.Sentence> bilingalCorpus = new ArrayList<PhraseExtract.Sentence>();
	public PhraseExtract() {
		// TODO Auto-generated constructor stub
	}
	
	public class Sentence{
		String source="";
		String target="";
		Vector<Integer> alignedCountS;
		Vector<Vector<Integer> > alignedToT;
		int sentenceID;
	}
	
	public void loadBilingualCorpus(String src, String tgt, String alt){
		try{
			File fileDir = new File(src);
			BufferedReader in1 = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));
			
			File fileDir1 = new File(tgt);
			BufferedReader in2 = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir1), "UTF8"));
			
			File fileDir2 = new File(alt);
			BufferedReader in3 = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir2), "UTF8"));
			
			String str1="";
			String str2="";
			String str3="";int x=0;
			while ((str1 = in1.readLine()) != null
					&&(str2 = in2.readLine()) != null
					&&(str3 = in3.readLine()) != null) {
				Sentence s = new Sentence();
				s.source=str1;
				s.target=str2;
				s.sentenceID=x;
				String ss[]=str3.split(" ");
				s.alignedCountS=new Vector<Integer>(ss.length);
				for(int i=0; i<s.source.length(); i++) {
				    s.alignedCountS.add( 0 );
				  }
				  for(int i=0; i<s.target.length(); i++) {
				    Vector< Integer > dummy=new Vector<Integer>();
				    s.alignedToT.add( dummy );
				  }

				for(int i=0;i<ss.length;i++){
					String al[]=ss[i].split("-");
		        	int t_a= Integer.parseInt(al[1]);
		        	int s_a = Integer.parseInt(al[0]);
		        	Vector<Integer> p=new Vector<>();
		        	p.add(s_a);
		        	s.alignedToT.add(t_a,p);//.add(s_a);
		            s.alignedCountS.add(s_a);
				}
				bilingalCorpus.add(s);
				x++;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void generalPhraseExtraction(int MaxPhraseLength){
		for(int i=0;i<bilingalCorpus.size();i++){
			this.ExtractPhrasePairs(bilingalCorpus.get(i), MaxPhraseLength);
		}
	}
	
	public void ExtractPhrasePairs(Sentence S,int maxPhraseLength){
		String Source[] =S.source.split(" ");
		String Target[] =S.target.split(" ");
		boolean buildExtraStructure = true;//m_options.isPhraseModel();
		int countE = Target.length;
		int countF = Source.length;
		
		HPhraseVector inboundPhrases;

		HSentenceVertices inTopLeft;
		HSentenceVertices inTopRight;
		HSentenceVertices inBottomLeft;
		HSentenceVertices inBottomRight;

		HSentenceVertices outTopLeft;
		HSentenceVertices outTopRight;
		HSentenceVertices outBottomLeft;
		HSentenceVertices outBottomRight;

		//HSentenceVertices const_iterator it;

		
		ArrayList<Sentence>ExtractedPhrasePairs = new ArrayList<Sentence>();
		
		for(int startE=0; startE<countE; startE++) {
		    for(int endE=startE;(endE<countE && endE<startE+maxPhraseLength);endE++) {

		      int minF = 9999;
		      int maxF = -1;
		     Vector< Integer > usedF = new Vector<Integer>(S.alignedCountS);
		     for(int ei=startE; ei<=endE; ei++) {
		         for(int i=0; i<S.alignedToT.get(ei).size(); i++) {
		           int fi = S.alignedToT.get(ei).get(i);
		           if (fi<minF) {
		             minF = fi;
		           }
		           if (fi>maxF) {
		             maxF = fi;
		           }
		           usedF.remove(fi);
		         }
		       }
		     if (maxF >= 0 && // aligned to any source words at all
		             ( maxF-minF < maxPhraseLength)) { // source phrase within limits

		           // check if source words are aligned to out of bound target words
		           boolean out_of_bounds = false;
		           for(int fi=minF; fi<=maxF && !out_of_bounds; fi++)
		             if (usedF.get(fi)>0) {
		               // cout << "ouf of bounds: " << fi << "\n";
		               out_of_bounds = true;
		             }

		           // cout << "doing if for ( " << minF << "-" << maxF << ", " << startE << "," << endE << ")\n";
		           if (!out_of_bounds) {
		             // start point of source phrase may retreat over unaligned
		             for(int startF=minF;
		                 (startF>=0 &&
		                  (startF>maxF-maxPhraseLength) && // within length limit
		                  (startF==minF || S.alignedCountS.get(startF)==0)); // unaligned
		                 startF--)
		               // end point of source phrase may advance over unaligned
		               for(int endF=maxF;
		                   (endF<countF &&
		                    (endF<startF+maxPhraseLength) && // within length limit
		                    (endF==maxF || S.alignedCountS.get(endF)==0)); // unaligned
		                   endF++) { // at this point we have extracted a phrase
		                 if(buildExtraStructure) { // phrase || hier
		                   //if(endE-startE < maxPhraseLength && endF-startF < maxPhraseLength) { // within limit
		                     //inboundPhrases.push_back(HPhrase(HPhraseVertex(startF,startE),
		                     //                                 HPhraseVertex(endF,endE)));
		                     //insertPhraseVertices(inTopLeft, inTopRight, inBottomLeft, inBottomRight,
		                      //                    startF, startE, endF, endE);
		                  // } else
		                     //insertPhraseVertices(outTopLeft, outTopRight, outBottomLeft, outBottomRight,
		                      //                    startF, startE, endF, endE);
		                 } else {
		                   String orientationInfo = "";
		                   //if(m_options.isWordModel()) {
		                     //REO_POS wordPrevOrient, wordNextOrient;
		                     //bool connectedLeftTopP  = isAligned( S, startF-1, startE-1 );
		                    // bool connectedRightTopP = isAligned( S, endF+1,   startE-1 );
		                     //bool connectedLeftTopN  = isAligned( S, endF+1, endE+1 );
		                     //bool connectedRightTopN = isAligned( S, startF-1,   endE+1 );
		                    // wordPrevOrient = getOrientWordModel(S, m_options.isWordType(), connectedLeftTopP, connectedRightTopP, startF, endF, startE, endE, countF, 0, 1, &ge, &lt);
		                    // wordNextOrient = getOrientWordModel(S, m_options.isWordType(), connectedLeftTopN, connectedRightTopN, endF, startF, endE, startE, 0, countF, -1, &lt, &ge);
		                   //  orientationInfo += getOrientString(wordPrevOrient, m_options.isWordType()) + " " + getOrientString(wordNextOrient, m_options.isWordType());
		                     //if(m_options.isAllModelsOutputFlag())
		                     //  " | | ";
		                   }
		                   //addPhrase(S, startE, endE, startF, endF, orientationInfo);
		                 }
		               }
		           }
		         }
		       }
		     }
   
		          
		          
		    

		
	
	/**
	 * @param args
	 */
	void insertVertex( HSentenceVertices corners, int x, int y )
	{
		/*Set<Integer> tmp;
	  tmp.add(x);
	  Pair<HSentenceVertices,Boolean> ret = corners.put(Pair<Integer, Set<Integer>>(y, tmp));
	  if(ret.getSecond() == false) {
	    ret.setSecond(x);
	  }*/
	}

	void insertPhraseVertices(
	  HSentenceVertices  topLeft,
	  HSentenceVertices  topRight,
	  HSentenceVertices  bottomLeft,
	  HSentenceVertices  bottomRight,
	  int startF, int startE, int endF, int endE)
	{

	  insertVertex(topLeft, startF, startE);
	  insertVertex(topRight, endF, startE);
	  insertVertex(bottomLeft, startF, endE);
	  insertVertex(bottomRight, endF, endE);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
