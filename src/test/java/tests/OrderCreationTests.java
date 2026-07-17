package tests;

import constants.Config;
import constants.TestConstants;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import models.Order;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.IngredientSteps;
import steps.OrderSteps;
import steps.UserSteps;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderCreationTests {
    private UserSteps userSteps;
    private OrderSteps orderSteps;
    private IngredientSteps ingredientSteps;
    private String accessToken;

    private String firstValidIngredientId;
    private String secondValidIngredientId;

    @Before
    @Step("Настройка тестового окружения")
    public void setUp() {
        RestAssured.baseURI = Config.BASE_URL;
        userSteps = new UserSteps();
        orderSteps = new OrderSteps();
        ingredientSteps = new IngredientSteps();

        // Получаем валидные ID ингредиентов динамически
        List<String> ingredientIds = ingredientSteps.getTwoIngredientIds();
        firstValidIngredientId = ingredientIds.get(0);
        secondValidIngredientId = ingredientIds.get(1);

        System.out.println("First valid ingredient ID: " + firstValidIngredientId);
        System.out.println("Second valid ingredient ID: " + secondValidIngredientId);

        // Создаем пользователя для тестов с авторизацией
        createAndRegisterUser();
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            ValidatableResponse deleteResponse = userSteps.deleteUser(accessToken);
            deleteResponse.statusCode(SC_ACCEPTED);
            accessToken = null;  // Сбрасываем токен после удаления
        }
    }

    // Вспомогательный метод для создания и регистрации пользователя
    private void createAndRegisterUser() {
        String uniqueEmail = "test_" + System.currentTimeMillis() + TestConstants.EMAIL_DOMAIN;
        User registeredUser = new User(uniqueEmail, TestConstants.DEFAULT_PASSWORD, TestConstants.DEFAULT_USER_NAME);

        ValidatableResponse createResponse = userSteps.createUser(registeredUser);
        createResponse.statusCode(SC_OK);
        accessToken = createResponse.extract().path("accessToken");
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Проверка успешного создания заказа авторизованным пользователем")
    public void createOrderWithAuthAndIngredientsTest() {
        List<String> ingredients = Arrays.asList(firstValidIngredientId, secondValidIngredientId);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderSteps.createOrderWithAuth(order, accessToken);

        response.statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации, но с ингредиентами")
    @Description("Проверка создания заказа неавторизованным пользователем")
    public void createOrderWithoutAuthButWithIngredientsTest() {
        List<String> ingredients = Arrays.asList(firstValidIngredientId, secondValidIngredientId);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderSteps.createOrderWithoutAuth(order);

        response.statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией, но без ингредиентов")
    @Description("Проверка ошибки при создании заказа без ингредиентов")
    public void createOrderWithAuthButWithoutIngredientsTest() {
        Order order = new Order(Collections.emptyList());

        ValidatableResponse response = orderSteps.createOrderWithAuth(order, accessToken);

        response.statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo(TestConstants.ERROR_INGREDIENTS_REQUIRED));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и неверным хешем ингредиента")
    @Description("Проверка ошибки при создании заказа с невалидным хешем ингредиента")
    public void createOrderWithInvalidIngredientHashTest() {
        List<String> ingredients = Arrays.asList(TestConstants.INVALID_INGREDIENT_HASH, secondValidIngredientId);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderSteps.createOrderWithAuth(order, accessToken);

        response.statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и частично неверным хешем ингредиента")
    @Description("Проверка ошибки при создании заказа с одним невалидным и одним валидным ингредиентом")
    public void createOrderWithPartialInvalidIngredientsTest() {
        List<String> ingredients = Arrays.asList(firstValidIngredientId, TestConstants.INVALID_INGREDIENT_HASH);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderSteps.createOrderWithAuth(order, accessToken);

        response.statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и несуществующим ID ингредиента")
    @Description("Проверка ошибки при создании заказа с несуществующим ID")
    public void createOrderWithNonExistentIngredientTest() {
        List<String> ingredients = Arrays.asList(TestConstants.NON_EXISTENT_INGREDIENT_ID, secondValidIngredientId);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderSteps.createOrderWithAuth(order, accessToken);

        response.statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Проверка получения списка ингредиентов")
    @Description("Проверка, что API возвращает список ингредиентов")
    public void getIngredientsListTest() {
        ValidatableResponse response = ingredientSteps.getIngredients();

        response.statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("data", notNullValue())
                .body("data.size()", notNullValue());
    }
}