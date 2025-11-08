package dn.jasm.mapper;

import dn.jasm.dto.user.UserResponse;
import dn.jasm.dto.user.UserResponseList;
import dn.jasm.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final CardMapper cardMapper;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;


    public UserResponse mapToDto(UserEntity user){
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .userStatus(user.getStatus())
                .build();
    }

    public UserEntity mapToEntity(UserResponse userResponse){
        UserEntity user = new UserEntity();
        user.setId(userResponse.getId());
        user.setEmail(userResponse.getEmail());
        user.setBalance(userResponse.getBalance());
        user.setCreatedAt(LocalDateTime.now());
        user.setUsername(userResponse.getUsername());
        user.setPhoneNumber(user.getPhoneNumber());
        return user;
    }

    public UserResponseList mapToDtoList(List<UserEntity> users){
        UserResponseList userResponseList = new UserResponseList();
        userResponseList.setUsers(users.stream()
                .map(this::mapToDto)
                .toList());
        return userResponseList;
    }
}
