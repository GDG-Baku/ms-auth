package az.gdg.authservice.security.service;


import az.gdg.authservice.dao.UserRepository;
import az.gdg.authservice.entity.UserEntity;
import az.gdg.authservice.exception.WrongDataException;
import az.gdg.authservice.security.bean.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity user = repository
                .findByEmail(username)
                .orElseThrow(() -> new WrongDataException("No such email is registered"));
        return buildSecurityUser(user);
    }

    private CustomUserDetails buildSecurityUser(UserEntity user) {
        return CustomUserDetails.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(user.getRole()))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true).build();
    }
}