package org.thingsboard.datatransfer.exporting.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.client.tools.RestClient;
import org.thingsboard.datatransfer.exporting.SaveContext;

import java.util.Optional;

@Slf4j
public class ExportEntity {

    final RestClient tbRestClient;
    final ObjectMapper mapper;
    final String basePath;
    private final boolean isPe;

    public ExportEntity(RestClient tbRestClient, ObjectMapper mapper, String basePath, boolean isPe) {
        this.tbRestClient = tbRestClient;
        this.mapper = mapper;
        this.basePath = basePath;
        this.isPe = isPe;
    }

    void addRelationToNode(ArrayNode relationsArray, String strEntityId, String strFromType) {
        Optional<JsonNode> relationOptional = tbRestClient.getRelationByFrom(strEntityId, strFromType);
        if (relationOptional.isPresent()) {
            JsonNode node = relationOptional.get();
            if (node.isArray() && node.size() != 0) {
                relationsArray.add(node);
            }
        }
    }

    StringBuilder getTelemetryKeys(String strFromType, String strEntityId) {
        Optional<JsonNode> telemetryKeysOptional = tbRestClient.getTelemetryKeys(strFromType, strEntityId);
        if (telemetryKeysOptional.isPresent()) {
            JsonNode telemetryKeysNode = telemetryKeysOptional.get();

            StringBuilder keys = new StringBuilder();
            int i = 1;
            for (JsonNode node : telemetryKeysNode) {
                keys.append(node.asText());
                if (telemetryKeysNode.has(i)) {
                    keys.append(",");
                }
                i++;
            }
            return keys;
        }
        return null;
    }

    ObjectNode getAttributes(String strFromType, String strEntityId) {
        Optional<JsonNode> attributesOptional = tbRestClient.getAttributes(strFromType, strEntityId);
        if (attributesOptional.isPresent()) {
            JsonNode jsonNode = attributesOptional.get();
            ObjectNode savedNode = createNode(strFromType, strEntityId, jsonNode, "attributes");
            Optional<JsonNode> attributesKeysByScopeOptional = tbRestClient.getAttributesKeysByScope(strFromType, strEntityId, "SERVER_SCOPE");
            attributesKeysByScopeOptional.ifPresent(node -> savedNode.set("attributeKeys", node));
            return savedNode;
        }
        return null;
    }


    ObjectNode createNode(String strFromType, String strEntityId, JsonNode node, String dataType) {
        ObjectNode resultNode = mapper.createObjectNode();
        resultNode.put("entityType", strFromType);
        resultNode.put("entityId", strEntityId);
        resultNode.set(dataType, node);
        return resultNode;
    }

    /*void addEntityGroups(SaveContext saveContext, String strFromType, int limit) {
        if (isPe) {
            Optional<JsonNode> assetEntityGroupsOptional = tbRestClient.getTenantEntityGroups(strFromType);
            assetEntityGroupsOptional.ifPresent(jsonNode -> {
                for (JsonNode node : jsonNode) {
                    saveContext.getEntityGroups().add(node);
                    if (!node.get("name").asText().equals("All")) {
                        Optional<JsonNode> entitiesOptional = tbRestClient.getTenantEntities(node.get("id").get("id").asText(), limit);
                        if (entitiesOptional.isPresent()) {
                            ObjectNode savedEntitiesNode = mapper.createObjectNode();
                            savedEntitiesNode.put("entityGroupId", node.get("id").get("id").asText());
                            savedEntitiesNode.setAll((ObjectNode) entitiesOptional.get());
                            saveContext.getEntitiesInGroups().add(savedEntitiesNode);
                        }
                    }
                }
            });
        }
    }*/

}
