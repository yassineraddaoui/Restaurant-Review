package com.restaurant.services.impl;

import co.elastic.clients.elasticsearch._types.DistanceUnit;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.restaurant.domain.GeoLocation;
import com.restaurant.domain.RestaurantCreateUpdateRequest;
import com.restaurant.domain.entities.Address;
import com.restaurant.domain.entities.Photo;
import com.restaurant.domain.entities.Restaurant;
import com.restaurant.repositories.RestaurantRepository;
import com.restaurant.services.GeoLocationService;
import com.restaurant.services.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final GeoLocationService geoLocationService;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public Restaurant createRestaurant(RestaurantCreateUpdateRequest request) {
        Address address = request.getAddress();
        GeoLocation geoLocation = geoLocationService.geoLocate(address);
        GeoPoint geoPoint = new GeoPoint(geoLocation.getLatitude(), geoLocation.getLongitude());

        List<String> photoIds = request.getPhotoIds();
        List<Photo> photos = photoIds.stream().map(photoUrl -> Photo.builder()
                .url(photoUrl)
                .uploadDate(LocalDateTime.now())
                .build()).toList();

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .cuisineType(request.getCuisineType())
                .contactInformation(request.getContactInformation())
                .address(address)
                .geoLocation(geoPoint)
                .operatingHours(request.getOperatingHours())
                .averageRating(0f)
                .photos(photos)
                .build();

        return restaurantRepository.save(restaurant);
    }

    @Override
    public Page<Restaurant> searchRestaurants(
            PageRequest of,
            String cuisineType,
            Float minRating,
            Double latitude,
            Double longitude,
            Double maxDistanceKm,
            boolean filterOpenNow,
            boolean requirePhotos,
            String createdById) {

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();


        if (cuisineType != null) {
            boolQueryBuilder.must(Query.of(q -> q
                    .match(t -> t
                            .field("cuisineType")
                            .query(cuisineType.toLowerCase())
                    )
            ));
        }


        // 3. Average Rating Filter
        if (minRating != null) {
            boolQueryBuilder.must(Query.of(q -> q
                    .range(r -> r
                            .number(nr -> nr
                                    .field("averageRating")
                                    .gte(minRating.doubleValue())
                            )
                    )
            ));
        }

        // 4. Geo-Location Filter
        if (latitude != null && longitude != null && maxDistanceKm != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                    .geoDistance(g -> g
                            .field("geoLocation")
                            .distance(maxDistanceKm + "km")
                            .location(gl -> gl
                                    .latlon(l -> l
                                            .lat(latitude)
                                            .lon(longitude)
                                    )
                            )
                    )
            ));
        }

        // 5. Open Now Filter (nested query)
        if (filterOpenNow) {
            DayOfWeek currentDay = DayOfWeek.from(java.time.LocalDate.now());
            String currentTime = LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

            Query openHoursQuery = Query.of(q -> q
                    .bool(b -> b
                            .must(
                                    // Day of week match
                                    Query.of(q2 -> q2
                                            .term(t -> t
                                                    .field("operatingHours.dayOfWeek")
                                                    .value(currentDay.toString())
                                            )
                                    ),
                                    // Open time <= current time (using term range for string comparison)
                                    Query.of(q2 -> q2
                                            .range(r -> r
                                                    .term(tr -> tr
                                                            .field("operatingHours.openTime")
                                                            .lte(currentTime)
                                                    )
                                            )
                                    ),
                                    // Close time >= current time
                                    Query.of(q2 -> q2
                                            .range(r -> r
                                                    .term(tr -> tr
                                                            .field("operatingHours.closeTime")
                                                            .gte(currentTime)
                                                    )
                                            )
                                    )
                            )
                    )
            );

            boolQueryBuilder.must(Query.of(q -> q
                    .nested(n -> n
                            .path("operatingHours")
                            .query(openHoursQuery)
                    )
            ));
        }


        // 6. Photos Availability Filter
        if (requirePhotos) {
            boolQueryBuilder.must(Query.of(q -> q
                    .nested(n -> n
                            .path("photos")
                            .query(q2 -> q2.exists(e -> e.field("photos.id")))
                    )
            ));
        }

        // 7. Created By Filter
        if (createdById != null) {
            boolQueryBuilder.must(Query.of(q -> q
                    .nested(n -> n
                            .path("createdBy")
                            .query(q2 -> q2.term(t -> t.field("createdBy.id").value(createdById)))
                    )
            ));
        }

        // 8. Build the query with sorting
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withPageable(of);

        if (latitude != null && longitude != null) {
            queryBuilder.withSort(s -> s
                    .geoDistance(g -> g
                            .field("geoLocation")
                            .location(gl -> gl.latlon(l -> l.lat(latitude).lon(longitude)))
                            .order(SortOrder.Asc)
                            .unit(DistanceUnit.Kilometers)
                    )
            );
        }
        var content = elasticsearchOperations.search(queryBuilder.build(), Restaurant.class)
                .stream()
                .map(SearchHit::getContent)
                .toList();

        return new PageImpl<>(content, of, content.size());
    }

    @Override
    public Page<Restaurant> getAllRestaurants(PageRequest pageRequest) {
        return restaurantRepository.findAll(pageRequest);
    }
}
