package dn.jasm.mapper;

import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.dto.item.ListItemResponse;
import dn.jasm.entity.ItemEntity;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ItemMapper {



    public ItemEntity mapToEntity(ItemRequest itemRequest){
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setName(itemRequest.getName());
        itemEntity.setPrice(itemEntity.getPrice());
        itemEntity.setQuantity(itemRequest.getQuantity());
        itemEntity.setDescription(itemRequest.getDescription());
        itemEntity.setIsShippable(itemRequest.getIsShippable());
        return itemEntity;
    }

    public ItemRequest mapToItemRequest(ItemEntity itemEntity){
        return ItemRequest.builder()
                .name(itemEntity.getName())
                .description(itemEntity.getDescription())
                .price(itemEntity.getPrice())
                .quantity(itemEntity.getQuantity())
                .isShippable(itemEntity.getIsShippable())
                .build();
    }

    public ItemResponse mapToDto(ItemEntity itemEntity) {
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setId(itemEntity.getId());
        itemResponse.setName(itemEntity.getName());
        itemResponse.setDescription(itemEntity.getDescription());
        itemResponse.setRating(itemEntity.getRating());
        itemResponse.setDiscount(itemEntity.getDiscount());
        itemResponse.setPrice(itemEntity.getPrice());
        itemResponse.setQuantity(itemEntity.getQuantity());
        itemResponse.setIsShippable(itemEntity.getIsShippable());
        return itemResponse;
    }

    public List<ItemEntity> mapToEntityList(List<ItemRequest> itemRequest){
        return itemRequest.stream()
                .map(this::mapToEntity)
                .toList();
    }

    public List<ItemResponse> mapToDtoList(List<ItemEntity> items){
        return items.stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<ItemRequest> mapToItemRequestList(List<ItemEntity> items){
        return items.stream()
                .filter(Objects::nonNull)
                .map(this::mapToItemRequest)
                .toList();
    }

    public ListItemResponse mapToItemListDto(List<ItemEntity> items){
        ListItemResponse listItemResponse = new ListItemResponse();
        listItemResponse.setItems(items
                .stream()
                .filter(Objects::nonNull)
                .map(this::mapToDto)
                .toList());
        return listItemResponse;
    }

}
