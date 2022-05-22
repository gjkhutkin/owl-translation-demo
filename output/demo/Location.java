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
import demo.ILocation;

/**
 * Class http://oss.fruct.org/etourism#Location
 */
public class Location extends IndividualImpl implements ILocation {

	private static Log log = LogFactory.getLog(Location.class);

	/**
	 * Implementation factory for Location
	 */
	static final public Implementation factory = new Implementation() {

		/**
		 * Convert a Node into an instance of the class
		 */
		public EnhNode wrap(Node n, EnhGraph eg) {
			if (canWrap(n, eg)) {
				return new Location(n, eg);
			} else {
				log.warn("Cannot convert node " + n.toString() + " to  Location");
				return null;
			}
		}

		/**
		 * Return true iff the node can be converted to an instance of
		 * this class (Location)
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
			return graph.contains(n, com.hp.hpl.jena.vocabulary.RDF.type.asNode(), demo.tools.Vocabulary.Location.asNode());
		}
	};

	/**
	 * Filtering support for Location
	 */
	static final public Filter<Location> nullFilter = new NullFilter<Location>();

	/**
	 * Mapping support for Location
	 */
	public static <From> Map1<From, Location> mapperFrom(Class<From> from) {
		return new Map1<From, Location>() {
			@Override
			public Location map1(Object x) {
				if (x instanceof Statement) {
					Resource r = ((Statement) x).getResource();
					if (r.canAs(Location.class))
						return r.as(Location.class);
				} else if (x instanceof RDFNode) {
					if (((RDFNode) x).canAs(Location.class))
						return ((RDFNode) x).as(Location.class);
				}
				return null;
			}
		};
	}

	// Instantiate some mappers for general use
	static final public Map1<Statement, Location> statementMapper = mapperFrom(Statement.class);
	static final public Map1<Individual, Location> individualMapper = mapperFrom(Individual.class);
	static final public Map1<RDFNode, Location> nodeMapper = mapperFrom(RDFNode.class);

	/**
	 * Generic functions from parent class
	 */
	public Location(Node n, EnhGraph g) {
		super(n, g);
	}

	/**
	 * Registers all custom classes with jena
	 */
	public static void register() {
		log.debug("Registering custom class Location with jena");
		BuiltinPersonalities.model.add(Location.class, Location.factory);
		BuiltinPersonalities.model.add(demo.Location.class, Location.factory);
	}

	/**
	 * Static Functions for instance handling
	 */
	public static Location get(String uri, OntModel ontModel) {
		Individual individual = ontModel.getIndividual(uri);
		return (demo.Location) individual.as(demo.Location.class);
	}

	public static Location get(String uri) {
		return get(uri, demo.tools.Factory.getDefaultModel());
	}

	public static Iterator<Location> iterate(OntModel ontModel) {
		ExtendedIterator<Individual> it = ontModel.listIndividuals(demo.tools.Vocabulary.Location);
		return it.mapWith(individualMapper).filterDrop(nullFilter);
	}

	public static Iterator<Location> iterate() {
		return iterate(demo.tools.Factory.getDefaultModel());
	}

	public static List<Location> list(OntModel ontModel) {
		List<Location> list = new ArrayList<Location>();
		Iterator<Location> it = iterate(ontModel);
		while (it.hasNext()) {
			Location cls = it.next();
			list.add(cls);
		}
		return list;
	}

	public static List<Location> list() {
		return list(demo.tools.Factory.getDefaultModel());
	}

	public static Iterator<Location> iterate(boolean direct, OntModel ontModel) {
		OntClass cls = ontModel.getOntClass("http://oss.fruct.org/etourism#Location");
		ExtendedIterator<? extends RDFNode> it = cls.listInstances(direct);
		ExtendedIterator<RDFNode> nodeIt = new WrappedIterator<RDFNode>(it) {
		};
		return nodeIt.mapWith(nodeMapper).filterDrop(nullFilter);
	}

	public static Iterator<Location> iterate(boolean direct) {
		return iterate(direct, demo.tools.Factory.getDefaultModel());
	}

	public static List<Location> list(boolean direct, OntModel ontModel) {
		List<Location> list = new ArrayList<Location>();
		Iterator<Location> it = iterate(direct, ontModel);
		while (it.hasNext()) {
			Location cls = it.next();
			list.add(cls);
		}
		return list;
	}

	public static List<Location> list(boolean direct) {
		return list(direct, demo.tools.Factory.getDefaultModel());
	}

	public static int count(OntModel ontModel) {
		int count = 0;
		Iterator<Location> it = iterate(ontModel);
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
		Iterator<Location> it = iterate(direct, ontModel);
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

	public static Location create(String uri, OntModel ontModel) {
		return (Location) ontModel.createOntResource(Location.class, demo.tools.Vocabulary.Location, uri);
	}

	public static Location create(OntModel ontModel) {
		return create(null, ontModel);
	}

	public static Location create(String uri) {
		return create(uri, demo.tools.Factory.getDefaultModel());
	}

	public static Location create() {
		return create(null, demo.tools.Factory.getDefaultModel());
	}

	public static void delete(String uri, OntModel ontModel) {
		demo.tools.Factory.deleteInstance(uri, ontModel);
	}

	public static void delete(String uri) {
		demo.tools.Factory.deleteInstance(uri);
	}

	/**
	 * Domain property lon
	 * with uri http://oss.fruct.org/etourism#lon
	 */
	public boolean existsLon() {
		return hasProperty(demo.tools.Vocabulary.lon);
	}

	public boolean hasLon(java.lang.Double doubleValue) {
		return hasProperty(demo.tools.Vocabulary.lon);
	}

	public int countLon() {
		int count = 0;
		Iterator<java.lang.Double> it = iterateLon();
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public Iterator<java.lang.Double> iterateLon() {
		ExtendedIterator<Statement> it = listProperties(demo.tools.Vocabulary.lon);
		return it.mapWith(demo.owl2java.model.xsd.XsdUtils.objectAsDoubleMapper).filterDrop(new NullFilter<java.lang.Double>());
	}

	public List<java.lang.Double> listLon() {
		List<java.lang.Double> list = new ArrayList<java.lang.Double>();
		Iterator<java.lang.Double> it = iterateLon();
		while (it.hasNext()) {
			java.lang.Double inst = it.next();
			list.add(inst);
		}
		return list;
	}

	public void addLon(java.lang.Double doubleValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), doubleValue, "http://www.w3.org/2001/XMLSchema#double");
		setPropertyValue(demo.tools.Vocabulary.lon, literal);
	}

	public void addAllLon(List<java.lang.Double> doubleList) {
		for (java.lang.Double o : doubleList)
			addLon(o);
	}

	public void removeLon(java.lang.Double doubleValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), doubleValue, "http://www.w3.org/2001/XMLSchema#double");
		removeProperty(demo.tools.Vocabulary.lon, literal);
	}

	public void removeAllLon() {
		removeAll(demo.tools.Vocabulary.lon);

	}

	/**
	 * Domain property lat
	 * with uri http://oss.fruct.org/etourism#lat
	 */
	public boolean existsLat() {
		return hasProperty(demo.tools.Vocabulary.lat);
	}

	public boolean hasLat(java.lang.Double doubleValue) {
		return hasProperty(demo.tools.Vocabulary.lat);
	}

	public int countLat() {
		int count = 0;
		Iterator<java.lang.Double> it = iterateLat();
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

	public Iterator<java.lang.Double> iterateLat() {
		ExtendedIterator<Statement> it = listProperties(demo.tools.Vocabulary.lat);
		return it.mapWith(demo.owl2java.model.xsd.XsdUtils.objectAsDoubleMapper).filterDrop(new NullFilter<java.lang.Double>());
	}

	public List<java.lang.Double> listLat() {
		List<java.lang.Double> list = new ArrayList<java.lang.Double>();
		Iterator<java.lang.Double> it = iterateLat();
		while (it.hasNext()) {
			java.lang.Double inst = it.next();
			list.add(inst);
		}
		return list;
	}

	public void addLat(java.lang.Double doubleValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), doubleValue, "http://www.w3.org/2001/XMLSchema#double");
		setPropertyValue(demo.tools.Vocabulary.lat, literal);
	}

	public void addAllLat(List<java.lang.Double> doubleList) {
		for (java.lang.Double o : doubleList)
			addLat(o);
	}

	public void removeLat(java.lang.Double doubleValue) {
		Literal literal = demo.owl2java.model.xsd.XsdUtils.createTypedLiteral((OntModel) getModel(), doubleValue, "http://www.w3.org/2001/XMLSchema#double");
		removeProperty(demo.tools.Vocabulary.lat, literal);
	}

	public void removeAllLat() {
		removeAll(demo.tools.Vocabulary.lat);

	}

}