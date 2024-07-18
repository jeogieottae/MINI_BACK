package com.example.mini.domain.accomodation.converter;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.model.response.AccomodationCardResponseDto;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccomodationConverter {

	private final RoomRepository roomRepository;

	/**
	 * Entity → Dto 변환 및 응답 객체로 변환하는 메서드
	 *
	 * @param accommodations    변환할 객체
	 * @return                  숙소 정보 목록을 포함한 응답 객체
	 */

	public PagedResponse<AccomodationCardResponseDto> convertToPagedResponse(
			Page<Accomodation> accommodations, String checkIn, String checkOut, Optional<Long> memberId, AccomodationService accomodationService) {
		List<AccomodationCardResponseDto> content = accommodations.getContent().stream().map(accommodation -> {
			Integer minPrice = roomRepository.findMinPriceByAccommodationId(accommodation.getId());
			boolean isAvailable = roomRepository.findByAccomodationId(accommodation.getId())
				.stream()
				.anyMatch(room -> accomodationService.getReservationAvailable(checkIn, checkOut, room.getId()));

			boolean isLiked = false;
			if (memberId.isPresent()) {
				isLiked = accomodationService.getIsLiked(memberId.get(), accommodation.getId());
			}

			return AccomodationCardResponseDto.toDto(accommodation, minPrice, isAvailable, isLiked);
		}).collect(Collectors.toList());

		return new PagedResponse<>(accommodations.getTotalPages(), accommodations.getTotalElements(), content);
	}

	public static Optional<Long> convertToMemberId(UserDetailsImpl userDetails) {
		return (userDetails==null) ? Optional.empty() :userDetails.getMemberId().describeConstable();
	}
}
