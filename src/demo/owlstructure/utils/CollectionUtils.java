package demo.owlstructure.utils;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionUtils {
	public static <T> Collection<T> intersectCollections(Collection<? extends T> collection1, Collection<? extends T> collection2) {
		Collection<T> result = new ArrayList<T>(collection1);
		result.retainAll(collection2);
		return result;
	}

	public static <T> Collection<T> subtractCollections(Collection<? extends T> collection1, Collection<? extends T> collection2) {
		Collection<T> result = new ArrayList<T>(collection1);
		result.removeAll(collection2);
		return result;
	}
}