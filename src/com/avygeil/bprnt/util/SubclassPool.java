package com.avygeil.bprnt.util;

import java.util.HashMap;
import java.util.Map;

public class SubclassPool<T> {
	
	private Class<T> superClassType; // required because we can't determine the parent type statically
	
	public SubclassPool(Class<T> superClassType) {
		this.superClassType = superClassType;
	}
	
	private Map<String, Class<? extends T>> pool = new HashMap<>();
	
	public void registerClass(String newClassName) throws ClassNotFoundException, ClassCastException {
		registerClass(Class.forName(newClassName).asSubclass(superClassType));
	}
	
	public void registerClass(Class<? extends T> newClass) {
		pool.put(newClass.getName(), newClass);
	}
	
	public int getNumClasses() {
		return pool.size();
	}
	
	public T newInstance(String className) throws IllegalAccessException, InstantiationException, IllegalArgumentException {
		Class<? extends T> clazz = pool.get(className);
		
		if (clazz == null) {
			throw new IllegalArgumentException();
		}
		
		return clazz.newInstance();
	}

}
