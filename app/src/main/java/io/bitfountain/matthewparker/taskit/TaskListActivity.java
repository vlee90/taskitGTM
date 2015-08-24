package io.bitfountain.matthewparker.taskit;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.DataLayer;
import com.google.android.gms.tagmanager.TagManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class TaskListActivity extends ActionBarActivity {

    private static final String TAG = "TaskListActivity";
    private static final int EDIT_TASK_REQUEST = 10;
    private static final int CREATE_TASK_REQUEST = 20;

    private ArrayList<Task> mTasks;
    private int mLastPositionClicked;
    private TaskAdapter mAdapter;
    private ListView mListView;

    //GTM Start
    private static final String CONTAINER_ID = "GTM-KQDB98";
    private static final int GTM_TIMEOUT = 2;
    //GTM End


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        //GTM Start
        TagManager tagManager = TagManager.getInstance(this);
        tagManager.setVerboseLoggingEnabled(true);

        PendingResult<ContainerHolder> pendingResult = tagManager.loadContainerPreferNonDefault(CONTAINER_ID, R.raw.gtm_kqdb98);
        pendingResult.setResultCallback(new ResultCallback<ContainerHolder>() {
            @Override
            public void onResult(ContainerHolder containerHolder) {
                //On Result is triggered when a saved container is loaded, network container is loaded, or error occurs, or timeout occurs.
                Log.d(TAG, "ContainerHolder Status = " + containerHolder.getStatus().toString());
                if (!containerHolder.getStatus().isSuccess()) {
                    Log.d(TAG, "Container load is not successful");
                    return;
                }


                containerHolder.setContainerAvailableListener(new ContainerHolder.ContainerAvailableListener() {
                    @Override
                    public void onContainerAvailable(ContainerHolder containerHolder, String s) {
                        //Listens for when new container is availiable. Conatiner isn't activie until getContainer() is called.
                        Log.d(TAG, "onContainerAvailable called. Container Version = " + s);
                        Container container = containerHolder.getContainer();
                    }
                });

            }
        }, GTM_TIMEOUT, TimeUnit.SECONDS);
        DataLayer dataLayer = TagManager.getInstance(this).getDataLayer();
        String event = "screen-load";
        Map<String, Object> update = DataLayer.mapOf("screen-name", "Task List Screen");
        Log.d(TAG,"event: + " + event + " Object: " + update);
        dataLayer.pushEvent(event, update);

        //GTM End

        mTasks = new ArrayList<Task>();
        mTasks.add(new Task());
        mTasks.get(0).setName("Task 1");
        mTasks.get(0).setDueDate(new Date());
        mTasks.add(new Task());
        mTasks.get(1).setName("Task 2");
        mTasks.get(1).setDone(true);
        mTasks.add(new Task());
        mTasks.get(2).setName("Task 3");

        mListView = (ListView)findViewById(R.id.task_list);
        mAdapter = new TaskAdapter(mTasks);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLastPositionClicked = position;

                Intent i = new Intent(TaskListActivity.this, TaskActivity.class);
                Task task = (Task) parent.getAdapter().getItem(position);
                i.putExtra(TaskActivity.EXTRA, task);
                startActivityForResult(i, EDIT_TASK_REQUEST);
            }
        });

        mListView.getSelectedItemPosition();
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_task_list_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();
                SparseBooleanArray positions = mListView.getCheckedItemPositions();
                if (id == R.id.delete_task) {
                    for (int i = positions.size() - 1; i >= 0 ; i--){
                        if (positions.valueAt(i)){
                            mTasks.remove(positions.keyAt(i));
                        }
                    }
                    mode.finish();
                    mAdapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case EDIT_TASK_REQUEST:
                if (resultCode == RESULT_OK){
                    Task task = (Task)data.getSerializableExtra(TaskActivity.EXTRA);
                    mTasks.set(mLastPositionClicked, task);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case CREATE_TASK_REQUEST:
                if (resultCode == RESULT_OK){
                    Task task = (Task)data.getSerializableExtra(TaskActivity.EXTRA);
                    mTasks.add(task);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            default:
        }
    }

    private class TaskAdapter extends ArrayAdapter<Task>{
        TaskAdapter(ArrayList<Task> tasks){
            super(TaskListActivity.this, R.layout.task_list_row, R.id.task_item_name, tasks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            Task task = getItem(position);
            TextView taskName = (TextView)convertView.findViewById(R.id.task_item_name);
            taskName.setText(task.getName());

            CheckBox doneBox = (CheckBox)convertView.findViewById(R.id.task_item_done);
            doneBox.setChecked(task.isDone());

            return convertView;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_task) {
            Intent i = new Intent(TaskListActivity.this, TaskActivity.class);
            startActivityForResult(i, CREATE_TASK_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_task_list_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete_task){
           AdapterView.AdapterContextMenuInfo menuInfo =  (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            mTasks.remove(menuInfo.position);
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
