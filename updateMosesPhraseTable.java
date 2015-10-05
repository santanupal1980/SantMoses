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
 * The difference with ScoreModified.java is that the imput here is moses phrase table
 * This code produce phrase table score modified with some parameter value lambda along with 
 * the the P(traget phrase position| source phrase position)
 * this code need extract.sorted.procesed.gz, which contains the log of the phrase that
 * from which sentence it was extracted from parallel corpus
 * for more details please contact me santanu.pal@uni-saarland.de
 * 
 * 
 */
public class updateMosesPhraseTable {

	/**
	 * 
	 */

	HashMap<String, HashMap<String, String>> dir_extract = new HashMap<String, HashMap<String, String>>();
	HashMap<String, HashMap<String, String>> phrase_table = new HashMap<String, HashMap<String, String>>();
	HashMap<String, Double> lex_ef = new HashMap<String, Double>();
	HashMap<String, Double> lex_fe = new HashMap<String, Double>();
	
	List<String> alignment = new ArrayList<String>();

	public void loadAlignmentFile() {

	}

	public updateMosesPhraseTable() {
		
	}
	public updateMosesPhraseTable(String path) {
		// TODO Auto-generated constructor stub

		//String path = "/root/workspace/PhraseExtraction/model/";
		this.load_resources(path + "extract.sorted.processed.gz", path + "phrase-table.gz");
	}

	public void load_resources(String extract, String phraseTable) {

		try {
			GZIPInputStream in = new GZIPInputStream(new FileInputStream(extract));
			BufferedReader rd = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String strLine = "";
			int x=0;
			while ((strLine = rd.readLine()) != null) {
				if (strLine.trim().length() > 0) {
					strLine=strLine.replace("\t", "@@@").replace(" ||| ", "@@@");
					//strLine=strLine.replaceAll("  *", " ");
					String ss[]=strLine.split("@@@");
					// System.out.println("::"+ss[5]);
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

			in = new GZIPInputStream(new FileInputStream(phraseTable));
			rd = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			strLine = "";
			while ((strLine = rd.readLine()) != null) {
				if (strLine.trim().length() > 0) {
					strLine=strLine.replace(" ||| ", "@@@");
					//strLine=strLine.replaceAll("  *", " ");
					String ss[]=strLine.split("@@@");
					HashMap<String, String>innerMap=phrase_table.get(ss[0]);
			        if (innerMap==null) {
			            innerMap = new HashMap<String,String>();
			            phrase_table.put(ss[0],innerMap);
			        }
			        innerMap.put(ss[1],ss[2]+"\t"+ss[3]+"\t"+ss[4]);
					phrase_table.put(ss[0],innerMap);
				}
			}
			rd.close();
			in.close();
//==========
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
		FileOutputStream o = new FileOutputStream("phrase-table.updated");
		w = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"));
		
		for (String key: phrase_table.keySet()) {
			// id
			HashMap<String, String>innerMap=phrase_table.get(key);
			
			for (String key1: innerMap.keySet()) {
				HashMap<String, String>innerMap_ex=dir_extract.get(key);
				String ex_val[] = innerMap_ex.get(key1).split("\t");
				
				String ptTok[]= innerMap.get(key1).split("\t");
				String score[] = ptTok[0].split(" ");
				double dirPosProb = Double.parseDouble(ex_val[1]);
				double invPosProb = Double.parseDouble(ex_val[2]);
				
				double dirTp=(0.9*Double.parseDouble(score[2])+0.1*dirPosProb);//-Double.parseDouble(score[2])*dirPosProb/(Double.parseDouble(score[2])+dirPosProb);
				double invTp=(0.9*Double.parseDouble(score[0])+0.1*invPosProb);//-Double.parseDouble(score[0])*invPosProb/(Double.parseDouble(score[0])+invPosProb);
				String ptab = invTp+" "+ score[1]+ " "+ dirTp+" "+ score[3]+" ||| "+ptTok[1]+" ||| "+ptTok[2];
				
				out.add(key+" ||| "+key1+"||| "+ptab);
			}
		
		}
		Collections.sort(out);
		for(int i=0;i<out.size();i++){
			if(i%1000==0)System.out.print(".");
			if(i%100000==0)System.out.println("|");
			w.write(out.get(i)+"\n");
			w.flush();
		}
		}catch (Exception e) {
			e.printStackTrace();// eTODO: handle exception
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.err.println("loading extract file begin....");
		updateMosesPhraseTable lc = new updateMosesPhraseTable("model2/");
		//lc.readGZFiles("model/extract.sorted.gz");
		
		System.err.println("loading complete....");
		lc.calculateNow();
	}

}
