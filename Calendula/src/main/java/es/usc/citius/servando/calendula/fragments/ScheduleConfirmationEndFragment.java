package es.usc.citius.servando.calendula.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ConfirmSchedulesActivity;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.medicine.Prescription;


/**
 * Created by joseangel.pineiro on 12/11/13.
 */
public class ScheduleConfirmationEndFragment extends Fragment {

    public static final String TAG = ScheduleConfirmationEndFragment.class.getName();

    List<ConfirmSchedulesActivity.PrescriptionWrapper> prescriptions;

    public static ScheduleConfirmationEndFragment newInstance() {
        ScheduleConfirmationEndFragment fragment = new ScheduleConfirmationEndFragment();
        Bundle args = new Bundle();
        //args.putSerializable(ARG_PRESCRIPTION, pw);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_confirmation_end, container, false);
        prescriptions = ((ConfirmSchedulesActivity) getActivity()).getPrescriptions();

        int newMedsCount = 0;

        for (ConfirmSchedulesActivity.PrescriptionWrapper p : prescriptions) {

            String name = "_";

            if (p.cn != null) {
                if (p.prescription == null) {
                    p.prescription = Prescription.findByCn(p.cn);
                }
                name = p.prescription.shortName();

            } else if (p.isGroup) {
                name = Strings.firstPart(p.group.name);
            }

            if (Medicine.findByName(name) == null) {
                newMedsCount++;
            }
        }

        StringBuffer sb = new StringBuffer();

        //.append(newMedsCount + " schedules will be updated")
        sb.append((prescriptions.size()) + " schedules will be created")
                .append("\n")
                .append(newMedsCount + " meds will be added to your medical kit");

        ((TextView) rootView.findViewById(R.id.textView)).setText(sb.toString());

        return rootView;
    }


}