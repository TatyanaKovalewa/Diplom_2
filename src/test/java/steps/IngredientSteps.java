package steps;

import constants.TestConstants;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class IngredientSteps {

    private static final String INGREDIENTS_PATH = "/api/ingredients";

    @Step("Получение списка всех ингредиентов")
    public ValidatableResponse getIngredients() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get(INGREDIENTS_PATH)
                .then();
    }

    @Step("Получение списка валидных ID ингредиентов")
    public List<String> getValidIngredientIds(int count) {
        ValidatableResponse response = getIngredients();
        // Получаем список всех ингредиентов как List<Map>
        List<Map<String, Object>> ingredients = response.extract().path("data");
        List<String> ids = new ArrayList<>();

        // Проходим по ингредиентам и собираем ID
        for (int i = 0; i < Math.min(count, ingredients.size()); i++) {
            String id = (String) ingredients.get(i).get("_id");
            ids.add(id);
        }
        return ids;
    }

    @Step("Получение двух валидных ID ингредиентов")
    public List<String> getTwoIngredientIds() {
        return getValidIngredientIds(TestConstants.DEFAULT_INGREDIENTS_COUNT);
    }

}
