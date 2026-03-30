package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Notification;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.NotificationType;
import aib.noticeboard.dto.response.NotificationResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.MemberRepository;
import aib.noticeboard.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    // 연결된 SSE 클라이언트 저장
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String email) {
        // DB 조회를 별도 트랜잭션으로 분리해서 커넥션 즉시 반환
        Long memberId = getMemberId(email);

        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 타임아웃

        emitters.put(memberId, emitter);

        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));
        emitter.onError(e -> emitters.remove(memberId));

        // 연결 직후 더미 이벤트 전송(연결 유지용)
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            emitters.remove(memberId);
        }

        return emitter;
    }

    @Transactional(readOnly = true)
    public Long getMemberId(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND))
                .getId();
    }

    @Transactional
    public void send(Member receiver, Member sender, Post post, NotificationType type) {
        // 자기 자신에게는 알림 전송 안 함
        // if (Objects.equals(receiver.getId(), sender.getId())) return;
        if (receiver.getEmail().equals(sender.getEmail())) return;

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .post(post)
                .type(type)
                .build();

        notificationRepository.save(notification);

        // SSE로 실시간 전송
        SseEmitter emitter = emitters.get(receiver.getId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(new NotificationResponseDto.Detail(notification)));
            } catch (IOException e) {
                emitters.remove(receiver.getId());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto.Detail> getList(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return notificationRepository.findAllByReceiverOrderByCreatedAtDesc(member)
                .stream()
                .map(NotificationResponseDto.Detail::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return notificationRepository.countByReceiverAndIsReadFalse(member);
    }

    @Transactional
    public void readAll(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        notificationRepository.findAllByReceiverOrderByCreatedAtDesc(member)
                .forEach(Notification::read);
    }
}
