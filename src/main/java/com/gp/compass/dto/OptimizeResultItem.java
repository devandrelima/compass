package com.gp.compass.dto;

public record OptimizeResultItem(

        Double lat,
        Double lng,
        int order,
        String clientName

) {}
