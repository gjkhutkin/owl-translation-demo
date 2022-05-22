package demo.owl2java.model.jmodel;

import demo.owl2java.model.jmodel.utils.LogUtils;
import demo.owl2java.utils.IReporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class JBaseRestriction implements IReporting {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JBaseRestriction.class);

	JModel jModel;
	JClass onClass;
	JProperty onProperty;

	boolean isEmpty = true;

	public JBaseRestriction(JClass onClass, JProperty onProperty) {
		this.jModel = onClass.getJModel();
		this.onClass = onClass;
		this.onProperty = onProperty;
	}

	public String getJModelReport() {
		return (LogUtils.toLogName(this) + ": Base restriction");
	}

	public JProperty getOnProperty() {
		return onProperty;
	}

	public JClass getOnClass() {
		return onClass;
	}

}
