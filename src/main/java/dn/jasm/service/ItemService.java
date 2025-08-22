package dn.jasm.service;

import com.stripe.model.Price;
import com.stripe.model.Product;
import dn.jasm.entity.ItemEntity;

import java.util.List;

public interface ItemService {

    ItemEntity createItem(Price price, Product product);



}
