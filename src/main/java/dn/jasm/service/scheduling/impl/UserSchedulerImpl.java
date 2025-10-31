package dn.jasm.service.scheduling.impl;
import dn.jasm.entity.UserEntity;
import dn.jasm.repository.UserRepository;
import dn.jasm.entity.enums.UserStatus;
import dn.jasm.service.RabbitService;
import dn.jasm.service.UserService;
import dn.jasm.service.scheduling.UserScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSchedulerImpl implements UserScheduler {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RedisTemplate<String,String> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public void cleanBannedUsers() {
        var userIds = userRepository.findAll()
                .stream()
                .filter(u->u.getStatus().equalsIgnoreCase(UserStatus.NEW.name()))
                .map(UserEntity::getId)
                .toList();
        userRepository.deleteAllByIdInBatch(userIds);
        var users = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(UserEntity::getUsername,
                        UserEntity::getStatus));
        rabbitTemplate.destroy();
        log.info("Banned users is delete : {}",users);
    }


    @Transactional
    @Override
    public void unbanUser() {
        List<UserEntity> bannedUsers = userRepository.findAllByStatus(UserStatus.ACTIVE.name())
                .stream()
                .toList();
        Map<String, Object> userMap = bannedUsers.stream()
                .collect(Collectors.toMap(
                        UserEntity::getUsername,
                        UserEntity::getStatus));
        List<UserEntity> unbannedUsers = bannedUsers.stream()
                .filter(user -> user.getIsPayOnceOrder()!=null)
                .filter(userService::isExpired)
                .peek(user-> {
                    user.setStatus(UserStatus.NEW.name());
                    user.setIsPayOnceOrder(false);
                }).toList();

        log.info("Unbanned users: {}",unbannedUsers);
        userRepository.saveAll(unbannedUsers);
        log.info("Users for delete: {}",userMap.keySet());
        userMap.entrySet()
                .removeIf(e -> e.getValue().equals(UserStatus.NEW.name()));
    }


    @Scheduled(cron = "0 0 0 * * *")
    @Override
    public void cleanCache(){
        Set<String> keys = Objects.requireNonNull(redisTemplate.keys("*"));
        keys.forEach(key -> {
                    redisTemplate.delete(key);
                    log.info("Deleted keys with unexpected key: {}", key);
                });
    }

}

