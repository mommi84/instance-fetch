package it.tsoru.instancefetch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.TreeSet;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ExcludeTargetClass {
	
	public static void main(String[] args) {
		
		exclude("http://dbpedia.org/class/yago/InfectiousAgent109312843", "Fungi.nt", "Fungi-no-target-class.nt");
		exclude("http://dbpedia.org/class/yago/InfectiousAgent109312843", "InfectiousFungi.nt", "InfectiousFungi-no-target-class.nt");
		
	}
	
	public static void exclude(String targetClass, String input, String output) {
		
		File inFile = new File(input);
		File outFile = new File(output);
		
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
				if (!triple.getObject().toString().equals(targetClass))
					outStream.triple(triple);
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
		RDFDataMgr.parse(aStream, input);

		outStream.finish();

		inFile.delete();
		outFile.renameTo(inFile);
		
	}

	/**
	 * Set difference.
	 * 
	 * @param setA
	 * @param setB
	 * @param output
	 */
	public static void minus(String setA, String setB, String output) {

		TreeSet<String> setBindex = new TreeSet<>();
		StreamRDF bStream = new StreamRDF() {

			@Override
			public void start() {
			}

			@Override
			public void triple(Triple triple) {
				setBindex.add(triple.toString());
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
		RDFDataMgr.parse(bStream, setB);

		final FileOutputStream out;
		final StreamRDF outStream;
		try {
			out = new FileOutputStream(new File(output));
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
				if (!setBindex.contains(triple.toString()))
					outStream.triple(triple);
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
		RDFDataMgr.parse(aStream, setA);

		outStream.finish();

	}

}
