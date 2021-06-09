<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="utf-8">
    <title>Ecriture d'un paragraphe</title>
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Spectral&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/styles.css">
    <style>
        p {
            text-align: justify;
        }
        #choicesList {

        }
    </style>
</head>
<body>
<%@include file="header.jsp" %>
<main>
    <form>
        <label for="content">Contenu du paragraphe :</label><br>
        <textarea rows="25" cols="200" id="content" name="content" placeholder="Vous savez, 
moi je ne crois pas qu'il y ait de bonne ou de mauvaise situation. 
Moi, si je devais résumer ma vie aujourd'hui avec vous, 
je dirais que c'est d'abord des rencontres. Des gens qui m'ont tendu la main, 
peut-être à un moment où je ne pouvais pas, où j'étais seul chez moi. 
Et c'est assez curieux de se dire que les hasards, les rencontres forgent une destinée... 
Parce que quand on a le goût de la chose, quand on a le goût de la chose bien faite, 
le beau geste, parfois on ne trouve pas l'interlocuteur en face je dirais, 
le miroir qui vous aide à avancer. 
Alors ça n'est pas mon cas, comme je disais là, puisque moi au contraire, 
j'ai pu : et je dis merci à la vie, je lui dis merci, je chante la vie, je danse la vie... 
je ne suis qu'amour ! Et finalement, quand beaucoup de gens aujourd'hui me disent 
« Mais comment fais-tu pour avoir cette humanité ? », et bien je leur réponds très simplement,
je leur dis que c'est ce goût de l'amour ce goût donc qui m'a poussé aujourd'hui à entreprendre
une construction mécanique, mais demain qui sait ? 
Peut-être simplement à me mettre au service de la communauté, à faire le don, le don de soi..."></textarea><br>
        <div>
          <input type="checkbox" id="fin" name="fin">
          <label for="fin">Fin</label>
        </div>
        <button type="button" id="addChoiceButton" onclick="addChoice()">Ajouter un choix</button>
        <button type="button" id="removeChoiceButton" onclick="removeChoice()">Enlever un choix</button>
        <ul id="choicesList">
            <li><input placeholder="Salut ma caille"/>
                    <div>
          <input type="checkbox" id="fin" name="fin">
          <label for="fin">Obligatoire</label>
        </div></li>
        </ul>

        <input type="submit" disabled value="Ajouter le paragraphe">
    </form>
</main>
<%@include file="footer.jsp" %>
<script type="text/javascript" src="javascript/addField.js"></script>
</body>
</html>