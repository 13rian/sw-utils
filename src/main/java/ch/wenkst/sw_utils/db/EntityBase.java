package ch.wenkst.sw_utils.db;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Version;

/**
 * minimal entity to store in the mongoDB that is meant to be extended 
 */
public class EntityBase {
	@Id
	private String id; 				// always required

	@Version
	private Long version = null; 	// managed by morphia - do not set

	public EntityBase() {
		id = new ObjectId().toHexString();
	}
	
	
	// getters and setters are not needed for mapping
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}


	

}


