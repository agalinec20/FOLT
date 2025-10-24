<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
  <title>Nadzorna konzola tvrtke</title>
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
    .status-radi {
      color: green;
      font-weight: bold;
    }
    .status-neradi {
      color: red;
      font-weight: bold;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
    }
    table, th, td {
      border: 1px solid #c1c1c1;
    }
    th {
      background-color: #d0e7d2;
      text-align: center;
      padding: 8px;
    }
    td {
      padding: 8px;
    }
    .desno {
      text-align: right;
    }
    button {
      padding: 6px 12px;
      margin: 2px;
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
    <h2>Nadzorna konzola tvrtke</h2>

    <button onclick="posaljiKomanduREST('kraj')">Završi rad poslužitelja</button>

    <table>
      <tr><th>Komponenta</th><th>Akcije</th></tr>
      <tr>
        <td class="desno">Dio 1 - Registracija</td>
        <td>
          <button onclick="posaljiKomanduREST('pauza', 1)">PAUZA</button>
          <button onclick="posaljiKomanduREST('start', 1)">START</button>
          <button onclick="posaljiKomanduREST('status', 1)">STATUS</button>
        </td>
      </tr>
      <tr>
        <td class="desno">Dio 2 - Rad s partnerima</td>
        <td>
          <button onclick="posaljiKomanduREST('pauza', 2)">PAUZA</button>
          <button onclick="posaljiKomanduREST('start', 2)">START</button>
          <button onclick="posaljiKomanduREST('status', 2)">STATUS</button>
        </td>
      </tr>
    </table>

    <br />
    <p>Status poslužitelja: <span id="status" class="status-neradi">NE RADI</span></p>
    <p>Broj obračuna: <span id="broj">?</span></p>
    <p>Zadnja interna poruka: <span id="poruka">/</span></p>

    <form onsubmit="posaljiPoruku(); return false;">
      <label for="porukaTekst">Unesi internu poruku:</label>
      <input type="text" id="porukaTekst" name="porukaTekst" required />
      <button type="submit">Pošalji poruku</button>
    </form>
  </div>
</div>

<script type="text/javascript">
  var wsocket;
  function connect() {
    var adresa = window.location.pathname;
    var dijelovi = adresa.split("/");
    var wsURL = "ws://" + window.location.hostname + ":" + window.location.port + "/" + dijelovi[1] + "/ws/tvrtka";

    if ('WebSocket' in window) {
      wsocket = new WebSocket(wsURL);
    } else if ('MozWebSocket' in window) {
      wsocket = new MozWebSocket(wsURL);
    } else {
      alert('WebSocket nije podržan od preglednika.');
      return;
    }
    wsocket.onmessage = onMessage;
  }

  function onMessage(evt) {
	  const data = evt.data;
	  const dijelovi = data.split(";");
	  const status = dijelovi[0] || "NE RADI";
	  const broj = dijelovi[1] || "?";
	  const poruka = dijelovi.slice(2).join(";");  

	  const statusEl = document.getElementById("status");
	  statusEl.textContent = status;
	  statusEl.className = (status === "RADI") ? "status-radi" : "status-neradi";

	  document.getElementById("broj").textContent = broj;
	  document.getElementById("poruka").textContent = poruka || "/";
	}

  function posaljiPoruku() {
    const tekst = document.getElementById("porukaTekst").value.trim();
    if (tekst && wsocket) {
      const poruka = "RADI;;" + tekst;
      wsocket.send(poruka);
      document.getElementById("porukaTekst").value = "";
    }
  }

  function posaljiKomanduREST(komanda, dio = "") {
    let url = "/agalinec20_vjezba_08_dz_3_klijenti/api/tvrtka/" + komanda + (dio ? "/" + dio : "");
    fetch(url, { method: 'HEAD' })
      .then(() => console.log(`Komanda '${komanda}' poslana${dio ? ' za dio ' + dio : ''}`))
      .catch(err => console.error('Greška pri slanju komande:', err));
  }

  window.addEventListener("load", connect, false);
</script>
</body>
</html>