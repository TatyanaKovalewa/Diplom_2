package constants;

public class TestConstants {
    // Константы для пользователей
    public static final String DEFAULT_PASSWORD = "SecurePassword123!";
    public static final String DEFAULT_USER_NAME = "TestUser";
    public static final String EMAIL_DOMAIN = "@yandex.ru";
    public static final String INCORRECT_PASSWORD = "WrongPassword456!";

    // Константы для ингредиентов
    public static final int DEFAULT_INGREDIENTS_COUNT = 2;
    public static final String INVALID_INGREDIENT_HASH = "invalid_hash_123";
    public static final String NON_EXISTENT_INGREDIENT_ID = "60d3b41abdacab026a733c7";

    // Константы для сообщений об ошибках
    public static final String ERROR_USER_ALREADY_EXISTS = "User already exists";
    public static final String ERROR_REQUIRED_FIELDS = "Email, password and name are required fields";
    public static final String ERROR_INVALID_CREDENTIALS = "email or password are incorrect";
    public static final String ERROR_INGREDIENTS_REQUIRED = "Ingredient ids must be provided";
}
