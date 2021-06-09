package fr.ensimag.tales.model;

import java.util.List;

/**
 * Paragraph of text in a story.
 */
public class Paragraph {
    /**
     * {@link Paragraph#getId}
     */
    private int id;

    /**
     * {@link Paragraph#getAuthor}
     */
    private String author;

    /**
     * {@link Paragraph#getText}
     */
    private String text;

    /**
     * {@link Paragraph@getNext}
     */
    private Paragraph next;

    /**
     * {@link Paragraph#isEnding}
     */
    private boolean ending;

    /**
     * {@link Paragraph#getChoices}
     */
    private List<Choice> choices;

    /**
     * Default constructor.
     * @param id {@link Paragraph#getId}
     * @param author {@link Paragraph#getAuthor}
     * @param text {@link Paragraph#getText}
     * @param next Optional next paragraph which immediately follows this one in the story.
     * @param ending True if the paragraph is a possible ending to its story.
     */
    public Paragraph(int id, String author, String text, Paragraph next, boolean ending, List<Choice> choices) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.next = next;
        this.ending = ending;
        this.choices = choices;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Paragraph)) {
            return false;
        }
        Paragraph other = (Paragraph)obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return ((Integer)id).hashCode();
    }

    /**
     * Unique paragraph identifier.
     *
     * @return Paragraph ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * Username of writer user.
     *
     * @return Writer username
     */
    public String getAuthor() {
        return author;
    }


    /**
     * Main paragraph text is limited to 4000 characters.
     *
     * @return Paragraph text
     */
    public String getText() {
        return text;
    }

    /**
     * Writers may specify a single next paragraph readers are forced to
     * when reading the current paragraph.
     *
     * @return Nullable reference to the next paragraph
     */
    public Paragraph getNext() {
        return next;
    }

    /**
     * Paragraphs may serve as a conclusion to a story.
     *
     * @return True if this paragraph is an ending to its story.
     */
    public boolean isEnding() {
        return ending;
    }

    /**
     * Paragraphs may provide options to the user
     * continuing the story in different ways.
     *
     * @return List of possible choices
     */
    public List<Choice> getChoices() {
        return choices;
    }

}
