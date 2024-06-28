package com.example.mini.domain.accomodation.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import com.example.mini.domain.accomodation.model.response.AccomodationDetailsResponseDto;
import com.example.mini.domain.accomodation.model.response.AccomodationResponseDto;
import com.example.mini.domain.accomodation.model.response.PagedResponse;
import com.example.mini.domain.accomodation.model.response.RoomResponseDto;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccomodationServiceTest {

    @Mock
    private AccomodationRepository accomodationRepository;

    @Mock
    private AccomodationSearchRepository accomodationSearchRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private AccomodationService accomodationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAccommodations() {
        Pageable pageable = PageRequest.of(0, 5);

        Accomodation accomodation1 = Accomodation.builder()
                .name("숙소 이름 1")
                .build();
        Accomodation accomodation2 = Accomodation.builder()
                .name("숙소 이름 2")
                .build();

        List<Accomodation> accomodations = Arrays.asList(accomodation1, accomodation2);
        Page<Accomodation> page = new PageImpl<>(accomodations, pageable, accomodations.size());

        // When
        when(accomodationRepository.findAll(pageable)).thenReturn(page);

        // Then
        PagedResponse<AccomodationResponseDto> result = accomodationService.getAllAccommodations(1);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals("숙소 이름 1", result.getContent().get(0).getName());

        verify(accomodationRepository).findAll(pageable);
    }

    @Test
    void testGetAccommodationsByCategory() {
        Pageable pageable = PageRequest.of(0, 5);
        AccomodationCategory category = AccomodationCategory.SEOUL;
        Accomodation accomodation1 = Accomodation.builder()
                .name("숙소 이름 1")
                .category(AccomodationCategory.SEOUL)
                .build();
        Accomodation accomodation2 = Accomodation.builder()
                .name("숙소 이름 2")
                .category(AccomodationCategory.SEOUL)
                .build();

        List<Accomodation> accomodations = Arrays.asList(accomodation1, accomodation2);
        Page<Accomodation> page = new PageImpl<>(accomodations, pageable, accomodations.size());

        // When
        when(accomodationRepository.findByCategoryName(category, pageable)).thenReturn(page);

        // Then
        PagedResponse<AccomodationResponseDto> response = accomodationService.getAccommodationsByCategory("서울", 1);
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getContent().size());
        assertEquals("숙소 이름 1", response.getContent().get(0).getName());
        assertEquals("숙소 이름 2", response.getContent().get(1).getName());

        verify(accomodationRepository).findByCategoryName(category, pageable);
    }

    @Test
    void testSearchByAccommodationName() {
    }

    @Test
    void testGetAccommodationDetails() {
        Accomodation accomodation = Accomodation.builder()
                .name("accommodation name")
                .build();
        Room room1 = Room.builder()
                .name("room 1")
                .baseGuests(2)
                .price(100000)
                .maxGuests(4)
                .extraPersonCharge(20000)
                .build();
        Room room2 = Room.builder()
                .name("room 2")
                .baseGuests(4)
                .price(150000)
                .maxGuests(6)
                .extraPersonCharge(10000)
                .build();
        List<Room> rooms = Arrays.asList(room1, room2);

        // When
        when(accomodationRepository.findById(any())).thenReturn(Optional.ofNullable(accomodation));
        when(roomRepository.findByAccomodationId(any())).thenReturn(rooms);


        // Then
        AccomodationDetailsResponseDto response = accomodationService.getAccomodationDetails(any());
        assertEquals("accommodation name", response.getAccomodation().getName());
        assertEquals(2, response.getRooms().size());
        assertEquals("room 1", response.getRooms().get(0).getName());

        verify(accomodationRepository).findById(any());
        verify(roomRepository).findByAccomodationId(any());
    }

    @Test
    void testGetRoomDetail() {
        Accomodation accomodation = Accomodation.builder()
                .id(1L)
                .name("accommodation name")
                .build();
        Room room = Room.builder()
                .name("room")
                .baseGuests(2)
                .price(100000)
                .maxGuests(4)
                .extraPersonCharge(20000)
                .accomodation(accomodation)
                .build();

        // When
        when(roomRepository.findById(any())).thenReturn(Optional.ofNullable(room));

        // Then
        RoomResponseDto response = accomodationService.getRoomDetail(1L, any());
        assertEquals("room", response.getName());

        verify(roomRepository).findById(any());
    }
}