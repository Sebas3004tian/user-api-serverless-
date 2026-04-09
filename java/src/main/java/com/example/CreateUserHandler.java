package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class CreateUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(Region.US_EAST_1)
            .build();

    private final String TABLE_NAME = System.getenv("TABLE_NAME");

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> body = mapper.readValue(
                    (String) input.get("body"),
                    Map.class
            );

            String id = (String) body.get("id");
            String nombre = (String) body.get("nombre");
            String email = (String) body.get("email");

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(id).build());
            item.put("nombre", AttributeValue.builder().s(nombre).build());
            item.put("email", AttributeValue.builder().s(email).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDb.putItem(request);

            return Map.of(
                    "statusCode", 200,
                    "body", "{\"message\":\"Usuario creado\"}"
            );

        } catch (Exception e) {
            return Map.of(
                    "statusCode", 500,
                    "body", "{\"error\":\"" + e.getMessage() + "\"}"
            );
        }
    }
}