package com.restaurant.mappers;


import com.restaurant.domain.ReviewCreateUpdateRequest;
import com.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.restaurant.domain.dtos.ReviewDto;
import com.restaurant.domain.entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    ReviewCreateUpdateRequest toReviewCreateUpdateRequest(ReviewCreateUpdateRequestDto dto);

    ReviewDto toDto(Review review);

}
