package de.olexiy.testing.example.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Regres API tests")
public class RegresTest {
    private final static String URL = "https://reqres.in";

    @Test
    @DisplayName("Avatar filename should contain user id and email should end with @reqres.in")
    public void checkAvatarAndIdTest() {
        Specifications.installSpecifications(
                Specifications.requestSpecification(URL),
                Specifications.responseSpecificationOk200()
        );

        List<UserData> users = given()
                .when()
                .get("/api/users?page=2")
                .then()
                .log().all()
                .extract().body().jsonPath().getList("data", UserData.class);

        users.forEach(x -> assertTrue(x.avatar().contains(x.id().toString())));
        assertTrue(users.stream().allMatch(x -> x.email().endsWith("@reqres.in")));
    }

    @Test
    @DisplayName("Registration should return token")
    public void successRegistrationTest() {
        Specifications.installSpecifications(
                Specifications.requestSpecification(URL),
                Specifications.responseSpecificationOk200()
        );

        RegistrationData user = new RegistrationData("eve.holt@reqres.in", "pistol");

        RegistrationResponse registrationResponse = given()
                .body(user)
                .when()
                .post("/api/register")
                .then()
                .log().all()
                .extract().body().as(RegistrationResponse.class);

        assertNotNull(registrationResponse.id());
        assertNotNull(registrationResponse.token());
        assertEquals(registrationResponse.id(), "4");
        assertEquals(registrationResponse.token(), "QpwL5tke4Pnpja7X4");
    }

    @Test
    @DisplayName("Registration without password should return error")
    public void unSuccessUserRegTest() {
        Specifications.installSpecifications(
                Specifications.requestSpecification(URL),
                Specifications.responseSpecificationError400());

        RegistrationData userWithoutPassword = new RegistrationData("sydney@fife", "");
        UnsuccessfullyRegistrationResponse unSuccessUserReg = given()
                .body(userWithoutPassword)
                .when()
                .post("/api/register")
                .then()
                .log().body()
                .extract().as(UnsuccessfullyRegistrationResponse.class);
        assertNotNull(unSuccessUserReg.error());
        assertEquals("Missing password", unSuccessUserReg.error());
    }

    @Test
    @DisplayName("Check that all entries in colors endpoint response are sorted by year")
    public void  sortedYearsTest() {
        Specifications.installSpecifications(
                Specifications.requestSpecification(URL),
                Specifications.responseSpecificationOk200()
        );

        List<ColorData> data = given()
                .when()
                .get("/api/unknown")
                .then()
                .log().all()
                .extract().body().jsonPath().getList("data", ColorData.class);

        List<Integer> dataYears = data.stream().map(ColorData::year).toList();
        List<Integer> sortedDataYears = dataYears.stream().sorted().toList();
        assertEquals(dataYears, sortedDataYears);

    }

    @Test
    @DisplayName("Check that user with id 2 is deleted")
    public void deleteUserTest() {
        Specifications.installSpecifications(
                Specifications.requestSpecification(URL),
                Specifications.responseSpecificationOk204()
        );

        given()
                .when()
                .delete("/api/users/2")
                .then()
                .log().all();
    }

    @Test
    @DisplayName("Check if local time and server time are the same")
    public void timeTest() {
        Specifications.installSpecifications(
                Specifications.requestSpecification(URL),
                Specifications.responseSpecificationOk200()
        );

        UserStatusData userStatusData = new UserStatusData("morpheus", "zion resident");

        UserStatusResponse response = given()
                .body(userStatusData)
                .when()
                .put("/api/users/2")
                .then().log().all()
                .extract().as(UserStatusResponse.class);

        String regex = "(.{5})$"; // regex to get last 5 symbols from string
        String currentTime = Clock.systemUTC().instant().toString().replaceAll(regex, "");
        assertEquals(currentTime, response.updatedAt());



    }

}
