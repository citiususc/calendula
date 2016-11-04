
package es.usc.citius.servando.calendula.remote.medicineallergiesinfo.datamodel;

import java.util.ArrayList;
import java.util.List;

public class FormasFarmaceuticas {

    private List<PrincipioActivo> principioActivos = new ArrayList<PrincipioActivo>();
    private List<Excipiente> excipientes = new ArrayList<Excipiente>();

    /**
     * 
     * @return
     *     The principioActivos
     */
    public List<PrincipioActivo> getPrincipioActivos() {
        return principioActivos;
    }

    /**
     * 
     * @param principioActivos
     *     The principioActivos
     */
    public void setPrincipioActivos(List<PrincipioActivo> principioActivos) {
        this.principioActivos = principioActivos;
    }

    /**
     * 
     * @return
     *     The excipientes
     */
    public List<Excipiente> getExcipientes() {
        return excipientes;
    }

    /**
     * 
     * @param excipientes
     *     The excipientes
     */
    public void setExcipientes(List<Excipiente> excipientes) {
        this.excipientes = excipientes;
    }

}
