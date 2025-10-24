package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "klijentTvrtka")
@Path("api/tvrtka")
public interface ServisTvrtkaKlijent {
	
  @HEAD
  public Response headPosluzitelj();

  @Path("status/{id}")
  @HEAD
  public Response headPosluziteljStatus(@PathParam("id") int id);

  @Path("pauza/{id}")
  @HEAD
  public Response headPosluziteljPauza(@PathParam("id") int id);

  @Path("start/{id}")
  @HEAD
  public Response headPosluziteljStart(@PathParam("id") int id);

  @Path("kraj")
  @HEAD
  public Response headPosluziteljKraj();

  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getPartneri();

  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getPartner(@PathParam("id") int id);

  @Path("obracun")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response getObracuni(@QueryParam("od") Long od, @QueryParam("do") Long kraj);

  @Path("obracun/jelo")
  @GET
  Response getObracuniJelo(@QueryParam("od") Long od,
                           @QueryParam("do") Long kraj);

  @Path("obracun/pice")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response getObracuniPice(@QueryParam("od") Long od, @QueryParam("do") Long kraj);

  @Path("obracun/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response getObracuniPartner(@PathParam("id") int id, @QueryParam("od") Long od, @QueryParam("do") Long kraj);

  @Path("partner")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  Response postPartner(Partner partner);
  
  @Path("spava")
  @GET
  Response headSpava(@QueryParam("vrijeme") int vrijeme);


}
