package demo.owl2java.model.xsd;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.datatypes.xsd.XSDDuration;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Map1;

public class XsdUtils {

	private static Log log = LogFactory.getLog(XsdUtils.class);

	private static final SimpleDateFormat XSD_date = new SimpleDateFormat("yyyy-MM-ddZ");

	private static final SimpleDateFormat XSD_dateTime = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private static final SimpleDateFormat XSD_time = new SimpleDateFormat("HH:mm:ss.SSSZ");

	private static final SimpleDateFormat XSD_gDay = new SimpleDateFormat("---ddZ");

	private static final SimpleDateFormat XSD_gMonth = new SimpleDateFormat("--MM--Z");

	private static final SimpleDateFormat XSD_gMonthDay = new SimpleDateFormat("--MM-ddZ");

	private static final SimpleDateFormat XSD_gYear = new SimpleDateFormat("yyyyZ");

	private static final SimpleDateFormat XSD_gYearMonth = new SimpleDateFormat("yyyy-MMZ");

	public static String CalendarToXSD(Calendar c, RDFDatatype dt) {
		String time = null;
		if (dt.equals(XSDDatatype.XSDdate)) {
			time = XSD_date.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDdateTime)) {
			time = XSD_dateTime.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgDay)) {
			time = XSD_gDay.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgMonth)) {
			time = XSD_gMonth.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgMonthDay)) {
			time = XSD_gMonthDay.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgYear)) {
			time = XSD_gYear.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgYearMonth)) {
			time = XSD_gYearMonth.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDtime)) {
			time = XSD_time.format(c.getTime());
		} else {
			return c.toString();
		}
		StringBuffer sb = new StringBuffer(time);
		sb.insert(sb.length() - 2, ':');
		return sb.toString();
	}

	public static String DateTimeToXSD(XSDDateTime datetime, RDFDatatype dt) {
		Calendar cal = datetime.asCalendar();
		return CalendarToXSD(cal, dt);
	}
	
	public static String DurationToXSD(XSDDuration duration, RDFDatatype dt) {
		return duration.toString();
	}

	public static Literal createTypedLiteral(OntModel ontModel, Object obj, String dataType) {
		RDFDatatype rdfdt = TypeMapper.getInstance().getTypeByName(dataType);
		if (obj instanceof Calendar) {
			String lex = CalendarToXSD((Calendar) obj, rdfdt);
			if (rdfdt.isValidValue(lex)) {
				return ontModel.createTypedLiteral(lex, dataType);
			} else {
				throw new DatatypeFormatException(lex, rdfdt, "Value does not match datatype.");
			}
		} else if (obj instanceof XSDDateTime) {
			String lex = DateTimeToXSD((XSDDateTime) obj, rdfdt);
			if (rdfdt.isValidValue(lex)) {
				return ontModel.createTypedLiteral(lex, dataType);
			} else {
				throw new DatatypeFormatException(lex, rdfdt, "Value does not match datatype.");
			}
		} else if (obj instanceof XSDDuration) {
			String lex = DurationToXSD((XSDDuration) obj, rdfdt);
			if (rdfdt.isValidValue(lex)) {
				return ontModel.createTypedLiteral(lex, dataType);
			} else {
				throw new DatatypeFormatException(lex, rdfdt, "Value does not match datatype.");
			}
		} else if (rdfdt.isValid(obj.toString())) {
			return ontModel.createTypedLiteral(obj.toString(), dataType);
		} else {
			throw new DatatypeFormatException(obj.toString(), rdfdt,
					"Value does not match datatype.");
		}
	}

	public static BigDecimal getBigDecimal(Literal l) {
		Object o = l.getValue();
		if (o instanceof BigDecimal) {
			return (BigDecimal) o;
		} else if (o instanceof Number) {
			Number n = (Number) o;
			try {
				return new BigDecimal(n.toString());
			} catch (NumberFormatException e) {
				log.error("Error in getBigDecimal for literal " + l.toString());
				return null;
			}
		} else {
			return null;
		}
	}

	public static BigInteger getBigInteger(Literal l) {
		Object o = l.getValue();
		if (o instanceof BigInteger) {
			return (BigInteger) o;
		} else if (o instanceof Number) {
			Number n = (Number) o;
			try {
				return new BigInteger(n.toString());
			} catch (NumberFormatException e) {
				log.error("Error in getBigInteger for literal " + l.toString());
				return null;
			}
		} else {

			return null;
		}
	}

	public static Boolean getBoolean(Literal l) {
		return new Boolean(l.getBoolean());
	}

	public static Byte getByte(Literal l) {
		return new Byte(l.getByte());
	}

	public static Calendar getCalendar(Literal l) {
		try {
			Object o = l.getValue();
			if (o instanceof Calendar) {
				return (Calendar) o;
			} else if (o instanceof XSDDateTime) {
				Calendar c = ((XSDDateTime) o).asCalendar();
				return c;
			}
		} catch (DatatypeFormatException e) {
			log.error("Error in getCalendar for literal " + l.toString());
		}
		return null;
	}

	public static Character getCharacter(Literal l) {
		return new Character(l.getChar());
	}

	public static Double getDouble(Literal l) {
		return new Double(l.getDouble());
	}

	public static Float getFloat(Literal l) {
		return new Float(l.getFloat());
	}

	public static Integer getInteger(Literal l) {
		return new Integer(l.getInt());
	}

	public static Long getLong(Literal l) {
		return new Long(l.getLong());
	}

	public static Short getShort(Literal l) {
		return new Short(l.getShort());
	}

	public static String getString(Literal l) {
		return l.getString();
	}

	public static XSDDateTime getXSDDateTime(Literal l) {
		Object o = l.getValue();
		if (o instanceof XSDDateTime) {
			XSDDateTime dt = (XSDDateTime) o;
			return dt;
		}
		return null;
	}

	public static XSDDuration getXSDDuration(Literal l) {
		Object o = l.getValue();
		if (o instanceof XSDDuration) {
			XSDDuration d = (XSDDuration) o;
			return d;
		}
		return null;
	}

	public static final Map1<Statement, BigDecimal> objectAsBigDecimalMapper = new Map1<Statement, BigDecimal>() {
		@Override
		public BigDecimal map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getBigDecimal(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to BigDecimal");
				return null;
			}
		}
	};

	public static final Map1<Statement, BigInteger> objectAsBigIntegerMapper = new Map1<Statement, BigInteger>() {
		@Override
		public BigInteger map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getBigInteger(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to BigInteger");
				return null;
			}
		}
	};

	public static final Map1<Statement, Boolean> objectAsBooleanMapper = new Map1<Statement, Boolean>() {
		@Override
		public Boolean map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getBoolean(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Boolean");
				return null;
			}
		}
	};

	public static final Map1<Statement, Byte> objectAsByteMapper = new Map1<Statement, Byte>() {
		@Override
		public Byte map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getByte(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Byte");
				return null;
			}
		}
	};

	public static final Map1<Statement, Character> objectAsCharacterMapper = new Map1<Statement, Character>() {
		@Override
		public Character map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getCharacter(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Character");
				return null;
			}
		}
	};

	public static final Map1<Statement, Double> objectAsDoubleMapper = new Map1<Statement, Double>() {
		@Override
		public Double map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getDouble(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Double");
				return null;
			}
		}
	};

	public static final Map1<Statement, Float> objectAsFloatMapper = new Map1<Statement, Float>() {
		@Override
		public Float map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getFloat(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Float");
				return null;
			}
		}
	};

	public static final Map1<Statement, Integer> objectAsIntegerMapper = new Map1<Statement, Integer>() {
		@Override
		public Integer map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getInteger(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Integer");
				return null;
			}
		}
	};

	public static final Map1<Statement, Long> objectAsLongMapper = new Map1<Statement, Long>() {
		@Override
		public Long map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getLong(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Long");
				return null;
			}
		}
	};

	public static final Map1<Statement, Short> objectAsShortMapper = new Map1<Statement, Short>() {
		@Override
		public Short map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getShort(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Short");
				return null;
			}
		}
	};

	public static final Map1<Statement, String> objectAsStringMapper = new Map1<Statement, String>() {
		@Override
		public String map1(Statement x) {
			if (x instanceof Statement)
				return x.getString();
			return null;
		}
	};

	public static final Map1<Statement, Calendar> objectAsCalendarMapper = new Map1<Statement, Calendar>() {
		@Override
		public Calendar map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getCalendar(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to Calendar");
				return null;
			}
		}
	};

	public static final Map1<Statement, XSDDuration> objectAsXSDDurationMapper = new Map1<Statement, XSDDuration>() {
		@Override
		public XSDDuration map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getXSDDuration(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to XsdDuration");
				return null;
			}
		}
	};

	public static final Map1<Statement, XSDDateTime> objectAsXSDDateTimeMapper = new Map1<Statement, XSDDateTime>() {
		@Override
		public XSDDateTime map1(Statement x) {
			try {
				Literal l = x.getLiteral();
				return getXSDDateTime(l);
			} catch (Exception e) {
				log.warn("Could not convert statement " + x + "to XsdDateTime");
				return null;
			}
		}
	};

}