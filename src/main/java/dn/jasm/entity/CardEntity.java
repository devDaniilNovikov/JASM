package dn.jasm.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(schema = "jasm",name = "card")
@Getter
@Setter
public class CardEntity extends BasedEntity {

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "card",fetch = FetchType.LAZY)
    private List<PaymentEntity> paymentEntity = new ArrayList<>();

    @Column(nullable = false)
    private Integer cvc;

    @Column(name = "card_number", nullable = false,length = 16,unique = true)
    private Integer cardNumber;

    @DateTimeFormat(pattern = "Mm/Yy")
    private Date date;
}
