package com.iavorskyi.controllers;

import com.iavorskyi.domain.Message;
import com.iavorskyi.domain.User;
import com.iavorskyi.repos.MessageRepo;
import com.iavorskyi.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.stream.Collectors;

@Controller
public class FirstController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MessageRepo messageRepo;
    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping
    public String greeting( Model model, Principal principal) {
        if(principal!=null) {
            String name = principal.getName();//get logged in username
            model.addAttribute("username", name);
            User user = userRepo.findByUsername(name);
            model.addAttribute("user", user);
        }
        return "greeting";
    }

    @GetMapping ("/main")
    public String showList(@RequestParam(required = false) String filter, Model model, Principal principal) {
        if(principal!=null) {
            String name = principal.getName();//get logged in username
            model.addAttribute("username", name);
            User user = userRepo.findByUsername(name);
            model.addAttribute("user", user);
        }
        Iterable<Message> messages = messageRepo.findAll();
        if(filter != null && !filter.isEmpty()){
            messages = messageRepo.findByTag(filter);
        } else {
            messages = messageRepo.findAll();
        }
        model.addAttribute("messages", messages);
        model.addAttribute("filter", filter);
        return "main";
    }

    @PostMapping ("/main")
    public String add(
            @AuthenticationPrincipal User user,
            @Valid Message message,
            BindingResult bindingResult,
            Model model,
            @RequestParam("file") MultipartFile file) throws IOException {
        message.setAuthor(user);
        if(bindingResult.hasErrors()){
            Map<String, String> errorsMap = ControllerUtil.getErrors(bindingResult);

            model.mergeAttributes(errorsMap);

            model.addAttribute("message", message);
            
        }
        else {
            if (file != null && !file.getOriginalFilename().isEmpty()) {
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists())
                    uploadDir.mkdir();
                String uuidFile = UUID.randomUUID().toString();
                String resultFileName = uuidFile + "." + file.getOriginalFilename();
                file.transferTo(new File("/C:/" + uploadPath + "/" + resultFileName));
                message.setFilename(resultFileName);
            }
            model.addAttribute("message", null);
            messageRepo.save(message);
        }
            Iterable<Message> messages = messageRepo.findAll();
            model.addAttribute("messages", messages);

        return "main";

    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id){
       messageRepo.deleteById(id);
        return "redirect:/main";
    }



}