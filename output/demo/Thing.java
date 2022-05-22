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
import demo.IThing;

/**
 * Class http://www.w3.org/2002/07/owl#Thing
 */
public class Thing extends IndividualImpl implements IThing {

	private static Log log = LogFactory.getLog(Thing.class);

	/**
	 * Implementation factory for Thing
	 */
	static final public Implementation factory = new Implementation() {

		/**
		 * Convert a Node into an instance of the class
		 */
		public EnhNode wrap(Node n, EnhGraph eg) {
			if (canWrap(n, eg)) {
				return new Thing(n, eg);
			} else {
				log.warn("Cannot convert node " + n.toString() + " to  Thing");
				return null;
			}
		}

		/**
		 * Return true iff the node can be converted to an instance of
		 * this class (Thing)
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
			return graph.contains(n, com.hp.hpl.jena.vocabulary.RDF.type.asNode(), demo.tools.Vocabulary.Thing.asNode()) || graph.contains(n, com.hp.hpl.jena.vocabulary.RDF.type.asNode(), demo.tools.Vocabulary.User.asNode()) || graph.contains(n, com.hp.hpl.jena.vocabulary.RDF.type.asNode(), demo.tools.Vocabulary.Location.asNode());
		}
	};

	/**
	 * Filtering support for Thing
	 */
	static final public Filter<Thing> nullFilter = new NullFilter<Thing>();

	/**
	 * Mapping support for Thing
	 */
	public static <From> Map1<From, Thing> mapperFrom(Class<From> from) {
		return new Map1<From, Thing>() {
			@Override
			public Thing map1(Object x) {
				if (x instanceof Statement) {
					Resource r = ((Statement) x).getResource();
					if (r.canAs(Thing.class))
						return r.as(Thing.class);
				} else if (x instanceof RDFNode) {
					if (((RDFNode) x).canAs(Thing.class))
						return ((RDFNode) x).as(Thing.class);
				}
				return null;
			}
		};
	}

	// Instantiate some mappers for general use
	static final public Map1<Statement, Thing> statementMapper = mapperFrom(Statement.class);
	static final public Map1<Individual, Thing> individualMapper = mapperFrom(Individual.class);
	static final public Map1<RDFNode, Thing> nodeMapper = mapperFrom(RDFNode.class);

	/**
	 * Generic functions from parent class
	 */
	public Thing(Node n, EnhGraph g) {
		super(n, g);
	}

	/**
	 * Registers all custom classes with jena
	 */
	public static void register() {
		log.debug("Registering custom class Thing with jena");
		BuiltinPersonalities.model.add(Thing.class, Thing.factory);
		BuiltinPersonalities.model.add(demo.Thing.class, Thing.factory);
	}

	/**
	 * Static Functions for instance handling
	 */
	public static Thing get(String uri, OntModel ontModel) {
		Individual individual = ontModel.getIndividual(uri);
		return (demo.Thing) individual.as(demo.Thing.class);
	}

	public static Thing get(String uri) {
		return get(uri, demo.tools.Factory.getDefaultModel());
	}

	public static Iterator<Thing> iterate(OntModel ontModel) {
		ExtendedIterator<Individual> it = ontModel.listIndividuals(demo.tools.Vocabulary.Thing);
		return it.mapWith(individualMapper).filterDrop(nullFilter);
	}

	public static Iterator<Thing> iterate() {
		return iterate(demo.tools.Factory.getDefaultModel());
	}

	public static List<Thing> list(OntModel ontModel) {
		List<Thing> list = new ArrayList<Thing>();
		Iterator<Thing> it = iterate(ontModel);
		while (it.hasNext()) {
			Thing cls = it.next();
			list.add(cls);
		}
		return list;
	}

	public static List<Thing> list() {
		return list(demo.tools.Factory.getDefaultModel());
	}

	public static Iterator<Thing> iterate(boolean direct, OntModel ontModel) {
		OntClass cls = ontModel.getOntClass("http://www.w3.org/2002/07/owl#Thing");
		ExtendedIterator<? extends RDFNode> it = cls.listInstances(direct);
		ExtendedIterator<RDFNode> nodeIt = new WrappedIterator<RDFNode>(it) {
		};
		return nodeIt.mapWith(nodeMapper).filterDrop(nullFilter);
	}

	public static Iterator<Thing> iterate(boolean direct) {
		return iterate(direct, demo.tools.Factory.getDefaultModel());
	}

	public static List<Thing> list(boolean direct, OntModel ontModel) {
		List<Thing> list = new ArrayList<Thing>();
		Iterator<Thing> it = iterate(direct, ontModel);
		while (it.hasNext()) {
			Thing cls = it.next();
			list.add(cls);
		}
		return list;
	}

	public static List<Thing> list(boolean direct) {
		return list(direct, demo.tools.Factory.getDefaultModel());
	}

	public static int count(OntModel ontModel) {
		int count = 0;
		Iterator<Thing> it = iterate(ontModel);
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
		Iterator<Thing> it = iterate(direct, ontModel);
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

	public static Thing create(String uri, OntModel ontModel) {
		return (Thing) ontModel.createOntResource(Thing.class, demo.tools.Vocabulary.Thing, uri);
	}

	public static Thing create(OntModel ontModel) {
		return create(null, ontModel);
	}

	public static Thing create(String uri) {
		return create(uri, demo.tools.Factory.getDefaultModel());
	}

	public static Thing create() {
		return create(null, demo.tools.Factory.getDefaultModel());
	}

	public static void delete(String uri, OntModel ontModel) {
		demo.tools.Factory.deleteInstance(uri, ontModel);
	}

	public static void delete(String uri) {
		demo.tools.Factory.deleteInstance(uri);
	}

}