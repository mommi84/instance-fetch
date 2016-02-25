package it.tsoru.instancefetch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.TreeSet;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class InstanceFetch {

	private static final String ENDPOINT = "http://dbpedia.org/sparql";
	private static final String GRAPH = "";

	private TreeSet<String> classURIs = new TreeSet<>();
	private String filename;

	public InstanceFetch(String filename, String... uris) {
		super();
		this.filename = filename;

		for (String uri : uris)
			classURIs.add(uri);
	}

	public static void main(String[] args) {

		String setA = "Fungi.nt";
		String setB = "InfectiousFungi.nt";
		String setOut = "NonInfectiousFungi.nt";

		new InstanceFetch(setA, "http://dbpedia.org/ontology/Fungus").build();
		new InstanceFetch(setB, "http://dbpedia.org/ontology/Fungus",
				"http://dbpedia.org/class/yago/InfectiousAgent109312843")
				.build();

		Utils.minus(setA, setB, setOut);

		System.out.println("Model saved to " + setOut);

	}

	public void build() {

		System.out.println("Building instance set for " + classURIs);

		Model m = ModelFactory.createDefaultModel();

		TreeSet<String> insts = addInstances(m);
		
		int i = 0;
		System.out.print(i+"\t");
		for (String inst : insts) {

			addCBD(inst, m);

			System.out.print(".");
			if (++i % 100 == 0)
				System.out.print("\n"+i+"\t");

		}
		System.out.println();

		FileOutputStream output;
		try {
			output = new FileOutputStream(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		RDFDataMgr.write(output, m, Lang.NTRIPLES);

		System.out.println("Model saved to " + filename);

	}

	private TreeSet<String> addInstances(Model m) {

		TreeSet<String> uris = new TreeSet<>();

		StringBuilder sb = new StringBuilder();
		for (String classURI : classURIs)
			sb.append("?s a <" + classURI + "> . ");

		String query = "select ?s where { " + sb.toString() + " }";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT,
				sparqlQuery, GRAPH);
		try {

			ResultSet rs = qexec.execSelect();
			while (rs.hasNext())
				uris.add(rs.next().get("s").asResource().getURI());

		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}

		System.out.println("Returning " + uris.size() + " URIs.");
		return uris;
	}

	private static boolean addCBD(String uri, Model m) {

		String query = "DESCRIBE <" + uri + ">";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT,
				sparqlQuery, GRAPH);
		try {
			m.add(qexec.execDescribe());
		} catch (Exception e1) {
			return false;
		}

		return true;
	}

}
