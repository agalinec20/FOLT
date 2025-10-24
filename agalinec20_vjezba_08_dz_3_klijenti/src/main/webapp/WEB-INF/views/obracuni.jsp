<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Obracun" %>
<%@ page import="java.time.*, java.time.format.DateTimeFormatter" %>
<%
  String  korisnik = request.getRemoteUser();
  boolean jeAdmin  = request.isUserInRole("admin");
  boolean jeNwtis  = request.isUserInRole("nwtis");

  List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
  Integer status = (Integer) request.getAttribute("status");

  String od   = (String) request.getAttribute("od");
  String kraj = (String) request.getAttribute("do");
  String filter = (String) request.getAttribute("filter");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Privatno - Pregled obračuna</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #fefae0; }
    header { background-color: #a8d5ba; color: #2d4739; padding: 20px; text-align: center; position: relative; }
    .auth { position: absolute; top: 20px; right: 30px; font-size: 14px; }
    .auth a { color: #2d4739; text-decoration: none; margin-left: 10px; }
    .auth a:hover { text-decoration: underline; }
    #container { display: flex; }
    #nav { background-color: #f0e5c9; width: 250px; padding: 20px; border-right: 2px solid #cfcfcf; height: 100vh; box-sizing: border-box; }
    #nav h3 { margin-top: 20px; color: #365b37; }
    #nav ul { list-style-type: none; padding-left: 0; }
    #nav li { margin-bottom: 10px; }
    #nav a { color: #3e553f; text-decoration: none; }
    #nav a:hover { text-decoration: underline; }
    #main { flex-grow: 1; padding: 40px; background-color: #f6fdf1; }
    h2 { border-bottom: 1px solid #a1c89b; padding-bottom: 5px; color: #2d4739; }
    table { width: 100%; border-collapse: collapse; margin-top: 20px; border: 1px solid #c1c1c1; }
    th, td { border: 1px solid #c1c1c1; padding: 8px; text-align: left; }
    th { background: #d0e7d2; text-align: center; }
    tr:nth-child(even) { background: #f9f9f9; }
    .desno { text-align: right; }
    form label { margin-right: 15px; }
  </style>
</head>
<body>
<header>
  <h1>Vježba 8 – zadaća 3</h1>
  <div class="auth">
    <% if (korisnik == null) { %>
        <a href='${pageContext.request.contextPath}/prijavaKorisnika.xhtml'>Prijava</a>
    <% } else { %>
        Prijavljeni korisnik: <strong><%= korisnik %></strong>
        (<a href='${pageContext.request.contextPath}/privatno/odjavaKorisnika.xhtml'>Odjava</a>)
    <% } %>
  </div>
</header>

<div id="container">
    <div id="nav">
      <ul>
        <li><strong>Javni dio</strong></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica Tvrtka</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/index.xhtml">Početna stranica Partner</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/provjera">Provjera rada poslužitelja</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled partnera</a></li>

        <% if (jeNwtis) { %>
        <li><strong>Privatni dio</strong></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni">Pregled obračuna</a></li>
        <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni/partner">Pregled obračuna po partneru</a>
        <% } %>

        <% if (jeAdmin) { %>
        <li><strong>Administracijski dio</strong></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/kraj">Šalji komandu za kraj</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/status">Status poslužitelja Tvrtka</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/1">Start poslužitelja – registracija</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/1">Pauza poslužitelja – registracija</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/2">Start poslužitelja – partneri</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/2">Pauza poslužitelja – partneri</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka">Nadzorna konzola Tvrtka</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/partner">Dodavanje partnera</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spava">Spavanje poslužitelja</a></li>
        <% } %>
      </ul>
    </div>

  <div id="main">
    <h2>Pregled obračuna</h2>

    <form id="frm" method="get"
          action='${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni'
          onsubmit="pretvoriUDugacki()">

      Od:
      <input id="odIso" type="date" value='<%= od == null ? "" : od %>'>

      Do:
      <input id="doIso" type="date" value='<%= kraj == null ? "" : kraj %>'>

      <input type="hidden" name="od" id="odMs">
      <input type="hidden" name="do" id="doMs">

      Vrsta obračuna:
      <select name="filter">
          <option value=''    <%= (filter==null||filter.isBlank())? "selected":"" %>>Jelo i piće</option>
          <option value='jelo' <%= "jelo".equals(filter) ? "selected":"" %>>Samo jelo</option>
          <option value='pice' <%= "pice".equals(filter) ? "selected":"" %>>Samo piće</option>
      </select>

      <button type="submit">Filtriraj</button>
    </form>

    <script>
    function pretvoriUDugacki(){
        const odISO = document.getElementById('odIso').value;
        const doISO = document.getElementById('doIso').value;

        document.getElementById('odMs').value = odISO ? Date.parse(odISO) : '';
        document.getElementById('doMs').value = doISO ? Date.parse(doISO) + 86399999 : '';
    }
    </script>

<% if (status != null && status == 200 && obracuni != null && !obracuni.isEmpty()) {
     DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d.M.yyyy. HH:mm:ss"); %>

    <table>
      <tr>
        <th>ID</th><th>Partner</th><th>Jelo/Piće</th>
        <th>Količina</th><th>Cijena</th><th>Ukupno</th><th>Vrijeme</th>
      </tr>
    <% for (Obracun o : obracuni) {
         double ukupno = o.kolicina() * o.cijena();
         Instant inst  = (o.vrijeme() > 9_999_999_999L)
                         ? Instant.ofEpochMilli(o.vrijeme())
                         : Instant.ofEpochSecond(o.vrijeme());
         String vrijeme = LocalDateTime.ofInstant(inst, ZoneId.systemDefault()).format(fmt); %>
        <tr>
          <td class='desno'><%= o.id() %></td>
          <td class='desno'><%= o.partner() %></td>
          <td><%= o.jelo() ? "Jelo" : "Piće" %></td>
          <td class='desno'><%= o.kolicina() %></td>
          <td class='desno'><%= o.cijena() %></td>
          <td class='desno'><%= String.format("%.2f", ukupno) %></td>
          <td><%= vrijeme %></td>
        </tr>
    <% } %>
    </table>

<% } else if (status != null && status != 200) { %>
    <p style="color:red">Greška pri dohvaćanju obračuna (HTTP <%= status %>)</p>
<% } else if (od != null || kraj != null) { %>
    <p>Nema pronađenih obračuna za zadane kriterije.</p>
<% } %>

  </div><!-- /main -->
</div><!-- /container -->
</body>
</html>
