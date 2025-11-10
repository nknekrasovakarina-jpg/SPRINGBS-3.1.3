package ru.kata.spring.boot_security.demo.CONTROLLER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.MODEL.User;
import ru.kata.spring.boot_security.demo.SERVICE.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String adminPage(Model model,
                            @RequestParam(value = "success", required = false) String success,
                            @RequestParam(value = "error", required = false) String error) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("allRoles", userService.getAllRoles());
        model.addAttribute("newUser", new User()); // Для формы создания

        if (success != null) {
            model.addAttribute("successMessage", success);
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "admin";
    }

    @GetMapping("/user/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user-details";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", userService.getAllRoles());
        return "user-create.html";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "User with username '" + user.getUsername() + "' already exists");
                return "redirect:/admin/create";
            }

            if (userService.existsByEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "User with email '" + user.getEmail() + "' already exists");
                return "redirect:/admin/create";
            }

            userService.saveUser(user);
            redirectAttributes.addAttribute("success", "User created successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               @RequestParam(value = "success", required = false) String success,
                               @RequestParam(value = "error", required = false) String error) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", userService.getAllRoles());

        if (success != null) {
            model.addAttribute("successMessage", success);
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "user-update";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
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

            userService.updateUser(id, user);
            redirectAttributes.addAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/edit/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/quick-create")
    public String quickCreateUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                redirectAttributes.addAttribute("error",
                        "User with username '" + user.getUsername() + "' already exists");
                return "redirect:/admin";
            }

            if (userService.existsByEmail(user.getEmail())) {
                redirectAttributes.addAttribute("error",
                        "User with email '" + user.getEmail() + "' already exists");
                return "redirect:/admin";
            }

            userService.saveUser(user);
            redirectAttributes.addAttribute("success", "User created successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}