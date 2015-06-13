package apollo.common.templates;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import apollo.common.util.Mapper;

/**
 * BookMap class that manages the matching and pairing of bids and asks.
 * @author santana
 *
 * @param <V>
 * 		Some extension of SwapMapper. SwapMapper class is used for loose mapping of swaps
 * @param <T>
 * 		Some extension of Swap. Swaps are the tangible objects
 */
public class BookMap<V extends SwapMapper, T extends Swap> {
	
	//map that holds all the swaps
	private HashMap<V, Map<V, Set<T>>> map = null;
	
	
	/**
	 * default constructor. instantiates new hashmap that backs the book map
	 */
	public BookMap() {
		map = new HashMap<V, Map<V, Set<T>>>();
	}
	
	/**
	 * puts the key, value pair in the map if both the key and the value are valid
	 * @param key
	 * 		key used to map to the value
	 * @param value
	 * 		value used to to map to itself in the Set
	 */
	public void put(T key, T value) {
		if( key != null && key.valid() && value != null && value.valid() ) {
			V mapper = Mapper.getMapper(key);
			V valueMapper = Mapper.getMapper(value);
	
			Map<V, Set<T>> valueMap = map.get(mapper);
			Set<T> valueSet = null;
			
			if(valueMap == null) {
				valueMap = new HashMap<V, Set<T>>();
				valueSet = new LinkedHashSet<T>();
				valueMap.put(valueMapper, valueSet);
				map.put(mapper, valueMap);	
			}
			else {
				valueSet = valueMap.get(valueMapper);
				if(valueSet == null) {
					valueSet = new LinkedHashSet<T>();
				}
			}
			
			valueSet.add(value);
			valueMap.put(valueMapper, valueSet);
		}
	}
	
	/**
	 * gets a match that equals the value swap passed in that is mapped by key. The key and the matched swap
	 * must be valid. This method will find a match to the value passed in and insure that it 
	 * cannot me matched elsewhere. The matched swap is removed from the map
	 * @param key
	 * 		key pair to the value. this will be matched
	 * @param value
	 * 		value swap used to find comparable match swap in map
	 * @return
	 * 		the swap that matches value and is readily available
	 */
	public T get(T key, T value) {
		T match = null;

		if(key != null && key.valid() && value != null && value.valid()) {
			V mapper = Mapper.getMapper(key);
			V valueMapper = Mapper.getMapper(value);
	
			Map<V, Set<T>> valueMap = map.get(mapper);
			
			if(valueMap != null) {
				Set<T> valueSet = valueMap.get(valueMapper);
				if(valueSet != null) {
					Iterator<T> iterator = valueSet.iterator();
					if(iterator.hasNext()) {
						T tmpMatch = iterator.next();
						UUID id = tmpMatch.match(value);
						if(id != null && tmpMatch.valid()) {
							match = tmpMatch;
							match.setMatchId(id);
							key.setMatchId(id);
							valueSet.remove(match);
						}
					}
				}
			}
		}
		return match;
	}
	
	/**
	 * gets a match that equals the keyValue and is paired with itself (bid/bid). The keyValue must be valid.
	 * This method will find a match to the keyValue passed in and insure that it 
	 * cannot me matched elsewhere. The matched swap is removed from the map
	 * @param keyValue
	 * 		value that is paired to itself. this will be matched
	 * @return
	 * 		the swap that matches value and is readily available
	 */
	public T get(T keyValue) {
		T match = null;

		if(keyValue != null && keyValue.valid()) {
			V mapper = Mapper.getMapper(keyValue);
	
			Map<V, Set<T>> valueMap = map.get(mapper);
			
			if(valueMap != null) {
				Set<T> valueSet = valueMap.get(mapper);
				if(valueSet != null) {
					Iterator<T> iterator = valueSet.iterator();
					if(iterator.hasNext()) {
						T tmpMatch = iterator.next();
						UUID id = tmpMatch.match(keyValue);
						if(id != null && tmpMatch.valid() && tmpMatch.isForSale()) {
							match = tmpMatch;
							match.setMatchId(id);
							valueSet.remove(match);
						}
					}
				}
			}
		}
		return match;
	}
	
