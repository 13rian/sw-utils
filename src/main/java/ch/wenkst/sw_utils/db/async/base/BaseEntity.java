package ch.wenkst.sw_utils.db.async.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;

import com.mongodb.reactivestreams.client.Success;

import ch.wenkst.sw_utils.db.async.MongoDBHandlerAsync;
import ch.wenkst.sw_utils.db.async.subscriber.CallbackSubscriber;

public class BaseEntity {
	final static Logger logger = LogManager.getLogger(BaseEntity.class);    // initialize the logger
	
    private String id;

    /**
     * models the base class for the entity in the db
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
    	MongoDBHandlerAsync dbHandler = MongoDBHandlerAsync.getInstance();
		CallbackSubscriber<Success> subscriber = new CallbackSubscriber<>((result, error) ->  {
			if (error == null) {
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
    	MongoDBHandlerAsync dbHandler = MongoDBHandlerAsync.getInstance();
    	dbHandler.insertOne(this, subscriber);
    	
    }
}