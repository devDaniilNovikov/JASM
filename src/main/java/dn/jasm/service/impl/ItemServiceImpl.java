package dn.jasm.service.impl;

import com.stripe.model.Price;
import com.stripe.model.Product;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;
import dn.jasm.repository.ItemRepository;
import dn.jasm.repository.OrderRepository;
import dn.jasm.service.ItemService;
import dn.jasm.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;


    @Override
    public ItemEntity createItem(Price price, Product product) {
        ItemEntity item = new ItemEntity();
        item.setName(product.getName());
        item.setPrice(price.getUnitAmountDecimal());
        item.setType(product.getType());
        itemRepository.save(item);
        log.info("[Created item: {}]", item.getName());
        return item;
    }

    @Override
    public ItemEntity addItem(ItemRequest itemRequest){
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setName(itemRequest.getName());
        itemEntity.setIsShippable(true);
        itemEntity.setDescription(itemRequest.getDescription());
        itemEntity.setPrice(itemRequest.getPrice());
        itemEntity.setQuantity(itemRequest.getQuantity());
        itemRepository.save(itemEntity);
        return itemEntity;
    }


}


