package com.job.security;

import com.job.dao.entity.User;
import com.job.dao.repository.UserRepository;
import com.job.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import static com.job.exception.constant.ErrorCode.NOT_FOUND;
import static com.job.exception.constant.ErrorMessage.NOT_FOUND_MESSAGE;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
       User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new NotFoundException(
                        String.format(NOT_FOUND_MESSAGE, "User"), NOT_FOUND)
                );

        return new UserDetailsImpl(user);
    }
}
