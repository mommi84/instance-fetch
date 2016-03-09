package it.tsoru.instancefetch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class AddMissingTypes {

	public static void main(String[] args) {
		
		run("http://dbpedia.org/ontology/Fungus", "http://dbpedia.org/class/yago/InfectiousAgent109312843");
		
	}
	
	public static void run(final String CLASS_NAME, final String MISSING_CLASS_NAME) {
		
		final Node missingNode = NodeFactory.createURI(MISSING_CLASS_NAME);
		
		String input = "InfectiousFungi.nt";
		String output = "missing.nt";
		
		File outFile = new File("datasets/"+output);
		
		final FileOutputStream out;
		final StreamRDF outStream;
		try {
			out = new FileOutputStream(outFile);
			outStream = StreamRDFWriter.getWriterStream(out, Lang.NT);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			return;
		}

		outStream.start();

		StreamRDF aStream = new StreamRDF() {

			@Override
			public void start() {
			}

			@Override
			public void triple(Triple triple) {
				if (triple.getPredicate().getURI().equals(RDF.type.getURI())) 
					if(triple.getObject().getURI().equals(CLASS_NAME))
					outStream.triple(new Triple(
							triple.getSubject(),
							RDF.type.asNode(),
							missingNode
					));
			}

			@Override
			public void quad(Quad quad) {
			}

			@Override
			public void base(String base) {
			}

			@Override
			public void prefix(String prefix, String iri) {
			}

			@Override
			public void finish() {
			}

		};
		RDFDataMgr.parse(aStream, "datasets/"+input);

		outStream.finish();


	}

}
