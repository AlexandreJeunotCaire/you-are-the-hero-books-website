<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="utf-8">
    <title>${paragraph.title}</title>
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Spectral&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/styles.css">
    <style>
        p {
            text-align: justify;
        }
    </style>
</head>
<body>
<%@include file="header.jsp" %>
<main>
    <h1>${paragraph.title}</h1>
    <p>${paragraph.text}</p>
    <ul>
    <c:forEach items="${paragraph.choices}" var="choice">
       <li>${choice.title}</li>
    </c:forEach>
    </ul>
</main>
<%@include file="footer.jsp" %>
</body>
</html>
