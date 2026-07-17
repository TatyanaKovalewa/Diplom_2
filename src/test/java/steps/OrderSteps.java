package steps;

import models.Order;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class OrderSteps {
    private static final String ORDER_PATH = "/api/orders";

    @Step("Создание заказа с авторизацией")
    public ValidatableResponse createOrderWithAuth(Order order, String accessToken) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .body(order)
                .when()
                .post(ORDER_PATH)
                .then();
    }

    @Step("Создание заказа без авторизации")
    public ValidatableResponse createOrderWithoutAuth(Order order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ORDER_PATH)
                .then();
    }
}
