package com.achawan.employee.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.achawan.common.Constant;
import com.achawan.employee.ApplyLeaves;
import com.achawan.employee.ApproveOrDiscardLeave;
import com.achawan.employee.Employee;
import com.achawan.employee.Leaves;
import com.achawan.employee.Login;
import com.achawan.employee.repository.EmployeeRepository;
import com.achawan.employee.security.JwtUtils;
import com.achawan.employee.security.UserDetailsImpl;
import com.achawan.rabbitMQ.MessagingConfig;

@Service
public class EmployeeService {

	private Employee employee;
	
	@Autowired
	private EmployeeRepository empRepo;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	private RabbitTemplate template;

	public String sendMessage(ApplyLeaves leaves) {
		System.out.println("PUBLISHER: " + leaves);
		template.convertAndSend(MessagingConfig.EXCHANGE, MessagingConfig.ROUTING_KEY, leaves);
		return "Success";
	}

//	<<<<<<<<<<<<<<<<<<<<<<<<< Authenticate User >>>>>>>>>>>>>>>>>>>>>>>>>
	public ResponseEntity<?> authenticateUser(@Valid Login login) {

		final Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

//		List<String> role = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
//				.collect(Collectors.toList());

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(jwtCookie.toString());

	}
//	<<<<<<<<<<<<<<<<<<<<<<<<<<< End of Authenticate User >>>>>>>>>>>>>>>>>>>>>>>>>>>

// #################### create new employee ###########################
	public Employee save(Employee employee) {

		if (employee.getFirstName() == null || employee.getLastName() == null) {
			throw new RuntimeException("FirstName or LastName cannot be empty");
		} else if (employee.getId()!=null && employee.getId().equals("")) {
			employee.setId(null);
		} else if (employee.getId() == null && employee.getUserName() != null
				&& findByUserName(employee.getUserName()) != null) {
			throw new RuntimeException(
					"username: " + employee.getUserName() + " alreday exists, please change username");
		} else if (employee.getId() == null && employee.getEmail() != null
				&& findByEmail(employee.getEmail()) != null) {
			throw new RuntimeException("Email: " + employee.getEmail() + " alreday exists, please change Email");
		}

		// do some addOn to employee
		if (employee.getLeaves() == null) {
			// set default leaves
			employee.setLeaves(new Leaves());
		}
		if (employee.getUserName() == null || employee.getUserName().equals("")) {
			// set default username
			employee.setUserName(
					(employee.getFirstName().toLowerCase().charAt(0) + employee.getLastName().toLowerCase()).substring(
							0, employee.getLastName().length() > 7 ? 8 : employee.getLastName().length() + 1));
		}
		if (employee.getEmail() == null || employee.getEmail().equals("")) {
			// set default email
			employee.setEmail(employee.getFirstName() + "." + employee.getLastName() + "@rbbn.com");
		}

		employee.setRole(Constant.DEFAULT_USER);
		String encryptedPwd = encoder.encode(employee.getUserName() + "@123");
//		System.out.println(employee.getUserName() + "@123");
		employee.setPassword(encryptedPwd);

		return empRepo.save(employee);
	}
// #################### End of create new employee ###########################

// #################### get all employees ####################
	public List<Employee> findAll() {
		return empRepo.findAll();
	}

	public List<Employee> findAll(String filterText) {
		if (filterText == null || filterText.isEmpty())
			return empRepo.findAll();
		else
			return empRepo.findAll(filterText);
	}
// #################### End of get all employees ####################

// #################### get employee by {field} ####################
	public Employee findById(String id) {
		return empRepo.findById(id).get();
	}

	// get employee by username
	public Employee findByUserName(String userName) {
		return empRepo.findByUserName(userName);
	}

	// get employee by email
	public Employee findByEmail(String email) {
		return empRepo.findByEmail(email);
	}

	// get employee count
	public long getEmployeeCount() {
		return empRepo.count();
	}
// #################### End of employee by {field} ####################

//	######################### update and delete employee ##################
	public Employee update(Employee employee) {
		return this.save(employee);
	}

	// delete
	public String deleteById(String id) {
		if (findById(id) == null) {
			return "Invalid id, Please provide valid id";
		}
		empRepo.deleteById(id);
		return "Employee with id: " + id + " , deleted Successfully";
	}
//	######################### End of update and delete employee ##################

// ####################### apply leaves #######################
	public String applyLeaves(ApplyLeaves leaves, Employee employee) {
		if (validateAndUpdateLeaves(leaves, employee)) {
			this.employee = employee;
			//rabbitMQ publisher
			this.sendMessage(leaves);
		} else {
			return "Some Error occoured";
		}
		return "Applied Successfully [Pending leaves: " + employee.getLeaves().getPendingLeaves() + " ]";
	}
	
	@RabbitListener(queues = MessagingConfig.QUEUE)
	public void receiveMessage(ApplyLeaves leaves) {
		System.out.println("CONSUMER: " + leaves);
		this.update(employee);
	}

