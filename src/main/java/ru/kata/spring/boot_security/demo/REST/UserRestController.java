package ru.kata.spring.boot_security.demo.REST;

import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.MODEL.User;
import ru.kata.spring.boot_security.demo.SERVICE.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    // Получение данных текущего пользователя
    @GetMapping("/me")
    public User getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName());
    }

    // Получение всех пользователей (для админа)
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Получение конкретного пользователя по id
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

}