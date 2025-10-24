package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("novaNarudzba")
@SessionScoped
public class NovaNarudzba implements Serializable {

    @Inject PrijavaKorisnika prijava;
    @Inject @RestClient ServisPartnerKlijent klijent;
    @Inject ZapisiFacade zapisiFacade;

    private List<Jelovnik> jelovnik;
    private List<KartaPica> karta;

    private String jeloId;
    private int    kolicinaJ = 1;
    private String piceId;
    private int    kolicinaP = 1;

    private final List<Jelovnik>  narucenaJela  = new ArrayList<>();
    private final List<KartaPica> narucenaPica  = new ArrayList<>();

    private boolean narudzbaAktivna;

    @PostConstruct
    private void init() {
        if (!prijava.getPartnerOdabran()) {
            jelovnik = List.of();               
            karta    = List.of();
            return;
        }
        try {
            jelovnik = klijent.getJelovnik();
        } catch (Exception e) {
            jelovnik = List.of();            
        }
        try {
            karta = klijent.getKartaPica();
        } catch (Exception e) {
            karta = List.of();
        }
    }

    public void novaNarudzba() {
        if (narudzbaAktivna) return;
        if (klijent.postNarudzba().getStatus()==201) {
            narudzbaAktivna = true;
            zapisi("NARUDZBA");
        }
    }

    public void dodajJelo() {
        if (!narudzbaAktivna) return;
        if (klijent.postJelo(jeloId, kolicinaJ).getStatus()==201) {
            Jelovnik j = jelovnik.stream().filter(x->x.id().equals(jeloId)).findFirst().orElse(null);
            if (j!=null) narucenaJela.add(new Jelovnik(j.id(), j.naziv(), j.cijena()*kolicinaJ));
        }
    }

    public void dodajPice() {
        if (!narudzbaAktivna) return;
        if (klijent.postPice(piceId, kolicinaP).getStatus()==201) {
            KartaPica p = karta.stream().filter(x->x.id().equals(piceId)).findFirst().orElse(null);
            if (p!=null) narucenaPica.add(new KartaPica(p.id(), p.naziv(),
                                                       p.kolicina(), p.cijena()*kolicinaP));
        }
    }

    public void plati() {
        if (!narudzbaAktivna) return;
        if (klijent.postRacun().getStatus()==201) {
            zapisi("RACUN");              
            narudzbaAktivna = false;
            narucenaJela.clear();
            narucenaPica.clear();
        }
    }

    private void zapisi(String opis){
        Zapisi z = new Zapisi();
        z.setKorisnickoime(prijava.getKorisnickoIme());
        z.setOpisrada(opis);
        z.setVrijeme(new Timestamp(System.currentTimeMillis()));
        zapisiFacade.create(z);
    }

    public List<Jelovnik>  getJelovnik() { return jelovnik; }
    
    public List<KartaPica> getKarta() { return karta; }
    
    public String getJeloId() { return jeloId; }
    
    public void setJeloId(String id) { jeloId=id; }
    
    public int getKolicinaJ() { return kolicinaJ; }
    
    public void setKolicinaJ(int k) { kolicinaJ=k; }
    
    public String getPiceId() { return piceId; }
    
    public void setPiceId(String id) { piceId=id; }
    
    public int getKolicinaP() { return kolicinaP; }
    
    public void setKolicinaP(int k)      { kolicinaP=k; }
    
    public List<Jelovnik>  getNarucenaJela() { return narucenaJela; }
    
    public List<KartaPica> getNarucenaPica() { return narucenaPica; }
    
    public boolean isNarudzbaAktivna()       { return narudzbaAktivna; }
}
