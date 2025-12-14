package dn.jasm.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Schema(name = "OrderMapResponse", description = "Список заказов по имени пользователя")
public class OrderMapResponse {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, ListOrderResponse> orderMap = new HashMap<>();
}
