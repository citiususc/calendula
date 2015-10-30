package es.usc.citius.servando.calendula.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.mikepenz.aboutlibraries.LibsBuilder;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;

public class AboutActivity extends CalendulaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupToolbar("Open Source", getResources().getColor(R.color.dark_grey_home));
        setupStatusBar(getResources().getColor(R.color.dark_grey_home));

        if (savedInstanceState == null) {

            Fragment fragment = new LibsBuilder()
                    .withAboutAppName("Calendula")
                    .withAboutIconShown(true)
                    .withAboutVersionShown(true)
                    .withLicenseShown(true)
                    .withLicenseDialog(true)
                    .withAboutDescription(
                            "Calendula es un proyecto de código abierto. Puedes contribuír con tus ideas y sugerencias en" +
                                    "<br/><br/> <a href='https://github.com/citiususc/calendula'> https://github.com/citiususc/calendula </a>")
                    .fragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_holder, fragment).commit();
        }
    }

}
