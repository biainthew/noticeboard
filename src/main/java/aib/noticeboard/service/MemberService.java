package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.enums.MemberRole;
import aib.noticeboard.dto.request.MemberRequestDto;
import aib.noticeboard.dto.response.MemberResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.MemberRepository;
import aib.noticeboard.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public MemberResponseDto.Info signUp (MemberRequestDto.SignUp request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(MemberRole.ROLE_USER)
                .build();

        return new MemberResponseDto.Info(memberRepository.save(member));
    }

    public MemberResponseDto.Token login (MemberRequestDto.Login request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());
        String refreshToken  = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().name());

        // Refresh Token Redis에 저장
        refreshTokenService.save(member.getEmail(), refreshToken);

        return new MemberResponseDto.Token(accessToken, refreshToken);
    }

    public MemberResponseDto.AccessToken refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmail(refreshToken);
        String role = jwtTokenProvider.getRole(refreshToken);

        if (!refreshTokenService.isValid(email, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(email, role);

        return new MemberResponseDto.AccessToken(newAccessToken);
    }
}
