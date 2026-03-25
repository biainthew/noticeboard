package aib.noticeboard.controller;

import aib.noticeboard.dto.request.MemberRequestDto;
import aib.noticeboard.dto.response.MemberResponseDto;
import aib.noticeboard.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<MemberResponseDto.Info> signUp (@Valid @RequestBody MemberRequestDto.SignUp request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<MemberResponseDto.Token> login(@Valid @RequestBody MemberRequestDto.Login request) {
        return ResponseEntity.ok(memberService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<MemberResponseDto.AccessToken> refresh(
            @Valid @RequestBody MemberRequestDto.Refresh request) {
        return ResponseEntity.ok(memberService.refresh(request.getRefreshToken()));
    }
}
