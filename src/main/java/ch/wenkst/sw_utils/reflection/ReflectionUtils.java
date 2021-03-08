package ch.wenkst.sw_utils.reflection;

import java.lang.reflect.Field;

public class ReflectionUtils {
	
	/**
	 *  allows to read out a property of an object if only the property name as String is known
	 *  the value of the field is returned.
	 *  @param object 		the object that contains the field of interest
	 *  @param fieldName 	the name of the field that should be read out
	 *  @return 			the value of the passed filed name or null if an error occurred
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static Object getObjProperty(Object object, String fieldName)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = getAccessibleField(object, fieldName);
		Object fieldValue = field.get(object);
		return fieldValue; 
	}

	
	/**
	 *  allows to set a property of an object if only the property name as String is known
	 * 	@param object 		the object that contains the field of interest
	 *  @param fieldName 	the name of the field that should be set
	 * 	@param valueToSet 	the new value to set
	 *  @return	 			returns true if property was successfully set and false otherwise
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static boolean setObjProperty(Object object, String fieldName, Object valueToSet)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = getAccessibleField(object, fieldName);
		field.set(object, valueToSet);
		return true;
	}
	
	
	private static Field getAccessibleField(Object object, String fieldName) throws NoSuchFieldException, SecurityException {
		Class<?> objectClass = object.getClass();
		Field field = objectClass.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field;
	}
}
