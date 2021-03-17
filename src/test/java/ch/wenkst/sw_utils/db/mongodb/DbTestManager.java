package ch.wenkst.sw_utils.db.mongodb;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;

import ch.wenkst.sw_utils.db.mongodb.entitiy.Address;
import ch.wenkst.sw_utils.db.mongodb.entitiy.Person;
import ch.wenkst.sw_utils.miscellaneous.StatusResult;

public class DbTestManager {
	private String dbName = "AsyncTest";
	private String ip = "192.168.0.129";
	private int port = 27017;
	
	private Person person = new Person("Ada Byron", 20, new Address("St James Square", "London", "W1"));
	
	private List<Person> personList = Arrays.asList(
			new Person("Charles Babbage", 45, new Address("5 Devonshire Street", "London", "W11")),
			new Person("Alan Turing", 28, new Address("Bletchley Hall", "Bletchley Park", "MK12")),
			new Person("Timothy Berners-Lee", 61, new Address("Colehill", "Wimborne", null))
	);
	
	
	public void connectToDb() throws DbConnectException, InterruptedException, ExecutionException, TimeoutException {
		DbConnectOptions options = new DbConnectOptions()
				.host(ip)
				.port(port)
				.connectTimeoutInSecs(10)
				.dbName(dbName)
				.packageNames(new String[] {Person.class.getPackage().getName()});
		
		MongoDBHandler.getInstance().connectToDB(options);
	}
	
	
	public void insertOnePerson() {
		StatusResult insertResult = MongoDBHandler.getInstance().insertSync(person);
		Assertions.assertTrue(insertResult.isSuccess());
	}
	
	
	public void insertManyPersons() {
		StatusResult insertResult = MongoDBHandler.getInstance().insertSync(personList);
		Assertions.assertTrue(insertResult.isSuccess());
	}
	
	
	public List<Person> queryAllPersons() {
		StatusResult findResult = MongoDBHandler.getInstance().findSync(Person.class);
		Assertions.assertTrue(findResult.isSuccess());
		List<Person> personList = findResult.getResult();
		return personList;
	}
	
	
	public Person onePerson() {
		return person;
	}
		
		
	public List<Person> personList() {
		return personList;
	}


	public Person getPerson() {
		return person;
	}


	public List<Person> getPersonList() {
		return personList;
	}
}
