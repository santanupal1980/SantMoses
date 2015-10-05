import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * 
 */

/**
 * @authorSantanu
 * This is faster than PhraseScoring.java and moses scoring also
 * This code takes model directory path provided by Moses tool kit
 * the program provides only lexical phrase translation table in moses format
 * it takes following input files extract.direct.sorted, extarct.inv.sorted, lex.e2f and lex.f2e 
 * for more details please contact me santanu.pal@uni-saarland.de
 * 
 ==============
 The difference with updateMosesPhraseTable.java is that the imput here it produce custom phrase table
 * This code produce phrase table score modified with some parameter value
 *  P(traget phrase position| source phrase position)
 * this code need extract1.sorted.procesed.gz (prepared from PostExtract.java), which contains the log of the phrase that
 * from which sentence it was extracted from parallel corpus
 *
 */
public class ScoreModified {

	/**
	 * 
	 */

	HashMap<String, HashMap<String, String>> dir_extract = new HashMap<String, HashMap<String, String>>();
	HashMap<String, HashMap<String, String>> inv_extract = new HashMap<String, HashMap<String, String>>();
	HashMap<String, Double> lex_ef = new HashMap<String, Double>();
	HashMap<String, Double> lex_fe = new HashMap<String, Double>();
	
	List<String> alignment = new ArrayList<String>();

	public void loadAlignmentFile() {

	}

	public ScoreModified() {
		
	}
	public ScoreModified(String path) {
		// TODO Auto-generated constructor stub

		//String path = "/root/workspace/PhraseExtraction/model/";
		this.load_resources(path + "extract1.sorted.processed.gz", path + "extract.inv.sorted.gz", path
				+ "lex.f2e", path + "lex.e2f");
	}

