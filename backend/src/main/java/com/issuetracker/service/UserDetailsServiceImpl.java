package com.issuetracker.service;

import com.issuetracker.entity.User;
import com.issuetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Implementation of Spring Security UserDetailsService.
 * Loads user details for authentication and authorization.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = Logger.getLogger(UserDetailsServiceImpl.class.getName());
    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user by username (email in our case).
     *
     * @param username the username (email) identifying the user
     * @return UserDetails for the user
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.fine("Loading user by username: " + username);
        
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.warning("User not found with email: " + username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

        logger.fine("User found: " + user.getName());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(new ArrayList<>()) // No roles for now, all users have same permissions
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
                
        logger.fine("UserDetails created successfully");
        return userDetails;
    }
}