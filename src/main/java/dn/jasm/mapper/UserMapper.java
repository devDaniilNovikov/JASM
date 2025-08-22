package dn.jasm.mapper;
import dn.jasm.entity.UserEntity;
import dn.jasm.dto.user.UserResponse;
import dn.jasm.dto.user.UserResponseList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface UserMapper extends Mappable<UserEntity, UserResponse> {

    default UserResponseList toList(List<UserEntity> userEntityList){
        UserResponseList userResponseList = new UserResponseList();
        userResponseList.setUsers(userEntityList.stream()
                .map(this::toDto)
                .toList());
        return userResponseList;
    }

    @Override
    @Mappings({
            @Mapping(target = "password", source = "password", ignore = true),
            @Mapping(target = "users", ignore = true),
            @Mapping(target = "userStatus", source = "status"),
            @Mapping(target = "comments",source = "comments",ignore = true)
    })
    UserResponse toDto(UserEntity entity);

    default String mapUserIdToString(Long id){
        return Objects.toString(id);
    }
}
