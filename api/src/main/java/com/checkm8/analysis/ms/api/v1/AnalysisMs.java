package com.checkm8.analysis.ms.api.v1;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(
  info = @Info(
    title = "Analysis MS API",
    version = "v1"
  ),
  security = @SecurityRequirement(name = "bearerAuth")
)
public class AnalysisMs extends Application {}
