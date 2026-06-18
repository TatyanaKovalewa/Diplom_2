import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserCreationTests {
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
    @DisplayName("Создание уникального пользователя")
    @Description("Проверка успешного создания нового пользователя")
    public void createUniqueUserTest() {
        ValidatableResponse response = userSteps.createUser(user);

        response.statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());

        // Сохраняем токен для удаления пользователя
        accessToken = response.extract().path("accessToken");
    }

    @Test
    @DisplayName("Создание уже существующего пользователя")
    @Description("Проверка ошибки при попытке создания дублирующего пользователя")
    public void createExistingUserTest() {
        // Сначала создаем пользователя
        ValidatableResponse firstResponse = userSteps.createUser(user);
        firstResponse.statusCode(SC_OK);
        accessToken = firstResponse.extract().path("accessToken");

        // Пытаемся создать такого же пользователя повторно
        ValidatableResponse secondResponse = userSteps.createUser(user);
        secondResponse.statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    @Description("Проверка ошибки при создании пользователя без поля email")
    public void createUserWithoutEmailTest() {
        ValidatableResponse response = userSteps.createUserWithoutEmail(user);

        response.statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без password")
    @Description("Проверка ошибки при создании пользователя без поля password")
    public void createUserWithoutPasswordTest() {
        ValidatableResponse response = userSteps.createUserWithoutPassword(user);

        response.statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без name")
    @Description("Проверка ошибки при создании пользователя без поля name")
    public void createUserWithoutNameTest() {
        ValidatableResponse response = userSteps.createUserWithoutName(user);

        response.statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя с пустым телом запроса")
    @Description("Проверка ошибки при создании пользователя с пустым телом")
    public void createUserWithEmptyBodyTest() {
        ValidatableResponse response = userSteps.createUserWithEmptyBody();

        response.statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}
