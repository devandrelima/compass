package com.gp.compass;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompassApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(port).isPositive();
    }
}
