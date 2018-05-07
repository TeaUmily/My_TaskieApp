package ada.osc.taskie.persistance;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ada.osc.taskie.model.Task;
import ada.osc.taskie.model.TaskPriority;

public class FakeDatabase {

	private List<Task> mTasks;

	public FakeDatabase(){
		mTasks = new ArrayList<>();
	}

	public List<Task> getTasks(){
		return new ArrayList<>(mTasks);
	}


	public Task getTaskById(int id){

		for (Task t:mTasks
			 ) {if(t.getId()==id)
			 	return t;
		}
		return null;
	}

	public void save(Task task){
		mTasks.add(task);
	}

	public void save(List<Task> tasks){
		mTasks.addAll(tasks);
	}

	public void delete(Task task) {
		mTasks.remove(task);
	}

	public void sortHighPriortiyFirst(){
		Collections.sort(this.mTasks, new Comparator<Task>() {
			@Override
			public int compare(Task o1, Task o2) {
				return o1.getPriority().compareTo(o2.getPriority());
			}
		});
	}

	public void sortLowPriorityFirst(){
		sortHighPriortiyFirst();
		Collections.reverse(this.mTasks);
	}


	public List<Task> filterUncompletedTasks(){
		List<Task>  uncompletedTasks= new ArrayList<>();
		for (Task t:mTasks
				) {if(!t.isCompleted()){
			uncompletedTasks.add(t);
			}
		}

		StringBuilder builder= new StringBuilder();
		for (Task t:mTasks
			 ) {if(!t.isCompleted()){
			 	builder.append(t.getTitle()+" ");
				}
			}
		Log.i("TEREZIIJA:",builder.toString());

		return uncompletedTasks;
	}

}