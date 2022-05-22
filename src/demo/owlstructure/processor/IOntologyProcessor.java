package demo.owlstructure.processor;

import com.hp.hpl.jena.ontology.OntModel;

public interface IOntologyProcessor {
	public OntModel process(OntModel ontModel);
}
