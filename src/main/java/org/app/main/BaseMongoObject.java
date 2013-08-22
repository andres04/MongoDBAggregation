package org.app.main;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public abstract class BaseMongoObject {
	public BaseMongoObject() {
		super();
		this.id = ObjectId.get();
	}

	@Id
	private ObjectId id;

	public ObjectId getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this.id==null) return false;
		return this.id.equals(((BaseMongoObject)obj).getId());
	}

}
