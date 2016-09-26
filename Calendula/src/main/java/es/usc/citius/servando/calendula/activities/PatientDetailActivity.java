/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nispok.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.DefaultDataGenerator;
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
//    TextView selectAvatarMsg;
    View top;
    View bg;
    EditText patientName;
    List<String> avatars = new ArrayList<>(AvatarMgr.avatars.keySet());

    private Menu menu;
    private int avatarBackgroundColor;

    FloatingActionButton fab;

    int color1;
    int color2;

    Drawable iconClose;
    Drawable iconSwich;

    CheckBox addRoutinesCheckBox;
    LinearLayout colorList;
    HorizontalScrollView colorScroll;
    Button linkButton;

    boolean linked = false;
    String token = null;
    long patientId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
        top = findViewById(R.id.top);
        bg = findViewById(R.id.bg);
        patientAvatar = (ImageView) findViewById(R.id.patient_avatar);
        patientAvatarBg = findViewById(R.id.patient_avatar_bg);
        gridContainer = (RelativeLayout) findViewById(R.id.grid_container);
        patientName = (EditText) findViewById(R.id.patient_name);
        fab = (FloatingActionButton) findViewById(R.id.avatar_change);
        avatarGrid = (GridView) findViewById(R.id.grid);
        addRoutinesCheckBox = (CheckBox) findViewById(R.id.checkBox);
        colorScroll = (HorizontalScrollView) findViewById(R.id.colorScroll);
        linkButton = (Button) findViewById(R.id.linkButton);

        avatarGrid.setVisibility(View.VISIBLE);
        gridContainer.setVisibility(View.GONE);

        patientId = getIntent().getLongExtra("patient_id", -1);

        lookForQrData(getIntent());

        if(patientId!=-1){
            patient = DB.patients().findById(patientId);
            addRoutinesCheckBox.setVisibility(View.GONE);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            token = prefs.getString("remote_token" + patientId, null);
            if(token != null){
                linkButton.setVisibility(View.VISIBLE);
                linkButton.setText("Desvincular");

                linkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUnlinkPatientDialog();
                    }
                });

            }else{
                linkButton.setVisibility(View.GONE);
            }



        }else{
            linkButton.setVisibility(View.GONE);
            patient = new Patient();
        }

        iconClose = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_close)
                .sizeDp(24)
                .paddingDp(5)
                .colorRes(R.color.dark_grey_home);

        iconSwich = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_account_switch)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(R.color.fab_light_normal);



        setSwichFab();
        setupToolbar(patient.name(), Color.TRANSPARENT);
        setupStatusBar(Color.TRANSPARENT);
        setupAvatarList();
        setupColorChooser();
        loadPatient();
    }

    void lookForQrData(Intent i){
        String qrData = i.getStringExtra("qr_data");
        if(qrData!=null){
            PatientLinkWrapper p = new Gson().fromJson(qrData, PatientLinkWrapper.class);
            Snack.show("Usuario vinculado correctamente!", this, Snackbar.SnackbarDuration.LENGTH_LONG);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PatientDetailActivity.this);
            prefs.edit().putString("remote_token" + patientId, p.token).commit();
            Log.d("PatDetail", p.toString());
        }
    }

    private void showUnlinkPatientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Si desvinculas este usuario se interrumpirá el seguimiento. Estás seguro de que deseas continuar?")
                .setCancelable(true)
                .setTitle("Ten cuidado")
                .setPositiveButton("Si, desvincular", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        token = null;
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PatientDetailActivity.this);
                        prefs.edit().remove("remote_token" + patientId).commit();
                        linkButton.setText("Vincular");
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startScanActivity(){
        Intent i = new Intent(this, ScanActivity.class);
        i.putExtra("after_scan_pkg", getPackageName());
        i.putExtra("after_scan_cls", PatientDetailActivity.class.getName());
        i.putExtra("patient_id", patientId);
        this.startActivity(i);
        this.overridePendingTransition(0, 0);
        finish();
    }

    private void setupColorChooser() {

        colorList = (LinearLayout) findViewById(R.id.color_chooser);
        colorList.removeAllViews();

        for(final String hex : COLORS){
            ImageView colorView = (ImageView) getLayoutInflater().inflate(R.layout.color_chooser_item, null);
            final int color = Color.parseColor(hex);
            colorView.setBackgroundColor(color);
            colorView.setPadding(2, 2, 2, 2);
            if(color == patient.color()) {
                colorView.setImageDrawable(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_checkbox_marked_circle)
                        .paddingDp(30)
                        .color(Color.WHITE)
                        .sizeDp(80));
            }else {
                colorView.setImageDrawable(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_checkbox_marked_circle)
                        .paddingDp(30)
                        .color(Color.TRANSPARENT)
                        .sizeDp(80));
            }
            colorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    patient.setColor(color);
                    setupColorChooser();
                    int x = (int) view.getX() + view.getWidth()/2 - colorScroll.getScrollX();
                    updateAvatar(patient.avatar(), 1, 400, x);
                    colorList.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollToColor(color);
                        }
                    },300);
                    //Toast.makeText(getBaseContext(), "Color: " + hex, Toast.LENGTH_SHORT).show();
                }
            });
            colorList.addView(colorView);
        }
    }

    private void scrollToColor(int color){
        int index = 0;
        for(int i = 0; i < COLORS.length; i++){
            if(color == Color.parseColor(COLORS[i])){
                index = i;
                break;
            }
        }
        int width = colorList.getChildAt(0).getWidth();
        colorScroll.smoothScrollTo(width*index + width/2 - colorScroll.getWidth()/2 ,0);
    }


    void setSwichFab(){
        fab.setColorNormalResId(R.color.fab_dark_normal);
        fab.setColorPressedResId(R.color.fab_dark_pressed);
        fab.setIconDrawable(iconSwich);

    }

    void setCloseFab(){
        fab.setColorNormalResId(R.color.fab_light_normal);
        fab.setColorPressedResId(R.color.fab_light_pressed);
        fab.setIconDrawable(iconClose);
    }

    @Override
    public void onBackPressed() {
        //ScreenUtils.setStatusBarColor(this, color2);
        patientAvatarBg.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }


    private void animateAvatarSelectorShow(int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gridContainer.setVisibility(View.INVISIBLE);
            // get the center for the clipping circle
            int cx = (int) fab.getX() + fab.getWidth()/2;
            int cy = 0;
            // get the final radius for the clipping circle
            int finalRadius = (int) Math.hypot(patientAvatarBg.getWidth(), bg.getHeight() - patientAvatarBg.getHeight());
            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(gridContainer, cx, cy, 0, finalRadius);
            anim.setInterpolator(new DecelerateInterpolator());
            // make the view visible and start the animation
            gridContainer.setVisibility(View.VISIBLE);
            anim.setDuration(duration).start();
            gridContainer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollToColor(patient.color());
                }
            },duration);
        }
    }

    private void animateAvatarSelectorHide(int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // get the center for the clipping circle
            int cx = (int) fab.getX() + fab.getWidth()/2;
            int cy = 0;
            // get the final radius for the clipping circle
            int finalRadius = (int) Math.hypot(patientAvatarBg.getWidth(), bg.getHeight() - patientAvatarBg.getHeight());
            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(gridContainer, cx, cy, finalRadius, 0);
            // make the view visible and start the animation
            anim.setInterpolator(new AccelerateInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    gridContainer.setVisibility(View.GONE);
                    setSwichFab();
                    super.onAnimationEnd(animation);
                }
            });
            anim.setDuration(duration).start();
        }
    }





    private void animateAvatarBg(int duration, int x, Animator.AnimatorListener cb) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            patientAvatarBg.setVisibility(View.INVISIBLE);
            // get the center for the clipping circle
            int cx = (patientAvatarBg.getLeft() + patientAvatarBg.getRight()) / 2;
            int cy = patientAvatarBg.getBottom();

            // get the final radius for the clipping circle
            int finalRadius = (int)Math.hypot(patientAvatarBg.getWidth(), patientAvatarBg.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(patientAvatarBg, x, cy, ScreenUtils.dpToPx(getResources(), 100f), finalRadius);
            // make the view visible and start the animation
            patientAvatarBg.setVisibility(View.VISIBLE);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(duration);
            if(cb!=null){
                anim.addListener(cb);
            }
            anim.start();
        }
    }

    void hideAvatarSelector(){
        animateAvatarSelectorHide(200);
    }


    void showAvatarSelector(){
        setCloseFab();
        animateAvatarSelectorShow(300);
    }

    private void setupAvatarList() {
        adapter = new PatientAvatarsAdapter(this);
        avatarGrid.setAdapter(adapter);
        avatarGrid.setOnItemClickListener(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gridContainer.getVisibility() == View.VISIBLE)
                    hideAvatarSelector();
                else
                    showAvatarSelector();
            }
        });
    }

    private void loadPatient() {
        patientName.setText(patient.name());
        top.setBackgroundColor(patient.color());
        updateAvatar(patient.avatar(), 400, 400, patientAvatar.getWidth()/2);
    }

    private void updateAvatar(String avatar, int delay, final int duration, final int x){
        patientAvatar.setImageResource(AvatarMgr.res(avatar));
        color1 = patient.color();
        color2 = ScreenUtils.equivalentNoAlpha(color1, 0.7f);
        avatarBackgroundColor = color1;
        gridContainer.setBackgroundColor(getResources().getColor(R.color.dark_grey_home));
        //ScreenUtils.setStatusBarColor(this, avatarBackgroundColor);

        if(delay > 0) {
            patientAvatarBg.postDelayed(new Runnable() {
                @Override
                public void run() {
                   patientAvatarBg.setBackgroundColor(avatarBackgroundColor);
                   animateAvatarBg(duration, x, new AnimatorListenerAdapter() {
                       @Override
                       public void onAnimationEnd(Animator animation) {
                           top.setBackgroundColor(avatarBackgroundColor);
                       }
                   });
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

        //if(token != null){
        this.menu.getItem(0).setVisible(false);
        //}

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //ScreenUtils.setStatusBarColor(this, color2);
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
                    if(addRoutinesCheckBox.isChecked()){
                        DefaultDataGenerator.generateDefaultRoutines(patient, this);
                    }
                    supportFinishAfterTransition();
                }else{
                    Snack.show("Indique un nombre, por favor.", this);
                }
                return true;

            case R.id.action_link_qr:
                if(token == null) {
                    startScanActivity();
                }else{
                    showUnlinkPatientDialog();
                }
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String avatar = avatars.get(position);
        patient.setAvatar(avatar);
        updateAvatar(avatar, 0, 0, patientAvatar.getWidth()/2);
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

            v = (ImageView) view.findViewById(R.id.imageView);
            v.setImageResource(resource);

            if(avatar.equals(patient.avatar())){
                v.setBackgroundResource(R.drawable.avatar_list_item_bg);
            }else{
                v.setBackgroundResource(R.color.transparent);
            }
            return view;
        }
    }

    public class PatientLinkWrapper{
        public String name;
        public String id;
        public String token;

        @Override
        public String toString() {
            return "PatientLinkWrapper{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", token='" + token + '\'' +
                    '}';
        }
    }


    public static final String[] COLORS = new String[]{

            "#1abc9c",
            "#16a085",
            "#f1c40f",
            "#f39c12",
            "#2ecc71",
            "#27ae60",
            "#e67e22",
            "#d35400",
            "#c0392b",
            "#e74c3c",
            "#2980b9",
            "#3498db",
            "#9b59b6",
            "#8e44ad",
            "#2c3e50",
            "#34495e"


//            "#f44336",
//            "#e91e63",
//            "#9c27b0",
//            "#673ab7",
//            "#3f51b5",
//            "#1976d2",
//            "#2196f3",
//            "#03a9f4",
//            "#00bcd4",
//            "#009688",
//            "#4caf50",
//            "#8bc34a",
//            "#cddc39",
//            "#ffeb3b",
//            "#ffc107",
//            "#ff9800",
//            "#ff5722",
//            "#795548",
//            "#9e9e9e",
//            "#607d8b"
    };


}
