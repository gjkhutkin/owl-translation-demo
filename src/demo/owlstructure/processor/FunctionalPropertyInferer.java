package demo.owlstructure.processor;

import demo.owlstructure.utils.OntologyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.Lock;

public class FunctionalPropertyInferer implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);
	public FunctionalPropertyInferer() {}

	@Override
	public OntModel process(OntModel ontModel) {
		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select ?property (max(?count) as ?max) { "
				+ "  select ?property (count(?o) as ?count) "
				+ "  where { "
				+ "    ?s ?property ?o . "
				+ "    ?property rdf:type ?t "
				+ "  } "
				+ "  group by ?s ?property "
				+ "} "
				+ "group by ?property "
				+ "having (?max = 1) ";

		ontModel.enterCriticalSection(Lock.READ);
		try {
			Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
			QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				OntProperty property = results.nextSolution().get("property").as(OntProperty.class);
				property.convertToFunctionalProperty();
				log.info("Functional property inference: " + property.getLocalName() + " is functional");
			}
			qexec.close();
		} finally {
			ontModel.leaveCriticalSection();
		}

		return ontModel;
	}
}
