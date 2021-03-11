package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

import ch.wenkst.sw_utils.db.mongodb.entity.BaseEntity;

public class PojoListCallbackSubscriber extends ListSubscriber<BaseEntity> {
	private PojoListCallback callback;
	
	public PojoListCallbackSubscriber(PojoListCallback callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {				
		callback.onResult(result, error);
	}
}
