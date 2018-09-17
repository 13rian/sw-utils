package ch.wenkst.sw_utils.reflection;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionUtils {
	private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);
	
	/**
	 *  allows to read out a property of an object if only the property name as String is known
	 *  the value of the field is returned.
	 *  @param object 		the object that contains the field of interest
	 *  @param fieldName 	the name of the field that should be read out
	 *  @return 			the value of the passed filed name or null if an error occurred
	 */
	public static Object getObjProperty(Object object, String fieldName) {
		try {
			Class<?> objectClass = object.getClass();     			// get the class of the object
			Field field = objectClass.getDeclaredField(fieldName);	// get the field of the object
			field.setAccessible(true); 			
	
			Object fieldValue = field.get(object);				    // get the value of the field
			return fieldValue; 
		
		} catch (Exception e) {
			logger.error("Can not get object property " + fieldName + ": ", e);
			return null;
		}
	}

	
	/**
	 *  allows to set a property of an object if only the property name as String is known
	 * 	@param object 		the object that contains the field of interest
	 *  @param fieldName 	the name of the field that should be set
	 * 	@param valueToSet 	the new value to set
	 *  @return	 			returns true if property was successfully set and false otherwise
	 */
	public static boolean setObjProperty(Object object, String fieldName, Object valueToSet) {
		try {
			Class<?> objectClass = object.getClass(); 				// get the class of the object
			Field field = objectClass.getDeclaredField(fieldName);	// get the field of the object
			field.setAccessible(true);
	
			field.set(object, valueToSet);
			return true;
		
		} catch (Exception e) {
			logger.error("Can not set object property " + fieldName + ": ", e);
			return false;
		}
	}

}
