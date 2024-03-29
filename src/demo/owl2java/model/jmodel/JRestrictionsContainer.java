package demo.owl2java.model.jmodel;

import java.util.ArrayList;
import java.util.List;

import demo.owl2java.model.jmodel.utils.LogUtils;
import demo.owl2java.utils.IReporting;
import demo.owl2java.utils.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class JRestrictionsContainer implements IReporting {

	private static Log log = LogFactory.getLog(JRestrictionsContainer.class);

	private JClass onClass;
	private JProperty onProperty;

	private JCardinalityRestriction cardinalityRestriction;
	private List<JAllValuesRestriction> allValuesRestrictions = new ArrayList<JAllValuesRestriction>();
	private JOtherRestriction otherRestriction;

	public JRestrictionsContainer(JClass onClass, JProperty onProperty) {
		this.onClass = onClass;
		this.onProperty = onProperty;
		cardinalityRestriction = new JCardinalityRestriction(onClass, onProperty);
		otherRestriction = new JOtherRestriction(onClass, onProperty);
		
		onClass.addDomainRestrictionsContainer(onProperty, this);
		onProperty.addRestrictionsContainer(onClass, this);
	}
	
	public boolean hasCardinalityRestriction() {
		if (cardinalityRestriction == null)
			return false;
		return true;
	}
	
	public boolean hasOtherRestriction() {
		if (otherRestriction == null)
			return false;
		return true;
	}

	public JOtherRestriction getOtherRestriction() {
		return otherRestriction;
	}

	public List<JAllValuesRestriction> listAllValuesRestrictions() {
		return allValuesRestrictions;
	}

	public void aggregateRestrictions(List<JClass> parentClasses) {
		for (JClass cls : parentClasses) {
			log.debug(LogUtils.toLogName(onClass, onProperty)
					+ ": Aggregating restrictions of parent class " + LogUtils.toLogName(cls));
			JRestrictionsContainer restrictions = cls.getAggregatedRestrictionsContainer(onProperty);
			if (restrictions == null)
				continue;
			cardinalityRestriction.mergeParent(restrictions.getCardinalityRestriction());
			otherRestriction.mergeParent(restrictions.getOtherRestriction());
			for (JAllValuesRestriction restriction : restrictions.listAllValuesRestrictions()) {
				if (!allValuesRestrictions.contains(restriction))
					allValuesRestrictions.add(restriction.clone());
			}
		}
	}

	public JCardinalityRestriction getCardinalityRestriction() {
		return cardinalityRestriction;
	}

	public JRestrictionsContainer clone() {
		JRestrictionsContainer rc = new JRestrictionsContainer(onClass, onProperty);
		rc.cardinalityRestriction = cardinalityRestriction.clone();
		rc.otherRestriction = otherRestriction.clone();
		for (JAllValuesRestriction avr : allValuesRestrictions) {
			JAllValuesRestriction r = avr.clone();
			rc.allValuesRestrictions.add(r);
		}
		return rc;
	}

	public String getJModelReport() {
		String report = LogUtils.toLogName(onClass, onProperty) + " Restriction Container:\n";
		report += StringUtils.indentText(cardinalityRestriction.getJModelReport()+"\n", 1) ;
		report += StringUtils.indentText(otherRestriction.getJModelReport(), 1);
		if (!allValuesRestrictions.isEmpty())
			report += "\n";
		for (JAllValuesRestriction r : allValuesRestrictions) {
			report += StringUtils.indentText(r.getJModelReport()+"\n" , 1);
		}
		return report;
	}

	public void addAllValuesRestriction(JClass allValuesJClass) {
		JAllValuesRestriction avr = new JAllValuesRestriction(onClass, onProperty);
		avr.setAllValues(allValuesJClass);
		allValuesRestrictions.add(avr);
	}
}
