package demo.tools;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * A vocabulary for all properties, classes etc. used
 * in the ontology. This is based on the jena schemagen
 * output
 *
 * Note, that the current implementation assumes unique names
 * for class names.
 */
public class Vocabulary {
	public static final List<String> NAMESPACES;

	public static final Resource NS;

	public static final OntClass Location;
	public static final OntClass User;
	public static final OntClass Thing;

	public static final DatatypeProperty name;
	public static final ObjectProperty location;
	public static final DatatypeProperty preferences;
	public static final DatatypeProperty lat;
	public static final DatatypeProperty surname;
	public static final DatatypeProperty lon;

	static {
		NAMESPACES = new ArrayList<String>();
		NAMESPACES.add("http://oss.fruct.org/etourism#");

		OntModel resourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);

		NS = resourceModel.createResource("http://oss.fruct.org/etourism#");

		Location = resourceModel.createClass("http://oss.fruct.org/etourism#Location");
		User = resourceModel.createClass("http://oss.fruct.org/etourism#User");
		Thing = resourceModel.createClass("http://www.w3.org/2002/07/owl#Thing");

		name = resourceModel.createDatatypeProperty("http://oss.fruct.org/etourism#name");
		location = resourceModel.createObjectProperty("http://oss.fruct.org/etourism#hasLocation");
		preferences = resourceModel.createDatatypeProperty("http://oss.fruct.org/etourism#preferences");
		lat = resourceModel.createDatatypeProperty("http://oss.fruct.org/etourism#lat");
		surname = resourceModel.createDatatypeProperty("http://oss.fruct.org/etourism#surname");
		lon = resourceModel.createDatatypeProperty("http://oss.fruct.org/etourism#lon");
	}
}