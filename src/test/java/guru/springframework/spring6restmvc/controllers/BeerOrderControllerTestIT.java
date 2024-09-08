package guru.springframework.spring6restmvc.controllers;

import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.repositories.BeerOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static guru.springframework.spring6restmvc.controllers.BeerControllerTest.jwtRequestPostProcessor;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BeerOrderControllerTestIT {

    @Autowired
    WebApplicationContext wac;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testListBeerOrders() throws Exception {
        mockMvc.perform(get(BeerOrderController.BEER_ORDER_PATH)
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", greaterThan(0)));
    }

    @Test
    void testGetBeerOrderById() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().get(0);

        mockMvc.perform(get(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                .with(jwtRequestPostProcessor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(beerOrder.getId().toString())));
    }
}