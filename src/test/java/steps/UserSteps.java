package steps;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import models.User;

import static io.restassured.RestAssured.given;

public class UserSteps {

    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String USER_PATH = "/api/auth/user";

    @Step("Создание пользователя")
    public ValidatableResponse createUser(User user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    @Step("Создание пользователя без email")
    public ValidatableResponse createUserWithoutEmail(User user) {
        User userWithoutEmail = new User(null, user.getPassword(), user.getName());
        return given()
                .header("Content-type", "application/json")
                .body(userWithoutEmail)
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    @Step("Создание пользователя без password")
    public ValidatableResponse createUserWithoutPassword(User user) {
        User userWithoutPassword = new User(user.getEmail(), null, user.getName());
        return given()
                .header("Content-type", "application/json")
                .body(userWithoutPassword)
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    @Step("Создание пользователя без name")
    public ValidatableResponse createUserWithoutName(User user) {
        User userWithoutName = new User(user.getEmail(), user.getPassword(), null);
        return given()
                .header("Content-type", "application/json")
                .body(userWithoutName)
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    @Step("Создание пользователя с пустым телом запроса")
    public ValidatableResponse createUserWithEmptyBody() {
        return given()
                .header("Content-type", "application/json")
                .body("{}")
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    @Step("Авторизация пользователя")
    public ValidatableResponse loginUser(User user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("Авторизация пользователя с пустым телом запроса")
    public ValidatableResponse loginUserWithEmptyBody() {
        return given()
                .header("Content-type", "application/json")
                .body("{}")
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("Удаление пользователя")
    public ValidatableResponse deleteUser(String accessToken) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .when()
                .delete(USER_PATH)
                .then();
    }

}
