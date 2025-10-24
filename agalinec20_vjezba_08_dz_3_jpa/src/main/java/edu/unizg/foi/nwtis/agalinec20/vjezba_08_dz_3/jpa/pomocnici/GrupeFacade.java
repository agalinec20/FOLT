package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Grupe;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GrupeFacade {

    @PersistenceContext(unitName = "vjezba_08_dz_3_jpa")
    private EntityManager em;

    @Transactional
    public void dodajKorisnikaUGrupu(String korisnikId) {
        Korisnici k = em.find(Korisnici.class, korisnikId);
        if (k == null) return;                         
        Grupe g = em.find(Grupe.class, "nwtis");       
        if (g == null) {                              
            g = new Grupe(); g.setGrupa("nwtis"); g.setNaziv("NWTiS korisnik");
            em.persist(g);
        }
        if (!k.getGrupes().contains(g)) {
            k.getGrupes().add(g);                      
        }
    }
}
