package dn.jasm.exception;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record ErrorBody (@JsonProperty(value = "code")
                         int statusCode,
                         String description,
                         String path){

}
