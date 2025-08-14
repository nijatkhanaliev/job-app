package com.job.security.provider;

import com.job.dao.entity.User;
import com.job.dao.repository.UserRepository;
import com.job.exception.custom.InvalidInputException;
import com.job.exception.custom.NotFoundException;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.job.exception.constant.ErrorCode.INVALID_INPUT;
import static com.job.exception.constant.ErrorCode.NOT_FOUND;
import static com.job.exception.constant.ErrorMessage.INVALID_INPUT_MESSAGE;
import static com.job.exception.constant.ErrorMessage.NOT_FOUND_MESSAGE;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        String.format(NOT_FOUND_MESSAGE, "User"), NOT_FOUND)
                );

        if (!user.getIsActive()) {
            throw new DisabledException("User is not active");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidInputException(
                    String.format(INVALID_INPUT_MESSAGE, "email or password"),
                    INVALID_INPUT);
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                user.getUserRole().toString()
        );

        return new UsernamePasswordAuthenticationToken(
                user,
                password,
                Collections.of(authority)
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}
