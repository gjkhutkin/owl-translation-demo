package demo.owlstructure.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import demo.owlstructure.utils.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Simplify complex intersectionOf / unionOf property ranges (as created by a.o. Protege)
 * to a simple flat list of named classes, and deletes the anonymous classes from the ontology.
 *
 * This is needed because Owl2Java chokes on complex property ranges;
 * therefore maybe this should be integrated in Owl2Java?
 */
public class PropertyRangeSimplifier implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);

	public PropertyRangeSimplifier() {}

	@Override
	public OntModel process(OntModel ontModel) {
		Collection<OntProperty> properties = ontModel.listAllOntProperties().toList();
		for(OntProperty property: properties) {
			Collection<Resource> oldRanges = new HashSet<Resource>(property.listRange().toList());
			Collection<Resource> newRanges = simplifyRange(ontModel, oldRanges);

			for (Resource range: oldRanges) {
				property.removeRange(range);
			}

			for (Resource range: newRanges) {
				property.addRange(range);
			}

			log.info("Property range simplification for property: " + property.getLocalName() + "\n"
					+ getLogMessage("retaining range(s)", CollectionUtils.intersectCollections(oldRanges, newRanges)) + "\n"
					+ getLogMessage("adding range(s)", CollectionUtils.subtractCollections(newRanges, oldRanges)) + "\n"
					+ getLogMessage("removing range(s)", CollectionUtils.subtractCollections(oldRanges, newRanges)));

		}

		return ontModel;
	}

	private Collection<Resource> simplifyRange(OntModel ontModel, Collection<Resource> oldRanges) {
		HashSet<Resource> newRanges = new HashSet<Resource>();

		Iterator<Resource> rangeIterator = oldRanges.iterator();
		while (rangeIterator.hasNext()) {
			Resource range = rangeIterator.next();
			if (range.canAs(OntClass.class)) {
				OntClass rangeClass = range.as(OntClass.class);
				if (rangeClass.isAnon()) {
					if (rangeClass.isIntersectionClass()) {
						IntersectionClass intersectionClass = rangeClass.asIntersectionClass();
						newRanges.addAll(simplifyRange(ontModel, new HashSet<Resource>(intersectionClass.listOperands().toSet())));
						intersectionClass.remove();
						continue;
					}
					if (rangeClass.isUnionClass()) {
						UnionClass unionClass = rangeClass.asUnionClass();
						newRanges.addAll(simplifyRange(ontModel, new HashSet<Resource>(unionClass.listOperands().toSet())));
						unionClass.remove();
						continue;
					}
				}
			}

			newRanges.add(range);
		}

		return newRanges;
	}

	private String getLogMessage(String message, Collection<? extends Resource> resources) {
		String result = "  - " + message + ": ";
		if (resources.size() > 0) {
			int counter = 0;
			for (Resource resource: resources) {
				result += (counter++ > 0 ? ", " : "") + resource.getLocalName();
			}
		} else {
			result += "none";
		}
		return result;
	}
}