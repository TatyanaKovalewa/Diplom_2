package tests;

import constants.Config;
import constants.TestConstants;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.UserSteps;

import java.util.UUID;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserLoginTests {
    private UserSteps userSteps;
    private User registeredUser;  // Переименовано для ясности
    private String accessToken;

    @Before
    @Step("Настройка тестового окружения и создание пользователя для тестов")
    public void setUp() {
        RestAssured.baseURI = Config.BASE_URL;
        userSteps = new UserSteps();

        // Создаем пользователя для всех тестов, которым нужен существующий пользователь
        createAndRegisterUser();
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        // Удаляем созданного пользователя после теста
        if (accessToken != null && !accessToken.isEmpty()) {
            ValidatableResponse deleteResponse = userSteps.deleteUser(accessToken);
            deleteResponse.statusCode(SC_ACCEPTED);
            // Сбрасываем токен после удаления
            accessToken = null;
        }
    }

    // Вспомогательный метод для создания и регистрации пользователя
    private void createAndRegisterUser() {
        String uniqueEmail = "test_" + System.currentTimeMillis() + TestConstants.EMAIL_DOMAIN;
        registeredUser = new User(uniqueEmail, TestConstants.DEFAULT_PASSWORD, TestConstants.DEFAULT_USER_NAME);

        ValidatableResponse createResponse = userSteps.createUser(registeredUser);
        createResponse.statusCode(SC_OK);
        accessToken = createResponse.extract().path("accessToken");
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверка успешного входа существующего пользователя")
    public void loginExistingUserTest() {
        // Используем пользователя, созданного в @Before
        ValidatableResponse loginResponse = userSteps.loginUser(registeredUser);

        loginResponse.statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(registeredUser.getEmail()))
                .body("user.name", equalTo(registeredUser.getName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Вход с неверным логином")
    @Description("Проверка ошибки при входе с несуществующим email")
    public void loginWithInvalidEmailTest() {
        // Создаем пользователя с несуществующим email для этого теста
        String nonExistentEmail = "nonexistent_" + UUID.randomUUID() + TestConstants.EMAIL_DOMAIN;
        User invalidUser = new User(nonExistentEmail, TestConstants.DEFAULT_PASSWORD, TestConstants.DEFAULT_USER_NAME);

        ValidatableResponse loginResponse = userSteps.loginUser(invalidUser);

        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(TestConstants.ERROR_INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Проверка ошибки при входе с неправильным паролем")
    public void loginWithInvalidPasswordTest() {
        // Используем пользователя, созданного в @Before, но с неверным паролем
        User invalidUser = new User(registeredUser.getEmail(), TestConstants.INCORRECT_PASSWORD, registeredUser.getName());

        ValidatableResponse loginResponse = userSteps.loginUser(invalidUser);

        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(TestConstants.ERROR_INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("Вход с неверным логином и паролем")
    @Description("Проверка ошибки при входе с несуществующим email и неправильным паролем")
    public void loginWithInvalidEmailAndPasswordTest() {
        // Создаем полностью невалидного пользователя
        String nonExistentEmail = "nonexistent_" + System.currentTimeMillis() + TestConstants.EMAIL_DOMAIN;
        User invalidUser = new User(nonExistentEmail, TestConstants.INCORRECT_PASSWORD, TestConstants.DEFAULT_USER_NAME);

        ValidatableResponse loginResponse = userSteps.loginUser(invalidUser);

        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(TestConstants.ERROR_INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("Вход без email")
    @Description("Проверка ошибки при входе без поля email")
    public void loginWithoutEmailTest() {
        User userWithoutEmail = new User(null, TestConstants.DEFAULT_PASSWORD, TestConstants.DEFAULT_USER_NAME);

        ValidatableResponse loginResponse = userSteps.loginUser(userWithoutEmail);

        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(TestConstants.ERROR_INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("Вход без пароля")
    @Description("Проверка ошибки при входе без поля password")
    public void loginWithoutPasswordTest() {
        String testEmail = "test" + System.currentTimeMillis() + TestConstants.EMAIL_DOMAIN;
        User userWithoutPassword = new User(testEmail, null, TestConstants.DEFAULT_USER_NAME);

        ValidatableResponse loginResponse = userSteps.loginUser(userWithoutPassword);

        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(TestConstants.ERROR_INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("Вход с пустым телом запроса")
    @Description("Проверка ошибки при входе с пустым телом")
    public void loginWithEmptyBodyTest() {
        ValidatableResponse loginResponse = userSteps.loginUserWithEmptyBody();

        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(TestConstants.ERROR_INVALID_CREDENTIALS));
    }
}