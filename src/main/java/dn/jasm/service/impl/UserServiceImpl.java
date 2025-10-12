package dn.jasm.service.impl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.aop.TimeResulting;
import dn.jasm.configuration.kafka.KafkaService;
import dn.jasm.dto.card.CardResponse;
import dn.jasm.dto.user.UserRequest;
import dn.jasm.dto.user.UserResponse;
import dn.jasm.dto.user.UserResponseList;
import dn.jasm.entity.*;
import dn.jasm.event.EventType;
import dn.jasm.event.PaymentEvent;
import dn.jasm.event.user.UserCreateEvent;
import dn.jasm.exception.AlreadyExistException;
import dn.jasm.exception.CardNotFoundException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.mapper.CardMapper;
import dn.jasm.mapper.UserMapper;
import dn.jasm.repository.*;
import dn.jasm.service.RedisService;
import dn.jasm.entity.enums.UserStatus;
import dn.jasm.event.user.UserUpdateEvent;
import dn.jasm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CommentRepository commentRepository;
    private final CardRepository cardRepository;
    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;
    private final UserMapper userMapper;
    private final CardMapper cardMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisService redisService;
    private final KafkaService kafkaService;


    @Override
    public UserResponse findByPhoneNumber(String phoneNumber) {
        if(!redisService.checkKeyExist(phoneNumber)){
             return userMapper.mapToDto(userRepository.findByPhoneNumber(phoneNumber)
                             .stream()
                             .peek(user-> redisService.writeObjectInRedis(phoneNumber,user))
                             .findAny()
                             .orElseThrow(()->new UserNotFoundException(
                                     MessageFormat.format("[User with phoneNumber: {0} not found]",phoneNumber))));
         }
         return userMapper.mapToDto(userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("[User with phoneNumber: {0} not found]",phoneNumber))));

    }




    @Override
    public UserResponse findByUsername(String username) {
        if (!redisService.checkKeyExist(username)){
            return userMapper.mapToDto(userRepository.findByUsername(username).map(
                    user->{
                        redisService.writeObjectInRedis(username,user);
                        return user;
                    }).orElseThrow(()->new UserNotFoundException(
                    MessageFormat.format("[User with username: {0} not found]",username))));
        }
        return userMapper.mapToDto(userRepository.findByUsername(username)
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("[User with username: {0} not found]",username))));
    }

    @Override
    @TimeResulting
    public UserResponse findById(Long id) {
        String cacheKey = String.valueOf(id);
        if (!redisService.checkKeyExist(cacheKey)) {
            return userMapper.mapToDto(userRepository.findById(id)
                    .map(user -> {
                        Hibernate.initialize(user.getComments());
                        redisService.writeObjectInRedis(cacheKey, user);
                        return user;
                    }).orElseThrow(() -> new UserNotFoundException(
                            MessageFormat.format("[User with id: {0} not found]", id))));
        }
        return userMapper.mapToDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        MessageFormat.format("[User with id: {0} not found]", id))));
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponseList findAllWithPagination(int pageNumber, int pageSize) {
        List<UserEntity> users = userRepository.findAll(
               PageRequest.of(pageNumber, pageSize))
                .getContent();
        Set<String> keys = users.stream()
                .map(UserEntity::getId)
                .map(String::valueOf)
                .collect(Collectors.toSet());
        if (redisService.checkKeysExist(keys)){
            var userValues = new HashSet<>(users);
            redisService.writeObjectsInRedis(keys, Collections.singleton(userValues));
            return userMapper.mapToDtoList(users);
        }
        return userMapper.mapToDtoList(users);


    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseList findAllByIds(List<Long> ids) {
        if (ids.isEmpty()){
            throw new IllegalArgumentException("[Ids is empty or null!]");
        }
        var idsAsList = new HashSet<>(ids).toString();
        List<UserEntity> users = userRepository.findAllById(ids)
                .stream()
                .takeWhile(user -> user.getId() != null)
                .toList();
        if (redisService.checkKeysExist(Collections.singleton(idsAsList))){
            String keysStringValues = ids.stream().map(String::valueOf).toString();
            Set<String> userIdsKeys = Collections.singleton(keysStringValues);
            redisService.writeObjectsInRedis(userIdsKeys, new HashSet<>(users));
            return userMapper.mapToDtoList(users);
        }
        return userMapper.mapToDtoList(users);
    }

    @Override
    @Transactional
    @TimeResulting

    public UserResponse createUser(UserRequest userRequest) {
            UserEntity user = new UserEntity();
            user.setUsername(userRequest.getUsername());
            if (userRepository.existsByUsernameOrPhoneNumber(
                    userRequest.getUsername(),
                    userRequest.getPhoneNumber())) {
                throw new AlreadyExistException("[User already exists!]");
            }
            user.setPassword(userRequest.getPassword());
            user.setPhoneNumber(userRequest.getPhoneNumber());
            user.setCreatedAt(user.getCreatedAt());
            user.setUpdatedAt(LocalDateTime.now());
            user.setStatus(UserStatus.NEW.name());
            userRepository.save(user);
            var cacheKey = String.valueOf(user.getId());
            redisService.writeObjectInRedis(cacheKey, user);
            publishEvent(user);
            updateBalanceOfUser(user.getId(), BigDecimal.valueOf(1000.1));
            return userMapper.mapToDto(user);
    }


    public void publishEvent(UserEntity user){
        eventPublisher.publishEvent(new UserCreateEvent(
                this,
                String.valueOf(user.getId()),
                user.getPhoneNumber(),
                LocalDateTime.now(),
                user.getUsername()
        ));
    }





    @Override
    @Transactional
    public void updateUser(Long id, UserRequest userRequest) {
       userRepository.findById(id)
               .ifPresentOrElse(user->{
                    isSucessfullValid(userRequest,user);
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    redisService.writeObjectInRedis(user.getId().toString(),user);
                    eventPublisher.publishEvent(new UserUpdateEvent(
                            this,
                            user.getUsername(),
                            user.getPhoneNumber(),
                            user.getUpdatedAt(),
                            String.valueOf(id)));
                    log.info("Updated user: {}",user.getUsername());
                },  ()->{
                    throw new UserNotFoundException(MessageFormat.format("[User with id: {0} not found]",id));
                }
                );

    }

    @Async
    public void isSucessfullValid(UserRequest userRequest,UserEntity user){
        if (userRequest.getUsername() != null && !userRequest.getUsername().isEmpty()) {
            user.setUsername(userRequest.getUsername());
            log.info("[Updated username: {}]",userRequest.getUsername());
        }
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(userRequest.getPassword());
        }
        if (userRequest.getPhoneNumber() != null && !userRequest.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(userRequest.getPhoneNumber());
            log.info("[Updated phoneNumber: {}]",userRequest.getPhoneNumber());
        }
    }

    @Override
    public void banUserById(Long id) {
        var requireUser = userRepository.findById(id)
                .stream()
                .peek(user-> user.setStatus(UserStatus.BANNED.name()))
                .findAny()
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("[User with id: {0} not found]",id)));
        requireUser.setBanTime(LocalDateTime.now());
        userRepository.save(requireUser);
        var cacheKey = requireUser.getId().toString();
        redisService.deleteCacheByKey(cacheKey);
        log.info("[Banned user is: {}]",requireUser.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUsersByStatus(String status) {
        Map<String,Object> usersAndTheirStatuses = userRepository.findAllByStatus(status)
                .stream()
                .collect(Collectors.filtering(
                        user -> UserStatus.NEW.name().equalsIgnoreCase(user.getStatus()),
                        Collectors.toMap(
                                UserEntity::getUsername,
                                UserEntity::getStatus)));

        var userKeys = new HashSet<>(usersAndTheirStatuses.keySet());
        var userValues = new HashSet<>(usersAndTheirStatuses.values());

        if (redisService.checkKeysExist(userKeys)){
            redisService.writeObjectsInRedis(userKeys,userValues);
        }
        log.info("[Users by status: {}]",usersAndTheirStatuses);
        return UserResponse.builder()
                .users(usersAndTheirStatuses)
                .build();
    }

    @Override
    @TimeResulting
    public UserResponse getUsersCountsOfDeals() {
        return UserResponse.builder()
                .users(userRepository.findAll()
                .stream()
                .takeWhile(user->!Objects.equals(user.getCountOfDeals(),null))
                .peek(user -> {
                    var cacheKey = user.getId().toString();
                    if (!redisService.checkKeyExist(cacheKey)) {
                        redisService.writeObjectInRedis(cacheKey, user);
                    }}).collect(Collectors.toMap(
                        UserEntity::getUsername,
                        UserEntity::getCountOfDeals)))
                .build();
    }

    @Override
    public UserResponse getUserByOrderId(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(
                        MessageFormat.format("[Order with id: {0} not found]",orderId)));
        UserEntity user = order.getUser();
        if (user == null) {
            throw new UserNotFoundException(
                    MessageFormat.format("[User not found for order with id: {0} ]",orderId));
        }
        String cacheKey = user.getId().toString();
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

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCardsOfUser(Long userId) {
        Map<String,Set<CardResponse>> cardMap = new ConcurrentHashMap<>();
        var cards = cardRepository.findByUserId(userId)
                .stream()
                .map(CardEntity::getUser)
                .filter(user -> user.getId().equals(userId))
                .map(UserEntity::getCards)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        String username = cards.stream()
                .map(CardEntity::getUser)
                .map(UserEntity::getUsername)
                .findAny()
                .orElseThrow(RuntimeException::new);
        Set<CardEntity> sortedCards = cards.stream()
                .sorted(Comparator.comparing(CardEntity::getDate)
                        .thenComparing(CardEntity::getFio))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var mapValue = cardMapper.mapToDtoSet(sortedCards);
        cardMap.put(username,mapValue);
        return UserResponse.builder()
                .cards(cardMap)
                .build();
    }


    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        MessageFormat.format("[User with id: {0} not found]", id)));
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        String cacheKey = String.valueOf(id);
        CompletableFuture<Void> completableFutureComments = CompletableFuture.runAsync(()->{
        if (user.getComments() != null) {
            user.getComments().forEach(comment -> comment.setUser(null));
            commentRepository.saveAll(user.getComments());
            user.getComments().clear();
        }},executorService);
        CompletableFuture<Void> completableFutureCards = CompletableFuture.runAsync(()->{
        if (user.getCards() != null) {
            user.getCards().forEach(card -> card.setUser(null));
            cardRepository.saveAll(user.getCards());
            user.getCards().clear();
        }},executorService);
        CompletableFuture<Void> completableFutureOrders = CompletableFuture.runAsync(()->{
        if (user.getOrders() != null) {
            user.getOrders().forEach(order -> order.setUser(null));
            orderRepository.saveAll(user.getOrders());
            user.getOrders().clear();
        }},executorService);
        CompletableFuture<Void> completableFutureNotifications = CompletableFuture.runAsync(()->{
        if (user.getNotifications() != null) {
            user.getNotifications().forEach(notificationEntity -> notificationEntity.setUser(null));
            notificationRepository.saveAll(user.getNotifications());
            user.getNotifications().clear();
        }},executorService);
        CompletableFuture<Void> completableFutureTransactions = CompletableFuture.runAsync(()->{
        if (user.getTransactionEntity() != null) {
            user.getTransactionEntity().forEach(tx->tx.setUser(null));
            transactionRepository.saveAll(user.getTransactionEntity());
        }},executorService);
        CompletableFuture<Void> futures = CompletableFuture.allOf(
                completableFutureCards,
                completableFutureComments,
                completableFutureOrders,
                completableFutureNotifications,
                completableFutureTransactions);
        futures.thenRunAsync(()-> {
            userRepository.delete(user);
            redisService.deleteCacheByKey(cacheKey);
        }).join();
    }



    @Transactional
    @Override
    public void deleteMultipleUsers(List<Long> ids) {
        var users = userRepository.findAllById(ids);
        var userTransactionIds = users.stream()
                         .map(UserEntity::getTransactionEntity)
                         .flatMap(Collection::stream)
                         .map(TransactionEntity::getId)
                         .toList();
        userRepository.deleteAllByIdInBatch(ids);
        transactionRepository.deleteAllByIdInBatch(userTransactionIds);
        log.info("[Deleted users: {}]",users);

    }



    @Override
    @Transactional
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
        String cacheKeys = ids.stream().map(String::valueOf).toString();
        Set<String> cacheKeysListValue = Collections.singleton(cacheKeys);
        Set<Object> userValues = new HashSet<>(usersForUpdate);
        redisService.writeObjectsInRedis(cacheKeysListValue,userValues);
        log.info("[UpdatedUsers: {}]",usersForUpdate);
    }

    @Override
    public void addCard(UserEntity user, CardEntity card) {
        List<CardEntity> cards = user.getCards();
        if (!Objects.equals(card,null)){
            cards.add(card);
            log.info("[Added card: {},cardList: {}]",card,cards);
        }

    }

    @Override
    public void addOrder(UserEntity user, OrderEntity order) {
        List<OrderEntity> orders = user.getOrders();
        if (!Objects.equals(order,null)){
            orders.add(order);
            log.info("[Added order: {}, orders: {}]",order,orders);
        }
    }

    @Override
    public void addComment(UserEntity user, CommentEntity comment) {
        List<CommentEntity> comments = user.getComments();
        if (!Objects.equals(comment,null)){
            comments.add(comment);
            log.info("[Added comment: {}, comments: {}]",comment,comments);
        }

    }

    @Override
    public boolean isExpired(UserEntity user) {
        return ChronoUnit.MINUTES
                .between(user.getCreatedAt(),LocalDateTime.now())>1;
    }

    @EventListener
    @Override
    public void handleUserCreateEvent(UserCreateEvent userCreateEvent) {
        redisService.writeObjectInRedis(userCreateEvent.getUserId(),
                userCreateEvent.toString());
        log.info("[Cached event: {}]",userCreateEvent.getUsername());
        kafkaService.sendMessage(userCreateEvent.toString());
    }

    @Override
    @EventListener
    @TimeResulting
    public void handleUserUpdateEvent(UserUpdateEvent userUpdateEvent) {
        Stream.of(userUpdateEvent)
                .peek(user-> {
                    user.setIsUpdate(true);
                    user.setTimeOfUpdating(userUpdateEvent.getTimeOfUpdating());
                    log.info("[Time of update is: {}]",user.getTimeOfUpdating());
                    redisService.writeObjectInRedis(userUpdateEvent.getEventId(),userUpdateEvent);

                })
                .forEach(user->log.info("[Updated event: {}, event id: {} isUpdate: {}]",
                        userUpdateEvent.getEventType(),
                        userUpdateEvent.getEventId(),
                        userUpdateEvent.getIsUpdate()));
    }

    @Override
    @Async
    public void updateBalanceOfUser(Long id, BigDecimal value) {
        userRepository.updateBalanceOfUser(id,value);
    }

    @Override
    public BigDecimal getBalanceOfUser(String email) {
        return userRepository.findByEmail(email)
                .stream()
                .map(UserEntity::getBalance)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    @Override
    public UserResponse getUserTransactions(Long userId) {
        List<TransactionEntity> transactions = transactionRepository.findByUserId(userId);
        Map<String,List<TransactionEntity>> transactionMap = new HashMap<>();
        var user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("[User with id: {0} not found]",userId)
                ));
        BigDecimal txAmount = transactions.stream()
                .map(TransactionEntity::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        List<TransactionEntity> txWithAmountValue = transactions.stream()
                .peek(tx-> tx.setTotalAmount(txAmount))
                .filter(tx->tx.getTotalAmount()!=null)
                .toList();
        String mapKey = user.getUsername();
        log.info("[Key is: {}]",mapKey);
        transactionMap.put(mapKey,txWithAmountValue);
        return UserResponse.builder()
                .txMap(transactionMap)
                .build();
    }

    @Override
    public UserResponse findByEmail(String email) {
       return userMapper.mapToDto(userRepository.findByEmail(email)
               .orElseThrow(UserNotFoundException::new));
    }

    @EventListener
    public void handlePaymentEvent(PaymentEvent paymentEvent){
        log.info("[Amount is : {}]",paymentEvent.getAmount());
        String email = paymentEvent.getEmail();
        var user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        final BigDecimal balance = user.getBalance();
        var card = cardRepository.findByCardNumber(paymentEvent.getCardNumber())
                .stream()
                .filter(cardEntity -> cardEntity.getUser()
                        .getId()
                        .equals(user.getId()))
                .findAny()
                .orElseThrow(CardNotFoundException::new);
        var balanceOfCard = card.getBalance();
        var amount = paymentEvent.getAmount();
        var totalBalanceOfCard = balanceOfCard.subtract(amount);
        log.info("[Balance of card: {} is: {}, balance of user: {}]",
                card.getId(),
                totalBalanceOfCard,
                user.getBalance());
        card.setBalance(balanceOfCard);
        cardRepository.save(card);
        log.info("[Total balance of card: {}]",totalBalanceOfCard);
        final BigDecimal totalBalance = balance.subtract(paymentEvent.getAmount());
        user.setBalance(totalBalance);
        if (totalBalance.compareTo(BigDecimal.ZERO)<0){
            throw new RuntimeException("Недостаточно средств");
        }
        userRepository.save(user);
        log.info("[Total Balance of user is: {}]",totalBalance);
    }

}
