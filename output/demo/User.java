package demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import demo.owl2java.model.jenautils.NullFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.enhanced.BuiltinPersonalities;
import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.enhanced.EnhNode;
import com.hp.hpl.jena.enhanced.Implementation;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Profile;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.Map1;

// import interface
import demo.IUser;

/**
 * Class http://oss.fruct.org/etourism#User
 */
public class User extends IndividualImpl implements IUser {

	private static Log log = LogFactory.getLog(User.class);

	/**
	 * Implementation factory for User
	 */
	static final public Implementation factory = new Implementation() {

		/**
		 * Convert a Node into an instance of the class
		 */
		public EnhNode wrap(Node n, EnhGraph eg) {
			if (canWrap(n, eg)) {
				return new User(n, eg);
			} else {
				log.warn("Cannot convert node " + n.toString() + " to  User");
				return null;
			}
		}

		/**
		 * Return true iff the node can be converted to an instance of
		 * this class (User)
		 */
		public boolean canWrap(Node n, EnhGraph eg) {
			Profile profile;
			if (eg instanceof OntModel)
				profile = ((OntModel) eg).getProfile();
			else
				return false;

			if (!profile.isSupported(n, eg, Individual.class)) {
				return false;
			}

			Graph graph = eg.asGraph();
			return graph.contains(n, com.hp.hpl.jena.vocabulary.RDF.type.asNode(), demo.tools.Vocabulary.User.asNode());
		}
	};

	/**
	 * Filtering support for User
	 */
	static final public Filter<User> nullFilter = new NullFilter<User>();

	/**
	 * Mapping support for User
	 */
	public static <From> Map1<From, User> mapperFrom(Class<From> from) {
		return new Map1<From, User>() {
			@Override
			public User map1(Object x) {
				if (x instanceof Statement) {
					Resource r = ((Statement) x).getResource();
					if (r.canAs(User.class))
						return r.as(User.class);
				} else if (x instanceof RDFNode) {
					if (((RDFNode) x).canAs(User.class))
						return ((RDFNode) x).as(User.class);
				}
				return null;
			}
		};
	}

	// Instantiate some mappers for general use
	static final public Map1<Statement, User> statementMapper = mapperFrom(Statement.class);
	static final public Map1<Individual, User> individualMapper = mapperFrom(Individual.class);
	static final public Map1<RDFNode, User> nodeMapper = mapperFrom(RDFNode.class);

	/**
	 * Generic functions from parent class
	 */
	public User(Node n, EnhGraph g) {
		super(n, g);
	}

	/**
	 * Registers all custom classes with jena
	 */
	public static void register() {
		log.debug("Registering custom class User with jena");
		BuiltinPersonalities.model.add(User.class, User.factory);
		BuiltinPersonalities.model.add(demo.User.class, User.factory);
	}

	/**
	 * Static Functions for instance handling
	 */
	public static User get(String uri, OntModel ontModel) {
		Individual individual = ontModel.getIndividual(uri);
		return (demo.User) individual.as(demo.User.class);
	}

	public static User get(String uri) {
		return get(uri, demo.tools.Factory.getDefaultModel());
	}

	public static Iterator<User> iterate(OntModel ontModel) {
		ExtendedIterator<Individual> it = ontModel.listIndividuals(demo.tools.Vocabulary.User);
		return it.mapWith(individualMapper).filterDrop(nullFilter);
	}

	public static Iterator<User> iterate() {
		return iterate(demo.tools.Factory.getDefaultModel());
	}

	public static List<User> list(OntModel ontModel) {
		List<User> list = new ArrayList<User>();
		Iterator<User> it = iterate(ontModel);
		while (it.hasNext()) {
			User cls = it.next();
			list.add(cls);
		}
		return list;
	}

	public static List<User> list() {
		return list(demo.tools.Factory.getDefaultModel());
	}

	public static Iterator<User> iterate(boolean direct, OntModel ontModel) {
		OntClass cls = ontModel.getOntClass("http://oss.fruct.org/etourism#User");
		ExtendedIterator<? extends RDFNode> it = cls.listInstances(direct);
		ExtendedIterator<RDFNode> nodeIt = new WrappedIterator<RDFNode>(it) {
		};
		return nodeIt.mapWith(nodeMapper).filterDrop(nullFilter);
	}

