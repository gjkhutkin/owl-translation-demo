package demo.owlstructure.utils;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

public final class OntologyUtils {
	public static void closeIterator(Iterator<?> iterator) {
		while (iterator.hasNext()) {
			iterator.next();
		}
	}

	public static boolean containsClassSuperset(OntClass ontClass, Collection<OntClass> candidates) {
		ClosableIterator<OntClass> superClassIterator = ontClass.listSuperClasses();
		while (superClassIterator.hasNext()) {
			OntClass superClass = superClassIterator.next();
			if (candidates.contains(superClass) || containsClassSuperset(superClass, candidates)) {
				superClassIterator.close();
				return true;
			}
		}
		return false;
	}

	public static boolean containsCompleteClassSuperset(OntClass ontClass, Collection<OntClass> candidates) {
		ClosableIterator<OntClass> superClassIterator = ontClass.listSuperClasses();
		if (!superClassIterator.hasNext()) {
			return false;
		}
		while (superClassIterator.hasNext()) {
			OntClass superClass = superClassIterator.next();
			if (!candidates.contains(superClass) && !containsCompleteClassSuperset(superClass, candidates)) {
				superClassIterator.close();
				return false;
			}
		}
		return true;
	}

	public static String getSparqlPrefixes(OntModel ontModel) {
		String sparqlPrefixes = "";

		Map<String, String> prefixMap = ontModel.getNsPrefixMap();
		Iterator<Map.Entry<String, String>> prefixIterator = prefixMap.entrySet().iterator();
		while (prefixIterator.hasNext()) {
			Map.Entry<String, String> prefixPair = prefixIterator.next();
			sparqlPrefixes += "PREFIX " + prefixPair.getKey() + ": <" + prefixPair.getValue() + ">\n";
		}

		return sparqlPrefixes;
	}

	public static OntClass getOwlThing(OntModel ontModel) {
		return ontModel.createClass("http://www.w3.org/2002/07/owl#Thing");
	}

	public static Collection<OntClass> listClassAncestors(OntClass ontClass) {
		Collection<OntClass> ancestors = new ArrayList<OntClass>();
		Iterator<OntClass> superClassIterator = ontClass.listSuperClasses();
		while (superClassIterator.hasNext()) {
			OntClass superClass = superClassIterator.next();
			ancestors.add(superClass);
			ancestors.addAll(listClassAncestors(superClass));
		}
		return ancestors;
	}

	public static Collection<OntClass> listClassDescendants(OntClass ontClass) {
		Collection<OntClass> descendants = new ArrayList<OntClass>();
		Iterator<OntClass> subClassIterator = ontClass.listSubClasses();
		while (subClassIterator.hasNext()) {
			OntClass subClass = subClassIterator.next();
			descendants.add(subClass);
			descendants.addAll(listClassDescendants(subClass));
		}
		return descendants;
	}

	public static OntModel createOntology() {
		try {
			return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static OntModel loadOntology(String path) {
		try {
			OntModel ontModel = OntologyUtils.createOntology();
			FileManager fm = FileManager.get();
			fm.addLocatorURL();
			fm.readModel(ontModel, path);
	
			return ontModel;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveOntologyRdf(OntModel ontModel, String path) {
		try {
			RDFWriter writer = ontModel.getWriter("RDF/XML-ABBREV");
			writer.setProperty("showXmlDeclaration","true");
			writer.setProperty("showDoctypeDeclaration","true");

			FileOutputStream stream = new FileOutputStream(path);
			writer.write(ontModel, stream, ontModel.getNsPrefixURI(""));
			stream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}