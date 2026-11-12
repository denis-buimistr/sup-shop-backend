package com.supshop.sup_shop_backend.security;

import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // в поле входа можно ввести и почту, и логин. в логине @ быть не может, поэтому по ней и различаю.
    // этот же метод зовёт JwtAuthFilter, но в токене всегда лежит логин, так что там идём по username
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login)
        throws UsernameNotFoundException {

        User user = findUser(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }

    private Optional<User> findUser(String login) {
        return login.contains("@")
                ? userRepository.findByEmailIgnoreCase(login.trim())
                : userRepository.findByUsername(login);
    }

}
