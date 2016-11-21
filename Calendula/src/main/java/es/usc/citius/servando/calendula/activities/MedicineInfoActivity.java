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

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.MedInfoPageAdapter;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.IconUtils;

public class MedicineInfoActivity extends CalendulaActivity {

    private static final String TAG = "MedicineInfoActivity";

    private MedInfoPageAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout toolbarLayout;
    TextView toolbarTitle;
    boolean appBarLayoutExpanded = true;
    private Handler handler;

    Patient activePatient;
    Medicine medicine;

    PrescriptionDBMgr dbMgr;

    ImageView medIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_info);
        setupToolbar(null, Color.TRANSPARENT);
        setupStatusBar(Color.TRANSPARENT);
        handler = new Handler();
        activePatient = DB.patients().getActive(this);
        dbMgr = DBRegistry.instance().current();

        processIntent();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new MedInfoPageAdapter(getSupportFragmentManager(), medicine);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        medIcon = (ImageView) findViewById(R.id.medicine_icon);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(getPageChangeListener());
        mViewPager.setOffscreenPageLimit(5);

        toolbarLayout.setContentScrimColor(activePatient.color());
        toolbarLayout.setBackgroundColor(activePatient.color());

        toolbarTitle.setText("Info | " + medicine.name());
        ((TextView)findViewById(R.id.medicine_name)).setText(medicine.name());
        medIcon.setImageDrawable(IconUtils.icon(this,medicine.presentation().icon(), R.color.white));
        // Setup the tabLayout
        setupTabLayout();

        AppBarLayout.OnOffsetChangedListener mListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if((toolbarLayout.getHeight() + verticalOffset) < (1.8 * ViewCompat.getMinimumHeight(toolbarLayout))) {
                    toolbarTitle.animate().alpha(1);
                    Log.d(TAG, "OnCollapse");
                } else {
                    toolbarTitle.animate().alpha(0);
                    Log.d(TAG, "OnExpand");
                }



            }
        };
        appBarLayout.addOnOffsetChangedListener(mListener);
        toolbarTitle.animate().alpha(0);
   }

    private void processIntent() {

        long medId = getIntent() != null ? getIntent().getLongExtra("medicine_id", -1) : -1;

        if(medId != -1 ){
            medicine = DB.medicines().findById(medId);
        }
        if(medicine == null){
            Toast.makeText(MedicineInfoActivity.this, "Medicine not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void setupTabLayout(){

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        IIcon[] icons = new IIcon[]{
                CommunityMaterial.Icon.cmd_book_open,
                CommunityMaterial.Icon.cmd_comment_alert_outline
        } ;

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            Drawable icon = new IconicsDrawable(this)
                    .icon(icons[i])
                    .alpha(80)
                    .paddingDp(2)
                    .color(Color.WHITE)
                    .sizeDp(24);

            tabLayout.getTabAt(i).setIcon(icon);
            if(i == 1){

            }

        }
    }




    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private ViewPager.OnPageChangeListener getPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(medicine!= null){
                    if(position == 0){
                        toolbarTitle.setText("Info | " + medicine.name());
                    }else if(position ==1){
                        toolbarTitle.setText("Avisos | " + medicine.name());
                    }

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }

}
