package edu.unizg.foi.nwtis.agalinec20.vjezba_08_dz_3.dao;

import edu.unizg.foi.nwtis.podaci.Obracun;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObracunDAO {
  private final Connection vezaBP;

  public ObracunDAO(Connection vezaBP) {
    this.vezaBP = vezaBP;
  }

  public List<Obracun> dohvatiSve(Long od, Long kraj) {
    String sql = "SELECT * FROM obracuni WHERE 1=1";
    if (od != null) sql += " AND vrijeme >= ?";
    if (kraj != null) sql += " AND vrijeme <= ?";

    List<Obracun> lista = new ArrayList<>();
    try (PreparedStatement ps = vezaBP.prepareStatement(sql)) {
      int i = 1;
      if (od != null) ps.setLong(i++, od);
      if (kraj != null) ps.setLong(i, kraj);

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        lista.add(kreirajObracun(rs));
      }
    } catch (Exception ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return lista;
  }

  public List<Obracun> dohvatiPoTipu(boolean tipJelo, Long od, Long kraj) {
    String sql = "SELECT * FROM obracuni WHERE jelo = ?";
    if (od != null) sql += " AND vrijeme >= ?";
    if (kraj != null) sql += " AND vrijeme <= ?";

    List<Obracun> lista = new ArrayList<>();
    try (PreparedStatement ps = vezaBP.prepareStatement(sql)) {
      int i = 1;
      ps.setBoolean(i++, tipJelo);
      if (od != null) ps.setLong(i++, od);
      if (kraj != null) ps.setLong(i++, kraj);

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        lista.add(kreirajObracun(rs));
      }
    } catch (Exception ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return lista;
  }

  public List<Obracun> dohvatiPoPartneru(int idPartner, Long od, Long kraj) {
    String sql = "SELECT * FROM obracuni WHERE partner = ?";
    if (od != null) sql += " AND vrijeme >= ?";
    if (kraj != null) sql += " AND vrijeme <= ?";

    List<Obracun> lista = new ArrayList<>();
    try (PreparedStatement ps = vezaBP.prepareStatement(sql)) {
      ps.setInt(1, idPartner);
      int i = 2;
      if (od != null) ps.setLong(i++, od);
      if (kraj != null) ps.setLong(i, kraj);

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        lista.add(kreirajObracun(rs));
      }
    } catch (Exception ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return lista;
  }

  public boolean dodaj(Obracun o) {
	  if (dohvati(o.id(), o.partner()) != null) {
		    return false;
		}

    String sql = "INSERT INTO obracuni (partner, id, jelo, kolicina, cijena, vrijeme) "
               + "VALUES (?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = vezaBP.prepareStatement(sql)) {
      ps.setInt(1, o.partner());
      ps.setString(2, o.id());
      ps.setBoolean(3, o.jelo());
      ps.setFloat(4, o.kolicina());
      ps.setFloat(5, o.cijena());
      ps.setTimestamp(6, new Timestamp(o.vrijeme()));

      return ps.executeUpdate() == 1;
    } catch (Exception ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  private Obracun kreirajObracun(ResultSet rs) throws SQLException {
    return new Obracun(
      rs.getInt("partner"),
      rs.getString("id"),
      rs.getBoolean("jelo"),
      rs.getFloat("kolicina"),
      rs.getFloat("cijena"),
      rs.getTimestamp("vrijeme").getTime()
    );
  }

  public Obracun dohvati(String id, int partnerId) {
	    String sql = "SELECT * FROM obracuni WHERE id = ? AND partner = ?";
	    try (PreparedStatement ps = vezaBP.prepareStatement(sql)) {
	        ps.setString(1, id);
	        ps.setInt(2, partnerId);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return kreirajObracun(rs);
	        }
	    } catch (Exception ex) {
	        Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    return null;
	}

}
