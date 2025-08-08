package com.disp.celesmaproject.model;

import com.disp.celesmaproject.repo.UserRepository;
import com.disp.celesmaproject.service.CustomUserDetailsService;
import com.disp.celesmaproject.util.CustomOAuth2UserAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Получаем данные из Google
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String pictureUrl = oAuth2User.getAttribute("picture");


        // Проверяем, есть ли пользователь в БД
        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            // Обновляем существующего пользователя
            user = userOptional.get();
            if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()){
                user.setAvatarUrl(pictureUrl);
            }
/*            user.setFirstName(firstName);
            user.setLastName(lastName);*/
        } else {
            // Создаем нового пользователя
            user = new User();
            assert email != null;
            String username = email.split("@")[0];
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setAvatarUrl(pictureUrl); // Устанавливаем аватарку
//            user.setPassword(passwordEncoder.encode("oauth2password"));
            user.setRole("USER"); // Устанавливаем роль по умолчанию
        }

        // Сохраняем пользователя в БД
        userDetailsService.saveUser(user);

        return new CustomOAuth2UserAdapter(user, oAuth2User.getAttributes());
    }
}