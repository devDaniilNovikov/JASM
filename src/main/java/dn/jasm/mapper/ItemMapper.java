package dn.jasm.mapper;

import dn.jasm.dto.item.ItemRequest;
import dn.jasm.entity.ItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper extends Mappable<ItemEntity, ItemRequest>{
}
