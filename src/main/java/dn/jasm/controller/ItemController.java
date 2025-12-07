package dn.jasm.controller;


import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private static final String ADD_ITEM = "/api/v1/items/add";
    private static final String GET_ITEM_BY_ID = "/api/v1/items/{id}";
    private static final String UPDATE_ITEM = "/api/v1/items/update";
    private static final String DELETE_ITEM = "/api/v1/items/item";
    private static final String GET_ITEM_LIST = "/api/v1/items/list";
    private static final String GET_ITEMS_BY_IDS = "/api/v1/items";
    private static final String DELETE_MULTIPLE_ITEMS = "/api/v1/items/delete";
    private static final String BACK_ITEM_TO_STORAGE = "/api/v1/items/item/back-to-storage";



    @PostMapping(ADD_ITEM)
    public void addItem(@RequestBody ItemRequest itemRequest){
        itemService.addItem(itemRequest);
    }

    @GetMapping(GET_ITEM_BY_ID)
    public ItemResponse getItemById(@PathVariable Long id){
        return itemService.getItemById(id);
    }
}
