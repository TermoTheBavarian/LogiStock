package com.example.ecommercebackend.service.impl;

import com.example.ecommercebackend.dto.AddressDTO;
import com.example.ecommercebackend.exception.custom.ResourceNotFoundException;
import com.example.ecommercebackend.model.Address;
import com.example.ecommercebackend.model.User;
import com.example.ecommercebackend.repository.AddressRepository;
import com.example.ecommercebackend.repository.UserRepository;
import com.example.ecommercebackend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;
    private AddressDTO testAddressDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setAddresses(new ArrayList<>());

        testAddress = new Address();
        testAddress.setAddressId(1L);
        testAddress.setStreet("Test Street");
        testAddress.setUser(testUser);

        testAddressDTO = new AddressDTO();
        testAddressDTO.setAddressId(1L);
        testAddressDTO.setStreet("Test Street");
    }

    @Test
    void testCreateAddress() {
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(modelMapper.map(testAddressDTO, Address.class)).thenReturn(testAddress);
        when(addressRepository.save(testAddress)).thenReturn(testAddress);
        when(modelMapper.map(testAddress, AddressDTO.class)).thenReturn(testAddressDTO);

        AddressDTO created = addressService.createAddress(testAddressDTO);

        assertNotNull(created);
        assertEquals(testAddressDTO.getStreet(), created.getStreet());
        verify(addressRepository, times(1)).save(testAddress);
    }

    @Test
    void testUpdateAddress_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(testAddress)).thenReturn(testAddress);
        when(modelMapper.map(testAddress, AddressDTO.class)).thenReturn(testAddressDTO);

        AddressDTO updated = addressService.updateAddress(1L, testAddressDTO);

        assertNotNull(updated);
        assertEquals(testAddressDTO.getStreet(), updated.getStreet());
        verify(addressRepository, times(1)).save(testAddress);
    }

    @Test
    void testUpdateAddress_NotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                addressService.updateAddress(1L, testAddressDTO));

        assertTrue(exception.getMessage().contains("Address not found"));
    }

    @Test
    void testDeleteAddressById_Success() {
        testUser.getAddresses().add(testAddress);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(userRepository.save(testUser)).thenReturn(testUser);

        addressService.deleteAddressById(1L);

        verify(addressRepository, times(1)).delete(testAddress);
        assertTrue(testUser.getAddresses().isEmpty());
    }

    @Test
    void testDeleteAddressById_NotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddressById(1L));
    }

    @Test
    void testFetchAddressById_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(modelMapper.map(testAddress, AddressDTO.class)).thenReturn(testAddressDTO);

        AddressDTO dto = addressService.fetchAddressById(1L);

        assertNotNull(dto);
        assertEquals("Test Street", dto.getStreet());
    }

    @Test
    void testFetchAddresses_Pagination() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(testAddress);

        Page<Address> page = new PageImpl<>(addresses);
        when(addressRepository.findAll(PageRequest.of(0, 10, Sort.by("addressId").ascending()))).thenReturn(page);
        when(modelMapper.map(testAddress, AddressDTO.class)).thenReturn(testAddressDTO);

        Page<AddressDTO> result = addressService.fetchAddresses(0, 10, "addressId", "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Street", result.getContent().get(0).getStreet());
    }
}
