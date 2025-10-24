package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("odabirParnera")
public class OdabirPartnera implements Serializable {

    @Inject 
    PartneriFacade partneriFacade;
    
    @Inject 
    PrijavaKorisnika prijava;

    private List<Partneri> partneri;
    
    private int partner;                  

    public int  getPartner() { return partner; }
    
    public void setPartner(int p) { partner = p; }

    public List<Partneri> getPartneri() { return partneri; }

    @PostConstruct
    private void ucitajPartnere() {
        partneri = partneriFacade.dohvatiSve();
    }

    public String odaberiPartnera() {
        partneri.stream()
                .filter(p -> p.getId() == partner)
                .findFirst()
                .ifPresentOrElse(p -> {
                        prijava.setOdabraniPartner(partneriFacade.pretvori(p));
                        prijava.setPartnerOdabran(true);
                    },
                    () -> prijava.setPartnerOdabran(false));
        return "index.xhtml?faces-redirect=true";
    }
}
