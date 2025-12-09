package dn.jasm.mapper;

import dn.jasm.dto.shop.ShopRequest;
import dn.jasm.dto.shop.ShopResponse;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.ShopEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class ShopMapper {

    private final UserRepository userRepository;

    private static final String DATE_PATTERN = "dd-MM-yyyy";


    public ShopEntity mapToEntity(ShopRequest shopRequest){
        ShopEntity shop = new ShopEntity();
        UserEntity owner = userRepository.findById(shopRequest.getOwnerId())
                .orElseThrow(UserNotFoundException::new);
        shop.setId(shop.getId());
        shop.setUser(owner);
        shop.setName(shopRequest.getName());
        shop.setOwnerName(owner.getUsername());
        shop.setRating(shopRequest.getRating());
        shop.setCountOfSales(shop.getCountOfSales());
        shop.setCreatedAt(LocalDateTime.now());
        shop.setDeposit(BigDecimal.valueOf(1000.0));
        shop.setUpdatedAt(LocalDateTime.now());
        return shop;
    }

    public ShopResponse mapToDto(ShopEntity shop){
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .category(shop.getCategory())
                .createdAt(shop.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern(DATE_PATTERN)))
                .isActive(true)
                .isVerified(true)
                .rating(shop.getRating())
                .shopStatus(shop.getStatus().name())
                .ownerName(shop.getUser().getUsername())
                .updatedAt(shop.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern(DATE_PATTERN)))
                .description(shop.getDescription())
                .totalCashTurnover(BigDecimal.ZERO)
                .itemsIds(new ArrayList<>())
                .buyersIds(new ArrayList<>(1))
                .countOfSales(0)
                .reviewCounts(0)
                .build();
    }


}
