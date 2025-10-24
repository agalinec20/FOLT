package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.sql.Timestamp;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;

@SessionScoped
@Named("prijavaKorisnika")
public class PrijavaKorisnika implements Serializable {

    private static final long serialVersionUID = -1826447622277477398L;

    private String  korisnickoIme;
    private String  lozinka;
    private Korisnik korisnik;
    private boolean prijavljen;
    private String  poruka = "";

    private boolean zapisano = false;      

    private boolean partnerOdabran;
    private Partner odabraniPartner;

    @Inject 
    private KorisniciFacade korisniciFacade;
    
    @Inject 
    private ZapisiFacade zapisiFacade;
    
    @Inject 
    private SecurityContext securityContext;

    public String getKorisnickoIme() {return korisnickoIme; }
    
    public void setKorisnickoIme(String k) { korisnickoIme = k; }
    
    public String getLozinka() {return lozinka; }
    
    public void setLozinka(String l) { lozinka = l; }
    
    public String getIme() {return korisnik!=null ? korisnik.ime(): ""; }
    
    public String getPrezime() {return korisnik!=null ? korisnik.prezime(): ""; }
    
    public String getEmail() {return korisnik!=null ? korisnik.email(): ""; }
    
    public String getPoruka() {return poruka; }
    
    public boolean getPartnerOdabran() { return partnerOdabran; }
    
    public void   setPartnerOdabran(boolean b) { partnerOdabran = b; }
    
    public Partner getOdabraniPartner() { return odabraniPartner; }
    
    public void   setOdabraniPartner(Partner p) { odabraniPartner = p; }

    public boolean isPrijavljen() {
        if (!prijavljen) provjeriPrijavuKorisnika();
        return prijavljen;
    }

    @PostConstruct
    private void provjeriPrijavuKorisnika() {
        if (securityContext.getCallerPrincipal() == null) return;
        String korIme = securityContext.getCallerPrincipal().getName();
        korisnik = korisniciFacade.pretvori(korisniciFacade.find(korIme));
        if (korisnik != null) {
            prijavljen    = true;
            korisnickoIme = korIme;
            lozinka       = korisnik.lozinka();
            if (!zapisano) zapisi("PRIJAVA");
        }
    }

    public String prijavaKorisnika() {
        if (korisnickoIme != null && korisnickoIme.length() > 3
                && lozinka != null && lozinka.length() > 5) {
            korisnik = korisniciFacade
                          .pretvori(korisniciFacade.find(korisnickoIme, lozinka));
            if (korisnik != null) {
                prijavljen = true;
                poruka     = "";
                if (!zapisano) zapisi("PRIJAVA");
                return "index.xhtml";
            }
        }
        prijavljen = false;
        poruka     = "Neuspješna prijava korisnika.";
        return "prijavaKorisnika.xhtml";
    }

    public String odjavaKorisnika() {

        if (!prijavljen) return "";
        zapisi("ODJAVA");
        zapisano  = false;             
        prijavljen = false;

        FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .invalidateSession();
        return "/index.xhtml?faces-redirect=true";
    }

    private void zapisi(String opis) {
        HttpServletRequest req = (HttpServletRequest)
                FacesContext.getCurrentInstance()
                            .getExternalContext()
                            .getRequest();

        Zapisi z = new Zapisi();
        z.setKorisnickoime(korisnickoIme);
        z.setOpisrada(opis);
        z.setVrijeme(new Timestamp(System.currentTimeMillis()));
        z.setIpadresaracunala(req.getRemoteAddr());
        z.setAdresaracunala(req.getRemoteHost() == null
                             ? req.getRemoteAddr() : req.getRemoteHost());
        zapisiFacade.create(z);       
        zapisano = true;
    }
}
