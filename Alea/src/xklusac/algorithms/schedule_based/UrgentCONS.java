/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.schedule_based;

import gridsim.GridSim;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.extensions.EndTimeComparator;
import xklusac.extensions.SchedulingEvent;
import xklusac.extensions.UrgentFlagComparator;

/**
 * Class CONS<p> extends CONS (Conservative Backfilling).
 *
 * @author Agung
 */
public class UrgentCONS extends CONS {

    public UrgentCONS(Scheduler scheduler) {
        super(scheduler);
    }

    @Override
    public int selectJob() {

        ResourceInfo ri = null;
        // remove jobs, resort jobs via URGENT flags
        boolean sortNeeded = false;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            if (ri.resSchedule.size() > 1 && anyUrgentJobs(ri.resSchedule)) {
                sortNeeded = true;
                Scheduler.schedQueue2.addAll(ri.resSchedule);
                ri.resSchedule.clear();
                ri.stable = false;
                ri.holes.clear();
            }
        }
        if (sortNeeded) {
            Collections.sort(Scheduler.schedQueue2, new UrgentFlagComparator());


            // reinsert jobs using CONS
            for (int i = 0; i < Scheduler.schedQueue2.size(); i++) {
                //System.out.print(((GridletInfo) Scheduler.schedQueue2.get(i)).getUser()+"("+Math.round(((GridletInfo) Scheduler.schedQueue2.get(i)).getPriority())+"),");
                addNewJob((GridletInfo) Scheduler.schedQueue2.get(i));
                //System.out.print(((GridletInfo) Scheduler.schedQueue2.get(i)).getUser()+"("+Math.round(((GridletInfo) Scheduler.schedQueue2.get(i)).getUrgency())+"),");
            }
            //System.out.println("---EOF");
            Scheduler.schedQueue2.clear();
        }
        //System.out.println("Selecting job by CONS...");
        int scheduled = 0;
        for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
            ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);
            if (ri.resSchedule.size() > 0) {
                GridletInfo gi = (GridletInfo) ri.resSchedule.get(0);
                
                if (ri.canExecuteNow(gi)) {
                    ri.removeFirstGI();
                    ri.addGInfoInExec(gi);

                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(ri.resource.getResourceID());
                    // tell the user where to send which gridlet

                    scheduler.submitJob(gi.getGridlet(), ri.resource.getResourceID());

                    ri.is_ready = true;
                    //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                    scheduled++;
                    return scheduled;
                }
            }
        }
        return scheduled;
    }
    
    public boolean anyUrgentJobs(ArrayList<GridletInfo> infos) {
        boolean found = false;
        int i = 1;
        while (!found && i < infos.size()) {
            GridletInfo info = infos.get(i);
            GridletInfo prevInfo = infos.get(i-1);
            if (info.getUrgency() > 0 && prevInfo.getUrgency() == 0)
                found = true;
            i++;
        }
        return found;
    }
}