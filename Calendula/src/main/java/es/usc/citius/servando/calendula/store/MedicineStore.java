package es.usc.citius.servando.calendula.store;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.model.Medicine;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class MedicineStore {

    private static final MedicineStore instance = new MedicineStore();
    private List<Medicine> medicines;

    public MedicineStore() {
        medicines = new ArrayList<Medicine>();
    }

    public static MedicineStore getInstance() {
        return instance;
    }

    public void addMedicine(Medicine m) {
        medicines.add(m);
    }

    public void removeMedicine(Medicine m) {
        medicines.remove(m);
    }

    public List<Medicine> getAll() {
        return medicines;
    }

    public Medicine getByName(String name) {
        for (Medicine m : medicines) {
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
        for (int i = 0; i < medicines.size(); i++) {
            names[i] = medicines.get(i).getName();
        }
        return names;
    }
}
