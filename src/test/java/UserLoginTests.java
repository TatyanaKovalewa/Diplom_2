import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserLoginTests {
    private UserSteps userSteps;
    private User user;
    private String accessToken;

    @Before
    @Step("Настройка тестового окружения")
    public void setUp() {
        RestAssured.baseURI = Config.BASE_URL;
        userSteps = new UserSteps();
        // Генерируем уникальные данные для пользователя
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@yandex.ru";
        user = new User(uniqueEmail, "password123", "TestUser");
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        // Удаляем созданного пользователя после теста
        if (accessToken != null && !accessToken.isEmpty()) {
            ValidatableResponse deleteResponse = userSteps.deleteUser(accessToken);
            deleteResponse.statusCode(SC_ACCEPTED);
        }
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверка успешного входа существующего пользователя")
    public void loginExistingUserTest() {
        // Сначала создаем пользователя
        ValidatableResponse createResponse = userSteps.createUser(user);
        createResponse.statusCode(SC_OK);
        accessToken = createResponse.extract().path("accessToken");

        // Пытаемся войти с созданными учетными данными
        ValidatableResponse loginResponse = userSteps.loginUser(user);
        loginResponse.statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Вход с неверным логином")
    @Description("Проверка ошибки при входе с несуществующим email")
    public void loginWithInvalidEmailTest() {
        // Генерируем уникальный email, который точно не существует
        String uniqueEmail = "nonexistent_" + UUID.randomUUID().toString() + "@yandex.ru";
        User invalidUser = new User(uniqueEmail, "password123", "TestUser");

        ValidatableResponse loginResponse = userSteps.loginUser(invalidUser);
        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Проверка ошибки при входе с неправильным паролем")
    public void loginWithInvalidPasswordTest() {
        // Сначала создаем пользователя
        ValidatableResponse createResponse = userSteps.createUser(user);
        createResponse.statusCode(SC_OK);
        accessToken = createResponse.extract().path("accessToken");

        // Пытаемся войти с неверным паролем
        User invalidUser = new User(user.getEmail(), "wrong_password", user.getName());
        ValidatableResponse loginResponse = userSteps.loginUser(invalidUser);
        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным логином и паролем")
    @Description("Проверка ошибки при входе с несуществующим email и неправильным паролем")
    public void loginWithInvalidEmailAndPasswordTest() {
        User invalidUser = new User("nonexistent_" + System.currentTimeMillis() + "@yandex.ru", "wrong_password", "TestUser");

        ValidatableResponse loginResponse = userSteps.loginUser(invalidUser);
        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход без email")
    @Description("Проверка ошибки при входе без поля email")
    public void loginWithoutEmailTest() {
        User userWithoutEmail = new User(null, "password123", "TestUser");

        ValidatableResponse loginResponse = userSteps.loginUser(userWithoutEmail);
        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход без пароля")
    @Description("Проверка ошибки при входе без поля password")
    public void loginWithoutPasswordTest() {
        User userWithoutPassword = new User("test@yandex.ru", null, "TestUser");

        ValidatableResponse loginResponse = userSteps.loginUser(userWithoutPassword);
        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с пустым телом запроса")
    @Description("Проверка ошибки при входе с пустым телом")
    public void loginWithEmptyBodyTest() {
        ValidatableResponse loginResponse = userSteps.loginUserWithEmptyBody();

        loginResponse.statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
