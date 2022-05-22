package demo.owl2java.model.jenautils;

import java.util.Iterator;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;

public class RestrictionUtils {
	@SuppressWarnings("unchecked")
	public static boolean hasRestrictionOnProperty(OntClass cls, OntProperty property) {
		Iterator restrictionIt = property.listReferringRestrictions();
		while (restrictionIt.hasNext()) {
			Restriction restriction = (Restriction) restrictionIt.next();
			Iterator subClassIt = restriction.listSubClasses();
			while (subClassIt.hasNext()) {
				OntClass ontClass = (OntClass) subClassIt.next();
				if (ontClass.equals(cls))
					return true;
			}
		}
		return false;
	}
}
