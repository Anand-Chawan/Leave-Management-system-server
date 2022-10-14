package com.achawan.employee.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.achawan.employee.ApplyLeaves;
import com.achawan.employee.ApproveOrDiscardLeave;
import com.achawan.employee.Employee;
import com.achawan.employee.Leaves;
import com.achawan.employee.Login;
import com.achawan.employee.service.EmployeeService;

@RestController
@RequestMapping("api")
public class EmployeeController {

	@Autowired
	private EmployeeService empService;

//	######################## LOGIN #####################

	// Authenticate user & generate cookie
	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody Login login) {
		return empService.authenticateUser(login);
	}
//	###################### END LOGIN ####################

// ######################## ROLE_ADMIN ##############################
	@PostMapping("/create")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Employee createEmployee(@Valid @RequestBody Employee employee) {
		return empService.save(employee);
	}

	@GetMapping("/getAll")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public List<Employee> getAll() {
		return empService.findAll();
	}

	@GetMapping("/getAll/{filter}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public List<Employee> getAll(@PathVariable String filter) {
		return empService.findAll(filter);
	}

	@GetMapping("/getbyid/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Employee getById(@PathVariable String id) {
		return empService.findById(id);
	}

	@GetMapping("/getbyusername/{username}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Employee getByUsername(@PathVariable String username) {
		return empService.findByUserName(username);
	}

	@GetMapping("/getbyemail/{email}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Employee getByEmail(@PathVariable String email) {
		return empService.findByEmail(email);
	}

	@PostMapping("/update")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Employee update(@RequestBody Employee employee) {
		return empService.update(employee);
	}

	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public String delete(@PathVariable String id) {
		return empService.deleteById(id);
	}

	@GetMapping("/allpendingleaves")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public HashMap<String, HashMap<LocalDate, String>> getAllPendingLeaves() {
		return empService.getAllPendingLeaves();
	}

	@PostMapping("/approveleave")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public String approveLeave(@RequestBody ApproveOrDiscardLeave toApprove) {
		return empService.approveLeave(toApprove);
	}

	@PostMapping("/denyleave")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public String denyLeave(@RequestBody ApproveOrDiscardLeave toDeny) {
		return empService.rejectOrDiscard(toDeny, true);
	}

// ############################ END OF ROLE_ADMIN #########################

// ############################### ROLE_USER #########################

	@PostMapping("/applyleave")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public String applyLeaves(@RequestBody ApplyLeaves applyleaves, Principal principal) {
		Employee employee = getLoggedInEmployee(principal);
		return empService.applyLeaves(applyleaves, employee);
	}

	// discard leave (before Admin approves)
	@PostMapping("/discardleave")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public String discardleave(@RequestBody ApproveOrDiscardLeave toDiscard, Principal principal) {
		Employee employee = getLoggedInEmployee(principal);
		toDiscard.setUsername(employee.getUserName());
		return empService.discardLeave(toDiscard);
	}

	// get pending leaves of loggedIn user
	@GetMapping("/pendingleaves")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public HashMap<LocalDate, String> getPendingLeaves(Principal principal) {
		// returns key->date, value->leaveType
		return empService.getPendingLeaves(getLoggedInEmployee(principal));
	}

	// get approved leaves of loggedIn user
	@GetMapping("approvedleaves")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public HashMap<LocalDate, String> getApprovedLeaves(Principal principal) {
		// returns key->date, value->leaveType
		return empService.getApprovedLeaves(getLoggedInEmployee(principal));
	}

	@GetMapping("/availableleaves")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public Leaves getAvailableLeaves(Principal principal) {
		return getLoggedInEmployee(principal).getLeaves();
	}

	@GetMapping("/getemployeename")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public String getIdWithName(Principal principal) {
		Employee employee = getLoggedInEmployee(principal);
		return employee.getFirstName() + " " + employee.getLastName();
	}

// ############################### End of ROLE_USER #########################

	// get logged In Employee
	private Employee getLoggedInEmployee(Principal principal) {
		return empService.findByUserName(principal.getName());
	}

}
