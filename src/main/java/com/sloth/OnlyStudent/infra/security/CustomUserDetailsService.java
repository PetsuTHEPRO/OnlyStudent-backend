package com.sloth.OnlyStudent.infra.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.repository.UserRepository;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository repository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.repository.findByLogin(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        
        grantedAuthorities.add(new SimpleGrantedAuthority(user.getRoles().getRole()));
        
        return new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPassword(), grantedAuthorities);
    }
}