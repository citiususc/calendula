
package es.usc.citius.servando.calendula.remote.activeingredients;


public class ActiveIngredient {

    private Integer _id;
    private String codigo;
    private String nombre;

    /**
     * 
     * @return
     *     The id
     */
    public Integer get_id() {
        return _id;
    }

    /**
     * 
     * @param _id
     *     The _id
     */
    public void set_id(Integer _id) {
        this._id = _id;
    }

    /**
     * 
     * @return
     *     The codigo
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * 
     * @param codigo
     *     The codigo
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * 
     * @return
     *     The nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * 
     * @param nombre
     *     The nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
