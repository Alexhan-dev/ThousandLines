package wtf.alexhan.thousandlines.service;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import wtf.alexhan.thousandlines.model.User;
import wtf.alexhan.thousandlines.model.UserRole;
import wtf.alexhan.thousandlines.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void createCreatorUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new RuntimeException("用户名或邮箱已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(UserRole.CREATOR);

        userRepository.save(user);
    }
}
