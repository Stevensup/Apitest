package com.example.tests;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import io.restassured.specification.RequestSpecification;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class ApiTest {

    private static final String BASE_URI = "https://dummy.restapiexample.com/api/v1";
    private static final int MIN_DELAY = 1000;
    private static final int MAX_DELAY = 5000;

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = BASE_URI;
    }

    private int getRandomDelay() {
        return new Random().nextInt(MAX_DELAY - MIN_DELAY) + MIN_DELAY;
    }

    @Test
    public void testE2EFlow() throws InterruptedException {
        // Realizar una llamada para obtener la lista de empleados
        when()
            .get("/employees")
        .then()
            .statusCode(200)
            .body("status", equalTo("success"))
            .body("data", hasSize(greaterThan(0)));
        
        // Realizar una llamada para obtener detalles de un empleado específico
        String employeeId = "1";
        given()
            .pathParam("id", employeeId)
        .when()
            .get("/employee/{id}")
        .then()
            .statusCode(200)
            .body("status", equalTo("success"))
            .body("data.id", equalTo(employeeId))
            .body("data.employee_name", notNullValue())
            .body("data.employee_salary", notNullValue())
            .body("data.employee_age", notNullValue())
            .body("data.profile_image", notNullValue());

        // Crear un nuevo empleado
        String requestBody = "{\"name\":\"test\",\"salary\":\"123\",\"age\":\"23\"}";
        given()
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("/create")
        .then()
            .statusCode(200)
            .body("status", equalTo("success"))
            .body("data.name", equalTo("test"))
            .body("data.salary", equalTo("123"))
            .body("data.age", equalTo("23"))
            .body("data.id", notNullValue());

        // Actualizar el nombre de un empleado existente
        String updatedNameRequestBody = "{\"name\":\"Updated Name\"}";
        String updatedEmployeeId = "21";
        given()
            .contentType("application/json")
            .body(updatedNameRequestBody)
            .pathParam("id", updatedEmployeeId)
        .when()
            .put("/update/{id}")
        .then()
            .statusCode(200)
            .body("status", equalTo("success"))
            .body("data.name", equalTo("Updated Name"));

        // Eliminar un empleado
        String employeeToDeleteId = "2";
        given()
            .pathParam("id", employeeToDeleteId)
        .when()
            .delete("/delete/{id}")
        .then()
            .statusCode(200)
            .body("status", equalTo("success"))
            .body("message", equalTo("successfully! deleted Records"));

        // Realizar múltiples solicitudes limitadas por tasa
        int numRequests = 10;
        int baseDelay = 1000; // Tiempo base de espera (en milisegundos)

        for (int i = 0; i < numRequests; i++) {
            when()
                .get("/employees")
            .then()
                .statusCode(200);

            // Calcular el tiempo de espera exponencial
            int exponentialDelay = (int) (baseDelay * Math.pow(2, i));

            // Limitar el tiempo de espera máximo a MAX_DELAY
            int delay = Math.min(exponentialDelay, MAX_DELAY);

            Thread.sleep(delay);
        }

    }
}