	public static Iterator<User> iterate(boolean direct) {
		return iterate(direct, demo.tools.Factory.getDefaultModel());
	}

	public static List<User> list(boolean direct, OntModel ontModel) {
		List<User> list = new ArrayList<User>();
		Iterator<User> it = iterate(direct, ontModel);
		while (it.hasNext()) {
			User cls = it.next();
			list.add(cls);
		}
		return list;
	}

	public static List<User> list(boolean direct) {
		return list(direct, demo.tools.Factory.getDefaultModel());
	}

	public static int count(OntModel ontModel) {
		int count = 0;
		Iterator<User> it = iterate(ontModel);
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public static int count() {
		return count(demo.tools.Factory.getDefaultModel());
	}

	public static int count(boolean direct, OntModel ontModel) {
		int count = 0;
		Iterator<User> it = iterate(direct, ontModel);
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public static int count(boolean direct) {
		return count(direct, demo.tools.Factory.getDefaultModel());
	}

	public static boolean exists(String uri, OntModel ontModel) {
		Individual individual = ontModel.getIndividual(uri);
		if (individual != null)
			return true;
		return false;
	}

	public static boolean exists(String uri) {
		return exists(uri, demo.tools.Factory.getDefaultModel());
	}

	public static User create(String uri, OntModel ontModel) {
		return (User) ontModel.createOntResource(User.class, demo.tools.Vocabulary.User, uri);
	}

	public static User create(OntModel ontModel) {
		return create(null, ontModel);
	}

	public static User create(String uri) {
		return create(uri, demo.tools.Factory.getDefaultModel());
	}

	public static User create() {
		return create(null, demo.tools.Factory.getDefaultModel());
	}

	public static void delete(String uri, OntModel ontModel) {
		demo.tools.Factory.deleteInstance(uri, ontModel);
	}

	public static void delete(String uri) {
		demo.tools.Factory.deleteInstance(uri);
	}

	/**
	 * Domain property location
	 * with uri http://oss.fruct.org/etourism#hasLocation
	 */
	public boolean existsLocation() {
		return hasProperty(demo.tools.Vocabulary.location);
	}

	public boolean hasLocation(demo.ILocation locationValue) {
		return hasProperty(demo.tools.Vocabulary.location, locationValue);
	}

	public int countLocation() {
		int count = 0;
		Iterator<demo.Location> it = iterateLocation();
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public Iterator<demo.Location> iterateLocation() {
		ExtendedIterator<Statement> it = listProperties(demo.tools.Vocabulary.location);
		return it.mapWith(demo.Location.statementMapper).filterDrop(demo.Location.nullFilter);
	}

	public List<demo.Location> listLocation() {
		List<demo.Location> list = new ArrayList<demo.Location>();
		Iterator<demo.Location> it = iterateLocation();
		while (it.hasNext()) {
			demo.Location inst = it.next();
			list.add(inst);
		}
		return list;
	}

	public void addLocation(demo.ILocation locationValue) {
		addProperty(demo.tools.Vocabulary.location, locationValue);
	}

	public void addAllLocation(List<? extends demo.ILocation> locationList) {
		for (demo.ILocation o : locationList)
			addLocation(o);

	}

	public void removeLocation(demo.ILocation locationValue) {
		removeProperty(demo.tools.Vocabulary.location, locationValue);
	}

	public void removeAllLocation() {
		removeAll(demo.tools.Vocabulary.location);
	}

	/**
	 * Domain property surname
	 * with uri http://oss.fruct.org/etourism#surname
	 */
	public boolean existsSurname() {
		return hasProperty(demo.tools.Vocabulary.surname);
	}

	public boolean hasSurname(java.lang.String stringValue) {
		return hasProperty(demo.tools.Vocabulary.surname);
	}

	public int countSurname() {
		int count = 0;
		Iterator<java.lang.String> it = iterateSurname();
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public Iterator<java.lang.String> iterateSurname() {
		ExtendedIterator<Statement> it = listProperties(demo.tools.Vocabulary.surname);
		return it.mapWith(demo.owl2java.model.xsd.XsdUtils.objectAsStringMapper).filterDrop(new NullFilter<java.lang.String>());
	}

	public List<java.lang.String> listSurname() {
		List<java.lang.String> list = new ArrayList<java.lang.String>();
		Iterator<java.lang.String> it = iterateSurname();
		while (it.hasNext()) {
			java.lang.String inst = it.next();
			list.add(inst);
		}
		return list;
	}

	public void addSurname(java.lang.String stringValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), stringValue, "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
		setPropertyValue(demo.tools.Vocabulary.surname, literal);
	}

	public void addAllSurname(List<java.lang.String> stringList) {
		for (java.lang.String o : stringList)
			addSurname(o);
	}

	public void removeSurname(java.lang.String stringValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), stringValue, "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
		removeProperty(demo.tools.Vocabulary.surname, literal);
	}

	public void removeAllSurname() {
		removeAll(demo.tools.Vocabulary.surname);

	}

	/**
	 * Domain property preferences
	 * with uri http://oss.fruct.org/etourism#preferences
	 */
	public boolean existsPreferences() {
		return hasProperty(demo.tools.Vocabulary.preferences);
	}

	public boolean hasPreferences(java.lang.String stringValue) {
		return hasProperty(demo.tools.Vocabulary.preferences);
	}

	public int countPreferences() {
		int count = 0;
		Iterator<java.lang.String> it = iteratePreferences();
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public Iterator<java.lang.String> iteratePreferences() {
		ExtendedIterator<Statement> it = listProperties(demo.tools.Vocabulary.preferences);
		return it.mapWith(demo.owl2java.model.xsd.XsdUtils.objectAsStringMapper).filterDrop(new NullFilter<java.lang.String>());
	}

	public List<java.lang.String> listPreferences() {
		List<java.lang.String> list = new ArrayList<java.lang.String>();
		Iterator<java.lang.String> it = iteratePreferences();
		while (it.hasNext()) {
			java.lang.String inst = it.next();
			list.add(inst);
		}
		return list;
	}

	public void addPreferences(java.lang.String stringValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), stringValue, "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
		setPropertyValue(demo.tools.Vocabulary.preferences, literal);
	}

	public void addAllPreferences(List<java.lang.String> stringList) {
		for (java.lang.String o : stringList)
			addPreferences(o);
	}

	public void removePreferences(java.lang.String stringValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), stringValue, "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
		removeProperty(demo.tools.Vocabulary.preferences, literal);
	}

	public void removeAllPreferences() {
		removeAll(demo.tools.Vocabulary.preferences);

	}

	/**
	 * Domain property name
	 * with uri http://oss.fruct.org/etourism#name
	 */
	public boolean existsName() {
		return hasProperty(demo.tools.Vocabulary.name);
	}

	public boolean hasName(java.lang.String stringValue) {
		return hasProperty(demo.tools.Vocabulary.name);
	}

	public int countName() {
		int count = 0;
		Iterator<java.lang.String> it = iterateName();
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public Iterator<java.lang.String> iterateName() {
		ExtendedIterator<Statement> it = listProperties(demo.tools.Vocabulary.name);
		return it.mapWith(demo.owl2java.model.xsd.XsdUtils.objectAsStringMapper).filterDrop(new NullFilter<java.lang.String>());
	}

	public List<java.lang.String> listName() {
		List<java.lang.String> list = new ArrayList<java.lang.String>();
		Iterator<java.lang.String> it = iterateName();
		while (it.hasNext()) {
			java.lang.String inst = it.next();
			list.add(inst);
		}
		return list;
	}

	public void addName(java.lang.String stringValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), stringValue, "http://www.w3.org/2001/XMLSchema#string");
		setPropertyValue(demo.tools.Vocabulary.name, literal);
	}

	public void addAllName(List<java.lang.String> stringList) {
		for (java.lang.String o : stringList)
			addName(o);
	}

	public void removeName(java.lang.String stringValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), stringValue, "http://www.w3.org/2001/XMLSchema#string");
		removeProperty(demo.tools.Vocabulary.name, literal);
	}

	public void removeAllName() {
		removeAll(demo.tools.Vocabulary.name);

	}

}