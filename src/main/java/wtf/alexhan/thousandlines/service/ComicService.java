package wtf.alexhan.thousandlines.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wtf.alexhan.thousandlines.dto.UploadComicRequest;
import wtf.alexhan.thousandlines.model.Comic;
import wtf.alexhan.thousandlines.model.Tag;
import wtf.alexhan.thousandlines.model.User;
import wtf.alexhan.thousandlines.model.UserRole;
import wtf.alexhan.thousandlines.repository.ComicRepository;
import wtf.alexhan.thousandlines.repository.TagRepository;

import java.io.IOException;
import java.util.*;

@Service
public class ComicService {
    private final ComicRepository comicRepository;
    private final TagRepository tagRepository;
    private final StorageService storageService;

    public ComicService(ComicRepository comicRepository, TagRepository tagRepository, StorageService storageService) {
        this.comicRepository = comicRepository;
        this.tagRepository = tagRepository;
        this.storageService = storageService;
    }

    /**
     * 获取所有漫画（不分页）
     */
    public List<Comic> getAllComics() {
        return comicRepository.findAll();
    }

    /**
     * 获取所有漫画（分页版）
     */
    public Page<Comic> getAllComics(Pageable pageable) {
        return comicRepository.findAll(pageable);
    }

    /**
     * 获取热门漫画（按浏览量排序）
     */
    public List<Comic> getPopularComics() {
        return comicRepository.findTop10ByOrderByViewCountDesc();
    }

    /**
     * 获取热门漫画（分页版）
     */
    public Page<Comic> getPopularComics(Pageable pageable) {
        return comicRepository.findAllByOrderByViewCountDesc(pageable);
    }

    /**
     * 获取最新漫画
     */
    public List<Comic> getLatestComics() {
        return comicRepository.findTop10ByOrderByCreatedAtDesc();
    }

    /**
     * 获取最新漫画（分页版）
     */
    public Page<Comic> getLatestComics(Pageable pageable) {
        return comicRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 根据ID获取漫画
     */
    public Comic getComicById(Long id) {
        return comicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("漫画不存在"));
    }

    /**
     * 根据用户ID获取漫画
     */
    public List<Comic> getComicsByUser(User user) {
        return comicRepository.findByUserId(user.getId());
    }

    /**
     * 根据用户ID获取漫画（分页版）
     */
    public Page<Comic> getComicsByUser(User user, Pageable pageable) {
        return comicRepository.findByUserId(user.getId(), pageable);
    }

    /**
     * 上传漫画
     */
    @Transactional
    public Comic uploadComic(UploadComicRequest request, User user) throws IOException {
        String comicFolder = UUID.randomUUID().toString();

        // 保存封面
        String coverPath = storageService.storeCoverImage(request.getCoverImage());

        // 保存漫画页面
        MultipartFile[] pages = request.getPages();
        if (pages == null || pages.length == 0) {
            throw new RuntimeException("请至少上传一页漫画");
        }

        for (int i = 0; i < pages.length; i++) {
            MultipartFile page = pages[i];
            if (page != null && !page.isEmpty()) {

                storageService.storeComicPage(page, comicFolder);
            }
        }

        // 处理标签
        Set<Tag> tags = new HashSet<>();
        List<String> tagList = request.getTags();

        if (tagList != null) {
            for (String tagItem : tagList) {
                // 处理逗号分隔的标签
                if (tagItem.contains(",")) {
                    String[] splitTags = tagItem.split(",");
                    for (String tagName : splitTags) {
                        tagName = tagName.trim();
                        if (!tagName.isEmpty()) {
                            Tag tag = getOrCreateTag(tagName);
                            tags.add(tag);
                        }
                    }
                } else {
                    String tagName = tagItem.trim();
                    if (!tagName.isEmpty()) {
                        Tag tag = getOrCreateTag(tagName);
                        tags.add(tag);
                    }
                }
            }
        }

        // 创建漫画记录
        Comic comic = new Comic();
        comic.setTitle(request.getTitle());
        comic.setDescription(request.getDescription());
        comic.setAuthor(request.getAuthor());
        comic.setCoverImagePath(coverPath);
        comic.setFolderPath(comicFolder);
        comic.setChapterCount(pages.length);
        comic.setUser(user);
        comic.setTags(tags);

        return comicRepository.save(comic);
    }

    /**
     * 删除漫画
     */
    @Transactional
    public void deleteComic(Long comicId, User user) throws IOException {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new RuntimeException("漫画不存在"));

