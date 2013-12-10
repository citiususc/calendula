package es.usc.citius.servando.calendula.model;


/**
 * Created by joseangel.pineiro on 12/5/13.
 */
public class Medicine {


    private String name;
    private Presentation presentation;

    public Medicine() {
    }

    public Medicine(String name) {
        this.name = name;
    }

    public Medicine(String name, Presentation presentation) {
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
}
