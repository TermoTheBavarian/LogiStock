package com.example.ecommercebackend.service.impl;

import com.example.ecommercebackend.dto.ProductDTO;
import com.example.ecommercebackend.exception.custom.ResourceNotFoundException;
import com.example.ecommercebackend.model.Category;
import com.example.ecommercebackend.model.Product;
import com.example.ecommercebackend.repository.CartRepository;
import com.example.ecommercebackend.repository.CategoryRepository;
import com.example.ecommercebackend.repository.ProductRepository;
import com.example.ecommercebackend.service.CartService;
import com.example.ecommercebackend.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileService fileService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    private ModelMapper modelMapper;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicializar ModelMapper
        modelMapper = new ModelMapper();

        // Inyectar manualmente los mocks y el modelMapper
        productService = new ProductServiceImpl(
                productRepository,
                categoryRepository,
                modelMapper,
                fileService,
                cartRepository,
                cartService
        );

        // Aquí asignamos imageUploadPath manualmente
        productService.imageUploadPath = "src/test/resources/uploads/";
    }


    @Test
    void testCreateProduct_Success() {
        Category category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductName("Laptop");
        productDTO.setPrice(1000.0);
        productDTO.setDiscount(10.0);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByProductName("Laptop")).thenReturn(Optional.empty());

        Product savedProduct = new Product();
        savedProduct.setProductId(1L);
        savedProduct.setProductName("Laptop");
        savedProduct.setPrice(1000.0);
        savedProduct.setDiscount(10.0);
        savedProduct.setSpecialPrice(900.0);
        savedProduct.setCategory(category);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDTO result = productService.createProduct(productDTO, 1L);

        assertNotNull(result);
        assertEquals("Laptop", result.getProductName());
        assertEquals(900.0, result.getSpecialPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_CategoryNotFound() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductName("Laptop");

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(productDTO, 1L));
    }

    @Test
    void testUpdateProductImage_Success() throws Exception {
        Product product = new Product();
        product.setProductId(1L);
        product.setProductName("Laptop");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "laptop.png", "image/png", "dummy".getBytes());

        ProductDTO result = productService.updateProductImage(1L, file);

        assertNotNull(result);
        assertTrue(result.getProductImage().contains("Laptop"));
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
