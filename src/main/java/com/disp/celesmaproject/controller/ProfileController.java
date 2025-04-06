/*
import com.disp.learnspringsecurity.model.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/user/home")
    public String userHome(@AuthenticationPrincipal CustomUserDetailsService userDetailsService, Model model) {
        model.addAttribute("email", userDetailsService.getEmail());
        model.addAttribute("registrationDate", userDetailsService.getRegistrationDate()); // Дата регистрации
        return "home_user";
    }
}
*/
