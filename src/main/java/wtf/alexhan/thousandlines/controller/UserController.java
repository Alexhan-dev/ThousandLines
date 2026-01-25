package wtf.alexhan.thousandlines.controller;

import jakarta.servlet.http.HttpSession;
import wtf.alexhan.thousandlines.model.User;
import wtf.alexhan.thousandlines.model.UserRole;
import wtf.alexhan.thousandlines.service.ComicService;
import wtf.alexhan.thousandlines.service.StorageService;
import wtf.alexhan.thousandlines.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ComicService comicService;
    
    @Autowired
    private StorageService storageService;

    @GetMapping("/profile/{username}")
    public String showProfile(@PathVariable String username, HttpSession session, Model model) {
        // 检查用户是否登录
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        // 获取目标用户
        User targetUser = userService.getUserByUsername(username).orElse(null);
        if (targetUser == null) {
            return "redirect:/dashboard"; // 用户不存在，重定向到dashboard
        }
        
        model.addAttribute("user", targetUser);
        model.addAttribute("isOwnProfile", currentUser.getUsername().equals(username)); // 标记是否是自己的profile
        
        return "profile";
    }

    @PostMapping("/profile/{username}/update")
    public String updateProfile(@PathVariable String username, @RequestParam String bio) {
        User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        user.setBio(bio);
        // 头像路径通过专门的上传接口处理，不在此处更新
        userService.updateUser(user);
        return "redirect:/profile/" + username;
    }

    @PostMapping("/profile/{username}/upload-avatar")
    public String uploadAvatar(@PathVariable String username, 
                              @RequestParam("avatarFile") MultipartFile avatarFile,
                              HttpSession session,
                              Model model) {
        try {
            User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            
            // 检查是否是当前登录用户或管理员
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null || (!currentUser.getUsername().equals(username) && currentUser.getRole() != UserRole.ADMIN)) {
                model.addAttribute("error", "没有权限上传头像");
                return "profile";
            }
            
            // 上传头像文件
            String avatarPath = storageService.storeAvatarImage(avatarFile, username);
            user.setAvatarPath(avatarPath);
            userService.updateUser(user);
            
            // 更新session中的用户信息
            if (currentUser.getUsername().equals(username)) {
                session.setAttribute("user", user);
            }
            
            return "redirect:/profile/" + username;
        } catch (Exception e) {
            model.addAttribute("error", "头像上传失败: " + e.getMessage());
            model.addAttribute("user", userService.getUserByUsername(username).orElse(null));
            return "profile";
        }
    }
    
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("user");
        
        // 检查用户是否登录
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        // 获取用户上传的漫画列表
        model.addAttribute("comics", comicService.getComicsByUser(currentUser));
        
        // 获取用户统计信息
        long totalComics = comicService.countUserComics(currentUser);
        long totalViews = comicService.getComicsByUser(currentUser).stream()
                .mapToLong(comic -> comic.getViewCount() != null ? comic.getViewCount() : 0)
                .sum();
        
        model.addAttribute("user", currentUser);
        model.addAttribute("totalComics", totalComics);
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("isOwnDashboard", true); // 标记是本用户的dashboard
        
        return "dashboard";
    }
    
    @GetMapping("/dashboard/{username}")
    public String showUserDashboard(@PathVariable String username, HttpSession session, Model model) {
        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("user");
        
        // 检查用户是否登录
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        // 获取目标用户
        User targetUser = userService.getUserByUsername(username).orElse(null);
        if (targetUser == null) {
            return "redirect:/dashboard"; // 用户不存在，重定向到自己的dashboard
        }
        
        // 检查是否是访问自己的dashboard或管理员
        boolean isOwnDashboard = currentUser.getUsername().equals(username);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        
        if (!isOwnDashboard && !isAdmin) {
            // 只能查看自己的dashboard或管理员可以查看任何用户的dashboard
            return "redirect:/dashboard";
        }
        
        // 获取用户上传的漫画列表
        model.addAttribute("comics", comicService.getComicsByUser(targetUser));
        
        // 获取用户统计信息
        long totalComics = comicService.countUserComics(targetUser);
        long totalViews = comicService.getComicsByUser(targetUser).stream()
                .mapToLong(comic -> comic.getViewCount() != null ? comic.getViewCount() : 0)
                .sum();
        
        model.addAttribute("user", targetUser);
        model.addAttribute("totalComics", totalComics);
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("isOwnDashboard", isOwnDashboard); // 标记是否是自己的dashboard
        
        return "dashboard";
    }
    
    @GetMapping("/terms")
    public String showTerms() {
        return "terms";
    }
    
    @GetMapping("/maintenance")
    public String showMaintenance(Model model) {
        model.addAttribute("maintenanceMessage", "系统维护中，请稍后再试");
        model.addAttribute("estimatedCompletionTime", "2-3小时");
        return "maintenance";
    }
}