package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("adminZapisi")
@RequestScoped
public class AdminZapisi implements Serializable {

    private String korisnik;          
    private Date   od  = new Date();  
    private Date   _do = new Date();  

    private List<Zapisi> lista;      

    @Inject ZapisiFacade      zapisiFacade;
    @Inject KorisniciFacade   korisniciFacade;

    public List<Korisnici> getKorisnici() { return korisniciFacade.findAll(); }

    public String getKorisnik()             { return korisnik; }
    public void   setKorisnik(String k)     { korisnik = k; }

    public Date getOd()                     { return od;  }
    public void setOd(Date d)               { od = d; }

    public Date getDo()                     { return _do; }
    public void setDo(Date d)               { _do = d; }

    public List<Zapisi> getLista()          { return lista; }

    public void pretrazi() {
        Instant odI = od.toInstant();                     
        Instant doI = _do.toInstant()
                         .plusSeconds(23 * 3600 + 59 * 60 + 59); 
        lista = zapisiFacade.find(korisnik, odI, doI);
    }
}
