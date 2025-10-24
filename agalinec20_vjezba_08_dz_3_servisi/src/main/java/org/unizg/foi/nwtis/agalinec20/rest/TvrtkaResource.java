package org.unizg.foi.nwtis.agalinec20.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.dao.PartnerDAO;
import edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.dao.ObracunDAO;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;
import edu.unizg.foi.nwtis.podaci.Jelovnik; 
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.gson.JsonSyntaxException;

@Path("api/tvrtka")
public class TvrtkaResource {

  @Inject
  @ConfigProperty(name = "adresa")
  private String tvrtkaAdresa;
  @Inject
  @ConfigProperty(name = "mreznaVrataKraj")
  private String mreznaVrataKraj;
  
  /** The mrezna vrata registracija. */
  @Inject
  @ConfigProperty(name = "mreznaVrataRegistracija")
  private String mreznaVrataRegistracija;
  @Inject
  @ConfigProperty(name = "mreznaVrataRad")
  private String mreznaVrataRad;
  @Inject
  @ConfigProperty(name = "kodZaAdminTvrtke")
  private String kodZaAdminTvrtke;
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;

  @Inject
  RestConfiguration restConfiguration;
  
  @Inject
  @RestClient
  TvrtkaInfoKlijent klijentTvrtkaInfo; 

