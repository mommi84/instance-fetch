package it.tsoru.instancefetch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.TreeSet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class InstanceFetch {

	private static final String ENDPOINT = "http://dbpedia.org/sparql";
	private static final String GRAPH = "";

	private boolean verbose = false;
	
	private String property;
	private TreeSet<String> uris = new TreeSet<>();
	private String filename;
	private boolean straight;

	/**
	 * @param filename
	 * @param property
	 * @param straight
	 * @param uris
	 */
	public InstanceFetch(String filename, String property, boolean straight, String... uris) {
		super();
		this.filename = filename;
		this.property = property;
		this.straight = straight;

		for (String uri : uris)
			this.uris.add(uri);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		String setA = "Fungi.nt";
//		String setB = "InfectiousFungi.nt";
//		String setOut = "NonInfectiousFungi.nt";
//
//		new InstanceFetch(setA, "http://dbpedia.org/ontology/Fungus").build();
//		new InstanceFetch(setB, "http://dbpedia.org/ontology/Fungus",
//				"http://dbpedia.org/class/yago/InfectiousAgent109312843")
//				.build();
//
//		ExcludeTargetClass.minus(setA, setB, setOut);
//
//		System.out.println("Model saved to " + setOut);
		
		InstanceFetch fetch = new InstanceFetch("President_of_Italy.rdf", 
				"http://dbpedia.org/property/title",
				true,
				"http://dbpedia.org/resource/President_of_Italy");
		
//		InstanceFetch fetch = new InstanceFetch("European_Union_Country.rdf", 
//				"http://dbpedia.org/property/text",
//				false,
//				"http://dbpedia.org/resource/European_Union");
		
		fetch.setVerbose(true);
		fetch.build();
		
	}

	/**
	 * 
	 */
	public void build() {

		System.out.println("Building instance set for " + uris);
		String pattern = straight ? "?s <"+property+"> [URIs] . " :
			"[URIs] <"+property+"> ?s . ";
		System.out.println("Pattern = " + pattern);

		Model m = ModelFactory.createDefaultModel();

		TreeSet<String> insts = addInstances(m);
		
		int i = 0;
		if(!verbose)
			System.out.print(i+"\t");
		
		for (String inst : insts) {

			long size = addCBD(inst, m);

			if(verbose) {
				System.out.println("CBD for <"+inst+"> added (size="+size+")");
			} else {
				System.out.print(".");
				if (++i % 100 == 0)
					System.out.print("\n"+i+"\t");
			}

		}
		if(!verbose)
			System.out.println();

		FileOutputStream output;
		try {
			output = new FileOutputStream(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		RDFDataMgr.write(output, m, Lang.RDFXML); // Lang.NTRIPLES);

		System.out.println("Model saved to " + filename);

	}

	/**
	 * @param m
	 * @return
	 */
	private TreeSet<String> addInstances(Model m) {

		TreeSet<String> uris = new TreeSet<>();

		StringBuilder sb = new StringBuilder();
		for (String classURI : this.uris) {
			if(straight)
				sb.append("?s <"+property+"> <" + classURI + "> . ");
			else
				sb.append("<" + classURI + "> <"+property+"> ?s . ");
		}

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

	/**
	 * @param uri
	 * @param m
	 * @return
	 */
	private long addCBD(String uri, Model m) {
		
		long size;

		String query = "DESCRIBE <" + uri + ">";
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryEngineHTTP qexec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(ENDPOINT,
				sparqlQuery, GRAPH);
		qexec.setModelContentType(WebContent.contentTypeRDFXML);
		
		
		try {
			Model m2 = qexec.execDescribe();
			size = m2.size();
			m.add(m2);
		} catch (Exception e1) {
			if(verbose)
				System.out.println("ERROR ON "+uri+": "+e1.getMessage());
			return -1;
		}

		return size;
	}
	
	/**
	 * Method for DBpedia only (solves RiotException, but has 10K triples limit). No longer needed.
	 * 
	 * @param uri
	 * @param m
	 * @return
	 */
	@SuppressWarnings("unused")
	private long addCBDfromDBpedia(String uri, Model m) {
		
		long size;
		String URL = "http://dbpedia.org/data/" + uri.substring(uri.lastIndexOf('/') + 1) + ".ntriples";
		
		if(verbose)
			System.out.println("Fetching " + URL);

		try {
			Model m2 = RDFDataMgr.loadModel(URL, Lang.NTRIPLES);
			size = m2.size();
			m.add(m2);
		} catch (Exception e1) {
			e1.printStackTrace();
			return -1;
		}

		return size;
	}


	/**
	 * @return
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * @return
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * @param property
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * @return
	 */
	public TreeSet<String> getUris() {
		return uris;
	}

	/**
	 * @param uris
	 */
	public void setUris(TreeSet<String> uris) {
		this.uris = uris;
	}

	/**
	 * @return
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return
	 */
	public boolean isStraight() {
		return straight;
	}

	/**
	 * @param straight
	 */
	public void setStraight(boolean straight) {
		this.straight = straight;
	}

}
