package com.leo.ostbm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class UnmodifiableSetWrapper<V> implements Set<V> {
	
	private Set<V> wrapped;
	
	public UnmodifiableSetWrapper(Set<V> wrapped) {
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
	public boolean contains(Object o) {
		return wrapped.contains(o);
	}

	@Override
	public Iterator<V> iterator() {
		return wrapped.iterator();
	}

	@Override
	public Object[] toArray() {
		return wrapped.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return wrapped.toArray(a);
	}

	@Override
	public boolean add(V e) {
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return wrapped.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends V> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
	}

}
