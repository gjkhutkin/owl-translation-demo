package demo.owl2java.model.jmodel.utils;

import demo.owl2java.model.jmodel.JBaseRestriction;
import demo.owl2java.model.jmodel.JClass;
import demo.owl2java.model.jmodel.JProperty;

import com.hp.hpl.jena.ontology.OntResource;


public class LogUtils {

	public static String toLogName(JBaseRestriction restriction) {
		return "Restriction " + LogUtils.toLogName(restriction.getOnClass(), restriction.getOnProperty());
	}

	public static String toLogName(JClass cls) {
		return cls.getJavaPackageName() + "." + cls.getName();
	}

	public static String toLogName(JClass cls, JProperty property) {
		return toLogName(cls) + "->" + LogUtils.toLogName(property);
	}

	public static String toLogName(JProperty prop) {
		return prop.getName();
	}

	public static String toLogName(OntResource res) {
		String ns = res.getNameSpace();
		if (res.getModel().getNsURIPrefix(ns) != null) {
			return res.getModel().getNsURIPrefix(res.getNameSpace()) + "#" + res.getLocalName();
		}
		return ns + res.getLocalName();
	}

}
