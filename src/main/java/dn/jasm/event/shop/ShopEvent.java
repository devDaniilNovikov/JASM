package dn.jasm.event.shop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ShopEvent extends ApplicationEvent {

    private String ownerId;
    private String shopId;
    private String shopName;

    public ShopEvent(Object source,
                     String ownerId,
                     String shopId,
                     String shopName) {
        super(source);
        this.ownerId = ownerId;
        this.shopId = shopId;
        this.shopName = shopName;

    }
}
