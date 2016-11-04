
package es.usc.citius.servando.calendula.remote.medicineallergiesinfo.datamodel;

public class AllergiesInfo {

    private Integer _id;
    private FormasFarmaceuticas formasFarmaceuticas;

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
     *     The formasFarmaceuticas
     */
    public FormasFarmaceuticas getFormasFarmaceuticas() {
        return formasFarmaceuticas;
    }

    /**
     * 
     * @param formasFarmaceuticas
     *     The formasFarmaceuticas
     */
    public void setFormasFarmaceuticas(FormasFarmaceuticas formasFarmaceuticas) {
        this.formasFarmaceuticas = formasFarmaceuticas;
    }

}
