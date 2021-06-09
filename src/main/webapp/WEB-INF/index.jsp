<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="utf-8">
    <title>Start Page</title>
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Spectral&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <style>
        #stories {
            display: flex;
            flex-wrap: wrap;
        }

        .story {
            flex: 1;
            display: flex;
            flex-direction: column;
            min-width: 500px;
            max-width: 500px;
            height: 344px;
            margin: 0 32px 32px 0;
            box-shadow: 0 2px 2px 0 #242424, 0 3px 1px -2px #333333, 0 1px 5px 0 #1f1f1f;
            border: 1px solid #606060;
            border-radius: 3px
        }

        .story-preview {
            flex: 1;
            overflow: hidden;
            padding: 8px;
            color: black;
            text-align: justify;
            text-decoration: none;
        }

        .story-author {
            text-align: right;
        }

        .story:hover {
            box-shadow: 0 16px 16px 0 #242424, 0 3px 1px -2px #333333, 0 1px 5px 0 #1f1f1f;
        }

        @media (prefers-color-scheme: dark) {
            .story {
                background-color: #333;
                box-shadow: 0 2px 2px 0 #dbdbdb, 0 3px 1px -2px #cccccc, 0 1px 5px 0 #e0e0e0;
            }

            .story:hover {
                box-shadow: 0 8px 8px 0 #dbdbdb, 0 3px 1px -2px #cccccc, 0 1px 5px 0 #e0e0e0;
            }

            .story-preview {
                color: white;
            }

            .story-author {
                background-color: #3c3c3c;
            }
        }
    </style>
</head>
<body>
<%@include file="header.jsp" %>
<main>
    <h1>Nos meilleures histoires</h1>
    <div id="stories">
        <c:forEach items="${stories}" var="story">
          <div class="story">
              <a class="story-preview" href="story/${story.id}">
                 <h2>${story.title}</h2>
                 <p>${story.summary}</p>
              </a>
              <span class="story-author">Par&nbsp;
                  <c:forEach items="${story.authors}" var="author">
                      <a href="user/${author}">@${author}</a>,
                  </c:forEach>
              </span>
          </div>
        </c:forEach>
    </div>
</main>
<%@include file="footer.jsp" %>
</body>
</html>
