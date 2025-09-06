package com.example.ecommercebackend.service.impl;

import com.example.ecommercebackend.dto.UserDTO;
import com.example.ecommercebackend.exception.custom.ResourceNotFoundException;
import com.example.ecommercebackend.model.User;
import com.example.ecommercebackend.repository.UserRepository;
import com.example.ecommercebackend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private AuthUtil authUtil;
    private ModelMapper modelMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        authUtil = Mockito.mock(AuthUtil.class);
        modelMapper = new ModelMapper();

        userService = new UserServiceImpl();
        userService.userRepository = userRepository;
        userService.authUtil = authUtil;
        userService.modelMapper = modelMapper;
    }

    @Test
    void fetchCurrentlyLoggedInUserDetails_shouldReturnUserDTO() {
        // Preparar datos
        User user = new User();
        user.setUserId(1L);
        user.setUserName("John Doe");
        user.setEmail("john@example.com");

        when(authUtil.loggedInUser()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Ejecutar método
        UserDTO userDTO = userService.fetchCurrentlyLoggedInUserDetails();

        // Verificar resultados
        assertNotNull(userDTO);
        assertEquals("John Doe", userDTO.getUserName());
        assertEquals("john@example.com", userDTO.getEmail());
    }

    @Test
    void updateUserDetails_shouldUpdateAndReturnUserDTO() {
        User existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setUserName("Old Name");
        existingUser.setEmail("old@example.com");

        UserDTO updateDTO = new UserDTO();
        updateDTO.setUserName("New Name");
        updateDTO.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserDTO result = userService.updateUserDetails(updateDTO, 1L);

        assertNotNull(result);
        assertEquals("New Name", result.getUserName());
        assertEquals("new@example.com", result.getEmail());

        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUserDetails_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserDTO updateDTO = new UserDTO();

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserDetails(updateDTO, 1L));
    }
}
