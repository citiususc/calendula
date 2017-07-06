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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Snack;

public class PatientsActivity extends CalendulaActivity implements GridView.OnItemClickListener {

    GridView gridView;
    PatientAdapter adapter;
    FloatingActionButton fab;

    List<Patient> patients = Collections.emptyList();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Patient item = (Patient) parent.getItemAtPosition(position);

        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("patient_id", item.id());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    new Pair<>(view.findViewById(R.id.patient_avatar), "transition"),
                    new Pair<>(findViewById(R.id.add_button), "fab")
            );
            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
        } else
            startActivity(intent);
    }

    // Method called from the event bus
    @Subscribe
    public void handleUserCreation(final PersistenceEvents.UserCreateEvent event) {
        this.patients = DB.patients().findAll();
        this.adapter.notifyDataSetChanged();
    }

    // Method called from the event bus
    @Subscribe
    public void handleActiveUserChange(final PersistenceEvents.ActiveUserChangeEvent event) {
        this.adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);
        setupStatusBar(getResources().getColor(R.color.dark_grey_home));
        setupToolbar(getString(R.string.title_activity_patients), getResources().getColor(R.color.dark_grey_home));
        subscribeToEvents();

        //patients = DB.patients().findAll();
        fab = (FloatingActionButton) findViewById(R.id.add_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientsActivity.this, PatientDetailActivity.class);
                startActivity(intent);
            }
        });
        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_plus)
                .paddingDp(5)
                .sizeDp(24)
                .colorRes(R.color.fab_default_icon_color));

        gridView = (GridView) findViewById(R.id.grid);
        adapter = new PatientAdapter(this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Patient p = (Patient) parent.getItemAtPosition(position);
                if (!p.isDefault())
                    showRemovePatientDialog(p);
                else
                    showRemoveDefaultPatientMsgDialog(p);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyDataChange();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribeFromEvents();
    }

    private void showRemoveDefaultPatientMsgDialog(final Patient p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.message_delete_default_patient)
                .setCancelable(true)
                .setPositiveButton(R.string.yes_delete_everything, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DB.patients().removeAllStuff(p);
                        Snack.show(R.string.default_user_data_deleted, PatientsActivity.this);
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

    private void showRemovePatientDialog(final Patient p) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Deseas eliminar el paciente " + p.name() + "?")
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (DB.patients().isActive(p, getApplicationContext())) {
                            DB.patients().setActive(DB.patients().getDefault());
                        }
                        DB.patients().removeCascade(p);
                        notifyDataChange();
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

    private void notifyDataChange() {
        patients = DB.patients().findAll();
        adapter.notifyDataSetChanged();
    }

    private class PatientAdapter extends BaseAdapter {


        private static final String TAG = "PatientAdapter";
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

            LogUtil.d(TAG, p.toString());

            boolean isActive = DB.patients().isActive(p, context);

            ImageView patientAvatar = (ImageView) view.findViewById(R.id.patient_avatar);
            ImageView patientAvatarBg = (ImageView) view.findViewById(R.id.patient_avatar_bg);
            TextView patientName = (TextView) view.findViewById(R.id.patient_name);
            ImageView lockIcon = (ImageView) view.findViewById(R.id.lock_icon);
            View activeIndicator = view.findViewById(R.id.active_indicator);
            int color = p.color();

            patientName.setText(p.name());
            patientName.setBackgroundColor(color);
            patientAvatar.setImageResource(AvatarMgr.res(p.avatar()));

            if (isActive) {
                activeIndicator.setVisibility(View.VISIBLE);
            } else {
                activeIndicator.setVisibility(View.GONE);
            }

            if (p.isDefault()) {
                lockIcon.setImageDrawable(new IconicsDrawable(getApplicationContext(), CommunityMaterial.Icon.cmd_lock)
                        .sizeDp(20)
                        .paddingDp(5)
                        .color(Color.WHITE));
            } else {
                lockIcon.setVisibility(View.GONE);
            }

            int colorAlpha = ScreenUtils.equivalentNoAlpha(color, 0.4f);
            patientAvatarBg.setBackgroundColor(colorAlpha);
            return view;

        }
    }


}
