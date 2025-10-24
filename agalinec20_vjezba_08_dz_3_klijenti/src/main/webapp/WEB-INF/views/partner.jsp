<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="edu.unizg.foi.nwtis.podaci.Partner" %>
<%@ page session="true" %>
<%
  String korisnik = request.getRemoteUser();
  boolean jeAdmin = request.isUserInRole("admin");
  boolean jeNwtis = request.isUserInRole("nwtis");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Detalji partnera</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      margin: 0;
      padding: 0;
      background-color: #fefae0;
    }
    header {
      background-color: #a8d5ba;
      color: #2d4739;
      padding: 20px;
      text-align: center;
    }
    #container {
      display: flex;
    }
    #nav {
      background-color: #f0e5c9;
      width: 250px;
      padding: 20px;
      border-right: 2px solid #cfcfcf;
      height: 100vh;
      box-sizing: border-box;
    }
    #nav ul {
      list-style-type: none;
      padding-left: 0;
    }
    #nav li {
      margin-bottom: 10px;
    }
    #nav a {
      color: #3e553f;
      text-decoration: none;
    }
    #nav a:hover {
      text-decoration: underline;
    }
    #main {
      flex-grow: 1;
      padding: 40px;
      background-color: #f6fdf1;
    }
    h2 {
      border-bottom: 1px solid #a1c89b;
      padding-bottom: 5px;
      color: #2d4739;
    }
    .auth {
      position: absolute;
      right: 20px;
      top: 20px;
    }
    .auth a {
      margin-left: 10px;
      color: #2d4739;
    }
    table {
	  border-collapse: collapse;
	  width: 100%;
	  max-width: 700px;
	  border: 1px solid #c1c1c1;
	}
	th, td {
	  padding: 10px;
	  text-align: left;
	  border: 1px solid #c1c1c1;
	}
    th {
      background-color: #d6eadf;
      width: 200px;
    }
    td {
      background-color: #fefefc;
    }
  </style>
</head>
<body>

<header>
  <h1>Vježba 8 – zadaća 3</h1>
  <div class="auth">
    <% if (korisnik == null) { %>
      <a href="${pageContext.request.contextPath}/prijavaKorisnika.xhtml">Prijava</a>
    <% } else { %>
      Prijavljeni korisnik: <strong><%= korisnik %></strong>
      (<a href="${pageContext.request.contextPath}/privatno/odjavaKorisnika.xhtml">Odjava</a>)
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
    <h2>Detalji partnera</h2>

    <%
      Partner partner = (Partner) request.getAttribute("partner");
      if (partner != null) {
    %>
      <table>
        <tr><th>ID</th><td><%= partner.id() %></td></tr>
        <tr><th>Naziv</th><td><%= partner.naziv() %></td></tr>
        <tr><th>Vrsta kuhinje</th><td><%= partner.vrstaKuhinje() %></td></tr>
        <tr><th>Adresa</th><td><%= partner.adresa() %></td></tr>
        <tr><th>Mrežna vrata</th><td><%= partner.mreznaVrata() %></td></tr>
        <tr><th>Mrežna vrata za kraj</th><td><%= partner.mreznaVrataKraj() %></td></tr>
        <tr><th>GPS</th><td><%= partner.gpsSirina() %>, <%= partner.gpsDuzina() %></td></tr>
        <tr><th>Admin kod</th><td>******</td></tr>
      </table>
    <%
      } else {
    %>
      <p>Partner nije pronađen ili je došlo do greške.</p>
    <%
      }
    %>
  </div>
</div>

</body>
</html>
