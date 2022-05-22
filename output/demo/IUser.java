package demo;

import java.util.List;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.Individual;

/**
 * Interface http://oss.fruct.org/etourism#User
 */

public interface IUser extends Individual, demo.IThing {

	/**
	 * Domain property Location
	 * with uri http://oss.fruct.org/etourism#hasLocation
	 */

	public boolean existsLocation();

	public boolean hasLocation(demo.ILocation locationValue);

	public int countLocation();

	public Iterator<demo.Location> iterateLocation();

	public List<demo.Location> listLocation();

	public void addLocation(demo.ILocation locationValue);

	public void addAllLocation(List<? extends demo.ILocation> locationList);

	public void removeLocation(demo.ILocation locationValue);

	public void removeAllLocation();

	/**
	 * Domain property Surname
	 * with uri http://oss.fruct.org/etourism#surname
	 */

	public boolean existsSurname();

	public boolean hasSurname(java.lang.String stringValue);

	public int countSurname();

	public Iterator<java.lang.String> iterateSurname();

	public List<java.lang.String> listSurname();

	public void addSurname(java.lang.String stringValue);

	public void addAllSurname(List<java.lang.String> stringList);

	public void removeSurname(java.lang.String stringValue);

	public void removeAllSurname();

	/**
	 * Domain property Preferences
	 * with uri http://oss.fruct.org/etourism#preferences
	 */

	public boolean existsPreferences();

	public boolean hasPreferences(java.lang.String stringValue);

	public int countPreferences();

	public Iterator<java.lang.String> iteratePreferences();

	public List<java.lang.String> listPreferences();

	public void addPreferences(java.lang.String stringValue);

	public void addAllPreferences(List<java.lang.String> stringList);

	public void removePreferences(java.lang.String stringValue);

	public void removeAllPreferences();

	/**
	 * Domain property Name
	 * with uri http://oss.fruct.org/etourism#name
	 */

	public boolean existsName();

	public boolean hasName(java.lang.String stringValue);

	public int countName();

	public Iterator<java.lang.String> iterateName();

	public List<java.lang.String> listName();

	public void addName(java.lang.String stringValue);

	public void addAllName(List<java.lang.String> stringList);

	public void removeName(java.lang.String stringValue);

	public void removeAllName();

}