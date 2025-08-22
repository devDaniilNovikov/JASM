package dn.jasm.mapper;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface Mappable <E,D>{

    E toEntity(D dto);

    D toDto(E entity);

    List<E> toEntityList(List<D> dto);

    List<D> toDtoList(List<E> entity);

    default String mapObjectToString(Long id){
        return Optional.of(Objects.toString(id)).orElseThrow(RuntimeException::new);
    }

    default List<Object> mapObjectToList(Object object){
        return Optional.of(Collections.singletonList(object)).orElseThrow(RuntimeException::new);
    }
}
