package org.unizg.foi.nwtis.agalinec20.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.dao.KorisnikDAO;
import edu.unizg.foi.nwtis.podaci.Korisnik;

@Path("api/partner")
public class PartnerResource {
	
	@Inject
	@ConfigProperty(name = "adresaPartner")
	private String partnerAdresa;
	@Inject
	@ConfigProperty(name = "mreznaVrataRadPartner")
	private String mreznaVrataRadPartner; 
	@Inject
	@ConfigProperty(name = "mreznaVrataKrajPartner")
	private String mreznaVrataKrajPartner;
	@Inject
	@ConfigProperty(name = "kodZaAdminPartnera")
	private String kodZaAdminPartnera;
	@Inject
	@ConfigProperty(name = "kodZaKraj")
	private String kodZaKrajPartner;
	@Inject
	RestConfiguration restConfiguration;

	@HEAD
	@Operation(summary = "Provjera statusa poslužitelja partner")
	@APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")
	})
	@Counted(name = "brojZahtjeva_headPartner",
	         description = "Koliko puta je pozvana operacija servisa partnera")
	@Timed(name = "trajanjeMetode_headPartner",
	       description = "Vrijeme trajanja metode za provjeru rada partner servisa")
	public Response head() {
	    try (Socket s = new Socket(this.partnerAdresa,
	                               Integer.parseInt(this.mreznaVrataKrajPartner))) {
	        return Response.ok().build();                     
	    } catch (IOException ex) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); 
	    }
	}
	
    /**
     * Provjerava status određenog dijela poslužitelja (rad/kraj).
     * Vraća OK ako je status uspješno dohvaćen.
     */
	@Path("status/{id}")
	@HEAD
	@Operation(summary = "Provjera statusa dijela poslužitelja partner")
	@APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
	    @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
	@Counted(name = "brojZahtjeva_headPartnerStatus", 
		description = "Koliko puta je pozvana operacija servisa partner status")
	@Timed(name = "trajanjeMetode_headPartnerStatus",
	       description = "Vrijeme trajanja metode provjere statusa partner servisa")
	public Response status(@PathParam("id") int id) {
	    if (id != 0 && id != 1)                               
	        return Response.status(Response.Status.NO_CONTENT).build(); 
	    String odgovor = posaljiKomandu("STATUS "
	                      + this.kodZaAdminPartnera + " " + id);
	    if (odgovor != null && odgovor.startsWith("OK"))
	        return Response.ok().build();                    
	    return Response.status(Response.Status.NO_CONTENT).build();     
	}

    /**
     * Pauzira određeni dio partner poslužitelja.
     * Vraća OK ako je uspješno pauziran.
     */
	@Path("pauza/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja partner u pauzu")
	@APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
	    @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
	@Counted(name = "brojZahtjeva_headPartnerPauza",
	         description = "Koliko puta je pozvana operacija pauziranja partner servisa")
	@Timed(name = "trajanjeMetode_headPartnerPauza",
	       description = "Vrijeme trajanja metode pauze partner servisa")
	public Response pauza(@PathParam("id") int id) {
	    String odgovor = posaljiKomandu("PAUZA "
	                      + this.kodZaAdminPartnera + " " + id);
	    if (odgovor != null && odgovor.startsWith("OK"))
	        return Response.ok().build();                   
	    return Response.status(Response.Status.NO_CONTENT).build();     
	}
	
    /**
     * Pokreće određeni dio partner poslužitelja koji je bio u pauzi.
     * Vraća OK ako je uspješno pokrenut.
     */
	@Path("start/{id}")
	@HEAD
	@Operation(summary = "Pokretanje dijela poslužitelja partner iz pauze")
	@APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
	    @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
	@Counted(name = "brojZahtjeva_headPartnerStart",
	         description = "Koliko puta je pozvana operacija pokretanja partner servisa")
	@Timed(name = "trajanjeMetode_headPartnerStart",
	       description = "Vrijeme trajanja metode pokretanja partner servisa")
	public Response start(@PathParam("id") int id) {
	    String odgovor = posaljiKomandu("START "
	                      + this.kodZaAdminPartnera + " " + id);
	    if (odgovor != null && odgovor.startsWith("OK"))
	        return Response.ok().build();                     
	    return Response.status(Response.Status.NO_CONTENT).build();     
	}

    /**
     * Zaustavlja partner poslužitelj slanjem KRAJ komande.
     */
	@Path("kraj")
	@HEAD
	@Operation(summary = "Zaustavljanje poslužitelja partner")
	@APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
	    @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
	@Counted(name = "brojZahtjeva_headPartnerKraj",
	         description = "Koliko puta je pozvana operacija zaustavljanja partner servisa")
	@Timed(name = "trajanjeMetode_headPartnerKraj",
	       description = "Vrijeme trajanja metode zaustavljanja partner servisa")
	public Response kraj() {
	    String odgovor = posaljiKomandu("KRAJ " + this.kodZaKrajPartner);
	    if (odgovor != null && odgovor.startsWith("OK")) {
	        return Response.ok().build();          
	    }
	    return Response.status(Response.Status.NO_CONTENT).build();
	}

    /**
     * Dohvaća jelovnik korisnika ako je autentikacija uspješna.
     */
	@Path("jelovnik")
	@GET @Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvat jelovnika za prijavljenog korisnika")
	@APIResponses({@APIResponse(responseCode = "200", description = "Uspješna operacija"),
	    @APIResponse(responseCode = "401", description = "Neautoriziran pristup"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	public Response getJelovnik(@HeaderParam("korisnik") String korisnickoIme,
	                            @HeaderParam("lozinka") String lozinka) {
	    try (var veza = restConfiguration.dajVezu()) {
	        var kor = new KorisnikDAO(veza).dohvati(korisnickoIme, lozinka, true);
	        if (kor == null) return Response.status(401).build();    
	        String rezultat = posaljiKomanduRad("JELOVNIK " + korisnickoIme);
	        if (rezultat == null)                          
	            return Response.status(500).build();
	        if (rezultat.startsWith("ERROR"))             
	            return Response.status(204).build();        
	        return Response.ok(rezultat, MediaType.APPLICATION_JSON).build();
	    } catch (Exception e) {
	        return Response.status(500).build();
	    }
	}

    /**
     * Dohvaća kartu pića za korisnika ako je autentikacija uspješna.
     */
	@Path("kartapica")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvat karte pića za prijavljenog korisnika")
	@APIResponses({@APIResponse(responseCode = "200", description = "Uspješna operacija"),
	    @APIResponse(responseCode = "401", description = "Neautoriziran pristup"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")
	})
	public Response getKartaPica(@HeaderParam("korisnik") String korisnickoIme,
	                             @HeaderParam("lozinka") String lozinka) {
	    try (var veza = restConfiguration.dajVezu()) {
	        var kor = new KorisnikDAO(veza).dohvati(korisnickoIme, lozinka, true);
	        if (kor == null) return Response.status(401).build();
	        String rezultat = posaljiKomanduRad("KARTAPIĆA " + korisnickoIme);
	        if (rezultat == null)               
	        	return Response.status(500).build();
	        if (rezultat.startsWith("ERROR"))   
	        	return Response.status(204).build();
	        return Response.ok(rezultat, MediaType.APPLICATION_JSON).build();
	    } catch (Exception e) {
	        return Response.status(500).build();
	    }
	}
	
    /**
     * Dohvaća otvorenu narudžbu korisnika ako postoji.
     */
	@Path("narudzba")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvat otvorene narudžbe za prijavljenog korisnika")
	@APIResponses({@APIResponse(responseCode = "200", description = "Otvorena narudžba uspješno dohvaćena"),
	    @APIResponse(responseCode = "401", description = "Neuspješna autentikacija korisnika"),
	    @APIResponse(responseCode = "500", description = "Greška pri dohvaćanju narudžbe")})
	public Response getNarudzba(@HeaderParam("korisnik") String korisnickoIme,
	                            @HeaderParam("lozinka") String lozinka) {
	    try (var veza = restConfiguration.dajVezu()) {
	        var dao = new KorisnikDAO(veza);
	        if (dao.dohvati(korisnickoIme, lozinka, true) == null)
	            return Response.status(401).build();
	        String rezultat = posaljiKomanduRad("NARUDŽBA " + korisnickoIme);
	        if (rezultat == null || rezultat.startsWith("ERROR"))
	            return Response.status(500).build();
	        int nl = rezultat.indexOf('\n');
	        String json = nl >= 0 ? rezultat.substring(nl + 1).trim() : "";
	        if (json.isEmpty() || json.equals("[]"))
	            return Response.status(500).build(); 

	        return Response.ok(json, MediaType.APPLICATION_JSON).build();
	    } catch (Exception e) {
	        return Response.status(500).build();
	    }
	}
	
    /**
     * Dodaje novu narudžbu korisniku ako ne postoji otvorena.
     */
	@Path("narudzba")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dodavanje nove narudžbe za korisnika")
	@APIResponses({@APIResponse(responseCode = "201", description = "Narudžba je uspješno dodana"),
	    @APIResponse(responseCode = "401", description = "Neautoriziran pristup"),
	    @APIResponse(responseCode = "409", description = "Narudžba već postoji"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_postNarudzbaPartner",
	         description = "Broj POST zahtjeva za dodavanje narudžbe")
	@Timed(name = "trajanjeMetode_postNarudzbaPartner",
	       description = "Trajanje metode dodavanja narudžbe partnera")
	public Response narudzba(@HeaderParam("korisnik") String korisnik,
	                             @HeaderParam("lozinka") String lozinka) {
	    try (var veza = restConfiguration.dajVezu()) {
	        if (new KorisnikDAO(veza).dohvati(korisnik, lozinka, true) == null)
	            return Response.status(401).build();                   
	        String odgovor = posaljiKomanduRad("NARUDŽBA " + korisnik);
	        if (odgovor == null)                
	        	return Response.status(500).build();
	        if (odgovor.startsWith("OK"))       
	        	return Response.status(201).build();
	        return Response.status(409).build();                     
	    } catch (Exception e) {
	        return Response.status(500).build();
	    }
	}

    /**
     * Dodaje jelo u narudžbu korisnika.
     */
	@Path("jelo")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)      
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dodavanje jela u narudžbu korisnika")
	@APIResponses({@APIResponse(responseCode = "201", description = "Jelo uspješno dodano"),
	    @APIResponse(responseCode = "401", description = "Neautoriziran pristup"),
	    @APIResponse(responseCode = "409", description = "Greška pri dodavanju"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_postJeloPartner", description = "Broj POST zahtjeva za jelo")
	@Timed(name = "trajanjeMetode_postJeloPartner", description = "Trajanje metode dodavanja jela")
	public Response jelo(@HeaderParam("korisnik") String korisnik,
	                         @HeaderParam("lozinka") String lozinka,
	                         Map<String,Object> body) {
		 try (var veza = restConfiguration.dajVezu()) {
		        if (new KorisnikDAO(veza).dohvati(korisnik, lozinka, true) == null)
		            return Response.status(401).build();
		        String id  = (String)  body.get("id");
		        Number k   = (Number)  body.get("kolicina");
		        if (id == null || k == null)        
		        	return Response.status(400).build();
		        String komanda = "JELO " + korisnik + " " + id + " " + k.floatValue();
		        String odgovor = posaljiKomanduRad(komanda);
		        if (odgovor == null)                
		        	return Response.status(500).build();
		        if (odgovor.startsWith("OK"))       
		        	return Response.status(201).build();
		        return Response.status(409).build();
		    } catch (Exception e) {
		        return Response.status(500).build();
		    }

	}

    /**
     * Dodaje piće u narudžbu korisnika.
     */
	@Path("pice")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)  
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dodavanje pića u narudžbu korisnika")
	@APIResponses({@APIResponse(responseCode = "201", description = "Piće uspješno dodano"),
	    @APIResponse(responseCode = "401", description = "Neautoriziran pristup"),
	    @APIResponse(responseCode = "409", description = "Greška pri dodavanju"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_postPicePartner", description = "Broj POST zahtjeva za piće")
	@Timed(name = "trajanjeMetode_postPicePartner", description = "Trajanje metode dodavanja pića")
	public Response pice(@HeaderParam("korisnik") String korisnik,
	                         @HeaderParam("lozinka") String lozinka,
	                         Map<String,Object> body) {              
	    try (var veza = restConfiguration.dajVezu()) {
	        if (new KorisnikDAO(veza).dohvati(korisnik, lozinka, true) == null)
	            return Response.status(401).build();
	        String id        = (String)  body.get("id");
	        Number kolicinaN = (Number)  body.get("kolicina");
	        if (id == null || kolicinaN == null || kolicinaN.floatValue() <= 0)
	            return Response.status(400).build();                
	        String komanda = "PIĆE " + korisnik + " " + id + " " + kolicinaN.floatValue();
	        String odgovor = posaljiKomanduRad(komanda);
	        if (odgovor == null)          
	        	return Response.status(500).build();
	        if (odgovor.startsWith("OK")) 
	        	return Response.status(201).build();
	        return Response.status(409).build();                    
	    } catch (Exception e) {
	        return Response.status(500).build();
	    }
	}

    /**
     * Kreira račun korisniku na temelju narudžbe.
     */
	@Path("racun")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Zahtjev za račun korisnika")
	@APIResponses({@APIResponse(responseCode = "201", description = "Račun uspješno kreiran"),
	    @APIResponse(responseCode = "401", description = "Neautoriziran pristup"),
	    @APIResponse(responseCode = "409", description = "Račun nije moguće kreirati"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_postRacunPartner", description = "Broj POST zahtjeva za račun")
	@Timed(name = "trajanjeMetode_postRacunPartner", description = "Trajanje metode kreiranja računa")
	public Response racun(@HeaderParam("korisnik") String korisnik,
	                          @HeaderParam("lozinka") String lozinka) {
		try (var veza = restConfiguration.dajVezu()) {
	        if (new KorisnikDAO(veza).dohvati(korisnik, lozinka, true) == null)
	            return Response.status(401).build();
	        String odgovor = posaljiKomanduRad("RAČUN " + korisnik);
	        if (odgovor == null)                
	        	return Response.status(500).build();
	        if (odgovor.startsWith("OK"))       
	        	return Response.status(201).build();
	        return Response.status(409).build();
	    } catch (Exception e) {
	        return Response.status(500).build();
	    }
	}

    /**
     * Dohvaća sve korisnike iz baze.
     */
	@Path("korisnik")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvat svih korisnika")
	@APIResponses({@APIResponse(responseCode = "200", description = "Korisnici dohvaćeni"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_getKorisnici", description = "Broj GET zahtjeva za sve korisnike")
	@Timed(name = "trajanjeMetode_getKorisnici", description = "Trajanje metode dohvaćanja korisnika")
	public Response getKorisnici() {
	    try (var veza = restConfiguration.dajVezu()) {
	        var dao = new KorisnikDAO(veza);
	        var korisnici = dao.dohvatiSve();
	        return Response.ok(korisnici, MediaType.APPLICATION_JSON).build();
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
    /**
     * Dohvaća jednog korisnika prema korisničkom imenu.
     */
	@Path("korisnik/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvat korisnika po korisničkom imenu")
	@APIResponses({@APIResponse(responseCode = "200", description = "Korisnik pronađen"),
	    @APIResponse(responseCode = "404", description = "Korisnik nije pronađen"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_getKorisnikId", description = "Broj GET zahtjeva za korisnika po ID-u")
	@Timed(name = "trajanjeMetode_getKorisnikId", description = "Trajanje metode dohvaćanja korisnika po ID-u")
	public Response getKorisnik(@PathParam("id") String korisnickoIme) {
	    try (var veza = restConfiguration.dajVezu()) {
	        var dao = new KorisnikDAO(veza);
	        var korisnik = dao.dohvati(korisnickoIme, "", false); 
	        if (korisnik == null) {
	            return Response.status(Response.Status.NOT_FOUND).build();
	        }
	        return Response.ok(korisnik, MediaType.APPLICATION_JSON).build();
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}

    /**
     * Dodaje novog korisnika ako ne postoji u bazi.
     */
	@Path("korisnik")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dodavanje novog korisnika")
	@APIResponses({@APIResponse(responseCode = "201", description = "Korisnik uspješno dodan"),
	    @APIResponse(responseCode = "409", description = "Korisnik već postoji ili nije moguće dodati"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_postKorisnik", description = "Broj POST zahtjeva za korisnika")
	@Timed(name = "trajanjeMetode_postKorisnik", description = "Trajanje metode dodavanja korisnika")
	public Response postKorisnik(Korisnik korisnik) {
	    try (var veza = restConfiguration.dajVezu()) {
	        var dao = new KorisnikDAO(veza);
	        boolean uspjeh = dao.dodaj(korisnik);
	        if (uspjeh) {
	            return Response.status(Response.Status.CREATED).build();
	        } else {
	            return Response.status(Response.Status.CONFLICT).build();
	        }
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}

    /**
     * Uspavljuje dretvu na određeno vrijeme.
     */
	@Path("spava")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Spavanje dretve određeno vrijeme")
	@APIResponses({@APIResponse(responseCode = "200", description = "Dretva je uspješno uspavana"),
	    @APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_spava", description = "Broj GET zahtjeva za spavanje")
	@Timed(name = "trajanjeMetode_spava", description = "Trajanje metode spavanja")
	public Response getSpava(@jakarta.ws.rs.QueryParam("vrijeme") Long trajanje) {
	    if (trajanje == null || trajanje < 0) {
	        return Response.status(Response.Status.BAD_REQUEST).build();
	    }
	    try {
	        Thread.sleep(trajanje);
	        return Response.ok().build();
	    } catch (InterruptedException ex) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}

     /**
     * Pomoćna metoda za slanje naredbi poslužitelju kraj.
     */
	private String posaljiKomandu(String komanda) {
	    try (var socket = new Socket(this.partnerAdresa, Integer.parseInt(this.mreznaVrataKrajPartner))) {
	        var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	        var out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));
	        out.write(komanda + "\n");
	        out.flush();
	        socket.shutdownOutput();
	        var odgovor = in.readLine();
	        socket.shutdownInput();
	        socket.close();
	        return odgovor;
	    } catch (IOException e) {
	        return null;
	    }
	}
	
     /**
     * Pomoćna metoda za slanje naredbi poslužitelju rad.
     */
	private String posaljiKomanduRad(String komanda) {
	    try (Socket socket = new Socket(partnerAdresa,
	                                    Integer.parseInt(mreznaVrataRadPartner));
	         BufferedReader  in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf8"));
	         PrintWriter      out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"utf8"))) {
	        out.println(komanda); out.flush(); socket.shutdownOutput();
	        String first = in.readLine();          
	        if (first == null) return null;
	        if (first.startsWith("OK")) {
	            String second = in.readLine();     
	            return (second == null) ? "OK" : second;
	        }
	        return first;                          
	    } catch (IOException e) {
	        return null;
	    }
	}
}
