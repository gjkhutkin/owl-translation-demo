package demo;

import demo.owl2java.JenaGenerator;
import demo.owlstructure.processor.PropertyRangeSimplifier;
import demo.owlstructure.utils.OntologyUtils;

import com.hp.hpl.jena.ontology.OntModel;

import java.io.File;

class Main {

	public static void main(String[] args) {
		try {
			OntModel ontModel = OntologyUtils.loadOntology(new File(args[0]).getAbsolutePath());
			(new PropertyRangeSimplifier()).process(ontModel);
			JenaGenerator generator = new JenaGenerator();
			generator.generate(ontModel, "output", "demo");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
