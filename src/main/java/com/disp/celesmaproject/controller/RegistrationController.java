package com.disp.celesmaproject.controller;

import com.disp.celesmaproject.model.CustomUserDetailsService;
import com.disp.celesmaproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;


import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/user")
    public String createUser(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password,
                             RedirectAttributes redirectAttributes) {

        // Проверка на существование пользователя с таким именем
        if (userDetailsService.getUserByUsername(username) != null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь с таким именем уже существует!");
            return "redirect:/register";
        }

        // Проверка на существование пользователя с таким email
        if (userDetailsService.getUserByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь с таким email уже зарегистрирован!");
            return "redirect:/register";
        }

        // Создание нового пользователя
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");

        // Сохранение пользователя
        userDetailsService.saveUser(user);

        // Сообщение об успешной регистрации
        redirectAttributes.addFlashAttribute("success", "Регистрация прошла успешно! Теперь вы можете войти.");
        return "redirect:/login";
    }
}
