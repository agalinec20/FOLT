package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.sql.Timestamp;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("adminSpavanje")
@RequestScoped
public class AdminSpavanje implements Serializable {

    private static final long serialVersionUID = 1L;

	private long trajanje;                        

    @Inject @RestClient ServisPartnerKlijent klijent;
    @Inject ZapisiFacade zapisiFacade;
    @Inject PrijavaKorisnika prijava;

    public long getTrajanje()          { return trajanje; }
    public void setTrajanje(long t)    { trajanje = t; }

    public void pokreni() {
        var r = klijent.spava(trajanje);

        if (r.getStatus() == 200) {
            Zapisi z = new Zapisi();
            z.setKorisnickoime(prijava.getKorisnickoIme());
            z.setOpisrada("SPAVANJE " + trajanje + " ms");
            z.setVrijeme(new Timestamp(System.currentTimeMillis()));
            zapisiFacade.create(z);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                 "Spavanje prihvaćeno.", null));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Status: " + r.getStatus(), null));
        }
    }
}
