package dn.jasm.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity

@Table(schema = "jasm",name = "user",indexes = {
        @Index(name = "username_idx",columnList = "username")})
@Getter
@Setter
public class UserEntity extends BasedEntity {

    private static final int BATCH_SIZE = 10;

    @Column(unique = true,length = 20,nullable = false)
    private String username;

    @Column(length = 50,nullable = false)
    private String password;

    @Column(unique = true,length = 11,nullable = false)
    private String phoneNumber;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = true,mappedBy = "user")
    @JsonManagedReference("user-transactions")
    @BatchSize(size = BATCH_SIZE)
    private List<TransactionEntity> transactionEntity = new ArrayList<>();



    private String status;

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @ToString.Exclude
    @BatchSize(size = BATCH_SIZE)
    @JsonManagedReference("user-orders")
    private List<OrderEntity> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @ToString.Exclude
    @BatchSize(size = BATCH_SIZE)
    @JsonManagedReference("user-shops")
    private List<ShopEntity> shops = new ArrayList<>();

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @ToString.Exclude
    @BatchSize(size = BATCH_SIZE)
    @JsonManagedReference("user-cards")
    private List<CardEntity> cards = new ArrayList<>();

    private Integer countOfDeals;

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    @BatchSize(size = BATCH_SIZE)
    @JsonManagedReference("user-comments")
    private List<CommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    @JsonManagedReference("user-notifications")
    private List<NotificationEntity> notifications = new ArrayList<>();


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime banTime;

    private String email;

    private BigDecimal balance;


    @Column(name = "payed_at")
    private Boolean isPayOnceOrder;

    public void addOrder(OrderEntity order){
        if (order!=null) orders.add(order);
        else orders = new ArrayList<>();
    }

    public void addCard(CardEntity card){
        if (card!=null) cards.add(card);
        else cards = new ArrayList<>();
    }

    public void addComment(CommentEntity comment){
        if (comment!=null) comments.add(comment);
        else comments = new ArrayList<>();
    }


    @Override
    public String toString() {
        return "UserEntity {" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UserEntity user = (UserEntity) object;
        return this.getId().equals(user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
