package demo;

import java.util.List;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.Individual;

/**
 * Interface http://oss.fruct.org/etourism#Location
 */

public interface ILocation extends Individual, demo.IThing {

	/**
	 * Domain property Lon
	 * with uri http://oss.fruct.org/etourism#lon
	 */

	public boolean existsLon();

	public boolean hasLon(java.lang.Double doubleValue);

	public int countLon();

	public Iterator<java.lang.Double> iterateLon();

	public List<java.lang.Double> listLon();

	public void addLon(java.lang.Double doubleValue);

	public void addAllLon(List<java.lang.Double> doubleList);

	public void removeLon(java.lang.Double doubleValue);

	public void removeAllLon();

	/**
	 * Domain property Lat
	 * with uri http://oss.fruct.org/etourism#lat
	 */

	public boolean existsLat();

	public boolean hasLat(java.lang.Double doubleValue);

	public int countLat();

	public Iterator<java.lang.Double> iterateLat();

	public List<java.lang.Double> listLat();

	public void addLat(java.lang.Double doubleValue);

	public void addAllLat(List<java.lang.Double> doubleList);

	public void removeLat(java.lang.Double doubleValue);

	public void removeAllLat();

}