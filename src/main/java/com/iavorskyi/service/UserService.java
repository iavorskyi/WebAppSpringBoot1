package com.iavorskyi.service;

import com.iavorskyi.domain.Role;
import com.iavorskyi.domain.User;
import com.iavorskyi.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepo userRepo;
    @Autowired
    MailSender mailSender;
    @Autowired
    PasswordEncoder passwordEncoder;

    public boolean addUser(User user){
        User userFromDb = userRepo.findByUsername(user.getUsername());
        if(userFromDb != null){
            return false;
        }
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepo.save(user);
        sendMessage(user);

        return true;

    }

    private void sendMessage(User user) {
        if(!StringUtils.isEmpty(user.getMail())){
            String msgText = String.format("Hello, %s! \n" +
                    "this link to activate your email on WebAppSpringBoot1 \n" +
                    "http://localhost:8080/activate/%s", user.getUsername(), user.getActivationCode());
            mailSender.send(user.getMail(), "Activation email", msgText);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username);
    }

    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);
        if(user==null) return false;

        user.setActivationCode(null);

        userRepo.save(user);
    return true;
    }

    public Iterable<User> findAll() {
        return userRepo.findAll();
    }

    public void saveUser(String username, Map<String, String> form, User user) {
        user.setUsername(username);

        Set<String> roles = Arrays.stream(Role.values()).map(Role::name).collect(Collectors.toSet());
        user.getRoles().clear();
        for (String key: form.keySet()) {
            if(roles.contains(key)){
                user.getRoles().add(Role.valueOf(key));
            }
        }
        userRepo.save(user);
    }

    public User getOne(Long userId) {
        return userRepo.getOne(userId);
    }

    public void saveProfile(String password, String mail, User user) {
        String userMail = "";
        if(user.getMail()!=null) {
            userMail = user.getMail();
        }

            boolean isEmailChanged = (mail != null && !userMail.equals(mail)) || (userMail != null && userMail.equals(mail));
            if (isEmailChanged) {
                user.setMail(mail);
                if (!StringUtils.isEmpty(mail)) {
                    user.setActivationCode(UUID.randomUUID().toString());

                }
            }

            if (!StringUtils.isEmpty(password)) {
                user.setPassword(password);
            }
            userRepo.save(user);
            if (isEmailChanged) sendMessage(user);
    }
}
