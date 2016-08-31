package it.tsoru.hyperconnect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceF;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class HyperConnect {

	private static final String NS = "http://cikmcup.org/hyper/";

	public static void main(String[] args) throws FileNotFoundException {
		
		HashMap<String, Property> hashes = new HashMap<>();
		
		String dir = "/Users/tom/PhD/CIKM-Cup-2016/cikmcup2rdf/tdb/";
		Dataset dataset = TDBFactory.createDataset(dir);
//		dataset.begin(ReadWrite.WRITE);
//		Model m = dataset.getDefaultModel();
//		
//		RDFDataMgr.read(m, "/Users/tom/PhD/CIKM-Cup-2016/cikmcup2rdf/data-100/CIKM-Cup.nt");
//		dataset.commit();
//		dataset.end();
//		System.out.println(m.size()+" statements loaded and saved to TDB.");
		
//		String q = "select (count(*) as ?c) "
//				+ "where { ?s ?p1 ?x . ?o ?p2 ?x }";
		
//		System.exit(0);
		
		String q = "select ?s ?p1 ?p2 ?o where { "
				+ "?s ?p1 ?x . "
				+ "?o ?p2 ?x . "
				+ "FILTER(?p1 != ?p2 || ?s != ?o) . "
				+ "FILTER(NOT EXISTS {{?s ?p ?o} UNION {?o ?p ?s}}) "
				+ "}";
		
		dataset.begin(ReadWrite.READ);
		
		Query sparqlQuery = QueryFactory.create(q, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, dataset);
		
		PrintWriter pw = new PrintWriter(new File("/Users/tom/PhD/CIKM-Cup-2016/cikmcup2rdf/data-100/CIKM-Cup-hyper.nt"));
		
		try {

			ResultSet rs = qexec.execSelect();
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				String key = qs.get("p1").toString() + "§" + qs.get("p1").toString();
				Property hash;
				if(hashes.containsKey(key)) 
					hash = hashes.get(key);
				else {
					hash = ResourceFactory.createProperty(NS + DigestUtils.sha1Hex(key));
					hashes.put(key, hash);
				}
				
//				(qs.get("s").asResource(), hash, qs.get("o"));
				pw.println("<"+qs.get("s").asResource()+"> <"+ hash + "> <" + qs.get("o")+"> .");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		pw.close();
		dataset.end();
		
		System.out.println("Done.");
	}

}
