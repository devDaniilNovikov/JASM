package dn.jasm.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import dn.jasm.entity.BasedEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Schema(name = "CardResponse", description = "ДТО добавленной карты")
public class CardResponse {

    @Schema(name = "id", description = "Уникальный идентификатор добавленной карты")
    private Long id;
    @Schema(name = "CardResponse", description = "Номер  добавленной карты")
    private String cardNumber;
    @Schema(name = "CardResponse", description = "Трехзначный код добавленной карты")
    private String cvc;
    @Schema(name = "CardResponse", description = "Месяц истечения срока действия добавленной карты")
    private Long expMonth;
    @Schema(name = "CardResponse", description = "Год истечения срока действия добавленной карты")
    private Long expYear;

    @Override
    public String toString() {
        return "CardResponse{" +
                "id='" + id + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", cvc='" + cvc + '\'' +
                ", expMonth=" + expMonth +
                ", expYear=" + expYear +
                '}';
    }
}
