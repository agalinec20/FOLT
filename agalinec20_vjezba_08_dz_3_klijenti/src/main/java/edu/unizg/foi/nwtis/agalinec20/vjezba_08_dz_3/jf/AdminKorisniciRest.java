package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("adminKorisniciRest")
@RequestScoped
public class AdminKorisniciRest implements Serializable {

    @Inject @RestClient ServisPartnerKlijent klijent;

    private List<Korisnik> korisnici;

    @PostConstruct
    private void init() {
        korisnici = klijent.getKorisnici();       
    }

    public List<Korisnik> getKorisnici() { return korisnici; }
}
