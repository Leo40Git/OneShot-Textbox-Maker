package com.leo.ostbm.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class UnmodifiableMapWrapper<K, V> implements Map<K, V> {
	
	private Map<K, V> wrapped;
	
	public UnmodifiableMapWrapper(Map<K, V> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public int size() {
		return wrapped.size();
	}

	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return wrapped.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return wrapped.containsValue(wrapped);
	}

	@Override
	public V get(Object key) {
		return wrapped.get(key);
	}

	@Override
	public V put(K key, V value) {
		return null;
	}

	@Override
	public V remove(Object key) {
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
	}

	@Override
	public void clear() {
	}

	@Override
	public Set<K> keySet() {
		return new UnmodifiableSetWrapper<>(wrapped.keySet());
	}

	@Override
	public Collection<V> values() {
		return new UnmodifiableCollectionWrapper<>(wrapped.values());
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new UnmodifiableSetWrapper<>(wrapped.entrySet());
	}

}
