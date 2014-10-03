package es.usc.citius.servando.calendula.store;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import es.usc.citius.servando.calendula.util.GsonUtil;

/**
 * Created by joseangel.pineiro on 7/16/14.
 */
public abstract class Store{

    protected Collection<StoreListener> listeners = new ArrayList<StoreListener>();

    public void addListener(StoreListener l){
        listeners.add(l);
    }

    public void removeListener(StoreListener l){
        listeners.remove(l);
    }

    public void removeListeners(){
        listeners.clear();
    }

    public void notifyDataChange(){
        for(StoreListener l : listeners){
            l.onChange();
        }
    }

}
