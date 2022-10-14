
package com.achawan.employee.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.achawan.employee.Employee;

public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private List<GrantedAuthority> authorities;

	public UserDetailsImpl(String username, String password, List<GrantedAuthority> authorities) {
		this.username = username;
		this.password = password;
		this.authorities = authorities; /* Collections.singletonList(new SimpleGrantedAuthority(employee.getRole())); */
	}

	public static UserDetailsImpl build(Employee employee) {
		List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(employee.getRole()));
		return new UserDetailsImpl(employee.getUserName(), employee.getPassword(), authorities);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;
		UserDetailsImpl user = (UserDetailsImpl) obj;
		return Objects.equals(username, user.getUsername());
	}

}
