package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Persistence;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.medicine.Prescription;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class MedicinesListFragment extends Fragment {

    public static final String PROSPECT_URL = "http://www.aemps.gob.es/cima/pdfs/es/p/#ID#/P_#ID#.pdf";

    List<Medicine> mMedicines;
    OnMedicineSelectedListener mMedicineSelectedCallback;
    ArrayAdapter adapter;
    ListView listview;
    long prospectDowloadRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medicines_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.medicines_list);
        mMedicines = Medicine.findAll();
        adapter = new MedicinesListAdapter(getActivity(), R.layout.medicines_list_item, mMedicines);
        listview.setAdapter(adapter);        
        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        getActivity().registerReceiver(onNotificationClick,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(onComplete);
        getActivity().unregisterReceiver(onNotificationClick);
        super.onDestroy();
    }

    public void notifyDataChange() {
        Log.d(getTag(), "Medicines - Notify data change");
        new ReloadItemsTask().execute();
    }

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mMedicines = Medicine.findAll();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.clear();
            for (Medicine m : mMedicines) {
                adapter.add(m);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private View createMedicineListItem(LayoutInflater inflater, final Medicine medicine) {

        View item = inflater.inflate(R.layout.medicines_list_item, null);

        ((TextView) item.findViewById(R.id.medicines_list_item_name)).setText(medicine.name());

        ImageView icon = (ImageView) item.findViewById(R.id.imageButton);
        icon.setImageDrawable(getResources().getDrawable(medicine.presentation().getDrawable()));

        View overlay = item.findViewById(R.id.medicines_list_item_container);
        overlay.setTag(medicine);

        String cn = medicine.cn();
        final Prescription p = cn != null ? Prescription.findByCn(medicine.cn()) : null;
        boolean hasProspect = (p != null && p.hasProspect);

        if (hasProspect) {
                if (p.isProspectDownloaded(getActivity())) {
                    item.findViewById(R.id.download_indicator).setVisibility(View.GONE);
                    item.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openProspect(p);
                        }
                    });
                } else {
                    item.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickProspect(medicine, p);
                        }
                    });
                }
            } else {
                item.findViewById(R.id.imageView).setAlpha(0.1f);
                item.findViewById(R.id.download_indicator).setVisibility(View.GONE);
            }


        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Medicine m = (Medicine) view.getTag();
                if (mMedicineSelectedCallback != null && m != null) {
                    Log.d(getTag(), "Click at " + m.name());
                    mMedicineSelectedCallback.onMedicineSelected(m);
                } else {
                    Log.d(getTag(), "No callback set");
                }
            }
        };

        overlay.setOnClickListener(clickListener);
        overlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (view.getTag() != null)
                    showDeleteConfirmationDialog((Medicine) view.getTag());
                return true;
            }
        });
        return item;
    }

    private void openProspect(Prescription p) {
        File f = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/prospects/" + p.pid + ".pdf");
        File file = new File(f.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }


    void onClickProspect(Medicine medicine, final Prescription p) {
        try {
            if (p != null) {
                final String purl = PROSPECT_URL.replaceAll("#ID#", p.pid);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.download_prospect_title));
                builder.setMessage(getString(R.string.download_prospect_message, p.shortName()))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.download_prospect_continue), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                downloadProspect(p, purl);
//                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(purl));
//                                startActivity(browserIntent);
//                                Log.d("MedicinesList", purl);
                            }
                        })
                        .setNegativeButton(getString(R.string.download_prospect_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();


            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.download_prospect_message))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.download_prospect_continue), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                Log.d("MedicinesList", "Prospect url not available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void showDeleteConfirmationDialog(final Medicine m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(String.format(getString(R.string.remove_medicine_message_short), m.name()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Persistence.instance().deleteCascade(m);
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

    private class MedicinesListAdapter extends ArrayAdapter<Medicine> {

        public MedicinesListAdapter(Context context, int layoutResourceId, List<Medicine> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            return createMedicineListItem(layoutInflater, mMedicines.get(position));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // If the container activity has implemented the callback interface, set it as listener
        if (activity instanceof OnMedicineSelectedListener) {
            mMedicineSelectedCallback = (OnMedicineSelectedListener) activity;
        }
    }


    public void downloadProspect(Prescription p, String uri) {

        File prospects = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/prospects/");
        prospects.mkdirs();
        
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(uri));

        Log.d("MedicinesListFragment.class", "Downloading prospect from  [" + uri + "]");

        r.setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, "prospects/" + p.pid + ".pdf");
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        r.setVisibleInDownloadsUi(false);
        r.setTitle(p.shortName() + " prospect");
        // Start download
        DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        prospectDowloadRef = dm.enqueue(r);
    }
    

    //
    // Container Activity must implement this interface
    //
    public interface OnMedicineSelectedListener {
        public void onMedicineSelected(Medicine m);
        public void onCreateMedicine();
    }


    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            adapter.notifyDataSetChanged();
            Snack.show(getString(R.string.prescriptions_download_complete), getActivity());
        }
    };
    BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

        }
    };

}