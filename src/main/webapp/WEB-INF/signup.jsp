<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html lang="fr">

<head>
    <meta charset="UTF-8"/>
    <title>Ensimag Stories - Inscription</title>
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Spectral&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <style>
        body {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            display: flex;
            flex-direction: column;
        }

        main {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        form {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-evenly;
        }

        form div {
            display: flex;
            justify-content: space-between;
        }

        input {
            margin-left: 8px;
        }
    </style>
</head>

<body>
<%@include file="header.jsp" %>
<main>
    <h1>Inscription</h1>
    <p>Inscrivez-vous sur notre plateforme pour proposer vos propres contributions et vos propres histoires.</p>
    <form name="signup" action="/signup" method="post">
        <div>
            <label for="user">Nom d'utilisateur&nbsp;: </label>
            <input id="user" name="user" required>
        </div>
        <div>
            <label for="mail">Adresse E-mail&nbsp;: </label>
            <input id="mail" name="mail" type="email" required>
        </div>
        <div>
            <label for="pass">Mot de passe&nbsp;: </label>
            <input id="pass" name="pass" type="password" required>
        </div>
        <input type="submit" name="submit" value="S'Inscrire">
    </form>
</main>
<%@include file="footer.jsp" %>
</body>

</html>
