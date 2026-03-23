package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Notification;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.MemberRole;
import aib.noticeboard.domain.enums.NotificationType;
import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.dto.response.NotificationResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.MemberRepository;
import aib.noticeboard.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member receiver;
    private Member sender;
    private Post post;

    @BeforeEach
    void setUp() {
        receiver = Member.builder()
                .email("receiver@test.com")
                .password("password123")
                .nickname("수신자")
                .role(MemberRole.ROLE_USER)
                .build();

        sender = Member.builder()
                .email("sender@test.com")
                .password("password123")
                .nickname("발신자")
                .role(MemberRole.ROLE_USER)
                .build();

        post = Post.builder()
                .member(receiver)
                .title("테스트 제목")
                .content("테스트 내용")
                .status(PostStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("알림 전송 성공")
    void send_success() {
        // when
        notificationService.send(receiver, sender, post, NotificationType.COMMENT);

        // then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 전송 실패 - 자기 자신에게는 알림 전송 안 함")
    void send_skip_self() {
        // when
        notificationService.send(receiver, receiver, post, NotificationType.COMMENT);

        // then
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getList_success() {
        // given
        given(memberRepository.findByEmail(receiver.getEmail())).willReturn(Optional.of(receiver));
        given(notificationRepository.findAllByReceiverOrderByCreatedAtDesc(receiver))
                .willReturn(List.of());

        // when
        List<NotificationResponseDto.Detail> result = notificationService.getList(receiver.getEmail());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("알림 목록 조회 실패 - 존재하지 않는 회원")
    void getList_fail_memberNotFound() {
        // given
        given(memberRepository.findByEmail(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.getList("wrong@test.com"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 성공")
    void getUnreadCount_success() {
        // given
        given(memberRepository.findByEmail(receiver.getEmail())).willReturn(Optional.of(receiver));
        given(notificationRepository.countByReceiverAndIsReadFalse(receiver)).willReturn(3L);

        // when
        long count = notificationService.getUnreadCount(receiver.getEmail());

        // then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("알림 전체 읽음 처리 성공")
    void readAll_success() {
        // given
        given(memberRepository.findByEmail(receiver.getEmail())).willReturn(Optional.of(receiver));
        given(notificationRepository.findAllByReceiverOrderByCreatedAtDesc(receiver))
                .willReturn(List.of());

        // when
        notificationService.readAll(receiver.getEmail());

        // then
        verify(notificationRepository).findAllByReceiverOrderByCreatedAtDesc(receiver);
    }
}
