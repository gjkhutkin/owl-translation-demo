package demo.tools;

import java.util.Map;
import java.util.HashMap;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.OntDocumentManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory
 */
public class Factory {

	private static Log log = LogFactory.getLog(Factory.class);

	private static OntModel defaultModel = null;

	private static Map<String, String> uri2Type;

	static {
		uri2Type = new HashMap<String, String>();
		uri2Type.put("http://oss.fruct.org/etourism#Location", "demo.ILocation");
		uri2Type.put("http://oss.fruct.org/etourism#User", "demo.IUser");
		uri2Type.put("http://www.w3.org/2002/07/owl#Thing", "demo.IThing");
	}

	/**
	 * Sets the default ontModel that may be used by ontology wrapper classes when no explicit ontModel argument is provided
	 */
	public static void setDefaultModel(OntModel defaultModel) {
		Factory.defaultModel = defaultModel;
	}

	/**
	 * Returns the default ontModel set with setDefaultModel()
	 */
	public static OntModel getDefaultModel() {
		if (defaultModel == null) {
			throw new RuntimeException("No default OntModel was provided to demo.tools.Factory");
		}
		return defaultModel;
	}

	/**
	 * Returns the interface name for a given OWL Class
	 */
	public static String getJavaInterfaceName(String uri) {
		return uri2Type.get(uri);
	}

	/**
	 * Returns true if there is a java interface for the
	 * given OWL Class
	 */
	public static boolean hasJavaType(String uri) {
		return uri2Type.containsKey(uri);
	}

	/**
	 * Deletes the individual with URI from the OntModel
	 */
	public static boolean deleteInstance(String uri, OntModel ontModel) {
		Individual individual = ontModel.getIndividual(uri);
		if (individual != null) {
			individual.remove();
			return true;
		}
		log.warn("Could not remove non existing instance " + uri + " from model");
		return false;
	}

	/**
	 * Same as deleteInstance, but works with the default OntModel
	 * @see Factory#deleteInstance(String, OntModel)
	 */
	public static boolean deleteInstance(String uri) {
		return deleteInstance(uri, getDefaultModel());
	}

	/**
	 * Registers all custom classes with jena
	 */
	public static void registerCustomClasses() {
		log.info("Registering custom classes with jena");
		demo.Location.register();
		demo.User.register();
		demo.Thing.register();
	}

	/**
	 * Adds imports statements to an ontology and adds
	 * imported subModels to a model.
	 *
	 * Currently, this uses the namespace URI without trailing '#' or ':'
	 * as location.
	 */
	public static void registerImports(Ontology ontology, OntModel ontModel) {
		log.info("Adding import statements to the model");
		OntDocumentManager odm = OntDocumentManager.getInstance();
		log.debug("Adding import http://oss.fruct.org/etourism to the model");
		odm.loadImport(ontModel, "http://oss.fruct.org/etourism");
		ontology.addImport(ontModel.createResource("http://oss.fruct.org/etourism"));
	}

	/**
	 * Same as registerImports, but works with the default OntModel
	 * @see Factory#registerImports(Ontology, OntModel)
	 */
	public void registerImports(Ontology ontology) {
		registerImports(ontology, getDefaultModel());
	}
}