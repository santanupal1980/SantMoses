import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * 
 */

/**
 * @author santanu
 *
 */
public class model {

	/**
	 * 
	 */
	public model() {
		// TODO Auto-generated constructor stub
	}
	public void TM(String filename, int k){
		  System.err.println("Reading translation model from %s...\n" + filename);
		  HashMap<String, String> tm = new HashMap<String, String>();
		  
		  try{
			  double lambda1=1,lambda2=1,lambda3=1,lambda4=1;
				File fileDir = new File(filename);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(fileDir), "UTF8"));
				
				String line;
				int x=0,y=0;
				
				while ((line = in.readLine()) != null) {
					
					String ss[]=line.split(" ||| ");
					String f=ss[0];
					String e=ss[1]; 
					String prob=ss[2]; 
					String almtinfo=ss[3];
					String other1=ss[4];
					String tp[] = prob.split(" ");
					double st= Double.parseDouble(tp[0]);
					double stl= Double.parseDouble(tp[1]);
					double ts= Double.parseDouble(tp[2]);
					double tsl= Double.parseDouble(tp[3]);
					double logprob= (lambda1)*Math.log10(st)+(lambda2)*Math.log10(stl)
										+(lambda3)*Math.log10(ts)+(lambda4)*Math.log10(tsl);
					
					
				}
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
		  

		    
		//    tm.setdefault(tuple(f.split()), []).append(phrase(e, logprob))
		//  for f in tm: # prune all but top k translations
		 //   tm[f].sort(key=lambda x: -x.logprob)
		   // del tm[f][k:] 
		  //return tm
	}

}
