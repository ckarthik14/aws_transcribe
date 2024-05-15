package com.amazonaws.translation;

import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.core.SdkBytes;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class WebSocketStreamer {

    private final ApiGatewayManagementApiClient apiClient;
    private final List<String> connectionIds;
    private final DynamoDbClient dynamoDbClient;

    public WebSocketStreamer(String apiGatewayEndpointUrl, String tableName) {
        this.apiClient = ApiGatewayManagementApiClient.builder()
                .endpointOverride(URI.create(apiGatewayEndpointUrl))
                .build();
        this.dynamoDbClient = DynamoDbClient.create();

        // Scan DynamoDB to get all connection IDs at the time of object construction
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();
        var scanResponse = dynamoDbClient.scan(scanRequest);
        this.connectionIds = scanResponse.items().stream()
                .map(item -> item.get("connectionId").s())
                .collect(Collectors.toList());
    }

    public void streamAudioToConnections(SynthesizeSpeechResult speechResult) {
        for (String connectionId : connectionIds) {
            System.out.println("Posting to connection: " + connectionId);
            try (InputStream audioStream = speechResult.getAudioStream()) {
                byte[] buffer = new byte[1024];
                while (audioStream.read(buffer) != -1) {
                    SdkBytes data = SdkBytes.fromByteArray(buffer);
                    PostToConnectionRequest postRequest = PostToConnectionRequest.builder()
                            .connectionId(connectionId)
                            .data(data)
                            .build();
                    apiClient.postToConnection(postRequest);
                }
            } catch (Exception e) {
                System.err.println("Error posting to WebSocket connection " + connectionId + ": " + e.getMessage());
                // Optionally handle connection cleanup if the connection is gone
            }
        }
    }
}
