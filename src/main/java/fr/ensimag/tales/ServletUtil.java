package fr.ensimag.tales;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class ServletUtil {
    /**
     * Infer integer value from the last part of the request URL.
     *
     * @param request  HTTP Request
     * @param response HTTP Response to redirect on error
     * @return Story ID
     * @throws NumberFormatException On invalid URL
     */
    public static int checkStoryId(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI();
        url = url.substring(url.lastIndexOf("/") + 1);
        try {
            return Integer.parseInt(url);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/");
            throw new ServletException(e);
        }
    }

    /**
     * Fail with an error message and HTTP 400 status code.
     *
     * @param response HTTP Response.
     * @param errorMessage Message dispalyed to the user.
     */
    public static void fail(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        final PrintWriter out = response.getWriter();
        out.println(errorMessage);
    }
}
