package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("konzolaPartner")
@RequestScoped
public class KonzolaPartner implements Serializable {

    private static final List<String> DIJELOVI =
        List.of("kontrola", "kupci");    

    @Inject @RestClient ServisPartnerKlijent klijent;

    private Map<String,String> status = new HashMap<>();

    @PostConstruct
    private void init() {
        for (String d : DIJELOVI) {
            try {                                  
                var r = klijent.status(d);
                status.put(d, r.getStatus()==200 ? "RADI" : "PAUZA");
            } catch (Exception e) {                 
                status.put(d, "NEPOZNATO");
            }
        }
    }

    public Map<String,String> getStatus() { return status; }
    public List<String> getDijelovi()     { return DIJELOVI; }

    public void pauziraj(String dio){
        if (klijent.pauza(dio).getStatus()==200) status.put(dio,"PAUZA");
    }
    public void pokreni(String dio){
        if (klijent.start(dio).getStatus()==200) status.put(dio,"RADI");
    }
    public void kraj(){ klijent.kraj(); }
}
