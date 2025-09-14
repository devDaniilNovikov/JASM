package dn.jasm.dto.card;

import dn.jasm.entity.enums.CardType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {

    @NotNull
    @Size(min = 16,max = 16,message = "Invalid card-number")
    private String cardNumber;
    @NotNull
    @Size(min = 3,max = 3)
    private String cvc;
    @NotNull
    private String fio;
    @NotNull
    private CardType cardType;


}
