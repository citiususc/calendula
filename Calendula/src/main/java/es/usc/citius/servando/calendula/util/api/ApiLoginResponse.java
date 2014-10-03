package es.usc.citius.servando.calendula.util.api;

/**
 * Created by joseangel.pineiro on 7/3/14.
 */
public class ApiLoginResponse extends ApiResponse{

    public LoginResponse data;


    public class LoginResponse{
        public String token;
    }

}
