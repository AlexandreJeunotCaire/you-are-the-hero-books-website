package fr.ensimag.tales.model;

/**
 * Choices define links between paragraphs.
 */
public class Choice {
    /**
     * {@link Choice#getId}
     */
    private int id;
    /**
     * {@link Choice#getTitle}
     */
    private String title;
    /**
     * {@link Choice#getSource}
     */
    private int src;
    /**
     * {@link Choice#getDestination}
     */
    private int dst;
    /**
     * {@link Choice#getCondition}
     */
    private int cond;
    /**
     * {@link Choice#getEditor}
     */
    private String editor;

    public Choice(int id, String title, int src, int dst, int cond, String editor) {
        this.id = id;
        this.title = title;
        this.src = src;
        this.dst = dst;
        this.cond = cond;
        this.editor = editor;
    }

    public Choice(String title, int dst, int cond) {
        this.title = title;
        this.dst = dst;
        this.cond = cond;
    }

    /**
     * Unique choice identifier.
     *
     * @return Choice ID
     */
    public int getId() {
        return id;
    }

    /**
     * Choice text is limited to 255 characters.
     *
     * @return Short title string
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get source paragraph ID.
     *
     * @return ID of the paragraph where this choice is offered.
     */
    public int getSource() {
        return src;
    }

    /**
     * Get destination paragraph ID.
     *
     * @return ID of the paragraph describing the outcome of the choice.
     */
    public int getDestination() {
        return dst;
    }

    /**
     * Get condition paragraph ID.
     *
     * @return ID of the paragraph that must have been traversed in order to proceed with the choice.
     */
    public int getCondition() {
        return cond;
    }

    /**
     * User currently editing the destination paragraph.
     * @return Username of the user holding the lock on this choice.
     */
    public String getEditor() {
        return editor;
    }

}
