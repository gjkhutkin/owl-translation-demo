package demo.owl2java.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;

import demo.owl2java.formatter.CodeFormattingWriter;
import demo.owl2java.model.jmodel.JClass;
import demo.owl2java.model.jmodel.JModel;
import demo.owl2java.model.jmodel.JPackage;
import demo.owl2java.model.jmodel.utils.NamingUtils;
import demo.owl2java.model.xsd.XsdMapTestData;
import demo.owl2java.utils.JavaUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;


public class JavaWriter {
	private static final String TEMPLATE_ROOT = JavaWriter.class.getResource("templates") + "/";

	private static final String TEMPLATE_CLASS = "class.vm";
	private static final String TEMPLATE_FACTORY = "factory.vm";
	private static final String TEMPLATE_INTERFACE = "interface.vm";
	private static final String TEMPLATE_TEST = "test.vm";
	private static final String TEMPLATE_VOCABULARY = "vocabulary.vm";

	static Log log = LogFactory.getLog(JavaWriter.class);

	private String baseDir;
	private String basePackage;
	private Properties codeFormatterOptions;
	private boolean enableCodeFormatting;
	private String factoryName;
	private boolean generateTestClass;
	private JModel jmodel;
	private String testClassName;
	private String toolsPackage;
	private VelocityEngine vEngine;
	private String vocabularyName;

	public void generate(JModel model, String baseDir, String basePackage) {
		this.baseDir = baseDir;
		jmodel = model;
		this.basePackage = basePackage;

		log.info("");
		log.info("Writing JModel to java");

		createPackageDirectories();
		initVelocityEngine();
		createInterfaces();
		createClasses();
		createVocabulary();
		createFactory();

		if (generateTestClass) {
			createTestClass();
		}

	}

	public void setCodeFormatterOptions(Properties codeFormatterOptions) {
		this.codeFormatterOptions = codeFormatterOptions;
	}

	public void setCreateTestClass(boolean createTestClass) {
		generateTestClass = createTestClass;
	}

