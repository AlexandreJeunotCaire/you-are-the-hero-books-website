<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="utf-8">
    <title>${story.title}</title>
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Spectral&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <style>
        aside {
            float: right;
            border: 1px solid black;
        }

        p {
            text-align: justify;
        }

        button {
            background: none;
            border: none;
            cursor: pointer;
            text-decoration: underline;
            color: blue;
            font-family: 'Spectral', serif;
        }
        button[disabled] {
            background: #606060;
            cursor: not-allowed;
        }

        @media (prefers-color-scheme: dark) {
            aside {
                border: 1px solid white;
            }
            button {
                color: white;
            }
        }
    </style>
</head>
<body>
<%@include file="header.jsp" %>
<main>
    <a href="?back">&larr;&nbsp;Retour</a>
    <hr>
    <aside id="history">
        <h2>Historique</h2>
        <ul>
            <c:forEach items="${history}" var="choice">
                <li><a href="?back=${choice.source}">${choice.title}</a></li>
            </c:forEach>
        </ul>
    </aside>
    <% if (session.getAttribute("user") != null) { %>
    <a href="/edit/${paragraph.id}">Éditer le paragraphe</a><br>
    <% } %>
    <p>${paragraph.text}</p>
    <c:if test="${paragraph.isEnding()}">
        <em>Vous avez terminé l'histoire. Nous espérons qu'elle vous a plu. Vous pouvez revenir en arrière ou <a
                href="${pageContext.request.contextPath}/">retourner à l'accueil</a>.</em>
    </c:if>
    <form method="post" name="choices"><!-- no action: uses same URL -->
        <ul id="choices">
            <c:forEach items="${paragraph.choices}" var="choice">
                <li>
                    <% if (session.getAttribute("user") == null) { %>
                        <button type="submit" name="choice" value="${choice.id}">${choice.title}</button>
                    <% } else { %>
                        <c:choose>
                            <c:when test="${choice.destination != 0 && choice.editor == null}">
                                <button type="submit" name="choice" value="${choice.id}">${choice.title}</button>
                            </c:when>
                            <c:otherwise>
                                <button disabled>${choice.title}</button>
                                <c:set var="user" value="<%= session.getAttribute(\"user\") %>" />
                                <c:if test="${choice.editor == null || choice.editor == user}">
                                    <a href="/edit/new/${choice.id}">(Proposer un nouveau paragraphe)</a>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    <% } %>
                </li>
            </c:forEach>
        </ul>
    </form>
</main>
<%@include file="footer.jsp" %>
<script type="application/javascript">
    "use strict";

    var initialChoice = document.cookie.split("=").pop();
    function checkChoice(event) {
        if (event.target.value !== initialChoice) {
            if (!confirm("Attention! Vous êtes sur le point de modifier un de vos choix dans l'histoire : cela supprimera votre progression dans les autres branches de l'histoire. Souhaitez-vous continuer ?")) {
                event.preventDefault();
            }
        }
    }

    if (initialChoice !== "") {
        window.onload = function () {
            var choices = document.getElementById("choices");
            for (var i = 0; i < choices.childElementCount; ++i) {
                choices.children[i].addEventListener("click", checkChoice, false);
            }
        };
    }
</script>
</body>
</html>
