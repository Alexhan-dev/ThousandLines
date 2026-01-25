package wtf.alexhan.thousandlines.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import wtf.alexhan.thousandlines.dto.LoginRequest;
import wtf.alexhan.thousandlines.dto.RegRequest;
import wtf.alexhan.thousandlines.model.User;
import wtf.alexhan.thousandlines.service.UserService;

import java.util.Optional;

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
                System.out.println(request.getPassword() + ": WTF Brooo ：" + user.getPassword());
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

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegRequest regRequest, 
                          @RequestParam(required = false) boolean agreeTerms,
                          BindingResult bindingResult, 
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "表单验证失败，请检查输入");
            return "register";
        }
        
        // 检查用户协议是否同意
        if (!agreeTerms) {
            model.addAttribute("error", "请阅读并同意用户协议");
            return "register";
        }

        try {
            userService.registerUser(regRequest);
            model.addAttribute("success", "注册成功，请登录");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}