package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.GrupeFacade;
import edu.unizg.foi.nwtis.podaci.Korisnik;

@Named("noviKorisnik")
@RequestScoped
public class NoviKorisnik implements Serializable {

    @Inject @RestClient
    ServisPartnerKlijent klijent;
    
    @Inject GrupeFacade grupeFacade;

    private String ime, prezime, korisnickoIme, email, lozinka;

    public String getIme() { return ime; }               
    
    public void setIme(String i){ ime=i; }
    
    public String getPrezime(){ return prezime; }       
    
    public void setPrezime(String p){ prezime=p; }
    
    public String getKorisnickoIme(){ return korisnickoIme; } 
    
    public void setKorisnickoIme(String k){ korisnickoIme=k; }
    
    public String getEmail(){ return email; }            
    
    public void setEmail(String e){ email=e; }
    
    public String getLozinka(){ return lozinka; }        
    
    public void setLozinka(String l){ lozinka=l; }

    public String spremi() {
        Korisnik k = new Korisnik(korisnickoIme, lozinka, ime, prezime, email);
        try {
            Response r = klijent.postKorisnik(k);
            if (r.getStatus() == 201) {
                grupeFacade.dodajKorisnikaUGrupu(korisnickoIme);     
                FacesContext.getCurrentInstance().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "", null));
                return "prijavaKorisnika?faces-redirect=true";
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "", null));
        }
        return null;
    }
}
