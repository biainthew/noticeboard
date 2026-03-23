package aib.noticeboard.controller;

import aib.noticeboard.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<Void> like(@AuthenticationPrincipal String email,
                                      @PathVariable Long postId) {
        likeService.like(email, postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlike(@AuthenticationPrincipal String email,
                                       @PathVariable Long postId) {
        likeService.unlike(email, postId);
        return ResponseEntity.noContent().build();
    }
}