    /**
     * HEAD metoda koja provjerava radi li poslužitelj za kraj rada.
     */
  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response head() {
	  var status = posaljiKomandu("KRAJ " + kodZaKraj);
    if (status != null) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.CONFLICT).build();
    }
  }

    /**
     * Provjerava status pojedinog poslužitelja po ID-u (0, 1 ili 2).
     */
  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa dijela poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_eadPosluziteljStatus",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_eadPosluziteljStatus", description = "Vrijeme trajanja metode")
  public Response status(@PathParam("id") int id) {
	    if (id != 0 && id != 1 && id != 2)
	        return Response.status(Response.Status.NO_CONTENT).build();          
	    String odgovor = posaljiKomandu("STATUS " + kodZaAdminTvrtke + " " + id);
	    if (odgovor == null)                       
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    if (odgovor.startsWith("OK"))              
	        return Response.ok().build();                                         
	    return Response.status(Response.Status.NO_CONTENT).build();             
	}

    /**
     * Postavlja određeni poslužitelj u stanje pauze.
     */
  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u pauzu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljPauza",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
  public Response pauza(@PathParam("id") int id) {
	    String odgovor = posaljiKomandu("PAUZA " + kodZaAdminTvrtke + " " + id);
	    if (odgovor == null)
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    if (odgovor.startsWith("OK"))
	        return Response.ok().build();                                        
	    return Response.status(Response.Status.NO_CONTENT).build();             
	}

    /**
     * Vraća određeni poslužitelj u aktivno stanje rada.
     */
  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u rad")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljStart",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStart(@PathParam("id") int id) {
	  String odgovor = posaljiKomandu("START " + kodZaAdminTvrtke + " " + id);
	    if (odgovor == null)
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    if (odgovor.startsWith("OK"))
	        return Response.ok().build();                                       
	    return Response.status(Response.Status.NO_CONTENT).build();        
	}

  /**
 * Zatvara glavni poslužitelj tvrtke (KRAJ komanda).
 */
  @Path("kraj")
  @HEAD
  @Operation(summary = "Zaustavljanje poslužitelja tvrtka")
  @APIResponses({      @APIResponse(responseCode = "200", description = "Tvrtka se uredno ugasila"),
      @APIResponse(responseCode = "204", description = "Zahtjev nije prihvaćen ili se nije moguće spojiti")
  })
  @Counted(name = "brojZahtjeva_headPosluziteljKraj",
           description = "Koliko puta je pozvan kraj za tvrtku")
  @Timed(name = "trajanjeMetode_headPosluziteljKraj",
         description = "Trajanje izvršavanja naredbe KRAJ")
  public Response kraj() {
	    String odgovor = posaljiKomandu("KRAJ " + kodZaKraj);

	    if (odgovor != null && odgovor.startsWith("OK")) {
	        return Response.ok().build();             
	    }
	    return Response.status(Response.Status.NO_CONTENT).build();   
	}

    /**
     * Ispisuje informaciju da je poslužitelj završio s radom.
     */
  @HEAD
  @Path("kraj/info")
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses({@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response krajInfo() {
	    try {
	        Response odgovor = klijentTvrtkaInfo.getKrajInfo();
	        return Response.status(odgovor.getStatus()).build();
	    } catch (Exception e) {
	        return Response.status(Response.Status.NO_CONTENT).build();
	    }
	}

    /**
     * Dohvaća sve jelovnike svih partnera iz sustava.
     */
  @Path("jelovnik")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvat svih jelovnika svih partnera")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")
  })
  @Counted(name = "brojZahtjeva_getSviJelovnici", description = "Broj GET zahtjeva za sve jelovnike")
  @Timed(name = "trajanjeMetode_getSviJelovnici", description = "Trajanje metode dohvaćanja svih jelovnika")
  public Response getJelovnik() {
	    try (var veza = restConfiguration.dajVezu()) {
	        var partneri = new PartnerDAO(veza).dohvatiSve(false);

	        Map<String, List<Jelovnik>> rezultat = new HashMap<>();

	        for (Partner p : partneri) {
	            List<Jelovnik> jela = preuzmiJelovnikOdTvrtke(p);

	            rezultat.computeIfAbsent(p.vrstaKuhinje(), k -> new ArrayList<>())
	                    .addAll(jela);
	        }
	        return rezultat.isEmpty()
	        	       ? Response.status(Response.Status.NO_CONTENT).build()
	        	       : Response.ok(rezultat).build();


	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}

    /**
     * Dohvaća jelovnik za jednog partnera prema ID-u.
     */
  @Path("jelovnik/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvat jelovnika zadanog partnera")
  @APIResponses({@APIResponse(responseCode = "200", description = "Jelovnik pronađen"),
      @APIResponse(responseCode = "404", description = "Partner ne postoji"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnikId",
           description = "Broj GET zahtjeva za jelovnik partnera")
  @Timed(name = "trajanjeMetode_getJelovnikId",
         description = "Trajanje metode dohvaćanja jelovnika partnera")
  public Response getJelovnikPartnera(@PathParam("id") int id) {
	  try (Connection veza = restConfiguration.dajVezu()) {
	        PartnerDAO pDao = new PartnerDAO(veza);
	        Partner partner = pDao.dohvati(id, false);
	        if (partner == null) {
	            return Response.status(Response.Status.NOT_FOUND).build();
	        }
	        try (Socket sock = new Socket(tvrtkaAdresa, Integer.parseInt(mreznaVrataRad));
	             PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
	             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {
	            out.println("JELOVNIK " + partner.id() + " " + partner.sigurnosniKod());
	            String status = in.readLine();
	            if (!"OK".equals(status)) {
	                return Response.status(Response.Status.NOT_FOUND).build();
	            }
	            String json = in.readLine();
	            Jelovnik[] niz = new Gson().fromJson(json, Jelovnik[].class);
	            return Response.ok(Arrays.asList(niz)).build();
	        }
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}

    /**
     * Dohvaća kartu pića jednog partnera iz sustava.
     */
  @Path("kartapica")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvat karte pića svih partnera")
  @APIResponses({@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getSvaPica", description = "Broj GET zahtjeva za sva pića partnera")
  @Timed(name = "trajanjeMetode_getSvaPica", description = "Trajanje metode dohvaćanja svih pića partnera")
  public Response getKartaPica() {
	    try (var veza = restConfiguration.dajVezu()) {
	        var partneri = new PartnerDAO(veza).dohvatiSve(false);
	        if (partneri.isEmpty())
	            return Response.status(Response.Status.NO_CONTENT).build();
	        String json = null;
	        for (Partner p : partneri) {
	            String cmd = "KARTAPIĆA " + p.id() + ' ' + p.sigurnosniKod();
	            String odg = posaljiKomanduRad(cmd);

	            if (odg != null && odg.startsWith("OK")) {
	                int idx = odg.indexOf('\n');
	                json = (idx >= 0 ? odg.substring(idx + 1) : odg.substring(2)).trim();
	                break;               
	            }
	        }
	        if (json == null || json.isEmpty())
	            return Response.status(Response.Status.BAD_GATEWAY).build();
	        return Response.ok(json, MediaType.APPLICATION_JSON).build();
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}

    /**
     * Dohvaća sve registrirane partnere iz baze.
     */
  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartneri",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartneri", description = "Vrijeme trajanja metode")
  public Response getPartneri() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(true);
      return Response.ok(partneri).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

    /**
     * Dohvaća samo partnere koji su registrirani u sustavu i aktivni.
     */
  @Path("partner/provjera")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvat partnera koji su aktivni na poslužitelju i u bazi")
  @APIResponses({@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  public Response getPartneriProvjera() {
      try (var veza = restConfiguration.dajVezu()) {
          var partnerDAO = new PartnerDAO(veza);
          var sviPartneri = partnerDAO.dohvatiSve(true);
          var komanda = "POPIS";
          var json = posaljiKomanduRegistracija(komanda);
          if (json == null || json.startsWith("ERROR")) {
              return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                             .entity("Neuspješna POPIS komanda").build();
          }
          json = json.replaceFirst("^OK\\s*", "");
          Type listType = new TypeToken<List<PartnerPopis>>() {}.getType();
          Gson gson = new Gson();
          List<PartnerPopis> partneriPopis;
          try {
              partneriPopis = gson.fromJson(json, listType);
          } catch (JsonSyntaxException e) {
              return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                             .entity("Neispravan JSON odgovor poslužitelja: " + json).build();
          }
          List<Integer> aktivniIdjevi = partneriPopis.stream().map(PartnerPopis::id).toList();
          var filtrirani = sviPartneri.stream().filter(p -> aktivniIdjevi.contains(p.id())).toList();
          return Response.ok(filtrirani, MediaType.APPLICATION_JSON).build();
      } catch (Exception e) {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
  }

    /**
     * Dohvaća jednog partnera po ID-u.
     */
  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartner", description = "Vrijeme trajanja metode")
  public Response getPartner(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partner = partnerDAO.dohvati(id, true);
      if (partner != null) {
        return Response.ok(partner).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

    /**
     * Dodaje novog partnera u bazu.
     */
  @Path("partner")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPartner", description = "Vrijeme trajanja metode")
  public Response postPartner(Partner partner) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var status = partnerDAO.dodaj(partner);
      if (status) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @GET
  @Path("obracun")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvat svih obračuna s opcionalnim vremenskim filtriranjem")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")
  })
  public Response getObracuni(@jakarta.ws.rs.QueryParam("od") Long od,
                              @jakarta.ws.rs.QueryParam("do") Long kraj) {
    try (var veza = restConfiguration.dajVezu()) {
      var dao = new ObracunDAO(veza);
      var rezultati = dao.dohvatiSve(od, kraj);
      return Response.ok(rezultati).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

    
    /**
     * Dohvaća samo obračune za jela, uz vremensko filtriranje.
     */
  @GET
  @Path("obracun/jelo")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvat obračuna za jela u zadanom vremenskom rasponu")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")
  })
  public Response getObracuniJelo(@jakarta.ws.rs.QueryParam("od") Long od,
                                  @jakarta.ws.rs.QueryParam("do") Long kraj) {
    try (var veza = restConfiguration.dajVezu()) {
      var dao = new ObracunDAO(veza);
      var rezultati = dao.dohvatiPoTipu(true, od, kraj);
      return Response.ok(rezultati).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }




    /**
     * Dohvaća samo obračune za pića, uz vremensko filtriranje.
     */
  @GET
  @Path("obracun/pice")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvat obračuna za pića u zadanom vremenskom rasponu")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")
  })
  public Response getObracuniPice(@jakarta.ws.rs.QueryParam("od") Long od,
                                  @jakarta.ws.rs.QueryParam("do") Long kraj) {
    try (var veza = restConfiguration.dajVezu()) {
      var dao = new ObracunDAO(veza);
      var rezultati = dao.dohvatiPoTipu(false, od, kraj);
      return Response.ok(rezultati).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


    /**
     * Dohvaća obračune za određenog partnera s vremenskim filtriranjem.
     */
  @GET
  @Path("obracun/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvat obračuna za odabranog partnera u vremenskom rasponu")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")
  })
  public Response getObracuniPartner(@jakarta.ws.rs.PathParam("id") int id,
                                     @jakarta.ws.rs.QueryParam("od") Long od,
                                     @jakarta.ws.rs.QueryParam("do") Long kraj) {
    try (var veza = restConfiguration.dajVezu()) {
      var dao = new ObracunDAO(veza);
      var rezultati = dao.dohvatiPoPartneru(id, od, kraj);
      return Response.ok(rezultati).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


    /**
     * Dodaje jedan obračun u bazu.
     */
  @POST
  @Path("obracun")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Unos novog obračuna u bazu")
  @APIResponses({@APIResponse(responseCode = "201", description = "Obračun uspješno spremljen"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  public Response postObracun(Obracun obracun) {
	    try (var veza = restConfiguration.dajVezu()) {
	        var dao = new ObracunDAO(veza);
	        boolean status = dao.dodaj(obracun);
	        if (!status) {
	            return Response.status(Response.Status.CONFLICT).build();
	        }
	        try {
	            Response odgovor = klijentTvrtkaInfo.getObracunWS();
	            return Response.status(odgovor.getStatus()).build();
	        } catch (Exception e) {
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	        }
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
	}
  
    /**
     * Dodaje više obračuna i šalje ih poslužitelju (OBRAČUNWS).
     */
  @Path("obracun/ws")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dodavanje obračuna i slanje na poslužitelj")
  @APIResponses({@APIResponse(responseCode = "201", description = "Uspješno dodani i proslijeđeni obračuni"),
	    @APIResponse(responseCode = "500", description = "Greška kod obrade obračuna")})
  public Response postObracunWS(List<Obracun> obracuni) {
	  try {
          Map<Integer, List<Obracun>> poPartnerima = obracuni.stream()
              .collect(Collectors.groupingBy(Obracun::partner));
          for (var entry : poPartnerima.entrySet()) {
              int idPartnera = entry.getKey();
              List<Obracun> obracuniZaPartnera = entry.getValue();
              try (var veza = restConfiguration.dajVezu()) {
                  var partnerDAO = new PartnerDAO(veza);
                  var partner = partnerDAO.dohvati(idPartnera, false);
                  if (partner == null) continue;
                  String komanda = "OBRAČUNWS " + idPartnera + " " + partner.sigurnosniKod();
                  String json = new Gson().toJson(obracuniZaPartnera);
                  String odgovor = posaljiKomanduRad(komanda + "\n" + json);
                  if (odgovor == null || !odgovor.startsWith("OK")) {
                      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                     .entity("Greška kod slanja za partnera " + idPartnera).build();
                  }
              }
          }
          return Response.status(Response.Status.CREATED).build(); 
      } catch (Exception e) {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
  }

    /**
     * Metoda koja uspavljuje dretvu na zadano vrijeme.
     */
  @Path("spava")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Usleep dretve određeno vrijeme")
  @APIResponses({@APIResponse(responseCode = "200", description = "Dretva uspješno uspavana"),
      @APIResponse(responseCode = "500", description = "Greška tijekom uspavljivanja dretve")})
  public Response getSpava(@jakarta.ws.rs.QueryParam("vrijeme") Long trajanje) {
      try {
          if (trajanje == null || trajanje < 0) {
              throw new IllegalArgumentException("Parametar 'vrijeme' mora biti >= 0");
          }
          Thread.sleep(trajanje);
          return Response.ok().build();
      } catch (Exception ex) {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity(Map.of("error", ex.getMessage()))
                         .build();
      }
  }

  private String posaljiKomandu(String komanda) {
    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataKraj));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();
      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
      return linija;
    } catch (IOException e) {
    }
    return null;
  }

    /**
     * Šalje komandu poslužitelju za rad i vraća odgovor.
     * Koristi se za komunikaciju s poslužiteljem koji obrađuje jelovnike, pića i obračune.
     */
  private String posaljiKomanduRad(String komanda) {
    try (var soket = new Socket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataRad))) {
      var in = new BufferedReader(new InputStreamReader(soket.getInputStream(), "utf8"));
      var out = new PrintWriter(new OutputStreamWriter(soket.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();
      soket.shutdownOutput();
      var odgovor = in.readLine();
      soket.shutdownInput();
      return odgovor;
    } catch (IOException ex) {
      return null;
    }
  }

  private String posaljiKomanduRegistracija(String komanda) {
	    try (var soket = new Socket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataRegistracija))) {
	        var in = new BufferedReader(new InputStreamReader(soket.getInputStream(), "utf8"));
	        var out = new PrintWriter(new OutputStreamWriter(soket.getOutputStream(), "utf8"));
	        out.write(komanda + "\n");
	        out.flush();
	        soket.shutdownOutput();
	        String odgovor1 = in.readLine(); 
	        String odgovor2 = in.readLine(); 
	        soket.shutdownInput();
	        soket.close();
	        if (odgovor1 == null || !odgovor1.startsWith("OK") || odgovor2 == null) {
	            return "ERROR";
	        }
	        return "OK " + odgovor2;
	    } catch (IOException ex) {
	        return "ERROR";
	    }
	}

    /**
     * Šalje komandu OBRAČUNWS i pripadajući JSON niz na poslužitelj za rad.
     */
  private String posaljiKomanduRadObracunWS(String komanda, String json) {
	    try (var soket = new Socket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataRad));
	         var in = new BufferedReader(new InputStreamReader(soket.getInputStream(), "utf8"));
	         var out = new PrintWriter(new OutputStreamWriter(soket.getOutputStream(), "utf8"))) {
		        out.write(komanda + "\n");
		        out.write(json + "\n");
		        out.flush();
		        soket.shutdownOutput();
	        return in.readLine(); 
	    } catch (IOException ex) {
	        return null;
	    }
	}
  
    /**
     * Pomoćna metoda koja dohvaća jelovnik od poslužitelja za određenog partnera.
     * Parsira odgovor u listu objekata Jelovnik.
     */
  private List<Jelovnik> preuzmiJelovnikOdTvrtke(Partner p) {
	    String odgovor = posaljiKomanduRad("JELOVNIK " + p.id() + ' ' + p.sigurnosniKod());
	    if (odgovor == null || !odgovor.startsWith("OK"))
	        return List.of();                   
	    int idx = odgovor.indexOf('\n');
	    String json = (idx >= 0 ? odgovor.substring(idx + 1) : odgovor.substring(2)).trim();
	    return Arrays.asList(new Gson().fromJson(json, Jelovnik[].class));
	}
  
  
}
