package demo.owlstructure.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import demo.owlstructure.utils.CollectionUtils;
import demo.owlstructure.utils.OntologyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

public class PropertyDomainInferer implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);

	private boolean keepExistingDomains;
	private boolean enableGeneralizion;
	private boolean allowThingDomain;

	public PropertyDomainInferer(boolean keepExistingDomains, boolean enableGeneralization, boolean allowThingDomain) {
		this.keepExistingDomains = keepExistingDomains;
		this.enableGeneralizion = enableGeneralization;
		this.allowThingDomain = allowThingDomain;
	}

	public PropertyDomainInferer(boolean keepExistingDomains, boolean enableGeneralization) {
		this(keepExistingDomains, enableGeneralization, false);
	}

	public PropertyDomainInferer(boolean keepExistingDomains) {
		this(keepExistingDomains, true);
	}
	public PropertyDomainInferer() {
		this(true);
	}

	@Override
	public OntModel process(OntModel ontModel) {
		Collection<OntProperty> properties = ontModel.listAllOntProperties().toList();
		for(OntProperty property: properties) {
			Collection<? extends OntResource> oldDomains = property.listDomain().toList();
			Collection<OntClass> newDomains = findPropertyDomains(ontModel, property);

			for (OntResource domainClass: oldDomains) {
				property.removeDomain(domainClass);
			}
			for (OntResource domainClass: newDomains) {
				property.addDomain(domainClass);
			}

			log.info("Property domain inference for property: " + property.getLocalName() + " \n"
					+ getLogMessage("retaining domain(s)", CollectionUtils.intersectCollections(oldDomains, newDomains)) + "\n"
					+ getLogMessage("adding domain(s)", CollectionUtils.subtractCollections(newDomains, oldDomains)) + "\n"
					+ getLogMessage("removing domain(s)", CollectionUtils.subtractCollections(oldDomains, newDomains)));
		}

		return ontModel;
	}

	private Collection<OntClass> findPropertyDomains(OntModel ontModel, OntProperty property) {
		HashSet<OntClass> domainClasses = new HashSet<OntClass>();
		if (keepExistingDomains) {
			Iterator<? extends OntResource> domainIterator = property.listDomain();
			while (domainIterator.hasNext()) {
				domainClasses.add(domainIterator.next().as(OntClass.class));
			}
		}
		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select distinct ?domain "
				+ "where { "
				+ "  ?s <" + property.getURI() + "> ?o . "
				+ "  ?s rdf:type ?domain . "
				+ "} ";
		ontModel.enterCriticalSection(Lock.READ);

		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				OntClass domainClass = results.nextSolution().get("domain").as(OntClass.class);
				if (someDirectInstancesHaveProperty(ontModel, property, domainClass)) {
					domainClasses.add(domainClass.as(OntClass.class));
				}
			}
			qexec.close();
		} finally {
			ontModel.leaveCriticalSection();
		}

		if (this.enableGeneralizion) {
			Set<OntClass> allClasses = ontModel.listClasses().toSet();
			for (OntClass ontClass: allClasses) {
				Set<OntClass> branches = new HashSet<OntClass>(OntologyUtils.listClassDescendants(ontClass));
				branches.retainAll(domainClasses);
				Iterator<OntClass> branchIterator = domainClasses.iterator();
				while (branchIterator.hasNext()) {
					OntClass branch = branchIterator.next();
					if (OntologyUtils.containsClassSuperset(branch, branches)) {
						branchIterator.remove();
					}
				}
				if (branches.size() > 1) {
					if (allIndirectInstancesHaveProperty(ontModel, property, ontClass, domainClasses)) {
						domainClasses.add(ontClass);
					}
				}

			}
		}
		if (!this.allowThingDomain) {
			domainClasses.remove(OntologyUtils.getOwlThing(ontModel));
		}
		Iterator<OntClass> classIterator = domainClasses.iterator();
		while (classIterator.hasNext()) {
			OntClass domainClass = classIterator.next();
			if (OntologyUtils.containsClassSuperset(domainClass, domainClasses)) {
				classIterator.remove();
			}
		}

		return domainClasses;
	}

	private boolean someDirectInstancesHaveProperty(OntModel ontModel, OntProperty property, OntClass ontClass) {
		Collection<OntClass> descendants = OntologyUtils.listClassDescendants(ontClass);

		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select (count(?o) as ?count) "
				+ "where { "
				+ "  ?s <" + property.getURI() + "> ?o . "
				+ "  ?s rdf:type <" + ontClass.getURI() + "> . ";
		for (OntClass descendant : descendants) {
			queryString += "  unsaid { ?s rdf:type <" + descendant.getURI() + "> } . ";
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

	private boolean allIndirectInstancesHaveProperty(OntModel ontModel, OntProperty property, OntClass ontClass, Collection<OntClass> ignoreBranches) {
		Set<OntClass> ignoreClasses = new HashSet<OntClass>(ignoreBranches);
		ignoreClasses.retainAll(OntologyUtils.listClassDescendants(ontClass));
		for (OntClass ignoreClass: ignoreBranches) {
			ignoreClasses.add(ignoreClass);
			ignoreClasses.addAll(OntologyUtils.listClassDescendants(ignoreClass));
		}

		Collection<OntClass> includeClasses = OntologyUtils.listClassDescendants(ontClass);
		includeClasses.removeAll(ignoreClasses);

		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select (count(?o) as ?count) "
				+ "where { "
				+ "  ?o rdf:type ?t . "
				+ "  unsaid { ?s <" + property.getURI() + "> ?o } . ";
		for (OntClass descendant : ignoreClasses) {
			queryString +=
				  "  unsaid { ?o rdf:type <" + descendant.getURI() + "> } . ";
		}
		queryString +=
				  "  filter ( "
				+ "       ?t = <" + ontClass.getURI() + "> ";
		for (OntClass descendant : includeClasses) {
			queryString +=
				  "    || ?t = <" + descendant.getURI() + "> ";
		}
		queryString +=
				  "  ) . "
				+ "} ";

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

		if (instanceCount == 0) {
			return true;
		} else {
			return false;
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