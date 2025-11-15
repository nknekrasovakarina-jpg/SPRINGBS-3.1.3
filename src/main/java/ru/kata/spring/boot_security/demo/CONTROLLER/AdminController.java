package ru.kata.spring.boot_security.demo.CONTROLLER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.MODEL.Role;
import ru.kata.spring.boot_security.demo.MODEL.User;
import ru.kata.spring.boot_security.demo.SERVICE.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", userService.getAllRoles());  // <-- вот ЭТО
        model.addAttribute("newUser", new User());
        return "admin";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("roles", userService.getAllRoles());
        model.addAttribute("user", new User());
        return "user-create";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute User user,
                             @RequestParam(required = false, value = "roleIds") List<Long> roleIds,
                             RedirectAttributes redirectAttributes) {

        try {
            Set<Role> roles = roleIds == null
                    ? Set.of(userService.findRoleByName("ROLE_USER").orElseThrow())
                    : roleIds.stream()
                    .map(id -> userService.getAllRoles().stream()
                            .filter(r -> r.getId().equals(id))
                            .findFirst().orElseThrow())
                    .collect(Collectors.toSet());

            user.setRoles(roles);

            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String editUserPage(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", userService.getAllRoles());
        return "user-update";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
                             @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                             RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userService.getUserById(id);

            if (!existingUser.getUsername().equals(user.getUsername()) &&
                    userService.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Username '" + user.getUsername() + "' is already taken");
                return "redirect:/admin/edit/" + id;
            }

            if (!existingUser.getEmail().equals(user.getEmail()) &&
                    userService.existsByEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Email '" + user.getEmail() + "' is already taken");
                return "redirect:/admin/edit/" + id;
            }

            // роли
            Set<Role> roles = (roleIds != null) ? roleIds.stream()
                    .map(rid -> userService.getAllRoles().stream()
                            .filter(r -> r.getId().equals(rid))
                            .findFirst()
                            .orElseThrow())
                    .collect(Collectors.toSet())
                    : existingUser.getRoles();

            user.setRoles(roles);

            userService.updateUser(id, user);

            redirectAttributes.addAttribute("success", "User updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error updating user: " + e.getMessage());
        }

        return "redirect:/admin/edit/" + id;
    }

    @GetMapping("/delete/{id}")
    public String showDeletePage(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "user-delete";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        return "redirect:/admin";
    }
}