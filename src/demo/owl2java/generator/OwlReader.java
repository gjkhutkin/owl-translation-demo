package demo.owl2java.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import demo.owl2java.model.jenautils.ResourceError;
import demo.owl2java.model.jmodel.JClass;
import demo.owl2java.model.jmodel.JModel;
import demo.owl2java.model.jmodel.JPackage;
import demo.owl2java.model.jmodel.JProperty;
import demo.owl2java.model.jmodel.JRestrictionsContainer;
import demo.owl2java.model.jmodel.utils.DebugUtils;
import demo.owl2java.model.jmodel.utils.LogUtils;
import demo.owl2java.model.jmodel.utils.NamingUtils;
import demo.owl2java.model.ns.NamespaceUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


public class OwlReader {

	private static Log log = LogFactory.getLog(OwlReader.class);

	private OntModel ontModel;
	public JModel jmodel;

	private String basePackage;
	private List<String> forbiddenPrefixes = new ArrayList<String>();
	private List<JClass> deferredIntersectionClasses = new ArrayList<JClass>();

	public void addForbiddenPrefix(String prefix) {
		forbiddenPrefixes.add(prefix);
	}

	protected void createJPackages() {
		JPackage defaultPkg = new JPackage(jmodel, basePackage);
		this.jmodel.addPackage(basePackage, defaultPkg);

		handleNamespaces();

		List<String> ns = jmodel.listNamespaces();
		for (String uri : ns) {
			String prefix = jmodel.getPrefix(uri);

			if (NamespaceUtils.defaultNs2UriMapping.containsKey(uri))
				continue;

			if (forbiddenPrefixes.contains(prefix)) {
				log
						.error("Prefix " + prefix
								+ " is identical with a system internal prefix (toolspackage?). Aborting!");
				System.exit(1);
			}

			if (prefix == JModel.BASEPREFIX) {
				log.info("Assigning namespace " + uri + " to base package " + basePackage);
				jmodel.addPackage(uri, basePackage);
				continue;
			}

			String pkgName = NamingUtils.getJavaPackageName(basePackage, prefix);
			log.info("Generating package " + pkgName);
			JPackage p = new JPackage(jmodel, pkgName);
			this.jmodel.addPackage(pkgName, p);
			jmodel.addPackage(uri, pkgName);
		}

	}

	protected void createJRestriction(Restriction res, OntClass cls, OntProperty prop) {
		JClass jClass = jmodel.getJClass(cls.getURI());
		JProperty jProp = jmodel.getJProperty(prop.getURI());

		JRestrictionsContainer jRestriction;
		if (jClass.hasDomainRestrictionsContainer(jProp)) {
			log.debug(LogUtils.toLogName(jClass, jProp) + ": Reusing existing restriction");
			jRestriction = jClass.getDomainRestrictionsContainer(jProp);
		} else {
			jRestriction = new JRestrictionsContainer(jClass, jProp);
			log.debug(LogUtils.toLogName(jClass, jProp) + ": Creating new restriction");
		}

		if (res.isMaxCardinalityRestriction()) {
			MaxCardinalityRestriction maxCardinalityRestriction = res.asMaxCardinalityRestriction();
			int maxCardinality = maxCardinalityRestriction.getMaxCardinality();
			jRestriction.getCardinalityRestriction().setMaxCardinality(maxCardinality);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Max cardinality set to " + maxCardinality);
		}

		if (res.isMinCardinalityRestriction()) {
			MinCardinalityRestriction minCardinalityRestriction = res.asMinCardinalityRestriction();
			int minCardinality = minCardinalityRestriction.getMinCardinality();
			jRestriction.getCardinalityRestriction().setMinCardinality(minCardinality);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Min cardinality set to " + minCardinality);
		}

		if (res.isCardinalityRestriction()) {
			CardinalityRestriction cardinalityRestriction = res.asCardinalityRestriction();
			int cardinality = cardinalityRestriction.getCardinality();
			jRestriction.getCardinalityRestriction().setCardinality(cardinality);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Cardinality (min=max) set to " + cardinality);
		}

		if (res.isAllValuesFromRestriction()) {
			if (prop.isDatatypeProperty()) {
				log.warn(LogUtils.toLogName(prop) + ": Not creating allValues restriction on datatype property");
				return;
			}
			AllValuesFromRestriction allValuesRestriction = res.asAllValuesFromRestriction();
			Resource allValuesResource = allValuesRestriction.getAllValuesFrom();
			JClass allValuesJClass = jmodel.getJClass(allValuesResource.getURI());
			jRestriction.addAllValuesRestriction(allValuesJClass);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Added allValues restriction:"
					+ LogUtils.toLogName(allValuesJClass));
		}

