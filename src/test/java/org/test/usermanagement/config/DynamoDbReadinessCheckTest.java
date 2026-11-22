package org.test.usermanagement.config;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamoDbReadinessCheckTest {

    private final DynamoDbClient dynamoDb = mock(DynamoDbClient.class);
    private final DynamoDbReadinessCheck check = new DynamoDbReadinessCheck(dynamoDb, "users");

    @Test
    @SuppressWarnings("unchecked")
    void readiness_isUp_whenTableExists() {
        when(dynamoDb.describeTable(any(Consumer.class))).thenReturn(DescribeTableResponse.builder().build());

        HealthCheckResponse response = check.call();

        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("users", response.getData().orElseThrow().get("table"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void readiness_isUpWithTableMissing_whenResourceNotFound() {
        when(dynamoDb.describeTable(any(Consumer.class)))
                .thenThrow(ResourceNotFoundException.builder().message("Table not found").build());

        HealthCheckResponse response = check.call();

        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("missing", response.getData().orElseThrow().get("table"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void readiness_isDown_whenSdkClientException() {
        when(dynamoDb.describeTable(any(Consumer.class)))
                .thenThrow(SdkClientException.create("Connection refused"));

        HealthCheckResponse response = check.call();

        assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertEquals("SdkClientException", response.getData().orElseThrow().get("error"));
    }
}
