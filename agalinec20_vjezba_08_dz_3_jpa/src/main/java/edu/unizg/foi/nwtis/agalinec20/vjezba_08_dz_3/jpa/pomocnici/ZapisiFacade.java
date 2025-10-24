package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.pomocnici;

import java.util.List;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless                                       
public class ZapisiFacade {

    @PersistenceContext(unitName = "vjezba_08_dz_3_jpa")
    private EntityManager em;

    public void create(Zapisi z) { em.persist(z); }
    
    public List<Zapisi> find(String korisnik,
            java.time.Instant odI, java.time.Instant doI) {

		return em.createQuery("""
		SELECT z FROM Zapisi z
		WHERE z.korisnickoime = :kor
		AND z.vrijeme BETWEEN :od AND :do
		ORDER BY z.vrijeme
		""", Zapisi.class)
		.setParameter("kor", korisnik)
		.setParameter("od", java.sql.Timestamp.from(odI))
		.setParameter("do", java.sql.Timestamp.from(doI))
		.getResultList();
		}

}
