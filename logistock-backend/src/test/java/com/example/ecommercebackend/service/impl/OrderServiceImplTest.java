package com.example.ecommercebackend.service.impl;

import com.example.ecommercebackend.dto.OrderDTO;
import com.example.ecommercebackend.dto.OrderItemDTO;
import com.example.ecommercebackend.exception.APIException;
import com.example.ecommercebackend.model.*;
import com.example.ecommercebackend.repository.*;
import com.example.ecommercebackend.service.CartService;
import com.example.ecommercebackend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthUtil authUtil;

    private ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService.modelMapper = modelMapper;
    }

    @Test
    void testPlaceOrder_Success() {
        // Datos simulados
        String email = "user@example.com";
        Long addressId = 1L;

        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setTotalPrice(100.0);
        Product product = new Product();
        product.setProductId(1L);
        product.setQuantity(10);
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setProductPrice(50.0);
        cartItem.setDiscount(5.0);
        cart.setCartItems(List.of(cartItem));

        Address address = new Address();
        address.setAddressId(addressId);

        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setEmail(email);
        savedOrder.setOrderDate(LocalDate.now());
        savedOrder.setTotalAmount(100.0);
        savedOrder.setAddress(address);

        Payment payment = new Payment();
        payment.setPaymentId(1L);

        // Simulaciones
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(cart);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderItemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar método
        OrderDTO orderDTO = orderService.placeOrder(addressId, "CARD", "PG", "PAY123", "SUCCESS", "OK");

        // Verificaciones
        assertNotNull(orderDTO);
        assertEquals(email, orderDTO.getEmail());
        assertEquals(1, orderDTO.getOrderItems().size());
        assertEquals(addressId, orderDTO.getAddressId());

        // Verificar interacciones con mocks
        verify(cartService, times(1)).deleteProductFromCart(cart.getCartId(), product.getProductId());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testPlaceOrder_EmptyCart_ThrowsException() {
        String email = "user@example.com";
        Cart emptyCart = new Cart();
        emptyCart.setCartId(1L);
        emptyCart.setCartItems(new ArrayList<>());

        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(emptyCart);

        Long addressId = 1L;
        Address address = new Address();
        address.setAddressId(addressId);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        APIException exception = assertThrows(APIException.class, () -> {
            orderService.placeOrder(addressId, "CARD", "PG", "PAY123", "SUCCESS", "OK");
        });

        assertEquals("Cart is empty", exception.getMessage());
    }

    @Test
    void testPlaceOrder_CartNotFound_ThrowsException() {
        String email = "user@example.com";
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(null);

        Long addressId = 1L;

        assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(addressId, "CARD", "PG", "PAY123", "SUCCESS", "OK");
        });
    }
}
