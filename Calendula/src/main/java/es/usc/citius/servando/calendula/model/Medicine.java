package es.usc.citius.servando.calendula.model;

/**
 * Created by joseangel.pineiro on 12/5/13.
 */
public class Medicine {

    private String name;


    public Medicine() {
    }

    public Medicine(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
