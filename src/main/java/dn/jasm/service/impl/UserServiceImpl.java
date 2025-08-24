package dn.jasm.service.impl;
import com.ea.async.Async;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.aop.TimeResulting;
import dn.jasm.dto.user.UserRequest;
import dn.jasm.dto.user.UserResponse;
import dn.jasm.dto.user.UserResponseList;
import dn.jasm.entity.*;
import dn.jasm.event.TransactionEvent;
import dn.jasm.event.UserCreateEvent;
import dn.jasm.exception.AlreadyExistException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.mapper.UserMapper;
import dn.jasm.repository.*;
import dn.jasm.configuration.redis.RedisService;
import dn.jasm.entity.enums.UserStatus;
import dn.jasm.event.UserUpdateEvent;
import dn.jasm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ea.async.Async.*;
import static java.util.concurrent.CompletableFuture.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderRepository orderRepository;
    private final RedisService redisService;
    private final Map<String,Integer> userBanMap = new HashMap<>();
    private final TransactionRepository transactionRepository;
    private final CommentRepository commentRepository;
    private final CardRepository cardRepository;
    private final NotificationRepository notificationRepository;


    @Override
    public UserResponse findByPhoneNumber(String phoneNumber) {
        if(!redisService.checkKeyExist(phoneNumber)){
             return userMapper.toDto(userRepository.findByPhoneNumber(phoneNumber)
                             .stream()
                             .peek(user-> redisService.writeObjectInRedis(phoneNumber,user))
                             .findAny()
                             .orElseThrow(()->new UserNotFoundException(
                                     MessageFormat.format("User with phoneNumber: {0} not found",phoneNumber))));
         }
         return userMapper.toDto(userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("User with phoneNumber: {0} not found",phoneNumber))));

    }


    private List<Object> mapToSingletonList(Object object){
        Async.init();
        await(completedFuture(object));
        return Optional.of(Collections.singletonList(object))
                .orElseThrow(()->new IllegalArgumentException("Element can't be null"));
    }

    @Override
    public UserResponse findByUsername(String username) {
        if (!redisService.checkKeyExist(username)){
            return userMapper.toDto(userRepository.findByUsername(username).map(
                    user->{
                        redisService.writeObjectInRedis(username,user);
                        return user;
                    }).orElseThrow(()->new UserNotFoundException(
                    MessageFormat.format("User with username: {0} not found",username))));
        }
        return userMapper.toDto(userRepository.findByUsername(username)
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("User with username: {0} not found",username))));
    }

    @Override
    @Loggable
    @TimeResulting
    public UserResponse findById(Long id) {
        String cacheKey = userMapper.mapUserIdToString(id);
        if (!redisService.checkKeyExist(cacheKey)) {
            return userMapper.toDto(userRepository.findById(id)
                    .map(user -> {
                        redisService.writeObjectInRedis(cacheKey, user);
                        return user;
                    }).orElseThrow(() -> new UserNotFoundException(
                            MessageFormat.format("User with id: {0} not found", id))));
        }
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        MessageFormat.format("User with id: {0} not found", id))));
    }

    @Override
    @Loggable
    public UserResponseList findAllWithPagination(int pageNumber, int pageSize) {
        List<UserEntity> users = userRepository.findAll(
               PageRequest.of(pageNumber, pageSize))
                .getContent();
        List<String> keys = users.stream()
                .map(UserEntity::getId)
                .map(String::valueOf)
                .toList();
        if (redisService.checkKeysExist(keys)){
            var userValues = Collections.singletonList(users);
            redisService.writeObjectsInRedis(keys, Collections.singletonList(userValues));
            return userMapper.toList(users);
        }
        return userMapper.toList(users);


    }

    @Override
    public UserResponseList findAllByIds(List<Long> ids) {
        if (ids.isEmpty()){
            throw new IllegalArgumentException("Ids is empty or null!");
        }
        var idsAsList = ids.stream().toList().toString();
        List<UserEntity> users = userRepository.findAllById(ids)
                .stream()
                .takeWhile(user -> user.getId() != null)
                .toList();
        if (redisService.checkKeysExist(Collections.singletonList(idsAsList))){
            String keysStringValues = mapToString(ids);
            List<String> userIdsKeys = Collections.singletonList(keysStringValues);
            redisService.writeObjectsInRedis(userIdsKeys, mapToSingletonList(users));
            return userMapper.toList(users);
        }
        return userMapper.toList(users);
    }

    @Override
    @Transactional
    @Loggable
    @TimeResulting
    public UserResponse createUser(UserRequest userRequest) {
        UserEntity user = new UserEntity();
        user.setUsername(userRequest.getUsername());
        if (userRepository.existsByUsernameOrPhoneNumber(
                userRequest.getUsername(),
                userRequest.getPhoneNumber())) {
            throw new AlreadyExistException("User already exists!");
        }
        user.setPassword(userRequest.getPassword());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setCreatedAt(user.getCreatedAt());
        user.setUpdatedAt(LocalDateTime.now());
        user.setStatus(UserStatus.NEW.name());
        userRepository.save(user);
        log.info("Created user: {}",user);
        var cacheKey = userMapper.mapUserIdToString(user.getId());
        redisService.writeObjectInRedis(cacheKey,user);
        eventPublisher.publishEvent(new UserCreateEvent(this,
                userRequest.getUsername(),
                userRequest.getPhoneNumber(),
                LocalDateTime.now()));
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .userStatus(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


    private String mapToString(Object element){
        return Optional.of(Objects.toString(element))
                .orElseThrow(()->new IllegalArgumentException("Element can't be null!".toUpperCase()));
    }


    @Override
    @Transactional
    public void updateUser(Long id, UserRequest userRequest) {
       userRepository.findById(id)
               .ifPresentOrElse(user->{
                    user.setUsername(userRequest.getUsername());
                    user.setPassword(userRequest.getPassword());
                    user.setPhoneNumber(userRequest.getPhoneNumber());
                    user.setUpdatedAt(LocalDateTime.now());
                    user.setCreatedAt(user.getCreatedAt());
                    userRepository.save(user);
                    redisService.writeObjectInRedis(mapToString(user.getId()),user);
                    eventPublisher.publishEvent(new UserUpdateEvent(
                            this,
                            user.getUsername(),
                            user.getPhoneNumber(),
                            user.getUpdatedAt(),
                            true));
                    log.info("Updated user: {}",user.getUsername());
                },  ()->{
                    throw new UserNotFoundException(MessageFormat.format("User with id: {0} not found",id));
                }
                );

    }

    @Override
    public void banUserById(Long id) {
        var requireUser = userRepository.findById(id)
                .stream()
                .peek(user-> user.setStatus(UserStatus.BANNED.name()))
                .findAny()
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("User with id: {0} not found",id)));
        requireUser.setBanTime(LocalDateTime.now());
        userRepository.save(requireUser);
        var cacheKey = mapToString(id);

        redisService.deleteCacheByKey(cacheKey);
        log.info("Banned user is: {}",requireUser.getUsername());
    }

    @Override
    public UserResponse getUsersByStatus(String status) {
        Map<String,Object> usersAndTheirStatuses = userRepository.findAllByStatus(status)
                .stream()
                .collect(Collectors.filtering(
                        user -> UserStatus.NEW.name().equalsIgnoreCase(user.getStatus()),
                        Collectors.toMap(
                                UserEntity::getUsername,
                                UserEntity::getStatus)));

        var userKeys = usersAndTheirStatuses.keySet()
                .stream()
                .toList();
        var userValues = mapToSingletonList(usersAndTheirStatuses.values()
                .stream()
                .toList());

        if (redisService.checkKeysExist(userKeys)){
            redisService.writeObjectsInRedis(userKeys,userValues);
        }
        log.info("Users by status: {}",usersAndTheirStatuses);
        return UserResponse.builder()
                .users(usersAndTheirStatuses)
                .build();
    }

    @Override
    @TimeResulting
    @Loggable
    public UserResponse getUsersCountsOfDeals() {
        return UserResponse.builder()
                .users(userRepository.findAll()
                .stream()
                .takeWhile(user->!Objects.equals(user.getCountOfDeals(),null))
                .peek(user -> {
                    String key = mapToString(user.getId());
                    if (!redisService.checkKeyExist(key)) {
                        redisService.writeObjectInRedis(key, user);
                    }}).collect(Collectors.toMap(
                        UserEntity::getUsername,
                        UserEntity::getCountOfDeals)))
                .build();
    }

    @Override
    public UserResponse getUserByOrderId(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(
                        MessageFormat.format("Order with id: {0} not found",orderId)));
        UserEntity user = order.getUser();
        if (user == null) {
            throw new UserNotFoundException(
                    MessageFormat.format("User not found for order with id: {0} ",orderId));
        }
        String cacheKey = mapToString(user.getId());
        if (!redisService.checkKeyExist(cacheKey)) {
            redisService.writeObjectInRedis(cacheKey, user);
        }
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(user.getId().toString(), user.getUsername());
        return UserResponse.builder()
                .users(userMap)
                .build();
    }

    @Override
    public UserResponse findAllWithCountOfDealsGreatherThanNull() {
        return UserResponse.builder()
                .users(userRepository.getAllByCountOfDealsNotNull()
                        .stream()
                        .collect(Collectors.toMap(
                                UserEntity::getUsername,
                                UserEntity::getCountOfDeals)))
                        .build();
    }


    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        MessageFormat.format("User with id: {0} not found", id)));

        if (user.getComments() != null) {
            user.getComments().forEach(comment -> comment.setUser(null));
            commentRepository.saveAll(user.getComments());
            user.getComments().clear();
        }
        if (user.getCards() != null) {
            user.getCards().forEach(card -> card.setUser(null));
            cardRepository.saveAll(user.getCards());
            user.getCards().clear();
        }
        if (user.getOrders() != null) {
            user.getOrders().forEach(order -> order.setUser(null));
            orderRepository.saveAll(user.getOrders());
            user.getOrders().clear();
        }
        if (user.getNotifications() != null) {
            user.getNotifications().forEach(notificationEntity -> notificationEntity.setUser(null));
            notificationRepository.saveAll(user.getNotifications());
            user.getNotifications().clear();
        }
        if (user.getTransactionEntity() != null) {
            TransactionEntity transaction = user.getTransactionEntity();
            transaction.getOrderEntity().setTransactionEntity(null);
            transaction.getUserEntity().setTransactionEntity(null);
            transaction.setUserEntity(null);
            transaction.setOrderEntity(null);
            transactionRepository.deleteById(transaction.getId());
            log.info("Saved transaction: {}",transaction.getId());
        }
        userRepository.delete(user);
        redisService.deleteCacheByKey(String.valueOf(user.getId()));
    }



    @Transactional
    @Override
    public void deleteMultipleUsers(List<Long> ids) {
        var users = userRepository.findAllById(ids);
        var userTransactionIds = users.stream()
                         .map(UserEntity::getTransactionEntity)
                         .map(TransactionEntity::getId)
                         .toList();
        userRepository.deleteAllByIdInBatch(ids);
        transactionRepository.deleteAllByIdInBatch(userTransactionIds);
        log.info("Deleted users: {}",users);

    }

    @Override
    public void unbanUser(Long id,int minutes) {
        long expiry = System.currentTimeMillis() + minutes * 60_000L;
        userBanMap.put(mapToString(id),minutes);

    }

    public boolean isBanned(Long id){
        var expiry = userBanMap.get(mapToString(id));
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    void cleanUpBannedUser(){
        userBanMap.entrySet()
                .removeIf(e->System.currentTimeMillis()>=e.getValue());
    }

    @Override
    @Transactional
    @Loggable
    public void updateUsers(Set<Long> ids, Set<UserRequest> userRequests) {
        List<UserEntity> usersForUpdate = userRepository.findAllById(ids)
                .stream()
                .peek( user -> {
                    UserRequest userRequest = userRequests.iterator().next();
                    user.setPhoneNumber(userRequest.getPhoneNumber());
                    user.setUsername(userRequest.getUsername());
                    user.setUpdatedAt(LocalDateTime.now());
                }).toList();
        userRepository.saveAll(usersForUpdate);
        String cacheKeys = mapToString(ids);
        List<String> cacheKeysListValue = Collections.singletonList(cacheKeys);
        List<Object> userValues = Collections.singletonList(usersForUpdate);
        redisService.writeObjectsInRedis(cacheKeysListValue,userValues);
        log.info("UpdatedUsers: {}",usersForUpdate);
    }

    @Override
    public void addCard(UserEntity user, CardEntity card) {
        List<CardEntity> cards = user.getCards();
        if (!Objects.equals(card,null)){
            cards.add(card);
            log.info("Added card: {},cardList: {}",card,cards);
        }

    }

    @Override
    public void addOrder(UserEntity user, OrderEntity order) {
        List<OrderEntity> orders = user.getOrders();
        if (!Objects.equals(order,null)){
            orders.add(order);
            log.info("Added order: {}, orders: {}",order,orders);
        }
    }

    @Override
    public void addComment(UserEntity user, CommentEntity comment) {
        List<CommentEntity> comments = user.getComments();
        if (!Objects.equals(comment,null)){
            comments.add(comment);
            log.info("Added comment: {}, comments: {}",comment,comments);
        }

    }

    @Override
    public boolean isExpired(UserEntity user) {
        return ChronoUnit.MINUTES
                .between(user.getCreatedAt(),LocalDateTime.now())>1;
    }

}
