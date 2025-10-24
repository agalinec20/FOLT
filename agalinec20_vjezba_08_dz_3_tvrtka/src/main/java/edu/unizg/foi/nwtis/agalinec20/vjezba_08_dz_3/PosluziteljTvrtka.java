package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;

/**
 * The Class PosluziteljTvrtka.
 */
public class PosluziteljTvrtka {

  /** Kuhinje. */
  private Map<String, String> kuhinje = new ConcurrentHashMap<>();

  /** Jelovnici. */
  private Map<String, Map<String, Jelovnik>> jelovnici = new ConcurrentHashMap<>();

  /** Karta pića. */
  private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

  /** Partneri. */
  private Map<Integer, Partner> partneri = new ConcurrentHashMap<>();

  /** Konfiguracijski podaci. */
  private Konfiguracija konfig;

  /** Pokretač dretvi. */
  private ExecutorService executor = null;

  /** Pauza dretve. */
  private int pauzaDretve = 1000;

  /** Kod za kraj rada. */
  private String kodZaKraj = "";

  /** Zastavica za kraj rada. */
  private AtomicBoolean kraj = new AtomicBoolean(false);

  /** The lock. */
  private final Object lock = new Object();
  
  private String restAdresa;     

  
  private AtomicBoolean pauzaRegistracija = new AtomicBoolean(false);
  private AtomicBoolean pauzaPartneri = new AtomicBoolean(false);

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      return;
    }

    var program = new PosluziteljTvrtka();
    var nazivDatoteke = args[0];

    program.pripremiKreni(nazivDatoteke);
  }


  /**
   * Pripremi kreni.
   *
   * @param nazivDatoteke the naziv datoteke
   */
  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }

    if (!this.ucitajPartnere()) {
      return;
    }

    if (!this.ucitajKuhinju()) {
      return;
    }

    if (!this.ucitajJelovnike()) {
      return;
    }

    if (!this.ucitajKartuPica()) {
      return;
    }

    this.restAdresa = this.konfig.dajPostavku("restAdresa").trim();
    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);

    var dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());
    var dretvaZaRegistraciju = this.executor.submit(() -> this.pokreniPosluziteljRegistracija());
    var dretvaZaRad = this.executor.submit(() -> this.pokreniPosluziteljRad());

    while (!dretvaZaKraj.isDone()) {
      try {
        Thread.sleep(this.pauzaDretve);
      } catch (InterruptedException e) {
      }
    }

    dretvaZaRegistraciju.cancel(true);
    dretvaZaRad.cancel(true);
  }

  /**
   * Pokreni posluzitelj kraj.
   */
  public void pokreniPosluziteljKraj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
    var brojCekaca = 0;

    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        var mreznaUticnica = ss.accept();
        this.obradiKraj(mreznaUticnica);

      }
      ss.close();

    } catch (IOException e) {
    }
  }

  /**
   * Pokreni posluzitelj registracija.
   */
  public void pokreniPosluziteljRegistracija() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));

    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        var mreznaUticnica = ss.accept();
        this.executor.submit(() -> this.obradiRegistraciju(mreznaUticnica));
      }
    } catch (IOException e) {
    }
  }

  /**
   * Pokreni posluzitelj rad.
   */
  public void pokreniPosluziteljRad() {
    int mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
    int brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));

    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        try {
          var mreznaUticnica = ss.accept();
          this.executor.submit(() -> this.obradiRad(mreznaUticnica));
        } catch (IOException e) {
        }
      }
    } catch (IOException e) {
    }
  }

  /**
   * Obradi kraj.
   *
   * @param mreznaUticnica the mrezna uticnica
   * @return the boolean
   */
  public Boolean obradiKraj(Socket mreznaUticnica) {
	    try (BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
	         PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {

	        String naredba = in.readLine().trim();
	        mreznaUticnica.shutdownInput();

	        if (obradiStatus(naredba, out) ||
	            obradiPauza(naredba, out) ||
	            obradiStart(naredba, out) ||
	            obradiSpava(naredba, out) ||
	            obradiKrajWS(naredba, out) ||
	            obradiOsvjezi(naredba, out) ||
	            obradiKrajLokalan(naredba, out)) {
	            return true;
	        }

	        out.write("ERROR 10 - Format komande nije ispravan\n");
	        out.flush();
	    } catch (Exception e) {
	        posaljiGresku(mreznaUticnica, "ERROR 19 - Nešto drugo nije u redu");
	    } finally {
	        zatvoriMreznuUticnicu(mreznaUticnica);
	    }
	    return true;
	}

      /**
     * Obrada komande STATUS – provjerava je li zadani dio sustava aktivan ili u pauzi.
     */
	private boolean obradiStatus(String naredba, PrintWriter out) {
	    Matcher m = Pattern.compile("^STATUS\\s+(\\w+)\\s+([12])$").matcher(naredba);
	    if (!m.matches()) return false;

	    String kod = m.group(1);
	    int dio = Integer.parseInt(m.group(2));
	    String ispravniKod = this.konfig.dajPostavku("kodZaAdminTvrtke");

	    if (!kod.equals(ispravniKod)) {
	        out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
	    } else {
	        int status = switch (dio) {
	            case 1 -> this.pauzaRegistracija.get() ? 0 : 1;
	            case 2 -> this.pauzaPartneri.get() ? 0 : 1;
	            default -> -1;
	        };
	        if (status == -1) {
	            out.write("ERROR 10 - Neispravan broj dijela sustava\n");
	        } else {
	            out.write("OK " + status + "\n");
	        }
	    }
	    out.flush();
	    return true;
	}

     /**
     * Obrada komande PAUZA – stavlja zadani dio sustava u pauzu ako već nije u pauzi.
     */
	private boolean obradiPauza(String naredba, PrintWriter out) {
	    Matcher m = Pattern.compile("^PAUZA\\s+(\\w+)\\s+([12])$").matcher(naredba);
	    if (!m.matches()) return false;

	    String kod = m.group(1);
	    int dio = Integer.parseInt(m.group(2));
	    String ispravniKod = this.konfig.dajPostavku("kodZaAdminTvrtke");

	    if (!kod.equals(ispravniKod)) {
	        out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
	    } else {
	        boolean vecUPauzi = switch (dio) {
	            case 1 -> this.pauzaRegistracija.get();
	            case 2 -> this.pauzaPartneri.get();
	            default -> true;
	        };
	        if (vecUPauzi) {
	            out.write("ERROR 13 - Pogrešna promjena pauze\n");
	        } else {
	            switch (dio) {
	                case 1 -> this.pauzaRegistracija.set(true);
	                case 2 -> this.pauzaPartneri.set(true);
	            }
	            out.write("OK\n");
	        }
	    }
	    out.flush();
	    return true;
	}

    /**
     * Obrada komande START – pokreće zadani dio sustava ako je trenutno u pauzi.
     */
	private boolean obradiStart(String naredba, PrintWriter out) {
	    Matcher m = Pattern.compile("^START\\s+(\\w+)\\s+([12])$").matcher(naredba);
	    if (!m.matches()) return false;

	    String kod = m.group(1);
	    int dio = Integer.parseInt(m.group(2));
	    String ispravniKod = this.konfig.dajPostavku("kodZaAdminTvrtke");

	    if (!kod.equals(ispravniKod)) {
	        out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
	    } else {
	        boolean vecAktivno = switch (dio) {
	            case 1 -> !this.pauzaRegistracija.get();
	            case 2 -> !this.pauzaPartneri.get();
	            default -> true;
	        };
	        if (vecAktivno) {
	            out.write("ERROR 13 - Pogrešna promjena starta\n");
	        } else {
	            switch (dio) {
	                case 1 -> this.pauzaRegistracija.set(false);
	                case 2 -> this.pauzaPartneri.set(false);
	            }
	            out.write("OK\n");
	        }
	    }
	    out.flush();
	    return true;
	}

     /**
     * Obrada komande SPAVA – dretva se uspava na zadani broj milisekundi.
     */
	private boolean obradiSpava(String naredba, PrintWriter out) {
	    Matcher m = Pattern.compile("^SPAVA\\s+(\\w+)\\s+(\\d+)$").matcher(naredba);
	    if (!m.matches()) return false;

	    String kod = m.group(1);
	    int trajanje = Integer.parseInt(m.group(2));
	    String ispravniKod = this.konfig.dajPostavku("kodZaAdminTvrtke");

	    if (!kod.equals(ispravniKod)) {
	        out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
	    } else {
	        try {
	            Thread.sleep(trajanje);
	            out.write("OK\n");
	        } catch (InterruptedException e) {
	            out.write("ERROR 16 - Prekid spavanja dretve\n");
	        }
	    }
	    out.flush();
	    return true;
	}

    /**
     * Obrada komande KRAJWS – šalje partnerima komandu za kraj rada.
     * Ako svi završe, zaustavlja i lokalni poslužitelj.
     */
	private boolean obradiKrajWS(String naredba, PrintWriter out) {
	    Matcher m = Pattern.compile("^KRAJWS\\s+(\\w+)$").matcher(naredba);
	    if (!m.matches()) return false;

	    String kod = m.group(1);
	    if (!kod.equals(this.kodZaKraj)) {
	        out.write("ERROR 10 - Neispravan kod za kraj\n");
	    } else {
	        boolean sviOK = true;
	        for (Partner p : this.partneri.values()) {
	            try (Socket s = new Socket(p.adresa(), p.mreznaVrataKraj())) {
	                PrintWriter outP = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "utf8"));
	                BufferedReader inP = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf8"));
	                outP.write("KRAJ " + kod + "\n");
	                outP.flush();
	                s.shutdownOutput();
	                String odgovor = inP.readLine();
	                if (!"OK".equals(odgovor)) sviOK = false;
	                s.shutdownInput();
	            } catch (IOException e) {
	                sviOK = false;
	            }
	        }
	        if (sviOK) {
	            out.write("OK\n");
	            this.kraj.set(true);
	        } else {
	            out.write("ERROR 14 - Barem jedan partner nije završio rad\n");
	        }
	    }
	    out.flush();
	    return true;
	}

        /**
     * Obrada komande OSVJEŽI – ponovno učitava kartu pića i jelovnike ako nije pauza.
     */
	private boolean obradiOsvjezi(String naredba, PrintWriter out) {
	    Matcher m = Pattern.compile("^OSVJEŽI\\s+(\\w+)$").matcher(naredba);
	    if (!m.matches()) return false;

	    String kod = m.group(1);
	    String ispravniKod = this.konfig.dajPostavku("kodZaAdminTvrtke");

	    if (!kod.equals(ispravniKod)) {
	        out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
	    } else if (this.pauzaPartneri.get()) {
	        out.write("ERROR 15 - Poslužitelj za partnere u pauzi\n");
	    } else {
	        boolean uspjeh1 = this.ucitajKartuPica();
	        boolean uspjeh2 = this.ucitajJelovnike();
	        if (uspjeh1 && uspjeh2) {
	            out.write("OK\n");
	        } else {
	            out.write("ERROR 17 - RESTful zahtjev nije uspješan\n");
	        }
	    }
	    out.flush();
	    return true;
	}

    /**
     * Obrada lokalne komande KRAJ – šalje kraj svim partnerima i zatvara REST servis.
     */
	private boolean obradiKrajLokalan(String naredba, PrintWriter out) {
	    Matcher m = Pattern.compile("^KRAJ\\s+([A-Za-z0-9]+)$").matcher(naredba);
	    if (!m.matches()) return false;

	    String kod = m.group(1);
	    if (!kod.equals(this.kodZaKraj)) {
	        out.write("ERROR 10 - Neispravan kod za kraj\n");
	        out.flush();
	        return true;
	    }

	    boolean sviOK = true;
	    for (Partner p : this.partneri.values()) {
	        try (Socket s = new Socket(p.adresa(), p.mreznaVrataKraj())) {
	            PrintWriter outP = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "utf8"));
	            BufferedReader inP = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf8"));
	            outP.write("KRAJ " + kod + "\n");
	            outP.flush();
	            s.shutdownOutput();
	            String odgovor = inP.readLine();
	            if (!"OK".equals(odgovor)) sviOK = false;
	            s.shutdownInput();
	        } catch (IOException e) {
	            sviOK = false;
	        }
	    }
	    if (!sviOK) {
	        out.write("ERROR 14 - Barem jedan partner nije završio rad\n");
	    } else {
	        try {
	            HttpClient client = HttpClient.newHttpClient();
	            HttpRequest req = HttpRequest.newBuilder()
	            		.uri(URI.create(restAdresa + "/kraj/info")) 
	            	      .method("HEAD", HttpRequest.BodyPublishers.noBody())
	            	      .build();
	            HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());
	            if (res.statusCode() == 200) {
	                out.write("OK\n");
	                this.kraj.set(true);
	            } else {
	                out.write("ERROR 17 - RESTful zahtjev nije uspješan\n");
	            }
	        } catch (Exception e) {
	            out.write("ERROR 17 - RESTful zahtjev nije uspješan\n");
	        }
	    }
	    out.flush();
	    return true;
	}

     /**
     * Pomoćna metoda koja vraća poruku o grešci preko socketa.
     */
	private void posaljiGresku(Socket s, String poruka) {
	    try (PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "utf8"))) {
	        out.write(poruka + "\n");
	        out.flush();
	    } catch (IOException ignored) {}
	}

     /**
     * Sigurno zatvara mrežnu utičnicu i izlazni tok.
     */
	private void zatvoriMreznuUticnicu(Socket s) {
	    try {
	        s.shutdownOutput();
	        s.close();
	    } catch (IOException ignored) {}
	}


  /**
   * Obradi registraciju.
   *
   * @param mreznaUticnica the mrezna uticnica
   */
  public void obradiRegistraciju(Socket mreznaUticnica) {
	  if (this.pauzaRegistracija.get()) {
		    try (PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {
		        out.write("ERROR 24 - Poslužitelj za registraciju partnera u pauzi\n");
		        out.flush();
		        mreznaUticnica.shutdownOutput();
		        mreznaUticnica.close();
		    } catch (IOException e) {}
		    return;
		}
	  
	  try (
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
        PrintWriter out =
            new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {
      String linija = in.readLine().trim();

      if (linija.startsWith("PARTNER")) {
        obradiNaredbuPartner(linija, out);
      } else if (linija.startsWith("OBRIŠI")) {
        obradiNaredbuObrisi(linija, out);
      } else if (linija.startsWith("POPIS")) {
        obradiNaredbuPopis(out);
      } else {
        out.write("ERROR 20 - Format komande nije ispravan\n");
      }
      out.flush();
    } catch (IOException e) {
    } finally {
      try {
        mreznaUticnica.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Obradi naredbu partner.
   *
   * @param linija the linija
   * @param out the out
   */
  private void obradiNaredbuPartner(String linija, PrintWriter out) {
	    try {
	        List<String> dijelovi = new ArrayList<>();
	        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(linija.trim());

	        while (m.find()) {
	            dijelovi.add(m.group(1).replaceAll("^\"|\"$", ""));
	        }


	        if (dijelovi.size() != 10) {
	            out.write("ERROR 20 - Format komande nije ispravan\n");
	            return;
	        }

	        String uneseniKod = dijelovi.get(9);
	        String ispravniKod = konfig.dajPostavku("kodZaAdminTvrtke");
	        


	        if (!uneseniKod.equals(ispravniKod)) {
	            out.write("ERROR 20 - Format komande nije ispravan\n");
	            return;
	        }

	        int id = Integer.parseInt(dijelovi.get(1));
	        if (this.partneri.containsKey(id)) {
	            out.write("ERROR 21 - Već postoji partner s id u kolekciji partnera\n");
	            return;
	        }

	        String naziv = dijelovi.get(2);
	        String vrstaKuhinje = dijelovi.get(3);
	        String adresa = dijelovi.get(4);
	        int mreznaVrata = Integer.parseInt(dijelovi.get(5));
	        float gpsSirina = Float.parseFloat(dijelovi.get(6));
	        float gpsDuzina = Float.parseFloat(dijelovi.get(7));
	        int mreznaVrataKraj = Integer.parseInt(dijelovi.get(8));

	        String kombinacija = naziv + adresa;
	        String sigurnosniKod = Integer.toHexString(kombinacija.hashCode());

	        Partner partner = new Partner(id, naziv, vrstaKuhinje, adresa,
	            mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, uneseniKod
	        );

	        this.partneri.put(id, partner);
	        spremiPartnere();
	        out.write("OK " + sigurnosniKod + "\n");
	    } catch (Exception e) {
	        out.write("ERROR 29 - Nešto drugo nije u redu\n");
	    }
	}

  /**
   * Spremi partnere.
   */
  private void spremiPartnere() {
    var nazivDatoteke = this.konfig.dajPostavku("datotekaPartnera");
    var datoteka = Path.of(nazivDatoteke);

    try (var writer = Files.newBufferedWriter(datoteka, StandardCharsets.UTF_8)) {
      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
      Partner[] nizPartnera = this.partneri.values().toArray(new Partner[0]);
      gson.toJson(nizPartnera, writer);
    } catch (IOException e) {
    }
  }

  /**
   * Obradi naredbu obrisi.
   *
   * @param linija the linija
   * @param out the out
   */
  private void obradiNaredbuObrisi(String linija, PrintWriter out) {
    String[] dijelovi = linija.split(" ");
    if (dijelovi.length != 3) {
      out.write("ERROR 20 - Format komande nije ispravan\n");
      return;
    }

    try {
      int id = Integer.parseInt(dijelovi[1]);
      String kod = dijelovi[2];
      Partner p = this.partneri.get(id);
      if (p == null) {
        out.write("ERROR 23 - Ne postoji partner s id u kolekciji partnera\n");
      } else if (!p.sigurnosniKod().equals(kod)) {
        out.write("ERROR 22 - Neispravan sigurnosni kod partnera\n");
      } else {
        this.partneri.remove(id);
        spremiPartnere();
        out.write("OK\n");
      }
    } catch (Exception e) {
      out.write("ERROR 29 - Nešto drugo nije u redu\n");
    }
  }

  /**
   * Obradi naredbu popis.
   *
   * @param out the out
   */
  private void obradiNaredbuPopis(PrintWriter out) {
    try {
      List<PartnerPopis> popis = this.partneri.values().stream().map(p -> new PartnerPopis(p.id(),
          p.naziv(), p.vrstaKuhinje(), p.adresa(), p.mreznaVrata(), p.gpsSirina(), p.gpsDuzina()))
          .toList();
      Gson gson = new GsonBuilder().disableHtmlEscaping().create();

      out.write("OK\n");

      String jsonPopis = gson.toJson(popis);
      out.write(jsonPopis + "\n");
      out.flush();
    } catch (Exception e) {
      out.write("ERROR 29 - Nešto drugo nije u redu\n");
    }
  }

  /**
   * Obradi rad.
   *
   * @param mreznaUticnica the mrezna uticnica
   */
  public void obradiRad(Socket mreznaUticnica) {
	  if (this.pauzaPartneri.get()) {
		    try (PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {
		        out.write("ERROR 15 - Poslužitelj za partnere u pauzi\n");
		        out.flush();
		        mreznaUticnica.shutdownOutput();
		        mreznaUticnica.close();
		    } catch (IOException e) {}
		    return;
		}

	  
	  try (
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
        PrintWriter out =
            new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {
      String linija = in.readLine();


      if (linija.startsWith("JELOVNIK")) {
        obradiNaredbuJelovnik(linija, out);
      } else if (linija.startsWith("KARTAPIĆA")) {
        obradiNaredbuKartaPica(linija, out);
      } else if (linija.startsWith("OBRAČUNWS")) {
    	obradiNaredbuObracunWS(linija, in, out);
      } else if (linija.startsWith("OBRAČUN")) {
        obradiNaredbuObracun(linija, in, out);
      } else {
        out.write("ERROR 30 - Format komande nije ispravan\n");
      }
      out.flush();
    } catch (IOException e) {
    } finally {
      try {
        mreznaUticnica.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Obradi naredbu jelovnik.
   *
   * @param linija the linija
   * @param out the out
   */
  private void obradiNaredbuJelovnik(String linija, PrintWriter out) {
    String[] dijelovi = linija.trim().split(" ");
    if (dijelovi.length != 3) {
      out.write("ERROR 30 - Format komande nije ispravan");
      return;
    }

    try {
      int id = Integer.parseInt(dijelovi[1]);
      String kod = dijelovi[2];
      Partner partner = this.partneri.get(id);

      if (partner == null || !partner.sigurnosniKod().equals(kod)) {
        out.write("ERROR 31 - Ne postoji partner s id u kolekciji "
            + "partnera i/ili neispravan sigurnoski kod partnera\n");
      } else {
        String vrstaKuhinje = partner.vrstaKuhinje();
        Map<String, Jelovnik> jelovnikZaKuhinju = this.jelovnici.get(vrstaKuhinje);
        if (jelovnikZaKuhinju == null) {
          out.write(
              "ERROR 32 - Ne postoji jelovnik s vrstom kuhinje " + "koju partner ima ugovorenu\n");
        } else if (jelovnikZaKuhinju.isEmpty()) {
          out.write("ERROR 33 - Neispravan jelovnik\n");
        } else {
          Gson gson = new GsonBuilder().disableHtmlEscaping().create();
          out.write("OK\n");

          String jsonJelovnik = gson.toJson(jelovnikZaKuhinju.values());
          out.write(jsonJelovnik + "\n");
        }
      }
    } catch (Exception e) {
      out.write("ERROR 39 - Nešto drugo nije u redu\n");
    }
  }

  /**
   * Obradi naredbu karta pica.
   *
   * @param linija the linija
   * @param out the out
   */
  private void obradiNaredbuKartaPica(String linija, PrintWriter out) {
	    String[] dijelovi = linija.trim().split("\\s+");
	    if (dijelovi.length != 3) {
	        out.println("ERROR 30 - Format komande nije ispravan");
	        return;
	    }
	    try {
	        int id = Integer.parseInt(dijelovi[1]);
	        String kod = dijelovi[2];
	        Partner partner = this.partneri.get(id);

	        if (partner == null || !partner.sigurnosniKod().equals(kod)) {
	            out.println("ERROR 31 - Ne postoji partner s id u kolekciji "
	                        + "partnera i/ili neispravan sigurnosni kod");
	            return;
	        }
	        if (this.kartaPica.isEmpty()) {
	            out.println("ERROR 34 - Neispravna karta pića");
	            return;
	        }
	        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	        String json = gson.toJson(this.kartaPica.values());
	        out.println("OK");
	        out.println(json);
	    } catch (Exception e) {
	        out.println("ERROR 39 - Nešto drugo nije u redu");
	    }
	}


  /**
   * Obradi naredbu obracun.
   *
   * @param linija the linija
   * @param in the in
   * @param out the out
   */
  private void obradiNaredbuObracun(String linija, BufferedReader in, PrintWriter out) {
    String[] dijelovi = linija.trim().split(" ");
    if (dijelovi.length != 3) {
      out.write("ERROR 30 - Format komande nije ispravan\n");
      out.flush();
      return;
    }
    try {
      int id = Integer.parseInt(dijelovi[1]);
      String kod = dijelovi[2];
      Partner partner = this.partneri.get(id);
      if (partner == null || !partner.sigurnosniKod().equals(kod)) {
        out.write("ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili "
            + "neispravan sigurnosni kod partnera\n");
        out.flush();
        return;
      }
      StringBuilder jsonBuilder = new StringBuilder();
      String red;
      while ((red = in.readLine()) != null) {
        jsonBuilder.append(red.trim());
        if (red.trim().endsWith("]"))
          break;
      }
      Gson gson = new Gson();
      Obracun[] noviObracuni = gson.fromJson(jsonBuilder.toString(), Obracun[].class);
      String datotekaObracuna = this.konfig.dajPostavku("datotekaObracuna");
      Path datoteka = Path.of(datotekaObracuna);
      List<Obracun> sviObracuni = new java.util.ArrayList<>();
      if (!Files.exists(datoteka)) {
        Files.createFile(datoteka);
        Files.writeString(datoteka, "[]", StandardCharsets.UTF_8);
      }
      try (var reader = Files.newBufferedReader(datoteka, StandardCharsets.UTF_8)) {
        Obracun[] postojeci = gson.fromJson(reader, Obracun[].class);
        if (postojeci != null)
          sviObracuni.addAll(Arrays.asList(postojeci));
      }
      sviObracuni.addAll(Arrays.asList(noviObracuni));
      try (var writer = Files.newBufferedWriter(datoteka, StandardCharsets.UTF_8)) {
        gson.toJson(sviObracuni, writer);
      }
      try {
    	    HttpClient client = HttpClient.newHttpClient();
    	    HttpRequest request = HttpRequest.newBuilder()
    	        .uri(URI.create("http://localhost:8080/api/tvrtka/obracun"))
    	        .header("Content-Type", "application/json")
    	        .POST(HttpRequest.BodyPublishers.ofString(jsonBuilder.toString()))
    	        .build();

    	    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    	    if (response.statusCode() != 200) {
    	        out.write("ERROR 37 - RESTful zahtjev nije uspješan\n");
    	        out.flush();
    	        return;
    	    }
    	} catch (Exception e) {
    	    out.write("ERROR 37 - RESTful zahtjev nije uspješan\n");
    	    out.flush();
    	    return;
    	}
      out.write("OK\n");
      out.flush();
    } catch (Exception e) {
      out.write("ERROR 35 - Neispravan obračun\n");
      out.flush();
    }
  }
  
  private void obradiNaredbuObracunWS(String linija, BufferedReader in, PrintWriter out) {
	    String[] dijelovi = linija.trim().split(" ");
	    if (dijelovi.length != 3) {
	        out.write("ERROR 30 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }
	    try {
	        int id = Integer.parseInt(dijelovi[1]);
	        String kod = dijelovi[2];
	        Partner partner = this.partneri.get(id);

	        if (partner == null || !partner.sigurnosniKod().equals(kod)) {
	            out.write("ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
	            out.flush();
	            return;
	        }
	        StringBuilder jsonBuilder = new StringBuilder();
	        String red;
	        while ((red = in.readLine()) != null) {
	            jsonBuilder.append(red.trim());
	            if (red.trim().endsWith("]"))
	                break;
	        }
	        Gson gson = new Gson();
	        Obracun[] noviObracuni = gson.fromJson(jsonBuilder.toString(), Obracun[].class);
	        String datotekaObracuna = this.konfig.dajPostavku("datotekaObracuna");
	        Path datoteka = Path.of(datotekaObracuna);
	        List<Obracun> sviObracuni = new ArrayList<>();
	        if (!Files.exists(datoteka)) {
	            Files.createFile(datoteka);
	            Files.writeString(datoteka, "[]", StandardCharsets.UTF_8);
	        }
	        try (var reader = Files.newBufferedReader(datoteka, StandardCharsets.UTF_8)) {
	            Obracun[] postojeci = gson.fromJson(reader, Obracun[].class);
	            if (postojeci != null)
	                sviObracuni.addAll(Arrays.asList(postojeci));
	        }
	        sviObracuni.addAll(Arrays.asList(noviObracuni));
	        try (var writer = Files.newBufferedWriter(datoteka, StandardCharsets.UTF_8)) {
	            gson.toJson(sviObracuni, writer);
	        }

	        out.write("OK\n");
	        out.flush();
	    } catch (Exception e) {
	        out.write("ERROR 35 - Neispravan obračun\n");
	        out.flush();
	    }
	}


  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke the naziv datoteke
   * @return true, if successful
   */
  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
    }
    return false;
  }

  /**
   * Gets the konfig.
   *
   * @return the konfig
   */
  public Konfiguracija getKonfig() {
    return this.konfig;
  }

  /**
   * Ucitaj kartu pica.
   *
   * @return true, if successful
   */
  public boolean ucitajKartuPica() {
	    Path rootDir   = Path.of(".").toAbsolutePath();        
	    Path datoteka  = rootDir.resolve(konfig.dajPostavku("datotekaKartaPica"));
	    if (!Files.isRegularFile(datoteka) || !Files.isReadable(datoteka))
	        return false;
	    this.kartaPica.clear();
	    try (var br = Files.newBufferedReader(datoteka)) {
	        KartaPica[] niz = new Gson().fromJson(br, KartaPica[].class);
	        for (KartaPica kp : niz) {
	            this.kartaPica.put(kp.id(), kp);
	        }
	        return !this.kartaPica.isEmpty();                    
	    } catch (Exception ex) {
	        return false;
	    }
	}

  /**
   * Gets the karta pica.
   *
   * @return the karta pica
   */
  public Map<String, KartaPica> getKartaPica() {
    return this.kartaPica;
  }

  /**
   * Ucitaj partnere.
   *
   * @return true, if successful
   */
  public boolean ucitajPartnere() {
    var nazivDatoteke = this.konfig.dajPostavku("datotekaPartnera");
    Path datoteka = Path.of(nazivDatoteke);
    try {
      if (!Files.exists(datoteka)) {
        Files.createFile(datoteka);
        Files.writeString(datoteka, "[]", StandardCharsets.UTF_8);
      }

      if (!Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
        return false;
      }

      try (var reader = Files.newBufferedReader(datoteka, StandardCharsets.UTF_8)) {
        Gson gson = new Gson();
        var nizPartnera = gson.fromJson(reader, Partner[].class);
        if (nizPartnera != null) {
          Arrays.stream(nizPartnera).forEach(p -> this.partneri.put(p.id(), p));
        }
      }

    } catch (IOException e) {
      return false;
    }

    return true;
  }

  /**
   * Ucitaj kuhinju.
   *
   * @return true, if successful
   */
  public boolean ucitajKuhinju() {
    for (String kljuc : this.konfig.dajSvePostavke().stringPropertyNames()) {
      if (kljuc.startsWith("kuhinja_")) {
        String vrijednost = this.konfig.dajPostavku(kljuc);
        String[] dijelovi = vrijednost.split(";");
        if (dijelovi.length == 2) {
          this.kuhinje.put(dijelovi[0], dijelovi[1]);
        }
      }
    }
    return !this.kuhinje.isEmpty();
  }

  /**
   * Ucitaj jelovnike.
   *
   * @return true, if successful
   */
  public boolean ucitajJelovnike() {
      Path rootDir = Path.of(".").toAbsolutePath();     
      this.jelovnici.clear();          
      this.kuhinje.clear();
      for (String kljuc : this.konfig.dajSvePostavke().stringPropertyNames()) {
          if (!kljuc.startsWith("kuhinja_"))
              continue;                               
          String[] polja = this.konfig.dajPostavku(kljuc).split(";", 2);
          if (polja.length != 2)                    
              continue;
          String oznaka = polja[0].trim();            
          String naziv  = polja[1].trim();
          this.kuhinje.put(oznaka, naziv);
          Path datoteka = rootDir.resolve(kljuc + ".json");  
          if (!Files.exists(datoteka)) {
              continue;
          }
          try (var br = Files.newBufferedReader(datoteka, java.nio.charset.StandardCharsets.UTF_8)) {
              Jelovnik[] jela = new com.google.gson.Gson().fromJson(br, Jelovnik[].class);
              if (jela == null || jela.length == 0) {
                  continue;
              }
              var mapa = new java.util.concurrent.ConcurrentHashMap<String, Jelovnik>();
              for (Jelovnik j : jela) {
                  mapa.put(j.id(), j);
              }
              this.jelovnici.put(oznaka, mapa);
          } catch (Exception ex) {
          }
      }
      return !this.jelovnici.isEmpty();
  }

}
