package ada.osc.taskie.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ada.osc.taskie.R;
import ada.osc.taskie.TaskRepository;
import ada.osc.taskie.CategoryRepository;
import ada.osc.taskie.model.Task;
import ada.osc.taskie.model.TaskPriority;
import ada.osc.taskie.util.SharedPrefsUtil;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;


public class NewTaskActivity extends AppCompatActivity {

	public static String EXTRA_TASK_ID ="task id";
	private String ACTION_EDIT_TASK= "edit task action";
	private String ACTION_NEW_TASK = "new task action";

	@BindView(R.id.edittext_newtask_title)	EditText mTitleEntry;
	@BindView(R.id.edittext_newtask_description) EditText mDescriptionEntry;
	@BindView(R.id.spinner_newtask_priority) Spinner mPriorityEntry;
	@BindView(R.id.edittext_newtask_dueDate) EditText mDueDate;
	@BindView(R.id.calendar_button) Button mCalendarBtn;
	@BindView(R.id.category_autoCompletedTextView) AutoCompleteTextView mCategory;
	@BindView(R.id.category_dropdown_icon)
	ImageView mDropDownImage;



	DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	TaskRepository mTaskRepository = TaskRepository.getInstance();
	CategoryRepository mCategoryRepository = CategoryRepository.getInstance();
	Intent intent;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_task);
		ButterKnife.bind(this);
		setUpSpinnerSource();
		setUpCategoryAutoCompletedTV();

		intent = getIntent();
		if(intent.getAction().equals(ACTION_EDIT_TASK)){
			setViewsToTaskValues();
		}
	}

	private void setUpCategoryAutoCompletedTV() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, mCategoryRepository.getAllCategories());
		mCategory.setThreshold(1);
		mCategory.setAdapter(adapter);
	}


	private void setUpSpinnerSource() {

		mPriorityEntry.setAdapter(
				new ArrayAdapter<TaskPriority>(
						this, android.R.layout.simple_list_item_1, getPriorityList()
				)
		);
		mPriorityEntry.setSelection(0);
	}

	private TaskPriority[] getPriorityList() {

		TaskPriority [] priorityArray = TaskPriority.values();
		for(int i = 0 ; i < priorityArray.length; i++){
			if (priorityArray[i].toString().equals(SharedPrefsUtil.getPreferencesField(this, SharedPrefsUtil.TASK_PRIORITY_KEY))){
				priorityArray[i] = priorityArray[0];
				priorityArray[0] = TaskPriority.valueOf(SharedPrefsUtil.getPreferencesField(this, SharedPrefsUtil.TASK_PRIORITY_KEY));
			}
		}
		return priorityArray;
	}


	public void setViewsToTaskValues(){

		String id = intent.getStringExtra(EXTRA_TASK_ID);

		Task task = mTaskRepository.getTaskById(id);

		mTitleEntry.setText(task.getTitle());
		mDescriptionEntry.setText(task.getDescription());
		mPriorityEntry.setSelection(task.getTaskPriorityEnum().ordinal());
		mDueDate.setText(formatter.format(task.getDueDate()));
		mCategory.setText(task.getCategory());

	}



	@OnClick(R.id.imagebutton_newtask_savetask)
	public void saveTask(){

		try{
			checkInput();

			String title = mTitleEntry.getText().toString();
			String description = mDescriptionEntry.getText().toString();
			TaskPriority priority = (TaskPriority) mPriorityEntry.getSelectedItem();
			Date date = formatter.parse(mDueDate.getText().toString());
			String category = mCategory.getText().toString();

			mCategoryRepository.addNewCategory(category);

			SharedPrefsUtil.storePreferencesField(this, SharedPrefsUtil.TASK_PRIORITY_KEY, priority.toString());

			checkDateInput(date);

			if(intent.getAction().equals(ACTION_EDIT_TASK)){

				String id = intent.getStringExtra(EXTRA_TASK_ID);
				Realm realm = Realm.getDefaultInstance();

				realm.beginTransaction();
				mTaskRepository.getTaskById(id).setTitle(title);
				mTaskRepository.getTaskById(id).setDescription(description);
				mTaskRepository.getTaskById(id).setTaskPriorityEnum(priority);
				mTaskRepository.getTaskById(id).setDueDate(date);
				mTaskRepository.getTaskById(id).setCategory(mCategory.getText().toString());
				realm.commitTransaction();

				mTaskRepository.updateTask(mTaskRepository.getTaskById(id));
				finish();
			}

			if(intent.getAction().equals(ACTION_NEW_TASK)){
				Task newTask = new Task(title, description, priority, date, category);
				mTaskRepository.saveTask(newTask);
				finish();
			}

		}catch (EmptyFieldException e){

			Toast toast= Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT);
			toast.show();

		}catch (ParseException e) {

			Toast toast = Toast.makeText(this, "Invalid date format!", Toast.LENGTH_SHORT);
			toast.show();

		}catch (InvalidDateException e){

			Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
			toast.show();

		}
	}

	private void checkDateInput(Date date)throws InvalidDateException {
		Date today = new Date();

		if(date.compareTo(today) < 0){
			throw new InvalidDateException("Invalid date!");
		}
	}


	@OnClick (R.id.calendar_button)
	public void pickDueDate(){

		final Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);

		DatePickerDialog picker = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						mDueDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
					}
				}, year, month, day);
		picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
		picker.show();

	}


	private void checkInput() throws EmptyFieldException {
		if(mCategory.getText().toString().isEmpty() || mTitleEntry.getText().toString().isEmpty() || mDescriptionEntry.getText().toString().isEmpty() || mDueDate.getText().toString().isEmpty()){
			throw new EmptyFieldException("You have left a empty field!");
		}
	}

	@OnClick (R.id.category_dropdown_icon)
	public void onDropDownIconClick(){
		mCategory.showDropDown();
	}



}
