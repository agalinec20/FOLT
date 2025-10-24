package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalniPodaci {

    private int brojObracuna = 0;
    private String poruka = "";
    private Map<Integer, Integer> brojOtvorenihNarudzbi = new ConcurrentHashMap<>();
    private Map<Integer, Integer> brojRacuna = new ConcurrentHashMap<>();

    public int getBrojObracuna() {
        return brojObracuna;
    }

    public void setBrojObracuna(int brojObracuna) {
        this.brojObracuna = brojObracuna;
    }

    public Map<Integer, Integer> getBrojOtvorenihNarudzbi() {
        return brojOtvorenihNarudzbi;
    }

    public void setBrojOtvorenihNarudzbi(Map<Integer, Integer> brojOtvorenihNarudzbi) {
        this.brojOtvorenihNarudzbi = brojOtvorenihNarudzbi;
    }

    public Map<Integer, Integer> getBrojRacuna() {
        return brojRacuna;
    }

    public void setBrojRacuna(Map<Integer, Integer> brojRacuna) {
        this.brojRacuna = brojRacuna;
    }

    public void povecajBrojObracuna() {
        this.brojObracuna++;
    }

    public void povecajOtvoreneNarudzbe(int idPartnera) {
        brojOtvorenihNarudzbi.merge(idPartnera, 1, Integer::sum);
    }

    public void smanjiOtvoreneNarudzbe(int idPartnera) {
        brojOtvorenihNarudzbi.merge(idPartnera, -1, Integer::sum);
        if (brojOtvorenihNarudzbi.get(idPartnera) <= 0) {
            brojOtvorenihNarudzbi.remove(idPartnera);
        }
    }

    public void povecajBrojPlaceniRacuni(int idPartnera) {
        brojRacuna.merge(idPartnera, 1, Integer::sum);
    }
    
    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }
}