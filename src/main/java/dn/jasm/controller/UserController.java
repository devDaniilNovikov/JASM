package dn.jasm.controller;
import dn.jasm.configuration.swagger.user.SwaggerAnnotationForUser;
import dn.jasm.configuration.swagger.user.SwaggerAnnotationForUserCollection;
import dn.jasm.dto.user.UserRequest;
import dn.jasm.dto.user.UserResponse;
import dn.jasm.dto.user.UserResponseList;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "User" ,description = "Действия с пользователем")
public class UserController {

    private static final String CREATE_USER = "/api/v1/user/create";
    private static final String DELETE_USER = "/api/v1/user/delete";
    private static final String GET_MULTIPLE_USERS_BY_IDS = "/api/v1/user/search";
    private static final String DELETE_USERS_BY_IDS = "/api/v1/users/delete";
    private static final String GET_ALL_USERS = "/api/v1/user/users-list";
    private static final String GET_USER_BY_ID = "/api/v1/user/{id}";
    private static final String GET_BY_USERNAME = "/api/v1/user";
    private static final String BAN_USER_BY_ID = "/api/v1/user/ban";
    private static final String GET_USERS_BY_STATUS = "/api/v1/user/users/status";
    private static final String GET_USER_BY_PHONE_NUMBER = "/api/v1/user/by-phoneNumber";
    private static final String UPDATE_USER = "/api/v1/user/update";
    private static final String GET_USER_COUNTS_OF_DEALS  = "/api/v1/users/deals/count";
    private static final String GET_USER_BY_ORDER_ID = "/api/v1/user/by-orderId";
    private static final String GET_USER_WITH_NOT_NULL_COUNT_OF_DEALS = "/api/v1/users/deals";
    private static final String GET_TRANSACTIONS_OF_USER = "/api/v1/user/transactions";
    private static final String GET_CARDS_OF_USER = "/api/v1/user/cards";
    private static final String GET_BALANCE_OF_USER = "/api/v1/user/balance";

    private final UserService userService;

    @GetMapping(GET_BALANCE_OF_USER)
    public BigDecimal getBalanceOfUser(@RequestParam String email){
        return userService.getBalanceOfUser(email);
    }

    @GetMapping(GET_CARDS_OF_USER)
    public UserResponse getCardList(@RequestParam Long userId){
        return userService.getCardsOfUser(userId);
    }

    @GetMapping(GET_TRANSACTIONS_OF_USER)
    public UserResponse getUserTransactions(@RequestParam Long userId){
        return userService.getUserTransactions(userId);
    }

    @GetMapping(GET_USER_WITH_NOT_NULL_COUNT_OF_DEALS)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUserCollection(operation = "Получение списка пользователей, у которых есть хотя бы одна покупка")
    public UserResponse findAllWithCountOfDealsGreatherThanNull(){
        return userService.findAllWithCountOfDealsGreatherThanNull();
    }

    @GetMapping(GET_USER_BY_ORDER_ID)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Получение пользователя по уникальному идентификатору заказа")
    public UserResponse getUserByOrderId(@RequestParam Long orderId){
        return userService.getUserByOrderId(orderId);
    }

    @GetMapping(GET_USER_COUNTS_OF_DEALS)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @SwaggerAnnotationForUser(operation = "Получение пользователя и его количество сделок")
    public UserResponse getCountsOfDealsOfUsers(){
        return userService.getUsersCountsOfDeals();
    }

    @GetMapping(GET_USER_BY_PHONE_NUMBER)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Получение пользователя по его номеру телефона")
    public UserResponse getByPhoneNumber(@RequestParam @Valid String phoneNumber){
        return userService.findByPhoneNumber(phoneNumber);
    }

    @PatchMapping(value = UPDATE_USER)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Обновление пользователя по его уникальному идентификатору")
    public void updateUser(@RequestParam Long id, @RequestBody UserRequest userRequest){
         userService.updateUser(id,userRequest);
    }

    @PatchMapping(value = BAN_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Бан пользователя по его уникальному идентификатору")
    public void banUserById(@RequestParam Long id){
        userService.banUserById(id);
    }

    @GetMapping(GET_USERS_BY_STATUS)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Получение пользователя по его статусу")
    public UserResponse getUsersByStatus(@RequestParam String status){
        return userService.getUsersByStatus(status);
    }

    @GetMapping(GET_BY_USERNAME)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Получение пользователя по его никнейму")
    public UserResponse getByUsername(@RequestParam String username){
        return userService.findByUsername(username);
    }

    @GetMapping(GET_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Получение пользователя по его уникальному идентификатору")
    public UserResponse getUserById(@PathVariable Long id){
        return userService.findById(id);
    }

    @GetMapping(GET_ALL_USERS)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUserCollection(operation = "Получение списка пользователей постгранично")
    public UserResponseList getAllWithPagination(@RequestParam int pageNumber,
                                                 @RequestParam int pageSize){
        return userService.findAllWithPagination(pageNumber,pageSize);
    }

    @GetMapping(GET_MULTIPLE_USERS_BY_IDS)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUserCollection(operation = "Получение списка пользователей по их уникальным идентификаторам")
    public UserResponseList getUserByIds(@RequestParam List<Long> ids){
        return userService.findAllByIds(ids);
    }

    @PostMapping(CREATE_USER)
    @ResponseStatus(HttpStatus.CREATED)
    @SwaggerAnnotationForUser(operation = "Создание пользователя")
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest){
       return userService.createUser(userRequest);
    }

    @DeleteMapping(DELETE_USER)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForUser(operation = "Удаление пользователя по его уникальному идентификатору")
    public void deleteUser(@RequestParam Long id){
        userService.deleteUser(id);
    }

    @DeleteMapping(DELETE_USERS_BY_IDS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForUser(operation = "Удаление пользователей по их уникальным идентификаторам")
    public void deleteUsersByIds(@RequestParam List<Long> ids){
        userService.deleteMultipleUsers(ids);
    }
}
