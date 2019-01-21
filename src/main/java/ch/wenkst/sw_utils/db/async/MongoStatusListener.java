package ch.wenkst.sw_utils.db.async;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.event.ServerClosedEvent;
import com.mongodb.event.ServerDescriptionChangedEvent;
import com.mongodb.event.ServerListener;
import com.mongodb.event.ServerOpeningEvent;

public class MongoStatusListener implements ServerListener {
	final static Logger logger = LogManager.getLogger(MongoStatusListener.class);
	
	private CompletableFuture<Boolean> connectionFuture = null;
	
	public MongoStatusListener(CompletableFuture<Boolean> connectionFuture) {
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
        	connectionFuture.complete(true);
        } else if (event.getNewDescription().getException() != null) {
            logger.error("error in mongo server description: ", event.getNewDescription().getException());
            connectionFuture.complete(false);
        }
    }
}