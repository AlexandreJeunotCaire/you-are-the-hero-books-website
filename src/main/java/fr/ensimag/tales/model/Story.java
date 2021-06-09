package fr.ensimag.tales.model;

import java.util.Set;

/**
 * A Story is a collection of paragraphs linked by choices.
 */
public class Story {
    /**
     * {@link Story#getId}
     */
    private int id;

    /**
     * {@link Story#getAuthor}
     */
    private String author;

    /**
     * {@link Story#getVisibility}
     */
    private Visibility visibility;

    /**
     * {@link Story#getPublished}
     */
    private boolean published;

    /**
     * {@link Story#getFirstParagraph}
     */
    private int firstPar;

    /**
     * {@link Story#getSummary}
     */
    private String summary;

    /**
     * {@link Story#getAuthors}
     */
    private Set<String> authors;

    public enum Visibility {
        PUBLIC,
        ONINVITE
    }

    /**
     * Default constructor.
     * @param id {@link Story#getId}
     * @param author {@link Story#getAuthor}
     * @param visibility {@link Story#getVisibility}
     * @param published {@link Story#getPublished}
     * @param firstPar {@link Story#getFirstParagraph}
     * @param summary {@link Story#getSummary}
     */
    public Story(int id, String author, Visibility visibility, boolean published, int firstPar, String summary, Set<String> authors) {
        this.id = id;
        this.author = author;
        this.visibility = visibility;
        this.published = published;
        this.firstPar = firstPar;
        this.summary = summary;
        this.authors = authors;
    }

    /**
     * Unique story identifier.
     * @return Story ID
     */
    public int getId() {
        return id;
    }

    /**
     * Original creator user.
     *
     * Note: Story author is not necessarily the writer of the first paragraph as
     * he or she may invite another user to collaborate on the first paragraph.
     *
     * @return Creator username.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * All users may collaborate on 'public' stories. 'on invite' stories
     * require their author to invite other users to participate before they can collaborate.
     *
     * @return Story visibility
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Whether the story was published and released to the general public.
     *
     * @return True if the story is publicly availble to readers
     */
    public boolean getPublished() {
        return published;
    }

    /**
     * All stories must start with a paragraph.
     *
     * @return Reference to the first paragraph in the story.
     */
    public int getFirstParagraph() {
        return firstPar;
    }

    /**
     * Infer title from first sentence.
     *
     * @return Short title for the paragraph.
     */
    public String getTitle() {
        final String[] pieces = summary.split("\\.");
        if (pieces.length > 0) {
            return pieces[0];
        }
        return "";
    }

    /**
     * Return story summary.
     * @return Summary text contents.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * List of effective authors (users who wrote at least one paragraph).
     */
    public Set<String> getAuthors() {
        return authors;
    }

}
