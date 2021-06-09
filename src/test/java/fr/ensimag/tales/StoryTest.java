package fr.ensimag.tales;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;

public class StoryTest {
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
    public void getInvalidStoryShouldRedirect() throws IOException {
        // No story id
        TextPage page = webClient.getPage("http://localhost:8080/story");
        WebResponse resp = page.getWebResponse();
        Assert.assertEquals(302, resp.getStatusCode());

        // Word instead of number
        page = webClient.getPage("http://localhost:8080/story/toto");
        resp = page.getWebResponse();
        Assert.assertEquals(302, resp.getStatusCode());

        // Number literal then gibberish
        page = webClient.getPage("http://localhost:8080/story/12#@9");
        resp = page.getWebResponse();
        Assert.assertEquals(302, resp.getStatusCode());

        // OK
        final HtmlPage htmlPage = webClient.getPage("http://localhost:8080/story/1");
        resp = htmlPage.getWebResponse();
        Assert.assertEquals(200, resp.getStatusCode());
    }

    @Test
    public void getInexistingStoryShouldRedirect() throws IOException {
        final TextPage page = webClient.getPage("http://localhost:8080/story");
        final WebResponse resp = page.getWebResponse();
        Assert.assertEquals(302, resp.getStatusCode());
    }

    @Test
    public void readStory() throws IOException {
        final HtmlPage page = webClient.getPage("http://localhost:8080/story/1");
        final HtmlForm form = page.getFormByName("choices");

        // Pick first choice
        List<HtmlButton> buttons = form.getButtonsByName("choice");
        final HtmlPage endPage = buttons.get(0).click();

        final HtmlEmphasis ending = (HtmlEmphasis) endPage.getElementsByTagName("em").get(0);
        Assert.assertTrue(ending.getTextContent().contains("Vous avez terminé l'histoire"));
    }

    @Test
    public void navigateStory() throws IOException {
        final HtmlPage page = webClient.getPage("http://localhost:8080/story/1");
        final HtmlForm form = page.getFormByName("choices");

        // Pick first choice
        List<HtmlButton> buttons = form.getButtonsByName("choice");
        final HtmlPage nextPage = buttons.get(0).click();

        // Go back
        final HtmlPage previousPage = page.getAnchorByHref("?back").click();

        // Pick second choice and confirm
        final HtmlPage endPage = buttons.get(1).click();

        final HtmlEmphasis ending = (HtmlEmphasis) endPage.getElementsByTagName("em").get(0);
        Assert.assertTrue(ending.getTextContent().contains("Vous avez terminé l'histoire"));
    }

}