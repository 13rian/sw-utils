package ch.wenkst.sw_utils.db.mongodb.base;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.reactivestreams.client.Success;

import ch.wenkst.sw_utils.db.mongodb.MongoDBHandler;
import ch.wenkst.sw_utils.db.mongodb.subscriber.IResultCallback;

public class BaseEntity {
	private static final Logger logger = LoggerFactory.getLogger(BaseEntity.class);
	
    private String id;

    /**
     * models the base class for the entity in the db
     * care: arrays are not supported, use list instead
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
    public void saveToDB(IResultCallback<Success> resultCallback) {
    	MongoDBHandler dbHandler = MongoDBHandler.getInstance();    	
    	dbHandler.insert(this, resultCallback);
    }
}