	public void load_resources(String extract, String invExtract,
			String lexf2e, String lexe2f) {

		try {
			GZIPInputStream in = new GZIPInputStream(new FileInputStream(extract));
			BufferedReader rd = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String strLine = "";
			while ((strLine = rd.readLine()) != null) {
				if (strLine.trim().length() > 0) {
					strLine=strLine.replace("\t", "@@@").replace(" ||| ", "@@@");
					//strLine=strLine.replaceAll("  *", " ");
					String ss[]=strLine.split("@@@");
					// System.out.println("::"+strLine);
					HashMap<String, String>innerMap=dir_extract.get(ss[0]);
			        if (innerMap==null) {
			            innerMap = new HashMap<String,String>();
			            dir_extract.put(ss[0],innerMap);
			        }
			        innerMap.put(ss[1],ss[2]+"\t"+ss[4]+"\t"+ss[5]);
					dir_extract.put(ss[0],innerMap);
				}
			}
			rd.close();
			in.close();

			in = new GZIPInputStream(new FileInputStream(invExtract));
			rd = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			strLine = "";
			while ((strLine = rd.readLine()) != null) {
				if (strLine.trim().length() > 0) {
					strLine=strLine.replace(" ||| ", "@@@");
					//strLine=strLine.replaceAll("  *", " ");
					String ss[]=strLine.split("@@@");
					HashMap<String, String>innerMap=inv_extract.get(ss[0]);
			        if (innerMap==null) {
			            innerMap = new HashMap<String,String>();
			            inv_extract.put(ss[0],innerMap);
			        }
			        innerMap.put(ss[1],ss[2]);
					inv_extract.put(ss[0],innerMap);
				}
			}
			rd.close();
			in.close();
//===============================Direct
			//f2e
			this.loadLexF2e("model/lex.f2e");
			this.loadLexE2f("model/lex.e2f"); // moses follows opposite
//====================================Inverse
			//e2f

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex);
		}

	}

	public void calculateNow() {
		try{
		double inv_phrase_translation_probability;
		double dir_phrase_translation_probability;
		double inv_lexical_weighting=1.0;
		double dir_lexical_weighting = 1.0;
		
		ArrayList<String> out = new ArrayList<String>();
		BufferedWriter w = null;
		FileOutputStream o = new FileOutputStream("phrasetable.combined");
		w = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"));
		
		for (String key: dir_extract.keySet()) {
			// id
			HashMap<String, String>innerMap=dir_extract.get(key);
			for (String key1: innerMap.keySet()) {
				// id
			//String extChk = key+"\t"+dir_extract.get(key).trim();
			//	System.out.println(innerMap.get(key1).trim());
			String extractValue[]=innerMap.get(key1).trim().split("\t");
			String dirAlmnt = extractValue[0].trim();
			double stRelPosProb = Double.parseDouble(extractValue[1]);
			double tsRelPosProb = Double.parseDouble(extractValue[2]);
			
			HashMap<String, String>innerMap_inv=inv_extract.get(key1);
			
			//String invExtChk = key2+"\t"+inv_extract.get(key2).trim();
			String invAlmnt = innerMap_inv.get(key);
			//System.out.println(extChk +"\t"+invExtChk);
			String ephrase = key.trim();
			String bphrase = key1.trim();
			//System.out.println(key +"\t"+key1+"\t"+invAlmnt);
			inv_lexical_weighting = this.inverselexicaLWeighting(bphrase, ephrase, invAlmnt);
			// System.out.println(extChk+"\t"+inv_lexical_weighting);
			int occurEF=1;
			
			
			
			dir_phrase_translation_probability=(double)occurEF/(double)innerMap.size();
			//dir_phrase_translation_probability = dir_phrase_translation_probability* 
			//System.out.println(ephrase +"\t"+ bphrase+"\t"+occurEF +"\t"+occurE);
			occurEF=1;
			
			
			//System.out.println(bphrase +"\t"+ ephrase+"\t"+occurEF +"\t"+occurE);
			inv_phrase_translation_probability=(double)occurEF/(double)innerMap_inv.size();
			// foreign
			dir_lexical_weighting = this.directLexicalWeighting(ephrase, bphrase, dirAlmnt);

			out.add(ephrase+ " ||| " +bphrase+ " ||| " +inv_phrase_translation_probability*stRelPosProb
					+" "+ dir_lexical_weighting+" "+dir_phrase_translation_probability*tsRelPosProb + " " + inv_lexical_weighting+" ||| "+dirAlmnt+"||| 0 2");
        /*
         The difference with updateMosesPhraseTable.java is that the imput here it produce custom phrase table
         * This code produce phrase table score modified with some parameter value
         *  P(traget phrase position| source phrase position)
         * this code need extract.sorted.procesed.gz, which contains the log of the phrase that
         * from which sentence it was extracted from parallel corpus
                */
			//w.flush();
			}
		}
		Collections.sort(out);
		for(int i=0;i<out.size();i++){
			if(i%1000==0)System.out.print(".");
			if(i%100000==0)System.out.print("|");
			w.write(out.get(i)+"\n");
			w.flush();
		}
		}catch (Exception e) {
			e.printStackTrace();// eTODO: handle exception
		}
	}
	
	public void phraseTranslationProbability(){
		
	}

	public double inverselexicaLWeighting(String tp, String sp, String invAlmnt) {
		

		return this.LexicalWeighting(sp, tp, invAlmnt, lex_fe);
	}

	public double directLexicalWeighting(String sp, String tp, String almnt ) {
		//
		return this.LexicalWeighting(tp, sp, almnt, lex_ef);
	}
	
	public double LexicalWeighting(String sp, String tp, String almnt, HashMap<String, Double> lexP ) {
		//System.out.println(candChk);
		String ePhrase = sp;
		String bPhrase = tp;
		String alignment = almnt;
		String etmpW[] = ePhrase.split(" ");// splitting English phrase
		// collecting from extract.inv
		String btmpW[] = bPhrase.split(" ");// splitting Bengali phrase
		// collecting from extract.inv
		String almt[] = alignment.split(" ");
		
		double inv_lexical_weighting = 1.0;
		

		for (int j = 0; j < btmpW.length; j++) {
			int count = 0;
			double thisWordScore = 0.0;
			for (int a = 0; a < almt.length; a++) {
				String index[] = almt[a].split("-");
				
				String key = btmpW[Integer.parseInt(index[0])]+" "+etmpW[Integer.parseInt(index[1])];
				if(lexP.containsKey(key)&& btmpW[Integer.parseInt(index[0])]
						.equals(btmpW[j])){
					double score = lexP.get(key);
				
					thisWordScore += score;
					//System.out.println(btmpW[Integer.parseInt(index[0])]+"\t"+eword+"\t"+thisWordScore);
					count++;
				}
				
				
				/*for (String lex:lexP.keySet()) {// lexical
					// sumE[w{f(i)/e(j)}]for
					// all i, j belongs
					// to Almt
					
					String ss[] = lex.split(" ");
					String bword = ss[0];
					String eword = ss[1];
					double score = lexP.get(lex);
					
					if (btmpW[Integer.parseInt(index[0])].equals(bword)
							&& btmpW[Integer.parseInt(index[0])]
									.equals(btmpW[j])
							&& etmpW[Integer.parseInt(index[1])].equals(eword)) {
						
						thisWordScore += score;
						//System.out.println(btmpW[Integer.parseInt(index[0])]+"\t"+eword+"\t"+thisWordScore);
						count++;

					}
				}*/
			}
			if (thisWordScore == 0.0) {
				
					// lexical
					// sumE[w{f(i)/e(j)}]for
					// all i, j belongs to
					// Almt NULL alignment calculation
				String key = btmpW[j] +" NULL";
				if(lexP.containsKey(key)){
					double score = lexP.get(key);
					inv_lexical_weighting *= score;
					thisWordScore = 1.0;
				}
				/*for (String lex:lexP.keySet()) {// lexical
					String ss[] = lex.split(" ");
					String bword = ss[0];
					String eword = ss[1];
					double score = lexP.get(lex);
					// System.out.println("================================="+eword+"\t"+score);
					if (btmpW[j].equals(bword) && (eword.equals("NULL"))) {
						inv_lexical_weighting *= score;
						thisWordScore = 1.0;
						
						
					}
				}*/
			}
			
			if (count == 0)
				count = 1;
			inv_lexical_weighting *= thisWordScore / (double) count;
			//System.out.println(btmpW[j]+"|||"+ePhrase+"|||"+thisWordScore+"/"+count+"=="+thisWordScore / (double) count+"\t"+inv_lexical_weighting);
		}

		return inv_lexical_weighting;
		
		
	}
	
	public void readGZFiles(String infile){
		try{
			GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));
			 
			Reader decoder = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(decoder);
			 
			String line;
			while ((line = br.readLine()) != null) {
			    System.out.println(line);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void loadLexF2e(String file){
		try{
			File fileDir = new File(file);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));
			
			String str;
			int x=0,y=0;
			
			while ((str = in.readLine()) != null) {
				
				String ss[]=str.split(" ");
				
				lex_fe.put(ss[0].trim()+" "+ss[1].trim(), Double.parseDouble(ss[2]));
				
				++x;
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void loadLexE2f(String file){
		try{
			File fileDir = new File(file);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));
			
			String str;
			int x=0,y=0;
			
			while ((str = in.readLine()) != null) {
				
				String ss[]=str.split(" ");
				
				lex_ef.put(ss[0].trim()+" "+ss[1].trim(), Double.parseDouble(ss[2]));
				
				++x;
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("loading extract file begin....");
		ScoreModified lc = new ScoreModified("model/");
		//lc.readGZFiles("model/extract.sorted.gz");
		
		System.out.println("loading complete....");
		lc.calculateNow();
	}

}
