//package com.example.mini.domain.accomodation.service;
//
//import com.example.mini.domain.accomodation.entity.Accomodation;
//import com.example.mini.domain.accomodation.entity.Room;
//import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
//import com.example.mini.domain.accomodation.model.response.AccomodationDetailsResponseDto;
//import com.example.mini.domain.accomodation.model.response.AccomodationResponseDto;
//import com.example.mini.domain.accomodation.model.response.PagedResponse;
//import com.example.mini.domain.accomodation.model.response.RoomResponseDto;
//import com.example.mini.domain.accomodation.repository.AccomodationRepository;
//import com.example.mini.domain.accomodation.repository.AccomodationSearchRepository;
//import com.example.mini.domain.accomodation.repository.RoomRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class AccomodationServiceTest {
//
//    @Mock
//    private AccomodationRepository accomodationRepository;
//
//    @Mock
//    private CategoryRepository categoryRepository;
//
//    @Mock
//    private AccomodationSearchRepository accomodationSearchRepository;
//
//    @Mock
//    private RoomRepository roomRepository;
//
//    @InjectMocks
//    private AccomodationService accomodationService;
//
//    private List<Category> categories;
//    private List<Accomodation> accomodations;
//    private List<Room> rooms;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.initMocks(this);
//
//        categories = new ArrayList<>();
//        categories.add(Category.builder()
//                .id(1L)
//                .name("서울")
//                .build());
//        categories.add(Category.builder()
//                .id(2L)
//                .name("경기")
//                .build());
//        categories.add(Category.builder()
//                .id(3L)
//                .name("인천")
//                .build());
//
//        accomodations = new ArrayList<>();
//        accomodations.add(Accomodation.builder()
//                .name("accomodation 1")
//                .description("accomodation description")
//                .postalCode(123)
//                .address("accomodation 1 address")
//                .parkingAvailable(true)
//                .cookingAvailable(true)
//                .checkIn(LocalDateTime.now())
//                .checkOut(LocalDateTime.now())
//                .category(AccomodationCategory.SEOUL)
//                .build());
//
//        accomodations.add(Accomodation.builder()
//                .name("accomodation 2")
//                .description("accomodation description")
//                .postalCode(456)
//                .address("accomodation 2 address")
//                .parkingAvailable(true)
//                .cookingAvailable(true)
//                .checkIn(LocalDateTime.now())
//                .checkOut(LocalDateTime.now())
//                .category(AccomodationCategory.BUSAN)
//                .build());
//        Page<Accomodation> page = new PageImpl<>(accomodations);
//
//        rooms = new ArrayList<>();
//        rooms.add(Room.builder()
//                .id(1L)
//                .name("room 1")
//                .accomodation(accomodations.get(0))
//                .baseGuests(2)
//                .maxGuests(4)
//                .price(60000)
//                .extraPersonCharge(10000)
//                .build());
//
//        rooms.add(Room.builder()
//                .id(2L)
//                .name("room 2")
//                .accomodation(accomodations.get(0))
//                .baseGuests(2)
//                .maxGuests(4)
//                .price(60000)
//                .extraPersonCharge(10000)
//                .build());
//
//        rooms.add(Room.builder()
//                .id(3L)
//                .name("room 3")
//                .accomodation(accomodations.get(1))
//                .baseGuests(2)
//                .maxGuests(4)
//                .price(60000)
//                .extraPersonCharge(10000)
//                .build());
//
//        rooms.add(Room.builder()
//                .id(4L)
//                .name("room 3")
//                .accomodation(accomodations.get(1))
//                .baseGuests(2)
//                .maxGuests(4)
//                .price(60000)
//                .extraPersonCharge(10000)
//                .build());
//
//    }
//
//
//    @Test
//    void testGetAllAccommodations() {
//        // Mock data
//        Page<Accomodation> page = new PageImpl<>(accomodations);
//
//        // Mock repository behavior
//        when(accomodationRepository.findAll(any(PageRequest.class))).thenReturn(page);
//
//        // Call service method
//        PagedResponse<AccomodationResponseDto> response = accomodationService.getAllAccommodations(1);
//
//        // Assertions
//        assertEquals(1, response.getTotalPages());
//        assertEquals(2, response.getTotalElements());
//        assertEquals(2, response.getContent().size());
//        assertEquals("accomodation 1", response.getContent().get(0).getName());
//        assertEquals("accomodation 2", response.getContent().get(1).getName());
//    }
//
//    @Test
//    void testGetAccommodationsByCategory() {
//        // Mock data
//        Page<Accomodation> page = new PageImpl<>(accomodations);
//
//        // Mock repository behavior
//        when(categoryRepository.findByName("서울")).thenReturn(AccomodationCategory.SEOUL);
//        when(accomodationRepository.findByCategoryId(eq(AccomodationCategory.SEOUL.getName()), any(PageRequest.class))).thenReturn(page);
//
//        // Call service method
//        PagedResponse<AccomodationResponseDto> response = accomodationService.getAccommodationsByCategory("서울", 1);
//
//        // Assertions
//        assertEquals(1, response.getTotalPages());
//        assertEquals(2, response.getTotalElements());
//        assertEquals(2, response.getContent().size());
//        assertEquals("accomodation 1", response.getContent().get(0).getName());
//        assertEquals("accomodation 2", response.getContent().get(1).getName());
//    }
//
//    @Test
//    void testSearchByAccommodationName() {
//    }
//
//    @Test
//    void testGetAccommodationDetails() {
//        Long accomodationId = 1L;
//        // Mock repository behavior
//        when(accomodationRepository.findById(accomodationId)).thenReturn(Optional.of(accomodations.get(0)));
//        when(roomRepository.findByAccomodationId(accomodationId)).thenReturn(rooms);
//
//        // Call service method
//        AccomodationDetailsResponseDto response = accomodationService.getAccomodationDetails(accomodationId);
//
//        // Assertions
//        assertNotNull(response);
//        assertEquals("accomodation 1", response.getAccomodation().getName());
//        assertEquals(4, response.getRooms().size());
//        assertEquals("room 1", response.getRooms().get(0).getName());
//    }
//
//    @Test
//    void testGetRoomDetail() {
//        Long accomodationId = 1L;
//        Long roomId = 1L;
//
//        // Mock repository behavior
//        when(roomRepository.findById(roomId)).thenReturn(Optional.of(rooms.get(0)));
//
//        // Call service method
//        RoomResponseDto response = accomodationService.getRoomDetail(accomodationId, roomId);
//
//        // Assertions
//        assertNotNull(response);
//        assertEquals("room 1", response.getName());
//    }
//}