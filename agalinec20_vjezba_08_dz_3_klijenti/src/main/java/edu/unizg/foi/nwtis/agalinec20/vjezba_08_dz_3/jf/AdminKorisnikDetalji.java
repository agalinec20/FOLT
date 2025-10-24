package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("adminKorisnikDetalji")
@RequestScoped
public class AdminKorisnikDetalji implements Serializable {

    @Inject @RestClient ServisPartnerKlijent klijent;

    private Korisnik korisnik;

    @PostConstruct
    private void init() {
        String id = FacesContext.getCurrentInstance()
                                 .getExternalContext()
                                 .getRequestParameterMap()
                                 .get("id");
        if (id != null) {
            korisnik = klijent.getKorisnik(id);      
        }
    }

    public Korisnik getKorisnik() { return korisnik; }
}
