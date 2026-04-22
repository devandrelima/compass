package com.gp.compass.dto;

import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.StopType;

public record UpdateTripStopRequest(

        StopType stopType,
        StopPriority priority

) {}