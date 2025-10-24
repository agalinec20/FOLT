package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.podaci.Obracun;


/**
 * The Class PosluziteljPartner.
 */
public class PosluziteljPartner {

  /** Konfiguracijski podaci. */
  private Konfiguracija konfig;

  /** Predložak za kraj. */
  private Pattern predlozakKraj = Pattern.compile("^KRAJ$");

  /** Jelovnici. */
  private Map<String, Map<String, Jelovnik>> jelovnici = new ConcurrentHashMap<>();

  /** Karta pića. */
  private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

  /** Otvorene narudžbe. */
  private Map<String, List<Narudzba>> narudzbeOtvorene = new ConcurrentHashMap<>();

  /** Zatvorene narudžbe. */
  private Queue<Narudzba> narudzbePlacene = new ConcurrentLinkedQueue<>();

  /** Izvršitelji. */
  private ExecutorService izvrsitelji = Executors.newVirtualThreadPerTaskExecutor();

  /** Broj narudžbi. */
  private AtomicInteger brojNarudzbi = new AtomicInteger(0);

  /** The lock. */
  private final Object lock = new Object();
  
  private AtomicBoolean pauzaPartner = new AtomicBoolean(false);
  private AtomicBoolean pauzaKupci = new AtomicBoolean(false);
  private AtomicBoolean kraj = new AtomicBoolean(false);


  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {

	    if (args.length < 1 || args.length > 2) {
	        return;
	    }

	    var program = new PosluziteljPartner();
	    var nazivDatoteke = args[0];


	    if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
	        return;
	    }

