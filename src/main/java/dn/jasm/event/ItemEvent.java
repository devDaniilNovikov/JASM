package dn.jasm.event;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
public final class ItemEvent extends ApplicationEvent {
        private final String id;
        private final String name;
        private final BigDecimal price;
        private final Integer quantity;
        private final String description;
        private final Long shopId;

    public ItemEvent(Object source,
                     String id,
                     String name,
                     BigDecimal price,
                     Integer quantity,
                     String description,
                     Long shopId) {
        super(source);
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.shopId = shopId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("price", price)
                .append("quantity", quantity)
                .append("description", description)
                .append("shopId", shopId)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemEvent itemEvent)) return false;

        return id.equals(itemEvent.id) && shopId.equals(itemEvent.shopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
