package guru.springframework.spring6restmvc.controllers;

import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvc.model.BeerOrderDTO;
import guru.springframework.spring6restmvc.model.BeerOrderLineCreateDTO;
import guru.springframework.spring6restmvc.services.BeerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerOrderController {

    public static final String BEER_ORDER_PATH = "/api/v1/beerOrder";
    public static final String BEER_ORDER_PATH_ID = BEER_ORDER_PATH + "/{beerOrderId}";

    private final BeerOrderService beerOrderService;

    @GetMapping(BEER_ORDER_PATH)
    public Page<BeerOrderDTO> listBeerOrders(
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        return beerOrderService.listBeerOrders(pageNumber, pageSize);
    }

    @GetMapping(BEER_ORDER_PATH_ID)
    public BeerOrderDTO getBeerOrderById(@PathVariable("beerOrderId") UUID id) {
        log.debug("getBeerOrderById - in controller");
        return beerOrderService.getBeerOrderById(id).orElseThrow(NotFoundException::new);
    }

    @PostMapping(BEER_ORDER_PATH)
    public ResponseEntity<Void> createOrder(@RequestBody BeerOrderCreateDTO beerOrderCreateDTO) {
        BeerOrder savedNewBeerOrder = beerOrderService.saveNewBeerOrder(beerOrderCreateDTO);

        return ResponseEntity.created(URI.create(BEER_ORDER_PATH + "/" + savedNewBeerOrder.getId().toString())).build();
    }
}
