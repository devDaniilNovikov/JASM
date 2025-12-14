package dn.jasm.dto.card;

import dn.jasm.entity.enums.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CardRequest", description = "ДТО для добавления карты")
public class CardRequest {

    @NotNull
    @Size(min = 16,max = 16,message = "Invalid card-number")
    @Schema(name = "cardNumber", description = "Номер карты")
    private String cardNumber;
    @NotNull
    @Size(min = 3,max = 3)
    @Schema(name = "cvc", description = "Трехзначный код карты")
    private String cvc;
    @NotNull
    @Schema(name = "fio", description = "Фамилия, имя, отчество, человека на которого добавляется карта")
    private String fio;
    @Schema(name = "expMonth", description = "Месяц истечения срока действия карты")
    private Long expMonth;
    @Schema(name = "expYear", description = "Год истечения срока действия карты")
    private Long expYear;


}
