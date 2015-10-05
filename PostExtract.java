import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * 
 */

/**
 * @author santanu
 *
 */
public class PostExtract {

	/**
	 * 
	 */
	public PostExtract() {
		// TODO Auto-generated constructor stub
	}
	ArrayList<String>source = new ArrayList<String>();
	ArrayList<String>extract = new ArrayList<String>();
	ArrayList<String>target = new ArrayList<String>();
	HashMap<String,Integer>st = new HashMap<String,Integer>();
	HashMap<String,Integer>s = new 	HashMap<String,Integer>();
	HashMap<String,Integer>t = new 	HashMap<String,Integer>();
	
	public void load_trainingFiles(String src, String tgt){
		try{
			File fileDir = new File(src);
			BufferedReader in1 = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));
			
			File fileDir1 = new File(tgt);
			BufferedReader in2 = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir1), "UTF8"));
			String str1="";
			String str2="";
			String str3="";int x=0;
			while ((str1 = in1.readLine()) != null
					&&(str2 = in2.readLine()) != null) {
				source.add(str1);
				target.add(str2);
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public void process_exatact(String extract_dir_file, String extract_out_file){
		try{
			GZIPInputStream fileDir= new GZIPInputStream(new FileInputStream(extract_dir_file));
			BufferedReader in1 = new BufferedReader(new InputStreamReader(fileDir, "UTF8"));
			
			
			
			BufferedWriter w = null;
			FileOutputStream o = new FileOutputStream(extract_out_file);
			w = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"));
			
			String str1="";
			String str2="";
			String str3="";int x=0;
			while ((str1 = in1.readLine()) != null) {
				//System.out.println(str1+"\t"+str2);
				String ss[] = str1.replace(" ||| ", "@@@").split("@@@");
				String srcStartWord[]=ss[0].split(" ");
				String tgtStartWord[]=ss[1].split(" ");
				int senId = Integer.parseInt(ss[3]);
				List<String> src = new ArrayList<String>();
				src=Arrays.asList(source.get(senId-1).split(" "));
				List<String> tgt = new ArrayList<String>();
				tgt= Arrays.asList(target.get(senId-1).split(" "));//target.get(senId-1);
				//Collections.sort(src);
				int sindex=src.indexOf(srcStartWord[0]);
				int tindex=tgt.indexOf(tgtStartWord[0]);
				//System.out.println(src+"\t"+tgt);
				String joint=sindex+"\t"+tindex+"\t"+src.size()+"\t"+tgt.size()+"\t"+srcStartWord.length+"\t"+tgtStartWord.length;
				if(st.containsKey(joint)){
					st.put(joint, st.get(joint)+1);
				}else st.put(joint, 1);
				
				String givenSrc=sindex+"\t"+src.size()+"\t"+tgt.size()+"\t"+srcStartWord.length;
				if(s.containsKey(givenSrc)){
					s.put(givenSrc, s.get(givenSrc)+1);
				}else s.put(givenSrc, 1);
				
				String givenTgt=tindex+"\t"+src.size()+"\t"+tgt.size()+"\t"+tgtStartWord.length;
				if(t.containsKey(givenTgt)){
					t.put(givenTgt, t.get(givenTgt)+1);
				}else t.put(givenTgt, 1);
				//w.write(sindex+" ||| "+tindex+"\n");
				//w.flush();
				extract.add(str1+"\t"+joint);///******** need to change 
				//if(++x==600)break;
			}
			System.out.println("Writing Processed File.....");
		/*	for(int i=0;i<st.size();i++){
				int joint_occ= Collections.frequency(st, st.get(i));
				int src_occ= Collections.frequency(s, s.get(i));
				int tgt_occ= Collections.frequency(t, t.get(i));
				double relSrcPosProb = joint_occ/(double)src_occ;
				double relTgtPosProb = joint_occ/(double)tgt_occ;
				w.write(extract.get(i)+"\t"+relSrcPosProb+"\t"+relTgtPosProb+"\n");
				w.flush();
			}*/
			System.out.println(extract.size());
			for(int i=0;i<extract.size();i++){
				String ss[]=extract.get(i).split("\t");
				//System.out.println(extract.get(i)+"\t"+ss[0]);
				int joint_occ= st.get(ss[1]+"\t"+ss[2]+"\t"+ss[3]+"\t"+ss[4]+"\t"+ss[5]+"\t"+ss[6]);
				int src_occ= s.get(ss[1]+"\t"+ss[3]+"\t"+ss[4]+"\t"+ss[5]);
				int tgt_occ= t.get(ss[2]+"\t"+ss[3]+"\t"+ss[4]+"\t"+ss[6]);
				double relSrcPosProb = joint_occ/(double)src_occ;
				double relTgtPosProb = joint_occ/(double)tgt_occ;
				System.out.println(ss[1]+"\t"+ss[2]+"\t"+relSrcPosProb+"\t"+relTgtPosProb+"\n");
				w.write(ss[0]+"\t"+relSrcPosProb+"\t"+relTgtPosProb+"\n");
				w.flush();
			}
			System.out.println("Writing Processed File completed.....");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PostExtract pe = new PostExtract();
		pe.load_trainingFiles("data/train.en", "data/train.bn");
		pe.process_exatact("model/extract.sorted.gz", "model/extract1.sorted.processed");
	}

}
