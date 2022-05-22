package demo.owl2java.formatter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

public class CodeFormattingWriter extends StringWriter {
	CodeFormatter formatter;
	Writer destination;

	public CodeFormattingWriter(Writer destination) {
		this(destination, new CodeFormatter());
	}

	public CodeFormattingWriter(Writer destination, Properties formatterSettings) {
		this(destination, new CodeFormatter(formatterSettings));
	}

	public CodeFormattingWriter(Writer destination, CodeFormatter formatter) {
		assert destination != null : "destination argument is null";
		assert formatter != null : "formatter argument is null";

		this.destination = destination;
		this.formatter = formatter;
	}

	@Override
	public void close() throws IOException {
		super.close();
		String formattedCode = formatter.format(getBuffer().toString());
		destination.write(formattedCode);
		destination.close();
		this.destination = null;
	}

	@Override
	public void flush() {}
}