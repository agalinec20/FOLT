package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/partner")
@RegisterRestClient(configKey = "klijentPartner")
public interface AktivnaNarudzba {

    @GET @Path("narudzba")
    Response aktivnaNarudzba();
}
