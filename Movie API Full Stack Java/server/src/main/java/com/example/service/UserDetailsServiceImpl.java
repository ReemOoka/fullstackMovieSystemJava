package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.model.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional; 
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username: {}", username);

        // userRepository.findByUsername now returns Optional<User>
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) { // Check if the Optional is empty
            logger.warn("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        User user = userOptional.get(); // Get the User object from the Optional

        logger.info("User found: {}. Hashed password from DB: [PROTECTED]. Roles: {}",
                    user.getUsername(),
                    user.getRoles().stream().map(Role::name).collect(Collectors.joining(", ")));


        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) { // Added check for empty roles
            logger.debug("User has no roles assigned or roles set is null.");
            return Set.of();
        }
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> {
                    logger.debug("Mapping role: {} to authority: ROLE_{}", role.name(), role.name());
                    return new SimpleGrantedAuthority("ROLE_" + role.name());
                })
                .collect(Collectors.toSet());
        logger.debug("Authorities mapped for user: {}", authorities);
        return authorities;
    }
}