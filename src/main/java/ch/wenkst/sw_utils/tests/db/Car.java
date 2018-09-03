package ch.wenkst.sw_utils.tests.db;

import org.mongodb.morphia.annotations.Entity;

import ch.wenkst.sw_utils.db.EntityBase;

/**
 * test class to map into mongoDB with morphia, the name of the collection is
 * defined in the Entity annotation. note: morphia needs a zero-arg constructor
 */
@Entity(value="test_morphia", noClassnameStored=true)
public class Car extends EntityBase {
	// define the name of the fields in this entity
	public static final String FIELD_NAME = "name";
	public static final String FIELD_WEIGHT = "weight";
	
	private String name = "";
	private double weight = 0;
	
	/**
	 * zero-arg constructor needed for morphia
	 */
	public Car() {
		name = "";
		weight = 0;
	}
	
	public Car(String name, double weight) {
		this.name = name;
		this.weight = weight;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	
	
}
