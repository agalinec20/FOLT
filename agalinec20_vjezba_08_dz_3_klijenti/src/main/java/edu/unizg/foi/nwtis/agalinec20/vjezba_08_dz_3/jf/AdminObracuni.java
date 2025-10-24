package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Obracuni;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.ObracuniFacade;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("adminObracuni")
@RequestScoped
public class AdminObracuni implements Serializable {

    private int partnerId;

    private java.util.Date od  = new java.util.Date();
    private java.util.Date _do = new java.util.Date();

    private List<Obracuni> lista;

    @Inject PartneriFacade  partnerFacade;
    @Inject ObracuniFacade  obracuniFacade;

    public List<Partneri> getPartneri() { return partnerFacade.dohvatiSve(); }

    public int getPartnerId()                 { return partnerId; }
    public void setPartnerId(int id)          { partnerId = id; }

    public java.util.Date getOd()             { return od;  }
    public void setOd(java.util.Date d)       { od  = d; }

    public java.util.Date getDo()             { return _do; }
    public void setDo(java.util.Date d)       { _do = d; }

    public List<Obracuni> getLista()          { return lista; }

    public void pretrazi() {
        Instant odI  = od.toInstant();                
        Instant doI  = _do.toInstant()
                          .plusSeconds(23 * 3600 + 59 * 60 + 59); 
        lista = obracuniFacade.find(partnerId, odI, doI);
    }
}
