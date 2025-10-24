package dn.jasm.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
public class PaymentResponse {

    private String id;
    private String status;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PaymentResponse{");
        sb.append("id='").append(id).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
