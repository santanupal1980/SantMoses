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
 * 
 * This code takes model directory path provided by Moses tool kit
 * the program provides only lexical phrase translation table in moses format
 * it takes following input files extract.direct.sorted, extarct.inv.sorted, lex.e2f and lex.f2e 
 * for more details please contact me santanu.pal@uni-saarland.de
 * 
 * 
 */
public class PhraseScoring {

	/**
	 * 
	 */

	HashMap<String, String> dir_extract = new HashMap<String, String>();
	HashMap<String, String> inv_extract = new HashMap<String, String>();
	HashMap<String, Double> lex_ef = new HashMap<String, Double>();
	HashMap<String, Double> lex_fe = new HashMap<String, Double>();
	
	List<String> alignment = new ArrayList<String>();

	public void loadAlignmentFile() {

	}

	public PhraseScoring() {
		
	}
	public PhraseScoring(String path) {
		// TODO Auto-generated constructor stub

		//String path = "/root/workspace/PhraseExtraction/model/";
		this.load_resources(path + "extract.sorted", path + "extract.inv.sorted", path
				+ "lex.f2e", path + "lex.e2f");
	}

	public void load_resources(String extract, String invExtract,
			String lexf2e, String lexe2f) {

		try {
			FileInputStream fi = new FileInputStream(extract);
			BufferedReader rd = new BufferedReader(new InputStreamReader(fi,
					"UTF-8"));
			String strLine = "";
			while ((strLine = rd.readLine()) != null) {
				if (strLine.trim().length() > 0) {
					strLine=strLine.replace(" ||| ", "@@@");
					//strLine=strLine.replaceAll("  *", " ");
					String ss[]=strLine.split("@@@");
					 //System.out.println("::"+strLine);
					
					dir_extract.put(ss[0]+"\t"+ss[1], ss[2]);
				}
			}
			rd.close();
			fi.close();

			fi = new FileInputStream(invExtract);
			rd = new BufferedReader(new InputStreamReader(fi, "UTF-8"));
			strLine = "";
			while ((strLine = rd.readLine()) != null) {
				if (strLine.trim().length() > 0) {
					strLine=strLine.replace(" ||| ", "@@@");
					//strLine=strLine.replaceAll("  *", " ");
					String ss[]=strLine.split("@@@");
					 //System.out.println("::"+strLine);
					//System.out.println(ss[0]+"\t"+ss[1]);
					inv_extract.put(ss[0]+"\t"+ss[1], ss[2]);
				}
			}
			rd.close();
			fi.close();
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
		

		BufferedWriter w = null;
		FileOutputStream o = new FileOutputStream("phrasetable.combined");
		w = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"));
		
		for (String key: dir_extract.keySet()) {
			// id
			//String extChk = key+"\t"+dir_extract.get(key).trim();
			String dirAlmnt = dir_extract.get(key).trim();
			String ss[]= key.split("\t");
			String key2 = ss[1].trim()+"\t"+ss[0].trim();
			//String invExtChk = key2+"\t"+inv_extract.get(key2).trim();
			String invAlmnt = inv_extract.get(key2).trim();
			//System.out.println(extChk +"\t"+invExtChk);
			String ephrase = ss[0].trim();
			String bphrase = ss[1].trim();
			
			inv_lexical_weighting = this.inverselexicaLWeighting(bphrase, ephrase, invAlmnt);
			// System.out.println(extChk+"\t"+inv_lexical_weighting);
			int occurEF=0;
			int occurE=0;
			for (String key_i: dir_extract.keySet()) {
				if(key_i.equals(ephrase +"\t"+ bphrase)){
					++occurEF; // joint occurrences
					
				}
				if(key_i.split("\t")[0].equals(ephrase)){
					++occurE; // source occurrences 
					
				}
			}
			dir_phrase_translation_probability=(double)occurEF/(double)occurE;
			//System.out.println(ephrase +"\t"+ bphrase+"\t"+occurEF +"\t"+occurE);
			occurEF=0;
			occurE=0;
			for (String key_i: inv_extract.keySet()) {
				if(key_i.equals(bphrase +"\t"+ ephrase)){
					++occurEF;
					
				}
				if(key_i.split("\t")[0].equals(bphrase)){
					++occurE;
					
				}
			}
			//System.out.println(bphrase +"\t"+ ephrase+"\t"+occurEF +"\t"+occurE);
			inv_phrase_translation_probability=(double)occurEF/(double)occurE;
			// foreign
			dir_lexical_weighting = this.directLexicalWeighting(ephrase, bphrase, dirAlmnt);

			w.write(ephrase+ " ||| " +bphrase+ " ||| " +inv_phrase_translation_probability
					+" "+ dir_lexical_weighting+" "+dir_phrase_translation_probability + " " + inv_lexical_weighting+" 2.718 ||| ||| 0 2\n");
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
		PhraseScoring lc = new PhraseScoring("model/");
		//lc.readGZFiles("model/extract.sorted.gz");
		
		System.out.println("loading complete....");
		lc.calculateNow();
	}

}
