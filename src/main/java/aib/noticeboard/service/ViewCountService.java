package aib.noticeboard.service;

import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;

    private static final String VIEW_COUNT_KEY = "post:viewcount";

    public void increaseViewCount(Long postId) {
        String key = VIEW_COUNT_KEY + postId;
        redisTemplate.opsForValue().increment(key);
    }

    public int getViewCount(Long postId) {
        String key = VIEW_COUNT_KEY + postId;
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    @Scheduled(fixedDelay = 60000) // 1분마다 DB에 반영
    @Transactional
    public void syncViewCountToDB() {
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_KEY + "*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) continue;

            Long postId = Long.parseLong(key.replace(VIEW_COUNT_KEY, ""));
            int count = Integer.parseInt(value);

            postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE).ifPresent(post -> {
                post.syncViewCount(count);
                redisTemplate.delete(key);
            });
        }
    }
}
