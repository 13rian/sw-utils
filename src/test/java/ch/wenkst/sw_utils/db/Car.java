package ch.wenkst.sw_utils.db;

import org.mongodb.morphia.annotations.Entity;

import ch.wenkst.sw_utils.db.EntityBase;

/**
 * test class to map into mongoDB with morphia, the name of the collection is
 * defined in the Entity annotation. note: morphia needs a zero-arg constructor
 */
@Entity(value="Car", noClassnameStored=true)
public class Car extends EntityBase {
	private String name = "";
	private int weight = 0;
	
	/**
	 * zero-arg constructor needed for morphia
	 */
	public Car() {
		name = "";
		weight = 0;
	}
	
	public Car(String name, int weight) {
		this.name = name;
		this.weight = weight;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	
	
}
