<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="fr">
<head>
    <meta charset="utf-8">
    <title>Ecriture d'un paragraphe</title>
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Spectral&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <style>
        p {
            text-align: justify;
        }

        #add-choice {
            list-style: none;
        }
    </style>
</head>
<body>
<%@include file="header.jsp" %>
<main>
    <h1>Éditeur de paragraphe</h1>
    <p>Paragraphe créé par <a href="/user/${paragraph.author}">${paragraph.author}</a></p>
    <form action="${pageContext.request.contextPath}/edit" method="post">
        <input type="hidden" name="paragraph" value="${paragraph.id}">
        <c:if test="${paragraph.choices.size() == 0 && paragraph.next == null}">
            <input type="submit" name="delete" value="Supprimer le paragraphe ou abandonner l'édition">
            <br>
        </c:if>
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
Peut-être simplement à me mettre au service de la communauté, à faire le don, le don de soi...">${paragraph.text}</textarea>
        <br>
        <p>Contrôlez les options proposées au lecteur à la fin du paragraphe&nbsp;:</p>
        <c:choose>
            <c:when test="${paragraph.choices.size() > 0}">
                <input id="choices-mode" type="radio" name="mode" value="choices" checked required>
            </c:when>
            <c:otherwise>
                <input id="choices-mode" type="radio" name="mode" value="choices" required>
            </c:otherwise>
        </c:choose>
        <label for="choices-mode">Proposer un ou plusieurs choix&nbsp;:</label>
        <ul id="choicesList">
            <c:choose>
                <c:when test="${paragraph.choices.size() != 0}">
                    <c:forEach items="${paragraph.choices}" var="choice">
                        <li id="choice${choice.id}">
                            <input disabled placeholder="Titre du choix" aria-label="Titre du choix" value="${choice.title}">
                            <select disabled
                                    name="cond"
                                    aria-label="Conditionner le choix par un paragraphe précédent">
                                <c:if test="${choice.condition == 0}">
                                    <option selected value="0">Condition</option>
                                </c:if>
                                <c:forEach items="${condTargets}" var="condPar">
                                    <c:choose>
                                        <c:when test="${condPar.id == choice.condition}">
                                            <option selected value="${condPar.id}">${condPar.text}</option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="${condPar.id}">${condPar.text}</option>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </select>
                        </li>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <li id="choice0">
                        <input placeholder="Titre du choix" aria-label="Titre du choix" name="title">
                        <select name="cond" aria-label="Conditionner le choix par un paragraphe précédent">
                            <option selected value="0">Condition</option>
                            <c:forEach items="${condTargets}" var="cndPar">
                                <option value="${cndPar.id}">${cndPar.text}</option>
                            </c:forEach>
                        </select>
                    </li>
                </c:otherwise>
            </c:choose>
            <li id="add-choice">
                <button type="button" onclick="addChoice()">Ajouter un choix</button>
            </li>
        </ul>
        <c:choose>
            <c:when test="${paragraph.next != null}">
                <input id="next-mode" type="radio" name="mode" value="next" checked required>
            </c:when>
            <c:otherwise>
                <input id="next-mode" type="radio" name="mode" value="next">
            </c:otherwise>
        </c:choose>
        <label for="next-mode">Forcer l'accès à un unique paragraphe suivant&nbsp;:</label>
        <select name="next" aria-label="Paragraphe suivant">
            <c:if test="${paragraph.next == null}">
                <option selected>Paragraphe suivant</option>
            </c:if>
            <c:forEach items="${nextTargets}" var="nextPar">
                <c:choose>
                    <c:when test="${paragraph.next == nextPar}">
                        <option selected value="${nextPar.id}">${nextPar.text}</option>
                    </c:when>
                    <c:otherwise>
                        <option value="${nextPar.id}">${nextPar.text}</option>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </select>
        <br>
        <br>
        <c:choose>
            <c:when test="${paragraph.isEnding()}">
                <input id="ending-mode" type="radio" name="mode" value="ending" checked>
            </c:when>
            <c:otherwise>
                <input id="ending-mode" type="radio" name="mode" value="ending">
            </c:otherwise>
        </c:choose>
        <label for="ending-mode">Ce paragraphe apporte une conclusion à l'histoire.</label>
        <br>
        <br>
        <button type="submit">Enregistrer</button>
    </form>
</main>
<%@include file="footer.jsp" %>
<script type="text/javascript">
    "use strict";

    var choices = null;
    var choiceCounter = 0;

    window.onload = function () {
        choices = document.getElementById('choicesList');
    };

    function addChoice() {
        var listItem = document.createElement('li');
        listItem.id = 'choice' + ++choiceCounter;
        // Choice title
        var input = document.createElement('input');
        input.placeholder = 'Titre du choix';
        input.name = 'title';
        listItem.appendChild(input);
        // Conditional choice
        var select = document.createElement('select');
        select.name = 'cond';
        var option = document.createElement('option');
        option.selected = true;
        option.value = '0';
        option.innerText = 'Condition';
        select.appendChild(option);
        listItem.appendChild(document.createTextNode(' '));
        listItem.appendChild(select);
        var button = document.createElement('button');
        button.type = 'button';
        button.id = 'remove' + choiceCounter;
        button.innerText = '\u00d7'; // &times;
        button.onclick = (function () {
            choices.removeChild(document.getElementById('choice' + this));
        }).bind(choiceCounter);
        listItem.appendChild(document.createTextNode(' '));
        listItem.appendChild(button);
        choices.insertBefore(listItem, choices.lastElementChild);
    }
</script>
</body>
</html>