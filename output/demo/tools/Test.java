package demo.tools;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import demo.tools.Factory;

/**
 * Factory
 */
public class Test {

	private static Log log = LogFactory.getLog(Test.class);

	private static String namePrefix = "ClassInstance";
	private static int nameCount = 0;

	public static void main(String[] args) {
		run();
	}

	private static String getNewInstanceName() {
		nameCount++;
		return namePrefix + nameCount;
	}

	private static String getNewInstanceURI() {
		String localName = getNewInstanceName();
		String base = "jmodel.getBaseNamespace()";
		return base + "#" + localName;
	}

	public static void run() {
		String base = "jmodel.getBaseNamespace()";

		log.info("Creating an empty ontology");
		OntModel ontModel = ModelFactory.createOntologyModel();
		Ontology ontology = ontModel.createOntology(base);

		log.info("Registering custom classes with jena");
		Factory.registerImports(ontology, ontModel);
		Factory.registerCustomClasses();

		log.info("Starting test case run");
		runLocation(ontModel);
		runUser(ontModel);
		runThing(ontModel);
		log.info("DONE DONE DONE DONE DONE DONE DONE DONE");
	}

	protected static void runLocation(OntModel ontModel) {
		log.info("Testing class Location");

		// create, create anonymous, exists, delete, list
		log.debug("  Creating anonymous class instance");
		demo.Location.create(ontModel);

		log.debug("  Creating two named class instance");
		String uri = getNewInstanceURI();
		demo.Location.create(uri, ontModel);
		uri = getNewInstanceURI();
		demo.Location.create(uri, ontModel);

		log.debug("  Checking for existance of class instance");
		boolean exists = demo.Location.exists(uri, ontModel);
		log.debug("  -> exists: " + exists);

		log.debug("  Fetching known instance");
		demo.Location clsInstance = demo.Location.get(uri, ontModel);
		log.debug("  -> instance: " + clsInstance.getLocalName());

		log.debug("  Iterate over all class instances");
		Iterator<demo.Location> it = demo.Location.iterate(ontModel);
		while (it.hasNext()) {
			clsInstance = (demo.Location) it.next();
			log.debug("  -> instance: " + clsInstance.getLocalName());
		}

		log.debug("  List all class instances and ");
		for (demo.Location cls : demo.Location.list(ontModel))
			log.debug("  -> instance: " + cls.getLocalName());

		log.debug("  Iterate over all class instances and subclass instances");
		Iterator<demo.Location> it2 = demo.Location.iterate(false, ontModel);
		while (it2.hasNext()) {
			clsInstance = (demo.Location) it2.next();
			log.debug("  -> instance: " + clsInstance.getLocalName());
		}

		log.debug("  List all class instances");
		for (demo.Location cls : demo.Location.list(false, ontModel))
			log.debug("  -> instance: " + cls.getLocalName());

		log.debug("  Counting class instances");
		log.debug("  -> count: " + demo.Location.count(ontModel));

		log.debug("  Deleting a named class instance");
		demo.Location.delete(uri, ontModel);

		// class instance for property tests
		uri = getNewInstanceURI();
		demo.Location.create(uri, ontModel);
		demo.Location instance = demo.Location.get(uri, ontModel);

		// test each representation
		log.info("  Testing property lon of class Location");

		log.debug("    Any property lon exist?");
		log.debug("    -> exists: " + instance.existsLon());

		log.debug("    Adding property instance");
		instance.addLon(demo.owl2java.model.xsd.XsdMapTestData.getDouble("http://www.w3.org/2001/XMLSchema#double"));

		log.debug("    Iterate over all property values");
		Iterator<java.lang.Double> itLon = instance.iterateLon();
		java.lang.Double instLon = null;
		while (itLon.hasNext()) {
			instLon = (java.lang.Double) itLon.next();
			log.debug("    -> instance: " + instLon);
		}

		log.debug("    List all property values");
		for (java.lang.Double iinstLon : instance.listLon())
			log.debug("    -> instance: " + iinstLon);

		log.debug("    Count property values");
		log.debug("    -> count: " + instance.countLon());

		log.debug("    Removing a known property instance");
		instance.removeLon(instLon);

		log.debug("    Removing all property instances");
		instance.removeAllLon();

		log.info("  Testing property lat of class Location");

		log.debug("    Any property lat exist?");
		log.debug("    -> exists: " + instance.existsLat());

		log.debug("    Adding property instance");
		instance.addLat(demo.owl2java.model.xsd.XsdMapTestData.getDouble("http://www.w3.org/2001/XMLSchema#double"));

		log.debug("    Iterate over all property values");
		Iterator<java.lang.Double> itLat = instance.iterateLat();
		java.lang.Double instLat = null;
		while (itLat.hasNext()) {
			instLat = (java.lang.Double) itLat.next();
			log.debug("    -> instance: " + instLat);
		}

		log.debug("    List all property values");
		for (java.lang.Double iinstLat : instance.listLat())
			log.debug("    -> instance: " + iinstLat);

		log.debug("    Count property values");
		log.debug("    -> count: " + instance.countLat());

		log.debug("    Removing a known property instance");
		instance.removeLat(instLat);

		log.debug("    Removing all property instances");
		instance.removeAllLat();

	}

