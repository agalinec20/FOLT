package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.PathParam;

/**
 * REST klijent za komunikaciju s partner servisom.
 */
@Path("api/partner")
@RegisterRestClient(configKey = "klijentPartner")
public interface ServisPartnerKlijent {

    @HEAD
    Response headPosluzitelj();
    
    @HEAD @Path("status/{id}") 
    Response status(@PathParam("id") String id);
    
    @HEAD @Path("pauza/{id}")  
    Response pauza (@PathParam("id") String id);
    
    @HEAD @Path("start/{id}")  
    Response start (@PathParam("id") String id);
    
    @HEAD @Path("kraj")        
    Response kraj ();
    
    @POST
    @Path("korisnik")
    @Consumes(MediaType.APPLICATION_JSON)
    Response postKorisnik(Korisnik korisnik);
    
    @GET @Path("korisnik")
    @Produces(MediaType.APPLICATION_JSON)
    List<Korisnik> getKorisnici();

    @GET @Path("korisnik/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Korisnik getKorisnik(@PathParam("id") String id);

    @GET
    @Path("jelovnik")
    @Produces(MediaType.APPLICATION_JSON)
    List<Jelovnik> getJelovnik();

    @GET
    @Path("kartapica")
    @Produces(MediaType.APPLICATION_JSON)
    List<KartaPica> getKartaPica();
    
    @POST @Path("narudzba") Response postNarudzba();

    @POST @Path("jelo") Response postJelo(@QueryParam("id") String id,
                                          @QueryParam("kolicina") int kol);

    @POST @Path("pice") Response postPice(@QueryParam("id") String id,
                                          @QueryParam("kolicina") int kol);

    @POST @Path("racun") Response postRacun();
    
    @GET @Path("spava")
    Response spava(@QueryParam("vrijeme") long trajanjeMs);

}
