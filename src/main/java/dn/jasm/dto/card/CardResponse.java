package dn.jasm.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import dn.jasm.entity.BasedEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CardResponse {

    private Long id;
    private String cardNumber;
    private String cvc;
    private Long expMonth;
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
