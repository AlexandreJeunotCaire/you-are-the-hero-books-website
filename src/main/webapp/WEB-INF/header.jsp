<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<header>
    <a href="${pageContext.request.contextPath}/">
        <img src="${pageContext.request.contextPath}/logo.svg" alt="Ensimag Stories">
    </a>
    <nav>
        <a href="${pageContext.request.contextPath}/">Accueil</a>
        <a href="${pageContext.request.contextPath}/">Histoires</a>
        <a href="${pageContext.request.contextPath}/edit">Édition</a>
        <% if (session.getAttribute("user") != null) { %>
        <a href="${pageContext.request.contextPath}/user">Mon Compte</a>
        <% } %>
    </nav>
    <% if (session.getAttribute("user") == null) { %>
    <form action="${pageContext.request.contextPath}/login" method="post" name="login">
        <table>
            <tbody>
            <tr>
                <td>
                    <label for="user">Nom d'utilisateur&nbsp;: </label>
                </td>
                <td>
                    <input id="user" name="user">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="pass">Mot de passe&nbsp;: </label>
                </td>
                <td>
                    <input id="pass" name="pass" type="password">
                </td>
            </tr>
            </tbody>
        </table>
        <br>
        <% if (session.getAttribute("error") != null) { %>
        <p style="color:red;"><%= session.getAttribute("error") %>
        </p>
        <% } %>
        <button>Se connecter</button>
    </form>
    <% } %>
</header>