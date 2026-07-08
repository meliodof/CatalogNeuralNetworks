package project.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Entity.User;
import project.Repository.RepUser;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final RepUser repUser;

    public UserService(RepUser repUser){
        this.repUser=repUser;
    }

    public Optional<User> getById(Long id) {
        return repUser.findById(id);
    }

    public Optional<User> getByUsername(String username) {
        return repUser.findByUsername(username);
    }

    public Optional<User> getByEmail(String email) {
        return repUser.findByEmail(email);
    }

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (repUser.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Пользователь с таким именем уже существует");
        }
        if (repUser.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hashPassword(rawPassword));  // временный хеш, позже Spring Security

        return repUser.save(user);
    }

    private String hashPassword(String rawPassword) {
        // TODO: заменить на BCryptPasswordEncoder из Spring Security
        return "{noop}" + rawPassword;  // временно, для разработки
    }
}
