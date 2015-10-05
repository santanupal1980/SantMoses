import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 *
 */

/**
 * @authorSantanu
 * This code produce Phrase extraction and produce extract.sorted.direct
 * the algorithm is same like moses
 * for more details please contact me santanu.pal@uni-saarland.de
 *
 *
 */

public class PhraseExtraction {

	ArrayList<PhraseExtraction.Sentence> bilingalCorpus = new ArrayList<PhraseExtraction.Sentence>();
	
	public PhraseExtraction() {
		// TODO Auto-generated constructor stub
	}
	
	public class Sentence{
		List<String> source = new ArrayList<String>();
		List<String> target =new ArrayList<String>();
		HashSet<Integer> sourceIndex = new HashSet<Integer>();
		HashSet<Integer> targetIndex = new HashSet<Integer>();;
		//Vector<Integer> alignedCountS;
		//Vector<Vector<Integer> > alignedToT;
		List<Pair<Integer, Integer>> WordAlignment= new ArrayList<Pair<Integer,Integer>>();
			
			
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
				
				s.source=Arrays.asList(str1.split(" "));
				s.target=Arrays.asList(str2.split(" "));;
				s.sentenceID=x;
				String ss[]=str3.split(" ");
				//s.alignedCountS=new Vector<Integer>(ss.length);
				for(int i=0; i<s.source.size(); i++) {
					//System.out.println(s.sentenceID+"\t"+s.source.get(i));
				    //s.alignedCountS.add( 0 );
				  }
				for(int i=0;i<ss.length;i++){
					String al[]=ss[i].split("-");
		        	int t_a= Integer.parseInt(al[1]);
		        	int s_a = Integer.parseInt(al[0]);
		        	//System.out.println(s_a+"\t"+t_a);
		        	Pair<Integer, Integer> p = new Pair<Integer, Integer>(s_a, t_a);
		        	//p.setFirst(s_a);
		        	//p.setFirst(t_a);
		        	s.WordAlignment.add(p);
		        	/*Vector<Integer> p=new Vector<>();
		        	p.add(s_a);
		        	s.alignedToT.add(t_a,p);//.add(s_a);
		            s.alignedCountS.add(s_a);
		            */
				}
				  
