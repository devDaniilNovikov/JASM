package dn.jasm.controller;


import dn.jasm.dto.item.ItemRequest;
import dn.jasm.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private static final String ADD_ITEM = "/api/v1/items/add";


    @PostMapping(ADD_ITEM)
    public void addItem(@RequestBody ItemRequest itemRequest){
        itemService.addItem(itemRequest);
    }
}
