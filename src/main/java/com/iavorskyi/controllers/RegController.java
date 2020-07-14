package com.iavorskyi.controllers;

import com.iavorskyi.domain.User;
import com.iavorskyi.repos.UserRepo;
import com.iavorskyi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class RegController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    public String registration(Model model, Principal principal) {
        if(principal!=null) {
            String name = principal.getName();//get logged in username
            model.addAttribute("username", name);
            User user = userRepo.findByUsername(name);
            model.addAttribute("user", user);
        }
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute("user") User user, Model model) {
        if (!userService.addUser(user)) {
            model.addAttribute("message", "User exists!");
        return "registration";
        }
        return "redirect:/login";
    }

    @GetMapping("/activate/{code}")
    public String activate( Model model, @PathVariable String code){
        String message;
        boolean isActivated = userService.activateUser(code);

        if(isActivated){
            message = "User is activated";
        }
        else message = "Activation error";

        model.addAttribute("message", message);
        return "login";
    }
}
