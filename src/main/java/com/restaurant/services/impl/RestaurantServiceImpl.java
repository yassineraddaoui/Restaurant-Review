package com.restaurant.services.impl;

import co.elastic.clients.elasticsearch._types.DistanceUnit;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.restaurant.domain.GeoLocation;
import com.restaurant.domain.PriceRange;
import com.restaurant.domain.RestaurantCreateUpdateRequest;
import com.restaurant.domain.entities.Address;
import com.restaurant.domain.entities.Photo;
import com.restaurant.domain.entities.Restaurant;
import com.restaurant.exceptions.RestaurantNotFoundException;
import com.restaurant.repositories.RestaurantRepository;
import com.restaurant.services.GeoLocationService;
import com.restaurant.services.RestaurantService;
import jakarta.validation.Valid;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final GeoLocationService geoLocationService;
    private final ElasticsearchOperations elasticsearchOperations;

    private static Restaurant createRestaurant(RestaurantCreateUpdateRequest request, GeoPoint geoPoint, List<Photo> photos) {
        return Restaurant.builder()
                .name(request.getName())
                .cuisineType(request.getCuisineType())
                .contactInformation(request.getContactInformation())
                .address(request.getAddress())
                .geoLocation(geoPoint)
                .operatingHours(request.getOperatingHours())
                .averageRating(0f)
                .photos(photos)
                .website(request.getWebsite())
                .rangePrice(PriceRange.fromValue(request.getRangePrice()))
                .build();
    }

    private static void updateRestaurantDetails(RestaurantCreateUpdateRequest request, Restaurant restaurant, GeoPoint newGeoPoint, List<Photo> photos) {
        restaurant.setName(request.getName());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setContactInformation(request.getContactInformation());
        restaurant.setAddress(request.getAddress());
        restaurant.setGeoLocation(newGeoPoint);
        restaurant.setOperatingHours(request.getOperatingHours());
        restaurant.setPhotos(photos);
        restaurant.setWebsite(request.getWebsite());
        restaurant.setRangePrice(PriceRange.fromValue(request.getRangePrice()));
    }

    private static NativeQueryBuilder filterQuery(PageRequest of, String cuisineType, Float minRating, Double latitude, Double longitude, Double maxDistanceKm, boolean filterOpenNow, boolean requirePhotos, String createdById, String address, Integer priceRange) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        filterByCity(address, boolQueryBuilder);
        filterByCuisineType(cuisineType, boolQueryBuilder);
        filterByAverageRating(minRating, boolQueryBuilder);
        filterByGeoLocation(latitude, longitude, maxDistanceKm, boolQueryBuilder);
        filterByOpeningHours(filterOpenNow, boolQueryBuilder);
        filterByPhoto(requirePhotos, boolQueryBuilder);
        filterByCreatedBy(createdById, boolQueryBuilder);
        filterByPriceRange(priceRange, boolQueryBuilder);

        return new NativeQueryBuilder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withPageable(of);
    }

    private static void filterByPriceRange(Integer priceRange, BoolQuery.Builder boolQueryBuilder) {
        if (priceRange != null) {
            boolQueryBuilder.must(Query.of(q -> q
                    .range(r -> r
                            .number(nr -> nr
                                    .field("averageRating")
                                    .gte(priceRange.doubleValue())
                            )
                    )
            ));
        }
    }

    private static void sortByDistance(Double latitude, Double longitude, NativeQueryBuilder queryBuilder) {
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
    }

    private static void filterByGeoLocation(Double latitude, Double longitude, Double maxDistanceKm, BoolQuery.Builder boolQueryBuilder) {
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
    }

    private static void filterByCreatedBy(String createdById, BoolQuery.Builder boolQueryBuilder) {
        if (createdById != null) {
            boolQueryBuilder.must(Query.of(q -> q
                    .nested(n -> n
                            .path("createdBy")
                            .query(q2 -> q2.term(t -> t.field("createdBy.id").value(createdById)))
                    )
            ));
        }
    }

    private static void filterByPhoto(boolean requirePhotos, BoolQuery.Builder boolQueryBuilder) {
        if (requirePhotos) {
            boolQueryBuilder.must(Query.of(q -> q
                    .nested(n -> n
                            .path("photos")
                            .query(q2 -> q2.exists(e -> e.field("photos.id")))
                    )
            ));
        }
    }

    private static void filterByOpeningHours(boolean filterOpenNow, BoolQuery.Builder boolQueryBuilder) {
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
    }

    private static void filterByAverageRating(Float minRating, BoolQuery.Builder boolQueryBuilder) {
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
    }

    private static void filterByCuisineType(String cuisineType, BoolQuery.Builder boolQueryBuilder) {
        if (cuisineType != null) {
            boolQueryBuilder.must(Query.of(q -> q
                    .match(t -> t
                            .field("cuisineType")
                            .query(cuisineType.toLowerCase())
                    )
            ));
        }
    }

    private static void filterByCity(String address, BoolQuery.Builder boolQueryBuilder) {
        if (address != null && !address.isBlank()) {
            boolQueryBuilder.must(Query.of(q -> q
                    .nested(n -> n
                            .path("address")
                            .query(q2 -> q2
                                    .bool(b -> b
                                            .should(
                                                    Query.of(t -> t.match(m -> m
                                                            .field("address.city")
                                                            .query(address)
                                                            .fuzziness("AUTO")
                                                    )),
                                                    Query.of(t -> t.match(m -> m
                                                            .field("address.streetName")
                                                            .query(address)
                                                            .fuzziness("AUTO")
                                                    )),
                                                    Query.of(t -> t.match(m -> m
                                                            .field("address.country")
                                                            .query(address)
                                                            .fuzziness("AUTO")
                                                    ))
                                            )
                                            .minimumShouldMatch("1")
                                    )
                            )
                    )
            ));
        }
    }

    @Override
    public Restaurant createRestaurant(@Valid RestaurantCreateUpdateRequest request) {
        AddressInfo addressInfo = buildAddressInfo(request);

        var photos = Photo.buildPhotos(request.getPhotoIds());

        Restaurant restaurant = createRestaurant(request, addressInfo.geoPoint(), photos);

        return restaurantRepository.save(restaurant);
    }

    private AddressInfo buildAddressInfo(@Valid RestaurantCreateUpdateRequest request) {
        Address address = request.getAddress();
        GeoLocation geoLocation = geoLocationService.geoLocate(address);
        GeoPoint geoPoint = new GeoPoint(geoLocation.getLatitude(), geoLocation.getLongitude());
        return new AddressInfo(address, geoPoint);
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
            String createdById,
            String address,
            Integer priceRange) {

        NativeQueryBuilder queryBuilder = filterQuery(of, cuisineType, minRating, latitude, longitude, maxDistanceKm, filterOpenNow, requirePhotos, createdById, address, priceRange);

        sortByDistance(latitude, longitude, queryBuilder);


        var searchHits = elasticsearchOperations.search(queryBuilder.build(), Restaurant.class);
        var content = searchHits
                .stream()
                .map(SearchHit::getContent)
                .toList();
        return new PageImpl<>(content, of, searchHits.getTotalHits());
    }

    @Override
    public Page<Restaurant> getAllRestaurants(PageRequest pageRequest) {
        return restaurantRepository.findAll(pageRequest);
    }

    @Override
    public Optional<Restaurant> getRestaurantById(String id) {
        return restaurantRepository.findById(id);
    }

    @Override
    public Restaurant updateRestaurant(String id, @Valid RestaurantCreateUpdateRequest request) {
        Restaurant restaurant = getRestaurantOrThrows(id);

        AddressInfo addressInfo = buildAddressInfo(request);
        List<Photo> photos = Photo.buildPhotos(request.getPhotoIds());

        updateRestaurantDetails(request, restaurant, addressInfo.geoPoint, photos);

        return restaurantRepository.save(restaurant);

    }

    private Restaurant getRestaurantOrThrows(String id) {
        return getRestaurantById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant with ID does not exist: " + id));
    }

    @Override
    public void deleteRestaurant(String restaurantId) {
        restaurantRepository.deleteById(restaurantId);
    }

    private record AddressInfo(Address address, GeoPoint geoPoint) {
    }


}
