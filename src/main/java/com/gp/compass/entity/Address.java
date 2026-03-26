 package com.gp.compass.entity;

 import java.util.UUID;

 import jakarta.persistence.*;
 import lombok.AllArgsConstructor;
 import lombok.Builder;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;

 @Entity
 @Table(name = "addresses")
 @Getter
 @Setter
 @NoArgsConstructor
 @AllArgsConstructor
 @Builder
 public class Address {
    
     @Id
     @GeneratedValue(strategy = GenerationType.UUID)
     private UUID id;
    
     @Column(nullable = false, length = 9)
     String cep;

     @Column(nullable = false, length = 150)
     String street;

     @Column(nullable = false, length = 100)
     String neighborhood;

     @Column(nullable = false, length = 20)
     String number;

     @Column(nullable = false, length = 100)
     String city;

     @Column(nullable = false, length = 2)
     String state;

     @Column(nullable = false, length = 150)
     String complement;

     @ManyToOne
     @JoinColumn(name = "client_id", nullable = false)
     private Client client;
 }
