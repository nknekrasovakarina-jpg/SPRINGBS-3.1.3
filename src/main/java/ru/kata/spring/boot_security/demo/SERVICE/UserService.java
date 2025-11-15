package ru.kata.spring.boot_security.demo.SERVICE;


import ru.kata.spring.boot_security.demo.MODEL.Role;
import ru.kata.spring.boot_security.demo.MODEL.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    List<User> getAllUsers();
    User getUserById(Long id);
    Optional<User> findByEmail(String email);
    User saveUser(User user);
    User updateUser(Long id, User updatedUser);
    void deleteUser(Long id);
    List<Role> getAllRoles();
    Optional<Role> findRoleByName(String name);
    Role createRole(String name);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByUsername(String username);
}