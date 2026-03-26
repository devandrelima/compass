package com.gp.compass.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cep",          column = @Column(name = "origin_cep")),
            @AttributeOverride(name = "street",       column = @Column(name = "origin_street")),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "origin_neighborhood")),
            @AttributeOverride(name = "number",       column = @Column(name = "origin_number")),
            @AttributeOverride(name = "city",         column = @Column(name = "origin_city")),
            @AttributeOverride(name = "state",        column = @Column(name = "origin_state")),
            @AttributeOverride(name = "complement",   column = @Column(name = "origin_complement"))
    })
    private AddressSnapshot originAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cep",          column = @Column(name = "destination_cep")),
            @AttributeOverride(name = "street",       column = @Column(name = "destination_street")),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "destination_neighborhood")),
            @AttributeOverride(name = "number",       column = @Column(name = "destination_number")),
            @AttributeOverride(name = "city",         column = @Column(name = "destination_city")),
            @AttributeOverride(name = "state",        column = @Column(name = "destination_state")),
            @AttributeOverride(name = "complement",   column = @Column(name = "destination_complement"))
    })
    private AddressSnapshot destinationAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TripStatus status = TripStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}