package com.example.ecommercebackend.service.impl;

import com.example.ecommercebackend.dto.CategoryDTO;
import com.example.ecommercebackend.dto.CategoryResponse;
import com.example.ecommercebackend.model.Category;
import com.example.ecommercebackend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category1;
    private Category category2;
    private CategoryDTO categoryDTO1;
    private CategoryDTO categoryDTO2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        category1 = new Category();
        category1.setCategoryId(1L);
        category1.setCategoryName("Electronics");

        category2 = new Category();
        category2.setCategoryId(2L);
        category2.setCategoryName("Books");

        categoryDTO1 = new CategoryDTO();
        categoryDTO1.setCategoryId(1L);
        categoryDTO1.setCategoryName("Electronics");

        categoryDTO2 = new CategoryDTO();
        categoryDTO2.setCategoryId(2L);
        categoryDTO2.setCategoryName("Books");
    }

    @Test
    void testFetchCategories() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("categoryName").ascending());
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(category1, category2), pageable, 2);
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(modelMapper.map(category1, CategoryDTO.class)).thenReturn(categoryDTO1);
        when(modelMapper.map(category2, CategoryDTO.class)).thenReturn(categoryDTO2);

        CategoryResponse response = categoryService.fetchCategories(0, 10, "categoryName", "asc");

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals("Electronics", response.getContent().get(0).getCategoryName());
        verify(categoryRepository, times(1)).findAll(pageable);
    }

    @Test
    void testCreateCategory_Success() {
        when(categoryRepository.findByCategoryName(categoryDTO1.getCategoryName())).thenReturn(Optional.empty());
        when(modelMapper.map(categoryDTO1, Category.class)).thenReturn(category1);
        when(categoryRepository.save(category1)).thenReturn(category1);
        when(modelMapper.map(category1, CategoryDTO.class)).thenReturn(categoryDTO1);

        CategoryDTO result = categoryService.createCategory(categoryDTO1);

        assertNotNull(result);
        assertEquals("Electronics", result.getCategoryName());
        verify(categoryRepository, times(1)).save(category1);
    }

    @Test
    void testCreateCategory_AlreadyExists() {
        when(categoryRepository.findByCategoryName(categoryDTO1.getCategoryName())).thenReturn(Optional.of(category1));
        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(categoryDTO1));
    }

    @Test
    void testUpdateCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.save(category1)).thenReturn(category1);
        when(modelMapper.map(category1, CategoryDTO.class)).thenReturn(categoryDTO1);

        CategoryDTO updatedDTO = new CategoryDTO();
        updatedDTO.setCategoryName("Electronics Updated");

        CategoryDTO result = categoryService.updateCategory(1L, updatedDTO);

        assertNotNull(result);
        verify(categoryRepository, times(1)).save(category1);
    }

    @Test
    void testUpdateCategory_NotFound() {
        when(categoryRepository.findById(3L)).thenReturn(Optional.empty());
        CategoryDTO updatedDTO = new CategoryDTO();
        updatedDTO.setCategoryName("NonExisting");
        assertThrows(RuntimeException.class, () -> categoryService.updateCategory(3L, updatedDTO));
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCategory_NotFound() {
        when(categoryRepository.existsById(3L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(3L));
    }

    @Test
    void testFetchAllCategory() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));
        when(modelMapper.map(category1, CategoryDTO.class)).thenReturn(categoryDTO1);
        when(modelMapper.map(category2, CategoryDTO.class)).thenReturn(categoryDTO2);

        List<CategoryDTO> result = categoryService.fetchAllCategory();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getCategoryName());
        verify(categoryRepository, times(1)).findAll();
    }
}
