<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" import="fr.ensimag.tales.model.Story" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ONINVITE" value="<%= Story.Visibility.ONINVITE %>"/>
<html lang="fr">
<head>
    <meta charset="utf-8">
    <title>Page utilisateur</title>
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Spectral&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <style>
        main {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
        }

        .story {
            margin: 4px 0 4px 0;
            padding: 0 0.83em 0 0.83em;
            border: 1px solid black;
        }

        @media (prefers-color-scheme: dark) {
            .story {
                border: 1px solid white;
            }
        }
    </style>
</head>
<body>
<%@include file="header.jsp" %>
<main>
    <h1>Histoires par ${user}</h1>
    <c:forEach items="${stories}" var="story">
        <div class="story">
            <div class="story-preview" href="story/${story.id}">
                <h2>${story.title}</h2>
                <a href="/story/${story.id}">Lire</a>
                <aside>
                    <h3>Co-auteurs</h3>
                    <ul>
                        <c:forEach items="${story.authors}" var="author">
                            <li>${author}</li>
                        </c:forEach>
                    </ul>
                </aside>
                <% if (session.getAttribute("user") == request.getAttribute("user")) { %>
                <form id="settings${story.id}" name="settings${story.id}" action="${pageContext.request.contextPath}/settings/${story.id}"
                      method="post"></form>
                <form id="invite${story.id}" name="invite${story.id}" action="${pageContext.request.contextPath}/invite/${story.id}" method="post"></form>
                <form id="publish${story.id}" name="invite${story.id}" action="${pageContext.request.contextPath}/publish/${story.id}" method="post"></form>
                <table>
                    <tr>
                    <tr>
                        <td>
                            <label for="vis${story.id}">Éditable par&nbsp;:</label>
                        </td>
                        <td>
                            <select id="vis${story.id}" class="setvis" form="settings${story.id}" name="visibility">
                                <c:choose>
                                    <c:when test="${story.visibility == ONINVITE}">
                                        <option value="public">Tout le monde</option>
                                        <option value="oninvite" selected>Sur invitation</option>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="public" selected>Tout le monde</option>
                                        <option value="oninvite">Sur invitation</option>
                                    </c:otherwise>
                                </c:choose>
                            </select>
                            <button form="settings${story.id}">OK</button>
                        </td>
                    </tr>
                    <tr id="row-invite${story.id}">
                        <c:if test="${story.visibility == ONINVITE}">
                            <td><label for="user${story.id}">Utilisateur &nbsp;: </label></td>
                            <td>
                                <input form="invite${story.id}" type="hidden" name="story" value="${story.id}"/>
                                <input form="invite${story.id}" id="user${story.id}" name="user">
                            </td>
                            <td>
                                <button form="invite${story.id}">Inviter</button>
                            </td>
                        </c:if>
                    </tr>
                    <tr>
                        <c:choose>
                            <c:when test="${story.published}">
                                <td><em>Histoire publiée</em></td>
                                <td>
                                    <button form="publish${story.id}">Dépublier</button>
                                </td>
                            </c:when>
                            <c:otherwise>
                                <td><em>Histoire non-publiée</em></td>
                                <td>
                                    <button form="publish${story.id}">Publier</button>
                                </td>
                            </c:otherwise>
                        </c:choose>
                    </tr>
                    </tbody>
                </table>
                <% } %>
            </div>
        </div>
    </c:forEach>
</main>
<%@include file="footer.jsp" %>
<script type="application/javascript">
    "use strict";
    var selects = document.getElementsByClassName('setvis');
    for (var i = 0; i < selects.length; ++i) {
        var select = selects.item(i);
        select.onchange = (function () {
            var story = this.id.slice(3);
            var row = document.getElementById(this.id.replace('vis', 'row-invite'));
            if (this.value === 'public') {
                row.innerHTML = '';
            } else {
                row.innerHTML = '';
                // Label
                var cell = document.createElement('td');
                var label = document.createElement('label');
                label.setAttribute('for', 'user' + story);
                label.innerText = 'Utilisateur\u00a0: ';
                cell.appendChild(label);
                row.appendChild(cell);
                // Input
                cell = document.createElement('td');
                var input = document.createElement('input');
                input.setAttribute('form', 'invite' + story);
                input.type = 'hidden';
                input.name = 'story';
                input.value = story;
                cell.appendChild(input);
                input = document.createElement('input');
                input.setAttribute('form', 'invite' + story);
                input.id = 'user' + story;
                input.name = 'user';
                cell.appendChild(input);
                row.appendChild(cell);
                cell = document.createElement('td');
                // Submit
                var button = document.createElement('button');
                button.setAttribute('form', 'invite' + story);
                button.innerText = 'Inviter';
                cell.appendChild(button);
                row.appendChild(cell);
            }
        }).bind(select);
    }
</script>
</body>
</html>
