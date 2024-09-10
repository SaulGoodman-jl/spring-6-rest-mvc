package guru.springframework.spring6restmvc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.BeerOrderMapper;
import guru.springframework.spring6restmvc.mappers.BeerOrderMapperImpl;
import guru.springframework.spring6restmvc.model.*;
import guru.springframework.spring6restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static guru.springframework.spring6restmvc.controllers.BeerControllerTest.jwtRequestPostProcessor;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BeerOrderControllerTestIT {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerRepository beerRepository;

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

    @Test
    void testCreateBeerOrderMVC() throws Exception {

        Customer customer = customerRepository.findAll().get(0);
        Beer beer = beerRepository.findAll().get(0);

        BeerOrderCreateDTO beerOrderCreateDTO = BeerOrderCreateDTO.builder()
                .customerId(customer.getId())
                .beerOrderLines(Set.of(BeerOrderLineCreateDTO.builder()
                        .beerId(beer.getId())
                        .orderQuantity(1)
                        .build()))
                .build();


        mockMvc.perform(post(BeerOrderController.BEER_ORDER_PATH)
                        .with(jwtRequestPostProcessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerOrderCreateDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
    }

    @Transactional
    @Test
    void testUpdateBeerOrderMVC() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().get(0);
        final String newCustomerRef = "John";

        Set<BeerOrderLineUpdateDTO> lines = new HashSet<>();

        beerOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            lines.add(BeerOrderLineUpdateDTO.builder()
                    .id(beerOrderLine.getId())
                    .beerId(beerOrderLine.getBeer().getId())
                    .orderQuantity(beerOrderLine.getOrderQuantity())
                    .quantityAllocated(beerOrderLine.getQuantityAllocated())
                    .build());
        });

        BeerOrderUpdateDTO beerOrderUpdateDTO = BeerOrderUpdateDTO.builder()
                .customerId(beerOrder.getCustomer().getId())
                .customerRef(newCustomerRef)
                .beerOrderLines(lines)
                .beerOrderShipment(BeerOrderShipmentUpdateDTO.builder()
                        .trackingNumber("123456")
                        .build())
                .build();

        mockMvc.perform(put(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                        .with(jwtRequestPostProcessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerOrderUpdateDTO))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRef", is(newCustomerRef)));
    }

    @Transactional
    @Test
    void testDeleteBeerOrder() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().get(0);

        mockMvc.perform(delete(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isNoContent());

        assertTrue(beerOrderRepository.findById(beerOrder.getId()).isEmpty());

        mockMvc.perform(delete(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isNotFound());
    }
}