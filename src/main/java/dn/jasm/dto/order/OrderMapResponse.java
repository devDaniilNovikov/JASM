package dn.jasm.dto.order;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class OrderMapResponse {

    private Map<String, ListOrderResponse> orderMap = new HashMap<>();
}
