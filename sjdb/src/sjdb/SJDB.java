/**
 * 
 */
package sjdb;
import java.io.*;

/**
 * @author nmg
 *
 */
public class SJDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// read serialised catalogue from file and parse
		String catFile = "data/cat.txt";
		Catalogue cat = new Catalogue();
		CatalogueParser catParser = new CatalogueParser(catFile, cat);
		catParser.parse();
		
		// read stdin, parse, and build canonical query plan
		QueryParser queryParser = new QueryParser(cat, new InputStreamReader(new FileInputStream("data/q5.txt")));
		Operator plan = queryParser.parse();
				
		// create estimator visitor and apply it to canonical plan
		Estimator est = new Estimator();
		Inspector insp = new Inspector();
		System.out.println("-----------------------------  PLAN  -------------------------------------------------------------");
		plan.accept(est);
		plan.accept(insp);

		System.out.println(est.getCost(plan));
		System.out.println("-----------------------------  OPTMISE  ----------------------------------------------------------");
		// create optimised plan
		Optimiser opt = new Optimiser(cat);
		Operator optPlan = opt.optimise(plan);
		optPlan.accept(est);
		optPlan.accept(insp);
		System.out.println(est.getCost(optPlan));
	}
}