	protected static void runUser(OntModel ontModel) {
		log.info("Testing class User");

		// create, create anonymous, exists, delete, list
		log.debug("  Creating anonymous class instance");
		demo.User.create(ontModel);

		log.debug("  Creating two named class instance");
		String uri = getNewInstanceURI();
		demo.User.create(uri, ontModel);
		uri = getNewInstanceURI();
		demo.User.create(uri, ontModel);

		log.debug("  Checking for existance of class instance");
		boolean exists = demo.User.exists(uri, ontModel);
		log.debug("  -> exists: " + exists);

		log.debug("  Fetching known instance");
		demo.User clsInstance = demo.User.get(uri, ontModel);
		log.debug("  -> instance: " + clsInstance.getLocalName());

		log.debug("  Iterate over all class instances");
		Iterator<demo.User> it = demo.User.iterate(ontModel);
		while (it.hasNext()) {
			clsInstance = (demo.User) it.next();
			log.debug("  -> instance: " + clsInstance.getLocalName());
		}

		log.debug("  List all class instances and ");
		for (demo.User cls : demo.User.list(ontModel))
			log.debug("  -> instance: " + cls.getLocalName());

		log.debug("  Iterate over all class instances and subclass instances");
		Iterator<demo.User> it2 = demo.User.iterate(false, ontModel);
		while (it2.hasNext()) {
			clsInstance = (demo.User) it2.next();
			log.debug("  -> instance: " + clsInstance.getLocalName());
		}

		log.debug("  List all class instances");
		for (demo.User cls : demo.User.list(false, ontModel))
			log.debug("  -> instance: " + cls.getLocalName());

		log.debug("  Counting class instances");
		log.debug("  -> count: " + demo.User.count(ontModel));

		log.debug("  Deleting a named class instance");
		demo.User.delete(uri, ontModel);

		// class instance for property tests
		uri = getNewInstanceURI();
		demo.User.create(uri, ontModel);
		demo.User instance = demo.User.get(uri, ontModel);

		// test each representation
		log.info("  Testing property location of class User");

		log.debug("    Any property location exist?");
		log.debug("    -> exists: " + instance.existsLocation());

		log.debug("    Adding property instance");
		uri = getNewInstanceURI();
		instance.addLocation(demo.Location.create(uri, ontModel));
		instance.addLocation(demo.Location.create(getNewInstanceURI(), ontModel));
		instance.addLocation(demo.Location.create(getNewInstanceURI(), ontModel));

		log.debug("    Iterate over all property instances");
		Iterator<demo.Location> itLocation = instance.iterateLocation();
		demo.Location instLocation = null;
		while (itLocation.hasNext()) {
			instLocation = (demo.Location) itLocation.next();
			log.debug("    -> instance: " + instLocation.getLocalName());
		}

		log.debug("    List all property values");
		for (demo.Location iinstLocation : instance.listLocation())
			log.debug("    -> instance: " + iinstLocation.getLocalName());

		log.debug("    Count property values");
		log.debug("    -> count: " + instance.countLocation());

		log.debug("    Removing a known property instance");
		instance.removeLocation(instLocation);

		log.debug("    Removing all property instances");
		instance.removeAllLocation();

		log.info("  Testing property surname of class User");

		log.debug("    Any property surname exist?");
		log.debug("    -> exists: " + instance.existsSurname());

		log.debug("    Adding property instance");
		instance.addSurname(demo.owl2java.model.xsd.XsdMapTestData.getString("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"));

		log.debug("    Iterate over all property values");
		Iterator<java.lang.String> itSurname = instance.iterateSurname();
		java.lang.String instSurname = null;
		while (itSurname.hasNext()) {
			instSurname = (java.lang.String) itSurname.next();
			log.debug("    -> instance: " + instSurname);
		}

		log.debug("    List all property values");
		for (java.lang.String iinstSurname : instance.listSurname())
			log.debug("    -> instance: " + iinstSurname);

		log.debug("    Count property values");
		log.debug("    -> count: " + instance.countSurname());

		log.debug("    Removing a known property instance");
		instance.removeSurname(instSurname);

		log.debug("    Removing all property instances");
		instance.removeAllSurname();

		log.info("  Testing property preferences of class User");

		log.debug("    Any property preferences exist?");
		log.debug("    -> exists: " + instance.existsPreferences());

		log.debug("    Adding property instance");
		instance.addPreferences(demo.owl2java.model.xsd.XsdMapTestData.getString("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"));

		log.debug("    Iterate over all property values");
		Iterator<java.lang.String> itPreferences = instance.iteratePreferences();
		java.lang.String instPreferences = null;
		while (itPreferences.hasNext()) {
			instPreferences = (java.lang.String) itPreferences.next();
			log.debug("    -> instance: " + instPreferences);
		}

		log.debug("    List all property values");
		for (java.lang.String iinstPreferences : instance.listPreferences())
			log.debug("    -> instance: " + iinstPreferences);

		log.debug("    Count property values");
		log.debug("    -> count: " + instance.countPreferences());

		log.debug("    Removing a known property instance");
		instance.removePreferences(instPreferences);

		log.debug("    Removing all property instances");
		instance.removeAllPreferences();

		log.info("  Testing property name of class User");

		log.debug("    Any property name exist?");
		log.debug("    -> exists: " + instance.existsName());

		log.debug("    Adding property instance");
		instance.addName(demo.owl2java.model.xsd.XsdMapTestData.getString("http://www.w3.org/2001/XMLSchema#string"));

		log.debug("    Iterate over all property values");
		Iterator<java.lang.String> itName = instance.iterateName();
		java.lang.String instName = null;
		while (itName.hasNext()) {
			instName = (java.lang.String) itName.next();
			log.debug("    -> instance: " + instName);
		}

		log.debug("    List all property values");
		for (java.lang.String iinstName : instance.listName())
			log.debug("    -> instance: " + iinstName);

		log.debug("    Count property values");
		log.debug("    -> count: " + instance.countName());

		log.debug("    Removing a known property instance");
		instance.removeName(instName);

		log.debug("    Removing all property instances");
		instance.removeAllName();

	}

