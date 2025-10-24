<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>vježba 8 - dz 3</title>
    </head>
    <body>
        <h1>vježba 8 - dz 3</h1>
        <% String status =  (String) request.getAttribute("status"); %>
        KRAJ <%= status %>
    </body>
</html>
