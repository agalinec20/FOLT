package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class PartneriFacade {

	@PersistenceContext(unitName = "vjezba_08_dz_3_jpa")
    private EntityManager em;

    public List<Partneri> dohvatiSve() {
        return em.createNamedQuery("Partneri.findAll", Partneri.class)
                 .getResultList();
    }
    
    public edu.unizg.foi.nwtis.podaci.Partner pretvori(Partneri p) {
        return new edu.unizg.foi.nwtis.podaci.Partner(
            p.getId(),
            p.getNaziv(),
            p.getVrstakuhinje(),
            p.getAdresa(),
            p.getMreznavrata(),
            p.getMreznavratakraj(),
            (float) p.getGpssirina(),
            (float) p.getGpsduzina(),
            p.getSigurnosnikod(),
            p.getAdminkod()
        );
    }
}
