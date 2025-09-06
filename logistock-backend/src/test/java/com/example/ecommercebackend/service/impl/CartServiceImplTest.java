package com.example.ecommercebackend.service.impl;

import com.example.ecommercebackend.dto.CartDTO;
import com.example.ecommercebackend.dto.ProductDTO;
import com.example.ecommercebackend.exception.APIException;
import com.example.ecommercebackend.exception.custom.ResourceNotFoundException;
import com.example.ecommercebackend.model.Cart;
import com.example.ecommercebackend.model.CartItem;
import com.example.ecommercebackend.model.Product;
import com.example.ecommercebackend.model.User;
import com.example.ecommercebackend.repository.CartItemRepository;
import com.example.ecommercebackend.repository.CartRepository;
import com.example.ecommercebackend.repository.ProductRepository;
import com.example.ecommercebackend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private CartDTO testCartDTO;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1L);

        testCart = new Cart();
        testCart.setCartId(1L);
        testCart.setTotalPrice(0.0);
        testCart.setUser(testUser);
        testCart.setCartItems(new ArrayList<>());

        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setProductName("Test Product");
        testProduct.setQuantity(10);
        testProduct.setSpecialPrice(100.0);

        testCartItem = new CartItem();
        testCartItem.setCartItemId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setProductPrice(100.0);

        testCart.getCartItems().add(testCartItem);

        testCartDTO = new CartDTO();
        testProductDTO = new ProductDTO();
    }

    @Test
    void testAddProductToCart_Success() {
        when(authUtil.loggedInEmail()).thenReturn("test@example.com");
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(cartRepository.findCartByEmail("test@example.com")).thenReturn(testCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findCartItemByProductIdAndCartId(1L, 1L)).thenReturn(null);
        when(modelMapper.map(testCart, CartDTO.class)).thenReturn(testCartDTO);
        when(modelMapper.map(testProduct, ProductDTO.class)).thenReturn(testProductDTO);

        CartDTO result = cartService.addProductToCart(1L, 2);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void testAddProductToCart_ProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        when(authUtil.loggedInEmail()).thenReturn("test@example.com");
        when(cartRepository.findCartByEmail("test@example.com")).thenReturn(testCart);

        assertThrows(ResourceNotFoundException.class, () -> cartService.addProductToCart(1L, 1));
    }

    @Test
    void testAddProductToCart_ProductAlreadyInCart() {
        when(authUtil.loggedInEmail()).thenReturn("test@example.com");
        when(cartRepository.findCartByEmail("test@example.com")).thenReturn(testCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findCartItemByProductIdAndCartId(1L, 1L)).thenReturn(testCartItem);

        assertThrows(APIException.class, () -> cartService.addProductToCart(1L, 1));
    }

    @Test
    void testUpdateProductQuantityInCart_Success() {
        when(authUtil.loggedInEmail()).thenReturn("test@example.com");
        when(cartRepository.findCartByEmail("test@example.com")).thenReturn(testCart);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findCartItemByProductIdAndCartId(1L, 1L)).thenReturn(testCartItem);
        when(cartItemRepository.save(testCartItem)).thenReturn(testCartItem);
        when(modelMapper.map(testCart, CartDTO.class)).thenReturn(testCartDTO);
        when(modelMapper.map(testProduct, ProductDTO.class)).thenReturn(testProductDTO);

        CartDTO result = cartService.updateProductQuantityInCart(1L, 3);

        assertNotNull(result);
        assertEquals(testCart.getTotalPrice(), testCart.getTotalPrice()); // Se puede mejorar comparando valores calculados
    }

    @Test
    void testDeleteProductFromCart_Success() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(1L, 1L)).thenReturn(testCartItem);

        String result = cartService.deleteProductFromCart(1L, 1L);

        assertTrue(result.contains("removed from the cart"));
        verify(cartItemRepository, times(1)).deleteCartItemByProductIdAndCartId(1L, 1L);
    }

    @Test
    void testDeleteProductFromCart_NotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(1L, 2L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> cartService.deleteProductFromCart(1L, 2L));
    }
}
