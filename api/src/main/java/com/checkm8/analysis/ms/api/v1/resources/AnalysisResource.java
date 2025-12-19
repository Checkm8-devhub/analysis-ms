package com.checkm8.analysis.ms.api.v1.resources;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.checkm8.analysis.ms.api.v1.dtos.AnalysisRequest;
import com.checkm8.analysis.ms.api.v1.dtos.GameplayGamesMsResponse;
import com.checkm8.analysis.ms.beans.AnalysisBean;
import com.checkm8.analysis.ms.dtos.AnalysisResponsePv;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@ApplicationScoped
@Path("analyses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnalysisResource {

    @Context
    private UriInfo uriInfo;

    private Client httpClient;
    @ConfigProperty(name = "gameplay.games.ms.base-url", defaultValue = "http://localhost:8080")
    private String baseUrl;
    @ConfigProperty(name = "engine.stockfish.moveTime", defaultValue = "150")
    private Integer moveTime;

    @Inject
    private AnalysisBean analysisBean;

    @PostConstruct
    private void init() {
        this.httpClient = ClientBuilder.newClient();
    }
    
    // ****************************************
    //  POST
    // ****************************************

    // Expects game_token and actions in body
    @POST
    public Response handleAction(AnalysisRequest req) {

        if (req == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body required").build();
        if (req.gameId == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing game id").build();

        try {

            GameplayGamesMsResponse gameplayGamesMsResponse = this.httpClient
                .target(this.baseUrl + "/games/" + req.gameId)
                .request().get(GameplayGamesMsResponse.class);

            List<List<AnalysisResponsePv>> analysis = analysisBean.analyzeUcis(gameplayGamesMsResponse.uciAsList, this.moveTime);
            return Response.ok(analysis).build();

        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
