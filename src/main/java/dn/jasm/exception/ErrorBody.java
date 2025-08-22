package dn.jasm.exception;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorBody {

    @JsonProperty(value = "code")
    private int statusCode;
    private String description;
    private String path;
}
