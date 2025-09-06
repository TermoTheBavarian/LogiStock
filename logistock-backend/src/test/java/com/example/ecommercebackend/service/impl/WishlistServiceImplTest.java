package com.example.ecommercebackend.service.impl;

import com.example.ecommercebackend.dto.WishlistDTO;
import com.example.ecommercebackend.model.Product;
import com.example.ecommercebackend.model.User;
import com.example.ecommercebackend.model.Wishlist;
import com.example.ecommercebackend.repository.ProductRepository;
import com.example.ecommercebackend.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistServiceImplTest {

    private WishlistRepository wishlistRepository;
    private ProductRepository productRepository;
    private ModelMapper modelMapper;
    private WishlistServiceImpl wishlistService;

    @BeforeEach
    void setUp() {
        wishlistRepository = Mockito.mock(WishlistRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        modelMapper = new ModelMapper();

        wishlistService = new WishlistServiceImpl();
        wishlistService.wishlistRepository = wishlistRepository;
        wishlistService.productRepository = productRepository;
        wishlistService.modelMapper = modelMapper;
    }

    @Test
    void getWishlistByUser_shouldReturnWishlistDTOs() {
        User user = new User();
        user.setUserId(1L);

        Product product = new Product();
        product.setProductId(100L);
        product.setProductName("Product1");
        product.setPrice(50.0);
        product.setProductImage("image.png");

        Wishlist wishlist1 = new Wishlist();
        wishlist1.setId(1L);
        wishlist1.setUser(user);
        wishlist1.setProduct(product);

        when(wishlistRepository.findByUserId(1L)).thenReturn(Arrays.asList(wishlist1));

        List<WishlistDTO> result = wishlistService.getWishlistByUser(1L);

        assertEquals(1, result.size());
        WishlistDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Product1", dto.getProductName());
        assertEquals(50.0, dto.getProductPrice());
        assertEquals("image.png", dto.getProductImageUrl());
        assertEquals(1L, dto.getUserId());
        assertEquals(100L, dto.getProductId());
    }

    @Test
    void addProductToWishlist_shouldSaveAndReturnDTO() {
        User user = new User();
        user.setUserId(1L);

        Product product = new Product();
        product.setProductId(100L);
        product.setProductImage("image.png");

        Wishlist wishlist = new Wishlist();
        wishlist.setId(1L);
        wishlist.setUser(user);
        wishlist.setProduct(product);

        when(wishlistRepository.findByUserIdAndProductId(1L, 100L)).thenReturn(Optional.empty());
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);

        WishlistDTO result = wishlistService.addProductToWishlist(1L, 100L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getProductId());
        assertEquals(1L, result.getUserId());
        assertNull(result.getProductImageUrl());

        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    void addProductToWishlist_whenAlreadyExists_shouldThrowException() {
        Wishlist existingWishlist = new Wishlist();
        when(wishlistRepository.findByUserIdAndProductId(1L, 100L))
                .thenReturn(Optional.of(existingWishlist));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> wishlistService.addProductToWishlist(1L, 100L));
        assertEquals("Product already in wishlist!", exception.getMessage());
    }

    @Test
    void removeProductFromWishlist_shouldDeleteWishlist() {
        Wishlist wishlist = new Wishlist();
        when(wishlistRepository.findByUserIdAndProductId(1L, 100L)).thenReturn(Optional.of(wishlist));

        wishlistService.removeProductFromWishlist(1L, 100L);

        verify(wishlistRepository, times(1)).delete(wishlist);
    }

    @Test
    void removeProductFromWishlist_whenNotFound_shouldThrowException() {
        when(wishlistRepository.findByUserIdAndProductId(1L, 100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> wishlistService.removeProductFromWishlist(1L, 100L));

        assertEquals("Wishlist item not found", exception.getMessage());
    }
}
