package org.unizg.foi.nwtis.agalinec20.rest;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "klijentTvrtkaInfo")
@Path("api/tvrtka")
public interface TvrtkaInfoKlijent {

    @GET
    @Path("kraj/info")
    Response getKrajInfo();

    @GET
    @Path("obracun/ws")
    Response getObracunWS();
}