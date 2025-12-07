package dn.jasm.service;

import com.stripe.model.Price;
import com.stripe.model.Product;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.entity.ItemEntity;

import java.util.List;

public interface ItemService {


    void addItem(ItemRequest itemRequest);

    ItemResponse getItemById(Long itemId);



}
