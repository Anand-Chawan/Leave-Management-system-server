package com.achawan.employee.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.achawan.employee.Employee;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {

	@Query("{$or: [{'firstName' : {'$regex': '?0', '$options':'i'}}, {'lastName' : {'$regex': '?0', '$options':'i'}},"
			+ "{'userName' : {'$regex': '?0', '$options':'i'}}," + "{'email' : {'$regex': '?0', '$options':'i'}}]}")
	public List<Employee> findAll(String filterText);
		
	public Employee findByUserName(String userName);

	public Employee findByEmail(String email);
}
