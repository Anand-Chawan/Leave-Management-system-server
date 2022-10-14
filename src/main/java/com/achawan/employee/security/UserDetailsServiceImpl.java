
package com.achawan.employee.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.achawan.employee.Employee;
import com.achawan.employee.repository.EmployeeRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Employee employee = employeeRepository.findByUserName(username);

		if (employee == null)
			throw new UsernameNotFoundException(username + " doesn't exists");
		return UserDetailsImpl.build(employee);
	}

}
