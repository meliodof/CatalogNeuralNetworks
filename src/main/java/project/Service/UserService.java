package project.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.DTO.ChangeEmailRequest;
import project.DTO.ChangePasswordRequest;
import project.DTO.RegisterRequest;
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
    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("Пользователь с таким именем уже существует");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    public void changeEmail(String username, ChangeEmailRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (userRepository.findByEmail(request.getNewEmail()).isPresent()) {
            throw new IllegalStateException("Этот email уже используется");
        }
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalStateException("Неверный текущий пароль");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}