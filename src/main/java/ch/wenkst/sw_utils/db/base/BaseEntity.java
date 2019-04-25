package ch.wenkst.sw_utils.db.base;

import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.reactivestreams.client.Success;

import ch.wenkst.sw_utils.db.MongoDBHandler;
import ch.wenkst.sw_utils.db.subscriber.CallbackSubscriber;

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
		CallbackSubscriber<Success> subscriber = new CallbackSubscriber<>((result, error) ->  {
			if (error != null) {
				logger.error("failed to save the entity " + getClass().getSimpleName() + " to the db: ", error);
			}
		});
    	
    	dbHandler.insertOne(this, subscriber);    	
    }
    
    
    /**
     * saves this entity to the db
     * @param subscriber 	 the subscriber to the insert one publisher
     */
    public void saveToDB(Subscriber<Success> subscriber) {
    	MongoDBHandler dbHandler = MongoDBHandler.getInstance();
    	dbHandler.insertOne(this, subscriber);
    	
    }
}