	    if (args.length == 1) {
	        program.posaljiRegistraciju();
	    } else {
	        switch (args[1]) {
	            case "KRAJ" -> {
	                program.posaljiKraj();
	            }
	            case "PARTNER" -> {
	                var dretvaKupci =
	                    program.izvrsitelji.submit(() -> program.pokreniPartnerPosluzitelj());

	                var dretvaKrajPartner =
	                    program.izvrsitelji.submit(() -> program.pokreniPosluziteljKrajPartner());

	                while (!program.kraj.get()) {
	                    try {
	                        Thread.sleep(100);          
	                    } catch (InterruptedException e) {
	                        Thread.currentThread().interrupt();   
	                    }
	                }
	            }
	        }
	    }
  }



  /**
   * Posalji kraj.
   */
  private void posaljiKraj() {
    var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write("KRAJ " + kodZaKraj + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
    } catch (IOException e) {
    }
  }

  /**
   * Posalji registraciju.
   */
  private void posaljiRegistraciju() {
    try {
      var adresa = konfig.dajPostavku("adresa");
      var port = Integer.parseInt(konfig.dajPostavku("mreznaVrataRegistracija"));

      var id = Integer.parseInt(konfig.dajPostavku("id"));
      var naziv = konfig.dajPostavku("naziv");
      var vrstaKuhinje = konfig.dajPostavku("kuhinja");
      var adresaPartnera = adresa;
      var mreznaVrata = Integer.parseInt(konfig.dajPostavku("mreznaVrata"));
      var gpsSirina = Float.parseFloat(konfig.dajPostavku("gpsSirina"));
      var gpsDuzina = Float.parseFloat(konfig.dajPostavku("gpsDuzina"));
      var mreznaVrataKraj = Integer.parseInt(konfig.dajPostavku("mreznaVrataKrajPartner"));
      var adminKod = konfig.dajPostavku("kodZaAdmin");


      var naredba = String.format(
    		    "PARTNER %d \"%s\" %s %s %d %.5f %.5f %d %s\n",
    		    id, naziv, vrstaKuhinje, adresaPartnera, mreznaVrata,
    		    gpsSirina, gpsDuzina, mreznaVrataKraj, adminKod
    		);

      try (var socket = new Socket(adresa, port);
          var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
          var out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"))) {

        out.println(naredba.trim());
        out.flush();
        socket.shutdownOutput();

        var odgovor = in.readLine();
        socket.shutdownInput();

        if (odgovor.startsWith("OK")) {
          var sigKod = odgovor.split(" ")[1];
          konfig.spremiPostavku("sigKod", sigKod);
          konfig.spremiKonfiguraciju();
        }
      }
    } catch (Exception e) {
    }
  }

  /**
   * Pokreni partner posluzitelj.
   */
  private void pokreniPartnerPosluzitelj() {

	    try {
	        var id = konfig.dajPostavku("id");
	        var sigurnosniKod = konfig.dajPostavku("sigKod");
	        var mreznaVrata = Integer.parseInt(konfig.dajPostavku("mreznaVrata"));
	        var pauzaDretve = Integer.parseInt(konfig.dajPostavku("pauzaDretve"));
	        if (!ucitajJelovnik(id, sigurnosniKod)) {
	            return;
	        }

	        if (!ucitajKartuPica(id, sigurnosniKod)) {
	            return;
	        }

	        var serverSocket = new ServerSocket(mreznaVrata);

	        while (true) {
	            var socket = serverSocket.accept();
	            izvrsitelji.submit(() -> obradiZahtjev(socket));
	            Thread.sleep(pauzaDretve);
	        }
	    } catch (Exception e) {
	    }
	}
  
  private void pokreniPosluziteljKrajPartner() {
	  int port = Integer.parseInt(konfig.dajPostavku("mreznaVrataKrajPartner"));
	    try (ServerSocket ss = new ServerSocket(port)) {
	        while (!this.kraj.get()) {
	            Socket socket = ss.accept();
	            izvrsitelji.submit(() -> obradiZahtjevKraj(socket));
	        }
	    } catch (IOException e) {
	    }
	}
  
    /**
     * Obrađuje zahtjev koji dolazi na port za kraj rada.
     * Prepoznaje i delegira komande poput KRAJ, STATUS, PAUZA, START, SPAVA, OSVJEŽI.
     */
      private void obradiZahtjevKraj(Socket socket) {
	        try (
	            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	            var out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"))
	        ) {
	            var linija = in.readLine();
	            if (linija == null || linija.isBlank()) {
	                out.write("ERROR 60 - Format komande nije ispravan\n");
	                out.flush();
	                return;
	            }
	            var dijelovi = linija.trim().split(" ");
	            var komanda = dijelovi[0];
	            switch (komanda) {
	                case "KRAJ" -> obradiKraj(dijelovi, out);
	                case "STATUS" -> obradiStatus(dijelovi, out);
	                case "PAUZA" -> obradiPauza(dijelovi, out);
	                case "START" -> obradiStart(dijelovi, out);
	                case "SPAVA" -> obradiSpava(dijelovi, out);
	                case "OSVJEŽI" -> obradiOsvjezi(dijelovi, out);
	                default -> {
	                    out.write("ERROR 60 - Format komande nije ispravan\n");
	                    out.flush();
	                }
	            }
	        } catch (IOException e) {
	        }
	    }

    /**
     * Obrada komande KRAJ. Ako je kod ispravan, postavlja zastavicu zaustavljanja.
     */
  private void obradiKraj(String[] args, PrintWriter out) {
	    if (args.length != 2 || !args[1].equals(konfig.dajPostavku("kodZaKraj"))) {
	        out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
	    } else {
	        out.write("OK\n");
	        kraj.set(true);
	    }
	    out.flush();
	}

    /**
     * Obrada komande STATUS. Vraća status (pauza ili nije) ako je kod ispravan.
     */
  private void obradiStatus(String[] args, PrintWriter out) {
	    if (args.length != 3 || !args[2].equals("1")) {
	        out.write("ERROR 60 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }
	    if (!args[1].equals(konfig.dajPostavku("kodZaAdmin"))) {
	        out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
	        out.flush();
	        return;
	    }
	    int status = pauzaKupci.get() ? 0 : 1;
	    out.write("OK " + status + "\n");
	    out.flush();
	}

    /**
     * Obrada komande PAUZA. Postavlja poslužitelj u stanje pauze ako je moguće.
     */
  private void obradiPauza(String[] args, PrintWriter out) {
	    if (args.length != 3 || !args[2].equals("1")) {
	        out.write("ERROR 60 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }
	    if (!args[1].equals(konfig.dajPostavku("kodZaAdmin"))) {
	        out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
	        out.flush();
	        return;
	    }
	    if (pauzaKupci.get()) {
	        out.write("ERROR 62 - Pogrešna promjena pauze\n");
	        out.flush();
	        return;
	    }
	    pauzaKupci.set(true);
	    out.write("OK\n");
	    out.flush();
	}

    /**
     * Obrada komande START. Vraća poslužitelj iz pauze ako je trenutno u pauzi.
     */
  private void obradiStart(String[] args, PrintWriter out) {
	    if (args.length != 3 || !args[2].equals("1")) {
	        out.write("ERROR 60 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }
	    if (!args[1].equals(konfig.dajPostavku("kodZaAdmin"))) {
	        out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
	        out.flush();
	        return;
	    }
	    if (!pauzaKupci.get()) {
	        out.write("ERROR 62 - Pogrešna promjena starta\n");
	        out.flush();
	        return;
	    }
	    pauzaKupci.set(false);
	    out.write("OK\n");
	    out.flush();
	}

    /**
     * Obrada komande SPAVA. Uspava dretvu na zadano vrijeme ako je kod ispravan.
     */
  private void obradiSpava(String[] args, PrintWriter out) {
	    if (args.length != 3) {
	        out.write("ERROR 60 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }
	    if (!args[1].equals(konfig.dajPostavku("kodZaAdmin"))) {
	        out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
	        out.flush();
	        return;
	    }
	    try {
	        int trajanje = Integer.parseInt(args[2]);
	        Thread.sleep(trajanje);
	        out.write("OK\n");
	    } catch (Exception e) {
	        out.write("ERROR 63 - Prekid spavanja dretve\n");
	    }
	    out.flush();
	}

    /**
     * Obrada komande OSVJEŽI. Dohvaća nove podatke (jelovnik i pića) s REST servisa.
     */
  private void obradiOsvjezi(String[] args, PrintWriter out) {
	    if (args.length != 2) {
	        out.write("ERROR 60 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }
	    if (!args[1].equals(konfig.dajPostavku("kodZaAdmin"))) {
	        out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
	        out.flush();
	        return;
	    }
	    if (pauzaKupci.get()) {
	        out.write("ERROR 62 - Poslužitelj je u pauzi\n");
	        out.flush();
	        return;
	    }
	    var id = konfig.dajPostavku("id");
	    var sigKod = konfig.dajPostavku("sigKod");
	    boolean jelovnik = ucitajJelovnik(id, sigKod);
	    boolean pica = ucitajKartuPica(id, sigKod);
	    if (jelovnik && pica) {
	        out.write("OK\n");
	    } else {
	        out.write("ERROR 47 - Neuspješno preuzimanje podataka\n");
	    }
	    out.flush();
	}


  /**
   * Ucitaj jelovnik.
   *
   * @param id the id
   * @param sigurnosniKod the sigurnosni kod
   * @return true, if successful
   */
  private boolean ucitajJelovnik(String id, String sigurnosniKod) {

	    try {
	        var adresa = konfig.dajPostavku("adresa");
	        var vrata = Integer.parseInt(konfig.dajPostavku("mreznaVrataRad"));

	        var socket = new Socket(adresa, vrata);
	        var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	        var out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

	        String komanda = "JELOVNIK " + id + " " + sigurnosniKod;
	        out.write(komanda + "\n");
	        out.flush();
	        socket.shutdownOutput();

	        var odgovor = in.readLine();

	        if (!odgovor.equals("OK")) {
	            return false;
	        }

	        var json = in.readLine();

	        if (json == null || !json.startsWith("[")) {
	            return false;
	        }

	        Jelovnik[] jelovnikNiz = new Gson().fromJson(json, Jelovnik[].class);

	        if (jelovnikNiz == null || jelovnikNiz.length == 0) {
	            return false;
	        }

	        Map<String, Jelovnik> mapaJelovnika = new HashMap<>();
	        for (Jelovnik j : jelovnikNiz) {
	            mapaJelovnika.put(j.id(), j);
	        }

	        var vrstaKuhinje = konfig.dajPostavku("kuhinja");
	        jelovnici.put(vrstaKuhinje, mapaJelovnika);

	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}


  /**
   * Ucitaj kartu pica.
   *
   * @param id the id
   * @param sigurnosniKod the sigurnosni kod
   * @return true, if successful
   */
  private boolean ucitajKartuPica(String id, String sigurnosniKod) {
    try (
        var socket = new Socket(konfig.dajPostavku("adresa"),
            Integer.parseInt(konfig.dajPostavku("mreznaVrataRad")));
        var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
        var out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"))) {

      out.write("KARTAPIĆA " + id + " " + sigurnosniKod + "\n");
      out.flush();
      socket.shutdownOutput();

      var odgovor = in.readLine();
      if (!odgovor.equals("OK"))
        return false;

      var json = in.readLine();
      KartaPica[] kartaPicaNiz = new Gson().fromJson(json, KartaPica[].class);

      for (KartaPica p : kartaPicaNiz) {
        kartaPica.put(p.id(), p);
      }

      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  private void obradiZahtjev(Socket socket) {
	    try (
	        var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	        var out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"), true)
	    ) {
	        var linija = in.readLine();
	        if (linija == null || linija.isBlank()) {
	            out.println("ERROR 40 - Format komande nije ispravan");
	            out.flush();
	            return;
	        }

	        var dijelovi = linija.trim().split(" ");
	        var komanda = dijelovi[0];

	        switch (komanda) {
	            case "JELOVNIK" -> obradiJelovnik(dijelovi, out);
	            case "KARTAPIĆA" -> obradiKartaPica(dijelovi, out);
	            case "NARUDŽBA" -> obradiNarudzba(dijelovi, out);
	            case "JELO" -> obradiJelo(dijelovi, out);
	            case "PIĆE" -> obradiPice(dijelovi, out);
	            case "RAČUN" -> obradiRacun(dijelovi, out);
	            case "STANJE" -> obradiStanje(dijelovi, out);
	            default -> {
	                out.println("ERROR 40 - Format komande nije ispravan");
	                out.flush();
	            }
	        }
	    } catch (IOException e) {
	    }
	}

 

  /**
   * Obradi jelovnik.
   *
   * @param args the args
   * @param out the out
   */
  private void obradiJelovnik(String[] args, PrintWriter out) {
	    if (args.length != 2) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    var vrstaKuhinje = konfig.dajPostavku("kuhinja");

	    if (!jelovnici.containsKey(vrstaKuhinje)) {
	        out.write("ERROR 46 - Neuspješno preuzimanje jelovnika\n");
	        out.flush();
	        return;
	    }

	    var kolekcija = jelovnici.get(vrstaKuhinje);
	    if (kolekcija == null || kolekcija.isEmpty()) {
	        out.write("ERROR 46 - Neuspješno preuzimanje jelovnika\n");
	        out.flush();
	        return;
	    }

	    out.write("OK\n");
	    out.write(new Gson().toJson(kolekcija.values()) + "\n");
	    out.flush();
	}



  /**
   * Obradi karta pica.
   *
   * @param args the args
   * @param out the out
   */
  private void obradiKartaPica(String[] args, PrintWriter out) {
	    if (args.length != 2) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    if (kartaPica.isEmpty()) {
	        out.write("ERROR 47 - Neuspješno preuzimanje karte pića\n");
	        out.flush();
	        return;
	    }

	    out.write("OK\n");
	    out.write(new Gson().toJson(kartaPica.values()) + "\n");
	    out.flush();
	}


  /**
   * Obradi narudzba.
   *
   * @param args the args
   * @param out the out
   */
  private void obradiNarudzba(String[] args, PrintWriter out) {
	    if (args.length != 2) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    var korisnik = args[1];

	    synchronized (lock) {
	        if (narudzbeOtvorene.containsKey(korisnik)) {
	            out.write("ERROR 44 - Već postoji otvorena narudžba za korisnika\n");
	            out.flush();
	            return;
	        }

	        var novaNarudzba = new Narudzba(korisnik, "", true, 0.0f, 0.0f, System.currentTimeMillis());
	        List<Narudzba> stavke = new ArrayList<>();
	        stavke.add(novaNarudzba);
	        narudzbeOtvorene.put(korisnik, stavke);
	    }

	    out.write("OK\n");
	    out.flush();
	}


  /**
   * Obradi jelo.
   *
   * @param args the args
   * @param out the out
   */
  private void obradiJelo(String[] args, PrintWriter out) {
	    if (args.length != 4) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    var korisnik = args[1];
	    var idJela = args[2];
	    float kolicina;

	    try {
	        kolicina = Float.parseFloat(args[3]);
	    } catch (NumberFormatException e) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    synchronized (lock) {
	        if (!narudzbeOtvorene.containsKey(korisnik)) {
	            out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika\n");
	            out.flush();
	            return;
	        }

	        var vrstaKuhinje = konfig.dajPostavku("kuhinja");
	        var mapaJela = jelovnici.get(vrstaKuhinje);
	        if (mapaJela == null || !mapaJela.containsKey(idJela)) {
	            out.write("ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera\n");
	            out.flush();
	            return;
	        }

	        var jelo = mapaJela.get(idJela);
	        var novaStavka = new Narudzba(korisnik, idJela, true, kolicina, jelo.cijena(), System.currentTimeMillis());
	        narudzbeOtvorene.get(korisnik).add(novaStavka);
	    }

	    out.write("OK\n");
	    out.flush();
	}

  /**
   * Obradi pice.
   *
   * @param args the args
   * @param out the out
   */
  private void obradiPice(String[] args, PrintWriter out) {
	    if (args.length != 4) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    var korisnik = args[1];
	    var idPica = args[2];
	    float kolicina;

	    try {
	        kolicina = Float.parseFloat(args[3]);
	    } catch (NumberFormatException e) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    synchronized (lock) {
	        if (!narudzbeOtvorene.containsKey(korisnik)) {
	            out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika\n");
	            out.flush();
	            return;
	        }

	        if (!kartaPica.containsKey(idPica)) {
	            out.write("ERROR 42 - Ne postoji piće s id u kolekciji karte pića kod partnera\n");
	            out.flush();
	            return;
	        }

	        var pice = kartaPica.get(idPica);
	        var novaStavka = new Narudzba(korisnik, idPica, false, kolicina, pice.cijena(), System.currentTimeMillis());
	        narudzbeOtvorene.get(korisnik).add(novaStavka);
	    }

	    out.write("OK\n");
	    out.flush();
	}


  /**
   * Obradi racun.
   *
   * @param args the args
   * @param out the out
   */
  private void obradiRacun(String[] args, PrintWriter out) {
	    if (args.length != 2) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    var korisnik = args[1];
	    List<Obracun> obracuni = new ArrayList<>();

	    synchronized (lock) {
	        if (!narudzbeOtvorene.containsKey(korisnik)) {
	            out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika\n");
	            out.flush();
	            return;
	        }

	        var stavke = narudzbeOtvorene.remove(korisnik);
	        narudzbePlacene.addAll(stavke);
	        var broj = brojNarudzbi.incrementAndGet();

	        if (broj % Integer.parseInt(konfig.dajPostavku("kvotaNarudzbi")) == 0) {
	            for (Narudzba nar : narudzbePlacene) {
	                int partnerId = Integer.parseInt(konfig.dajPostavku("id"));
	                var obr = new Obracun(partnerId, nar.id(), nar.jelo(), nar.kolicina(), nar.cijena(), nar.vrijeme());
	                obracuni.add(obr);
	            }

	            narudzbePlacene.clear();
	        }
	    }

	    if (!obracuni.isEmpty()) {
	        try (
	            var socket = new Socket(konfig.dajPostavku("adresa"),
	                    Integer.parseInt(konfig.dajPostavku("mreznaVrataRad")));
	            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	            var outTvrtka = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"), true)
	        ) {
	            var id = konfig.dajPostavku("id");
	            var sigKod = konfig.dajPostavku("sigKod");

	            outTvrtka.println("OBRAČUN " + id + " " + sigKod);
	            outTvrtka.println(new Gson().toJson(obracuni));

	            var odgovor = in.readLine();
	            if (!"OK".equals(odgovor)) {
	                out.write("ERROR 45 - Neuspješno slanje obračuna\n");
	                out.flush();
	                return;
	            }
	        } catch (IOException e) {
	            out.write("ERROR 45 - Neuspješno slanje obračuna\n");
	            out.flush();
	            return;
	        }
	    }

	    out.write("OK\n");
	    out.flush();
	}

  
  private void obradiStanje(String[] args, PrintWriter out) {
	    if (args.length != 2) {
	        out.write("ERROR 40 - Format komande nije ispravan\n");
	        out.flush();
	        return;
	    }

	    if (pauzaKupci.get()) {
	        out.write("ERROR 48 - Poslužitelj za prijem zahtjeva kupaca u pauzi\n");
	        out.flush();
	        return;
	    }

	    var korisnik = args[1];

	    synchronized (lock) {
	        if (!narudzbeOtvorene.containsKey(korisnik)) {
	            out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika\n");
	            out.flush();
	            return;
	        }

	        var stavke = narudzbeOtvorene.get(korisnik);
	        out.write("OK\n");
	        out.write(new Gson().toJson(stavke) + "\n");
	        out.flush();
	    }
	}

  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke the naziv datoteke
   * @return true, if successful
   */
  private boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
    }
    return false;
  }
}
