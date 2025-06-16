package com.disp.celesmaproject.service;

import com.disp.celesmaproject.model.CustomUserDetails;
import com.disp.celesmaproject.model.User;
import com.disp.celesmaproject.model.UserProfileDTO;
import com.disp.celesmaproject.util.AuthenticationFacade;
import com.disp.celesmaproject.repo.UserRepository;
import com.disp.celesmaproject.util.YandexDiskService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private YandexDiskService yandexDiskService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            var userObj = user.get();
            return new CustomUserDetails(
                    userObj.getUsername(),
                    userObj.getPassword(),
                    userObj.getEmail(),
                    getAuthorities(userObj)
            );
        } else {
            throw new UsernameNotFoundException(username);
        }
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Преобразуем роли в GrantedAuthority
        return Arrays.stream(user.getRole().split(","))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Transactional
    public User updateUserProfile(UserProfileDTO userProfileDto) {
        String username = authenticationFacade.getAuthenticatedUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!user.getUsername().equals(userProfileDto.getUsername())) {
            throw new SecurityException("Нельзя изменить чужой профиль");
        }

        user.setFirstName(StringUtils.hasText(userProfileDto.getFirstName()) ?
                userProfileDto.getFirstName() : null);
        user.setLastName(StringUtils.hasText(userProfileDto.getLastName()) ?
                userProfileDto.getLastName() : null);

        if (userProfileDto.getAvatarUrl() != null) {
            user.setAvatarUrl(userProfileDto.getAvatarUrl());
        }

        return user; // Сохранение произойдет автоматически благодаря @Transactional
    }

    public User getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    public User getUserByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    public User getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

/*
    private String[] getRoles(User user) {
        if (user.getRole() == null) {
            return new String[]{"USER"};
        }
        return user.getRole().split(",");
*/

}