        // 检查权限
        if (!comic.getUser().getId().equals(user.getId()) &&
                user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("没有删除权限");
        }

        // 删除文件
        if (comic.getFolderPath() != null) {
            storageService.deleteFolder(comic.getFolderPath());
        }

        // 删除数据库记录
        comicRepository.delete(comic);
    }

    /**
     * 更新漫画信息
     */
    @Transactional
    public Comic updateComic(Long comicId, String title, String description, String author,
                             List<String> tagNames, User user) {
        Comic comic = getComicById(comicId);

        // 检查权限
        if (!comic.getUser().getId().equals(user.getId()) &&
                user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("没有编辑权限");
        }

        // 更新基本信息
        comic.setTitle(title);
        comic.setDescription(description);
        comic.setAuthor(author);

        // 更新标签
        if (tagNames != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : tagNames) {
                tagName = tagName.trim();
                if (!tagName.isEmpty()) {
                    Tag tag = getOrCreateTag(tagName);
                    tags.add(tag);
                }
            }
            comic.setTags(tags);
        }

        return comicRepository.save(comic);
    }

    /**
     * 更新漫画封面
     */
    @Transactional
    public Comic updateCoverImage(Long comicId, MultipartFile coverImage, User user) throws IOException {
        Comic comic = getComicById(comicId);

        // 检查权限
        if (!comic.getUser().getId().equals(user.getId()) &&
                user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("没有编辑权限");
        }

        // 保存新封面
        String newCoverPath = storageService.storeCoverImage(coverImage);

        // TODO: 可选 - 删除旧的封面文件

        // 更新封面路径
        comic.setCoverImagePath(newCoverPath);

        return comicRepository.save(comic);
    }

    /**
     * 增加漫画浏览量
     */
    @Transactional
    public void incrementViewCount(Long comicId) {
        Comic comic = getComicById(comicId);
        comic.setViewCount(comic.getViewCount() + 1);
        comicRepository.save(comic);
    }

    /**
     * 搜索漫画
     */
    public List<Comic> searchComics(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllComics();
        }
        return comicRepository.searchComics(keyword.trim());
    }

    /**
     * 按标题搜索漫画
     */
    public List<Comic> searchByTitle(String title) {
        return comicRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * 按作者搜索漫画
     */
    public List<Comic> searchByAuthor(String author) {
        return comicRepository.findByAuthorContainingIgnoreCase(author);
    }

    /**
     * 按标签搜索漫画
     */
    public List<Comic> searchByTag(String tagName) {
        return comicRepository.findByTagName(tagName);
    }

    /**
     * 高级搜索（多条件组合）
     */
    public List<Comic> advancedSearch(String title, String author, String tag) {
        Set<Comic> resultSet = new HashSet<>();
        boolean hasCondition = false;

        if (title != null && !title.trim().isEmpty()) {
            resultSet.addAll(searchByTitle(title.trim()));
            hasCondition = true;
        }

        if (author != null && !author.trim().isEmpty()) {
            if (hasCondition) {
                // 取交集
                resultSet.retainAll(searchByAuthor(author.trim()));
            } else {
                resultSet.addAll(searchByAuthor(author.trim()));
                hasCondition = true;
            }
        }

        if (tag != null && !tag.trim().isEmpty()) {
            if (hasCondition) {
                // 取交集
                resultSet.retainAll(searchByTag(tag.trim()));
            } else {
                resultSet.addAll(searchByTag(tag.trim()));
            }
        }

        if (!hasCondition && (tag == null || tag.trim().isEmpty())) {
            return getAllComics();
        }

        return new ArrayList<>(resultSet);
    }

    /**
     * 获取所有标签
     */
    public List<String> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        List<String> tagNames = new ArrayList<>();
        for (Tag tag : tags) {
            tagNames.add(tag.getName());
        }
        return tagNames;
    }

    /**
     * 获取热门标签（按使用频率）
     */
    public List<Object[]> getPopularTags(int limit) {
        return comicRepository.findPopularTags(limit);
    }

    /**
     * 统计漫画数量
     */
    public long countComics() {
        return comicRepository.count();
    }

    /**
     * 统计用户漫画数量
     */
    public long countUserComics(User user) {
        return comicRepository.countByUserId(user.getId());
    }

    /**
     * 获取或创建标签
     */
    private Tag getOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                });
    }
}