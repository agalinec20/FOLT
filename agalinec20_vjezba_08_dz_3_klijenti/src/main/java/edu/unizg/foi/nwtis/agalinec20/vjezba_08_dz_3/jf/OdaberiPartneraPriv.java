package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Named("odabirPartneraPriv")
public class OdaberiPartneraPriv implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PartneriFacade partneriFacade;

    @Inject
    private PrijavaKorisnika prijava;

    @Inject 
    @RestClient
    private AktivnaNarudzba narudzbe;

    private List<Partneri> partneri;
    private int partner;         
    private String poruka = "";

    public int getPartner() {
        return partner;
    }

    public void setPartner(int partner) {
        this.partner = partner;
    }

    public List<Partneri> getPartneri() {
        return partneri;
    }

    public String getPoruka() {
        return poruka;
    }

    @PostConstruct
    private void ucitajPartnere() {
        partneri = partneriFacade.dohvatiSve();
    }

    public String odaberi() {
        boolean imaAktivnu = false;
        try {
            Response r = narudzbe.aktivnaNarudzba();
            imaAktivnu = (r.getStatus() == 200);
        } catch (Exception e) {}

        if (imaAktivnu) {
            poruka = "Imate aktivnu narudžbu – partner se ne može promijeniti.";
            return null;
        }

        Optional<Partneri> odabrani = partneri.stream()
                .filter(p -> p.getId() == partner)
                .findFirst();

        if (odabrani.isPresent()) {
            Partner p = partneriFacade.pretvori(odabrani.get());
            prijava.setOdabraniPartner(p);
            prijava.setPartnerOdabran(true);
        } else {
            prijava.setPartnerOdabran(false);
        }

        poruka = "";
        return "index.xhtml?faces-redirect=true";
    }
}
