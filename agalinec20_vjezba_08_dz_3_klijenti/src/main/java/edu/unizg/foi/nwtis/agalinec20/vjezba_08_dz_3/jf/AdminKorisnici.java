package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("adminKorisnici")
@RequestScoped
public class AdminKorisnici implements Serializable {

    private String ime = "";
    private String prezime = "";
    private List<Korisnici> rezultat;       

    @Inject KorisniciFacade facade;

    @PostConstruct
    private void init() {
        rezultat = facade.findAll();       
    }

    public String getIme()          { return ime; }
    public void   setIme(String i)  { ime = i; }
    public String getPrezime()      { return prezime; }
    public void   setPrezime(String p){ prezime = p; }
    public List<Korisnici> getRezultat() { return rezultat; }

    public void pretrazi() {
        boolean praznoIme      = ime.trim().isEmpty();
        boolean praznoPrezime  = prezime.trim().isEmpty();
        if (praznoIme && praznoPrezime) {         
            rezultat = facade.findAll();
        } else {
            String p = praznoPrezime ? "%" : "%" + prezime + "%";
            String i = praznoIme     ? "%" : "%" + ime     + "%";
            rezultat = facade.findAll(p, i);
        }
    }
}
