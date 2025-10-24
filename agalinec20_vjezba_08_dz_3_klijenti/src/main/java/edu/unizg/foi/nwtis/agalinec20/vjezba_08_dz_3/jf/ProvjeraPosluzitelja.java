package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Bean koji provjerava radi li partner-servis
 * (HEAD  /nwtis/v1/api/partner).
 */
@RequestScoped
@Named("provjeraPosluzitelja")
public class ProvjeraPosluzitelja implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    @RestClient
    ServisPartnerKlijent partnerKlijent;   

    private String poruka;
    private boolean radi;

    @PostConstruct
    public void init() {
        osvjezi();
    }

    public void osvjezi() {
        try {
            Response r = partnerKlijent.headPosluzitelj();
            radi   = r.getStatus() == 200;
            poruka = radi
                   ? "Poslužitelj radi (200 OK)"
                   : "Poslužitelj NE radi (" + r.getStatus() + ")";
        } catch (Exception e) {
            radi   = false;
            poruka = "Greška pri spajanju: " + e.getMessage();
        }
    }

    public String getPoruka() { return poruka; }
    
    public String getBoja()   { return radi ? "green" : "red"; }
}
