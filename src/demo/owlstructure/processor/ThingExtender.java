package demo.owlstructure.processor;

import java.util.Collection;

import demo.owlstructure.utils.OntologyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

public class ThingExtender implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);

	public static enum Target {
		TOP_CLASSES,
		ALL_CLASSES
	}

	private Target target;

	public ThingExtender(Target topClasses) {
		this.target = topClasses;
	}

	public ThingExtender() {
		this(Target.TOP_CLASSES);
	}

	@Override
	public OntModel process(OntModel ontModel) {
		OntClass Thing = OntologyUtils.getOwlThing(ontModel);
		Collection<OntClass> ontClasses = ontModel.listClasses().toList();

		for (OntClass ontClass : ontClasses) {
			if (!ontClass.equals(Thing)) {
				switch (target) {
					case ALL_CLASSES:
						if (!ontClass.hasSuperClass(Thing)) {
							log.info("Adding owl:Thing superclass to " + ontClass.getLocalName());
							ontClass.addSuperClass(Thing);
						}
						break;

					case TOP_CLASSES:
						if (ontClass.listSuperClasses().toList().isEmpty()) {
							log.info("Adding owl:Thing superclass to " + ontClass.getLocalName());
							ontClass.addSuperClass(Thing);
						}
						break;
				}
			}
		}
		return ontModel;
	}
}