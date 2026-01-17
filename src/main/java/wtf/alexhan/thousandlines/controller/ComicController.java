package wtf.alexhan.thousandlines.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wtf.alexhan.thousandlines.dto.UploadComicRequest;
import wtf.alexhan.thousandlines.model.Comic;
import wtf.alexhan.thousandlines.model.User;
import wtf.alexhan.thousandlines.model.UserRole;
import wtf.alexhan.thousandlines.service.ComicService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/comics")
public class ComicController {
    private final ComicService comicService;

    public ComicController(ComicService comicService) {
        this.comicService = comicService;
    }

    @GetMapping
    public String listComics(@RequestParam(value = "sort", required = false) String sort,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "12") int size,
                             Model model) {
        Pageable pageable;
        Page<Comic> comicPage;

        if ("popular".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by("viewCount").descending());
            comicPage = comicService.getPopularComics(pageable);
            model.addAttribute("sort", "popular");
        } else if ("latest".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            comicPage = comicService.getLatestComics(pageable);
            model.addAttribute("sort", "latest");
        } else {
            pageable = PageRequest.of(page, size);
            comicPage = comicService.getAllComics(pageable);
        }

        // 获取所有标签用于搜索框
        List<String> allTags = comicService.getAllTags();

        model.addAttribute("comics", comicPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", comicPage.getTotalPages());
        model.addAttribute("totalItems", comicPage.getTotalElements());
        model.addAttribute("allTags", allTags);

        return "index";
    }

    @GetMapping("/search")
    public String searchComics(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "author", required = false) String author,
                               @RequestParam(value = "tag", required = false) String tag,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "12") int size,
                               Model model) {

        boolean isSearch = keyword != null || title != null || author != null || tag != null;

        if (isSearch) {
            // 高级搜索（不分页）
            List<Comic> comics = comicService.advancedSearch(title, author, tag);

            // 检查是否有搜索结果
            if (comics.isEmpty()) {
                model.addAttribute("searchNoResult", true);
                model.addAttribute("keyword", keyword);
                model.addAttribute("title", title);
                model.addAttribute("author", author);
                model.addAttribute("tag", tag);

                // 获取所有标签
                List<String> allTags = comicService.getAllTags();
                model.addAttribute("allTags", allTags);

                return "index";
            }

            model.addAttribute("title", title);
            model.addAttribute("author", author);
            model.addAttribute("tag", tag);
            model.addAttribute("comics", comics);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalItems", comics.size());
        } else {
            // 没有搜索条件，显示所有漫画（分页）
            Pageable pageable = PageRequest.of(page, size);
            Page<Comic> comicPage = comicService.getAllComics(pageable);
            model.addAttribute("comics", comicPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", comicPage.getTotalPages());
            model.addAttribute("totalItems", comicPage.getTotalElements());
        }

        // 获取所有标签用于搜索框
        List<String> allTags = comicService.getAllTags();

        model.addAttribute("allTags", allTags);
        model.addAttribute("searchPerformed", isSearch);
        return "index";
    }

    // 添加分页搜索方法到 ComicService
    // 在 ComicService.java 中添加：
    /*
    public Page<Comic> searchComics(String keyword, Pageable pageable) {
        // 需要先在 ComicRepository 中添加分页搜索方法
        return comicRepository.searchComics(keyword, pageable);
    }
    */

    @GetMapping("/advanced-search")
    public String showAdvancedSearch(Model model) {
        List<String> allTags = comicService.getAllTags();
        model.addAttribute("allTags", allTags);
        return "advanced-search";
    }

    @GetMapping("/{id}")
    public String viewComic(@PathVariable Long id, Model model, HttpSession session) {
        Comic comic = comicService.getComicById(id);
        comicService.incrementViewCount(id);

        User user = (User) session.getAttribute("user");
        boolean canDelete = user != null &&
                (user.getId().equals(comic.getUser().getId()) ||
                        user.getRole() == UserRole.ADMIN);
        boolean canEdit = user != null &&
                (user.getId().equals(comic.getUser().getId()) ||
                        user.getRole() == UserRole.ADMIN);

        // 获取相关漫画（同作者或同标签）
        List<Comic> relatedComics = getRelatedComics(comic);

        model.addAttribute("comic", comic);
        model.addAttribute("canDelete", canDelete);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("relatedComics", relatedComics);
        return "viewer";
    }

    private List<Comic> getRelatedComics(Comic comic) {
        // 简单的相关推荐逻辑
        // 可以改为从数据库查询同作者或同标签的漫画
        return comicService.searchByAuthor(comic.getAuthor()).stream()
                .filter(c -> !c.getId().equals(comic.getId()))
                .limit(4)
                .toList();
    }

    @GetMapping("/upload")
    public String showUploadForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() == UserRole.USER) {
            return "redirect:/auth/login";
        }
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadComic(@RequestParam("title") String title,
                              @RequestParam(value = "author", required = false) String author,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "tags", required = false) String tags,
                              @RequestParam("coverImage") MultipartFile coverImage,
                              @RequestParam("pages") MultipartFile[] pages,
                              HttpSession session,
                              Model model) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null || user.getRole() == UserRole.USER) {
                return "redirect:/auth/login";
            }

            // 创建请求对象
            UploadComicRequest request = new UploadComicRequest();
            request.setTitle(title);
            request.setAuthor(author);
            request.setDescription(description);

            // 处理标签
            if (tags != null && !tags.trim().isEmpty()) {
                List<String> tagList = Arrays.asList(tags.split(","));
                request.setTags(tagList);
            }

            request.setCoverImage(coverImage);
            request.setPages(pages);

            Comic comic = comicService.uploadComic(request, user);
            return "redirect:/comics/" + comic.getId();
        } catch (IOException e) {
            model.addAttribute("error", "文件上传失败: " + e.getMessage());
            return "upload";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "upload";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteComic(@PathVariable Long id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/auth/login";
            }

            comicService.deleteComic(id, user);
            return "redirect:/comics";
        } catch (IOException e) {
            return "redirect:/comics/" + id;
        }
    }
}