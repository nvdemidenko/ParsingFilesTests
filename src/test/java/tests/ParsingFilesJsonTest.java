package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.model.Clients;

import java.io.InputStream;

public class ParsingFilesJsonTest {
    private final ClassLoader cl = ParsingFilesJsonTest.class.getClassLoader();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("Проверяем файл Clients.json")
    @Test
    void parseJsonWithJackson() throws Exception {
        try (InputStream is = cl.getResourceAsStream("Clients.json")) {
            Assertions.assertNotNull(is, "Файл Clients.json не найден в resources");

            Clients client = objectMapper.readValue(is, Clients.class);

            Assertions.assertEquals("Natalya", client.getName());
            Assertions.assertEquals("natalya@test.ru", client.getEmail());
            Assertions.assertEquals(2, client.orders.size());

            Assertions.assertEquals("water", client.orders.get(0).product);
            Assertions.assertEquals(100, client.orders.get(0).price);

            Assertions.assertEquals("apples", client.orders.get(1).product);
            Assertions.assertEquals(90, client.orders.get(1).price);
        }
    }
}
