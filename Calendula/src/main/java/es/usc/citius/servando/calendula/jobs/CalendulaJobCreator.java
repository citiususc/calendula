package es.usc.citius.servando.calendula.jobs;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by alvaro.brey.vilas on 17/11/16.
 */

public class CalendulaJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case PurgeCacheJob.TAG:
                return new PurgeCacheJob();
            default:
                return null;
        }
    }
}
