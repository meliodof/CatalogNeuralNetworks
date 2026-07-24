package project.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Entity.User;
import project.Repository.RepUser;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final RepUser userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(RepUser userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Пользователь с таким именем уже существует");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        return userRepository.save(user);
    }

    @Transactional
    public void changeEmail(String username, String newEmail) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalStateException("Этот email уже используется");
        }
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalStateException("Неверный текущий пароль");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}