				bilingalCorpus.add(s);
				x++;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println(ex.toString());
		}
	}
	
	public void extract_word_alignment(Sentence S){
	    /*
	    This function will extract the position of word alignment(WA) into two list.
	    list_e will contain all of the position of 'e' that is used in WA
	    list_f will contain all of the position of 'f' that is used in WA
	    
	    input:
	    word alignment
	    */
	    

	    //extract e and f from word alignment
		//Pair<Integer,Integer> p = new Pair<Integer, Integer>();
		//System.out.println(S.WordAlignment.size());
	    for (Pair<Integer,Integer> p : S.WordAlignment){
	    	int e = p.getFirst();
	    	int f= p.getSecond();
	    	if (!S.sourceIndex.contains(e))
	            	S.sourceIndex.add(e);

	    	if (!S.targetIndex.contains(f))
            	S.targetIndex.add(f);

	    }
	     System.out.println("successfully run extract_word_alignment "+S.sourceIndex.size()+"\t"+S.targetIndex.size());   
	}
	
	public  HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> 
	extract(Sentence S, int f_start, int f_end, int e_start, int e_end, int len_f){
	
	   /*
	    This function will find the minimal foreign phrase that matches and 
	    consistent with the word alignment. The consistency is checked by 
	    examining each word alignment in the possible pair of english and
	    foreign phrase.
	    input:
	    f_start = start position of foreign word
	    f_end = end position of foreign word
	    e_start = start position of english word
	    e_end = end position of english word
	    len_f = length of sentence f
	    output:
	    phrase_pair_position = list of phrase pair position (start to end)
	    */
	    HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> phrase_pair_position 
	    = new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();//<String>();
	   // if the position of f_end is -1
	    if (f_end == -1)
	        return new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();
	    
	    //check the consistency 
	    
	    for (Pair<Integer,Integer> p : S.WordAlignment){
	    	int e = p.getFirst();
	    	int f= p.getSecond();
	    	//System.out.println(e+"\t"+f);
	    	//each of word alignment is inside the phrase range
	    	if ((e_start <= e) && (e <= e_end) && (f_start <= f) && (f<= f_end))
	            continue;
	    	//each of word alignment is outside the phrase range
	    	else if ((e > e_end || e < e_start) && (f > f_end || f < f_start))
	            continue;
	    	//'f' position outside the phrase range
	    	else if( ((e_start <= e && e <= e_end))  && (f > f_end || f < f_start))
	            return new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();
	        //'e' position outside the phrase range
	        else if ((e > e_end || e < e_start) && (f_start <= f && f <= f_end))
	            return new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();
	    }
	    int fs = f_start;
	    //check the possible phrase pair for unaligned words
	    //do until position of fs inside of word alignment
	    while(true){
	    	 while(true){
		    	int fe=f_end;
		    	phrase_pair_position.put(new Pair(e_start, e_end),new Pair(fs, fe));
	            fe += 1;
	            System.out.println(fs+"\t"+fe);
	            //check fe position in word alignment or
	            //its position index > length of foreign sentence - 1
	            if (S.sourceIndex.contains(fe) || (fe > (len_f-1)))
            		break;
	    	 }
	    	 fs -= 1;

	    	 //check fs position in word alignment or its position index < 0
	    	 if(S.sourceIndex.contains(fs) || (fs <0))//fs in list_f or fs < 0:
	                break;
	    }
	     return phrase_pair_position;

	}
	
	public void print_phrase_pair(Sentence S, HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>phrase_pair_position_list){
	    /*
	    This function will generate a phrase pair from a list of phrase position
	    Each pair of english and foreign phrase are separated by '-' 
	    input: 
	    words_e = list of english words
	    words_f = list of foreign words
	    phrase_pair_position_list: list of phrase position from start to end
	    output: 
	    list of phrase pair such as
	    >>> phrase_pair_position_list = [[(0, 0), (0, 0)], [(0, 1), (0, 3)]]
	    >>> words_e = ['michael', 'assumes', 'that', 'he', 'will', 'stay', 'in', 'the', 'house']
	    >>> words_f = ['michael', 'geht', 'davon', 'aus', ',', 'dass', 'er', 'im', 'haus', 'bleibt']
	    >>> print_phrase_pair(words_e, words_f, phrase_pair_position_list)
	    ['michael - michael', 'michael assumes - michael geht davon aus']
	    */
	    

	    for( Pair pair : phrase_pair_position_list.keySet()){
	        String phrase_e ="";
	        String phrase_f ="";

	        //english range 
	        int start = (int) pair.getFirst();;
	        int end = (int) pair.getSecond();;
	        for (int i = start;i<end+1;i++){
	            phrase_e = phrase_e + " " + S.source.get(i);
	        }

	        //foreign range
	        Pair pair1 = phrase_pair_position_list.get(pair);
	        start = (int) pair1.getFirst();;
	        end = (int) pair1.getSecond();;
	        for (int i = start;i<end+1;i++){
	            phrase_f = phrase_f + " " + S.target.get(i);
	        }
	        
	        System.out.println(phrase_e +" ||| "+ phrase_f);
	    }
	}
	
	public void find_phrase(Sentence S){
	    /*
	    This function will find all possible of english phrase and foreign phrase 
	    given these 3 inputs:
	    e: english sentence
	    f: foreign sentenvce
	    wa: list of word alignment based on e and f
	    After get all of the possibility of english and foreign phrase, it will
	    call extract function to check its consistency. In the end, it will call
	    print_phrase_apir function to generate the pair or english and foreign 
	    phrase
	    output: 
	    list of english and foreign pair
	    */
	    

	    //updates to global variable
		this.extract_word_alignment(S);
	    List<Pair<Integer, Integer>>word_alignment = S.WordAlignment;

	    HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> phrase_pair_position_list 
	    = new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();

	    //extract a sentence into a list of words
	    //words_e = e.split(" ") #list of word from english sentence
	    //words_f = f.split(" ") #list of word from foreign sentence

	    //extract the position
	    
	    //loop
	    int len_s=S.source.size();
	    int len_t=S.target.size();
	    System.out.println("Find phrase......");
	    for (int e_start=0; e_start< len_s; e_start++){
	        for (int e_end =e_start; e_end<len_s;e_end++){
	            int f_start = len_t-1;
	            int f_end = -1;
	            for (Pair<Integer, Integer> pair : word_alignment){
	            	int e = pair.getFirst();
	            	int f = pair.getSecond();
	            	if (e_start <= e && e <= e_end){
	                    f_start = Math.min(f, f_start);
	                    f_end = Math.max(f, f_end);
	            	}
	            }
	            
	            System.out.println("Extraction begin....");

	            //find the minimal foreign phrase
	            HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> phrase_pair_position
	            = this.extract(S,f_start, f_end, e_start, e_end, len_t);
	            System.out.println("Extraction end....");
	            //add into phrase_pair list
	            if (phrase_pair_position.size()>0){
	            	System.out.println(phrase_pair_position.keySet());
	                for (Pair<Integer, Integer> i : phrase_pair_position.keySet()){
	                    if (!phrase_pair_position_list.containsKey(i)) 
	                        phrase_pair_position_list.put(i, phrase_pair_position.get(i));
	                }
	            }
	        }
	    }
	      System.out.println("printing....");     
	    //generate phrase pair from 
	    this.print_phrase_pair(S, phrase_pair_position_list);
	}
	
	public void runExtract(){
		for(int i=0;i<bilingalCorpus.size();i++){
			Sentence S = bilingalCorpus.get(i);
			//this.extract_word_alignment(S);
			this.find_phrase(S);
		}
	}
	
	public static void main(String args[]){
		PhraseExtraction pe = new PhraseExtraction();
		pe.loadBilingualCorpus("data/train.en", "data/train.bn", "model/aligned.grow-diag-final-and");
		System.out.println("End loading...");
		pe.runExtract();
	}
}