	public void setEnableCodeFormatting(boolean enableCodeFormatting) {
		this.enableCodeFormatting = enableCodeFormatting;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}

	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}

	public void setVocabularyName(String vocabularyName) {
		this.vocabularyName = vocabularyName;
	}

	private void createClasses() {
		log.info("Creating java classes");
		Iterator<JClass> clsIt = jmodel.listJClasses().iterator();
		while (clsIt.hasNext()) {
			JClass cls = clsIt.next();
			String outDir = JavaUtils.toDirectoryFromPackage(cls.getJavaPackageName(), baseDir);
			String outName = cls.getJavaClassName();
			String outPath = outDir + "/" + outName + ".java";
			log.info("Creating class " + outName);
			log.debug("Creating class as " + outPath);

			Template template;
			try {
				template = vEngine.getTemplate(TEMPLATE_CLASS);
			} catch (ResourceNotFoundException e) {
				throw new RuntimeException();
			} catch (ParseErrorException e) {
				throw new RuntimeException();
			} catch (Exception e) {
				throw new RuntimeException();
			}

			VelocityContext vContext = getBaseVelocityContext();
			vContext.put("cls", cls);

			try {
				Writer writer = getCodeWriter(outPath);
				template.merge(vContext, writer);
				writer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void createFactory() {
		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String outDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		String outName = factoryName;
		String outPath = outDir + "/" + outName + ".java";
		log.debug("Creating factory " + outPath);

		Template template;
		try {
			template = vEngine.getTemplate(TEMPLATE_FACTORY);
		} catch (ResourceNotFoundException e) {
			throw new RuntimeException();
		} catch (ParseErrorException e) {
			throw new RuntimeException();
		} catch (Exception e) {
			throw new RuntimeException();
		}

		try {
			Writer writer = getCodeWriter(outPath);
			template.merge(getBaseVelocityContext(), writer);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createInterfaces() {
		log.info("Creating java interfaces");

		Iterator<JClass> clsIt = jmodel.listJClasses().iterator();
		while (clsIt.hasNext()) {
			JClass cls = clsIt.next();
			String outDir = JavaUtils.toDirectoryFromPackage(cls.getJavaPackageName(), baseDir);
			String outName = cls.getJavaInterfaceName();
			String outPath = outDir + "/" + outName + ".java";
			log.info("Creating interface " + outName);
			log.debug("Creating interface as " + outPath);

			Template template;
			try {
				template = vEngine.getTemplate(TEMPLATE_INTERFACE);
			} catch (ResourceNotFoundException e) {
				throw new RuntimeException();
			} catch (ParseErrorException e) {
				throw new RuntimeException();
			} catch (Exception e) {
				throw new RuntimeException();
			}

			VelocityContext vContext = getBaseVelocityContext();
			vContext.put("cls", cls);

			try {
				Writer writer = getCodeWriter(outPath);
				template.merge(vContext, writer);
				writer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void createPackageDirectories() {
		boolean success = true;

		log.info("Creating directory structure");
		log.debug("Creating base directory " + baseDir);
		success &= new File(baseDir).mkdirs();

		Iterator<JPackage> pkgIt = jmodel.listPackages().iterator();
		while (pkgIt.hasNext()) {
			JPackage pkg = pkgIt.next();
			if (pkg.listJClasses().size() > 0) {
				String pkgName = pkg.getPackageName();
				String pkgDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
				log.debug("Creating directory for package " + pkgName);
				success &= new File(pkgDir).mkdirs();
			}
		}

		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String pkgDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		log.debug("Creating tools directory for package " + pkgName);
		success &= new File(pkgDir).mkdirs();
	}

	private void createTestClass() {
		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String outDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		String outName = testClassName;
		String outPath = outDir + "/" + outName + ".java";
		log.debug("Creating test cases " + outPath);

		Template template;
		try {
			template = vEngine.getTemplate(TEMPLATE_TEST);
		} catch (ResourceNotFoundException e) {
			throw new RuntimeException();
		} catch (ParseErrorException e) {
			throw new RuntimeException();
		} catch (Exception e) {
			throw new RuntimeException();
		}

		try {
			Writer writer = getCodeWriter(outPath);
			template.merge(getBaseVelocityContext(), writer);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createVocabulary() {
		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String outDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		String outName = vocabularyName;
		String outPath = outDir + "/" + outName + ".java";
		log.debug("Creating vocabulary " + outPath);

		Template template;
		try {
			template = vEngine.getTemplate(TEMPLATE_VOCABULARY);
		} catch (ResourceNotFoundException e) {
			throw new RuntimeException();
		} catch (ParseErrorException e) {
			throw new RuntimeException();
		} catch (Exception e) {
			throw new RuntimeException();
		}

		try {
			Writer writer = getCodeWriter(outPath);
			template.merge(getBaseVelocityContext(), writer);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private VelocityContext getBaseVelocityContext() {
		VelocityContext vContext = new VelocityContext();
		Calendar c = Calendar.getInstance();
		vContext.put("now", DateFormat.getInstance().format(c.getTime()));
		vContext.put("pkgBase", basePackage);
		vContext.put("pkgTools", toolsPackage);
		vContext.put("jmodel", jmodel);
		vContext.put("factoryName", factoryName);
		vContext.put("factoryPkg", NamingUtils.getJavaPackageName(basePackage, toolsPackage));
		vContext.put("vocabName", vocabularyName);
		vContext.put("vocabPkg", NamingUtils.getJavaPackageName(basePackage, toolsPackage));
		vContext.put("testcaseName", testClassName);
		vContext.put("testcasePkg", NamingUtils.getJavaPackageName(basePackage, toolsPackage));
		XsdMapTestData xsdMap = new XsdMapTestData();
		vContext.put("xsdMap", xsdMap);
		return vContext;
	}

	private Writer getCodeWriter(String fileName) throws IOException {
		Writer writer = new FileWriter(fileName);
		if (enableCodeFormatting) {
			writer = new CodeFormattingWriter(writer, codeFormatterOptions);
		}
		return writer;
	}

	private void initVelocityEngine() {
		log.info("Init velocity engine");

		vEngine = new VelocityEngine();

		vEngine.setProperty("resource.loader", "url");
		vEngine.setProperty("url.resource.loader.description", "Velocity URL Resource Loader");
		vEngine.setProperty("url.resource.loader.class", "org.apache.velocity.runtime.resource.loader.URLResourceLoader");
		vEngine.setProperty("url.resource.loader.root", TEMPLATE_ROOT);

		vEngine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogSystem");
		vEngine.setProperty("velocimacro.library", "macros.vm");
		try {
			vEngine.init();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
}
