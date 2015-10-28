package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.Screen;

public class PatientsActivity extends ActionBarActivity implements GridView.OnItemClickListener{

    Toolbar toolbar;
    GridView gridView;
    PatientAdapter adapter;
    FloatingActionButton fab;

    List<Patient> patients = Collections.emptyList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey_home));
        }

        setContentView(R.layout.activity_patients);
        patients = DB.patients().findAll();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(
                new InsetDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp), 10, 10, 10, 10));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setTitle("Usuarios");
        fab = (FloatingActionButton) findViewById(R.id.add_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientsActivity.this, PatientDetailActivity.class);
                startActivity(intent);
            }
        });

        gridView = (GridView) findViewById(R.id.grid);
        adapter = new PatientAdapter(this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Patient item = (Patient) parent.getItemAtPosition(position);
                // remove
                return false;
            }
        });

        CalendulaApp.eventBus().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CalendulaApp.eventBus().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patients, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Patient item = (Patient) parent.getItemAtPosition(position);

        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("patient_id", item.id());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            new Pair<>(view.findViewById(R.id.patient_avatar), "transition")
                    );
            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
        } else
            startActivity(intent);
    }


    private class PatientAdapter extends BaseAdapter{


        private Context context;

        public PatientAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return patients.size();
        }

        @Override
        public Object getItem(int position) {
            return patients.get(position);
        }

        @Override
        public long getItemId(int position) {
            return patients.get(position).id();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.activity_patients_item, viewGroup, false);
            }

            final Patient p = (Patient) getItem(position);
            boolean isActive = DB.patients().isActive(p,context);

            ImageView patientAvatar = (ImageView) view.findViewById(R.id.patient_avatar);
            ImageView patientAvatarBg = (ImageView) view.findViewById(R.id.patient_avatar_bg);
            TextView patientName = (TextView) view.findViewById(R.id.patient_name);
            View activeIndicator = view.findViewById(R.id.active_indicator);
            int[] color = AvatarMgr.colorsFor(getResources(), p.avatar());

            patientName.setText(p.name());
            patientName.setBackgroundColor(color[0]);
            patientAvatar.setImageResource(p.avatar());

            if(isActive){
                activeIndicator.setVisibility(View.VISIBLE);
            }else{
                activeIndicator.setVisibility(View.GONE);
            }
            
            int colorAlpha = Screen.equivalentNoAlpha(color[0], 0.7f);
            patientAvatarBg.setBackgroundColor(colorAlpha);
            return view;

        }
    }


    // Method called from the event bus
    @SuppressWarnings("unused")
    public void onEvent(PersistenceEvents.UserEvent event) {
        this.patients = DB.patients().findAll();
        this.adapter.notifyDataSetChanged();
    }


    // Method called from the event bus
    @SuppressWarnings("unused")
    public void onEvent(PersistenceEvents.ActiveUserChangeEvent event) {
        this.adapter.notifyDataSetChanged();
    }


}
