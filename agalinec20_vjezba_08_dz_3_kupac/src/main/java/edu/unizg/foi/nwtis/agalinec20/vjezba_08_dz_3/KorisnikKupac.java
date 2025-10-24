package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klasa KorisnikKupac.
 */
public class KorisnikKupac {

    /** The konfig. */
    private Konfiguracija konfig;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            return;
        }

        var program = new KorisnikKupac();
        if (!program.ucitajKonfiguraciju(args[0])) {
            return;
        }

        program.obradiKomande(args[1]);
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
        } catch (NeispravnaKonfiguracija ex) {}
        return false;
    }
   
    /**
     * Obradi komande.
     *
     * @param csvDatoteka the csv datoteka
     */
    private void obradiKomande(String csvDatoteka) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvDatoteka))) {
            String linija;

            while ((linija = reader.readLine()) != null) {
                if (linija.isBlank()) continue;

                String[] dijelovi = linija.split(";");
                if (dijelovi.length != 5) {
                    continue;
                }

                String korisnik = dijelovi[0];
                String adresa = dijelovi[1];
                int mreznaVrata = Integer.parseInt(dijelovi[2]);
                int spavanje = Integer.parseInt(dijelovi[3]);
                String komanda = dijelovi[4];

                System.out.println(korisnik + ";" + adresa + ";" + 
                mreznaVrata + ";" + spavanje + ";" + komanda);

                try {
                    Thread.sleep(spavanje);  
                } catch (InterruptedException e) {}

                try (Socket socket = new Socket(adresa, mreznaVrata);
                     BufferedReader in = new BufferedReader
                    		 (new InputStreamReader(socket.getInputStream(), "utf8"));
                     PrintWriter out = new PrintWriter
                    		 (new OutputStreamWriter(socket.getOutputStream(), "utf8"), true)) {

                    out.println(komanda);

                    while (in.readLine() != null) {}

                } catch (IOException e) {}
            }
        } catch (IOException e) {}
    }
}
