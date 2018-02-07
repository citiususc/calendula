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

package es.usc.citius.servando.calendula.drugdb.model.database;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.util.LogUtil;


public class DrugDBModule {


    private static final String TAG = "DrugDBModule";
    private static DrugDBModule instance = null;
    // Active ingredients DAO
    private final ActiveIngredientDAO ActiveIngredients;
    // Content unit DAO
    private final ContentUnitDAO ContentUnits;
    // Excipient DAO
    private final ExcipientDAO Excipients;
    // Homogeneous groups
    private final HomogeneousGroupDAO HomogeneousGroups;
    // Package types
    private final PackageTypeDAO PackageTypes;
    // Prescription active ingredients
    private final PrescriptionActiveIngredientDAO PrescriptionActiveIngredients;
    // Prescriptions
    private final PrescriptionDAO Prescriptions;
    // Prescription excipients
    private final PrescriptionExcipientDAO PrescriptionExcipients;
    // Presentation forms
    private final PresentationFormDAO PresentationForms;

    private DrugDBModule(final DatabaseHelper db) {
        ActiveIngredients = new ActiveIngredientDAO(db);
        ContentUnits = new ContentUnitDAO(db);
        Excipients = new ExcipientDAO(db);
        HomogeneousGroups = new HomogeneousGroupDAO(db);
        PackageTypes = new PackageTypeDAO(db);
        PrescriptionActiveIngredients = new PrescriptionActiveIngredientDAO(db);
        Prescriptions = new PrescriptionDAO(db);
        PrescriptionExcipients = new PrescriptionExcipientDAO(db);
        PresentationForms = new PresentationFormDAO(db);
        LogUtil.v(TAG, "Drug DB Module initialized");
    }

    public static DrugDBModule getInstance() {
        if (instance == null) {
            instance = new DrugDBModule(DB.helper());
        }
        return instance;
    }

    public ActiveIngredientDAO activeIngredients() {
        return ActiveIngredients;
    }

    public ContentUnitDAO contentUnits() {
        return ContentUnits;
    }

    public ExcipientDAO excipients() {
        return Excipients;
    }

    public HomogeneousGroupDAO homogeneousGroups() {
        return HomogeneousGroups;
    }

    public PackageTypeDAO packageTypes() {
        return PackageTypes;
    }

    public PrescriptionActiveIngredientDAO prescriptionActiveIngredients() {
        return PrescriptionActiveIngredients;
    }

    public PrescriptionDAO prescriptions() {
        return Prescriptions;
    }

    public PrescriptionExcipientDAO prescriptionExcipients() {
        return PrescriptionExcipients;
    }

    public PresentationFormDAO presentationForms() {
        return PresentationForms;
    }
}
