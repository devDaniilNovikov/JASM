package dn.jasm.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopUpdateRequest {


    private Long shopId;
    private String name;
    private String description;
    private Long ownerId;
    private BigDecimal deposit;
    private String category;
    private String dateOfUpdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-Mm-Dd"));
}
