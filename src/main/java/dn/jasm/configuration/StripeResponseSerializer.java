package dn.jasm.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.stripe.net.StripeResponse;

import java.io.IOException;

public class StripeResponseSerializer extends StdSerializer<StripeResponse> {

    public StripeResponseSerializer(){
        super(StripeResponse.class);
    }
    @Override
    public void serialize(StripeResponse value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();;
        gen.writeStringField("body",value.body());
        gen.writeNumberField("code",value.code());
        gen.writeEndObject();
    }
}
