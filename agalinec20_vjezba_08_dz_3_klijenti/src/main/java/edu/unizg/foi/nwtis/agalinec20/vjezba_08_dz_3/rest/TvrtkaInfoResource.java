package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.rest;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.ws.WebSocketTvrtka;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

@Path("nwtis/v1/api/tvrtka")
public class TvrtkaInfoResource {
	
   @Inject
   private GlobalniPodaci gp;
	
  @HEAD
  @Path("kraj/info")
  @Operation(summary = "Infromacija o zaustavljanju posluzitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_krajInfo", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response krajInfo() {
      gp.setPoruka("");                          
      String payload = "NE RADI;" + gp.getBrojObracuna() + ";";
      WebSocketTvrtka.send(payload);              
      return Response.ok().build();                
  }
  
  @Path("obracun/ws")
  @GET
  @Operation(summary = "Infromacija obračunu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_getObracunWs",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracunWs", description = "Vrijeme trajanja metode")
  public Response obracunWs() {
      gp.povecajBrojObracuna();                    
      String payload = "RADI;" + gp.getBrojObracuna() + ";" + gp.getPoruka();              
      WebSocketTvrtka.send(payload);
      return Response.ok().build();                
  }
}

