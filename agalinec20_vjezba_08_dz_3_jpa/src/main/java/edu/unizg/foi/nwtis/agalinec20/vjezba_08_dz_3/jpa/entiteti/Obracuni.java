package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the OBRACUNI database table.
 * 
 */
@Entity
@NamedQuery(name="Obracuni.findAll", query="SELECT o FROM Obracuni o")
public class Obracuni implements Serializable {
	private static final long serialVersionUID = 1L;

	 @Id
	  @SequenceGenerator(name = "OBRACUNI_RB_GENERATOR", sequenceName = "OBRACUNI_ID", initialValue = 1,
	      allocationSize = 1)
	  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OBRACUNI_RB_GENERATOR")
	  private int rb;

	  private double cijena;

	  @Transient
	  private double gpsduzina;

	  @Transient
	  private double gpssirina;

	  private String id;

	  private boolean jelo;

	  private double kolicina;

	  @Transient
	  private String sigurnosnikod;

	  private Timestamp vrijeme;

	  @ManyToOne
	  @JoinColumn(name = "PARTNER")
	  private Partneri partneri;

	  public Obracuni() {}

	  public int getRb() {
	    return this.rb;
	  }

	  public void setRb(int rb) {
	    this.rb = rb;
	  }

	  public double getCijena() {
	    return this.cijena;
	  }

	  public void setCijena(double cijena) {
	    this.cijena = cijena;
	  }

	  public double getGpsduzina() {
	    return this.gpsduzina;
	  }

	  public void setGpsduzina(double gpsduzina) {
	    this.gpsduzina = gpsduzina;
	  }

	  public double getGpssirina() {
	    return this.gpssirina;
	  }

	  public void setGpssirina(double gpssirina) {
	    this.gpssirina = gpssirina;
	  }

	  public String getId() {
	    return this.id;
	  }

	  public void setId(String id) {
	    this.id = id;
	  }

	  public boolean getJelo() {
	    return this.jelo;
	  }

	  public void setJelo(boolean jelo) {
	    this.jelo = jelo;
	  }

	  public double getKolicina() {
	    return this.kolicina;
	  }

	  public void setKolicina(double kolicina) {
	    this.kolicina = kolicina;
	  }

	  public String getSigurnosnikod() {
	    return this.sigurnosnikod;
	  }

	  public void setSigurnosnikod(String sigurnosnikod) {
	    this.sigurnosnikod = sigurnosnikod;
	  }

	  public Timestamp getVrijeme() {
	    return this.vrijeme;
	  }

	  public void setVrijeme(Timestamp vrijeme) {
	    this.vrijeme = vrijeme;
	  }

	  public Partneri getPartneri() {
	    return this.partneri;
	  }

	  public void setPartneri(Partneri partneri) {
	    this.partneri = partneri;
	  }

}