package ru.kata.spring.boot_security.demo.REST;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.MODEL.Role;
import ru.kata.spring.boot_security.demo.MODEL.User;
import ru.kata.spring.boot_security.demo.SERVICE.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AdminRestController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Получить всех пользователей */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /** Получить пользователя по id */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new UserDTO(user));
    }

    /** Создать нового пользователя */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            // Проверка уникальности username/email
            if (userService.existsByUsername(userDTO.username)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Пользователь с таким username уже существует");
            }
            if (userService.existsByEmail(userDTO.email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Пользователь с таким email уже существует");
            }

            User user = userDTO.toUser();
            if (userDTO.password != null && !userDTO.password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDTO.password));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Пароль обязателен для создания пользователя");
            }

            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(new UserDTO(savedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании пользователя: " + e.getMessage());
        }
    }

    /** Обновить пользователя */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            User existing = userService.getUserById(id);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Проверка уникальности username/email при обновлении
            if (!existing.getUsername().equals(userDTO.username) && userService.existsByUsername(userDTO.username)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username уже занят");
            }
            if (!existing.getEmail().equals(userDTO.email) && userService.existsByEmail(userDTO.email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email уже занят");
            }

            User userToUpdate = userDTO.toUser();
            userToUpdate.setId(id);

            // Если пароль не пустой, обновляем его
            if (userDTO.password != null && !userDTO.password.isEmpty()) {
                userToUpdate.setPassword(passwordEncoder.encode(userDTO.password));
            } else {
                userToUpdate.setPassword(existing.getPassword());
            }

            User updatedUser = userService.updateUser(id, userToUpdate);
            return ResponseEntity.ok(new UserDTO(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при обновлении пользователя: " + e.getMessage());
        }
    }

    /** Удалить пользователя */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }
            userService.deleteUser(id);
            return ResponseEntity.ok("Пользователь удалён");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при удалении пользователя: " + e.getMessage());
        }
    }

    /** DTO для безопасности и удобной работы с JSON */
    public static class UserDTO {
        public Long id;
        public String username;
        public String email;
        public List<String> roles;
        public String password;

        public UserDTO() {}

        public UserDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
        }

        public User toUser() {
            User user = new User();
            user.setId(this.id);
            user.setUsername(this.username);
            user.setEmail(this.email);
            user.setPassword(this.password);
            if (this.roles != null) {
                Set<Role> roleSet = this.roles.stream()
                        .map(name -> {
                            Role r = new Role();
                            r.setName(name);
                            return r;
                        }).collect(Collectors.toSet());
                user.setRoles(roleSet);
            }
            return user;
        }
    }
}