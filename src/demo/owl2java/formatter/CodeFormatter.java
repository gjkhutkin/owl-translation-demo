package demo.owl2java.formatter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;


public class CodeFormatter {
	private final static String DEFAULT_SETTINGS_FILE = "formatter-defaults.properties";
	private static Log log = LogFactory.getLog(CodeFormattingWriter.class);
	private Properties formatterSettings;

	public CodeFormatter() {
		this(null);
	}
	public CodeFormatter(Properties formatterSettings) {
		setFormatterSettings(formatterSettings);
	}

	public String format(String code) {
		if (!formatterSettings.containsKey(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM)) {
			log.warn("Code formatter settings must define " + JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM);
			return code;
		}
		if (!formatterSettings.containsKey(JavaCore.COMPILER_COMPLIANCE)) {
			log.warn("Code formatter settings must define " + JavaCore.COMPILER_COMPLIANCE);
			return code;
		}
		if (!formatterSettings.containsKey(JavaCore.COMPILER_SOURCE)) {
			log.warn("Code formatter settings must define " + JavaCore.COMPILER_SOURCE);
			return code;
		}

		IDocument document = new Document();
		document.set(code);

		org.eclipse.jdt.core.formatter.CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(this.formatterSettings);
		TextEdit modifications = codeFormatter.format(org.eclipse.jdt.core.formatter.CodeFormatter.K_COMPILATION_UNIT,
				code, 0, code.length(), 0, null);
		if (modifications != null) {
			try {
				modifications.apply(document);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return document.get();
	}

	public Properties getFormatterSettings() {
		return formatterSettings;
	}

	public void setFormatterSettings(Properties formatterSettings) {
		if (formatterSettings != null) {
			this.formatterSettings = formatterSettings;
		} else {
			this.formatterSettings = getDefaultSettings();
		}
	}

	private static Properties getDefaultSettings() {
		try {
			InputStream inputStream = CodeFormattingWriter.class.getResourceAsStream(DEFAULT_SETTINGS_FILE);
			if (inputStream != null) {
				Properties options = new Properties();
				options.load(inputStream);
				return options;
			} else {
				throw new FileNotFoundException(DEFAULT_SETTINGS_FILE);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}