package de.blogspot.soahowto.ppm;

import java.util.Map;

public class FluentMapDecorator<K, V> {
	private Map<K, V> map;

	public FluentMapDecorator(Map<K, V> map) {
		this.map = map;
	}

	public Map<K, V> map() {
		return map;
	}

	public FluentMapDecorator<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}
}
