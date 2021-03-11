package ch.wenkst.sw_utils.db.mongodb.entity;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.result.InsertOneResult;

import ch.wenkst.sw_utils.db.mongodb.MongoDBHandler;
import ch.wenkst.sw_utils.db.mongodb.subscriber.value.ValueCallback;

public class BaseEntity {
	private static final Logger logger = LoggerFactory.getLogger(BaseEntity.class);
	
    private String id;

    /**
     * models the base class for the entity in the db
     * care: array properties are not supported, use lists instead
     */
    public BaseEntity() {
    	id = new ObjectId().toHexString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * asynchronously saves this entity to the db the 
     */
    public void saveToDB() {
    	MongoDBHandler dbHandler = MongoDBHandler.getInstance();
    	
    	dbHandler.insert(this, (result, error) -> {
    		if (error != null) {
				logger.error("failed to save the entity " + getClass().getSimpleName() + " to the db: ", error);
			}
    	});    	
    }
    
    
    /**
     * saves this entity to the db
     * @param subscriber 	 the subscriber to the insert one publisher
     */
    public void saveToDB(ValueCallback<InsertOneResult> resultCallback) {
    	MongoDBHandler dbHandler = MongoDBHandler.getInstance();    	
    	dbHandler.insert(this, resultCallback);
    }
}