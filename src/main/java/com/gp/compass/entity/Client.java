 package com.gp.compass.entity;

 import java.time.LocalDateTime;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;

 import jakarta.persistence.*;
 import lombok.AllArgsConstructor;
 import lombok.Builder;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;

 @Entity
 @Table(name = "clients")
 @Getter
 @Setter
 @NoArgsConstructor
 @AllArgsConstructor
 @Builder
 public class Client {

     @Id
     @GeneratedValue(strategy = GenerationType.UUID)
     private UUID id;

     @Column(nullable = false, length = 100)
     private String name;

     @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
     private List<Address> addresses = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    
 }