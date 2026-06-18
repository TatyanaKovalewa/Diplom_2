import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderCreationTests {
    private UserSteps userSteps;
    private OrderSteps orderSteps;
    private IngredientSteps ingredientSteps;  // НОВОЕ
    private User user;
    private String accessToken;

    private String validIngredient1;
    private String validIngredient2;
    private static final String INVALID_INGREDIENT = "invalid_hash_123";

    @Before
    @Step("Настройка тестового окружения")
    public void setUp() {
        RestAssured.baseURI = Config.BASE_URL;
        userSteps = new UserSteps();
        orderSteps = new OrderSteps();
        ingredientSteps = new IngredientSteps();  // НОВОЕ

        // Получаем валидные ID ингредиентов динамически
        List<String> ingredientIds = ingredientSteps.getTwoIngredientIds();
        validIngredient1 = ingredientIds.get(0);
        validIngredient2 = ingredientIds.get(1);

        System.out.println("Valid ingredient 1: " + validIngredient1);
        System.out.println("Valid ingredient 2: " + validIngredient2);

        // Создаем пользователя для тестов с авторизацией
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@yandex.ru";
        user = new User(uniqueEmail, "password123", "TestUser");

        ValidatableResponse createResponse = userSteps.createUser(user);
        createResponse.statusCode(SC_OK);
        accessToken = createResponse.extract().path("accessToken");

        System.out.println("Access Token: " + accessToken);
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            ValidatableResponse deleteResponse = userSteps.deleteUser(accessToken);
            deleteResponse.statusCode(SC_ACCEPTED);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Проверка успешного создания заказа авторизованным пользователем")
    public void createOrderWithAuthAndIngredientsTest() {
        List<String> ingredients = Arrays.asList(validIngredient1, validIngredient2);
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
        List<String> ingredients = Arrays.asList(validIngredient1, validIngredient2);
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
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и неверным хешем ингредиента")
    @Description("Проверка ошибки при создании заказа с невалидным хешем ингредиента")
    public void createOrderWithInvalidIngredientHashTest() {
        List<String> ingredients = Arrays.asList(INVALID_INGREDIENT, validIngredient2);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderSteps.createOrderWithAuth(order, accessToken);

        response.statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и частично неверным хешем ингредиента")
    @Description("Проверка ошибки при создании заказа с одним невалидным и одним валидным ингредиентом")
    public void createOrderWithPartialInvalidIngredientsTest() {
        List<String> ingredients = Arrays.asList(validIngredient1, INVALID_INGREDIENT);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderSteps.createOrderWithAuth(order, accessToken);

        response.statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и несуществующим ID ингредиента")
    @Description("Проверка ошибки при создании заказа с несуществующим ID")
    public void createOrderWithNonExistentIngredientTest() {
        // Генерируем несуществующий ID
        String nonExistentId = "60d3b41abdacab026a733c7";
        List<String> ingredients = Arrays.asList(nonExistentId, validIngredient2);
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