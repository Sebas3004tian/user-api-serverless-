package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

public class DeleteUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(Region.US_EAST_1)
            .build();

    private final String TABLE_NAME = System.getenv("TABLE_NAME");

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {

        try {

            Map<String, String> pathParams = (Map<String, String>) input.get("pathParameters");
            String id = pathParams != null ? pathParams.get("id") : null;

            if (id == null) {
                return Map.of(
                        "statusCode", 400,
                        "body", "{\"error\":\"El id es obligatorio\"}"
                );
            }

            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().s(id).build());

            DeleteItemRequest request = DeleteItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .build();

            dynamoDb.deleteItem(request);

            return Map.of(
                    "statusCode", 200,
                    "body", "{\"message\":\"Usuario eliminado\"}"
            );

        } catch (Exception e) {
            return Map.of(
                    "statusCode", 500,
                    "body", "{\"error\":\"" + e.getMessage() + "\"}"
            );
        }
    }
}