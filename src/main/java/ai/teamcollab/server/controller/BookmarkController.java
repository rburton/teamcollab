package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.BookmarkService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Autowired
    public BookmarkController(@NonNull BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public String index(@NonNull @AuthenticationPrincipal User user, Model model) {
        model.addAttribute("bookmarkedMessages", bookmarkService.getBookmarkedMessages(user.getId()));
        return "bookmarks/index";
    }
}