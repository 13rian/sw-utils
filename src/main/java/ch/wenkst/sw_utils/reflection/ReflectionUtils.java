package ch.wenkst.sw_utils.reflection;

import java.lang.reflect.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReflectionUtils {
	final static Logger logger = LogManager.getLogger(ReflectionUtils.class);    // initialize the logger
	
	/**
	 *  allows to read out a property of an object if only the property name as String is known
	 *  the value of the field is returned. It needs to be casted to the type of the field value since it is of type Object
	 *  @param object 		the object that contains the field of interest
	 *  @param fieldName 	the name of the field that should be read out
	 *  @return 			field value of type Object, or an empty string if an error occurs
	 */
	public static Object getObjPropertyFromFieldName(Object object, String fieldName) {
		try {
			Class<?> objectClass = object.getClass();     			// get the class of the object
			Field field = objectClass.getDeclaredField(fieldName);	// get the field of the object
			field.setAccessible(true); 			
	
			Object fieldValue = field.get(object);				    // get the value of the field
			return fieldValue; 
		
		} catch (Exception e) {
			logger.error("Can not get object property " + fieldName + ": ", e);
			return "";
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
