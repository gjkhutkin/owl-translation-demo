package demo.owl2java.model.jmodel.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import demo.owl2java.model.jmodel.JModel;
import demo.owl2java.model.jmodel.JPackage;
import demo.owl2java.model.ns.NamespaceUtils;
import demo.owl2java.utils.JavaUtils;
import demo.owl2java.utils.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


public class NamingUtils {

	private static Log log = LogFactory.getLog(NamingUtils.class);

	public static int anonCounter = 0;
	public static String anonPrefix = "Anon";

	public static String unionClassPrefix = "";
	public static String unionClassGlue = "And";

	public static String intersectionClassPrefix = "";
	public static String intersectionClassGlue = "Or";

	public static String classNameAddOn = "";
	public static String classNamingSchema = "%c%n";

	public static String interfaceNameAddOn = "I";
	public static String interfaceNamingSchema = "%i%n";

	public static String propertyNamingSchema = "%n";
	public static boolean propertyStripPrefix = true;

	public static List<String> propertyIgnoredPrefixes;

	static {
		propertyIgnoredPrefixes = new ArrayList<String>();
		propertyIgnoredPrefixes.add("has");
		propertyIgnoredPrefixes.add("is");
	}

	public static String getJavaClassName(OntClass ontClass) {
		String nsUri = ontClass.getNameSpace();
		String prefix = ontClass.getModel().getNsURIPrefix(nsUri);

		if (NamespaceUtils.defaultNs2UriMapping.containsKey(nsUri))
			prefix = JModel.BASEPREFIX;

		String localName = ontClass.getLocalName();
		if (prefix != null) {
			prefix = StringUtils.toFirstUpperCase(prefix);
		} else {
			prefix = JModel.BASEPREFIX;
		}

		String name = classNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		name = name.replace("%c", classNameAddOn);
		return StringUtils.toFirstUpperCase(name);
	}

	public static String getJavaClassName(String localName, String prefix) {
		String name = classNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		name = name.replace("%c", classNameAddOn);
		return StringUtils.toFirstUpperCase(name);
	}

	public static String getJavaFullName(JPackage pkg, String className) {
		String name = pkg.getJavaName() + "." + className;
		return name;
	}

	public static String getJavaInterfaceName(OntClass ontClass) {
		String nsUri = ontClass.getNameSpace();
		String prefix = ontClass.getModel().getNsURIPrefix(nsUri);

		if (NamespaceUtils.defaultNs2UriMapping.containsKey(nsUri))
			prefix = JModel.BASEPREFIX;

		String localName = ontClass.getLocalName();
		if (prefix != null) {
			prefix = StringUtils.toFirstUpperCase(prefix);
		} else {
			prefix = JModel.BASEPREFIX;
		}

		String name = interfaceNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		name = name.replace("%i", interfaceNameAddOn);
		return StringUtils.toFirstUpperCase(name);
	}

	public static String getJavaInterfaceName(String localName, String prefix) {
		String name = interfaceNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		name = name.replace("%i", interfaceNameAddOn);
		return StringUtils.toFirstUpperCase(name);
	}

	public static String getJavaPackageName(String basePackage, String prefix) {
		if (prefix != JModel.BASEPREFIX)
			return basePackage + "." + prefix;
		return basePackage;
	}

	public static String getPropertyName(OntProperty ontProperty) {
		String nsUri = ontProperty.getNameSpace();
		String prefix = ontProperty.getModel().getNsURIPrefix(nsUri);
		String localName = ontProperty.getLocalName();
		if (prefix != null) {
			prefix = StringUtils.toFirstUpperCase(prefix);
		} else {
			prefix = JModel.BASEPREFIX;
		}

		if (propertyStripPrefix) {
			String newLocalName = stripPropertyPrefixes(localName);

			if (!newLocalName.equals(localName)) {
				Property p1 = ontProperty.getOntModel().getOntProperty(nsUri +  newLocalName);
				Property p2 = ontProperty.getOntModel().getOntProperty(
						nsUri + StringUtils.toFirstLowerCase(newLocalName));
				if (p1 == null && p2 == null) {
					localName = newLocalName;
				} else {
					log.warn(LogUtils.toLogName(ontProperty) + ": Can not strip property prefix "
							+ "as another property of such name exists");
				}
			}
		}

		String name = propertyNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		return StringUtils.toFirstLowerCase(name);
	}

	public static String getValidJavaName(String aName) {
		return JavaUtils.toValidJavaName(aName);
	}

	public static String stripPropertyPrefixes(String string) {
		for (String prefix : propertyIgnoredPrefixes)
			string = string.replace(prefix, JModel.BASEPREFIX);
		return string;
	}

	@SuppressWarnings("unchecked")
	public static String createUnionClassName(UnionClass cls) {
		String name = unionClassPrefix;
		Iterator operandIt = sortOperandClasses(cls.listOperands()).iterator();
		while (operandIt.hasNext()) {
			OntClass c = (OntClass) operandIt.next();
			name += StringUtils.toFirstUpperCase(c.getLocalName());
			if (operandIt.hasNext()) {
				name += unionClassGlue;
			}
		}
		name = NamingUtils.getValidJavaName(name);
		return name;
	}

	@SuppressWarnings("unchecked")
	public static String createIntersectionClassName(IntersectionClass cls) {
		String name = intersectionClassPrefix;
		Iterator operandIt = sortOperandClasses(cls.listOperands()).iterator();
		while (operandIt.hasNext()) {
			OntClass c = (OntClass) operandIt.next();
			if (c.isAnon()) {
				name += anonPrefix + anonCounter;
				anonCounter++;
			} else {
				name += StringUtils.toFirstUpperCase(c.getLocalName());
			}
			if (operandIt.hasNext()) {
				name += intersectionClassGlue;
			}
		}
		return name;
	}

	private static List<? extends OntClass> sortOperandClasses(ExtendedIterator<? extends OntClass> operands) {
		List<? extends OntClass> operandList = operands.toList();

		Collections.sort(operandList, new Comparator<OntClass>() {
			@Override
			public int compare(OntClass a, OntClass b) {
				int result = a.getLocalName().compareToIgnoreCase(b.getLocalName());

				if (result == 0) {
					result = a.getURI().compareToIgnoreCase(b.getLocalName());
				}

				return result;
			}
		});
		return operandList;
	}
}
