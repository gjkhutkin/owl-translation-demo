package demo.owl2java.generator;

import java.util.ArrayList;
import java.util.List;

import demo.owl2java.model.jenautils.RestrictionUtils;
import demo.owl2java.model.jmodel.JClass;
import demo.owl2java.model.jmodel.JInheritanceGraph;
import demo.owl2java.model.jmodel.JModel;
import demo.owl2java.model.jmodel.JProperty;
import demo.owl2java.model.jmodel.JRestrictionsContainer;
import demo.owl2java.model.jmodel.utils.LogUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;


public class ModelPreparation {

	private static Log log = LogFactory.getLog(ModelPreparation.class);

	private boolean reasignDomainlessProperties;
	private JModel jModel;

	JClass baseCls;

	public JModel prepareModel(JModel model) {
		jModel = model;
		baseCls = jModel.getJClass(jModel.getBaseThingUri());
		log.info("");
		log.info("Prepaing model for class writer");

		if (reasignDomainlessProperties)
			reassignProperties();

		createDomainPropertyRepresentations();

		int aggregateActions = 1;
		while (aggregateActions != 0) {
			aggregateActions = aggregateAllOnClass();
		}


		return jModel;
	}

	protected void createDomainPropertyRepresentations() {
		log.info("Creating domain property representations");
		JClass baseCls = jModel.getBaseThing();
		JInheritanceGraph<JClass, DefaultEdge> classGraph = jModel.getClassGraph();
		BreadthFirstIterator<JClass, DefaultEdge> it = new BreadthFirstIterator<JClass, DefaultEdge>(classGraph,
				baseCls);
		it.setCrossComponentTraversal(true);
		while (it.hasNext()) {
			JClass c = (JClass) it.next();
			c.createDomainPropertyRepresentations();
		}
	}

	protected int aggregateAllOnClass() {
		int aggregateActions = 0;
		JClass baseCls = jModel.getBaseThing();
		JInheritanceGraph<JClass, DefaultEdge> classGraph = jModel.getClassGraph();
		BreadthFirstIterator<JClass, DefaultEdge> it = new BreadthFirstIterator<JClass, DefaultEdge>(classGraph,
				baseCls);
		it.setCrossComponentTraversal(true);
		while (it.hasNext()) {
			JClass c = (JClass) it.next();
			int count = c.aggegrateAll();
			aggregateActions = aggregateActions+count;
		}
		log.info("Aggregating all from parent classes: " + aggregateActions + " Actions");
		return aggregateActions;
	}

	protected boolean reasignProperty(JClass cls, JProperty property) {

		boolean success = false;

		OntClass ontClass = cls.getOntClass();
		OntProperty ontProperty = property.getOntProperty();

		if (RestrictionUtils.hasRestrictionOnProperty(ontClass, ontProperty)) {
			log.debug(LogUtils.toLogName(property) + ": Changing domain from " + LogUtils.toLogName(baseCls) + " to "
					+ LogUtils.toLogName(cls));
			property.addDomain(cls);

			JRestrictionsContainer rc = cls.getDomainRestrictionsContainer(property);
			if (rc != null) {
				if (rc.hasCardinalityRestriction()) {
					if (rc.getCardinalityRestriction().getMaxCardinality() == 1) {
						rc.getCardinalityRestriction().setMultipleEnabled(false);
						rc.getCardinalityRestriction().setSingleEnabled(true);

						property.setFunctional(true);
					}
					if (rc.getCardinalityRestriction().getMaxCardinality() == 0) {
						rc.getCardinalityRestriction().setMultipleEnabled(false);
						rc.getCardinalityRestriction().setSingleEnabled(false);
					}
				}
			}
			success = true;
		} else {
			for (JClass subCls : cls.listDirectSubClasses()) {
				boolean suc = reasignProperty(subCls, property);
				if (suc)
					success = true;
			}
		}
		return success;
	}

	protected void reassignProperties() {
		log.info("Reassigning unbound properties to corresponding classes with restrictions");

		List<JProperty> properties = baseCls.listDomainProperties();
		List<JProperty> propsToRemove = new ArrayList<JProperty>();

		for (JProperty property : properties) {
			boolean success = reasignProperty(baseCls, property);
			if (success)
				propsToRemove.add(property);
		}

		for (JProperty prop : propsToRemove) {
			prop.removeDomain(baseCls);
			prop.removeClassRestrictions(baseCls);
			baseCls.removeDomainRestrictionsContainer(prop);
			
		}
	}

	public void setReassignDomainlessProperties(boolean reasignDomainlessProperties) {
		this.reasignDomainlessProperties = reasignDomainlessProperties;
	}
}
