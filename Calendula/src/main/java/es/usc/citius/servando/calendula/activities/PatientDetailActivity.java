package es.usc.citius.servando.calendula.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

import com.mikepenz.materialize.Materialize;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Snack;

public class PatientDetailActivity extends CalendulaActivity implements GridView.OnItemClickListener {

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
    List<String> avatars = new ArrayList<>(AvatarMgr.avatars.keySet());

    float avatarSelectorY = -1;
    private Menu menu;
    private int avatarBackgroundColor;
    Materialize mt;

    int color1;
    int color2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
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
        if(patientId!=-1){
            patient = DB.patients().findById(patientId);
        }else{
            patient = new Patient();
        }

        setupToolbar(patient.name(), Color.TRANSPARENT);
        setupAvatarList();
        loadPatient();
    }

    @Override
    public void onBackPressed() {
        ScreenUtils.setStatusBarColor(this, color2);
        patientAvatarBg.setVisibility(View.INVISIBLE);
        super.onBackPressed();
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
                    .y(avatarSelectorY - patientAvatarBg.getHeight())
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
                avatarSelectorY = top.getHeight() + ScreenUtils.getStatusBarHeight(this);
            }
            selectAvatarMsg.setVisibility(View.VISIBLE);
            shadowTop.setVisibility(View.VISIBLE);
            gridContainer.setVisibility(View.VISIBLE);
            gridContainer.setY(avatarSelectorY - patientAvatarBg.getHeight());
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

    private void loadPatient() {
        patientName.setText(patient.name());
        updateAvatar(patient.avatar(), 400, 400);
    }

    private void updateAvatar(String avatar, int delay, final int duration){
        int[] color = AvatarMgr.colorsFor(getResources(), avatar);
        patientAvatar.setImageResource(AvatarMgr.res(avatar));

        color1 = ScreenUtils.equivalentNoAlpha(color[0], 0.6f);
        color2 = ScreenUtils.equivalentNoAlpha(color[0], 0.4f);

        avatarBackgroundColor = color1;
        top.setBackgroundColor(color2);
        selectAvatarMsg.setBackgroundColor(avatarBackgroundColor);

        ScreenUtils.setStatusBarColor(this, avatarBackgroundColor);

        if(delay > 0) {
            patientAvatarBg.postDelayed(new Runnable() {
                @Override
                public void run() {
                   patientAvatarBg.setBackgroundColor(avatarBackgroundColor);
                   animateAvatarBg(duration);
                }
            }, delay);
        }else{
            patientAvatarBg.setBackgroundColor(avatarBackgroundColor);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patient_detail, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                ScreenUtils.setStatusBarColor(this, color2);
                patientAvatarBg.setVisibility(View.INVISIBLE);
                supportFinishAfterTransition();
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
        String avatar = avatars.get(position);
        patient.setAvatar(avatar);
        updateAvatar(avatar, 0, 0);
        adapter.notifyDataSetChanged();

    }


    private class PatientAvatarsAdapter extends BaseAdapter {

        private Context context;

        public PatientAvatarsAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return avatars.size();
        }

        @Override
        public Object getItem(int position) {
            return avatars.get(position);
        }

        @Override
        public long getItemId(int position) {
            return avatars.get(position).hashCode();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ImageView v;
            String avatar = avatars.get(position);
            int resource = AvatarMgr.res(avatar);
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.avatar_list_item, viewGroup, false);
            }

            v = (ImageView) view;
            v.setImageResource(resource);

            if(avatar.equals(patient.avatar())){
                v.setBackgroundResource(R.drawable.avatar_list_item_bg);
            }else{
                v.setBackgroundResource(R.color.transparent);
            }
            return view;

        }
    }


}
