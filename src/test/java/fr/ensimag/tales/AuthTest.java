package fr.ensimag.tales;

import java.io.IOException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

public class AuthTest {

    private static final WebClient webClient = new WebClient();

    @Before
    public void setupClient() {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false); // allow 4xx, 5xx
        webClient.getOptions().setRedirectEnabled(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
    }

    @After
    public void teardownClient() {
        webClient.close();
    }

    @Test
    public void getLoginShouldRedirect() throws IOException {
        final TextPage page = webClient.getPage("http://localhost:8080/login?user=willie&pass=toto");
        final WebResponse resp = page.getWebResponse();
        Assert.assertEquals(302, resp.getStatusCode());
    }

    @Test
    public void loginWithBadCredentials() throws IOException {
        final HtmlPage page = webClient.getPage("http://localhost:8080");
        HtmlForm form = page.getFormByName("login");
        form.getInputByName("user").type("willie");
        form.getInputByName("pass").type("toto");

        final HtmlPage newPage = form.getInputByName("submit").click();
        final WebResponse resp = newPage.getWebResponse();
        Assert.assertEquals(400, resp.getStatusCode());
    }

    @Test
    public void signup() throws IOException {
        final HtmlPage page = webClient.getPage("http://localhost:8080/signup");
        HtmlForm form = page.getFormByName("signup");
        form.getInputByName("user").type("johndoe");
        form.getInputByName("mail").type("john.doe@example.com");
        form.getInputByName("pass").type("p4s$w0rd");

        final TextPage newPage = form.getInputByName("submit").click();
        final WebResponse resp = newPage.getWebResponse();
        Assert.assertEquals(302, resp.getStatusCode()); // redirect to homepage
    }

    @Test
    public void signupWithBadEmail() throws IOException {
        final HtmlPage page = webClient.getPage("http://localhost:8080/signup");
        HtmlForm form = page.getFormByName("signup");
        form.getInputByName("user").type("johndoe");

        // Disable browser email validation
        final HtmlInput mailInput = form.getInputByName("mail");
        mailInput.setAttribute("type", "text"); // required to be able to send malformed request
        mailInput.type("john.doe@ example.com");

        form.getInputByName("pass").type("p4s$w0rd");

        final HtmlPage newPage = form.getInputByName("submit").click();
        final WebResponse resp = newPage.getWebResponse();
        Assert.assertEquals(400, resp.getStatusCode());
    }

    /**
     * To prevent user enumeration, signing up with an already used username
     * fails silently telling the user they should check their mailbox.
     *
     * @throws IOException
     */
    @Test
    public void signupWithAlreadyUsedUsername() throws IOException {
        final HtmlPage page = webClient.getPage("http://localhost:8080/signup");
        HtmlForm form = page.getFormByName("signup");
        form.getInputByName("user").type("willie");
        form.getInputByName("mail").type("ricardgu@example.com");
        form.getInputByName("pass").type("new_p4s$w0rd");

        final HtmlPage newPage = form.getInputByName("submit").click();
        final WebResponse resp = newPage.getWebResponse();
        Assert.assertEquals(200, resp.getStatusCode());
    }

}