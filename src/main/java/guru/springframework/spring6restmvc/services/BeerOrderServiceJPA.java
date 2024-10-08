package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.controllers.NotFoundException;
import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.BeerOrderLine;
import guru.springframework.spring6restmvc.entities.BeerOrderShipment;
import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.BeerOrderMapper;
import guru.springframework.spring6restmvc.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvc.model.BeerOrderDTO;
import guru.springframework.spring6restmvc.model.BeerOrderUpdateDTO;
import guru.springframework.spring6restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderServiceJPA implements BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerRepository beerRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderMapper beerOrderMapper;

    private static final Integer DEFAULT_PAGE = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    @Override
    public Page<BeerOrderDTO> listBeerOrders(Integer pageNumber, Integer pageSize) {
        log.info("listBeerOrders - in service");

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        Page<BeerOrder> beerOrderPage = beerOrderRepository.findAll(pageRequest);

        return beerOrderPage.map(beerOrderMapper::beerOrderToBeerOrderDto);
    }

    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber;
        int queryPageSize;

        if (pageNumber != null && pageNumber > 0) {
            queryPageNumber = pageNumber - 1;
        } else {
            queryPageNumber = DEFAULT_PAGE;
        }

        if (pageSize == null) {
            queryPageSize = DEFAULT_PAGE_SIZE;
        } else {
            queryPageSize = pageSize > 1000 ? 1000 : pageSize;
        }

        Sort sort = Sort.by(Sort.Order.asc("createdDate"));

        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }

    @Override
    public Optional<BeerOrderDTO> getBeerOrderById(UUID id) {
        log.info("getBeerOrderById - in service");

        return Optional.ofNullable(beerOrderMapper.beerOrderToBeerOrderDto(beerOrderRepository.findById(id)
                .orElse(null)));
    }

    @Override
    public BeerOrder saveNewBeerOrder(BeerOrderCreateDTO beerOrderCreateDTO) {

        Customer customer = customerRepository.findById(beerOrderCreateDTO.getCustomerId())
                .orElseThrow(NotFoundException::new);

        Set<BeerOrderLine> beerOrderLines = new HashSet<>();

        beerOrderCreateDTO.getBeerOrderLines().forEach(beerOrderLineCreateDTO -> {
            beerOrderLines.add(BeerOrderLine.builder()
                    .beer(beerRepository.findById(beerOrderLineCreateDTO.getBeerId())
                            .orElseThrow(NotFoundException::new))
                    .orderQuantity(beerOrderLineCreateDTO.getOrderQuantity())
                    .build());
        });

        return beerOrderRepository.save(BeerOrder.builder()
                .customer(customer)
                .beerOrderLines(beerOrderLines)
                .customerRef(beerOrderCreateDTO.getCustomerRef())
                .build());
    }

    @Override
    public BeerOrderDTO updateOrder(UUID beerOrderId, BeerOrderUpdateDTO beerOrderUpdateDTO) {
        BeerOrder order = beerOrderRepository.findById(beerOrderId).orElseThrow(NotFoundException::new);

        order.setCustomer(customerRepository.findById(beerOrderUpdateDTO.getCustomerId()).orElseThrow(NotFoundException::new));
        order.setCustomerRef(beerOrderUpdateDTO.getCustomerRef());

        beerOrderUpdateDTO.getBeerOrderLines().forEach(beerOrderLineUpdateDTO -> {
            if (beerOrderLineUpdateDTO.getBeerId() != null) {
                BeerOrderLine foundLine = order.getBeerOrderLines().stream()
                        .filter(beerOrderLine -> beerOrderLine.getId().equals(beerOrderLineUpdateDTO.getId()))
                        .findFirst().orElseThrow(NotFoundException::new);
                foundLine.setBeer(beerRepository.findById(beerOrderLineUpdateDTO.getBeerId()).orElseThrow(NotFoundException::new));
                foundLine.setOrderQuantity(beerOrderLineUpdateDTO.getOrderQuantity());
                foundLine.setQuantityAllocated(beerOrderLineUpdateDTO.getQuantityAllocated());
            } else {
                order.getBeerOrderLines().add(BeerOrderLine.builder()
                        .beer(beerRepository.findById(beerOrderLineUpdateDTO.getBeerId()).orElseThrow(NotFoundException::new))
                        .orderQuantity(beerOrderLineUpdateDTO.getOrderQuantity())
                        .quantityAllocated(beerOrderLineUpdateDTO.getQuantityAllocated())
                        .build());
            }
        });

        if (beerOrderUpdateDTO.getBeerOrderShipment() != null && beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber() != null) {
            if (order.getBeerOrderShipment() == null) {
                order.setBeerOrderShipment(BeerOrderShipment.builder().trackingNumber(beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber()).build());
            } else {
                order.getBeerOrderShipment().setTrackingNumber(beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber());
            }
        }

        return beerOrderMapper.beerOrderToBeerOrderDto(beerOrderRepository.save(order));
    }

    @Override
    public void deleteOrder(UUID beerOrderId) {
        if (beerOrderRepository.existsById(beerOrderId)) {
            beerOrderRepository.deleteById(beerOrderId);
        } else {
            throw new NotFoundException();
        }
    }
}