	/**
	 * gets a match that equals the value swap passed in that is mapped by key. The key and the matched swap
	 * must be valid. This method will find a match to the value but will not remove match from the map.
	 * Nor will it block others from matching with it at a later time. equivalent of peeking into the map
	 * @param key
	 * 		key pair to the value. this will be matched
	 * @param value
	 * 		value swap used to find comparable match swap in map
	 * @return
	 * 		the swap that equals the value and is readily available
	 */
	public T check(T key, T value) {
		T match = null;

		if(key != null && key.valid() && value != null && value.valid()) {
			V mapper = Mapper.getMapper(key);
			V valueMapper = Mapper.getMapper(value);
	
			Map<V, Set<T>> valueMap = map.get(mapper);
			
			if(valueMap != null) {
				Set<T> valueSet = valueMap.get(valueMapper);
				if(valueSet != null) {
					Iterator<T> iterator = valueSet.iterator();
					if(iterator.hasNext()) {
						T tmpMatch = iterator.next();
						UUID id = tmpMatch.match(value);
						if(id != null && tmpMatch.valid()) {
							match = tmpMatch;
						}
					}
				}
			}
		}
		return match;
	}
	
	/**
	 * gets a match that equals the keyValue and is paired with itself (bid/bid). The keyValue must be valid.
	 * This method will find a match to the keyValue passed and will not remove the match from the map.
	 * Nor will it block any other get from matching with this swap
	 * @param keyValue
	 * 		value that is paired to itself.
	 * @return
	 * 		the swap that equals the keyValue and is readily available
	 */
	public T check(T keyValue) {
		T match = null;
		
		if(keyValue != null && keyValue.valid()) {
			V mapper = Mapper.getMapper(keyValue);

			Map<V, Set<T>> valueMap = map.get(mapper);
			
			if(valueMap != null) {
				Set<T> valueSet = valueMap.get(mapper);
				if(valueSet != null) {
					Iterator<T> iterator = valueSet.iterator();
					if(iterator.hasNext()) {
						T tmpMatch = iterator.next();
						UUID id = tmpMatch.match(keyValue);
						if(id != null && tmpMatch.valid() && tmpMatch.isForSale()) {
							match = tmpMatch;
						}
					}
				}
			}
		}
		return match;
	}
	
	/**
	 * Removes the key / value pair from this map
	 * @param key
	 * 			key that the value is paired to
	 * @param value
	 * 			value to be removed from the map
	 * @return
	 * 			true - value was removed from the map
	 */
	public boolean remove(T key, T value) {
		boolean removed = false;

		if(key != null && value != null) {
			V mapper = Mapper.getMapper(key);
			V valueMapper = Mapper.getMapper(value);
	
			Map<V, Set<T>> valueMap = map.get(mapper);
			
			if(valueMap != null) {
				Set<T> valueSet = valueMap.get(valueMapper);
				if(valueSet != null) {
					removed = valueSet.remove(value);
				}
			}
		}
		return removed;
	}
	
	/**
	 * gets all swaps associated with that key
	 * @param key
	 * 		key pair for all swaps returned
	 * @return
	 * 		Set of swaps that pair with key
	 */
	public Set<T> getSet(T key) {		
		Set<T> values =  new HashSet<T>();
		
		if(key != null && key.valid()) {
			V mapper = Mapper.getMapper(key);
	
			Map<V, Set<T>> valueMap = map.get(mapper);
			
			if(valueMap != null) {
				for(V valueMapper : valueMap.keySet()) {
					Set<T> valueSet = valueMap.get(valueMapper);
					if(valueSet != null) {
						values.addAll(valueSet);
					}
				}
				
			}
		}
		return values;
	}
	
	/**
	 * gets all swaps stored in this map
	 * @return
	 * 		Set of all swaps in this map
	 */
	public Set<T> getAll() {
		Set<T> values =  new HashSet<T>();

		for(V mapper : map.keySet()) {
			Map<V, Set<T>> valueMap = map.get(mapper);
			
			if(valueMap != null) {
				for(V valueMapper : valueMap.keySet()) {
					Set<T> valueSet = valueMap.get(valueMapper);
					if(valueSet != null) {
						values.addAll(valueSet);
					}
				}
				
			}
		}
		return values;
	}
	
	/**
	 * clears the entire map out.
	 */
	public void clear() {
		map.clear();
	}
	
	

}
