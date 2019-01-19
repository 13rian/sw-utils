package ch.wenkst.sw_utils.db.async.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * defines the annotation that is used to save the db name and the collection name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) 				//on class level
public @interface EntityInfo {
	String db() default ""; 			// to save the name of the db
	 
	String collection() default ""; 	// to save the name of the collection	
}
