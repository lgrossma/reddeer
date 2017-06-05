/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.reddeer.junit.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.junit.requirement.Requirement;

/**
 * Annotation utils provide various useful methods for processing annotations.
 * 
 * @author mlabuda@redhat.com
 *
 */
public class AnnotationUtils {	
	
	/**
	 * Gets all requirement annotations on the class including annotations present on parent
	 * classes up to Object class.
	 * 
	 * @param clazz
	 *            class to get its requirement annotations
	 * @return list of annotation on a class and on its ancestors
	 */
	public static List<Annotation> getRequirementAnnotations(Class<?> clazz) {
		List<Annotation> result = new ArrayList<>();
		List<Annotation> classAnnotations = getClassLevelAnnotations(clazz);
		for (Annotation annotation: classAnnotations) {
			if (getEnclosingRequirementClass(annotation.annotationType()) != null) {
				result.add(annotation);
			}
		}
		return result;
	}

	/**
	 * Gets all class level annotation up to Object class for a specified class.
	 * 
	 * @param clazz class to get its annotations
	 * @return list of class level annotations for specified class
	 */
	public static List<Annotation> getClassLevelAnnotations(Class<?> clazz) {
		List<Annotation> result = new ArrayList<Annotation>();
		Class<?> currentClass = clazz;
		while (!currentClass.equals(Object.class)) {
			Annotation[] annotations = currentClass.getDeclaredAnnotations();
			for (Annotation annotation : annotations) {
				if (!listContainsAnnotation(result, annotation.annotationType())) {
					result.add(annotation);
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return result;
	}
	
	/**
	 * Gets enclosing requirement class of an annotation class
	 * 
	 * @param clazz
	 *            annotation class
	 * @return enclosing requirement class if exists, null otherwise
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> Class<Requirement<Annotation>> getEnclosingRequirementClass(Class<T> clazz) {
		Class<?> enclosingClass = clazz.getEnclosingClass();
		if (enclosingClass != null && Requirement.class.isAssignableFrom(enclosingClass)) {
			return (Class<Requirement<Annotation>>) enclosingClass;
		}
		return null;
	}

	/**
	 * Executes a static non-parametric method with annotation, if exists.
	 * Otherwise do nothing and return null.
	 * 
	 * @param clazz
	 *            class where to obtain and execute a method with a specified
	 *            annotation
	 * @param annotationClass
	 *            annotation class
	 * @param <T>
	 *            annotation type
	 * @return object returned from invoked static method annotated with an
	 *         annotation, null if such object does not exist
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation, K extends Object> K invokeStaticMethodWithAnnotation(Class<?> clazzWithMethod,
			Class<T> annotationClass, Class<K> methodReturnType) {

		for (Method method : clazzWithMethod.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotationClass)) {
				try {
					if (!method.getReturnType().isAssignableFrom(methodReturnType)) {
						throw new RedDeerException("Method annotated with annotation " + annotationClass
								+ " does not have specified return type " + methodReturnType);
					}
					return (K) method.invoke(null, (Object[]) null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RedDeerException("Something were wrong when executing a method annotated with "
							+ " annotation " + annotationClass, e);
				}
			}
		}
		return null;
	}

	private static boolean listContainsAnnotation(List<Annotation> annotations, Class<? extends Annotation> annotationClass) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(annotationClass)) {
				return true;
			}
		}
		return false;
	}
}