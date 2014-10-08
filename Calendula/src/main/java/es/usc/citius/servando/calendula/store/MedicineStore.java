package es.usc.citius.servando.calendula.store;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.util.GsonUtil;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class MedicineStore extends Store {

    public static final String TAG = MedicineStore.class.getName();
    private static final String MEDS_FILE_NAME = "medicines.json";

    private static final MedicineStore instance = new MedicineStore();

    private Map<String, Medicine> medicines;

    public MedicineStore() {
        medicines = new HashMap<String, Medicine>();
    }

    public static MedicineStore instance() {
        return instance;
    }

    public void addMedicine(Medicine m) {
        if (!medicines.containsKey(m.getId())) {
            medicines.put(m.getId(), m);
            notifyDataChange();
        }
    }

    public void removeMedicine(Medicine m) {

        medicines.remove(m.getId());
        Log.d(TAG, "Remove medicine " + m.getName() + ", size:  " + medicines.size());
        notifyDataChange();
    }

    public Medicine get(String id) {
        return medicines.get(id);
    }

    public List<Medicine> getAll() {
        Log.d(TAG, "Get all, size:  " + medicines.size());
        return new ArrayList<Medicine>(medicines.values());
    }

    public Medicine getByName(String name) {
        for (Medicine m : medicines.values()) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public int size() {
        return medicines.size();
    }

    public String[] getMedicineNames() {
        String names[] = new String[medicines.size()];
        List<Medicine> values = getAll();
        for (int i = 0; i < values.size(); i++) {
            names[i] = values.get(i).getName();
        }
        return names;
    }

    public void save(Context context) {
        // open session file where user data is stored
        FileOutputStream out = null;
        try {
            out = context.openFileOutput(MEDS_FILE_NAME, Context.MODE_PRIVATE);
            String json = GsonUtil.get().toJson(medicines);
            out.write(json.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error saving medicines", e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                // do nothing
            }
        }

    }

    public void load(Context context) throws Exception {
        // open session file where user data is stored
        FileInputStream is = null;
        try {
            is = context.openFileInput(MEDS_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            HashMap<String, Medicine> medicines = GsonUtil.get().fromJson(reader, new TypeToken<HashMap<String, Medicine>>() {
            }.getType());
            if (medicines != null) {
                this.medicines = medicines;
            }
            Log.d(TAG, "Medicines loaded (" + medicines.size() + ")");
            Log.d(TAG, GsonUtil.get().toJson(this.medicines));
        } catch (Exception e) {
            medicines = new HashMap<String, Medicine>();
            Log.e(ScheduleStore.class.getName(), "Error reading routines file", e);
        } finally {
            try {
                is.close();
            } catch (Exception unhandled) {
                //do nothing
            }
        }
    }
}
