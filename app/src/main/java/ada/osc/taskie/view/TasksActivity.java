package ada.osc.taskie.view;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.List;

import ada.osc.taskie.R;
import ada.osc.taskie.SimpleItemTouchCallback;
import ada.osc.taskie.TaskRepository;
import ada.osc.taskie.model.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class TasksActivity extends AppCompatActivity {

	private static final String TAG = TasksActivity.class.getSimpleName();
	private String ACTION_NEW_TASK = "new task action";
	private String ACTION_EDIT_TASK = "edit task action";


	TaskRepository mRepository = TaskRepository.getInstance();
	TaskAdapter mTaskAdapter;


	@BindView(R.id.fab_tasks_addNew) FloatingActionButton mNewTask;
	@BindView(R.id.recycler_tasks) RecyclerView mTasksRecycler;


	ItemEventListener mEventListener = new ItemEventListener() {
		@Override
		public void onClick(Task task) {
			toastTask(task);
		}

		@Override
		public void onToggleClick(Task task) {
			if(!task.isCompleted()){
				mRepository.updateTaskState(task);
				Log.i("Terezija:", "task <"+task.getTitle()+"> changes state to completed");
			}
			else{
				mRepository.updateTaskState(task);
				Log.i("Terezija:", "task <"+task.getTitle()+"> changes state to uncompleted");
			}
		}

		@Override
		public void onPriorityColorClick(Task task) {
			mRepository.changeTaskPriority(task);
		}


		@Override
		public void onTaskSwipeRight(Task task) {
			displayAlertDialog(task);
		}

		@Override
		public void onTaskSwipeLeft(Task task) {
			startNewTaskActivityForEdit(task);
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasks);

		AlertDialog.Builder ad = new AlertDialog.Builder(this);

		ButterKnife.bind(this);
		setUpRecyclerView();
		updateTasksDisplay();

		initSwipe();
	}


	private void displayAlertDialog(final Task task) {


		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle("Confirm delete");
		ad.setMessage("Are you sure you want to delete this task?");

		ad.setPositiveButton(
				R.string.delete,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						mRepository.removeTask(task.getId());
						updateTasksDisplay();
					}
				}
		);

		ad.setNegativeButton(
				R.string.cancel,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int arg1) {
						updateTasksDisplay();
					}
				}
		);

		ad.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateTasksDisplay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.lowPriority_first_sort: {
				mTaskAdapter.updateTasks(mRepository.getSortedTasksLowPriorityFirst());
				return true;
			}
			case R.id.highPriority_first_sort: {
				mTaskAdapter.updateTasks(mRepository.getSortedTasksHighPriorityFirst());
				return true;
			}
			case R.id.uncompleted_filter: {
				mTaskAdapter.updateTasks(mRepository.filterUncompletedTasks());
				return true;

			}case R.id.all_filter: {
				updateTasksDisplay();
				return true;
			}
			default:return super.onOptionsItemSelected(item);
		}
	}


	private void setUpRecyclerView() {

		int orientation = LinearLayoutManager.VERTICAL;

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
				this,
				orientation,
				false
		);

		RecyclerView.ItemDecoration decoration =
				new DividerItemDecoration(this, orientation);

		RecyclerView.ItemAnimator animator = new DefaultItemAnimator();

		mTaskAdapter = new TaskAdapter(mEventListener);

		mTasksRecycler.setLayoutManager(layoutManager);
		mTasksRecycler.addItemDecoration(decoration);
		mTasksRecycler.setItemAnimator(animator);
		mTasksRecycler.setAdapter(mTaskAdapter);
	}

	private void initSwipe() {
		SimpleItemTouchCallback simpleItemTouchCallback = new SimpleItemTouchCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
		simpleItemTouchCallback.setAdapter(mTaskAdapter);
		simpleItemTouchCallback.setRes(getResources());

		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		itemTouchHelper.attachToRecyclerView(mTasksRecycler);
	}

	private void updateTasksDisplay() {
		List<Task> tasks = mRepository.getTasks();
		mTaskAdapter.updateTasks(tasks);
		for (Task t : tasks){
			Log.d(TAG, t.getTitle());
		}
	}

	private void toastTask(Task task) {
		Toast.makeText(
				this,
				task.getTitle() + "\n" + task.getDescription(),
				Toast.LENGTH_SHORT
		).show();
	}

	@OnClick(R.id.fab_tasks_addNew)
	public void startNewTaskActivity(){
		Intent newTask = new Intent();
		newTask.setAction(ACTION_NEW_TASK);
		newTask.setClass(this, NewTaskActivity.class);
		startActivity(newTask);
	}


	public void startNewTaskActivityForEdit(Task task){
		Intent editTask = new Intent();
		editTask.setAction(ACTION_EDIT_TASK);
		editTask.setClass(this, NewTaskActivity.class);
		editTask.putExtra(NewTaskActivity.EXTRA_TASK_ID, task.getId());
		startActivity(editTask);
	}



}
