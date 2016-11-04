package es.usc.citius.servando.calendula.remote.excipients;

/**
 * Created by alvaro.brey.vilas on 4/11/16.
 */
public class Excipient {

    private Integer _id;
    private String nombre;

    /**
     *
     * @return
     * The id
     */
    public Integer get_id() {
        return _id;
    }

    /**
     *
     * @param _id
     * The _id
     */
    public void set_id(Integer _id) {
        this._id = _id;
    }

    /**
     *
     * @return
     * The nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     *
     * @param nombre
     * The nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
