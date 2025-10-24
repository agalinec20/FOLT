/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;


/**
 *
 * @author NWTiS
 */
@Controller
@Path("tvrtka")
@RequestScoped
public class Kontroler {

  @Inject
  private Models model;

  @Inject
  @RestClient
  ServisTvrtkaKlijent servisTvrtka;

  @GET
  @Path("pocetak")
  @View("index.jsp")
  public void pocetak() {}
  
  @GET
  @Path("provjera")
  @View("provjeraRadaPosluziteljaTvrtka.jsp")
  public void provjeraPosluzitelja() {
      var odgovor = servisTvrtka.headPosluzitelj();
      model.put("status", odgovor.getStatus());
  }

  @GET
  @Path("admin/nadzornaKonzolaTvrtka")
  @View("nadzornaKonzolaTvrtka.jsp")
  public void nadzornaKonzolaTvrtka() {}

  @GET
  @Path("kraj")
  @View("status.jsp")
  public void kraj() {
    var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
    this.model.put("statusOperacije", status);
    dohvatiStatuse();
  }
  
  @POST
  @Path("kraj")
  @View("status.jsp")
  public void krajPost() {
    var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
    this.model.put("statusOperacije", status);
    dohvatiStatuse();
  }

  @GET
  @Path("status")
  @View("status.jsp")
  public void status() {
    dohvatiStatuse();
  }
  
  @GET
  @Path("status/{id}")
  @View("status.jsp")
  public void statusId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljStatus(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  @GET
  @Path("start/{id}")
  @View("status.jsp")
  public void startId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  @GET
  @Path("pauza/{id}")
  @View("status.jsp")
  public void pauzatId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljPauza(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  @GET
  @Path("partner")
  @View("partneri.jsp")
  public void partneri() {
    var odgovor = this.servisTvrtka.getPartneri();
    var status = odgovor.getStatus();
    if (status == 200) {
      var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {});
      this.model.put("status", status);
      this.model.put("partneri", partneri);
    }
  }
  
  @GET
  @Path("partner/{id}")
  @View("partner.jsp")
  public void partnerDetalji(@PathParam("id") int id) {
    var odgovor = this.servisTvrtka.getPartner(id);
    var status = odgovor.getStatus();
    if (status == 200) {
      var partner = odgovor.readEntity(Partner.class);
      this.model.put("partner", partner);
    } else {
      this.model.put("partner", null);
    }
  }
  
  @GET
  @Path("admin/partner")
  @View("noviPartner.jsp")
  public void unosPartneraGet() {}

  @POST
  @Path("admin/partner")
  @View("noviPartner.jsp")
  public void unosPartneraPost(
      @FormParam("id") int id,
      @FormParam("naziv") String naziv,
      @FormParam("vrstaKuhinje") String vrstaKuhinje,
      @FormParam("adresa") String adresa,
      @FormParam("mreznaVrata") int mreznaVrata,
      @FormParam("mreznaVrataKraj") int mreznaVrataKraj,
      @FormParam("adminKod") String adminKod,
      @FormParam("gpsSirina") float gpsSirina,
      @FormParam("gpsDuzina") float gpsDuzina
  ) {
	var partner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, "", adminKod);
    var odgovor = this.servisTvrtka.postPartner(partner);
    this.model.put("status", odgovor.getStatus());
  }

  private void dohvatiStatuse() {
	    this.model.put("samoOperacija", false);

	    try (Response r = servisTvrtka.headPosluzitelj()) {
	        model.put("statusT", r.getStatus());
	    } catch (Exception e) {
	        e.printStackTrace();   
	        model.put("statusT", -1);
	    }

	
	    try (Response r1 = servisTvrtka.headPosluziteljStatus(1)) {
	        model.put("statusT1", r1.getStatus());
	    } catch (Exception e) {
	        e.printStackTrace();   
	        model.put("statusT1", -1);
	        model.put("errorStatus1", e.getClass().getName() + ": " + e.getMessage());
	    }

	
	    try (Response r2 = servisTvrtka.headPosluziteljStatus(2)) {
	        model.put("statusT2", r2.getStatus());
	    } catch (Exception e) {
	        e.printStackTrace(); 
	        model.put("statusT2", -1);
	        model.put("errorStatus2", e.getClass().getName() + ": " + e.getMessage());
	    }
	}
  
  @GET
  @Path("privatno/obracuni")
  @View("obracuni.jsp")
  public void pregledObracuna(@QueryParam("od") Long od,
                               @QueryParam("do") Long kraj,
                               @QueryParam("filter") String filter,
                               @Context HttpServletRequest request) {
      List<Obracun> obracuni = null;
      Response odgovor;

      if ("jelo".equals(filter)) {
          odgovor = servisTvrtka.getObracuniJelo(od, kraj);
      } else if ("pice".equals(filter)) {
          odgovor = servisTvrtka.getObracuniPice(od, kraj);
      } else {
          odgovor = servisTvrtka.getObracuni(od, kraj);
      }

      int status = odgovor.getStatus();
      if (status == 200) {
          obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      }

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      request.setAttribute("status", status);
      request.setAttribute("obracuni", obracuni);
      request.setAttribute("od", od != null ? sdf.format(new Date(od)) : null);
      request.setAttribute("do", kraj != null ? sdf.format(new Date(kraj)) : null);
      request.setAttribute("filter", filter);
  }
  
  @GET
  @Path("privatno/obracuni/partner")
  @View("obracuniPartner.jsp")
  public void pregledObracunaPartner(@QueryParam("partnerId") Integer idPartnera,
                                     @QueryParam("od") Long od,
                                     @QueryParam("do") Long kraj,
                                     @Context HttpServletRequest request) {
      List<Obracun> obracuni = null;
      List<Partner> partneri = null;
      Integer status = null;

      Response r1 = servisTvrtka.getPartneri();
      if (r1.getStatus() == 200) {
          partneri = r1.readEntity(new GenericType<List<Partner>>() {});
      }

      if (idPartnera != null) {
          Response r2 = servisTvrtka.getObracuniPartner(idPartnera, od, kraj);
          status = r2.getStatus();
          if (status == 200) {
              obracuni = r2.readEntity(new GenericType<List<Obracun>>() {});
          }
      }

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      request.setAttribute("status", status);
      request.setAttribute("partneri", partneri);
      request.setAttribute("partnerId", idPartnera != null ? idPartnera.toString() : null);
      request.setAttribute("od", od != null ? sdf.format(new Date(od)) : null);
      request.setAttribute("do", kraj != null ? sdf.format(new Date(kraj)) : null);
      request.setAttribute("obracuni", obracuni);
  }

  @GET
  @Path("admin/spava")
  @View("spavanje.jsp")
  public void spavaGet() {}

  @POST
  @Path("admin/spava")
  @View("spavanje.jsp")
  public void spavaPost(@FormParam("vrijeme") int vrijeme) {
    var odgovor = this.servisTvrtka.headSpava(vrijeme);
    this.model.put("status", odgovor.getStatus());
    this.model.put("vrijeme", vrijeme);
  }

}
