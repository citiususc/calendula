package es.usc.citius.servando.calendula.model;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
public class Dose {

    int ammount = 1;

    public Dose(){}

    public Dose(int ammount){
        this.ammount=ammount;
    }

    public Integer ammount(){
        return ammount;
    }

    public void setAmmount(int ammount){
        this.ammount = ammount;
    }
    // units
    // ....
}
