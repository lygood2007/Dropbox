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
	
	private void _dlog(String str){
		if(_debug)
			System.out.println("[ThreadBase (DEBUG)]:" + str);
	}
	
	private void _elog(String str){
		System.err.println("[ThreadBase (ERROR)]:" + str);
	}
	
	/*private void _log(String str){
		System.out.println("[ThreadBase:"+_threadName+"]:" + str);
	}*/
	
	public ThreadBase(String name, boolean debug){
		_threadName = name;
		_dlog("Creating " +  _threadName );
	}
	
	synchronized void suspend() {
		_suspended = true;
	}
	
	synchronized void resume() {
		_suspended = false;
		notify();
	}
	
    public void stop(){
    	if(_suspended == true){
    		_elog("Cannot stop when suspending");
    		return;
    	}
    	_t = null;
    }
	   
    public void start(){
    	_dlog("Starting " +  _threadName);
    	if (_t == null)
    	{
    		_t = new Thread (this, _threadName);
    		_t.start ();
    	}
    }
	
	public void join(){
		if(_t == null){
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
	
	protected abstract void clear();
	
	public Thread.State state(){
		return _t.getState();
	}
	
	public boolean terminated(){
		return _t.getState() == Thread.State.TERMINATED;
	}
}