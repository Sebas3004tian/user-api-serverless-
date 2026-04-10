package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;

public class CreateUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

        private final DynamoDbClient dynamoDb = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .build();

        private final String TABLE_NAME = System.getenv("TABLE_NAME");

        private final SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .build();

        private final String QUEUE_URL = System.getenv("QUEUE_URL");

        private Map<String, AttributeValue> buildUserItem(String id, String nombre, String email) {
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("id", AttributeValue.builder().s(id).build());
                item.put("nombre", AttributeValue.builder().s(nombre).build());
                item.put("email", AttributeValue.builder().s(email).build());
                return item;
        }

        private String buildUserMessage(String id, String nombre, String email) throws Exception {
                Map<String, String> messageBody = new HashMap<>();
                messageBody.put("id", id);
                messageBody.put("nombre", nombre);
                messageBody.put("email", email);

                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(messageBody);
        }

        private void sendMessageToSqs(String messageJson) {
                SendMessageRequest request = SendMessageRequest.builder()
                        .queueUrl(QUEUE_URL)
                        .messageBody(messageJson)
                        .build();

                sqsClient.sendMessage(request);
        }

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

                Map<String, AttributeValue> item = buildUserItem(id, nombre, email);

                PutItemRequest request = PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build();

                dynamoDb.putItem(request);

                String messageJson = buildUserMessage(id, nombre, email);
                sendMessageToSqs(messageJson);

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