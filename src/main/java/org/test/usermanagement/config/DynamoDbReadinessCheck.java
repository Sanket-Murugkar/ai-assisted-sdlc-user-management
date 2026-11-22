package org.test.usermanagement.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * Reports ready when DynamoDB answers a {@code DescribeTable} call for the users table.
 * Uses {@code DescribeTable} rather than {@code ListTables} because the production IAM policy
 * only grants table-scoped actions.
 */
@Readiness
@ApplicationScoped
public class DynamoDbReadinessCheck implements HealthCheck {

    static final String NAME = "dynamodb";

    private final DynamoDbClient dynamoDb;
    private final String tableName;

    public DynamoDbReadinessCheck(DynamoDbClient dynamoDb,
                                  @ConfigProperty(name = "user-management.dynamodb.table-name") String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    @Override
    public HealthCheckResponse call() {
        try {
            dynamoDb.describeTable(r -> r.tableName(tableName));
            return HealthCheckResponse.named(NAME).up().withData("table", tableName).build();
        } catch (ResourceNotFoundException e) {
            // DynamoDB is reachable but the table does not exist yet. MVP-04 turns this into DOWN.
            return HealthCheckResponse.named(NAME).up().withData("table", "missing").build();
        } catch (RuntimeException e) {
            return HealthCheckResponse.named(NAME).down().withData("error", e.getClass().getSimpleName()).build();
        }
    }
}
