package aib.noticeboard.controller;

import aib.noticeboard.dto.request.PostRequestDto;
import aib.noticeboard.dto.response.PostResponseDto;
import aib.noticeboard.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDto.Detail> create(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody PostRequestDto.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(email, request));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponseDto.Summary>> getList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getList(pageable));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto.Detail> getDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getDetail(postId));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto.Detail> update(
            @AuthenticationPrincipal String email,
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto.Update request) {
        return ResponseEntity.ok(postService.update(email, postId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal String email,
            @PathVariable Long postId) {
        postService.delete(email, postId);
        return ResponseEntity.noContent().build();
    }
}
