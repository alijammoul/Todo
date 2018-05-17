package com.todo;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.custom.volley.requester.RequestGenerator;
import com.custom.volley.requester.fields.SimpleRequestFields;
import com.custom.volley.requester.response.NetworkResponseHandler;
import com.custom.volley.requester.types.NetworkMethodTypes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;
    public static final String todoPref = "todoPref";
    public static final String MADE_API_CALL = "madeCall";

    RecyclerView recycler_todo;
    ProgressBar progress;
    List<TodoItem> todoItemList = new ArrayList<>();
    TodoAdapter todoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = (ProgressBar) findViewById(R.id.progress);
        recycler_todo = (RecyclerView) findViewById(R.id.recycler_todo);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_todo.setLayoutManager(layoutManager);
        todoAdapter = new TodoAdapter(todoItemList);
        recycler_todo.setAdapter(todoAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        recycler_todo.addItemDecoration(dividerItemDecoration);

        sharedpreferences = getSharedPreferences(todoPref,
                Context.MODE_PRIVATE);

        if (!sharedpreferences.contains(MADE_API_CALL)) {
            //Make call one time only
            String url = "http://jsonplaceholder.typicode.com/todos";
//            SimpleRequestFields<TodoList> simpleRequestFields = new SimpleRequestFields<>(
//                    NetworkMethodTypes.GET, url, null, null, TodoList.class, "Debug");

            SimpleRequestFields<JsonArray> simpleRequestFields = new SimpleRequestFields<>(
                    NetworkMethodTypes.GET, url, null, null, JsonArray.class, "Debug");
            RequestGenerator.getInstance().getRequesterInitiator().createRequest(simpleRequestFields,
                    new NetworkResponseHandler<JsonArray>() {
                @Override
                public void onSuccess(JsonArray result) {
                    Gson gson = new Gson();
                    //TodoList todoList = gson.fromJson(result, TodoList.class);
                    TodoItem [] items = gson.fromJson(result, TodoItem[].class);

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(MADE_API_CALL, true);
                    editor.apply();

                    todoItemList.addAll(Arrays.asList(items));
                    editViewsVisibility(true, false);
                    todoAdapter.notifyDataSetChanged();

                    new AsyncTask<TodoItem, Void, Void >() {
                        @Override
                        protected Void doInBackground(TodoItem... params) {
                            AppDatabase.getAppDatabase(MainActivity.this).todoDao().insertAll(params);
                            return null;
                        }
                    }.execute(items);
                }
            });

           /* RequestGenerator.getInstance().getRequesterInitiator().createRequest(simpleRequestFields,
                    new NetworkResponseHandler<TodoList>() {
                        @Override
                        public void onSuccess(TodoList result) {
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putBoolean(MADE_API_CALL, true);
                            editor.apply();

                            todoItemList = result.getTodoItemList();
                            editViewsVisibility(true, false);
                            todoAdapter.notifyDataSetChanged();

                            TodoItem [] items = todoItemList.toArray(new TodoItem[result.getTodoItemList().size()]);
                            new AsyncTask<TodoItem, Void, Void >() {
                                @Override
                                protected Void doInBackground(TodoItem... params) {
                                    AppDatabase.getAppDatabase(MainActivity.this).todoDao().insertAll(params);
                                    return null;
                                }
                            }.execute(items);
                        }
            });*/
        } else {
            new AsyncTask<Void, Void, List<TodoItem> >() {
                @Override
                protected List<TodoItem> doInBackground(Void... params) {
                    return AppDatabase.getAppDatabase(MainActivity.this).todoDao().getAllItems();
                }

                @Override
                protected void onPostExecute(List<TodoItem> todoItems) {
                    super.onPostExecute(todoItems);
                    todoItemList.addAll(todoItems);
                    editViewsVisibility(true, false);
                    todoAdapter.notifyDataSetChanged();
                }
            }.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_menu:
                // custom dialog
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog);
                dialog.setTitle("Add item");

                final EditText editText = (EditText) dialog.findViewById(R.id.editText_name);
                Button dialogButton = (Button) dialog.findViewById(R.id.button_add);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String name = editText.getText() == null ? null : editText.getText().toString();

                        if (name != null && !name.isEmpty()) {
                            new AsyncTask<Context, Void, Void >() {
                                @Override
                                protected Void doInBackground(Context... params) {
                                    Context c = params[0];
                                    TodoItem item = new TodoItem(name, 1, false);
                                    todoItemList.add(0, item);
                                    AppDatabase.getAppDatabase(c).todoDao().insertAll(item);
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
                                    todoAdapter.notifyDataSetChanged();
                                }
                            }.execute(MainActivity.this);
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editViewsVisibility(boolean hideProgress, boolean hideRecyclerView) {
        progress.setVisibility(hideProgress ? View.GONE : View.VISIBLE);
        recycler_todo.setVisibility(hideRecyclerView ? View.GONE : View.VISIBLE);
        /*if (!hideRecyclerView) {
            recycler_todo.requestLayout();
        }*/
    }
}
