package es.usc.citius.servando.calendula.util.api;


/**
 * Created by joseangel.pineiro on 7/3/14.
 */
public class ApiResponse{

    public int status;
    public boolean success;
    public String message;

    public ApiResponse(){

    }

    public ApiResponse(boolean success){
        this.success=success;
    }
}
