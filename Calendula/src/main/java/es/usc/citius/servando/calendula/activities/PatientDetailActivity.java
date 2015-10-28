package es.usc.citius.servando.calendula.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.Screen;
import es.usc.citius.servando.calendula.util.Snack;

public class PatientDetailActivity extends ActionBarActivity implements GridView.OnItemClickListener {

    Toolbar toolbar;
    GridView avatarGrid;
    BaseAdapter adapter;
    Patient patient;

    ImageView patientAvatar;
    View patientAvatarBg;
    RelativeLayout gridContainer;
    TextView selectAvatarMsg;
    View top;
    View bg;
    View shadowTop;
    EditText patientName;
    int[] avatars = AvatarMgr.avatars;

    float avatarSelectorY = -1;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(
                new InsetDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp), 10, 10, 10, 10));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        top = findViewById(R.id.top);
        bg = findViewById(R.id.bg);
        shadowTop = findViewById(R.id.shadow_top);
        selectAvatarMsg = (TextView) findViewById(R.id.select_avatar_message);
        patientAvatar = (ImageView) findViewById(R.id.patient_avatar);
        patientAvatarBg = findViewById(R.id.patient_avatar_bg);
        gridContainer = (RelativeLayout) findViewById(R.id.grid_container);
        patientName = (EditText) findViewById(R.id.patient_name);
        gridContainer.setVisibility(View.GONE);
        findViewById(R.id.close_avatar_selector_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAvatarSelector();
            }
        });

        long patientId = getIntent().getLongExtra("patient_id", -1);

        setupAvatarList();

        if(patientId!=-1){
            loadPatient(patientId);
        }else{
            patient = new Patient();
        }
    }



    private void animateAvatarBg(int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            patientAvatarBg.setVisibility(View.INVISIBLE);
            // get the center for the clipping circle
            int cx = (patientAvatarBg.getLeft() + patientAvatarBg.getRight()) / 2;
            int cy = (patientAvatarBg.getTop() + patientAvatarBg.getBottom()) / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(patientAvatarBg.getWidth(), patientAvatarBg.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(patientAvatarBg, cx, cy, 0, finalRadius);
            // make the view visible and start the animation
            patientAvatarBg.setVisibility(View.VISIBLE);
            anim.setDuration(duration).start();
        }
    }

    void hideAvatarSelector(){
        if(gridContainer.getVisibility()==View.VISIBLE) {
            selectAvatarMsg.setVisibility(View.INVISIBLE);
            shadowTop.setVisibility(View.INVISIBLE);
            gridContainer.animate()
                    .y(avatarSelectorY - gridContainer.getHeight())
                    .alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            gridContainer.setVisibility(View.GONE);
                            gridContainer.setY(avatarSelectorY);
                        }
            });
        }
    }


    void showAvatarSelector(){

            if(avatarSelectorY == -1){
                avatarSelectorY = top.getHeight();
            }
            selectAvatarMsg.setVisibility(View.VISIBLE);
            shadowTop.setVisibility(View.VISIBLE);
            gridContainer.setVisibility(View.VISIBLE);
            gridContainer.setY(avatarSelectorY - gridContainer.getHeight());
            gridContainer.animate().y(avatarSelectorY).alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    gridContainer.setY(avatarSelectorY);
                    gridContainer.setVisibility(View.VISIBLE);
                }
            });
    }

    private void setupAvatarList() {

        avatarGrid = (GridView) findViewById(R.id.grid);
        adapter = new PatientAvatarsAdapter(this);
        avatarGrid.setAdapter(adapter);
        avatarGrid.setOnItemClickListener(this);
        patientAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarSelector();
            }
        });
    }

    private void loadPatient(long patientId) {
        patient = DB.patients().findById(patientId);
        patientName.setText(patient.name());
        updateAvatar(patient.avatar(), 200, 300);
    }

    private void updateAvatar(int avatar, int delay, final int duration){
        int[] color = AvatarMgr.colorsFor(getResources(), avatar);
        patientAvatar.setImageResource(avatar);
        int colorAlpha = Screen.equivalentNoAlpha(color[0], 0.7f);
        top.setBackgroundColor(Screen.equivalentNoAlpha(color[0], 0.4f));
        patientAvatarBg.setBackgroundColor(colorAlpha);
        selectAvatarMsg.setBackgroundColor(colorAlpha);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(colorAlpha);
        }

        if(delay > 0) {
            patientAvatarBg.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateAvatarBg(duration);
                }
            }, delay);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patient_detail, menu);
        this.menu = menu;

        if(patient!=null && DB.patients().isActive(patient,this)){
            menu.getItem(0).getIcon().setAlpha(255);
        }else if(patient!=null && patient.id() == null){
            menu.getItem(0).setVisible(false);
        }else{
            menu.getItem(0).getIcon().setAlpha(100);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;

            case R.id.action_active:
                if(!DB.patients().isActive(patient,this)){
                    DB.patients().setActive(patient,this);
                    item.getIcon().setAlpha(255);
                }else{
                    // Show message: set another active, pls
                }
                return true;

            case R.id.action_done:

                String text = patientName.getText().toString().trim();

                if(!TextUtils.isEmpty(text) && !text.equals(patient.name())){
                    patient.setName(text);
                }

                if(!TextUtils.isEmpty(patient.name())){
                    DB.patients().saveAndFireEvent(patient);
                    supportFinishAfterTransition();
                }else{
                    Snack.show("Indique un nombre, por favor.",this);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int imageResource = avatars[position];
        patient.setAvatar(imageResource);
        updateAvatar(imageResource, 0, 0);
        adapter.notifyDataSetChanged();

    }


    private class PatientAvatarsAdapter extends BaseAdapter {

        private Context context;

        public PatientAvatarsAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return avatars.length;
        }

        @Override
        public Object getItem(int position) {
            return avatars[position];
        }

        @Override
        public long getItemId(int position) {
            return avatars[position];
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ImageView v;
            int resource = avatars[position];
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.avatar_list_item, viewGroup, false);
            }

            v = (ImageView) view;
            v.setImageResource(resource);

            if(resource == patient.avatar()){
                v.setBackgroundResource(R.drawable.avatar_list_item_bg);
            }else{
                v.setBackgroundResource(R.color.transparent);
            }
            return view;

        }
    }


}
