<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.time.*, java.time.format.DateTimeFormatter" %>
<%@ page import="edu.unizg.foi.nwtis.podaci.Obracun" %>
<%
  List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
  String greska = (String) request.getAttribute("greska");
  String povratak = (String) request.getAttribute("povratak");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Rezultati obračuna</title>
  <style>
    body { font-family: Arial, sans-serif; background-color: #fefae0; padding: 20px; }
    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
    th, td { border: 1px solid #ccc; padding: 8px; }
    th { background-color: #d0e7d2; }
    tr:nth-child(even) { background-color: #f9f9f9; }
    .desno { text-align: right; }
  </style>
</head>
<body>

<h2>Rezultati obračuna</h2>

<% if (greska != null) { %>
  <p style="color:red;"><%= greska %></p>
<% } else if (obracuni != null && !obracuni.isEmpty()) { 
     DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d.M.yyyy. HH:mm:ss");
%>
  <table>
    <tr>
      <th>ID</th><th>Partner</th><th>Jelo/Piće</th>
      <th>Količina</th><th>Cijena</th><th>Ukupno</th><th>Vrijeme</th>
    </tr>
    <% for (Obracun o : obracuni) {
         double ukupno = o.kolicina() * o.cijena();
         Instant inst = (o.vrijeme() > 9999999999L) ? Instant.ofEpochMilli(o.vrijeme()) : Instant.ofEpochSecond(o.vrijeme());
         String vrijeme = LocalDateTime.ofInstant(inst, ZoneId.systemDefault()).format(fmt);
    %>
      <tr>
        <td class="desno"><%= o.id() %></td>
        <td class="desno"><%= o.partner() %></td>
        <td><%= o.jelo() ? "Jelo" : "Piće" %></td>
        <td class="desno"><%= o.kolicina() %></td>
        <td class="desno"><%= o.cijena() %></td>
        <td class="desno"><%= String.format("%.2f", ukupno) %></td>
        <td><%= vrijeme %></td>
      </tr>
    <% } %>
  </table>
<% } else { %>
  <p>Nema pronađenih obračuna za zadane kriterije.</p>
<% } %>

<p>
  <a href="<%= (povratak != null && povratak.equals("partner")) 
              ? request.getContextPath() + "/mvc/tvrtka/privatno/obracuni/partner" 
              : request.getContextPath() + "/mvc/tvrtka/privatno/obracuni" %>">Povratak</a>
</p>

</body>
</html>