	// --------------------Validate Leaves---------------------------//
	private boolean validateAndUpdateLeaves(ApplyLeaves leaves, Employee employee) {
		if (leaves.getFromDate() == null || leaves.getTillDate() == null || leaves.getLeaveType() == null)
			throw new RuntimeException("Invalid input, one or more fields is/are empty");

		// check fromDate > tillDate ---> throw exception
		if (leaves.getFromDate().isAfter(leaves.getTillDate()))
			throw new RuntimeException("fromDate cannot be after tillDate [Note: fromDate <= tillDate]");

		if (!(leaves.getLeaveType().equals(Constant.OH) || leaves.getLeaveType().equals(Constant.SL)
				|| leaves.getLeaveType().equals(Constant.PTO))) {
			throw new RuntimeException("Invalid leaveType " + leaves.getLeaveType());
		}

		// get all dates
		List<LocalDate> dates = leaves.getFromDate().datesUntil(leaves.getTillDate().plusDays(1)).filter(e -> {
			return e.getDayOfWeek() != DayOfWeek.SATURDAY && e.getDayOfWeek() != DayOfWeek.SUNDAY
					&& !(employee.getLeaves().getPendingLeaves() != null
							&& employee.getLeaves().getPendingLeaves().containsKey(e))
					&& !(employee.getLeaves().getApprovedLeaves() != null
							&& employee.getLeaves().getApprovedLeaves().containsKey(e));
		}).toList();

		int leaveSize = dates.size();
		if(leaveSize<1) {
			System.err.println("---------------Invalid/Already applied date-------------------");
			return false;
		}
		// check appliedLeaves < current leaves left
		if ((leaves.getLeaveType().equals(Constant.PTO) && employee.getLeaves().getPTO() < leaveSize)
				|| (leaves.getLeaveType().equals(Constant.OH) && employee.getLeaves().getOH() < leaveSize)
				|| (leaves.getLeaveType().equals(Constant.SL) && employee.getLeaves().getSL() < leaveSize)) {
			throw new RuntimeException("Applied leaves is/are more than balanced leaves");
		}

		dates.forEach(date -> {
			employee.getLeaves().setPendingLeaves(date, leaves.getLeaveType());

			switch (leaves.getLeaveType()) {
			case Constant.OH:
				employee.getLeaves().setOH(employee.getLeaves().getOH() - 1);
				break;

			case Constant.PTO:
				employee.getLeaves().setPTO(employee.getLeaves().getPTO() - 1);
				break;

			case Constant.SL:
				employee.getLeaves().setSL(employee.getLeaves().getSL() - 1);
				break;

			default:
				break;
			}
		});

		return true;
	}
// ####################### End of apply leaves #######################

	// get pending leaves by loggedIn user
	public HashMap<LocalDate, String> getPendingLeaves(Employee employee) {
		// returns key->date, value->leaveType
		return employee.getLeaves().getPendingLeaves();
	}

	// get approved leaves by loggedIn user
	public HashMap<LocalDate, String> getApprovedLeaves(Employee employee) {
		// returns key->date, value->leaveType
		return employee.getLeaves().getApprovedLeaves();
	}

	// get all pending leaves
	public HashMap<String, HashMap<LocalDate, String>> getAllPendingLeaves() {
		// returns key->userName, value->{date, leaveType}
		HashMap<String, HashMap<LocalDate, String>> res = new HashMap<>();
		findAll().stream().filter(e -> {
			return e.getLeaves() != null && e.getLeaves().getPendingLeaves() != null
					&& !(e.getLeaves().getPendingLeaves().isEmpty());
		}).forEach(e -> {
			res.put(e.getUserName(), e.getLeaves().getPendingLeaves());
		});
		return res;
	}

	// approve leave
	public String approveLeave(ApproveOrDiscardLeave toApprove) {
		Employee employee = findByUserName(toApprove.getUsername());
		if (employee == null) {
			return "user not found, username: " + toApprove.getUsername();
		}
		if (!employee.getLeaves().getPendingLeaves().containsKey(toApprove.getDate())) {
			return "No pending leaves on date: " + toApprove.getDate();
		}
		// set leave to approved
		employee.getLeaves().setApprovedLeaves(toApprove.getDate(),
				employee.getLeaves().getPendingLeaves().get(toApprove.getDate()));

		// remove leave from pending leave (coz the leave is been approved)
		employee.getLeaves().getPendingLeaves().remove(toApprove.getDate());

		this.update(employee);

		return "Approved";
	}

	// deny leave
	public String rejectOrDiscard(ApproveOrDiscardLeave toDeny, boolean isAdmin) {
		Employee employee = findByUserName(toDeny.getUsername());
		if (employee == null) {
			return "user not found, username: " + toDeny.getUsername();
		}
		if (!employee.getLeaves().getPendingLeaves().containsKey(toDeny.getDate())) {
			return "No pending leaves on date: " + toDeny.getDate();
		}

		String leaveType = employee.getLeaves().getPendingLeaves().get(toDeny.getDate());

		switch (leaveType) {
		case Constant.OH:
			employee.getLeaves().setOH(employee.getLeaves().getOH() + 1);
			break;
		case Constant.PTO:
			employee.getLeaves().setPTO(employee.getLeaves().getPTO() + 1);
			break;
		case Constant.SL:
			employee.getLeaves().setSL(employee.getLeaves().getSL() + 1);
			break;

		}

		employee.getLeaves().getPendingLeaves().remove(toDeny.getDate());
		if(isAdmin) {
			employee.getLeaves().setRejectedLeaves(toDeny.getDate(), leaveType);
		}
		this.update(employee);

		return isAdmin? "Rejected Successfully" : "Discarded Successfully";
	}

	public String discardLeave(ApproveOrDiscardLeave toDiscard) {
		return this.rejectOrDiscard(toDiscard, false);
	}

}
