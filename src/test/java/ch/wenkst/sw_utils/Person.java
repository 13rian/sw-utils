package ch.wenkst.sw_utils;


import org.bson.types.ObjectId;

import ch.wenkst.sw_utils.db.async.base.BaseEntity;
import ch.wenkst.sw_utils.db.async.base.EntityInfo;

@EntityInfo(db="AsyncTest", collection="Person")
public final class Person extends BaseEntity {
    private String id;
    private String name;
    private int age;
    private Address address;

    public Person() {
    	id = new ObjectId().toHexString();
    }
    
    public Person(String name, int age, Address address) {
    	id = new ObjectId().toHexString();
    	
    	this.name = name;
    	this.age = age;
    	this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

}