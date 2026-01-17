package wtf.alexhan.thousandlines.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wtf.alexhan.thousandlines.dto.LoginRequest;
import wtf.alexhan.thousandlines.model.User;
import wtf.alexhan.thousandlines.service.UserService;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (!userService.validatePassword(request.getPassword(), user.getPassword())) {
                System.out.println(request.getPassword()+": WTF Brooo ："+user.getPassword());
                throw new RuntimeException("密码错误");
            }

            session.setAttribute("user", user);
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
