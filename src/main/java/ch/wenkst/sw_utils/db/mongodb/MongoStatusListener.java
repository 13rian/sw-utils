package ch.wenkst.sw_utils.db.mongodb;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.event.ServerClosedEvent;
import com.mongodb.event.ServerDescriptionChangedEvent;
import com.mongodb.event.ServerListener;
import com.mongodb.event.ServerOpeningEvent;

public class MongoStatusListener implements ServerListener {
	private static final Logger logger = LoggerFactory.getLogger(MongoStatusListener.class);
	
	private CompletableFuture<Throwable> connectionFuture = null;
	
	public MongoStatusListener(CompletableFuture<Throwable> connectionFuture) {
		super();
		this.connectionFuture = connectionFuture;
	}
	
	
    @Override
    public void serverOpening(ServerOpeningEvent event) {}

    @Override
    public void serverClosed(ServerClosedEvent event) {}

    @Override
    public void serverDescriptionChanged(ServerDescriptionChangedEvent event) {
        if (event.getNewDescription().isOk()) {
        	connectionFuture.complete(null);
        	
        } else if (event.getNewDescription().getException() != null) {
            logger.error("error in mongo server description: " + event.getNewDescription().getException().getMessage());
            connectionFuture.complete(event.getNewDescription().getException());
        }
    }
}