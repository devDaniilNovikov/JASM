package dn.jasm.mapper;

import dn.jasm.dto.user.UserResponse;
import dn.jasm.dto.user.UserResponseList;
import dn.jasm.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserMapper {


    public UserResponse mapToDto(UserEntity user){
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userStatus(user.getStatus())
                .build();
    }

    public UserResponseList mapToDtoList(List<UserEntity> users){
        UserResponseList userResponseList = new UserResponseList();
        userResponseList.setUsers(users.stream()
                .map(this::mapToDto)
                .toList());
        return userResponseList;
    }
}
