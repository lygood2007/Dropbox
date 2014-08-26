/**
 * File: ThreadBase.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package common;

/**
 * 
 * class ThreadBase
 * Description: ThreadBase should be inherited from by any class that 
 *              wants to use the regular thread routine
 */
abstract public class ThreadBase implements Runnable{
	
	protected boolean _suspended = false;
	protected volatile Thread _t;
	protected String _threadName;
	protected boolean _debug;
	
	/**
	 * _dlog: debug log
	 * @param str: the log string
	 */
	private void _dlog(String str){
		if (_debug)
			System.out.println("[ThreadBase (DEBUG)]:" + str);
	}
	
	/**
	 * _log: general log
	 * @param str: the log string
	 */
	private void _elog(String str){
		System.err.println("[ThreadBase (ERROR)]:" + str);
	}
	
	/**
	 * Constructor
	 * @param name: the name of the thread
	 * @param debug: debug mode?
	 */
	public ThreadBase(String name, boolean debug){
		_threadName = name;
		_dlog("Creating " +  _threadName );
	}
	
	/**
	 * suspend: suspend the thread
	 */
	synchronized void suspend() {
		_suspended = true;
	}
	
	/**
	 * resume: resume the thread
	 */
	synchronized void resume() {
		_suspended = false;
		notify();
	}
	
	/**
	 * stop: stop the thread
	 */
    public void stop(){
    	if (_suspended == true){
    		_elog("Cannot stop when suspending");
    		return;
    	}
    	_t = null;
    }
	
    /**
     * start: start the thread
     */
    public void start(){
    	_dlog("Starting " +  _threadName);
    	if (_t == null)
    	{
    		_t = new Thread (this, _threadName);
    		_t.start ();
    	}
    }
	
    /**
     * join: join the thread
     */
	public void join(){
		if (_t == null){
			return;
		}else
		{
			try {
				_t.join();
				_t = null;
			}catch(InterruptedException e){
				_elog(_threadName + " exits");
			}
		}
	}
	
	/**
	 * clear: sub-classes must provide implementation of clear
	 */
	protected abstract void clear();
	
	/**
	 * state: return the state of the current thread
	 * @return: the state of current thread
	 */
	public Thread.State state(){
		return _t.getState();
	}
	
	/**
	 * terminated: check if the current thread is already terminated
	 * @return: true for terminated, false for still running
	 */
	public boolean terminated(){
		return _t.getState() == Thread.State.TERMINATED;
	}
}