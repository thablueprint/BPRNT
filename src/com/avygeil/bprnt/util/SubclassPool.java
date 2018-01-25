package com.avygeil.bprnt.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SubclassPool<T> {
	
	private Class<T> superClassType; // required because we can't determine the parent type statically
	private Map<String, Class<? extends T>> pool = new HashMap<>();
	
	public SubclassPool(Class<T> superClassType) {
		this.superClassType = superClassType;
	}
	
	public void registerClass(String newClassName) throws ClassNotFoundException, ClassCastException {
		registerClass(Class.forName(newClassName).asSubclass(superClassType));
	}
	
	public void registerClass(Class<? extends T> newClass) {
		pool.put(newClass.getName(), newClass);
	}
	
	public int getNumClasses() {
		return pool.size();
	}
	
	public Collection<Class<? extends T>> getClasses() {
		return Collections.unmodifiableCollection(pool.values());
	}
	
	public Class<? extends T> getClassWithName(String className) {
		return pool.get(className);
	}

}
