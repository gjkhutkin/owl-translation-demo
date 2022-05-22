package demo.owlstructure.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import demo.owlstructure.utils.CollectionUtils;
import demo.owlstructure.utils.OntologyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

public class PropertyRangeInferer implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);

	private boolean keepExistingRanges;
	private boolean allowThingRange;

	public PropertyRangeInferer(boolean keepExistingRanges, boolean allowThingRange) {
		this.keepExistingRanges = keepExistingRanges;
		this.allowThingRange = allowThingRange;
	}

	public PropertyRangeInferer(boolean keepExistingRanges) {
		this(keepExistingRanges, false);
	}

	public PropertyRangeInferer() {
		this(true, false);
	}

	@Override
	public OntModel process(OntModel ontModel) {
		Collection<OntProperty> properties = ontModel.listAllOntProperties().toList();
		for(OntProperty property: properties) {
			Collection<Resource> oldRanges = new HashSet<Resource>(property.listRange().toList());
			Collection<Resource> newRanges = findPropertyRanges(ontModel, property);

			for (Resource range: oldRanges) {
				property.removeRange(range);
			}
			for (Resource range: newRanges) {
				property.addRange(range);
			}

			log.info("Property range inference for property: " + property.getLocalName() + "\n"
					+ getLogMessage("retaining range(s)", CollectionUtils.intersectCollections(oldRanges, newRanges)) + "\n"
					+ getLogMessage("adding range(s)", CollectionUtils.subtractCollections(newRanges, oldRanges)) + "\n"
					+ getLogMessage("removing range(s)", CollectionUtils.subtractCollections(oldRanges, newRanges)));
		}

		return ontModel;
	}

	private Collection<Resource> findPropertyRanges(OntModel ontModel, OntProperty property) {
		HashSet<Resource> rangeDatatypes = new HashSet<Resource>();
		HashSet<OntClass> rangeClasses = new HashSet<OntClass>();

		if (this.keepExistingRanges) {
			Iterator<? extends Resource> rangeIterator = property.listRange();
			while (rangeIterator.hasNext()) {
				Resource range = rangeIterator.next();
				if (range.canAs(OntClass.class)) {
					rangeClasses.add(range.as(OntClass.class));
				} else {
					rangeDatatypes.add(range);
				}
			}
		}

		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select distinct ?class (datatype(?o) as ?datatype) "
				+ "where { "
				+ "  ?s <" + property.getURI() + "> ?o . "
				+ "  optional { ?o rdf:type ?class } . "
				+ "} ";
		ontModel.enterCriticalSection(Lock.READ);
		try {
			Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
			QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

			ResultSet results = qexec.execSelect();
				while(results.hasNext()) {
					QuerySolution result = results.nextSolution();

					if (result.contains("datatype")) {
						rangeDatatypes.add(result.get("datatype").as(Resource.class));
					} else if (result.contains("class")) {
						OntClass ontClass = result.get("class").as(OntClass.class);
						if (anyInstanceRefersToDirectClassInstance(ontModel, property, ontClass)) {
							rangeClasses.add(ontClass);
						}
					}
				}
				qexec.close();
		} finally {
			ontModel.leaveCriticalSection();
		}

		if (!allowThingRange) {
			rangeClasses.remove(OntologyUtils.getOwlThing(ontModel));
		}

		for (OntClass ontClass: rangeClasses.toArray(new OntClass[]{})) {
			if (OntologyUtils.containsCompleteClassSuperset(ontClass, rangeClasses)) {
				rangeClasses.remove(ontClass);
			}
		}

		HashSet<Resource> ranges = new HashSet<Resource>();
		ranges.addAll(rangeDatatypes);
		ranges.addAll(rangeClasses);
		return ranges;
	}

	private boolean anyInstanceRefersToDirectClassInstance(OntModel ontModel, OntProperty property, OntClass ontClass) {
		Collection<OntClass> descendants = OntologyUtils.listClassDescendants(ontClass);

		if (descendants.isEmpty()) {
			return true;

		} else {
			String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
					+ "select (count(?s) as ?count) "
					+ "where { "
					+ "  ?s <" + property.getURI() + "> ?o . "
					+ "  ?o rdf:type <" + ontClass.getURI() + "> . ";
			for (OntClass descendant : descendants) {
				queryString += "  unsaid { ?o rdf:type <" + descendant.getURI() + "> } . ";
			}
			queryString += "} ";

			ontModel.enterCriticalSection(Lock.READ);
			int instanceCount;
			try {
				Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
				QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

				ResultSet results = qexec.execSelect();
				if (results.hasNext()) {
					instanceCount = results.nextSolution().getLiteral("count").getInt();
				} else {
					instanceCount = 0;
				}
				OntologyUtils.closeIterator(results);
				qexec.close();
			} finally {
				ontModel.leaveCriticalSection();
			}

			if (instanceCount > 0) {
				return true;
			} else {
				return false;
			}
		}
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