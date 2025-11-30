package dn.jasm.dto.shop;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopRequest {

    private String name;
    private String description;
    private Long ownerId;
    private String avatarUrl;
    private BigDecimal deposit = BigDecimal.ZERO;
    private String category;
    private Double rating = 0.0;
    private String dateOfRegistration = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-Mm-Dd"));
}