	protected static void runThing(OntModel ontModel) {
		log.info("Testing class Thing");

		// create, create anonymous, exists, delete, list
		log.debug("  Creating anonymous class instance");
		demo.Thing.create(ontModel);

		log.debug("  Creating two named class instance");
		String uri = getNewInstanceURI();
		demo.Thing.create(uri, ontModel);
		uri = getNewInstanceURI();
		demo.Thing.create(uri, ontModel);

		log.debug("  Checking for existance of class instance");
		boolean exists = demo.Thing.exists(uri, ontModel);
		log.debug("  -> exists: " + exists);

		log.debug("  Fetching known instance");
		demo.Thing clsInstance = demo.Thing.get(uri, ontModel);
		log.debug("  -> instance: " + clsInstance.getLocalName());

		log.debug("  Iterate over all class instances");
		Iterator<demo.Thing> it = demo.Thing.iterate(ontModel);
		while (it.hasNext()) {
			clsInstance = (demo.Thing) it.next();
			log.debug("  -> instance: " + clsInstance.getLocalName());
		}

		log.debug("  List all class instances and ");
		for (demo.Thing cls : demo.Thing.list(ontModel))
			log.debug("  -> instance: " + cls.getLocalName());

		log.debug("  Iterate over all class instances and subclass instances");
		Iterator<demo.Thing> it2 = demo.Thing.iterate(false, ontModel);
		while (it2.hasNext()) {
			clsInstance = (demo.Thing) it2.next();
			log.debug("  -> instance: " + clsInstance.getLocalName());
		}

		log.debug("  List all class instances");
		for (demo.Thing cls : demo.Thing.list(false, ontModel))
			log.debug("  -> instance: " + cls.getLocalName());

		log.debug("  Counting class instances");
		log.debug("  -> count: " + demo.Thing.count(ontModel));

		log.debug("  Deleting a named class instance");
		demo.Thing.delete(uri, ontModel);

	}
}