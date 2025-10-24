package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici;

import java.util.List;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Obracuni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class ObracuniFacade {

    @PersistenceContext(unitName = "vjezba_08_dz_3_jpa")
    private EntityManager em;

    public List<Obracuni> find(int partnerId,
                               java.time.Instant odI,
                               java.time.Instant doI) {

        return em.createQuery("""
            SELECT o FROM Obracuni o
            WHERE o.partneri.id = :pid
              AND o.vrijeme BETWEEN :od AND :do
            ORDER BY o.vrijeme
        """, Obracuni.class)
        .setParameter("pid", partnerId)
        .setParameter("od" , java.sql.Timestamp.from(odI))
        .setParameter("do" , java.sql.Timestamp.from(doI))
        .getResultList();
    }
}
