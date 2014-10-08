package es.usc.citius.servando.calendula.model;


import java.util.UUID;

/**
 * Created by joseangel.pineiro on 12/5/13.
 */
public class Medicine {

    private String id;
    private String name;

    private Presentation presentation;

    public Medicine() {
        this.id = UUID.randomUUID().toString();
    }

    public Medicine(String name) {
        this();
        this.name = name;
    }

    public Medicine(String name, Presentation presentation) {
        this();
        this.name = name;
        this.presentation = presentation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Presentation getPresentation() {
        return presentation;
    }

    public void setPresentation(Presentation presentation) {
        this.presentation = presentation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
