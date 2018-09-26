package org.thingsboard.datatransfer.exporting.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.client.tools.RestClient;
import org.thingsboard.datatransfer.exporting.SaveContext;
import org.thingsboard.server.common.data.id.DashboardId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by mshvayka on 13.09.18.
 */
@Slf4j
public class ExportDashboards extends ExportEntity {

    private final ArrayNode dashboardNode;

    public ExportDashboards(RestClient tbRestClient, ObjectMapper mapper, String basePath) {
        super(tbRestClient, mapper, basePath, false);
        dashboardNode = mapper.createArrayNode();
    }

    public void getTenantDashboards(SaveContext saveContext, int limit) {
        Optional<JsonNode> dashboardsOptional = tbRestClient.findTenantDashboards(limit);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(basePath + "Dashboards.json")))) {
            if (dashboardsOptional.isPresent()) {
                ArrayNode dashboardArray = (ArrayNode) dashboardsOptional.get().get("data");
                String strFromType = "DASHBOARD";
                for (JsonNode node : dashboardArray) {
                    String strDashboardId = node.get("id").get("id").asText();
                    addRelationToNode(saveContext.getRelationsArray(), strDashboardId, strFromType);

                    Optional<JsonNode> dashboardOptional = tbRestClient.getDashboardById(new DashboardId(UUID.fromString(strDashboardId)));
                    dashboardOptional.ifPresent(dashboardNode::add);
                }
                writer.write(mapper.writeValueAsString(dashboardNode));
            }
        } catch (IOException e) {
            log.warn("Could not export dashboards to file.");
        }
    }
}
