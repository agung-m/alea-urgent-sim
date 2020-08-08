package agung.extensions.urgent;

import alea.core.AleaSimTags;
import gridsim.GridSim;
import xklusac.environment.ComplexGridlet;

public class RandomBasedJobInjector implements JobInjector {

	private SxAceJobUtil sxJobUtil;
	//private int numInjects;
	//private int numInjectsNow = 0;
	private float injectProb;
	private RNG injectRand;
	private double lastJobArrival;
	
	public void init(float injectProb, SxAceJobUtil sxJobUtil, RNG rng) {
		//this.numInjects = numInjects;
		this.injectProb = injectProb;
		this.injectRand = rng;
		this.sxJobUtil = sxJobUtil;
	}
	
	@Override
	public int injectJobs(GridSim gridsim, double arrivalTime, int ratingPE, int numJobs) {
		int injected = 0;
		//if (numInjectsNow < numInjects && injectRand.nextFloat() <= injectProb) {
		if (injectRand.nextFloat() <= injectProb) {
			
			ComplexGridlet gl = sxJobUtil.generateUrgentJob(arrivalTime, ratingPE);
			
	        // and set user id to the Scheduler entity - otherwise it would be returned to the JobLoader when completed.
	        //System.out.println(id+" job has limit = "+(job_limit/3600.0)+" queue = "+queue);
	        gl.setUserID(gridsim.getEntityId("Alea_3.0_scheduler"));
	        
			//current_gl++;
			//numInjectsNow++;
			lastJobArrival = arrivalTime;
			//if (gl == null) {
			//	super.sim_schedule(this.getEntityId(this.getEntityName()), 0.0, AleaSimTags.EVENT_WAKE);
			//}
			
			// to synchronize job arrival wrt. the data set.
			double delay = Math.max(0.0, (gl.getArrival_time() - gridsim.clock()));
			// some time is needed to transfer this job to the scheduler, i.e., delay should
			//System.out.println("- Inject urgent job: "+ gl.getGridletID());
			//last_delay = delay;
			gridsim.sim_schedule(gridsim.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, gl);
			
			System.out.println("- Inject urgent job #"+ gl.getGridletID() + " at " + gl.getArrival_time() 
				+ ", numPE = " + gl.getNumPE() + ", length = " + gl.getGridletLength());

			//delay = Math.max(0.0, (gl.getArrival_time() - super.clock()));
			//if (current_gl < total_jobs) {
				// use delay - next job will be loaded after the simulation time is equal to the
				// previous job arrival.
			//	super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
			//}
			injected++;
		}
		return injected;
	}

	// Use singleton for the default access
	private static RandomBasedJobInjector instance;
	
	public static RandomBasedJobInjector getInstance() {
		if(instance == null){
            instance = new RandomBasedJobInjector();
        }
        return instance;
	}


	@Override
	public int getTotalNumInjects() {
		return -1;
	}


	@Override
	public double getLastJobArrival() {
		return lastJobArrival;
	}

	@Override
	public boolean isFinished() {
		// Always finished because it depends on the job traces
		return true;
	}
}
