package com.restaurant;

import com.restaurant.domain.RestaurantCreateUpdateRequest;
import com.restaurant.domain.entities.Address;
import com.restaurant.domain.entities.OperatingHours;
import com.restaurant.domain.entities.Photo;
import com.restaurant.domain.entities.TimeRange;
import com.restaurant.services.PhotoService;
import com.restaurant.services.RestaurantService;
import com.restaurant.services.impl.RandomTunisiaGeoLocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class RestaurantDataLoaderTest {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RandomTunisiaGeoLocationService geoLocationService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @Rollback(false) // Allow changes to persist
    public void createSampleRestaurants()  {
        List<RestaurantCreateUpdateRequest> restaurants = createRestaurantData();
        restaurants.forEach(restaurant -> {
            String fileName = restaurant.getPhotoIds().getFirst();
            Resource resource = resourceLoader.getResource("classpath:testdata/" + fileName);
            MultipartFile multipartFile;
            try {
                multipartFile = new MockMultipartFile(
                        "file", // parameter name
                        fileName, // original filename
                        MediaType.IMAGE_PNG_VALUE,
                        resource.getInputStream()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            // Call the service method
            Photo uploadedPhoto = photoService.uploadPhoto(multipartFile);

            restaurant.setPhotoIds(List.of(uploadedPhoto.getUrl()));

            restaurantService.createRestaurant(restaurant);

            System.out.println("Created restaurant: " + restaurant.getName());
        });
    }

    private List<RestaurantCreateUpdateRequest> createRestaurantData() {
        return Arrays.asList(
                createRestaurant(
                        "The Golden Dragon",
                        "Chinese",
                        "+44 20 7123 4567",
                        createAddress("12", "Gerrard Street", "W1D 5PR"),
                        createStandardOperatingHours("11:30", "23:00", "11:30", "23:30"),
                        "golden-dragon.png"
                ),
                createRestaurant(
                        "La Petite Maison",
                        "French",
                        "+44 20 7234 5678",
                        createAddress("54", "Brook Street", "W1K 4HR"),
                        createStandardOperatingHours("12:00", "22:30", "12:00", "23:00"),
                        "la-petit-maison.png"
                ),
                createRestaurant(
                        "Raj Pavilion",
                        "Indian",
                        "+44 20 7345 6789",
                        createAddress("27", "Brick Lane", "E1 6PU"),
                        createStandardOperatingHours("12:00", "23:00", "12:00", "23:30"),
                        "raj-pavilion.png"
                ),
                createRestaurant(
                        "Sushi Master",
                        "Japanese",
                        "+44 20 7456 7890",
                        createAddress("8", "Poland Street", "W1F 8PR"),
                        createStandardOperatingHours("11:30", "22:00", "11:30", "22:30"),
                        "sushi-master.png"
                ),
                createRestaurant(
                        "The Rustic Olive",
                        "Italian",
                        "+44 20 7567 8901",
                        createAddress("92", "Dean Street", "W1D 3SR"),
                        createStandardOperatingHours("11:00", "23:00", "11:00", "23:30"),
                        "rustic-olive.png"
                ),
                createRestaurant(
                        "El Toro",
                        "Spanish",
                        "+44 20 7678 9012",
                        createAddress("15", "Charlotte Street", "W1T 1RH"),
                        createStandardOperatingHours("12:00", "23:00", "12:00", "23:30"),
                        "el-toro.png"
                ),
                createRestaurant(
                        "The Greek House",
                        "Greek",
                        "+44 20 7789 0123",
                        createAddress("32", "Store Street", "WC1E 7BS"),
                        createStandardOperatingHours("12:00", "22:30", "12:00", "23:00"),
                        "greek-house.png"
                ),
                createRestaurant(
                        "Seoul Kitchen",
                        "Korean",
                        "+44 20 7890 1234",
                        createAddress("71", "St John Street", "EC1M 4AN"),
                        createStandardOperatingHours("11:30", "22:00", "11:30", "22:30"),
                        "seoul-kitchen.png"
                ),
                createRestaurant(
                        "Thai Orchid",
                        "Thai",
                        "+44 20 7901 2345",
                        createAddress("45", "Warren Street", "W1T 6AD"),
                        createStandardOperatingHours("11:00", "22:30", "11:00", "23:00"),
                        "thai-orchid.png"
                ), createRestaurant(
                        "MAMA",
                        "Thai",
                        "+44 20 7901 2345",
                        createAddress("22", "Dwali", "2100"),
                        createStandardOperatingHours("11:00", "22:30", "11:00", "23:00"),
                        "thai-orchid.png"
                ),

                createRestaurant(
                        "The Burger Joint",
                        "American",
                        "+44 20 7012 3456",
                        createAddress("88", "Commercial Street", "E1 6LY"),
                        createStandardOperatingHours("11:00", "23:00", "11:00", "23:30"),
                        "burger-joint.png"
                )
        );
    }

    private RestaurantCreateUpdateRequest createRestaurant(
            String name,
            String cuisineType,
            String contactInformation,
            Address address,
            OperatingHours operatingHours,
            String photoId
    ) {
        return RestaurantCreateUpdateRequest.builder()
                .name(name)
                .cuisineType(cuisineType)
                .contactInformation(contactInformation)
                .address(address)
                .operatingHours(operatingHours)
                .photoIds(List.of(photoId))
                .website("www.restaurant.com")
                .rangePrice(4)
                .build();
    }

    private Address createAddress(
            String streetNumber,
            String streetName,
            String postalCode
    ) {
        Address address = new Address();
        address.setStreetNumber(streetNumber);
        address.setStreetName(streetName);
        address.setUnit(null);
        address.setCity("London");
        address.setState("Greater London");
        address.setPostalCode(postalCode);
        address.setCountry("United Kingdom");
        return address;
    }

    private OperatingHours createStandardOperatingHours(
            String weekdayOpen,
            String weekdayClose,
            String weekendOpen,
            String weekendClose
    ) {
        TimeRange weekday = new TimeRange();
        weekday.setOpenTime(weekdayOpen);
        weekday.setCloseTime(weekdayClose);

        TimeRange weekend = new TimeRange();
        weekend.setOpenTime(weekendOpen);
        weekend.setCloseTime(weekendClose);

        OperatingHours hours = new OperatingHours();
        hours.setMonday(weekday);
        hours.setTuesday(weekday);
        hours.setWednesday(weekday);
        hours.setThursday(weekday);
        hours.setFriday(weekend);
        hours.setSaturday(weekend);
        hours.setSunday(weekend);

        return hours;
    }
}
