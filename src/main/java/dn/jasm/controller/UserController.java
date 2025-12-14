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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "User" ,description = "Действия с пользователем")
public class UserController {

    private static final String CREATE_USER = "/api/v1/users/user/create";
    private static final String DELETE_USER = "/api/v1/users/user/delete";
    private static final String GET_MULTIPLE_USERS_BY_IDS = "/api/v1/users/search";
    private static final String DELETE_USERS_BY_IDS = "/api/v1/users/delete";
    private static final String GET_ALL_USERS = "/api/v1/users/users-list";
    private static final String GET_USER_BY_ID = "/api/v1/users/users/{id}";
    private static final String GET_BY_USERNAME = "/api/v1/users/user-by-name/{username}";
    private static final String BAN_USER_BY_ID = "/api/v1/users/user/ban";
    private static final String GET_USERS_BY_STATUS = "/api/v1/users/users/status";
    private static final String GET_USER_BY_PHONE_NUMBER = "/api/v1/users/user/by-phoneNumber";
    private static final String UPDATE_USER = "/api/v1/users/user/update";
    private static final String GET_USER_COUNTS_OF_DEALS  = "/api/v1/users/deals/count";
    private static final String GET_USER_BY_ORDER_ID = "/api/v1/users/user/by-orderId";
    private static final String GET_USER_WITH_NOT_NULL_COUNT_OF_DEALS = "/api/v1/users/deals";
    private static final String GET_TRANSACTIONS_OF_USER = "/api/v1/users/user/transactions";
    private static final String GET_CARDS_OF_USER = "/api/v1/users/user/cards";
    private static final String GET_BALANCE_OF_USER = "/api/v1/users/user/balance";
    private static final String GET_STATS = "/api/v1/users/user/stats";
    private static final String GET_BY_EMAIL = "/api/v1/users/user/{email}";
    private static final String PAGE_SIZE_DEFAULT_VALUE = "10";
    private static final String PAGE_NUMBER_DEFAULT_VALUE = "0";

    private final UserService userService;

    @GetMapping(GET_BY_EMAIL)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Получение пользователя по его почте")
    public UserResponse getByEmail(@PathVariable String email){
        return userService.findByEmail(email);
    }

    @GetMapping(GET_BALANCE_OF_USER)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Получение баланса по его номеру телефона")
    public BigDecimal getBalanceOfUser(@RequestParam String email){
        return userService.getBalanceOfUser(email);
    }

    @GetMapping(GET_CARDS_OF_USER)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUserCollection(operation = "Получение банковских карта пользователей")
    public UserResponse getCardList(@RequestParam Long userId){
        return userService.getCardsOfUser(userId);
    }

    @GetMapping(GET_TRANSACTIONS_OF_USER)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUserCollection(operation = "Получение транзакцией пользователей")
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
    @SwaggerAnnotationForUser(operation = "Получение пользователя по его заказу")
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
    @SwaggerAnnotationForUser(operation = "Обновление данных пользователя")
    public void updateUser(@RequestParam Long id,
                           @RequestBody UserRequest userRequest){
         userService.updateUser(id,userRequest);
    }

    @PatchMapping(value = BAN_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Бан пользователя")
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
    public UserResponse getByUsername(@PathVariable String username){
        return userService.findByUsername(username);
    }

    @GetMapping(GET_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUser(operation = "Получение пользователя")
    public UserResponse getUserById(@PathVariable Long id){
        return userService.findById(id);
    }

    @GetMapping(GET_ALL_USERS)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUserCollection(operation = "Получение списка пользователей с пагинацией")
    public UserResponseList getAllWithPagination(@RequestParam(defaultValue = PAGE_NUMBER_DEFAULT_VALUE) int pageNumber,
                                                 @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_VALUE) int pageSize){
        return userService.findAllWithPagination(pageNumber,pageSize);
    }

    @GetMapping(GET_MULTIPLE_USERS_BY_IDS)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForUserCollection(operation = "Получение списка  пользователей")
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
    @SwaggerAnnotationForUser(operation = "Удаление пользователя")
    public void deleteUser(@RequestParam Long id){
        userService.deleteUser(id);
    }

    @DeleteMapping(DELETE_USERS_BY_IDS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForUser(operation = "Удаление нескольких пользователей")
    public void deleteUsersByIds(@RequestParam List<Long> ids){
        userService.deleteMultipleUsers(ids);
    }

}
