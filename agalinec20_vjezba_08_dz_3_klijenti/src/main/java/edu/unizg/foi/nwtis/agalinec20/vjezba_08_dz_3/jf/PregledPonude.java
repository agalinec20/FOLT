package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("pregledPonude")
@RequestScoped
public class PregledPonude implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject 
    PrijavaKorisnika prijava;
    
    @Inject 
    @RestClient 
    ServisPartnerKlijent partnerKlijent;

    private List<Jelovnik>  jelovnik;
    private List<KartaPica> karta;

    public List<Jelovnik>  getJelovnik() { return jelovnik; }
    
    public List<KartaPica> getKarta() { return karta; }

    public boolean isPartnerOdabran() { return prijava.getPartnerOdabran(); }

    @PostConstruct
    private void ucitaj() {
        if (!isPartnerOdabran()) return;          
        try { jelovnik = partnerKlijent.getJelovnik(); } catch(Exception e) { }
        try { karta    = partnerKlijent.getKartaPica(); } catch(Exception e) { }
    }
}
