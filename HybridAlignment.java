/**
 * 
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author santanu
 *
 */
public class HybridAlignment {

	/**
	 * 
	 */
	ArrayList<String>MeteorAl = new ArrayList<String>();
	ArrayList<String>BaAl = new ArrayList<String>();
	
	public HybridAlignment() {
		// TODO Auto-generated constructor stub
	}

	public void readMeteorAlignment(String ifile){
		try{
			File fileDir = new File(ifile);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));
			
			

			String str;
			int x=0,f=0,line=0;
			String hyp="";
			String ref="";
			String alignment="";
			while ((str = in.readLine()) != null) {
				
				if(str.startsWith("Alignment")){
					hyp = in.readLine();
					ref = in.readLine();
				}else{
					String hyp_tok[]=hyp.split(" ");
					String ref_tok[]=ref.split(" ");
					if(str.contains("\t\t\t")){
						String ss[]=str.split("\t\t\t");
						String ss_hyp[]=ss[0].split(":");
						String ss_ref[]=ss[1].split(":");
						int hi = Integer.parseInt(ss_hyp[0]);
						int ri = Integer.parseInt(ss_ref[0]);
						//System.out.println(hyp_tok[ri]+"\t"+ref_tok[hi]);
						alignment+=(ri+"-"+hi+" ");
					}
					f=1;
				}
				if(str.length()==0){
					MeteorAl.add(alignment);
					alignment="";
					x=0;f=0;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void loadBAAlignment(String ifile){
		try{
			File fileDir = new File(ifile);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));
			
			String str;
			while ((str = in.readLine()) != null) {
				BaAl.add(str);
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void combine_BA_Meteor(String out){
		System.out.println(BaAl.size()+"\t"+MeteorAl.size());
		try{
		BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(out), "UTF-8"));
		for(int i=0;i<BaAl.size();i++){
			String ba[]=BaAl.get(i).split(" ");
			String ma[]=MeteorAl.get(i).split(" ");
			ArrayList<String>ls = new ArrayList<String>();
			for(int j=0;j<ba.length;j++){
				ls.add(ba[j]);
			}
			for(int j=0;j<ma.length;j++){
				ls.add(ma[j]);
			}
			Collections.sort(ls);
			HashSet<String> sh = new HashSet<String>(ls);
			ls= new ArrayList<String>(sh);
			Collections.sort(ls);
			String al="";
			for(int j=0;j<ls.size();j++){
				al+=ls.get(j)+" ";
			}
			//System.out.println(i+"\n"+BaAl.get(i)+"\n"+MeteorAl.get(i)+"\n" +al.trim());
			pw.write(al.trim()+"\n");
			pw.flush();
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void runMeteorAlignment(String peFile, String tgtFile){
		try{
			Runtime rt = Runtime.getRuntime();
			String command = "java -Xmx2G -cp meteor-1.2.jar Matcher" 
					+ tgtFile +" "+peFile+ " "+ ">"+ tgtFile+".wa.meteor";
			//String[] cmd= {"cmd","/c", command};
			Process P = rt.exec(command);
			P.waitFor();
			rt.gc();
					
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public String  makeConfFile4BA(String example, String traingFileName, 
			String devFileName, String SL, String TL, String WA_out_Dir){
		
		String ret="";
		try{
			File fileDir = new File(example);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("alignment.conf"), "UTF-8"));
			ret="alignment.conf";
			String str;
			int x=0;
			while ((str = in.readLine()) != null) {
				if(str.startsWith("foreignSuffix")){
					String ss[] = str.split("\t");
					str=ss[0]+"\t"+SL;
				}
				if(str.startsWith("englishSuffix")){
					String ss[] = str.split("\t");
					str=ss[0]+"\t"+TL;
				}
				if(str.startsWith("trainSources")){
					String ss[] = str.split("\t");
					str=ss[0]+"\t"+traingFileName;
				}
				if(str.startsWith("testSources")){
					String ss[] = str.split("\t");
					str=ss[0]+"\t"+devFileName;
				}
				if(str.startsWith("execDir")){
					String ss[] = str.split("\t");
					str=ss[0]+"\t"+WA_out_Dir;
				}
				pw.write(str+"\n");
				pw.flush();
			}
					
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
	}
	public void runBerkeleyAlignment(String conf, String destDir){
		try{
			Runtime rt = Runtime.getRuntime();
			String command = "java -server -mx2048m -jar berkeleyaligner.jar ++" + conf;
			//String[] cmd= {"cmd","/c", command};
			Process P = rt.exec(command);
			P.waitFor();
			rt.gc();
			
			command ="cp outptut/training.align "+ destDir;
			P = rt.exec(command);
			P.waitFor();
			rt.gc();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HybridAlignment f  = new HybridAlignment();
		//*************************************System Input don,t change
		String example = "example.conf"; // sample example file in Berkeley Aligner format
		//*************************************System Input
		//*************************************User Input change Path
		String traingFileName="data/train/train"; // train file has been tokenised and cleaned with moses tokenizer.perl
		String devFileName="data/devset/devset"; // train file has been tokenised and cleaned with moses tokenizer.perl
		String SL="hyp"; // hypothesis :MT output extension
		String TL="ref"; // reference : PE output extension
		String alignmentOutDir= traingFileName+".wa.align"; // Berkeley alignment output
		String peFile=traingFileName+TL;
		String tgtFile=traingFileName+SL;
		String WA_outDir="output_BA";
		String conf=f.makeConfFile4BA(example, traingFileName, devFileName, SL, TL,WA_outDir);
		f.runMeteorAlignment(peFile, tgtFile);
		
		f.runBerkeleyAlignment(conf, alignmentOutDir);
		
		//f.readMeteorAlignment(tgtFile+".wa.meteor"); // input meteor alignment
		//f.loadBAAlignment(alignmentOutDir); // input Barkeley alignment alignment
		
		//f.combine_BA_Meteor(tgtFile+"_aligned.hybrid");
	}

}
