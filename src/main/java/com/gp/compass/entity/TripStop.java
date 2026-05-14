package com.gp.compass.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "stop_type", nullable = false, length = 20)
    private StopType stopType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private StopPriority priority = StopPriority.NORMAL;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "cep", column = @Column(name = "embarque_cep", nullable = false, length = 9)),
        @AttributeOverride(name = "street", column = @Column(name = "embarque_street", nullable = false, length = 150)),
        @AttributeOverride(name = "neighborhood", column = @Column(name = "embarque_neighborhood", nullable = false, length = 100)),
        @AttributeOverride(name = "number", column = @Column(name = "embarque_number", nullable = false, length = 20)),
        @AttributeOverride(name = "city", column = @Column(name = "embarque_city", nullable = false, length = 100)),
        @AttributeOverride(name = "state", column = @Column(name = "embarque_state", nullable = false, length = 2)),
        @AttributeOverride(name = "complement", column = @Column(name = "embarque_complement", length = 150)),
        @AttributeOverride(name = "lat", column = @Column(name = "embarque_lat")),
        @AttributeOverride(name = "lng", column = @Column(name = "embarque_lng"))
    })
    private AddressSnapshot embarque;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "cep", column = @Column(name = "desembarque_cep", length = 9)),
        @AttributeOverride(name = "street", column = @Column(name = "desembarque_street", length = 150)),
        @AttributeOverride(name = "neighborhood", column = @Column(name = "desembarque_neighborhood", length = 100)),
        @AttributeOverride(name = "number", column = @Column(name = "desembarque_number", length = 20)),
        @AttributeOverride(name = "city", column = @Column(name = "desembarque_city", length = 100)),
        @AttributeOverride(name = "state", column = @Column(name = "desembarque_state", length = 2)),
        @AttributeOverride(name = "complement", column = @Column(name = "desembarque_complement", length = 150)),
        @AttributeOverride(name = "lat", column = @Column(name = "desembarque_lat")),
        @AttributeOverride(name = "lng", column = @Column(name = "desembarque_lng"))
    })
    private AddressSnapshot desembarque;

    @Column(name = "embarque_checked", nullable = false)
    @Builder.Default
    private boolean embarqueChecked = false;

    @Column(name = "desembarque_checked", nullable = false)
    @Builder.Default
    private boolean desembarqueChecked = false;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
