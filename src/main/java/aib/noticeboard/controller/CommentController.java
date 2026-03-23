package aib.noticeboard.controller;

import aib.noticeboard.dto.request.CommentRequestDto;
import aib.noticeboard.dto.response.CommentResponseDto;
import aib.noticeboard.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto.Detail> create(@AuthenticationPrincipal String email,
                                                            @PathVariable Long postId,
                                                            @Valid @RequestBody CommentRequestDto.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(email, postId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponseDto.Detail>> getList(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getList(postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto.Detail> update(@AuthenticationPrincipal String email,
                                                            @PathVariable Long postId,
                                                            @PathVariable Long commentId,
                                                            @Valid @RequestBody CommentRequestDto.Update request) {
        return ResponseEntity.ok(commentService.update(email, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal String email,
                                       @PathVariable Long postId,
                                       @PathVariable Long commentId) {
        commentService.delete(email, commentId);
        return ResponseEntity.noContent().build();
    }
}
