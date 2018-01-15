/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.allergies.AllergenFacade;
import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.allergies.AllergyAlertUtil;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.MedicineCreateOrEditFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.DrivingCautionAlert;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;

public class MedicinesActivity extends CalendulaActivity implements MedicineCreateOrEditFragment.OnMedicineEditListener {

    public static final String EXTRA_SEARCH_TEXT = "MedicinesActivity.extras.SEARCH_TEXT";
    private static final String TAG = "MedicinesActivity";
    private static final int REQUEST_CODE_GET_MED = 1314;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    @BindView(R.id.pager)
    ViewPager mViewPager;
    String qrData;
    Long mMedicineId;
    MenuItem removeItem;
    @BindView(R.id.add_button)
    FloatingActionButton fab;
    int color;

    private Prescription prescriptionToSet = null;
    private String prescriptionNameToSet = null;
    private String intentAction;
    private String intentSearchText = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.medicines, menu);
        removeItem = menu.findItem(R.id.action_remove);
        removeItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                //noop
                return true;
            default:
                finish();
                return true;
        }
    }

    @Override
    public void onMedicineEdited(final Medicine m) {
        // check for allergies
        if (m.isBoundToPrescription()) {
            final List<AllergenVO> vos = AllergenFacade.checkAllergies(this, DB.drugDB().prescriptions().findByCn(m.getCn()));
            if (!vos.isEmpty()) {
                showAllergyDialog(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        updateMedicine(m, vos);
                    }
                });
            } else {
                updateMedicine(m, null);
            }
        } else {
            updateMedicine(m, null);
        }
    }

    @Override
    public void onMedicineCreated(final Medicine m) {

        // check for allergies
        if (m.isBoundToPrescription()) {
            final List<AllergenVO> vos = AllergenFacade.checkAllergies(this, DB.drugDB().prescriptions().findByCn(m.getCn()));
            if (!vos.isEmpty()) {
                showAllergyDialog(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        createMedicine(m, vos);
                    }
                });
            } else {
                createMedicine(m, null);
            }
        } else {
            createMedicine(m, null);
        }
    }

    @Override
    public void onMedicineDeleted(Medicine m) {
        Toast.makeText(this, getString(R.string.medicine_deleted_message), Toast.LENGTH_SHORT).show();
        DB.medicines().deleteCascade(m, true);
        finish();
    }

    public void showSearchView(@Nullable final String searchText) {
        LogUtil.d(TAG, "showSearchView() called with: searchText = [" + searchText + "]");
        Intent i = new Intent(this, MedicinesSearchActivity.class);
        i.putExtra(MedicinesSearchActivity.EXTRA_SEARCH_TERM, searchText);
        startActivityForResult(i, REQUEST_CODE_GET_MED);
    }


    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines);
        ButterKnife.bind(this);

        color = DB.patients().getActive(this).getColor();
        setupToolbar(null, color);
        setupStatusBar(color);

        processIntent();

        TextView title = ((TextView) findViewById(R.id.textView2));
        if (mMedicineId != -1) {
            title.setText(getString(R.string.edit_medicine));
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).onEdit();
            }
        });

        title.setBackgroundColor(color);

        if (mMedicineId == -1 || intentSearchText != null) {
            mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    showSearchView(intentSearchText);
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (requestCode == REQUEST_CODE_GET_MED) {
            if (resultCode == RESULT_OK) {
                final String prescriptionName = data.getStringExtra(MedicinesSearchActivity.RETURN_EXTRA_PRESCRIPTION_NAME);
                if (prescriptionName != null) {
                    ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).setMedicineName(prescriptionName);
                } else {
                    final Prescription p = data.getParcelableExtra(MedicinesSearchActivity.RETURN_EXTRA_PRESCRIPTION);
                    if (p != null) {
                        ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).setPrescription(p);
                    } else {
                        LogUtil.e(TAG, "onActivityResult: result was OK but no prescription extras received ");
                    }
                }
            }
        } else {
            LogUtil.w(TAG, "onActivityResult: invalid request code " + requestCode + ", ignoring");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void processIntent() {
        mMedicineId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, -1);
        intentAction = getIntent().getStringExtra(CalendulaApp.INTENT_EXTRA_ACTION);
        intentSearchText = getIntent().getStringExtra(EXTRA_SEARCH_TEXT);
        qrData = getIntent().getStringExtra("qr_data");
    }

    private void showAllergyDialog(final MaterialDialog.SingleButtonCallback onOk) {
        new MaterialStyledDialog.Builder(this)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_exclamation, R.color.white, 100))
                .setHeaderColor(R.color.android_red)
                .withDialogAnimation(true)
                .setTitle(R.string.title_medicine_allergy_alert)
                .setDescription(R.string.message_medicine_alert)
                .setCancelable(true)
                .setNegativeText(R.string.cancel)
                .setPositiveText(getString(R.string.ok))
                .onPositive(onOk)
                .show();
    }

    private void updateMedicine(final Medicine m, final List<AllergenVO> allergies) {
        try {
            if (allergies != null) {
                createAllergyAlerts(m, allergies);
            }
            removeOldAlerts(m);
            DB.medicines().saveAndFireEvent(m);
            Toast.makeText(this, getString(R.string.medicine_edited_message), Toast.LENGTH_SHORT).show();
            finish();
        } catch (RuntimeException | SQLException e) {
            Snack.show(R.string.medicine_save_error_message, this);
            LogUtil.e(TAG, "updateMedicine: ", e);
        }
    }

    /**
     * Removes obsolete alerts for a medicine that is going to be updated.
     * This MUST be called before calling the persistence method.
     *
     * @param m the medicine
     */
    private void removeOldAlerts(final Medicine m) throws SQLException {
        Medicine old = DB.medicines().findById(m.getId());
        if (m.isBoundToPrescription() && !m.getCn().equals(old.getCn())) { // if prescription didn't change, don't check for alerts
            AllergyAlertUtil.removeAllergyAlerts(m);
        }
    }

    private void createMedicine(final Medicine m, final List<AllergenVO> allergies) {
        try {
            DB.transaction(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    DB.medicines().save(m);
                    if (allergies != null) {
                        createAllergyAlerts(m, allergies);
                    }

                    if (m.isBoundToPrescription()) {
                        Prescription p = DB.drugDB().prescriptions().findByCn(m.getCn());
                        if (p.isAffectsDriving()) {
                            AlertManager.createAlert(new DrivingCautionAlert(m));
                        }
                    }

                    DB.medicines().fireEvent();
                    return null;
                }
            });

            CalendulaApp.eventBus().post(new PersistenceEvents.MedicineAddedEvent(m.getId()));
            Toast.makeText(this, getString(R.string.medicine_created_message), Toast.LENGTH_SHORT).show();
            finish();
        } catch (RuntimeException e) {
            Snack.show(R.string.medicine_save_error_message, this);
            LogUtil.e(TAG, "createMedicine: ", e);
        }
    }

    /**
     * Creates or updates allergy alert for a medicine with te given allergens.
     *
     * @param m         the medicine
     * @param allergies the allergens
     * @return <code>true</code> if changes have been made, <code>false</code> otherwise (error or no allergy changes)
     */
    private boolean createAllergyAlerts(final Medicine m, final List<AllergenVO> allergies) {

        final List<PatientAlert> allergyAlerts = DB.alerts().findByMedicineAndType(m, new AllergyPatientAlert().getType());
        switch (allergyAlerts.size()) {
            case 0:
                AllergyPatientAlert alert = new AllergyPatientAlert(m, allergies);
                AlertManager.createAlert(alert);
                LogUtil.d(TAG, "createAllergyAlerts: New alert created");
                return true;
            case 1:
                AllergyPatientAlert alert1 = (AllergyPatientAlert) allergyAlerts.get(0).map();
                final AllergyPatientAlert.AllergyAlertInfo details = alert1.getDetails();
                final Set<AllergenVO> previousAllergens = new HashSet<>(details.getAllergens());
                final Set<AllergenVO> allergiesSet = new HashSet<>(allergies);
                if (allergiesSet.equals(previousAllergens)) {
                    //do nothing
                    return false;
                } else {
                    details.setAllergens(allergies);
                    alert1.setDetails(details);
                    try {
                        DB.alerts().update(alert1);
                        return true;
                    } catch (SQLException e) {
                        LogUtil.e(TAG, "createAllergyAlerts: ", e);
                        return false;
                    }
                }
            default:
                LogUtil.e(TAG, "createAllergyAlerts: more than 1 allergy alert for a medicine!");
                return false;
        }

    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment f = new MedicineCreateOrEditFragment();
            Bundle args = new Bundle();
            args.putLong(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, mMedicineId);
            args.putString(CalendulaApp.INTENT_EXTRA_ACTION, intentAction);
            f.setArguments(args);
            return f;
        }

        @Override
        public int getCount() {
            // Show 1 total pages.
            return 1;
        }

    }

}