		if (res.isHasValueRestriction()) {
			if (prop.isDatatypeProperty()) {
				log.warn(LogUtils.toLogName(prop) + ": Not creating hasValue restriction on datatype property");
				return;
			}
			HasValueRestriction hasValueRestriction = res.asHasValueRestriction();
			RDFNode hasValueResource = hasValueRestriction.getHasValue();
			jRestriction.getOtherRestriction().addHasValue(hasValueResource.toString());
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Added hasValue restriction:"
					+ hasValueResource.toString());
			log.warn(DebugUtils.logPropertyOnClass(cls, prop) + ": HasValueRestriction currently ignored");
		}

		if (res.isSomeValuesFromRestriction()) {
			if (prop.isDatatypeProperty()) {
				log.warn(LogUtils.toLogName(prop) + ": Not creating someValues restriction on datatype property");
				return;
			}
			SomeValuesFromRestriction someValuesRestriction = res.asSomeValuesFromRestriction();
			Resource someValuesResource = someValuesRestriction.getSomeValuesFrom();
			JClass someValuesJClass = jmodel.getJClass(someValuesResource.getURI());
			jRestriction.getOtherRestriction().addSomeValues(someValuesJClass);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Added someValues restriction:"
					+ LogUtils.toLogName(someValuesJClass));
			log.warn(DebugUtils.logPropertyOnClass(cls, prop) + ": SomeValuesRestriction currently ignored");
		}
	}

	protected void createProperty(OntProperty ontProperty) {
		log.info(LogUtils.toLogName(ontProperty) + ": Found property");
		log.debug(DebugUtils.logProperty(ontProperty));

		if (!jmodel.hasJProperty(ontProperty.getURI()))
			jmodel.createJProperty(ontProperty);
		JProperty jProperty = jmodel.getJProperty(ontProperty.getURI());
		jProperty.setOntProperty(ontProperty);

		if (ontProperty.isDatatypeProperty())
			jProperty.setPropertyType(JProperty.DataTypeProperty);
		else
			jProperty.setPropertyType(JProperty.ObjectProperty);

		Iterator<? extends OntResource> rIt = ontProperty.listRange();
		while (rIt.hasNext()) {
			OntResource range = rIt.next();
			log.debug(LogUtils.toLogName(ontProperty) + ": Found range " + LogUtils.toLogName(range));

			if (jmodel.hasJClass(range.getURI())) {
				jProperty.setPropertyType(JProperty.ObjectProperty);
				jProperty.addRange(jmodel.getJClass(range.getURI()));

				log.debug(LogUtils.toLogName(ontProperty) + ": Registering class " + LogUtils.toLogName(range)
						+ " as range");

			} else if (ontProperty.isDatatypeProperty()) {
				jProperty.setPropertyType(JProperty.DataTypeProperty);
				jProperty.addRange(range.getURI());
				log.debug(LogUtils.toLogName(ontProperty) + ": Registering " + LogUtils.toLogName(range) + " as range");
			}
		}

		if (ontProperty.isFunctionalProperty()) {
			log.debug(LogUtils.toLogName(ontProperty) + ": Is a functional property. Marking it. ");
			jProperty.setFunctional(true);
		}

		if (ontProperty.isInverseFunctionalProperty()) {
			log.warn(LogUtils.toLogName(ontProperty) + ": Is a inverse functional property. Ignored");
			jProperty.setInverseFunctional(true);
		}

		if (ontProperty.isSymmetricProperty()) {
			log.debug(LogUtils.toLogName(ontProperty) + ": Is a symmetric property: handled elsewhere");
			jProperty.setSymetric(true);
		}

		if (ontProperty.isTransitiveProperty()) {
			log.warn(LogUtils.toLogName(ontProperty) + ": Is a transitive property: IGNORED");
			jProperty.setTransitive(true);
		}

		if (ontProperty.hasInverse()) {
			Iterator<? extends OntProperty> inverseIt = ontProperty.listInverse();
			while (inverseIt.hasNext()) {
				OntProperty inverseProp = inverseIt.next();

				if (!jmodel.hasJProperty(inverseProp.getURI()))
					jmodel.createJProperty(inverseProp);
				JProperty iProp = jmodel.getJProperty(inverseProp.getURI());
				iProp.setOntProperty(inverseProp);
				iProp.setPropertyType(JProperty.ObjectProperty);

				if (!iProp.hasInverseProperty(jProperty)) {
					iProp.addInverseProperty(jProperty);
					log.info(LogUtils.toLogName(ontProperty) + ": Marked as inverse of " + LogUtils.toLogName(iProp));
				} else {
					log.debug(LogUtils.toLogName(ontProperty) + ": Already defined as inverse of "
							+ LogUtils.toLogName(iProp));
				}
			}
		}

		Iterator<? extends OntProperty> superIt = ontProperty.listSuperProperties(true);
		while (superIt.hasNext()) {
			OntProperty superProp = superIt.next();
			log
					.debug(LogUtils.toLogName(ontProperty) + ": Registering super property "
							+ LogUtils.toLogName(superProp));

			if (!jmodel.hasJProperty(superProp.getURI()))
				jmodel.createJProperty(superProp);
			JProperty sProp = jmodel.getJProperty(superProp.getURI());
			if (superProp.isDatatypeProperty())
				sProp.setPropertyType(JProperty.DataTypeProperty);
			else
				sProp.setPropertyType(JProperty.ObjectProperty);
			sProp.setOntProperty(superProp);
			sProp.addSubProperty(jProperty);
		}

		Iterator<? extends OntProperty> equivalentIt = ontProperty.listEquivalentProperties();
		while (equivalentIt.hasNext()) {
			OntProperty equProp = equivalentIt.next();

			if (!jmodel.hasJProperty(equProp.getURI()))
				jmodel.createJProperty(equProp);
			JProperty eProp = jmodel.getJProperty(equProp.getURI());
			if (equProp.isDatatypeProperty())
				eProp.setPropertyType(JProperty.DataTypeProperty);
			else
				eProp.setPropertyType(JProperty.ObjectProperty);

			eProp.setOntProperty(equProp);

			if (eProp.hasEquivalentProperty(jProperty)) {
				log.debug(LogUtils.toLogName(ontProperty) + ": Registering inverse property "
						+ LogUtils.toLogName(equProp));
				eProp.addEquivalentProperty(jProperty);
			} else {
				log.debug(LogUtils.toLogName(ontProperty) + ": Alredy defined as inverse property of "
						+ LogUtils.toLogName(equProp));
			}
		}

		boolean domainProp = false;
		ExtendedIterator<? extends OntResource> dIt = ontProperty.listDomain();
		while (dIt.hasNext()) {
			OntResource domain = dIt.next();
			log.debug(LogUtils.toLogName(ontProperty) + ": Found domain " + LogUtils.toLogName(domain));

			if (domain.isAnon()) {
				log.debug(LogUtils.toLogName(ontProperty) + ": Domain is Anonymous class. Ignored.");
				continue;
			}

			JClass domainCls = jmodel.getJClass(domain.getURI());
			domainCls.addDomainProperty(jProperty);
			domainProp = true;
			log.debug(LogUtils.toLogName(ontProperty) + ": Registering as domain property in class "
					+ LogUtils.toLogName(domainCls));
		}

		if (!domainProp) {
			JClass domainCls = jmodel.getJClass(jmodel.getBaseThingUri());
			domainCls.addDomainProperty(jProperty);
			log.debug(LogUtils.toLogName(ontProperty) + ": Registering property without domain in "
					+ LogUtils.toLogName(domainCls));
		}

	}

	protected boolean hasBaseThingURI(OntClass ontClass) {
		String ontClassUri = ontClass.getURI();
		String baseThingUri = jmodel.getBaseNamespace() + JModel.getBaseThingName();

		if (ontClassUri == null)
			return false;

		if (ontClassUri.equals(baseThingUri)) {
			return true;
		}
		return false;
	}

	protected void createJClassish(OntClass ontClass) {
		log.info(LogUtils.toLogName(ontClass) + ": Found owl/rdf class");
		log.debug(DebugUtils.logClass(ontClass));

		if (NamespaceUtils.defaultNs2UriMapping.containsKey(ontClass.getNameSpace())) {
			log.debug(LogUtils.toLogName(ontClass) + ": Is a base owl/rdfs class. Ignored");
			return;
		}

		if (hasBaseThingURI(ontClass)) {
			log.error(LogUtils.toLogName(ontClass) + ": An unprefixed class named 'Thing' in "
					+ "the BaseURI namespace is not allowed");
			System.exit(1);
			log.error("Aborting");
		}

		if (!jmodel.hasJClass(ontClass.getURI()))
			jmodel.createJClass(ontClass, basePackage);
		JClass cls = jmodel.getJClass(ontClass.getURI());
		cls.setOntClass(ontClass);

		ExtendedIterator<OntClass> superIt = ontClass.listSuperClasses(true);
		while (superIt.hasNext()) {
			OntClass superCls = superIt.next();
			hasBaseThingURI(superCls);

			if (superCls.isAnon()) {
				continue;
			}

			JClass superClass;
			if (!jmodel.hasJClass(superCls.getURI()))
				jmodel.createJClass(superCls, basePackage);
			superClass = jmodel.getJClass(superCls.getURI());
			superClass.setOntClass(superCls);

			superClass.addSubClass(cls);
			log.debug(LogUtils.toLogName(ontClass) + ": Registering super class "
					+ NamingUtils.getJavaFullName(superClass.getPackage(), superClass.getName()));
		}
		if (!cls.hasSuperClasses()) {
			log.debug(LogUtils.toLogName(ontClass) + ": No parent class given.");
			JClass superClass = jmodel.getJClass(jmodel.getBaseThingUri());
			superClass.addSubClass(cls);
			log.debug(LogUtils.toLogName(ontClass) + ": Registering super class "
					+ NamingUtils.getJavaFullName(superClass.getPackage(), superClass.getName()));
		}

		ExtendedIterator<OntClass> equIt = ontClass.listEquivalentClasses();
		while (equIt.hasNext()) {
			OntClass equCls = equIt.next();

			if (equCls.isAnon()) {
				log.warn("Currently, only primitive equivalent class definitions are used (OWL Lite)");
				continue;
			}

			JClass equClass;
			if (!jmodel.hasJClass(equCls.getURI()))
				jmodel.createJClass(equCls, basePackage);
			equClass = jmodel.getJClass(equCls.getURI());
			equClass.setOntClass(equCls);

			equClass.addEquivalentClass(cls);
			log.debug(LogUtils.toLogName(ontClass) + ": Registering equivalent class "
					+ NamingUtils.getJavaFullName(equClass.getPackage(), equClass.getName()));
		}

	}

	public JModel generateJModel(OntModel model) {
		this.jmodel = new JModel();
		this.jmodel.setOntModel(model);
		this.ontModel = model;

		createJPackages();
		handleClassishObjects();
		handleAnonymousClasses();
		handleProperties();
		handlePropertyRanges();
		handleDeferredIntersectionClasses();
		handleRestrictions();

		return this.jmodel;
	}

	protected void handleDeferredIntersectionClasses() {
		for (JClass cls : deferredIntersectionClasses) {
			handleIntersectionClassProperties(cls);
		}
	}

	protected void handlePropertyRanges() {
		log.info("");
		log.info("Checking for multiple ranges of object properties");
		Iterator<String> propertyIt = jmodel.getUri2property().keySet().iterator();
		while (propertyIt.hasNext()) {
			String propUri = propertyIt.next();
			JProperty prop = jmodel.getUri2property().get(propUri);

			if (prop.getPropertyType() == JProperty.DataTypeProperty)
				continue;

			if (prop.listObjectPropertyRange().size() < 2)
				continue;

			log.info(LogUtils.toLogName(prop) + ": Found multiple range. Replacing with IntersectionClass");

			ArrayList<OntClass> operandOntClasses = new ArrayList<OntClass>();
			for (JClass operandClass: prop.listObjectPropertyRange()) {
				operandOntClasses.add(operandClass.getOntClass());
			}

			IntersectionClass intersectionClass = ontModel.createIntersectionClass(null, this.ontModel.createList(operandOntClasses.iterator()));

			JClass cls = createIntersectionClass(intersectionClass);

			prop.listObjectPropertyRange().clear();
			prop.addRange(cls);
			log.debug(LogUtils.toLogName(prop) + ": Setting range to " + cls.getName());
		}

	}

	public JModel getJModel() {
		return jmodel;
	}

	protected void handleAnonymousClasses() {
		log.info("");
		log.info("Analyzing anonymous classes");

		List<OntClass> list = ontModel.listClasses().toList();
		for (int i = 0; i < list.size(); i++) {
			OntClass ontClass = list.get(i);

			if (!ontClass.isAnon())
				continue;

			if (ontClass.isRestriction())
				continue;

			if (ontClass.isUnionClass())
				createUnionClass(ontClass.asUnionClass());

			if (ontClass.isComplementClass()) {
				jmodel.addOntResourceError(new ResourceError(ontClass, "ComplementClass ignored"));
				log.warn("Found non restriction anonymous class: " + "ComplementClass ignored");
			}

			if (ontClass.isIntersectionClass()) {
				log.info(LogUtils.toLogName(ontClass)
						+ ": Is intersection class. Handled as multiple subClassOf definitions.");
				log.info(LogUtils.toLogName(ontClass) + ": ---> This should be done by a reasoner!");
			}

			if (ontClass.isEnumeratedClass()) {
				jmodel.addOntResourceError(new ResourceError(ontClass, "Enumerated class handled as simple class"));
				log.warn("Found non restriction anonymous class: " + "EnumeratedClass handled " + "as simple class");
			}

		}

	}

	protected JClass createIntersectionClass(IntersectionClass intersectionClass) {
		JClass cls = jmodel.getAnonymousJClass(JClass.AnonymousClassType.INTERSECTION, intersectionClass.listOperands().toList());
		if (cls != null)
			log.info("Reusing existing anonymous intersection class " + LogUtils.toLogName(cls));
		else {
			String anonClassName = NamingUtils.createIntersectionClassName(intersectionClass);
			String namespace = getAnonymousNamespace(intersectionClass.listOperands());;
			String anonClassUri = namespace + anonClassName;

			log.info("Renaming anonymous intersection class to: " + anonClassUri);
			ResourceUtils.renameResource(intersectionClass, anonClassUri);
			intersectionClass = ontModel.getOntClass(anonClassUri).asIntersectionClass();

			if (!jmodel.hasJClass(intersectionClass.getURI()))
				jmodel.createJClass(intersectionClass, basePackage);
			cls = jmodel.getJClass(intersectionClass.getURI());
			cls.setAnonymous(JClass.AnonymousClassType.INTERSECTION, intersectionClass.listOperands().toList());
			cls.setOntClass(intersectionClass);
		}

		Iterator<OntClass> subIt = intersectionClass.listSubClasses();
		while (subIt.hasNext()) {
			OntClass ontCls = subIt.next();
			String ontUri = ontCls.getURI();
			JClass subCls = jmodel.getJClass(ontUri);
			subCls.addSuperClass(cls);
			log.debug(LogUtils.toLogName(intersectionClass) + ": Registering sub class "
					+ NamingUtils.getJavaFullName(subCls.getPackage(), subCls.getName()));
		}

		Iterator<? extends OntClass> operandsIt = intersectionClass.listOperands();
		while (operandsIt.hasNext()) {
			OntClass ontCls = operandsIt.next();

			if (ontCls.isAnon())
				continue;

			String ontUri = ontCls.getURI();
			JClass operandCls = jmodel.getJClass(ontUri);

			operandCls.addSuperClass(cls);
			log.debug(LogUtils.toLogName(intersectionClass) + ": Registering sub class "
					+ LogUtils.toLogName(operandCls));
		}

		List<JClass> reassignedSuperClasses = new ArrayList<JClass>();
		operandsIt = intersectionClass.listOperands();
		while (operandsIt.hasNext()) {
			OntClass operandCls = operandsIt.next();
			Iterator<OntClass> superIt = operandCls.listSuperClasses();
			while (superIt.hasNext()) {
				OntClass ontCls = superIt.next();
				String ontUri = ontCls.getURI();
				JClass superCls = jmodel.getJClass(ontUri);

				log.debug(LogUtils.toLogName(intersectionClass) + ": Registering super class "
						+ LogUtils.toLogName(superCls));
				superCls.addSubClass(cls);
				reassignedSuperClasses.add(superCls);
			}
		}

		operandsIt = intersectionClass.listOperands();
		while (operandsIt.hasNext()) {
			OntClass ontCls = operandsIt.next();
			if (ontCls.isAnon())
				continue;
			String ontUri = ontCls.getURI();
			JClass operandCls = jmodel.getJClass(ontUri);

			for (JClass oldSuperClass : reassignedSuperClasses) {
				if (operandCls.hasSuperClass(oldSuperClass, false)) {
					log.debug(LogUtils.toLogName(operandCls) + ": Removing old super class relation "
							+ LogUtils.toLogName(oldSuperClass));
					operandCls.removeSuperClassRelation(oldSuperClass);
				}
			}
		}

		deferredIntersectionClasses.add(cls);
		return cls;
	}

	protected void handleIntersectionClassProperties(JClass cls) {
		IntersectionClass intersectionClass = cls.getOntClass().asIntersectionClass();
		Iterator<? extends OntClass> operandsIt;

		List<JProperty> properties = new ArrayList<JProperty>();
		List<JProperty> propertiesToRemove = new ArrayList<JProperty>();
		operandsIt = intersectionClass.listOperands();
		while (operandsIt.hasNext()) {
			OntClass ontCls = operandsIt.next();
			if (ontCls.isAnon())
				continue;
			String ontUri = ontCls.getURI();
			JClass operandCls = jmodel.getJClass(ontUri);
			for (JProperty p : operandCls.listDomainProperties())
				properties.add(p);
		}

		for (JProperty p : properties) {
			operandsIt = intersectionClass.listOperands();
			while (operandsIt.hasNext()) {
				OntClass ontCls = operandsIt.next();
				String ontUri = ontCls.getURI();
				JClass operandCls = jmodel.getJClass(ontUri);

				if (!operandCls.hasDomainProperty(p))
					propertiesToRemove.add(p);
			}
		}
		for (JProperty p : propertiesToRemove)
			properties.remove(p);

		for (JProperty p : properties) {
			cls.addDomainProperty(p);
			log.debug(LogUtils.toLogName(p) + ": Reassigning to intersection class " + LogUtils.toLogName(cls));
		}
	}

	protected JClass createUnionClass(UnionClass unionClass) {
		JClass cls = jmodel.getAnonymousJClass(JClass.AnonymousClassType.UNION, unionClass.listOperands().toList());
		if (cls != null) {
			log.info("Reusing existing anonymous union class " + LogUtils.toLogName(cls));
		} else {
			String anonClassName = NamingUtils.createUnionClassName(unionClass);
			String namespace = getAnonymousNamespace(unionClass.listOperands());
			String anonClassUri = namespace + anonClassName;

			log.info("Renaming anonymous union class to :" + anonClassUri);
			ResourceUtils.renameResource(unionClass, anonClassUri);
			unionClass = ontModel.getOntClass(anonClassUri).asUnionClass();

			if (!jmodel.hasJClass(unionClass.getURI()))
				jmodel.createJClass(unionClass, basePackage);
			cls = jmodel.getJClass(unionClass.getURI());
			cls.setAnonymous(JClass.AnonymousClassType.UNION, unionClass.listOperands().toList());
			cls.setOntClass(unionClass);
		}

		Iterator<OntClass> subIt = unionClass.listSubClasses();
		while (subIt.hasNext()) {
			OntClass ontCls = subIt.next();
			String ontUri = ontCls.getURI();
			JClass subCls = jmodel.getJClass(ontUri);
			subCls.removeSuperClassRelation(jmodel.getBaseThing());
			subCls.addSuperClass(cls);
			log.debug(LogUtils.toLogName(unionClass) + ": Registering sub class "
					+ NamingUtils.getJavaFullName(subCls.getPackage(), subCls.getName()));
		}

		Iterator<? extends OntClass> operandIt = unionClass.listOperands();
		while (operandIt.hasNext()) {
			OntClass superClass = operandIt.next();

			JClass superCls;
			if (!jmodel.hasJClass(superClass.getURI()))
				jmodel.createJClass(superClass, basePackage);
			superCls = jmodel.getJClass(superClass.getURI());
			superCls.setOntClass(superClass);
			superCls.addSubClass(cls);
			log.debug(LogUtils.toLogName(unionClass) + ": Registering super class "
					+ NamingUtils.getJavaFullName(superCls.getPackage(), superCls.getName()));
		}
		return cls;
	}

	public String getAnonymousNamespace(ExtendedIterator<? extends OntClass> operandClasses) {
		if (!operandClasses.hasNext()) {
			return jmodel.getBaseNamespace();
		}
		String namespace = operandClasses.next().getNameSpace();
		while (operandClasses.hasNext()) {
			if (!namespace.equals(operandClasses.next().getNameSpace())) {
				return jmodel.getBaseNamespace();
			}
		}
		return namespace;
	}

	protected void handleClassishObjects() {
		createBaseJClassish();

		log.info("");
		log.info("Found " + ontModel.listNamedClasses().toList().size() + " named classes");

		Iterator<OntClass> it = ontModel.listNamedClasses();
		while (it.hasNext()) {
			OntClass ontClass = it.next();
			createJClassish(ontClass);
		}
	}

	protected void createBaseJClassish() {
		String thingUri;
		String thingName = JModel.getBaseThingName();
		thingUri = ontModel.getNsPrefixURI("owl") + thingName;

		jmodel.setBaseThingUri(thingUri);
		jmodel.createJClass(thingName, thingUri, basePackage);
	}

	protected void handleNamespaces() {
		Iterator<String> it = this.ontModel.getNsPrefixMap().keySet().iterator();
		while (it.hasNext()) {
			String prefix = it.next();
			String uri = this.ontModel.getNsPrefixURI(prefix);

			jmodel.addNamespacePrefix(uri, prefix);
			log.info("Adding prefix " + prefix + " for namespace " + uri);
		}

		Iterator<String> importedUriIt = ontModel.listImportedOntologyURIs(true).iterator();
		while (importedUriIt.hasNext()) {
			String importedUri = importedUriIt.next() + "#";
			if (!jmodel.hasNamespace(importedUri)) {
				String ns = importedUri;
				log.info("Found namespace without prefix: " + ns);
				String prefix = jmodel.createNewPrefix();
				jmodel.addNamespacePrefix(ns, prefix);
				ontModel.setNsPrefix(prefix, ns);
				log.info("Adding auto-generated prefix " + prefix + " for namespace " + ns);
			}
		}
	}

	protected void handleProperties() {
		Iterator<? extends OntProperty> it;
		log.info("");

		log.info("Found " + ontModel.listObjectProperties().toList().size() + " object properties");
		it = ontModel.listObjectProperties();
		handleProperties(it);

		log.info("Found " + ontModel.listTransitiveProperties().toList().size() + " object properties");
		it = ontModel.listTransitiveProperties();
		handleProperties(it);


		log.info("Found " + ontModel.listFunctionalProperties().toList().size() + " functional properties");
		it = ontModel.listFunctionalProperties();
		handleProperties(it);

		log.info("Found " + ontModel.listInverseFunctionalProperties().toList().size()
				+ " inverse functional properties");
		it = ontModel.listInverseFunctionalProperties();
		handleProperties(it);

		log.info("Found " + ontModel.listSymmetricProperties().toList().size() + " symetrical properties");
		it = ontModel.listSymmetricProperties();
		handleProperties(it);

		log.info("Found " + ontModel.listDatatypeProperties().toList().size() + " datatype properties");
		it = ontModel.listDatatypeProperties();
		handleProperties(it);
	}

	protected void handleProperties(Iterator<? extends OntProperty> propertiesIt) {
		while (propertiesIt.hasNext()) {
			OntProperty ontProperty = propertiesIt.next();

			String uri = ontProperty.getURI();
			
			if (NamespaceUtils.isPrimitiveNamespace(uri))
				continue;
			createProperty(ontProperty);
		}
	}

	protected void handleRestrictions() {
		log.info("");
		log.info("Analyzing restriction classes");

		Iterator<OntClass> it = ontModel.listClasses();
		while (it.hasNext()) {
			OntClass cls = it.next();
			if (!cls.isAnon())
				continue;

			if (cls.isRestriction()) {
				Restriction ontRestriction = cls.asRestriction();
				OntProperty ontProperty = ontRestriction.getOnProperty();

				log.info("Found Restriction on Property " + LogUtils.toLogName(ontRestriction.getOnProperty()));
				log.debug(DebugUtils.logRestriction(ontRestriction));

				Iterator<OntClass> subClassIt = ontRestriction.listSubClasses();
				while (subClassIt.hasNext()) {
					OntClass ontClass = subClassIt.next();

					if (ontClass.getURI() == null) {
						log.info(LogUtils.toLogName(ontClass) + ": Anonymous restriction class. Ignored");
						continue;
					}

					if (NamespaceUtils.defaultNs2UriMapping.containsKey(ontClass.getNameSpace())) {
						log.debug(LogUtils.toLogName(ontClass) + ": Is a base class. Ignored");
						continue;
					}

					createJRestriction(ontRestriction, ontClass, ontProperty);
				}
			}
		}
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}
}
