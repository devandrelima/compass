package com.gp.compass.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_stops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cep",           column = @Column(name = "cep",          nullable = false, length = 9)),
            @AttributeOverride(name = "street",        column = @Column(name = "street",       nullable = false, length = 150)),
            @AttributeOverride(name = "neighborhood",  column = @Column(name = "neighborhood", nullable = false, length = 100)),
            @AttributeOverride(name = "number",        column = @Column(name = "number",       nullable = false, length = 20)),
            @AttributeOverride(name = "city",          column = @Column(name = "city",         nullable = false, length = 100)),
            @AttributeOverride(name = "state",         column = @Column(name = "state",        nullable = false, length = 2)),
            @AttributeOverride(name = "complement",    column = @Column(name = "complement",   length = 150))
    })
    private AddressSnapshot address;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
