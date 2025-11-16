package dn.jasm.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import dn.jasm.entity.enums.CardType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(schema = "jasm",name = "card")
@Getter
@Setter
public class CardEntity extends BasedEntity {

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-cards")
    private UserEntity user;

    private String fio;

    @OneToMany(mappedBy = "card",fetch = FetchType.LAZY)
    @JsonManagedReference("card-payments")
    private List<PaymentEntity> paymentEntity = new ArrayList<>();

    @Column(nullable = false)
    private String cvc;

    private String stripePaymentMethodId;

    @Column
    private BigDecimal balance;

    @Column(name = "card_number", nullable = false,unique = true)
    private String cardNumber;

    @DateTimeFormat(pattern = "Mm/Yy")
    private LocalDateTime date;

    private Long expMonth;

    private Long expYear;

    @OneToMany(mappedBy = "card",fetch = FetchType.LAZY)
    @JsonManagedReference("card-transactions")
    private Set<TransactionEntity> transactions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")

    private CardType cardType;

    private Integer transactionsCount;

    @Override
    public String toString() {
        return "CardEntity{" +
                "cvc='" + cvc + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", date=" + date +
                '}';
    }
}
