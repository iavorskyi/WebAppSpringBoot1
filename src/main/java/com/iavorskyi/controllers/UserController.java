package com.iavorskyi.controllers;

import com.iavorskyi.domain.Role;
import com.iavorskyi.domain.User;
import com.iavorskyi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping ("/user")

public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String userList(Model model, @AuthenticationPrincipal User user){
        String name = user.getUsername();//get logged in username
        model.addAttribute("username", name);
        model.addAttribute("user", user);

        Iterable<User> userList = userService.findAll();
        model.addAttribute("userList", userList);
        return "userList";
    }
    @GetMapping ("{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String userEdit(@PathVariable Long userId, Model model, @AuthenticationPrincipal User currentUser) {
        User user = userService.getOne(userId);
        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("roles", Role.values());
        model.addAttribute("username", user.getUsername());
        return "editUser";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String saveUser(
            @RequestParam String username,
            @RequestParam Map<String, String> form,
            @RequestParam ("userId") Long userId
    ){
        User user = userService.getOne(userId);
    userService.saveUser(username, form, user);
        return "redirect:/user";
    }

    @GetMapping("/profile")
        public String profile(@AuthenticationPrincipal User user, Model model){

        model.addAttribute("username", user.getUsername());
        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String password,
                                @RequestParam String mail,
                                @AuthenticationPrincipal User user){

        userService.saveProfile(password, mail, user);
        return "redirect:/user/profile";
    }
}




